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
package com.mgmtp.a12.kernel.core.service.internal;

import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;

import lombok.NonNull;

public class MoveSupportExtmp {

	private MoveSupportExtmp() {
	}

	public static String changeConditionTextForMove(
			@NonNull String ruleConditionText,
			@NonNull String ruleCurrentFullname,
			@NonNull Map<String, String> entityMoves,
			boolean shortNamesAllowed,
			Locale conditionLanguage
	) {

		if (changesAreNotSupported(shortNamesAllowed, ruleCurrentFullname, entityMoves)) {
			return ruleConditionText;
		} else {
			return delegateChangeConditionTextForMove(ruleConditionText, ruleCurrentFullname,
					entityMoves, conditionLanguage);
		}
	}

	static String delegateChangeConditionTextForMove(@NonNull String ruleConditionText,
			@NonNull String ruleCurrentFullname, @NonNull Map<String, String> entityMoves, Locale conditionLanguage) {
		return MoveSupportInternal.getInstance().changeConditionTextForMove(ruleConditionText, ruleCurrentFullname, entityMoves,
				conditionLanguage);
	}

	public static Map<Locale, String> changeErrorTextsForMove(Map<Locale, String> errorTexts,
			@NonNull String ruleCurrentFullname,
			@NonNull Map<String, String> entityMoves,
			boolean shortNamesAllowed,
			Locale conditionLanguage) {

		if (changesAreNotSupported(shortNamesAllowed, ruleCurrentFullname, entityMoves)) {
			return errorTexts;
		} else {
			return delegateChangeErrorTextsForMove(errorTexts, ruleCurrentFullname, entityMoves, conditionLanguage);

		}
	}

	static Map<Locale, String> delegateChangeErrorTextsForMove(Map<Locale, String> errorTexts,
			@NonNull String ruleCurrentFullname
			, @NonNull Map<String, String> entityMoves, Locale conditionLanguage) {
		return MoveSupportInternal.getInstance()
				.changeErrorTextsForMove(errorTexts, ruleCurrentFullname, entityMoves, conditionLanguage);
	}

	static boolean changesAreNotSupported(boolean shortNamesAllowed, String ruleCurrentFullname,
			Map<String, String> entityMoves) {
		return shortNamesAllowed && ruleIsMovedRelativeToRuleGroup(ruleCurrentFullname, entityMoves);
	}

	/**
	 * @return whether child parent relation of rule group and rule is maintained after the move
	 */
	static boolean ruleIsMovedRelativeToRuleGroup(@NonNull String ruleCurrentFullname, @NonNull Map<String, String> entityMoves) {
		if (!entityMoves.containsKey(ruleCurrentFullname)) {
			return false;
		}
		String fullNameMovedRuleGroup = MoveSupportInternal.findNewFullname(ElementPathUtils.getParentPath(ruleCurrentFullname),
				entityMoves);
		String fullNameOfMovedRule =
				MoveSupportInternal.findNewFullname(ruleCurrentFullname, entityMoves);
		return !ElementPathUtils.isChild(fullNameOfMovedRule, fullNameMovedRuleGroup);
	}
}
