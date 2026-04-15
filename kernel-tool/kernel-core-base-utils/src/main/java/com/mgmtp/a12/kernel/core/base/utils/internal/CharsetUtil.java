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

package com.mgmtp.a12.kernel.core.base.utils.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.base.utils.internal.charset.CharsetErrorLookup;
import com.mgmtp.a12.kernel.core.base.utils.internal.charset.CharsetErrorType;
import com.mgmtp.a12.kernel.core.base.utils.internal.charset.CharsetProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.Pair;

/**
 * Utility class with helper functions for restricted charset definitions.
 * Kernel expects charsets to be a list of strings, where each string is either
 * <ul>
 *     <li>A grapheme of length 1 (just a single character), or</li>
 *     <li>A range definition "a-b", or</li>
 *     <li>A grapheme of length larger 1, or</li>
 *     <li>Multiple graphemes concatenated, where at least one grapheme must have a length larger than 1</li>
 * </ul>
 * Restrictions for graphemes of length 1:
 * <ul>
 *     <li>The Unicode code point must be in the range of 0x0000 to 0xFFFF (BMP)</li>
 * </ul>
 * Restrictions for ranges:
 * <ul>
 *     <li>Exactly 3 code points in length</li>
 *     <li>All code points in the BMP</li>
 *     <li>The second character is a dash '-'</li>
 *     <li>For all ranges "a-b", the Unicode code point of "a" must be smaller or equal to the code point of "b"</li>
 * </ul>
 * Restrictions for graphemes of length larger 1 and concatenated graphemes:
 * <ul>
 *     <li>If the length of its string representation is l, the length of its grapheme decomposition must be smaller than l.</li>
 *     <li>Graphemes must be non overlapping if common prefix graphemes are in the charset</li>
 * </ul>
 */
public final class CharsetUtil {

	private static final Pattern UNICODE_PATTERN = Pattern.compile("\\X");

	// Note: If we raise this constant beyond 3 we should at least check the following runtime classes:
	// CheckCommand#markInvalidGraphemes: pairwise checking could no longer be enough
	// LegalCharTester#pruefeZeichensatz: longest grapheme matching should return the longest, possibly partial, match
	public static final int MAX_GRAPHEME_LENGTH = 3;
	public static final int RANGE_DEFINITION_LENGTH = 3;

	private final CharsetErrorLookup charsetErrorLookup;

	private static final IProblemReporter VOID_PROBLEM_REPORTER = p -> {
	};

	public CharsetUtil() {
		this(Locale.GERMANY);
	}

	public CharsetUtil(final Locale locale) {
		charsetErrorLookup = new CharsetErrorLookup(locale);
	}

	/**
	 * Simplifies a given restricted charset by:
	 * <ul>
	 *     <li>parsing ranges including single characters as ranges of length 1</li>
	 *     <li>sorting ranges ascending by interval start</li>
	 *     <li>merging ranges if they overlap or are directly neighboring each other</li>
	 *     <li>parsing all combined graphemes removing duplicates</li>
	 *     <li>return the concatenation of all ranges (collapsing ranges of length 1 back into one character)
	 *     and combined graphemes in ascending order</li>
	 * </ul>
	 * Therefore, a simplified charset has the following form:
	 * <ul>
	 *     <li>first come all single character and character ranges in ascending order</li>
	 *     <lI>afterwards come all combined graphemes in ascending order</lI>
	 * </ul>
	 * So there always exists a range or single character, after which all graphemes follow.
	 * This form should be canonical, therefore
	 * <p>
	 *     simplifyCharset(cs) = simplifyCharset(simplifyCharset(cs))
	 * </p>
	 * should hold.
	 *
	 * @param restrictedCharset
	 * 		the possibly unminified charset
	 * @param problemReporter
	 * 		the problem reporter receiving error messages.
	 * @return Optional containing the simplified charset, empty Option if the charset was not parsable
	 */
	public Optional<List<String>> simplifyCharset(final List<String> restrictedCharset,
			final IProblemReporter problemReporter) {
		return simplifyInternal(restrictedCharset, problemReporter)
				.map(CharsetUtil::parseResultToGraphemeSet);
	}

