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
import { addCommentToDeclaration } from "../../handlers/addCommentToDeclaration.js";
import { matchLineEndings } from "../../handlers/lineEndings.js";
import { renamedProperty } from "../../handlers/renamedProperty.js";

export const A12K_3009: Recipe = {
    metadata: {
        id: "v30/A12K-3009",
        description: "Rename getInstanceV2 -> getInstance, deprecate V2 factory methods",
        supportedVersions: "^30.0.0"
    },
    execute(project) {
        for (const sourceFile of project.getSourceFiles()) {
            matchLineEndings(sourceFile);
            renamedProperty(sourceFile, {
                className: "CustomConditionService",
                methodName: "getInstanceV2",
                newName: "getInstance"
            });
            renamedProperty(sourceFile, {
                className: "CustomFieldTypeService",
                methodName: "getInstanceV2",
                newName: "getInstance"
            });
            addComment(sourceFile, {
                methodName: "createCustomConditionV2",
                comments: [" FIXME: Renamed, use `createCustomCondition` instead"]
            });
            addCommentToDeclaration(sourceFile, {
                className: "ICustomConditionFactory",
                methodName: "createCustomConditionV2",
                comments: [
                    " FIXME: This method is deprecated, rename the method to `createCustomCondition`"
                ]
            });
            addComment(sourceFile, {
                methodName: "createCustomFieldTypeV2",
                comments: [" FIXME: Renamed, use `createCustomFieldType` instead"]
            });
            addCommentToDeclaration(sourceFile, {
                className: "ICustomFieldTypeFactory",
                methodName: "createCustomFieldTypeV2",
                comments: [
                    " FIXME: This method is deprecated, rename the method to `createCustomFieldType`"
                ]
            });
        }
    }
};
