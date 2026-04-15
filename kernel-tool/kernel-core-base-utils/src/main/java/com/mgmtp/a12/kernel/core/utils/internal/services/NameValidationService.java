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

package com.mgmtp.a12.kernel.core.utils.internal.services;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.INameValidationService;

import lombok.NonNull;

/**
 * Implements methods to check whether a given groups or field name is valid.
 */
public class NameValidationService implements INameValidationService {

	private static final Pattern RE_VALID_IDENTIFIER_FELD_GESAMT = Pattern.compile("[a-zA-Z0-9_:]+([.][a-zA-Z][a-zA-Z0-9_:]*)*");

	private final @NonNull Locale lang;
	private final @NonNull Set<String> forbiddenErrorCodesInLowerCase;

	public NameValidationService(@NonNull Locale lang, @NonNull Set<String> forbiddenErrorCodesInLowerCase) {
		this.lang = lang;
		this.forbiddenErrorCodesInLowerCase = forbiddenErrorCodesInLowerCase;
	}

	@Override
	public boolean isValidErrorCode(@NonNull String errorCode) {
		return !forbiddenErrorCodesInLowerCase.contains(errorCode.toLowerCase());
	}

	@Override
	public boolean isValidFieldname(@NonNull String name, boolean isHModel) {
		return checkFieldNameMetaModelChecks(name, isHModel)
				&& checkFieldNameNoMetaModelChecks(name);
	}

	/**
	 * Do checks for the field name that are also done in rules in the meta model (the rule names
	 * are given in braces):
	 *
	 * <ul>
	 * <li>In H models the name may not end with ":". (MVK_NAME_ENDS_WITH_COLON_H_DV)</li>
	 * <li>In H-models, if the name contains "." or ":", it may not start with a digit.
	 * (MVK_NAME_STARTS_WITH_DIGIT_H_DV)</li>
	 * <li>In H-models the name may not contain the following strings: "::", ":.".
	 * (MVK_NAME_INVALID_DOT_COLON_H_DV)</li>
	 * <li>In A12 models the name may not contain ":". (MVK_NAME_INVALID_COLON_A12DV)</li>
	 * <li>In A12 models the name may not start with a digit.
	 * (MVK_NAME_STARTS_WITH_DIGIT_A12_DV)</li>
	 * </ul>
	 *
	 * @param name
	 * 		field name to be tested.
	 * @param isHModel
	 * 		Indicates whether the model is a H or A12 model.
	 * @return true, if the given field name is valid w.r.t. the above criteria, false otherwise.
	 */
	private boolean checkFieldNameMetaModelChecks(String name, boolean isHModel) {
		if (isHModel) {
			return checkFieldNameMetaModelChecksHModel(name);
		} else {
			return checkFieldNameMetaModelChecksA12Model(name);
		}
	}

	private boolean checkFieldNameMetaModelChecksHModel(String name) {
		if (name.endsWith(":")) {//MVK_NAME_ENDS_WITH_COLON_H_DV
			return false;
		}
		if ((name.contains(".") || name.contains(":")) && name.matches("[0-9].*")) {//MVK_NAME_STARTS_WITH_DIGIT_H_DV
			return false;
		}
		return !name.contains("::") && !name.contains(":.");//MVK_NAME_INVALID_DOT_COLON_H_DV
	}

	private boolean checkFieldNameMetaModelChecksA12Model(String name) {
		if (name.contains(":")) {//MVK_NAME_INVALID_COLON_A12DV
			return false;
		}
		return !name.matches("[0-9].*");//MVK_NAME_STARTS_WITH_DIGIT_A12_DV
	}

	/**
	 * Do checks for the field name that are NOT done in rules the meta model:
	 *
	 * <ul>
	 * <li>Check if the given field name matches the regular expression
	 * {@link #RE_VALID_IDENTIFIER_FELD_GESAMT} (this part is also done in the meta model via the
	 * field type definition) and does not have a suffix that is reserved for
	 * error text parameters (this part is not done in the meta model) : e.g. the name may not end
	 * with ".value" if the language is set to English.</li>
	 * <li>Check if the parts of the field name are valid, see
	 * {@link #checkNameParts(String, boolean)}.</li>
	 * </ul>
	 *
	 * @param name
	 * 		field name to be tested.
	 * @return true, if the given field name is valid w.r.t. the regular expression, error text
	 * 		parameters and name parts, false otherwise.
	 */
	public boolean checkFieldNameNoMetaModelChecks(@NonNull String name) {
		Matcher matcher = RE_VALID_IDENTIFIER_FELD_GESAMT.matcher(name);
		if (matcher.matches()) {
			String part = matcher.group(1);
			if (Locale.GERMANY.equals(lang)) {
				if (".Wert".equalsIgnoreCase(part) || ".Vordruckzeile".equalsIgnoreCase(part) || ".Vordruckname".equalsIgnoreCase(
						part)) {
					return false;
				}
			} else {
				if (".value".equalsIgnoreCase(part)) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValidRuleName(@NonNull String ruleName) {
		return isValidErrorCode(ruleName);
	}
}
