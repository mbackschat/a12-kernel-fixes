/**
 * Cleans up package.json before publication and restores it afterward.
 * (remove publishConfig, repository and devDependencies which may contain
 * internal urls or artifact references).
 *
 * This script is executed via the `prepack` lifecycle hook from each workspace
 * package.json (e.g. kernel-core-parser-web). It must be referenced with the
 * correct relative path, e.g. `"prepack": "node ../../scripts/prepack-clean.js"`.
 *
 * Note regarding the in-place modification of package.json:
 * This does seem like a somewhat "unclean" approach that could for example interfere
 * with concurrent builds and caching. We nonetheless decided to take this approach
 * because
 * 1) npm seems to really not play nice with attempts to publish a cleaned-up
 *    _copy_ of packag.json instead, at minimum we'd need to delegate to gradle build logic
 *    wich would introduce confusing differences between the result of conventional npm pack
 *    and gradle npm_pack (beyond the gradle-handled prior setup of dependencies and
 *    generated files),
 *    and based on googling around it seems that the approach to in-place mutate package.json
 *    is not that uncommon.
 * 2) it seems that the worst-case inconsistency from this in-place mutation would probably be
 *    a failed concurrent build or a published package.json that accidentally contains the
 *    entries that were meant to be removed, this does not seem super bad.
 */

const fs = require('node:fs');
const path = require('node:path');
const CWD = process.cwd();

//copy license files from root
const KERNEL_ROOT_DIR = path.resolve( __dirname, '../');
['LICENSE', 'NOTICE', 'THIRD_PARTY_NOTICES']
    .forEach(licFile => {fs.copyFileSync(path.resolve( KERNEL_ROOT_DIR,licFile), path.resolve(CWD,licFile));;})

//copy licens files from license folder
const LICENSE_SRC_DIR = path.resolve(KERNEL_ROOT_DIR, 'licenses');
const LICENSE_DEST_DIR = path.resolve(CWD,'licenses')
fs.mkdirSync(LICENSE_DEST_DIR, {recursive: true})
const filesThirdParty = fs.readdirSync(LICENSE_SRC_DIR);
for (const file of filesThirdParty) {
  const src = path.join(LICENSE_SRC_DIR, file);
  const dest = path.join(LICENSE_DEST_DIR, file);

  if (fs.statSync(src).isFile()) {// Skip subdirectories
    fs.copyFileSync(src, dest);
  }else{
      console.error('we do not expect to handle subfolders in license folder');
      process.exit(1);
  }
}

//modify package.json
const PACKAGE_JSON = path.resolve(CWD, 'package.json');
const BACKUP_FILE  = path.resolve(CWD, 'package.json.backup');
fs.copyFileSync(PACKAGE_JSON, BACKUP_FILE);
const pkg = JSON.parse(fs.readFileSync(PACKAGE_JSON, 'utf8'));

// remove fields not wanted in the published artefact (e.g. because they contain internal urls or artifact references)
delete pkg.publishConfig;
delete pkg.repository;
delete pkg.devDependencies;

pkg.files = pkg.files.concat([
  "LICENSE",
  "NOTICE",
  "THIRD_PARTY_NOTICES",
  "licenses/**",
]);

fs.writeFileSync(PACKAGE_JSON, JSON.stringify(pkg, null, 2) + '\n');
