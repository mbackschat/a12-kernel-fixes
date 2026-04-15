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
package com.mgmtp.a12.kernel.md.model.migration.internal.utils;

import java.util.regex.Pattern;

/**
 * Provides verification and alignment of {@link DocumentModel} names which have to conform to the
 * rules which are described in {@link DocumentModel#setName(String)}.
 */
public class ModelNameUtil {

	private static final String START_CHARACTERS = "[_a-zA-Z]";
	private static final Pattern START_PATTERN = Pattern.compile(START_CHARACTERS);

	private static final String ALLOWED_CHARACTERS = "[-_.a-zA-Z0-9]";
	private static final Pattern ALLOWED_CHARACTERS_PATTERN = Pattern.compile(ALLOWED_CHARACTERS);

	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	private static final Pattern VALID_CHARACTERS_PATTERN = Pattern.compile(START_CHARACTERS + ALLOWED_CHARACTERS + "*");

	private static final Pattern INVALID_PREFIX = Pattern.compile("xml.*", Pattern.CASE_INSENSITIVE);

	private static final String SPECIAL_CHARS = "-.:,;()[]{}&%$§!?=+*";

	private ModelNameUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Checks whether the model name rules as specified in {@link ModelNameUtil} are followed.
	 *
	 * @param name
	 * 		the name to check
	 * @return true, if valid, false otherwise
	 */
	public static boolean isValidModelName(final String name) {
		return VALID_CHARACTERS_PATTERN.matcher(name).matches() && !INVALID_PREFIX.matcher(name).matches();
	}

	/**
	 * Sanitizes a given model name candidate by replacing certain invalid characters with
	 * replacement characters or
	 * removing invalid characters.
	 *
	 * @param initialName
	 * 		the name to clean up
	 * @return the sanitized name
	 */
	public static String proposeValidModelName(final String initialName) {
		if (isValidModelName(initialName)) {
			return initialName;
		}
		final StringBuilder sb = new StringBuilder();
		sanitizeInitialName(initialName, sb);

		if (INVALID_PREFIX.matcher(sb.toString()).matches()) {
			sb.insert(0, '_');
		}

		if (sb.length() > 0) {
			return sb.toString();
		} else {
			return "try_again";
		}
	}

	private static void sanitizeInitialName(final String initialName, final StringBuilder sb) {
		boolean firstCharacterPassed = false;
		int i = 0;
		// find/adapt first suitable character
		for (; i < initialName.length() && !firstCharacterPassed; i++) {
			final char c = initialName.charAt(i);
			final String charString = Character.toString(c);
			if (START_PATTERN.matcher(charString).matches()) {
				sb.append(c);
				firstCharacterPassed = true;
			} else if (ALLOWED_CHARACTERS_PATTERN.matcher(charString).matches()
					&& !START_PATTERN.matcher(charString).matches()) {
				sb.append("_").append(c);
				firstCharacterPassed = true;
			} else if (SPECIAL_CHARS.contains(charString) || WHITESPACE.matcher(charString).matches()) {
				sb.append("_");
				firstCharacterPassed = true;
			}
		}
		// make remaining characters conform to rules
		for (; i < initialName.length(); i++) {
			final char c = initialName.charAt(i);
			final String charString = Character.toString(c);
			if (ALLOWED_CHARACTERS_PATTERN.matcher(charString).matches()) {
				sb.append(c);
			} else if (SPECIAL_CHARS.contains(charString) || WHITESPACE.matcher(charString).matches()) {
				sb.append("_");
			}
		}
	}
}