	private Optional<Pair<List<int[]>, Set<String>>> simplifyInternal(final List<String> restrictedCharset,
			final IProblemReporter problemReporter) {
		return parse(restrictedCharset, problemReporter)
				.map(parseResult -> Pair.of(parseResult.getEntry1(),
						filterOutComposables(parseResult.getEntry1(), parseResult.getEntry2())));
	}

	/**
	 * Tries to parse a charset into a list of Unicode code point ranges and a set of graphemes of length > 1
	 *
	 * <p>Returned ranges are always minimized and sorted in ascending order by interval start.</p>
	 *
	 * @param restrictedCharset
	 * 		the charset
	 * @param problemReporter
	 * 		the problem reporter receiving error messages.
	 * @return Optional containing a {@link Pair} with the list of ranges and the set of graphemes, empty Optional if a parse error
	 * 		occurred.
	 */
	public Optional<Pair<List<int[]>, Set<String>>> parse(final List<String> restrictedCharset,
			final IProblemReporter problemReporter) {
		final List<int[]> ranges = new ArrayList<>();
		final Set<String> combinedGraphemes = new HashSet<>();
		boolean errors = false;

		for (int i = 0; i < restrictedCharset.size(); i++) {
			final String graphemeOrRange = restrictedCharset.get(i);
			if (isRangeDefinition(graphemeOrRange)) {
				if (graphemeOrRange.codePointAt(0) <= graphemeOrRange.codePointAt(2)) {
					ranges.add(new int[] { graphemeOrRange.codePointAt(0), graphemeOrRange.codePointAt(2) });
				} else {
					problemReporter.reportProblem(new CharsetProblem(i,
							charsetErrorLookup.getErrorMessage(CharsetErrorType.CHARSET_RANGE_START_AFTER_END,
									graphemeOrRange, i),
							restrictedCharset));
					errors = true;
				}
			} else if (isCombinedGrapheme(graphemeOrRange)) {
				if (graphemeClustersOf(graphemeOrRange).size() < graphemeOrRange.length()) {
					combinedGraphemes.add(graphemeOrRange);
				} else {
					// currently we don't allow a grapheme like "ch" even if there are languages where "ch" could be considered a single grapheme
					// We return false if the string has only grapheme clusters of length 1 and therefore no combining character is contained
					problemReporter.reportProblem(new CharsetProblem(i,
							charsetErrorLookup.getErrorMessage(CharsetErrorType.CHARSET_GRAPHEME_CLUSTER_EQUALS_LENGTH,
									graphemeOrRange, i),
							restrictedCharset));
					errors = true;
				}
			} else if (isAtomicGrapheme(graphemeOrRange)) {
				final int single = graphemeOrRange.codePointAt(0);
				ranges.add(new int[] { single, single });
			} else {
				problemReporter.reportProblem(new CharsetProblem(i,
						charsetErrorLookup.getErrorMessage(CharsetErrorType.CHARSET_PARSE_ERROR, graphemeOrRange, i),
						restrictedCharset));
				errors = true;
			}
		}
		final List<int[]> mergedRanges = mergeCodepointRanges(ranges); // sorted and merged
		// don't simplify the grapheme set here, because validate depends on parse and should be able to
		// report graphemes that are too long, even if simplify would get rid of them (these could end up in
		// a database or model otherwise)
		return errors ? Optional.empty() : Optional.of(Pair.of(mergedRanges, combinedGraphemes));
	}

	/**
	 * See {@link CharsetUtil#parse}, but without problem reporting.
	 *
	 * @param restrictedCharset
	 * 		the charset
	 * @return Optional containing a {@link Pair} with the list of ranges and the set of graphemes, empty Optional if a parse error
	 * 		occurred.
	 */
	public static Optional<Pair<List<int[]>, Set<String>>> parse(final List<String> restrictedCharset) {
		return new CharsetUtil().parse(restrictedCharset, VOID_PROBLEM_REPORTER);
	}

