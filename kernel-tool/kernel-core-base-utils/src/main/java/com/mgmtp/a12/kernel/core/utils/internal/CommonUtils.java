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
package com.mgmtp.a12.kernel.core.utils.internal;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.NonNull;

/**
 * Allgemeine Hilfsmethoden und Konstanten.
 */
public class CommonUtils {

	/**
	 * With EXT-20103 these characters are allowed for print data additionally to ISO-8859-15
	 * characters.
	 */
	public static final Set<Character> SPECIAL_CHARS_LEGAL_FOR_PRINT_DATA = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			'\u201E', '\u201C', // '„', '“', german double quotes
			'\u201A', '\u2018', // '‚', '‘', german single quotes
			'\u2013', // '–', en dash
			// '\u00A0', non-breaking space - already contained in ISO-8859-15
			'\u2011' // non-breaking hyphen
	)));

	// For the following reg exps we have:
	// (?<!\\\\)(\\\\\\\\)* means an even number (incl. 0) of backslashes, where no other backslash may precede. So the following character is not escaped respectively the next backslash escapes the following character.
	// A disallowed escape-sequence. Valid escaped characters: \,*,?,+,{,}(,), [,],.,d und D,-,^
	private static Pattern pNegativeListEscapeSequence = Pattern.compile(
			"(?<!\\\\)(\\\\\\\\)*\\\\([^\\\\\\*\\?\\+\\{\\}\\(\\)\\[.dDun\\]\\-\\^]|u(?!0022))");
	// Analogous to pNegativListeNachBackslash but additionally limited by characters that are allowed in character classes only. I.e. \] \^ and \- are not allowed.
	private static Pattern pNegativeListEscapeSequenceOutsideCharacterClass = Pattern.compile(
			"(?<!\\\\)(\\\\\\\\)*\\\\([^\\\\\\*\\?\\+\\{\\}\\(\\)\\[.dDun]|u(?!0022))");
	// A character class of the form [A] where an even number of \ can precede the first bracket and A can contain \] or any other escape-sequence.
	private static Pattern pCharacterClass = Pattern.compile("(?<!\\\\)(\\\\\\\\)*\\[[^\\]\\\\]*((\\\\.)[^\\]\\\\]*)*\\]");
	// An unescaped square bracket inside a character class, e.g. [abc&&[xyz]] or [abc[def]], which is not allowed.
	private static Pattern pCharacterClassStartInsideCharacterClass = Pattern.compile(
			"(?<!\\\\)(\\\\\\\\)*\\[[^\\[\\]\\\\]*((\\\\.)[^\\[\\]\\\\]*)*\\[");
	// A non-escaped Quantifier (+,* or ?) followed by + or ?
	private static Pattern pNonSimpleQuantifier = Pattern.compile("(?<!\\\\)(\\\\\\\\)*[+*?][+?]");
	// A multiplicative Quantifier {n,m} followed by + or ?
	private static Pattern pNonSimpleMultiplicativeQuantifier = Pattern.compile(
			"(?<!\\\\)(\\\\\\\\)*\\{(\\s*\\d+\\s*)(,(\\s*|\\s*\\d*\\s*))?\\}[+?]");
	// A non-escaped opening parentheses followed by a ?
	private static Pattern pOpenParenthesesWithQuestionMark = Pattern.compile("(?<!\\\\)(\\\\\\\\)*\\(\\?");

	private CommonUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Remove quotes from the beginning and end of the string and replace duplicate quotes with
	 * single ones (deescape).
	 * Replace all occurrences of CrLf with Lf.
	 *
	 * @param s
	 * 		the original string with quotes at the beginning and end. This condition must be
	 * 		met.
	 * @return the resulting string without quotes at the beginning and unescaped quotes
	 * 		and CrLf replaced with Lf.
	 */
	public static String removeAndUnescapeQuotesAndRemoveCrFromCrLf(String s) {
		if (!s.startsWith("\"") || !s.endsWith("\"")) {
			throw new IllegalArgumentException("The string passed has to begin and end with quotes.");
		}

		if (s.length() < 2) {
			return "";
		}
		String result = s.substring(1, s.length() - 1);
		return result.replace("\"\"", "\"").replace("\r\n", "\n");
	}

	/**
	 * Replace duplicate quotes with single ones (deescape).
	 * Replace all occurrences of CrLf with Lf.
	 *
	 * @param s
	 * 		the original string
	 * @return the resulting string with unescaped quotes
	 * 		and CrLf replaced with Lf.
	 */
	public static String unescapeQuotesAndRemoveCrFromCrLf(String s) {
		if (s == null) {
			return null;
		}
		return s.replace("\"\"", "\"").replace("\r\n", "\n");
	}

	/**
	 * Escape contained quotes by duplicating them and add quotes at the beginning and end of the
	 * string.
	 */
	public static String addAndEscapeQuotes(String s) {
		return String.format("\"%s\"", s.replace("\"", "\"\""));
	}

	/**
	 * Split the path and returns an array of all pathes<br>
	 * e.g. /A/k0/k1/f returns <br>
	 * /A, /A/k0, /A/k0/k1 and /A/k0/k1/f, <br>
	 * k0/k1/f returns <br>
	 * k0, k0/k1 und k0/k1/f
	 */
	public static String[] getAllPathes(String path) {

		List<String> result = new ArrayList<>();
		int index = path.indexOf('/');
		while (index != -1) {
			if (index > 0) {//der Leerstring ist kein Teilpfad
				result.add(path.substring(0, index));
			}
			index = path.indexOf('/', index + 1);
		}
		result.add(path);
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Test whether the given regular expression is valid (see validation language documentation).
	 * Additionally to the parseability as Java regular expressions, the escape sequences,
	 * quantifiers, and groups are limited. This is because the regular expression will eventually
	 * be generated for Java, C++, JavaScript and VBA.
	 *
	 * @param regexp
	 * 		the regular expression as being interpreted and output by the code generator.
	 * @return <code>true</code> if the regular expression is valid, <code>false</code> otherwise.
	 */
	public static boolean isRegExValid(String regexp) {

		try {
			// The pattern must compile for java
			Pattern.compile(regexp);
		} catch (PatternSyntaxException pe) {
			return false;
		}

		// The escape-sequences are limited
		if (pNegativeListEscapeSequence.matcher(regexp).find()) {
			return false;
		}
		// Character Classes may not start inside character classes
		if (pCharacterClassStartInsideCharacterClass.matcher(regexp).find()) {
			return false;
		}
		// The escape-sequences are more limited outside of characterClasses
		String patternOhneZeichenauswahl = pCharacterClass.matcher(regexp).replaceAll("");
		if (pNegativeListEscapeSequenceOutsideCharacterClass.matcher(patternOhneZeichenauswahl).find()) {
			return false;
		}
		// Neither reluctant nor possessive quantifiers (simple or multiple)
		if (pNonSimpleQuantifier.matcher(regexp).find()) {
			return false;
		}
		if (pNonSimpleMultiplicativeQuantifier.matcher(regexp).find()) {
			return false;
		}
		// No flags, lookaheads, or special groups.
		if (pOpenParenthesesWithQuestionMark.matcher(regexp).find()) {
			return false;
		}
		return true;
	}

	public static String toStr(Object o) {
		return (o == null) ? "" : o.toString();
	}

	public static boolean checkValidCharsForPrintData(String text) {
		for (char c : text.toCharArray()) {
			if (!checkEncoding(Character.toString(c), "ISO-8859-15") && !SPECIAL_CHARS_LEGAL_FOR_PRINT_DATA.contains(c)) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkEncoding(String text, String encoding) {
		if (StringUtils.isEmpty(text)) {
			return true; // wenn nix drin steht, ist auch das Encoding o.k.
		}
		try {

			Charset cs = Charset.forName(encoding);
			CharsetEncoder csEnc = cs.newEncoder();
			csEnc.onUnmappableCharacter(CodingErrorAction.REPORT);
			csEnc.encode(CharBuffer.wrap(text));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Check if the char represents a whole character in UTF-16. This is not the case if it is a
	 * half of a surrogate pair (used in the representation of a supplementary character).
	 * <p>
	 * Only symbols, that are representable as one character in UTF-16 are allowed for:
	 * <ul>
	 * <li>charset imported in ExtMP (see EXT-17645)</li>
	 * <li>(internal) description of rules and fields (see EXT-18189)</li>
	 * </ul>
	 *
	 * @param c
	 * @return true, if <code>c</code> is no Unicode surrogate code unit, false otherwise
	 */
	public static boolean representsOneCharacter(char c) {
		return !Character.isSurrogate(c);
	}

	/**
	 * Converts a string to a {@link Locale} object, see
	 * <a
	 * href="https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/LocaleUtils.html#toLocale(java.lang.String)">
	 * LocaleUtils##toLocale</a>
	 *
	 * @param localeString
	 * 		a valid locale identifier, or the predefined strings "deutsch" or "english" (which are used by EXTMP project
	 * 		and cannot be migrated without significant coordination overhead)
	 * @return the corresponding Locale object to the given string
	 */
	public static Locale toLocale(@NonNull String localeString) {
		if (localeString.equalsIgnoreCase("deutsch")) {
			return Locale.GERMANY;
		} else if (localeString.equalsIgnoreCase("english")) {
			return Locale.US;
		}
		return LocaleUtils.toLocale(localeString);
	}

	/**
	 * Converts a {@link Locale} to its full locale code string (e.g. {@code "de_DE"}).
	 * Inverse of {@link #toLocale(String)}.
	 */
	public static String localeToCode(@NonNull Locale locale) {
		return locale.toString();
	}
}
