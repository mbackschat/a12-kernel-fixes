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

import java.util.HashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

/**
 * <p>
 * This class evaluates a given pattern to calculate the minimum and maximum length of a match.
 * </p>
 *
 * <p>
 * TODO: Es fehlt noch die Behandlung von:
 * <ul>
 * <li>{@literal \0n The character with octal value 0n (0 <= n <= 7)}</li>
 * <li>{@literal \0nn The character with octal value 0nn (0 <= n <= 7)}</li>
 * <li>{@literal \0mnn The character with octal value 0mnn (0 <= m <= 3, 0 <= n <= 7)}</li>
 * <li>{@literal \xhh The character with hexadecimal value 0xhh}</li>
 * <li>
 * {@literal \\uhhhh The character with hexadecimal value 0xhhhh POSIX character classes (US-ASCII only)}
 * </li>
 * </ul>
 * </p>
 */

public class RegExpLengthTester {

	private static final Logger LOG = LoggerFactory.getLogger(RegExpLengthTester.class);

	private static class InfiniteLengthException extends Exception {
		private static final long serialVersionUID = -6718529868673695775L;
	}

	/**
	 * This class is used to find matching parenthesis and therefore extract groups out of the
	 * pattern
	 */
	private static class GroupFinder {
		private final String s;
		private final int startId;
		private String groupString = null;
		private String resultString;

		private int start = 0;
		private int end = -1;
		private int replacementAdjustment = 0;

		private int id = 0;
		private int nextId;
		private boolean capturingGroup;

		private GroupFinder(String s, int startId) {
			this.s = s;
			this.resultString = s;
			this.startId = startId;
		}

		/**
		 * Returns the currently selected group
		 */
		private String innerGroup() {
			return groupString;
		}