	/**
	 * Splits the given string in grapheme clusters.
	 *
	 * @param string
	 * 		the string
	 * @return The list of grapheme clusters the string is made of
	 */
	public static List<String> graphemeClustersOf(final String string) {
		return UNICODE_PATTERN.matcher(string).results().map(MatchResult::group).toList();
	}

	private static List<String> parseResultToGraphemeSet(final Pair<List<int[]>, Set<String>> parseResult) {
		final List<String> charset = codepointRangesToString(parseResult.getEntry1());
		final List<String> combined = new ArrayList<>(parseResult.getEntry2());
		Collections.sort(combined);
		charset.addAll(combined);
		return charset;
	}

	private static List<String> codepointRangesToString(final List<int[]> codePointRanges) {
		final List<String> charset = new ArrayList<>();
		for (final int[] range : codePointRanges) {
			final char[] startChar = Character.toChars(range[0]);
			if (range[0] == range[1]) {
				charset.add(String.valueOf(startChar));
			} else {
				charset.add(String.valueOf(startChar) + '-' + String.valueOf(Character.toChars(range[1])));
			}
		}
		return charset;
	}

	private static Set<String> filterOutComposables(final List<int[]> ranges, final Set<String> combinedGraphemes) {
		final Set<String> filtered = new HashSet<>();
		for (final String grapheme : combinedGraphemes) {
			// If all code points of a combined grapheme are found in the ranges, we can delete it (see A12K-3445)
			if (grapheme.codePoints().allMatch(i -> rangeSearch(ranges, i))) {
				continue;
			}
			final List<String> graphemeClusters = graphemeClustersOf(grapheme);
			if (graphemeClusters.size() > 1) {
				// If the grapheme is a concatenation of clusters and all clusters are contained in the charset
				// it does not match new characters and can therefore be filtered out
				if (!clustersContained(graphemeClusters, combinedGraphemes, ranges, 0)) {
					filtered.add(grapheme);
				}
			} else {
				filtered.add(grapheme);
			}
		}
		return filtered;
	}

	/**
	 * @return {@code true} if all {@code clusters} (subtracting the offset) are contained either in {@code graphemes} or
	 *        {@code ranges}, otherwise {@code false}.
	 */
	private static boolean clustersContained(final List<String> clusters, final Set<String> graphemes, final List<int[]> ranges,
			final int offset) {
		for (int i = 0; i < clusters.size() - offset; i++) {
			final String clusterPart = clusters.get(i);
			final int length = clusterPart.length();
			if ((length > 1 && !graphemes.contains(clusterPart))
					|| (isAtomicGrapheme(clusterPart) && !rangeSearch(ranges, clusterPart.codePointAt(0)))) {
				return false;
			}
		}
		return true;
	}

	public static boolean rangeSearch(final List<int[]> ranges, final int character) {
		return isRangeIncludedIn(new int[] { character, character }, ranges);
	}

	private static boolean isRangeDefinition(final String graphemeOrRange) {
		return graphemeOrRange.length() == RANGE_DEFINITION_LENGTH &&
				graphemeOrRange.charAt(1) == '-';
	}

	private static boolean isCombinedGrapheme(final String graphemeOrRange) {
		return !isAtomicGrapheme(graphemeOrRange) && !graphemeOrRange.isEmpty();
	}

	private static boolean isAtomicGrapheme(final String graphemeOrRange) {
		// for now, we only check that there is exactly one code point
		return graphemeOrRange.length() == 1;
	}

