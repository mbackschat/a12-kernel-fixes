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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeConversionResult;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.CustomFieldTypeConversionResultImpl;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.CustomFieldTypeValidationParamImpl;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorEnum;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorKeys;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorUtil;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.iban.IBANUtils;

import lombok.NonNull;

/**
 * This class validates the IBAN (International Bank Account Number).
 * The IBAN verification consists of four parts:
 * <ul>
 * <li>Is the IBAN formally valid?</li>
 * <li>Is the country code valid?</li>
 * <li>Is the country format valid?</li>
 * <li>Is the check digit valid?</li>
 * </ul>
 *
 * <p>
 * The IBAN must match the following format: Two uppercase letters for the country code, two
 * check digits, Basic BAN with 1-30 digits/letters.
 * This check is performed with the following regular expression:
 * <code>([a-zA-Z]{2})[0-9]{2}[0-9a-zA-Z]{1,30}</code>
 * </p>
 *
 * <p>
 * If the IBAN does not match the pattern defined by the regular expression, it is
 * a formal error.
 * </p>
 *
 * <p>
 * The country code is extracted from the IBAN using the regular expression and then checked
 * whether it is contained in the IBAN country code list ({@link IBANVerifier#LAENDER_X_PATTERN}).
 * </p>
 *
 * <p>
 * For each country in the IBAN country code list, a regular expression is specified (see
 * ({@link IBANVerifier#LAENDER_X_PATTERN}), against which the country format is validated.
 * </p>
 *
 * <p>
 * When verifying the check digit, the first four characters are moved to the end of the IBAN.
 * Then a conversion is performed: using a conversion table, each position of the IBAN is converted
 * into a number. The resulting number is divided by the check digit 97 and it is verified whether
 * the remainder of this division equals the expected remainder of 1.
 * </p>
 */
public class IBANVerifier implements ICustomFieldValidator {

	private static final IBANVerifier INSTANCE = new IBANVerifier();
	private static final int SCHIEBE_POSITION = 4;

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static final String CONVERSION_ERROR_IDENTIFIER = "CONVERSION_ERROR";

	// Formal check: Two uppercase letters for country code, two
	// check digits, Basic BAN with 1-30 digits/letters.
	private static final Pattern VALID_IBAN = Pattern.compile("([a-zA-Z]{2})[0-9]{2}[0-9a-zA-Z]{1,30}");

	/**
	 * List of valid IBAN country codes. The BIC list currently contains
	 * four fewer entries than the IBAN list. Since the lists do not need to be
	 * identical according to the specification and are also very small,
	 * they are maintained separately. The commented-out countries belong to
	 * France for IBAN validation purposes: GF, GP, MQ, RE.
	 */
	private static final Map<String, Pattern> LAENDER_X_PATTERN;

