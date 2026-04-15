/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
*/

import Path from "path";
import Process from "process";
import fs from "fs";

import { run } from "jscodeshift/src/Runner";
import Yargs from "yargs";

const cwd = Process.cwd();

const targetVersions = fs
  .readdirSync(Path.resolve(__dirname, "transforms"), { withFileTypes: true })
  .filter((dirent) => dirent.isDirectory())
  .map((dirent) => dirent.name.replace(/^version-/, ""));

const argv = Yargs.usage(
  "Usage: $0 <source-dir-path> [options]\n\n" +
  `Note that the tool is only able to migrate code from one major version to the next major one. E.g, if you are using Kernel 28.x, you can only migrate to 29.x.`,
)
  .describe("help", "Show help")
  .help("h")
  .demandCommand(1, "Please specify the path to target source directory")
  .option({
    targetVersion: {
      demandOption: true,
      describe: "The target Kernel version is migrating to",
      alias: "t",
      string: true,
      choices: targetVersions,
    },
  })
  .locale("en")
  .parseSync();

async function main() {
  const path = String(argv._?.[0]);

  const absolutePath = Path.resolve(cwd, path);

  await run(
    Path.join(__dirname, "transforms", `version-${argv.targetVersion}`, "transform.js"),
    [absolutePath],
    {
      extensions: "ts,tsx",
      parser: "tsx",
      ignorePattern: "**/node_modules/**",
      verbose: 1,
    },
  );
}

main().catch((error) => {
  // eslint-disable-next-line no-console
  console.error(error);
  process.exit(1);
});
