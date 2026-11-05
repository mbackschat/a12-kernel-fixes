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
import {NewLineKind, type SourceFile} from "ts-morph";

// ts-morph's `newLineKind` defaults to LineFeed and doesn't detect existing line
// endings. On CRLF files this turns reprinted lines into LF, causing full-file
// diffs. Detect each file's dominant ending and update `manipulationSettings` before
// any manipulation so CRLF files are preserved.
// Note: mixed files are normalized to whichever ending is more frequent.
export function matchLineEndings(sourceFile: SourceFile): void {
    const text = sourceFile.getFullText();
    const crlfCount = (text.match(/\r\n/g) ?? []).length;
    const lfCount = (text.match(/\n/g) ?? []).length - crlfCount;
    sourceFile.getProject().manipulationSettings.set({
        newLineKind: crlfCount >= lfCount ? NewLineKind.CarriageReturnLineFeed : NewLineKind.LineFeed
    });
}
