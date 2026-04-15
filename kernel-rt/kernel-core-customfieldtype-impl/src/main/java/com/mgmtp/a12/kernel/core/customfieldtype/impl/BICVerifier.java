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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
 * This class validates the BIC (Bank Identifier Code).
 * The BIC verification consists of two parts:
 * <ul>
 * <li>Is the BIC formally valid?</li>
 * <li>Is the country code valid?</li>
 * </ul>
 * <p>
 * If the BIC does not match the pattern defined by the regular expression, it is
 * a formal error.
 * </p>
 */
public class BICVerifier implements ICustomFieldValidator {

	private static final BICVerifier INSTANCE = new BICVerifier();

	/**
	 * Two different formats are allowed for the BIC:
	 * <p>
	 * BIC format 1:<br>
	 * Bank code: four letters,<br>
	 * Country code: two letters,<br>
	 * Location code: two letters/digits.
	 * </p>
	 * <p>
	 * BIC format 2:<br>
	 * Like format 1, but additionally:<br>
	 * Branch or department identifier: three letters/digits.
	 * </p>
	 * <p>
	 * Both formats can be combined into a single regular expression with
	 * an optional department identifier:
	 * <code>[a-zA-Z]{4}([a-zA-Z]{2})[0-9a-zA-Z]{2}([0-9a-zA-Z]{3})?</code>
	 * </p>
	 */
	private static final Pattern VALID_BIC = Pattern.compile("[a-zA-Z]{4}([a-zA-Z]{2})[0-9a-zA-Z]{2}([0-9a-zA-Z]{3})?");

	/**
	 * List of valid BIC country codes.
	 * The BIC list currently contains four fewer entries than the IBAN list.
	 * Since the lists do not need to be identical according to the specification and are
	 * also very small, they are maintained separately.
	 */
	private static final Set<String> BIC_LAENDER;

	static {
		Set<String> bicLaender = new HashSet<>();
		bicLaender.add("AT"); // Austria
		bicLaender.add("BE"); // Belgium
		bicLaender.add("BG"); // Bulgaria
		bicLaender.add("CH"); // Switzerland
		bicLaender.add("CY"); // Cyprus
		bicLaender.add("CZ"); // Czech Republic
		bicLaender.add("DE"); // Germany
		bicLaender.add("DK"); // Denmark
		bicLaender.add("EE"); // Estonia
		bicLaender.add("ES"); // Spain
		bicLaender.add("FI"); // Finland
		bicLaender.add("FR"); // France
		bicLaender.add("GB"); // Great Britain
		bicLaender.add("GF"); // French Guiana
		bicLaender.add("GI"); // Gibraltar
		bicLaender.add("GP"); // Guadeloupe
		bicLaender.add("GR"); // Greece
		bicLaender.add("HU"); // Hungary
		bicLaender.add("IE"); // Ireland
		bicLaender.add("IS"); // Iceland
		bicLaender.add("IT"); // Italy
		bicLaender.add("LI"); // Liechtenstein
		bicLaender.add("LT"); // Lithuania
		bicLaender.add("LU"); // Luxembourg
		bicLaender.add("LV"); // Latvia
		bicLaender.add("MQ"); // Martinique
		bicLaender.add("MT"); // Malta
		bicLaender.add("NL"); // Netherlands
		bicLaender.add("NO"); // Norway
		bicLaender.add("PL"); // Poland
		bicLaender.add("PT"); // Portugal
		bicLaender.add("RE"); // Réunion
		bicLaender.add("RO"); // Romania
		bicLaender.add("SE"); // Sweden
		bicLaender.add("SI"); // Slovenia
		bicLaender.add("SK"); // Slovakia
		bicLaender.add("MC"); // Monaco
		bicLaender.add("YT"); // Mayotte (belongs to France (FR) for IBAN and is therefore already included in IBAN)
		bicLaender.add("BL"); // Saint Barthélemy (belongs to France (FR) for IBAN and is therefore already included in IBAN)
		bicLaender.add("MF"); // Saint Martin (belongs to France (FR) for IBAN and is therefore already included in IBAN)
		bicLaender.add(
				"PM"); // Saint Pierre and Miquelon (belongs to France (FR) for IBAN and is therefore already included in IBAN)
		bicLaender.add("HR"); // Croatia
		bicLaender.add("SM"); // San Marino
		bicLaender.add("AD"); // Andorra
		bicLaender.add("VA"); // Vatican City
		bicLaender.add("AL"); // Albania
		bicLaender.add("MD"); // Moldova
		bicLaender.add("ME"); // Montenegro
		bicLaender.add("MK"); // North Macedonia

		BIC_LAENDER = Collections.unmodifiableSet(bicLaender);
	}

	private BICVerifier() {
	}

	public static BICVerifier getInstance() {
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
				CustomFieldTypeErrorKeys.UNGUELTIGE_BIC);
	}

	/**
	 * Validates the specified BIC and returns the key of the corresponding error message,
	 * or <code>null</code> if the BIC is valid.
	 */
	private CustomFieldTypeErrorEnum checkBic(final String bic) {
		Matcher matcher = VALID_BIC.matcher(bic);
		if (!matcher.matches()) {
			return CustomFieldTypeErrorEnum.BIC_FORMALER_FEHLER;
		}

		String laenderkennung = matcher.group(1);
		if (!istLaenderkennungGueltig(laenderkennung)) {
			return CustomFieldTypeErrorEnum.BIC_LAENDERCODE_FEHLER;
		}
		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Checks whether the country code is valid for a BIC.
	 */
	private boolean istLaenderkennungGueltig(final String laenderkennung) {
		return BIC_LAENDER.contains(laenderkennung);
	}
}
