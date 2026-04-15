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
import type {TrieNode} from "./TrieNode.js";

export class LegalCharTester {
    private _legalCodepoints: number[][];
    private _trieRoot: TrieNode;
    private _deactivateLegalCharCheck: boolean;

    constructor(legalCodepoints: number[][], legalGraphemeTrie: TrieNode, deactivateLegalCharCheck: boolean) {
        this._legalCodepoints = legalCodepoints;
        this._deactivateLegalCharCheck = deactivateLegalCharCheck;
        this._trieRoot = legalGraphemeTrie;
    }

    /**
     * Prüft, ob im übergebenen String nur Zeichen aus dem erlaubten
     * Zeichensatz verwendet werden. Der erlaubte Zeichensatz wird aus
     * dem MetaObjekt geholt, der Teil der generierten Sourcen ist.
     *
     * @param value    zu überprüfende Zeichenkette
     * @return true, falls nur gültige Zeichen verwendet werden,
     *         false sonst.
     */

    public pruefeZeichensatz(value: string): boolean {
        if (this._noCharsetDefined() || this._deactivateLegalCharCheck) {
            return this._deactivateLegalCharCheck || !/.*[\uD800-\uDFFF].*/.test(value);
        }
        const length: number = value.length;
        let currentIndex: number = 0;

        while (currentIndex < length) {
            let c: number = currentIndex;
            let match: TrieNode = this._trieRoot;
            // If the Trie is populated, find the longest matching grapheme
            if (!(this._trieRoot.children().size === 0)) {
                while (c < length) {
                    const codepoint = value.codePointAt(c);
                    if (codepoint !== undefined) {
                        const nextMatch: TrieNode | undefined = match.findNext(codepoint);
                        // match until a non match occurs -> c is the end of the string, and we are done
                        // or c is at the position of the longest prefix + 1 for the next iteration
                        if (nextMatch !== undefined) {
                            match = nextMatch;
                            c++;
                        } else {
                            break;
                        }
                    }
                }
            }
            if (match.terminal()) {
                currentIndex = c; // found a combined grapheme - move forward
            } else {
                const character = value.codePointAt(currentIndex);
                if (character !== undefined && this._rangeSearch(character)) {
                    currentIndex++; // no combined grapheme - test for graphemes of length 1
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    private _noCharsetDefined(): boolean {
        return (this._legalCodepoints === undefined || this._legalCodepoints.length === 0
                || this._legalCodepoints[0] === undefined || this._legalCodepoints[0].length === 0) &&
            this._trieRoot.children().size === 0;
    }

    private _rangeSearch(character: number): boolean {
        let l: number = 0;
        let r: number = this._legalCodepoints[0].length - 1;
        while (l <= r) {
            const m: number = (l + r) >>> 1;
            const lowerBound: number = this._legalCodepoints[0][m];
            if (character >= lowerBound && character <= this._legalCodepoints[1][m]) {
                return true;
            }
            if (lowerBound < character) {
                l = m + 1;
            } else {
                r = m - 1;
            }
        }
        return false;
    }

}
