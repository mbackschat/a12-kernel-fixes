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
import { addCommentToImplementOptionalMethod } from "../../handlers/addCommentToImplementOptionalMethod.js";
import { matchLineEndings } from "../../handlers/lineEndings.js";

export const A12K_2938: Recipe = {
    metadata: {
        id: "v29/A12K-2938",
        description:
            "Deprecate custom field type/condition service methods, introduce V2 variants",
        supportedVersions: "^29.2.0"
    },
    execute(project) {
        for (const sourceFile of project.getSourceFiles()) {
            matchLineEndings(sourceFile);
            addCommentToImplementOptionalMethod(sourceFile, {
                className: "ICustomFieldTypeFactory",
                methodName: "createCustomFieldTypeV2",
                comments: [" FIXME: Implement the new optional method createCustomFieldTypeV2()"]
            });
            addComment(sourceFile, {
                methodName: "getRegisteredTypeNames",
                comments: [" FIXME: This method is deprecated and should no longer be used"]
            });
            addCommentToDeclaration(sourceFile, {
                className: "ICustomFieldTypeService",
                methodName: "getRegisteredTypeNames",
                comments: [
                    " FIXME: This method is deprecated and should no longer be implemented"
                ]
            });
            addCommentToDeclaration(sourceFile, {
                className: "ICustomFieldTypeFactory",
                methodName: "getSupportedTypeNames",
                comments: [
                    " FIXME: getSupportedTypeNames() should no longer be used (returns an empty set) ... "
                ]
            });
            addComment(sourceFile, {
                methodName: "getSupportedTypeNames",
                comments: [" FIXME: This method is deprecated and should no longer be used"]
            });
            addComment(sourceFile, {
                methodName: "createCustomFieldType",
                comments: [
                    " FIXME: Implement the new optional method createCustomFieldTypeV2() - then createCustomFieldType() should no longer be used (throws an Error) ... "
                ]
            });
            addCommentToDeclaration(sourceFile, {
                className: "ICustomFieldTypeFactory",
                methodName: "createCustomFieldType",
                comments: [
                    " FIXME: This method is deprecated, implement `createCustomFieldTypeV2` instead"
                ]
            });
            addComment(sourceFile, {
                methodName: "getSupportedConditionNames",
                comments: [" FIXME: This method is deprecated and should no longer be used"]
            });
            addComment(sourceFile, {
                methodName: "getRegisteredConditionNames",
                comments: [
                    " FIXME: Change implementation of the method getCustomCondition() - then getRegisteredConditionNames() should no longer be used (returns an empty set) ... "
                ]
            });
            addCommentToDeclaration(sourceFile, {
                className: "ICustomConditionService",
                methodName: "getRegisteredConditionNames",
                comments: [
                    " FIXME: This method is deprecated and should no longer be implemented"
                ]
            });
        }
    }
};
