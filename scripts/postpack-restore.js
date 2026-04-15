/**
 * Restores the original package.json after `npm pack` / `npm publish`
 * by copying package.json.backup over package.json.
 * The backup file is intentionally kept to aid transparency and is ignored
 * by git and npm via .gitignore / .npmignore.
 */

const fs = require("node:fs");
const path = require("node:path");

const PACKAGE_JSON = path.resolve(process.cwd(), "package.json");
const BACKUP_FILE = path.resolve(process.cwd(), "package.json.backup");

if (fs.existsSync(BACKUP_FILE)) {
  // overwrite the modified package.json with the original content
  fs.copyFileSync(BACKUP_FILE, PACKAGE_JSON);
}

["LICENSE", "NOTICE", "THIRD_PARTY_NOTICES"].forEach((licFile) => {
  fs.rm(path.resolve(process.cwd(), licFile), (err) => {
    if (err) {
      console.error("Error removing file:", err);
    }
  });
});

fs.rmSync('./licenses', { recursive: true, force: true });
