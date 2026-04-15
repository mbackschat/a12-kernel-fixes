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
package com.mgmtp.a12.kernel.core.customfieldtype.impl;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorEnum;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorKeys;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorUtil;

import lombok.NonNull;

/**
 * This class validates the international BIC (Bank Identifier Code).
 * The BIC verification consists only of a formal check. The country code list is not
 * verified, since new BIC countries should always be possible.
 */
public class BICinternationalVerifier implements ICustomFieldValidator {

	private static final BICinternationalVerifier INSTANCE = new BICinternationalVerifier();

	/**
	 * Two different formats are allowed for the BIC:
	 * <p>
	 * BIC format 1:<br>
	 * Bank code: four letters,<br>
	 * Country code: two uppercase letters,<br>
	 * Location code: two letters/digits.<br>
	 * </p>
	 * <p>
	 * BIC format 2:<br>
	 * Like format 1, but additionally:<br>
	 * Branch or department identifier: three letters/digits.
	 * </p>
	 * <p>
	 * Both formats can be combined into a single regular expression with
	 * an optional department identifier:
	 * <code>[a-zA-Z]{4}[A-Z]{2}[0-9a-zA-Z]{2}([0-9a-zA-Z]{3})?</code>
	 * </p>
	 */
	private static final Pattern VALID_BICINTERNATIONAL = Pattern.compile("[a-zA-Z]{4}([A-Z]{2})[0-9a-zA-Z]{2}([0-9a-zA-Z]{3})?");

	private BICinternationalVerifier() {
	}

	public static BICinternationalVerifier getInstance() {
		return INSTANCE;
	}

	@Override
	public @NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String bic,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	) {
		CustomFieldTypeErrorEnum errorMsgKey = checkBic(bic);
		return CustomFieldTypeErrorUtil.getResult(errorMsgKey, valParam.getErrorMsgLocale(),
				CustomFieldTypeErrorKeys.UNGUELTIGER_BICINTERNATIONAL);
	}

	/**
	 * Validates the specified BIC and returns the key of the corresponding error message,
	 * or <code>null</code> if the BIC is valid.
	 */
	private CustomFieldTypeErrorEnum checkBic(final String bic) {
		Matcher matcher = VALID_BICINTERNATIONAL.matcher(bic);
		if (!matcher.matches()) {
			return CustomFieldTypeErrorEnum.BICINTERNATIONAL_FORMALER_FEHLER;
		}
		// The country list does not need to be checked, since new BIC countries should always be possible (see EXT-12475).

		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}
}
