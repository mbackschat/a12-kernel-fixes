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
import type { Recipe } from "@com.mgmtp.a12.devtools/codemod";

import { applyImportMigrations } from "../../handlers/importMigration.js";
import { matchLineEndings } from "../../handlers/lineEndings.js";

function preferTopLevel(pkg: string) {
    return {
        pathMigrations: [
            { from: `${pkg}/**/a12internal/**`, to: `${pkg}/a12internal`, exclude: `${pkg}/a12internal` },
            { from: `${pkg}/**/internal/**`, to: `${pkg}/internal`, exclude: `${pkg}/internal` },
            { from: `${pkg}/lib/**`, to: pkg, exclude: `${pkg}/lib/main/resources/**` }
        ]
    };
}

export const A12K_3928: Recipe = {
    metadata: {
        id: "v30/A12K-3928",
        description:
            "Prefer top-level imports for kernel packages; rename kernel-core-runtime-api-ts to kernel-md-facade",
        supportedVersions: "^30.7.0"
    },
    execute(project) {
        for (const sourceFile of project.getSourceFiles()) {
            matchLineEndings(sourceFile);
            applyImportMigrations(sourceFile, preferTopLevel("@com.mgmtp.a12.kernel/kernel-md-facade"));
            applyImportMigrations(sourceFile, preferTopLevel("@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts"));
            applyImportMigrations(sourceFile, preferTopLevel("@com.mgmtp.a12.kernel/kernel-core-customfieldtype-impl"));
            applyImportMigrations(sourceFile, preferTopLevel("@com.mgmtp.a12.kernel/kernel-core-parser-web"));
            applyImportMigrations(sourceFile, {
                pathMigrations: [
                    {
                        from: "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts",
                        to: "@com.mgmtp.a12.kernel/kernel-md-facade"
                    }
                ]
            });
            applyImportMigrations(sourceFile, {
                pathMigrations: [
                    {
                        from: "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal",
                        to: "@com.mgmtp.a12.kernel/kernel-md-facade/a12internal"
                    }
                ]
            });
            applyImportMigrations(sourceFile, {
                pathMigrations: [
                    {
                        from: "@com.mgmtp.a12.kernel/kernel-core-parser-web/lib/main/resources/validation_language_tooltip_de.json",
                        to: "@com.mgmtp.a12.kernel/kernel-core-parser-web/resources/validation_language_tooltip_de.json"
                    }
                ]
            });
            applyImportMigrations(sourceFile, {
                pathMigrations: [
                    {
                        from: "@com.mgmtp.a12.kernel/kernel-core-parser-web/lib/main/resources/validation_language_tooltip_en.json",
                        to: "@com.mgmtp.a12.kernel/kernel-core-parser-web/resources/validation_language_tooltip_en.json"
                    }
                ]
            });
        }
    }
};
