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

package com.mgmtp.a12.kernel.core.base.utils.internal.charset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.core.base.utils.internal.CharsetUtil;

/**
 * Helper class to match strings against a given charset.
 * This class also extends the basic matching logic of the LegalCharTester in kernel-core-runtime
 * with the ability to find all problematic indizes in the given string.
 *
 * <p>
 * <strong>PLEASE NOTE:</strong> This class exists duplicated in kernel-core-runtime and kernel-core-base-utils.
 * Please keep the behavior of both LegalCharTester implementations consistent!
 * </p>
 */
public final class LegalCharTester {

	private final List<int[]> ranges;

	private final TrieNode trieRoot;

	public LegalCharTester(final List<int[]> ranges, final Set<String> graphemes) {
		this.ranges = ranges;
		final TrieNode root = new TrieNode(new HashMap<>(), false);
		graphemes.forEach(grapheme -> root.insert(grapheme.codePoints().toArray()));
		this.trieRoot = root;
	}

	public static Optional<LegalCharTester> fromGraphemeSet(final List<String> graphemeSet) {
		return CharsetUtil.parse(graphemeSet).map(result -> new LegalCharTester(result.getEntry1(), result.getEntry2()));
	}

	/**
	 * @param string
	 * 		the string to match
	 * @return true if there are no invalid graphemes, false otherwise
	 */
	public boolean pruefeZeichensatz(final String string) {
		return findErrorIndizes(string).isEmpty();
	}

	/**
	 * @param string
	 * 		the string to match
	 * @return a List with indizes of the original string with invalid code points, empty list if the string is valid
	 */
	public List<Integer> findErrorIndizes(final String string) {
		final List<Integer> errorPositions = new ArrayList<>();
		final int length = string.length();
		int currentIndex = 0;
		while (currentIndex < length) {
			int c = currentIndex;
			TrieNode match = trieRoot;
			if (!trieRoot.children().isEmpty()) {
				while (c < length) {
					final TrieNode nextMatch = match.findNext(string.codePointAt(c));
					if (nextMatch != null) {
						match = nextMatch;
						c++;
					} else {
						break;
					}
				}
			}
			if (match.terminal()) {
				currentIndex = c;
			} else if (CharsetUtil.rangeSearch(ranges, string.codePointAt(currentIndex))) {
				currentIndex++;
			} else {
				errorPositions.add(currentIndex);
				currentIndex++;
			}
		}
		return errorPositions;
	}
}