	/**
	 * Merges overlapping or neighboring Unicode code point ranges
	 *
	 * @param ranges
	 * 		List of code point ranges [a,b] where range[0] = a and range[1] = b
	 * @return minimal list of ranges
	 */
	public static List<int[]> mergeCodepointRanges(final List<int[]> ranges) {
		final List<int[]> sortedRanges = new ArrayList<>(ranges);
		sortedRanges.sort(Comparator.comparingInt(range -> range[0]));

		final List<int[]> minified = new ArrayList<>();
		final Deque<int[]> stack = new ArrayDeque<>();
		if (!sortedRanges.isEmpty()) {
			stack.push(sortedRanges.get(0));
			for (int i = 1; i < sortedRanges.size(); i++) {
				final int[] prev = stack.peek();
				final int[] range = sortedRanges.get(i);

				assert prev != null;
				final int prevEnd = prev[1];
				final int nextStart = range[0];
				final int nextEnd = range[1];

				if (prevEnd < nextStart && prevEnd + 1 != nextStart) {
					// not overlapping AND not a direct neighbor which we could join
					stack.push(range);
				} else {
					// overlapping or a direct neighbor - join by choosing the biggest interval end
					stack.pop();
					prev[1] = Math.max(prevEnd, nextEnd);
					stack.push(prev);
				}
			}
		}
		// let's preserve the order
		final Iterator<int[]> it = stack.descendingIterator();
		while (it.hasNext()) {
			minified.add(it.next());
		}
		return minified;
	}

	/**
	 * Validates a restricted charset by:
	 * <ul>
	 *     <li>Parsing the charset into a list of valid ranges and a set of graphemes of length greater 1</li>
	 *     <li>If parsable: Checking that there are no surrogates contained</li>
	 *     <li>If parsable: Checking the length restriction that all graphemes have length <= {@link CharsetUtil#MAX_GRAPHEME_LENGTH}</li>
	 *     <li>If parsable: Checking the overlapping constraint if there are concatenated graphemes in the charset</li>
	 * </ul>
	 * <p>
	 * All errors reported return the problematic index in the original charset in {@link IProblem#getSourceStart()} and {@link IProblem#getSourceEnd()}
	 * such that restrictedCharset.get(problem.getSourceStart()) is the cause of the error.
	 *
	 * @param restrictedCharset
	 * 		the charset to validate
	 * @param problemReporter
	 * 		the problem reporter receiving error messages.
	 */
	public void validate(final List<String> restrictedCharset, final IProblemReporter problemReporter) {
		final Optional<Pair<List<int[]>, Set<String>>> parseResult = parse(restrictedCharset, problemReporter);
		if (parseResult.isPresent()) {
			// needed for overlapping checks
			final Map<Integer, List<String>> clusteredGraphemes = new HashMap<>();
			for (int i = 0; i < restrictedCharset.size(); i++) {
				final String graphemeOrRange = restrictedCharset.get(i);
				if (containsSurrogates(graphemeOrRange)) {
					problemReporter.reportProblem(new CharsetProblem(i,
							charsetErrorLookup.getErrorMessage(CharsetErrorType.CHARSET_CONTAINS_SURROGATES, graphemeOrRange, i),
							restrictedCharset));
				}
				//We use that RANGE_DEFINITION_LENGTH == MAX_GRAPHEME_LENGTH currently, no need to distinguish between ranges and graphemes
				if (MAX_GRAPHEME_LENGTH < graphemeOrRange.length()) {
					problemReporter.reportProblem(new CharsetProblem(i,
							charsetErrorLookup.getErrorMessage(CharsetErrorType.CHARSET_GRAPHEME_TOO_LONG, graphemeOrRange, i,
									MAX_GRAPHEME_LENGTH),
							restrictedCharset));
				}
				// Remember all graphemes with a grapheme cluster composition for the overlap check
				if (!isRangeDefinition(graphemeOrRange) && isCombinedGrapheme(graphemeOrRange)) {
					final List<String> clusters = graphemeClustersOf(graphemeOrRange);
					if (clusters.size() > 1) {
						clusteredGraphemes.put(i, clusters); // index of the original grapheme - clusters bigger than 1
					}
				}
			}
			checkOverlappingConstraint(restrictedCharset, problemReporter, parseResult.get(), clusteredGraphemes);
		}
	}

