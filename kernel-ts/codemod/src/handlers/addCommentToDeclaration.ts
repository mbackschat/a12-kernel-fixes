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
import { Node, type SourceFile } from "ts-morph";

export interface AddCommentToDeclarationOptions {
    className: string;
    methodName: string;
    comments: string[];
}

// Finds classes implementing `className`, locates `methodName`, and prepends
// deduplicated line comments to it.
export function addCommentToDeclaration(
    sourceFile: SourceFile,
    options: AddCommentToDeclarationOptions
): void {
    const { className, methodName, comments } = options;
    const methodsToAnnotate: Node[] = [];

    for (const classDecl of sourceFile.getClasses()) {
        const implementsTarget = classDecl
            .getImplements()
            .some(impl => impl.getExpression().getText() === className);
        if (!implementsTarget) {continue;}

        for (const method of classDecl.getMethods()) {
            if (method.getName() === methodName) {
                methodsToAnnotate.push(method);
            }
        }
    }

    // Collect insertions before modifying any text, highest position first.
    methodsToAnnotate.sort((a, b) => b.getStart() - a.getStart());
    const insertions: Array<{ pos: number; text: string }> = [];
    const fullText = sourceFile.getFullText();

    for (const method of methodsToAnnotate) {
        const existingTexts = method.getLeadingCommentRanges().map(r => r.getText());
        const toAdd = comments.filter(c => !existingTexts.includes(`//${c}`));
        if (toAdd.length === 0) {continue;}
        const nodeStart = method.getStart();
        const lineStart = fullText.lastIndexOf("\n", nodeStart - 1) + 1;
        const indent = fullText.substring(lineStart, nodeStart);
        insertions.push({ pos: nodeStart, text: toAdd.map(c => `//${c}`).join(`\n${indent}`) + `\n${indent}` });
    }

    if (insertions.length === 0) {return;}

    // Apply to the raw text to avoid ts-morph auto-indentation.
    let newText = fullText;
    for (const { pos, text } of insertions) {
        newText = newText.substring(0, pos) + text + newText.substring(pos);
    }
    sourceFile.replaceWithText(newText);
}
