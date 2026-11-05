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
import { Node, SyntaxKind, type SourceFile } from "ts-morph";

export interface AddCommentToImplementOptionalMethodOptions {
    className: string;
    methodName: string;
    comments: string[];
}

interface Insertion {
    pos: number;
    text: string;
}

// Unlike addCommentToDeclaration, this attaches the comment when the method is
// ABSENT. It's a reminder to implement the new optional method.
export function addCommentToImplementOptionalMethod(
    sourceFile: SourceFile,
    options: AddCommentToImplementOptionalMethodOptions
): void {
    const { className, methodName, comments } = options;
    const insertions: Insertion[] = [];

    for (const classDecl of sourceFile.getClasses()) {
        const implementsTarget = classDecl
            .getImplements()
            .some(impl => impl.getExpression().getText() === className);
        if (!implementsTarget) { continue; }

        const members = classDecl.getMembers();

        if (members.length === 0) {
            // Empty class body, insert the comments between the braces.
            const openBrace = classDecl.getFirstChildByKind(SyntaxKind.OpenBraceToken);
            const closeBrace = classDecl.getLastChildByKind(SyntaxKind.CloseBraceToken);
            if (!openBrace || !closeBrace) { continue; }

            const bodyText = sourceFile
                .getFullText()
                .slice(openBrace.getEnd(), closeBrace.getStart());
            const toAdd = comments.filter(c => !bodyText.includes(`//${c}`));
            if (toAdd.length === 0) { continue; }

            const classIndent = classDecl.getIndentationText();
            const memberIndent = classIndent + "    ";
            const text =
                "\n" +
                toAdd.map(c => `${memberIndent}//${c}`).join("\n") +
                "\n" +
                classIndent;
            insertions.push({ pos: openBrace.getEnd(), text });
            continue;
        }

        // Only act when every member is a method and none is named `methodName`;
        // any non-method member (property, ctor, getter, ...) skips the class.
        const allMethodsNonMatching = members.every(
            m => Node.isMethodDeclaration(m) && m.getName() !== methodName
        );
        if (!allMethodsNonMatching) { continue; }

        const member = members[0];
        const existingTexts = member.getLeadingCommentRanges().map(r => r.getText());
        const toAdd = comments.filter(c => !existingTexts.includes(`//${c}`));
        if (toAdd.length === 0) { continue; }

        const indent = member.getIndentationText();
        const text = toAdd.map(c => `//${c}`).join("\n" + indent) + "\n" + indent;
        insertions.push({ pos: member.getStart(), text });
    }

    // Apply back-to-front so earlier positions stay valid.
    insertions.sort((a, b) => b.pos - a.pos);
    for (const ins of insertions) {
        sourceFile.insertText(ins.pos, ins.text);
    }
}
