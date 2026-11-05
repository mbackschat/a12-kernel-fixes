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
import type { SourceFile } from "ts-morph";

import { migrateImports, type ImportMigrationConfiguration } from "@com.mgmtp.a12.devtools/codemod";


export function applyImportMigrations(file: SourceFile, cfg: ImportMigrationConfiguration): void {
    migrateImportsPreservingHeader(file, cfg);
    rewriteUncoveredModuleSpecifiers(file, cfg);
}


// Runs `migrateImports` while preserving the file's leading header.
// Without this, ts-morph inserts any newly created import at position 0, above
// the file's copyright/license comment block. We strip the header, migrate, then
// put it back so new imports land below it.
function migrateImportsPreservingHeader(file: SourceFile, cfg: ImportMigrationConfiguration): void {
    const firstStatement = file.getStatements()[0];
    const headerEnd = firstStatement ? firstStatement.getStart() : 0;
    const header = file.getFullText().substring(0, headerEnd);
    const hasHeader = header.trim().length > 0;

    if (hasHeader) {
        file.replaceText([0, headerEnd], "");
    }

    migrateImports(file, cfg);

    if (hasHeader) {
        file.insertText(0, header);
    }
}


// `migrateImports` skips `export ... from "..."` declarations and leaves
// namespace imports stale when their target collides with an existing import.
// Rewrite those specifiers ourselves, reusing the framework's path rules via a
// throwaway side-effect import so the matching stays identical.
function rewriteUncoveredModuleSpecifiers(file: SourceFile, cfg: ImportMigrationConfiguration): void {
    const rewriteCache = new Map<string, string>();
    const rewriteSpecifier = (spec: string): string => {
        const cached = rewriteCache.get(spec);
        if (cached !== undefined) {
            return cached;
        }
        const project = file.getProject();
        const probe = project.createSourceFile("__rewrite-probe__.ts", `import ${JSON.stringify(spec)};`, {
            overwrite: true
        });
        migrateImports(probe, cfg);
        const rewritten = probe.getImportDeclarations()[0]?.getModuleSpecifierValue() ?? spec;
        project.removeSourceFile(probe);
        rewriteCache.set(spec, rewritten);
        return rewritten;
    };

    const applyTo = (node: { getModuleSpecifierValue(): string | undefined; setModuleSpecifier(s: string): unknown }): void => {
        const spec = node.getModuleSpecifierValue();
        if (spec === undefined) {
            return;
        }
        const rewritten = rewriteSpecifier(spec);
        if (rewritten !== spec) {
            node.setModuleSpecifier(rewritten);
        }
    };

    for (const exportDecl of file.getExportDeclarations()) {
        applyTo(exportDecl);
    }
    for (const importDecl of file.getImportDeclarations()) {
        if (importDecl.getNamespaceImport()) {
            applyTo(importDecl);
        }
    }
}
