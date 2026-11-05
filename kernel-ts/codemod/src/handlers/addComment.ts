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
import { Node, SyntaxKind, type SourceFile, type Statement } from "ts-morph";

export interface AddCommentOptions {
    className?: string;
    methodName?: string;
    propertyName?: string;
    comments: string[];
}

// True when `node` is the first non-whitespace token on its line.
function startsOwnLine(node: Node): boolean {
    const fullText = node.getSourceFile().getFullText();
    const start = node.getStart();
    const lineStart = fullText.lastIndexOf("\n", start - 1) + 1;
    return /^\s*$/.test(fullText.substring(lineStart, start));
}

// Nearest enclosing statement that starts its own line, so the comment can be
// placed above usages in any position (expression, assignment, if/return/throw)
// without mangling constructs like `} else if`.
function getNearestStatement(node: Node): Statement | undefined {
    let current: Node | undefined = node.getParent();
    while (current) {
        if (Node.isStatement(current) && startsOwnLine(current)) {return current;}
        current = current.getParent();
    }
    return undefined;
}

export function addComment(sourceFile: SourceFile, options: AddCommentOptions): void {
    const { className, methodName, propertyName, comments } = options;
    const stmtsToAnnotate = new Set<Node>();

    if (methodName) {
        for (const callExpr of sourceFile.getDescendantsOfKind(SyntaxKind.CallExpression)) {
            const callee = callExpr.getExpression();
            if (!Node.isPropertyAccessExpression(callee)) {continue;}
            if (!Node.isIdentifier(callee.getExpression())) {continue;}
            if (callee.getName() !== methodName) {continue;}
            if (className && callee.getExpression().getText() !== className) {continue;}
            const stmt = getNearestStatement(callExpr);
            if (stmt) {stmtsToAnnotate.add(stmt);}
        }
    }

    if (propertyName) {
        for (const propAccess of sourceFile.getDescendantsOfKind(SyntaxKind.PropertyAccessExpression)) {
            if (!Node.isIdentifier(propAccess.getExpression())) {continue;}
            if (propAccess.getName() !== propertyName) {continue;}
            if (className && propAccess.getExpression().getText() !== className) {continue;}
            const stmt = getNearestStatement(propAccess);
            if (stmt) {stmtsToAnnotate.add(stmt);}
        }
    }

    // Collect insertions before modifying any text, highest position first.
    const sorted = [...stmtsToAnnotate].sort((a, b) => b.getStart() - a.getStart());
    const insertions: Array<{ pos: number; text: string }> = [];
    const fullText = sourceFile.getFullText();

    for (const stmt of sorted) {
        const existingTexts = stmt.getLeadingCommentRanges().map(r => r.getText());
        const toAdd = comments.filter(c => !existingTexts.includes(`//${c}`));
        if (toAdd.length === 0) {continue;}
        const nodeStart = stmt.getStart();
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
