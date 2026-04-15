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

	private static String[] escapedEntities = { "\\(", "\\)",
			"\\\\", "\\t", "\\n", "\\r", "\\f", "\\a", "\\e", "\\[", "\\]",
			"\\D", "\\s", "\\S", "\\w", "\\W", "\\^", "\\$", "\\{",
			"\\}", "\\|", "\\u0022" };

	// "\\." is treated special
	private static String[] escapedSpecialEntities = { "\\*", "\\+" };

	private static String[] escapedNumericEntities = { "\\d" };

	private static String[] escapedNonEntities = { "^", "$", "\\b", "\\B",
			"\\A", "\\G", "\\Z", "\\z" };

	private static String[] quantifiers = { "*", "+", "?", "}" };

	private static String placeholderToken = "X";

	private static String placeholderNumber = "1";

	private static Pattern backreference = Pattern.compile("\\\\(\\d)");

	private static Pattern repetitionQuantifierPattern = Pattern
			.compile("(.)\\{(\\d+)(?:,(\\d*))?\\}");

	private HashMap<Integer, Integer> groupLengthMap;

	private HashMap<Integer, String> groupCharMap;

	private String pattern;

	public RegExpLengthTester(String pattern) {
		this.pattern = pattern;
		Pattern.compile(pattern);
		groupLengthMap = new HashMap<>();
		groupCharMap = new HashMap<>();
		tidyPattern();
	}

	private void tidyPattern() {
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
		//Wir ersetzen alle Negations-Gruppen, welche nicht das placeholderToken enthalten durch das placeholderToken
		pattern = pattern.replaceAll("\\[\\^[^" + placeholderToken + "]*?\\]", placeholderToken);
		pattern = pattern.replaceAll("\\[\\^[^a]*?\\]", "a");

		for (String entity : escapedNonEntities) {
			pattern = pattern.replace(entity, "");
		}

		// Remove all Reluctant
		for (String quantifier : quantifiers) {
			pattern = pattern.replace(quantifier + "?", quantifier);
		}
		// Remove all Possessive
		for (String quantifier : quantifiers) {
			pattern = pattern.replace(quantifier + "+", quantifier);
		}

		// Replace all characterGroups by placeholder
		//Verschiebe Escapte Minuszeichen nach vorne
		pattern = pattern.replaceAll("\\[[0-9\\-]*?\\]", placeholderNumber);

		//Nehme aus dem ersten Bereich nur Anfang und Ende:  a-z => az oder 0-9 => 09
		pattern = pattern.replaceAll("\\[(.*?)([^\\\\])\\-(.*?)\\]", "\\[$1$2$3\\]");
		pattern = pattern.replaceAll("\\[.*?([^0-9\\\\]).*?\\]", "$1");
	}

	private String calculateRegExpString(String s, int groupId,
			boolean returnMaxLength, boolean testNumeric) throws InfiniteLengthException {
		GroupFinder groupFinder = new GroupFinder(s, groupId);
		while (groupFinder.next()) {
			// First: Descend into inner groups
			String innerGroup = groupFinder.innerGroup();
			int gId = groupFinder.getGroupId();
			String groupString = calculateRegExpString(innerGroup, gId,
					returnMaxLength, testNumeric);
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
				String partString = calculateRegExpString(part, groupId, returnMaxLength, testNumeric);
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
			return calculateToken(s, returnMaxLength, testNumeric);
		}
	}

	private String calculateToken(String s, boolean returnMaxLength, boolean testNumeric)
			throws InfiniteLengthException {
		String replacementC;
		String replacementD;
		if (testNumeric) {
			replacementC = "$1";
			replacementD = placeholderNumber;
		} else if (returnMaxLength) {
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
		if (testNumeric) {
			s = s.replaceAll("(.)\\+", "$1");
		} else {
			s = s.replaceAll(".\\+", placeholderToken);
		}

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

	private int getRepetition(MatchResult matchResult, boolean returnMaxLength)
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

	/**
	 * Calculates the minimum length of a match for the given regular expression pattern
	 *
	 * @return the minimum length of a match or -1 if the pattern has no minimum length
	 */
	public int calculateMinLength() {
		try {
			String result = calculateRegExpString(pattern, 0, false, false);
			return result.length();
		} catch (InfiniteLengthException e) {
			return -1;
		}
	}

	/**
	 * Calculates the maximum length of a match for the given regular expression pattern
	 *
	 * @return the maximum length of a match or -1 if the pattern's maximum match length is infinite
	 */
	public int calculateMaxLength() {
		if (pattern.contains("*") || pattern.contains("+")) {
			return -1;
		}
		try {
			String result = calculateRegExpString(pattern, 0, true, false);
			return result.length();
		} catch (InfiniteLengthException e) {
			return -1;
		}
	}
}
