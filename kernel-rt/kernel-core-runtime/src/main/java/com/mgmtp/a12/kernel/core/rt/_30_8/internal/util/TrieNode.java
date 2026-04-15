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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A custom Trie implementation used to match the longest possible Grapheme in {@link LegalCharTester}.
 * Each Trie node is marked with a boolean terminal flag, if the node represents the last code point of a valid Grapheme.
 *
 * <p>
 * Example: Trie.insert([1,2]), Trie.insert([1,2,3]) - "12" and "123" should be matchable, so the node 2 und 3 are marked as
 * terminal.
 * </p>
 *
 * <p>
 * <strong>PLEASE NOTE:</strong> This class exists duplicated in kernel-core-runtime and kernel-core-base-utils.
 * Please keep the behavior of both TrieNode implementations consistent!
 * </p>
 */
public class TrieNode {
	private final Map<Integer, TrieNode> children;

	/**
	 * Not final - possible optimization: If the charset is "not large" we could insert all Graphemes of length 1 and mark them
	 * as terminal -> no binary search over ranges needed in that case
	 */
	private boolean terminal;

	public TrieNode(final Map<Integer, TrieNode> children, final boolean terminal) {
		this.children = children;
		this.terminal = terminal;
	}

	/**
	 * Inserts an array of Unicode code points into the Trie.
	 * This method will mark leaf nodes as terminal.
	 *
	 * @param codepoints
	 * 		- The code points of a grapheme
	 */
	public void insert(final int[] codepoints) {
		TrieNode current = this;
		for (int i = 0; i < codepoints.length; i++) {
			final int codepoint = codepoints[i];
			if (!current.children().containsKey(codepoint)) {
				final TrieNode newNode = new TrieNode(new HashMap<>(), i == codepoints.length - 1);
				current.children().put(codepoint, newNode);
			}
			current = current.children().get(codepoint);
			current.terminal = current.terminal || i == codepoints.length - 1;
		}
	}

	/**
	 * Trie to find the next node if the code point is contained in a valid prefix.
	 *
	 * @param codepoint
	 * 		- the current code point
	 * @return Either the next Trie node if the code point is matched, or null
	 */
	public TrieNode findNext(final int codepoint) {
		return this.children.get(codepoint);
	}

	public Map<Integer, TrieNode> children() {
		return children;
	}

	public boolean terminal() {
		return terminal;
	}

	public void setTerminal(final boolean terminal) {
		this.terminal = terminal;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != getClass())
			return false;
		final var that = (TrieNode) obj;
		return Objects.equals(children, that.children) &&
				terminal == that.terminal;
	}

	@Override
	public int hashCode() {
		return Objects.hash(children, terminal);
	}

	@Override
	public String toString() {
		return "TrieNode[" +
				"children=" + children + ", " +
				"terminal=" + terminal + ']';
	}

}
