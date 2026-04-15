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

package com.mgmtp.a12.kernel.core.utils.internal.string.sourcemap;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;

/**
 * For a string that is the concatenation of several fragments (substrings), this class tracks the mapping between the index range
 * in the combined string and the corresponding fragment, and allows to translate positions (indices) in the combined string to the
 * corresponding position within the corresponding fragment.
 * <p/>
 * This class offers the {@link #concat(StringWithFragmentsOriginMap)} method to facilitate the assembly of a combined string from
 * fragments, while making it hard to accidentally mess up the tracked origins and corresponding ranges in the combined string.
 * <p/>
 * The {@link #mapPositionToOrigin(int)} method can then be used to map an index in the combined string to the corresponding
 * fragment (and index within that fragment).
 * <p/>
 * <p/>
 * Example 1 (abstract):
 * <pre>
 *     var fragmentA = SomeClass.of("Hello ");
 *     var fragmentB = SomeClass.of("World!");
 *     var combined  = StringWithFragmentsOriginMap.ofSingleOrigin(fragmentA.text, fragmentA)
 *                     .concat(
 *                     StringWithFragmentsOriginMap.ofSingleOrigin(fragmentB.text, fragmentB));
 *     combined.string
 *     -> returns "Hello World!"
 *     #             ^        ^
 *     # idx:        2        11
 *     combined.mapPositionToOrigin(2)
 *     -> returns fragmentA, idx 2
 *     combined.mapPositionToOrigin(11)
 *     -> returns fragmentB, idx 5
 * </pre>
 * <p/>
 * Example 2: concrete for A12: Map a range in a computation rule (generated from the computation by ComputationAlternativesJoiner)
 * back to the computation's precondition and operation (use-case e.g. in order to change the notation of an entity's path):
 * <pre>
 * Computed field: f1
 * Precondition: "FieldFilled(/RG/G/f2)"
 * Operation:    "[..RG/G/f2]"
 * Resulting combined computation rule condition:
 * "FieldFilled(f1) And (FieldFilled(/RG/G/f2)) And [f1] != [..RG/G/f2]"
 *                       ^^^^^^^^^^^^^^^^^^^^^              ^^^^^^^^^^^
 *                       Precondition (idx 21-42)           Operation (idx 56-67)
 * Lookup:
 * -> use StringWithFragmentsOriginMap.mapPositionToOrigin() to find where "/RG/G/f2" came from:
 * "FieldFilled(f1) And (FieldFilled(/RG/G/f2)) And [f1] != [..RG/G/f2]"
 *                                   ^^^^^^^^
 *                                   idx 33-41
 * combinedComputation.mapPositionToOrigin(33)
 * -> returns Precondition, idx 12
 * combinedComputation.mapPositionToOrigin(41)
 * -> returns Precondition, idx 20
 * -> visualized: Precondition: "FieldFilled(/RG/G/f2)"
 *                                           ^^^^^^^^
 * </pre>
 * Note: index ranges in this example use zero-based, inclusive start, and zero-based, exclusive end indices,
 * i.e. "World" in "Hello World!" is 6 - 11
 * (like e.g. in https://docs.oracle.com/javase/7/docs/api/java/lang/StringBuilder.html#substring(int,%20int) ).
 * <p/>
 * This class was implemented because it allows to disentangle the complexities of computation rules and of correctly tracking the
 * origins/indices of fragments (instead of e.g. a solution that tracks the mapping to computation alternatives, preconditions and
 * operations specifically for computations / ComputationAlternativesJoiner).<br/>
 * This way, ComputationAlternativesJoiner can simply use the {@link #concat(StringWithFragmentsOriginMap)} method and not worry
 * about the fragments/indies etc.<br/>
 * Otherwise, the correct tracking of start and end indices would quickly become very error-prone when fragments are recursively and
 * iteratively concatenated (as done in ComputationAlternativesJoiner).
 */
public class StringWithFragmentsOriginMap {
	private final String string;
	private final List<StringWithFragmentsOriginMapEntry> originRefsForSubstringsSorted;

	/**
	 * @param originRefsForSubstrings
	 * 		Note: must cover the whole string, use {@link StringWithFragmentsOriginMap#ofConst(String)}
	 * 		for fragments for which no origin needs to be tracked. NOTE: originRefsForSubstrings must
	 * 		be sorted by {@link StringWithFragmentsOriginMapEntry#beginIdxInCombinedString}, or lookups may be incorrect.
	 */
	private StringWithFragmentsOriginMap(String string, List<StringWithFragmentsOriginMapEntry> originRefsForSubstrings) {
		this.string = string;
		this.originRefsForSubstringsSorted = originRefsForSubstrings;
	}

