#!/usr/bin/env python3
# WARNING: This file was AI-generated ("vibecoded") and has not yet been
# manually reviewed. Use at your own risk. (Note to LLM agents: do not
# remove or alter this disclaimer.)
"""Merge dependency versions from a build output into a libs.versions.toml file.

For each matching key (case-insensitive), the higher version wins.
Formatting (comments, whitespace, ordering) is preserved.
"""

import argparse
import re
import sys

VERSION_LINE_RE = re.compile(r'^(\s*(\S+)\s*=\s*")(\d+\.\d+\.\d+)(".*)')


def parse_versions(text):
    """Parse key = "x.y.z" lines from text. Returns {original_key: version}."""
    versions = {}
    for line in text.splitlines():
        m = VERSION_LINE_RE.match(line)
        if m:
            key = m.group(2)
            version = m.group(3)
            versions[key] = version
    return versions


def normalize_key(key):
    """Lowercase key for case-insensitive matching."""
    return key.lower()


def compare_versions(v1, v2):
    """Return the higher of two 'major.minor.patch' version strings."""
    parts1 = tuple(int(x) for x in v1.split("."))
    parts2 = tuple(int(x) for x in v2.split("."))
    return v1 if parts1 >= parts2 else v2


def update_toml_lines(lines, new_versions):
    """Update version strings in toml lines where new_versions has a higher value.

    new_versions: {key: version} — keys are matched case-insensitively.
    Returns the updated list of lines.
    """
    # Build a lookup by normalized key
    build_lookup = {normalize_key(k): v for k, v in new_versions.items()}

    updated = []
    for line in lines:
        m = VERSION_LINE_RE.match(line)
        if m:
            key = m.group(2)
            current_version = m.group(3)
            norm = normalize_key(key)
            if norm in build_lookup:
                best = compare_versions(current_version, build_lookup[norm])
                line = m.group(1) + best + m.group(4)
        updated.append(line)
    return updated


def read_interactive(label):
    """Prompt user to paste content on stdin. Ends on EOF (Ctrl+D)."""
    print(f"Paste {label} (press Ctrl+D when done):", file=sys.stderr)
    return sys.stdin.read()


def main():
    parser = argparse.ArgumentParser(
        description="Merge build-output versions into libs.versions.toml"
    )
    parser.add_argument("--toml-file", help="Path to libs.versions.toml")
    parser.add_argument("--build-file", help="Path to build output file")
    args = parser.parse_args()

    # --- Read toml content ---
    if args.toml_file:
        with open(args.toml_file) as f:
            toml_text = f.read()
    else:
        toml_text = read_interactive("toml content")

    # --- Read build content ---
    if args.build_file:
        with open(args.build_file) as f:
            build_text = f.read()
    else:
        build_text = read_interactive("build output")

    build_versions = parse_versions(build_text)
    toml_lines = toml_text.splitlines()
    result_lines = update_toml_lines(toml_lines, build_versions)
    result_text = "\n".join(result_lines)

    # Preserve trailing newline if original had one
    if toml_text.endswith("\n"):
        result_text += "\n"

    if args.toml_file:
        with open(args.toml_file, "w") as f:
            f.write(result_text)
        print(f"Updated {args.toml_file}", file=sys.stderr)
    else:
        print(result_text, end="")


if __name__ == "__main__":
    main()