		/**
		 * Selects the next group, returns false, if no group is found
		 */
		private boolean next() {
			end++;
			start = end + 1;
			id = nextId;
			groupString = null;

			int parenthesisCount = 0;
			boolean capturing = true;
			for (; end < s.length(); end++) {
				if (s.charAt(end) == '(') {
					// Normal check would be: if (s.length() > end+1) {
					// But if the length isn't greater then opening bracket, we
					// have an illegal pattern
					if (s.charAt(end + 1) == '?') {
						capturing = false;
					} else {
						capturing = true;
						nextId++;
					}

					if (parenthesisCount == 0) {
						start = end + 1;
						if (capturing) {
							id++;
						}
					}

					parenthesisCount++;
				} else if (s.charAt(end) == ')') {
					parenthesisCount--;
					if (parenthesisCount == 0) {
						groupString = s.substring(start, end);
						capturingGroup = true;
						if (groupString.startsWith("?")) {
							// Non Capturing Group
							groupString = groupString.substring(2);
							capturingGroup = false;
						}
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * Replaces the currently selectedGroup with the given String
		 */
		private String replaceGroup(String placeholderString) {
			int startIdx = start - replacementAdjustment - 1;
			int endIdx = end - replacementAdjustment + 1;
			if (hasRepetition()) {
				endIdx += s.substring(end + 2, s.indexOf('}', end)).length() + 2;
			}
			resultString = resultString.substring(0, startIdx)
					+ placeholderString + resultString.substring(endIdx);
			replacementAdjustment += (endIdx - startIdx)
					- placeholderString.length();
			return resultString;
		}

		private int getGroupId() {
			return startId + id;
		}

		private boolean hasRepetition() {
			if (s.length() > end + 1) {
				return s.charAt(end + 1) == '{';
			}
			return false;
		}

		private int getRepetitionQuantifier(boolean returnMaxLength)
				throws InfiniteLengthException {
			if (hasRepetition()) {
				String repetitionQuantifier = s.substring(end + 2, s.indexOf(
						'}', end));
				String[] parts = repetitionQuantifier.split(",");
				if (returnMaxLength) {
					if (parts.length == 1) {
						return Integer.parseInt(parts[0]);
					}
					try {
						return Integer.parseInt(parts[1]);
					} catch (NumberFormatException nfe) {
						throw new InfiniteLengthException();
					}
				} else {
					return Integer.parseInt(parts[0]);
				}
			} else {
				return 1;
			}
		}

		private boolean isCapturingGroup() {
			return capturingGroup;
		}
	}

	private static final String NUMERIC_TEST_PATTERN = "(\\+|-)?\\d+(\\.\\d+)?";

	private static final String[] escapedEntities = { "\\(", "\\)",
			"\\\\", "\\t", "\\n", "\\r", "\\f", "\\a", "\\e", "\\[", "\\]",
			"\\D", "\\s", "\\S", "\\w", "\\W", "\\^", "\\$", "\\{",
			"\\}", "\\|", "\\u0022" };

	// "\\." is treated special
	private static final String[] escapedSpecialEntities = { "\\*", "\\+" };

	private static final String[] escapedNumericEntities = { "\\d" };

	private static final String[] escapedNonEntities = { "^", "$", "\\b", "\\B",
			"\\A", "\\G", "\\Z", "\\z" };

	private static final String[] quantifiers = { "*", "+", "?", "}" };

	private static final String placeholderToken = "X";

	private static final String placeholderNumber = "1";

	private static final Pattern backreference = Pattern.compile("\\\\(\\d)");

	private static final Pattern repetitionQuantifierPattern = Pattern
			.compile("(.)\\{(\\d+)(?:,(\\d*))?\\}");

	/**
	 * @return the minimum length of any match or -1 for infinite if the minimum length could not be derived
	 */
	@Getter
	private final int minLength;
	/**
	 * @return a heuristic upper bound on the maximum possible length of any match, or -1 for infinite if no bound can be derived
	 */
	@Getter
	private final int maxLength;

	public RegExpLengthTester(String givenPattern) {
		Pattern.compile(givenPattern);
		String tidied = simplifyPatternForLengthCalculation(givenPattern);

		int calculatedMin;
		try {
			HashMap<Integer, Integer> groupLengthMap = new HashMap<>();
			HashMap<Integer, String> groupCharMap = new HashMap<>();
			String result = calculateRegExpString(tidied, 0, false, groupLengthMap, groupCharMap);
			calculatedMin = result.length();
		} catch (InfiniteLengthException e) {
			calculatedMin = -1;
		}
		this.minLength = calculatedMin;

		int calculatedMax;
		if (tidied.contains("*") || tidied.contains("+")) {
			calculatedMax = -1;
		} else {
			try {
				HashMap<Integer, Integer> groupLengthMap = new HashMap<>();
				HashMap<Integer, String> groupCharMap = new HashMap<>();
				String result = calculateRegExpString(tidied, 0, true, groupLengthMap, groupCharMap);
				calculatedMax = result.length();
			} catch (InfiniteLengthException e) {
				calculatedMax = -1;
			}
		}
		this.maxLength = calculatedMax;
	}

	/**
	 * Replaces all character class expressions ({@code [...]}) with the placeholder token.
	 * Every character class matches exactly one character, so this is safe for length calculation.
	 * <p>
	 * Handles escaped characters inside brackets ({@code [\]]}) and {@code ]} as first character
	 * in a class ({@code []abc]}, {@code [^]abc]}).
	 * Escape sequences outside brackets are passed through unchanged.
	 */
	private static String replaceCharacterClasses(String pattern) {
		StringBuilder result = new StringBuilder(pattern.length());
		int i = 0;
		while (i < pattern.length()) {
			char c = pattern.charAt(i);
			if (c == '\\' && i + 1 < pattern.length()) {
				// Escape sequence outside brackets -- copy both chars through unchanged
				result.append(c);
				result.append(pattern.charAt(i + 1));
				i += 2;
			} else if (c == '[') {
				// Start of character class -- scan forward to find matching ']'
				int j = i + 1;
				// Handle negation caret
				if (j < pattern.length() && pattern.charAt(j) == '^') {
					j++;
				}
				// Handle ']' as first char in class (or first after '^')
				if (j < pattern.length() && pattern.charAt(j) == ']') {
					j++;
				}
				// Scan for closing ']'
				boolean found = false;
				while (j < pattern.length()) {
					if (pattern.charAt(j) == '\\' && j + 1 < pattern.length()) {
						j += 2; // skip escaped character inside brackets
					} else if (pattern.charAt(j) == ']') {
						found = true;
						break;
					} else {
						j++;
					}
				}
				if (found) {
					result.append(placeholderToken);
					i = j + 1; // skip past the closing ']'
				} else {
					// Unmatched '[' -- copy it through (Pattern.compile would have rejected this)
					result.append(c);
					i++;
				}
			} else {
				result.append(c);
				i++;
			}
		}
		return result.toString();
	}

	/**
	 * Simplifies a regular expression pattern for length calculation by replacing constructs
	 * with fixed-length placeholders. The result is not a valid regex but preserves the length
	 * semantics of the original pattern.
	 *
	 * <p>Processing steps:</p>
	 * <ol>
	 *   <li>Replace all character classes ({@code [...]}) with a single placeholder character
	 *       via {@link #replaceCharacterClasses(String)}. This happens first so that
	 *       metacharacters inside brackets (e.g. {@code +}, {@code *}, {@code (}, {@code )})
	 *       are safely removed before they could be misinterpreted as regex operators.</li>
	 *   <li>Replace escape sequences ({@code \(}, {@code \d}, {@code \+}, {@code \.}, etc.)
	 *       with appropriate placeholders.</li>
	 *   <li>Remove zero-width assertions ({@code ^}, {@code $}, {@code \b}, etc.).</li>
	 *   <li>Remove reluctant ({@code *?}, {@code +?}) and possessive ({@code *+}, {@code ++})
	 *       quantifier modifiers, keeping only the base quantifier.</li>
	 * </ol>
	 *
	 * @param p
	 * 		the original regex pattern (must be a valid Java regex)
	 * @return the simplified pattern with length-preserving placeholders
	 */
	static String simplifyPatternForLengthCalculation(final String p) {
		// Step 1: Replace ALL character classes with placeholder token.
		// This must happen before escape replacements so that metacharacters
		// inside brackets (like +, *, (, )) are safely removed.
		String pattern = replaceCharacterClasses(p);

		// Step 2: Replace escaped entities with placeholders
		for (String entity : escapedEntities) {
			pattern = pattern.replace(entity, placeholderToken);
		}
		for (String entity : escapedNumericEntities) {
			pattern = pattern.replace(entity, placeholderNumber);
		}
		for (String entity : escapedSpecialEntities) {
			pattern = pattern.replace(entity, "-");
		}
		pattern = pattern.replace("\\.", ".");

		// Step 3: Remove non-entities (anchors, word boundaries)
		for (String entity : escapedNonEntities) {
			pattern = pattern.replace(entity, "");
		}

		// Step 4: Remove reluctant quantifier modifiers
		for (String quantifier : quantifiers) {
			pattern = pattern.replace(quantifier + "?", quantifier);
		}
		// Step 5: Remove possessive quantifier modifiers
		for (String quantifier : quantifiers) {
			pattern = pattern.replace(quantifier + "+", quantifier);
		}

		return pattern;
	}

	private static String calculateRegExpString(String s, int groupId,
			boolean returnMaxLength,
			HashMap<Integer, Integer> groupLengthMap,
			HashMap<Integer, String> groupCharMap) throws InfiniteLengthException {
		GroupFinder groupFinder = new GroupFinder(s, groupId);
		while (groupFinder.next()) {
			// First: Descend into inner groups
			String innerGroup = groupFinder.innerGroup();
			int gId = groupFinder.getGroupId();
			String groupString = calculateRegExpString(innerGroup, gId,
					returnMaxLength, groupLengthMap, groupCharMap);
			int repetition = groupFinder
					.getRepetitionQuantifier(returnMaxLength);
			// Both calculateRegExpLength and
			// groupFinder.getRepetitionQuantifier will throw a
			// InfitieLengthException
			String placeholderString = StringUtils.repeat(groupString, repetition);
			s = groupFinder.replaceGroup(placeholderString);
			if (groupFinder.isCapturingGroup()) {
				groupLengthMap.put(Integer.valueOf(gId), Integer
						.valueOf(groupString.length()));
				groupCharMap.put(Integer.valueOf(gId), groupString);
			}
		}

		if (s.contains("|")) {
			int partLength;
			boolean isNumeric = true;
			boolean isPlusMinus = true;
			boolean isDot = true;
			// Second: split into OR parts
			String[] parts = s.split("\\|");
			if (returnMaxLength) {
				partLength = 0;
			} else {
				partLength = Integer.MAX_VALUE;
			}
			for (String part : parts) {
				String partString = calculateRegExpString(part, groupId,
						returnMaxLength, groupLengthMap, groupCharMap);
				isNumeric &= partString.matches(NUMERIC_TEST_PATTERN);
				isPlusMinus &= partString.matches("\\+|-");
				isDot &= partString.matches("\\.");
				int tmpL = partString.length();
				if (returnMaxLength) {
					partLength = Math.max(partLength, tmpL);
				} else {
					partLength = Math.min(partLength, tmpL);
				}
			}
			String replacementChar = isNumeric ? placeholderNumber : placeholderToken;
			if (isPlusMinus) {
				replacementChar = "-";
			} else if (isDot) {
				replacementChar = ".";
			}
			return StringUtils.repeat(replacementChar, partLength);
		} else {
			// Third: calculate AND-Parts
			return calculateToken(s, returnMaxLength, groupLengthMap, groupCharMap);
		}
	}

	private static String calculateToken(String s, boolean returnMaxLength,
			HashMap<Integer, Integer> groupLengthMap,
			HashMap<Integer, String> groupCharMap)
			throws InfiniteLengthException {
		String replacementC;
		String replacementD;
		if (returnMaxLength) {
			replacementC = placeholderToken;
			replacementD = placeholderNumber;
		} else {
			replacementC = "";
			replacementD = "";
		}
		s = s.replaceAll("\\d\\?", replacementD);
		s = s.replaceAll("(.)\\?", replacementC);
		// If returnMaxLength is set '*' and '+' result in Integer.MAX_LENGTH in
		// calculateMaxLength()
		s = s.replaceAll("\\d\\*", replacementD);
		s = s.replaceAll("(.)\\*", replacementC);
		s = s.replaceAll("\\d\\+", placeholderNumber);
		s = s.replaceAll(".\\+", placeholderToken);

		Matcher brMatcher = backreference.matcher(s);
		StringBuffer sb = new StringBuffer();
		while (brMatcher.find()) {
			String br = brMatcher.group(1);
			Integer brId = Integer.valueOf(br);
			String replacementString = "";
			if (groupLengthMap.containsKey(brId)) {
				replacementString = groupCharMap.get(brId);
			} else {
				// Pattern references a group, which is not yet analyzed
				// As we analyze in the same order as JAVA, the pattern is WRONG
				// Nevertheless, we do the same as JAVA => Ignore it
			}
			brMatcher.appendReplacement(sb, replacementString);
		}
		brMatcher.appendTail(sb);

		Matcher repetitionQuantifierMatcher = repetitionQuantifierPattern
				.matcher(sb.toString());
		sb = new StringBuffer();
		while (repetitionQuantifierMatcher.find()) {
			// At this point, length qualifiers can only reference single
			// characters, as all groups have been handled earlier
			MatchResult matchResult = repetitionQuantifierMatcher
					.toMatchResult();
			int l = getRepetition(matchResult, returnMaxLength);
			repetitionQuantifierMatcher.appendReplacement(sb, StringUtils
					.repeat(matchResult.group(1), l));
		}
		repetitionQuantifierMatcher.appendTail(sb);

		return sb.toString();
	}

	private static int getRepetition(MatchResult matchResult, boolean returnMaxLength)
			throws InfiniteLengthException {
		int groupId;
		if (returnMaxLength) {
			groupId = 3;
		} else {
			groupId = 2;
		}
		String lString = matchResult.group(groupId);
		if (lString == null) {
			// Passiert dann, wenn nach maxLength gefragt wurde, diese aber
			// nicht angegeben ist
			// => Max == Min
			return Integer.parseInt(matchResult.group(2));
		}
		if (lString.trim().length() > 0) {
			try {
				return Integer.parseInt(lString);
			} catch (NumberFormatException nfe) {
				LOG.warn(nfe.getLocalizedMessage(), nfe);
				if (returnMaxLength) {
					throw new InfiniteLengthException();
				} else {
					return 1;
				}
			}
		} else {
			if (returnMaxLength) {
				throw new InfiniteLengthException();
			} else {
				return 1;
			}
		}
	}

	// minLength and maxLength are accessed via Lombok @Getter
}