	static {
		Map<String, Pattern> ibanLaenderXPattern = new HashMap<>();
		ibanLaenderXPattern.put("AT", Pattern.compile("AT[0-9]{2}[0-9]{5}[0-9]{11}")); // Austria
		ibanLaenderXPattern.put("BE", Pattern.compile("BE[0-9]{2}[0-9]{3}[0-9]{7}[0-9]{2}")); // Belgium
		ibanLaenderXPattern.put("BG", Pattern.compile("BG[0-9]{2}[A-Z]{4}[0-9]{4}[0-9]{2}[a-zA-Z0-9]{8}")); // Bulgaria
		ibanLaenderXPattern.put("CH", Pattern.compile("CH[0-9]{2}[0-9]{5}[a-zA-Z0-9]{12}")); // Switzerland
		ibanLaenderXPattern.put("CY", Pattern.compile("CY[0-9]{2}[0-9]{3}[0-9]{5}[a-zA-Z0-9]{16}")); // Cyprus
		ibanLaenderXPattern.put("CZ", Pattern.compile("CZ[0-9]{2}[0-9]{4}[0-9]{6}[0-9]{10}")); // Czech Republic
		ibanLaenderXPattern.put("DE", Pattern.compile("DE[0-9]{2}[0-9]{8}[0-9]{10}")); // Germany
		ibanLaenderXPattern.put("DK", Pattern.compile("DK[0-9]{2}[0-9]{4}[0-9]{9}[0-9]{1}")); // Denmark
		ibanLaenderXPattern.put("EE", Pattern.compile("EE[0-9]{2}[0-9]{2}[0-9]{2}[0-9]{11}[0-9]{1}")); // Estonia
		ibanLaenderXPattern.put("ES", Pattern.compile("ES[0-9]{2}[0-9]{4}[0-9]{4}[0-9]{1}[0-9]{1}[0-9]{10}")); // Spain
		ibanLaenderXPattern.put("FI", Pattern.compile("FI[0-9]{2}[0-9]{3}[0-9]{11}")); // Finland
		ibanLaenderXPattern.put("FR", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // France
		ibanLaenderXPattern.put("GB", Pattern.compile("GB[0-9]{2}[A-Z]{4}[0-9]{6}[0-9]{8}")); // Great Britain
		// ibanLaender.put( "GF", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // French Guiana
		ibanLaenderXPattern.put("GI", Pattern.compile("GI[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{15}")); // Gibraltar
		// ibanLaender.put( "GP", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Guadeloupe
		ibanLaenderXPattern.put("GR", Pattern.compile("GR[0-9]{2}[0-9]{3}[0-9]{4}[a-zA-Z0-9]{16}")); // Greece
		ibanLaenderXPattern.put("HU", Pattern.compile("HU[0-9]{2}[0-9]{3}[0-9]{4}[0-9]{1}[0-9]{15}[0-9]{1}")); // Hungary
		ibanLaenderXPattern.put("IE", Pattern.compile("IE[0-9]{2}[A-Z]{4}[0-9]{6}[0-9]{8}")); // Ireland
		ibanLaenderXPattern.put("IS", Pattern.compile("IS[0-9]{2}[0-9]{4}[0-9]{2}[0-9]{6}[0-9]{10}")); // Iceland
		ibanLaenderXPattern.put("IT", Pattern.compile("IT[0-9]{2}[A-Z]{1}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{12}")); // Italy
		ibanLaenderXPattern.put("LI", Pattern.compile("LI[0-9]{2}[0-9]{5}[a-zA-Z0-9]{12}")); // Liechtenstein
		ibanLaenderXPattern.put("LT", Pattern.compile("LT[0-9]{2}[0-9]{5}[0-9]{11}")); // Lithuania
		ibanLaenderXPattern.put("LU", Pattern.compile("LU[0-9]{2}[0-9]{3}[a-zA-Z0-9]{13}")); // Luxembourg
		ibanLaenderXPattern.put("LV", Pattern.compile("LV[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{13}")); // Latvia
		// ibanLaender.put( "MQ", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Martinique
		ibanLaenderXPattern.put("MT", Pattern.compile("MT[0-9]{2}[A-Z]{4}[0-9]{5}[a-zA-Z0-9]{18}")); // Malta
		ibanLaenderXPattern.put("NL", Pattern.compile("NL[0-9]{2}[A-Z]{4}[0-9]{10}")); // Netherlands
		ibanLaenderXPattern.put("NO", Pattern.compile("NO[0-9]{2}[0-9]{4}[0-9]{6}[0-9]{1}")); // Norway
		ibanLaenderXPattern.put("PL", Pattern.compile("PL[0-9]{2}[0-9]{8}[0-9]{16}")); // Poland
		ibanLaenderXPattern.put("PT", Pattern.compile("PT[0-9]{2}[0-9]{4}[0-9]{4}[0-9]{11}[0-9]{2}")); // Portugal
		// ibanLaender.put( "RE", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Réunion
		ibanLaenderXPattern.put("RO", Pattern.compile("RO[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{16}")); // Romania
		ibanLaenderXPattern.put("SE", Pattern.compile("SE[0-9]{2}[0-9]{3}[0-9]{16}[0-9]{1}")); // Sweden
		ibanLaenderXPattern.put("SI", Pattern.compile("SI[0-9]{2}[0-9]{5}[0-9]{8}[0-9]{2}")); // Slovenia
		ibanLaenderXPattern.put("SK", Pattern.compile("SK[0-9]{2}[0-9]{4}[0-9]{6}[0-9]{10}")); // Slovakia
		ibanLaenderXPattern.put("MC", Pattern.compile("MC[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Monaco
		ibanLaenderXPattern.put("HR", Pattern.compile("HR[0-9]{2}[0-9]{7}[0-9]{10}")); // Croatia
		ibanLaenderXPattern.put("SM", Pattern.compile("SM[0-9]{2}[A-Z]{1}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{12}")); // San Marino
		ibanLaenderXPattern.put("AD", Pattern.compile("AD[0-9]{2}[0-9]{4}[0-9]{4}[a-zA-Z0-9]{12}")); // Andorra
		ibanLaenderXPattern.put("VA", Pattern.compile("VA[0-9]{2}[0-9]{3}[0-9]{15}")); // Vatican City
		ibanLaenderXPattern.put("AL", Pattern.compile("AL[0-9]{2}[0-9]{8}[a-zA-Z0-9]{16}")); // Albania
		ibanLaenderXPattern.put("MD", Pattern.compile("MD[0-9]{2}[a-zA-Z0-9]{2}[a-zA-Z0-9]{18}")); // Moldova
		ibanLaenderXPattern.put("ME", Pattern.compile("ME[0-9]{2}[0-9]{3}[0-9]{13}[0-9]{2}")); // Montenegro
		ibanLaenderXPattern.put("MK", Pattern.compile("MK[0-9]{2}[0-9]{3}[a-zA-Z0-9]{10}[0-9]{2}")); // North Macedonia

		LAENDER_X_PATTERN = Collections.unmodifiableMap(ibanLaenderXPattern);
	}

	private IBANVerifier() {
	}

	public static IBANVerifier getInstance() {
		return INSTANCE;
	}

	@Override
	public @NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String iban,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	) {
		Locale errorMsgLocale = valParam.getErrorMsgLocale();
		if (isDisplayValue) {// the value must be converted to XML format before validation
			ICustomFieldTypeConversionResult convertResult = convertDisplay2Internal(iban);

			if (convertResult.getErrorMessage().isPresent()) {// conversion error
				return CustomFieldTypeErrorUtil.getResult(CustomFieldTypeErrorEnum.IBAN_LEERZEICHEN_UNGUELTIG, errorMsgLocale,
						CustomFieldTypeErrorKeys.UNGUELTIGE_IBAN);
			}
			// use the XML representation
			iban = convertResult.getConvertedValue();
		}
		CustomFieldTypeErrorEnum error = checkIban(iban);
		return CustomFieldTypeErrorUtil.getResult(error, errorMsgLocale, CustomFieldTypeErrorKeys.UNGUELTIGE_IBAN);
	}

	/**
	 * Validates the specified IBAN and returns the key of the corresponding error message,
	 * or <code>null</code> if the IBAN is valid.
	 */
	private CustomFieldTypeErrorEnum checkIban(final String iban) {
		Matcher matcher = VALID_IBAN.matcher(iban);
		if (!matcher.matches()) {
			return CustomFieldTypeErrorEnum.IBAN_FORMALER_FEHLER;
		}

		final String laenderkennung = matcher.group(1);
		if (!istLaenderkennungGueltig(laenderkennung)) {
			return CustomFieldTypeErrorEnum.IBAN_LAENDERCODE_FEHLER;
		}

		if (!istLandesformatGueltig(laenderkennung, iban)) {
			return CustomFieldTypeErrorEnum.IBAN_LANDESFORMAT_FEHLER;
		}

		if (laenderkennung.contentEquals("DE") && !istDeutschesFormatGueltig(iban)) {
			return CustomFieldTypeErrorEnum.IBAN_LANDESFORMAT_FEHLER;
		}

		if (!istPruefzifferGueltig(iban)) {
			return CustomFieldTypeErrorEnum.IBAN_PRUEFZIFFER_FEHLER;
		}

		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Is the country code valid for IBAN?
	 */
	private boolean istLaenderkennungGueltig(final String laenderkennung) {
		return LAENDER_X_PATTERN.containsKey(laenderkennung);
	}

	/**
	 * Does the IBAN have a valid format for the country?
	 */
	private boolean istLandesformatGueltig(final String laenderkennung, final String iban) {
		Pattern landesformatPattern = LAENDER_X_PATTERN.get(laenderkennung);
		return landesformatPattern.matcher(iban).matches();
	}

	/**
	 * Does the IBAN have a valid format for Germany? A German IBAN that consists only of the digit
	 * “0” in positions 13 to 22 should be rejected as invalid. (see EXT-17593)
	 */
	private boolean istDeutschesFormatGueltig(final String iban) {
		String endziffern = iban.substring(12, 22);
		return !endziffern.equals("0000000000");
	}

	static boolean istPruefzifferGueltig(final String iban) {
		return IBANUtils.istPruefzifferGueltig(iban, SCHIEBE_POSITION);
	}

	@Override
	public @NonNull ICustomFieldTypeConversionResult convertDisplay2Internal(@NonNull String displayValue) {
		String xmlValue = IBANUtils.convertDisplay2Internal(displayValue);

		if (xmlValue != null) {
			return new CustomFieldTypeConversionResultImpl(xmlValue, null);
		} else {
			return new CustomFieldTypeConversionResultImpl(displayValue, CONVERSION_ERROR_IDENTIFIER);
		}
	}

	@Override
	public @NonNull ICustomFieldTypeConversionResult convertInternal2Display(@NonNull String xmlValue) {
		ICustomFieldTypeValidationParam valParam = new CustomFieldTypeValidationParamImpl(Locale.GERMANY,
				CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH, CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH);
		Optional<ICustomFieldTypeCheckError> checkResult = validate(xmlValue, valParam, false);
		if (checkResult.isPresent()) {
			return new CustomFieldTypeConversionResultImpl(xmlValue,
					"Konvertiere nur gültige IBAN. " + checkResult.get().getErrorMessage());
		}
		return new CustomFieldTypeConversionResultImpl(IBANUtils.convertInternal2Display(xmlValue), null);
	}
}