	/**
	 * Analyze the given charset if there exists graphemes G with the following properties:
	 * <ul>
	 *     <li>G = [p] + s, meaning, G is composed of a list of prefix graphemes [p] and a suffix grapheme s</li>
	 *     <li>Every grapheme in [p] is contained in the charset</li>
	 *     <li>The suffix s is a prefix of at least one grapheme in the charset</li>
	 * </ul>
	 * If a grapheme has these properties, we want to prohibit it to be in a charset definition to avoid ambiguous matches.
	 * <p>
	 *     Example: G = "ps", let the charset contain "p" and another grapheme in the charset be defined as "sq".
	 *     "psq" is now an ambivalent string: ("ps", "q") or ("p", "sq") both could be matched.
	 *     Our current matching algorithm could return false negatives in this case.
	 * </p>
	 */
	private void checkOverlappingConstraint(final List<String> restrictedCharset,
			final IProblemReporter problemReporter,
			final Pair<List<int[]>, Set<String>> parseResult,
			final Map<Integer, List<String>> clusteredGraphemes) {
		final Set<String> graphemes = parseResult.getEntry2();
		final List<int[]> ranges = parseResult.getEntry1();

		for (final Map.Entry<Integer, List<String>> entry : clusteredGraphemes.entrySet()) {
			final String original = restrictedCharset.get(entry.getKey());
			final List<String> clusters = entry.getValue();
			if (clustersContained(clusters, graphemes, ranges, 1)) {
				final String suffix = clusters.get(clusters.size() - 1);
				for (final String grapheme : graphemes) {
					if (suffix.length() != grapheme.length() && // this would be solved by simplify
							grapheme.startsWith(suffix) && // suffix of original clustered grapheme is a prefix of another grapheme
							!original.equals(grapheme)) { // don't overlap with yourself
						problemReporter.reportProblem(new CharsetProblem(entry.getKey(),
								charsetErrorLookup.getErrorMessage(CharsetErrorType.CHARSET_GRAPHEME_OVERLAP, original,
										entry.getKey(), suffix,
										grapheme),
								restrictedCharset));
					}
				}
			}
		}
	}

