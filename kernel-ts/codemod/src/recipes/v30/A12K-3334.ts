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

const PKG = "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

export const A12K_3334: Recipe = {
    metadata: {
        id: "v30/A12K-3334",
        description: "Move error/localization entities from a12internal paths to public API",
        supportedVersions: "^30.0.0"
    },
    execute(project) {
        for (const sourceFile of project.getSourceFiles()) {
            matchLineEndings(sourceFile);
            applyImportMigrations(sourceFile, {
                entityMigrations: [
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal", entity: "defaultResourceProvider" },
                        to: { packageName: PKG, subPath: "", entity: "defaultResourceProvider" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal/formatdef/error/localization", entity: "defaultResourceProvider" },
                        to: { packageName: PKG, subPath: "/lib/main/js/formatdef/error/localization", entity: "defaultResourceProvider" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal", entity: "ErrorLocalizationBundle" },
                        to: { packageName: PKG, subPath: "", entity: "ErrorLocalizationBundle" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal/formatdef/error/localization", entity: "ErrorLocalizationBundle" },
                        to: { packageName: PKG, subPath: "/lib/main/js/formatdef/error/localization", entity: "ErrorLocalizationBundle" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal", entity: "StaticResourceProvider" },
                        to: { packageName: PKG, subPath: "", entity: "StaticResourceProvider" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal/formatdef/error/localization", entity: "StaticResourceProvider" },
                        to: { packageName: PKG, subPath: "/lib/main/js/formatdef/error/localization", entity: "StaticResourceProvider" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal", entity: "DIVISION_BY_ZERO_INVALID" },
                        to: { packageName: PKG, subPath: "", entity: "DIVISION_BY_ZERO_INVALID" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal/formatdef/error/keys", entity: "DIVISION_BY_ZERO_INVALID" },
                        to: { packageName: PKG, subPath: "/lib/main/js/formatdef/error/keys", entity: "DIVISION_BY_ZERO_INVALID" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal", entity: "ERROR_DURING_DATA_CHECK_EVAL" },
                        to: { packageName: PKG, subPath: "", entity: "ERROR_DURING_DATA_CHECK_EVAL" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal/formatdef/error/keys", entity: "ERROR_DURING_DATA_CHECK_EVAL" },
                        to: { packageName: PKG, subPath: "/lib/main/js/formatdef/error/keys", entity: "ERROR_DURING_DATA_CHECK_EVAL" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal", entity: "RuntimeFormalErrorEnum" },
                        to: { packageName: PKG, subPath: "", entity: "RuntimeFormalErrorEnum" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal/formatdef/error/keys", entity: "RuntimeFormalErrorEnum" },
                        to: { packageName: PKG, subPath: "/lib/main/js/formatdef/error/keys", entity: "RuntimeFormalErrorEnum" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal", entity: "de" },
                        to: { packageName: PKG, subPath: "", entity: "de" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal/formatdef/error/bundles", entity: "de" },
                        to: { packageName: PKG, subPath: "/lib/main/js/formatdef/error/bundles", entity: "de" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal", entity: "en" },
                        to: { packageName: PKG, subPath: "", entity: "en" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal/formatdef/error/bundles", entity: "en" },
                        to: { packageName: PKG, subPath: "/lib/main/js/formatdef/error/bundles", entity: "en" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal", entity: "fr" },
                        to: { packageName: PKG, subPath: "", entity: "fr" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal/formatdef/error/bundles", entity: "fr" },
                        to: { packageName: PKG, subPath: "/lib/main/js/formatdef/error/bundles", entity: "fr" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal", entity: "nl" },
                        to: { packageName: PKG, subPath: "", entity: "nl" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/a12internal/formatdef/error/bundles", entity: "nl" },
                        to: { packageName: PKG, subPath: "/lib/main/js/formatdef/error/bundles", entity: "nl" }
                    }
                ]
            });
        }
    }
};
