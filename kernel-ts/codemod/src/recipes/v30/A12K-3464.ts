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

import { addComment } from "../../handlers/addComment.js";
import { applyImportMigrations } from "../../handlers/importMigration.js";
import { matchLineEndings } from "../../handlers/lineEndings.js";
import { renamedIdentifier } from "../../handlers/renamedIdentifier.js";

const PKG = "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

export const A12K_3464: Recipe = {
    metadata: {
        id: "v30/A12K-3464",
        description: "Rename ICustomFieldType -> ICustomFieldValidator, deprecate extension services",
        supportedVersions: "^30.6.0"
    },
    execute(project) {
        for (const sourceFile of project.getSourceFiles()) {
            matchLineEndings(sourceFile);
            // Migrate imports first, then rename remaining usages.
            applyImportMigrations(sourceFile, {
                entityMigrations: [
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js//index.js", entity: "ICustomFieldType" },
                        to: { packageName: PKG, subPath: "/lib/main/js//index.js", entity: "ICustomFieldValidator" }
                    },
                    {
                        from: { packageName: PKG, subPath: "/lib/main/js/custom/ICustomFieldType.js", entity: "ICustomFieldType" },
                        to: { packageName: PKG, subPath: "/lib/main/js/custom/ICustomFieldValidator.js", entity: "ICustomFieldValidator" }
                    }
                ]
            });
            renamedIdentifier(sourceFile, { oldName: "ICustomFieldType", newName: "ICustomFieldValidator" });
            addComment(sourceFile, {
                className: "RtCustomExtensionService",
                methodName: "registerCustomFieldTypes",
                comments: [
                    " FIXME: This service is deprecated. Provide the custom field types to the Kernel APIs directly instead (see e.g. GeneratedCodeRtConfig.customFieldTypeFactory)."
                ]
            });
            addComment(sourceFile, {
                className: "RtCustomExtensionService",
                methodName: "registerCustomConditions",
                comments: [
                    " FIXME: This service is deprecated. Provide the custom conditions to the validation call directly instead (see GeneratedCodeRtConfig.customConditionFactory or ValidationMode.customConditionFactory)."
                ]
            });
            addComment(sourceFile, {
                className: "DocumentRtCustomExtensionService",
                methodName: "registerCustomFieldTypes",
                comments: [
                    " FIXME: This service is deprecated. Provide the custom field types to the Kernel APIs directly instead (see GeneratedCodeRtConfig.customFieldTypeFactory)."
                ]
            });
            addComment(sourceFile, {
                className: "DocumentRtCustomExtensionService",
                methodName: "registerCustomConditions",
                comments: [
                    " FIXME: This service is deprecated. Provide the custom conditions to the validation call directly instead (see GeneratedCodeRtConfig.customConditionFactory)."
                ]
            });
            addComment(sourceFile, {
                className: "CustomFieldTypeService",
                methodName: "getInstance",
                comments: [
                    " FIXME: This service is deprecated. Provide the custom field types to the Kernel APIs directly instead (see e.g. GeneratedCodeRtConfig.customFieldTypeFactory)."
                ]
            });
            addComment(sourceFile, {
                className: "CustomConditionService",
                methodName: "getInstance",
                comments: [
                    " FIXME: This service is deprecated. Provide the custom conditions to the validation call directly instead (see GeneratedCodeRtConfig.customConditionFactory or ValidationMode.customConditionFactory)."
                ]
            });
        }
    }
};
