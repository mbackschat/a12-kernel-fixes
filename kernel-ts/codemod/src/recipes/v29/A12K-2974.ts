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

export const A12K_2974: Recipe = {
    metadata: {
        id: "v29/A12K-2974",
        description:
            "Deprecate getSupportedConditionNames/createCustomCondition, introduce createCustomConditionV2",
        supportedVersions: "^29.2.0"
    },
    execute(project) {
        for (const sourceFile of project.getSourceFiles()) {
            matchLineEndings(sourceFile);
            addComment(sourceFile, {
                methodName: "getSupportedConditionNames",
                comments: [" FIXME: Deprecated, this method is now obsolete"]
            });
            addCommentToImplementOptionalMethod(sourceFile, {
                className: "ICustomConditionFactory",
                methodName: "createCustomConditionV2",
                comments: [" FIXME: Implement the new optional method createCustomConditionV2()"]
            });
            addCommentToDeclaration(sourceFile, {
                className: "ICustomConditionFactory",
                methodName: "getSupportedConditionNames",
                comments: [
                    " FIXME: This method is deprecated and should no longer be implemented"
                ]
            });
            addComment(sourceFile, {
                methodName: "createCustomCondition",
                comments: [" FIXME: Deprecated, use `createCustomConditionV2` instead"]
            });
            addCommentToDeclaration(sourceFile, {
                className: "ICustomConditionFactory",
                methodName: "createCustomCondition",
                comments: [
                    " FIXME: This method is deprecated, implement `createCustomConditionV2` instead"
                ]
            });
        }
    }
};
