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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.util;

/**
 * Creates the localization keys of the validation results.
 */
public class LocalizableUtil {

	/**
	 * @param fullName
	 * 		the full name of the model element
	 * @param modelId
	 * 		the id of the document model, as specified in the header of the document model
	 * @param type
	 * 		the type of the key, e.g. {@code label} or {@code ruleErrorMessage}
	 * @return a key of the form {@code documentModel.<type>.<modelId>.<dotSeparatedPath>}. Only the model id is escaped, the
	 * 		path is used as-is since it does not contain problematic characters.
	 */
	public static String createModelKey(String fullName, String modelId, String type) {
		return "documentModel.%s.%s%s".formatted(type, addDotEscaping(modelId), extractDotSeparatedPath(fullName));
	}

	public static String createRuleErrorKey(String ruleName, String modelId) {
		return createModelKey(ruleName, modelId, "ruleErrorMessage");
	}

	public static String createFormalErrorKeyFromString(String key) {
		return "kernel.formalErrors.%s".formatted(key);
	}

	public static String createFieldErrorKey(String fieldFullName, String modelId) {
		return createModelKey(fieldFullName, modelId, "fieldErrorMessage");
	}

	public static String createFieldRequiredErrorKey(String fieldFullName, String modelId) {
		return createModelKey(fieldFullName, modelId, "fieldRequiredErrorMessage");
	}

	private static String extractDotSeparatedPath(String fullName) {
		return fullName.replace(EntityUtil.SEPARATOR_CHAR, ".");
	}

	/**
	 * @return the given input with all occurrences of '.' and '\' escaped, as specified by the A12 localization documentation
	 */
	private static String addDotEscaping(String input) {
		StringBuilder escaped = new StringBuilder(input.length());
		for (int i = 0; i < input.length(); i++) {
			char character = input.charAt(i);
			switch (character) {
				case '.' -> escaped.append("\\p");
				case '\\' -> escaped.append("\\\\");
				default -> escaped.append(character);
			}
		}
		return escaped.toString();
	}

	private LocalizableUtil() {
		throw new UnsupportedOperationException("utility class");
	}
}
