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

export class TrieNode {
    private _children: Map<number, TrieNode>;
    private _terminal: boolean;

    constructor(children: Map<number, TrieNode>, terminal: boolean) {
        this._children = children;
        this._terminal = terminal;
    }

    public insert(codepoints: number[]) {
        // eslint-disable-next-line @typescript-eslint/no-this-alias
        let current: TrieNode = this;
        for (let i = 0; i < codepoints.length; i++) {
            const codepoint = codepoints[i];
            if (!current.children().has(codepoint)) {
                const newNode: TrieNode = new TrieNode(new Map<number, TrieNode>(), i === codepoints.length - 1);
                current.children().set(codepoint, newNode);
            }
            const node: TrieNode | undefined = current.children().get(codepoint)
            if (node !== undefined) {
                current = node;
            }
            current._terminal = current._terminal || i === codepoints.length - 1;
        }
    }

    public findNext(codepoint: number): TrieNode | undefined {
        return this._children.get(codepoint);
    }

    public children(): Map<number, TrieNode> {
        return this._children;
    }

    public terminal(): boolean {
        return this._terminal;
    }
}