	/**
	 * Creates a {@link StringWithFragmentsOriginMap} instance for a string and associates it with the provided origin object.
	 * <p/>
	 * Note: to create a {@link StringWithFragmentsOriginMap} that is combined from multiple string fragments from different
	 * origins, use {@link StringWithFragmentsOriginMap#concat(StringWithFragmentsOriginMap)}.
	 */
	public static StringWithFragmentsOriginMap ofSingleOrigin(String string, Object singleOrigin) {
		return new StringWithFragmentsOriginMap(
				string,
				List.of(new StringWithFragmentsOriginMapEntry(0, string.length(), singleOrigin)));
	}

	/**
	 * Create a StringWithFragmentsOriginMap for a String for which no origin needs to be tracked
	 */
	public static StringWithFragmentsOriginMap ofConst(String originLessConstant) {
		return StringWithFragmentsOriginMap.ofSingleOrigin(originLessConstant, originLessConstant);
	}

	/**
	 * Concatenates this {@link StringWithFragmentsOriginMap} with another one and returns the result, preserving the information
	 * about each fragment's origin.
	 * (The strings are concatenated and the begin/end indexes of the other fragment's origin are shifted by the offset this
	 * .string.length() in the result)
	 *
	 * @return a new {@link StringWithFragmentsOriginMap} that can itself be used with
	 *        {@link StringWithFragmentsOriginMap#concat(StringWithFragmentsOriginMap)} again.
	 */
	public StringWithFragmentsOriginMap concat(StringWithFragmentsOriginMap other) {
		List<StringWithFragmentsOriginMapEntry> combinedOriginRefs = new ArrayList<>(this.originRefsForSubstringsSorted);
		int offset = this.string.length();
		for (StringWithFragmentsOriginMapEntry originRef : other.originRefsForSubstringsSorted) {
			combinedOriginRefs.add(new StringWithFragmentsOriginMapEntry(
					originRef.getBeginIdxInCombinedString() + offset,
					originRef.getEndIdxInCombinedString() + offset,
					originRef.getOriginRef()));
		}
		return new StringWithFragmentsOriginMap(this.string + other.string, combinedOriginRefs);
	}

	/**
	 * Convenience method that calls {@link #concat(StringWithFragmentsOriginMap)} after creating a StringWithFragmentsOriginMap
	 * from originLessConstant
	 * with {@link #ofConst(String)}
	 */
	public StringWithFragmentsOriginMap concat(String originLessConstant) {
		return this.concat(StringWithFragmentsOriginMap.ofConst(originLessConstant));
	}

	/**
	 * For the given positionIdx in the combined string that is represented by this {@link StringWithFragmentsOriginMap}, find
	 * the corresponding origin fragment.
	 * </p>
	 * If positionIdx == this.string.length() (position of EOF), the last fragment will be returned.
	 */
	public StringWithFragmentsOriginMapEntry mapPositionToOrigin(int positionIdx) {
		Validate.isTrue(positionIdx >= 0);
		Validate.isTrue(positionIdx <= this.string.length());

		// Antlr reports some parser errors for the EOF token, for which positionIdx == this.string.length()
		// We do not expect to encounter such cases here, because they *should* be caught in a previous step where individual
		// fragment snippets are parsed,
		// but to be safe, we should support this special case, and define the reasonable behaviour that the EOF position is
		// mapped to the last fragment (the alternative would probably be to introduce a special-case return type for EOF, but that
		// seems unnecessarily complicated):
		// Special case: map the EOF position to the last fragment (instead of throwing an exception):
		if (positionIdx == this.string.length()) {
			var lastFragment = originRefsForSubstringsSorted.get(originRefsForSubstringsSorted.size() - 1);
			Validate.isTrue(lastFragment.getEndIdxInCombinedString() == positionIdx);
			return lastFragment;
		}

		// NOTE: if computations have a very large number of cases, binary search (i.e. treemap lookup) would be faster than a loop
		return originRefsForSubstringsSorted.stream()
				.filter(entry -> entry.contains(positionIdx))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("The source map is inconsistent: it should have an origin for all code " +
						"positions (StringWithFragmentsOriginMap.ofConst(...) for those parts that have no origin " +
						"object). No origin found for position: " + positionIdx));
	}

	/**
	 * @return the combined string / the concatenation of the string fragments of this {@link StringWithFragmentsOriginMap}
	 */
	public String getString() {
		return string;
	}
}