	private static boolean containsSurrogates(final String string) {
		final char[] chars = string.toCharArray();
		for (final char c : chars) {
			if (Character.isSurrogate(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks, if the first given charset contains all graphemes of the second charset. Treats a charset that is represented as the
	 * empty list as equivalent to 0x0000-0xFFFF, in order to match kernel's runtime semantics (definde in LegalCharTester).
	 * Note that this does not take into account the annotation MODEL_DEACTIVATE_LEGALCHARCHECK which in effect allows to
	 * allow even larger charsets than 0x0000-0xFFFF.
	 * <ul>
	 *     <li>Every range in the second charset must be at least a sub range of a range in the first charset</li>
	 *     <lI>All combined graphemes in the second charset must be contained in the first charset</lI>
	 * </ul>
	 *
	 * @return true if potentialSubset is contained in potentialSuperset, false otherwise
	 */
	public static boolean isCharsetContained(List<String> potentialSuperset, List<String> potentialSubset) {
		potentialSuperset = expandEmptyListCase(potentialSuperset);
		potentialSubset = expandEmptyListCase(potentialSubset);
		return isCharsetContainedInternal(potentialSuperset, potentialSubset);
	}

	/**
	 * Replaces empty lists with List.of("\u0000-\uFFFF")
	 * <br>
	 * Background:
	 * <br>
	 * It appears that a charset that is not given (represented as an empty list of string) is treated as equivalent to
	 * List.of("\u0000-\uFFFF) (LegalCharTester in the runtime appears to ultimately define the semantics of charsets, in other
	 * places we seem to just pass around lists or arrays, without explicitly defined semantics)
	 */
	private static List<String> expandEmptyListCase(List<String> charsetAsRangesAndGraphemes) {
		return charsetAsRangesAndGraphemes.isEmpty() ? List.of("\u0000-\uFFFF") : charsetAsRangesAndGraphemes;
	}

	private static boolean isCharsetContainedInternal(final List<String> potentialSuperset, final List<String> potentialSubset) {
		final CharsetUtil charsetUtil = new CharsetUtil();
		final Optional<Pair<List<int[]>, Set<String>>> parsedPotentialSuperset = charsetUtil.simplifyInternal(potentialSuperset,
				VOID_PROBLEM_REPORTER);
		final Optional<Pair<List<int[]>, Set<String>>> parsedPotentialSubset = charsetUtil.simplifyInternal(potentialSubset,
				VOID_PROBLEM_REPORTER);
		if (parsedPotentialSuperset.isEmpty() || parsedPotentialSubset.isEmpty()) {
			return false;
		}
		return isCharsetContainedInternal(parsedPotentialSuperset.get(), parsedPotentialSubset.get());
	}

	private static boolean isCharsetContainedInternal(final Pair<List<int[]>, Set<String>> parsedPotentialSuperset,
			final Pair<List<int[]>, Set<String>> parsedPotentialSubset) {
		final List<int[]> potentialSupersetRanges = parsedPotentialSuperset.getEntry1();
		final Set<String> potentialSupersetGraphemes = parsedPotentialSuperset.getEntry2();

		final List<int[]> potentialSubsetRanges = parsedPotentialSubset.getEntry1();
		final Set<String> potentialSubsetGraphemes = parsedPotentialSubset.getEntry2();

		return containsAllRanges(potentialSupersetRanges, potentialSubsetRanges) &&
				potentialSubsetGraphemes.stream()
						.allMatch(subsetGrapheme -> potentialSupersetGraphemes.contains(subsetGrapheme) ||
								subsetGrapheme.codePoints().allMatch(codePoint -> rangeSearch(potentialSupersetRanges, codePoint)));
	}

	private static boolean containsAllRanges(final List<int[]> ranges1, final List<int[]> ranges2) {
		for (final int[] range : ranges2) {
			if (!isRangeIncludedIn(range, ranges1)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isRangeIncludedIn(final int[] range, final List<int[]> ranges) {
		int l = 0;
		int r = ranges.size() - 1;
		while (l <= r) {
			final int m = (l + r) >>> 1;
			final int lowerBound = ranges.get(m)[0];
			if (range[0] >= lowerBound && range[1] <= ranges.get(m)[1]) {
				return true;
			}
			if (lowerBound < range[0]) {
				l = m + 1;
			} else {
				r = m - 1;
			}
		}
		return false;
	}

	/**
	 * Splits the given range if characters intervals in forbiddenRanges are included.
	 * If the whole range is covered in forbidden ranges, the returned list will be empty.
	 *
	 * @param range
	 * 		the range to split
	 * @return List of ranges where not forbidden interval is contained, possibly empty if the whole range is not allowed.
	 */
	public static List<int[]> removeForbiddenRanges(final int[] range, final List<int[]> forbiddenRanges) {
		final List<int[]> filtered = new ArrayList<>();
		final Deque<int[]> stack = new ArrayDeque<>();
		stack.push(range);
		for (final int[] forbidden : forbiddenRanges) {
			final int[] curr = stack.pop();
			final int start = curr[0];
			final int end = curr[1];
			final int forbiddenStart = forbidden[0];
			final int forbiddenEnd = forbidden[1];
			if (start > forbiddenEnd || end < forbiddenStart) {
				stack.push(curr); // current range outside forbidden range
			} else {
				if (start < forbiddenStart) {
					stack.push(new int[] { start, forbiddenStart - 1 }); // cut away forbidden chars at the end
				}
				if (end <= forbiddenEnd) {
					break; // range completely in forbidden range
				} else {
					stack.push(new int[] { forbiddenEnd + 1, end }); // cut away forbidden chars at the start
				}
			}
		}

		final Iterator<int[]> it = stack.descendingIterator();
		while (it.hasNext()) {
			filtered.add(it.next());
		}
		return filtered;
	}
}
