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

export interface RenamedIdentifierOptions {
    oldName: string;
    newName: string;
}

// Binding-aware rename: after migrateImports aliases the entity
// (e.g. `{ NewName as OldName }`), find that binding and `rename()` it so all
// usages update together. Falls back to a text sweep when no import matched.
export function renamedIdentifier(
    sourceFile: SourceFile,
    options: RenamedIdentifierOptions
): void {
    const { oldName, newName } = options;

    for (const importDecl of sourceFile.getImportDeclarations()) {
        for (const specifier of importDecl.getNamedImports()) {
            const aliasNode = specifier.getAliasNode();
            const localName = aliasNode?.getText() ?? specifier.getName();
            if (localName !== oldName) {continue;}

            // Both alias and name nodes can be Identifier | StringLiteral; only Identifier supports rename()
            const nameNode = specifier.getNameNode();
            if (!Node.isIdentifier(nameNode)) {continue;}
            const bindingNode =
                aliasNode && Node.isIdentifier(aliasNode) ? aliasNode : nameNode;
            bindingNode.rename(newName);

            // Remove redundant alias { newName as newName } -> { newName }
            if (!specifier.wasForgotten()) {
                const alias = specifier.getAliasNode();
                if (alias && Node.isIdentifier(alias) && alias.getText() === specifier.getName()) {
                    specifier.removeAliasWithRename();
                }
            }
            return;
        }
    }

    // Fallback: text-based sweep for usages without a local import binding
    for (const id of sourceFile.getDescendantsOfKind(SyntaxKind.Identifier)) {
        if (id.wasForgotten()) {continue;}
        if (id.getFirstAncestorByKind(SyntaxKind.ImportDeclaration)) {continue;}
        if (id.getText() === oldName) {
            id.replaceWithText(newName);
        }
    }
}
