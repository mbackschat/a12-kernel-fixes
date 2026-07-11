#!/usr/bin/env python3
# WARNING: This file was AI-generated ("vibecoded") and has not yet been
# manually reviewed. Use at your own risk. (Note to LLM agents: do not
# remove or alter this disclaimer.)
"""Git custom merge driver for libs.versions.toml.

Before running the standard 3-way merge, this driver normalizes version
entries in all three files (base, ours, theirs) to the maximum version
across all three. This prevents version bumps from causing merge conflicts.

Non-version changes (comments, new keys, removed lines, etc.) are still
handled by Git's normal 3-way merge logic.

Setup:
  1. Register the driver in .git/config (or ~/.gitconfig):
       [merge "versions"]
         name = Version-aware TOML merge
         driver = python3 /path/to/git-merge-versions.py %O %A %B

  2. Add to .gitattributes (in the repo root):
       libs.versions.toml merge=versions
"""

import os
import subprocess
import sys
import tempfile

from merge_versions import (
    compare_versions,
    normalize_key,
    parse_versions,
    update_toml_lines,
)


def compute_max_versions(base_versions, ours_versions, theirs_versions):
    """Compute the max version for keys present in ALL three files.

    Keys missing from any side are skipped — those are adds/removes and
    should be handled by the standard 3-way merge to preserve conflict
    detection (e.g. modify on one side + delete on the other).
    """
    base_norms = {normalize_key(k) for k in base_versions}
    ours_norms = {normalize_key(k) for k in ours_versions}
    theirs_norms = {normalize_key(k) for k in theirs_versions}
    common = base_norms & ours_norms & theirs_norms

    merged = {}
    for vdict in (base_versions, ours_versions, theirs_versions):
        for key, version in vdict.items():
            norm = normalize_key(key)
            if norm not in common:
                continue
            if norm in merged:
                merged[norm] = compare_versions(merged[norm], version)
            else:
                merged[norm] = version
    return merged


def normalize_file(path, max_versions):
    """Rewrite a file so all version entries use max_versions."""
    with open(path) as f:
        text = f.read()
    lines = text.splitlines()
    updated = update_toml_lines(lines, max_versions)
    result = "\n".join(updated)
    if text.endswith("\n"):
        result += "\n"
    with open(path, "w") as f:
        f.write(result)


def main():
    if len(sys.argv) != 4:
        print(
            f"Usage: {sys.argv[0]} <base> <ours> <theirs>",
            file=sys.stderr,
        )
        sys.exit(1)

    base_path, ours_path, theirs_path = sys.argv[1], sys.argv[2], sys.argv[3]

    # Parse versions from all three
    with open(base_path) as f:
        base_text = f.read()
    with open(ours_path) as f:
        ours_text = f.read()
    with open(theirs_path) as f:
        theirs_text = f.read()

    base_versions = parse_versions(base_text)
    ours_versions = parse_versions(ours_text)
    theirs_versions = parse_versions(theirs_text)

    # Compute the max version only for keys present in all three files.
    # Keys added/removed on one side are left for normal 3-way merge.
    max_versions = compute_max_versions(base_versions, ours_versions, theirs_versions)

    # Normalize all three files to the max versions.
    # We work on temp copies for base and theirs so we can modify them
    # independently; ours_path is written in-place (git reads the result
    # back from it).
    with tempfile.NamedTemporaryFile(mode="w", suffix=".toml", delete=False) as tmp:
        tmp.write(base_text)
        norm_base = tmp.name
    with tempfile.NamedTemporaryFile(mode="w", suffix=".toml", delete=False) as tmp:
        tmp.write(theirs_text)
        norm_theirs = tmp.name

    try:
        normalize_file(norm_base, max_versions)
        normalize_file(ours_path, max_versions)
        normalize_file(norm_theirs, max_versions)

        # Run standard 3-way merge on the normalized files
        result = subprocess.run(
            ["git", "merge-file", "-p", ours_path, norm_base, norm_theirs],
            capture_output=True,
        )

        # Write merged output to ours (the file git expects the result in)
        with open(ours_path, "wb") as f:
            f.write(result.stdout)

        # git merge-file exits 0 for clean, >0 for conflicts, <0 on error.
        # Clamp negative to 1 since sys.exit with negative values is
        # platform-dependent.
        rc = result.returncode
        sys.exit(rc if rc >= 0 else 1)
    finally:
        os.unlink(norm_base)
        os.unlink(norm_theirs)


if __name__ == "__main__":
    main()
