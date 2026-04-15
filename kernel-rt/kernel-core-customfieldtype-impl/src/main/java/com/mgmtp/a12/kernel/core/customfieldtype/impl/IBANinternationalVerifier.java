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
 * This class validates the international IBAN (International Bank Account Number).
 * The IBAN verification consists of three parts.
 * <ul>
 * <li>Formal check</li>
 * <li>Is the country format valid?</li>
 * <li>Is the check digit valid?</li>
 * </ul>
 * <p>
 * The IBAN (International) must match the following format: Two uppercase letters for the
 * country code, two check digits, Basic BAN with 1-30 digits/letters.
 * This check is performed with the following regular expression:
 * <code>[A-Z]{2}[0-9]{2}[0-9a-zA-Z]{1,30}</code>
 * <p>
 * If the IBAN (International) does not match the pattern defined by the regular expression,
 * it is a formal error.
 * </p>
 * <p>
 * For each country in the international IBAN country code list, a regular expression is specified
 * against which the country format is validated
 * ({@link IBANinternationalVerifier#LAENDER_X_PATTERN}).
 * </p>
 * <p>
 * The check digit verification is performed analogously to {@link IBANVerifier#istPruefzifferGueltig}.
 */
public class IBANinternationalVerifier implements ICustomFieldValidator {

	private static final IBANinternationalVerifier INSTANCE = new IBANinternationalVerifier();

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static final String CONVERSION_ERROR_IDENTIFIER = "CONVERSION_ERROR";

	// Formal check: Two uppercase letters for country code, two check digits, Basic BAN with 1-30 digits/letters.
	private static final Pattern VALID_IBANINTERNATIONAL = Pattern.compile("([A-Z]{2})[0-9]{2}[0-9a-zA-Z]{1,30}");

	/**
	 * List of valid IBAN country codes (see <a href=
	 * "https://www.swift.com/sites/default/files/resources/iban_registry.pdf">https://www.swift.com/sites/default/files/resources/iban_registry.pdf</a>).
	 */
	private static final Map<String, Pattern> LAENDER_X_PATTERN;

	static {
		Map<String, Pattern> ibanLaenderXPattern = new HashMap<>();
		// Europe (SEPA)
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
		// ibanLaenderXPattern.put("BL", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Saint Barthelemy
		// ibanLaenderXPattern.put("GF", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // French Guyana
		// ibanLaenderXPattern.put("GP", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Guadeloupe
		// ibanLaenderXPattern.put("MF", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Saint Martin (French part)
		// ibanLaenderXPattern.put("MQ", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Martinique
		// ibanLaenderXPattern.put("RE", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Réunion
		// ibanLaenderXPattern.put("PM", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Saint Pierre et Miquelon
		// ibanLaenderXPattern.put("YT", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Mayotte

		ibanLaenderXPattern.put("GB", Pattern.compile("GB[0-9]{2}[A-Z]{4}[0-9]{6}[0-9]{8}")); // United Kingdom
		ibanLaenderXPattern.put("GI", Pattern.compile("GI[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{15}")); // Gibraltar
		ibanLaenderXPattern.put("GR", Pattern.compile("GR[0-9]{2}[0-9]{3}[0-9]{4}[a-zA-Z0-9]{16}")); // Greece
		ibanLaenderXPattern.put("HR", Pattern.compile("HR[0-9]{2}[0-9]{7}[0-9]{10}")); // Croatia
		ibanLaenderXPattern.put("HU", Pattern.compile("HU[0-9]{2}[0-9]{3}[0-9]{4}[0-9]{1}[0-9]{15}[0-9]{1}")); // Hungary
		ibanLaenderXPattern.put("IE", Pattern.compile("IE[0-9]{2}[A-Z]{4}[0-9]{6}[0-9]{8}")); // Ireland
		ibanLaenderXPattern.put("IS", Pattern.compile("IS[0-9]{2}[0-9]{4}[0-9]{2}[0-9]{6}[0-9]{10}")); // Iceland
		ibanLaenderXPattern.put("IT", Pattern.compile("IT[0-9]{2}[A-Z]{1}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{12}")); // Italy
		ibanLaenderXPattern.put("LI", Pattern.compile("LI[0-9]{2}[0-9]{5}[a-zA-Z0-9]{12}")); // Liechtenstein
		ibanLaenderXPattern.put("LT", Pattern.compile("LT[0-9]{2}[0-9]{5}[0-9]{11}")); // Lithuania
		ibanLaenderXPattern.put("LU", Pattern.compile("LU[0-9]{2}[0-9]{3}[a-zA-Z0-9]{13}")); // Luxembourg
		ibanLaenderXPattern.put("LV", Pattern.compile("LV[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{13}")); // Latvia
		ibanLaenderXPattern.put("MC", Pattern.compile("MC[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Monaco
		ibanLaenderXPattern.put("MT", Pattern.compile("MT[0-9]{2}[A-Z]{4}[0-9]{5}[a-zA-Z0-9]{18}")); // Malta
		ibanLaenderXPattern.put("NL", Pattern.compile("NL[0-9]{2}[A-Z]{4}[0-9]{10}")); // Netherlands
		ibanLaenderXPattern.put("NO", Pattern.compile("NO[0-9]{2}[0-9]{4}[0-9]{6}[0-9]{1}")); // Norway
		ibanLaenderXPattern.put("PL", Pattern.compile("PL[0-9]{2}[0-9]{8}[0-9]{16}")); // Poland
		ibanLaenderXPattern.put("PT", Pattern.compile("PT[0-9]{2}[0-9]{4}[0-9]{4}[0-9]{11}[0-9]{2}")); // Portugal
		ibanLaenderXPattern.put("RO", Pattern.compile("RO[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{16}")); // Romania
		ibanLaenderXPattern.put("SE", Pattern.compile("SE[0-9]{2}[0-9]{3}[0-9]{16}[0-9]{1}")); // Sweden
		ibanLaenderXPattern.put("SI", Pattern.compile("SI[0-9]{2}[0-9]{5}[0-9]{8}[0-9]{2}")); // Slovenia
		ibanLaenderXPattern.put("SK", Pattern.compile("SK[0-9]{2}[0-9]{4}[0-9]{6}[0-9]{10}")); // Slovak Republic
		ibanLaenderXPattern.put("SM", Pattern.compile("SM[0-9]{2}[A-Z]{1}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{12}")); // San Marino

		// International
		ibanLaenderXPattern.put("AD", Pattern.compile("AD[0-9]{2}[0-9]{4}[0-9]{4}[a-zA-Z0-9]{12}")); // Andorra
		ibanLaenderXPattern.put("AE", Pattern.compile("AE[0-9]{2}[0-9]{3}[0-9]{16}")); // United Arab Emirates
		ibanLaenderXPattern.put("AL", Pattern.compile("AL[0-9]{2}[0-9]{8}[a-zA-Z0-9]{16}")); // Albania
		ibanLaenderXPattern.put("AZ", Pattern.compile("AZ[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{20}")); // Azerbaijan
		ibanLaenderXPattern.put("BA", Pattern.compile("BA[0-9]{2}[0-9]{3}[0-9]{3}[0-9]{8}[0-9]{2}")); // Bosnia and Herzegovina
		ibanLaenderXPattern.put("BH", Pattern.compile("BH[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{14}")); // Bahrain
		ibanLaenderXPattern.put("BR", Pattern.compile("BR[0-9]{2}[0-9]{8}[0-9]{5}[0-9]{10}[A-Z]{1}[a-zA-Z0-9]{1}")); // Brazil
		ibanLaenderXPattern.put("BY", Pattern.compile("BY[0-9]{2}[a-zA-Z0-9]{4}[0-9]{4}[a-zA-Z0-9]{16}")); // Republic of Belarus
		ibanLaenderXPattern.put("CR", Pattern.compile("CR[0-9]{2}[0-9]{4}[0-9]{14}")); // Costa Rica
		ibanLaenderXPattern.put("DO", Pattern.compile("DO[0-9]{2}[a-zA-Z0-9]{4}[0-9]{20}")); // Dominican Republic
		ibanLaenderXPattern.put("FO", Pattern.compile("FO[0-9]{2}[0-9]{4}[0-9]{9}[0-9]{1}")); // Faroe Islands
		ibanLaenderXPattern.put("GE", Pattern.compile("GE[0-9]{2}[A-Z]{2}[0-9]{16}")); // Georgia
		ibanLaenderXPattern.put("GL", Pattern.compile("GL[0-9]{2}[0-9]{4}[0-9]{9}[0-9]{1}")); // Greenland
		ibanLaenderXPattern.put("GT", Pattern.compile("GT[0-9]{2}[a-zA-Z0-9]{4}[a-zA-Z0-9]{20}")); // Guatemala
		ibanLaenderXPattern.put("IL", Pattern.compile("IL[0-9]{2}[0-9]{3}[0-9]{3}[0-9]{13}")); // Israel
		ibanLaenderXPattern.put("IQ", Pattern.compile("IQ[0-9]{2}[A-Z]{4}[0-9]{3}[0-9]{12}")); // Iraq
		ibanLaenderXPattern.put("JO", Pattern.compile("JO[0-9]{2}[A-Z]{4}[0-9]{4}[a-zA-Z0-9]{18}")); // Jordan
		ibanLaenderXPattern.put("KW", Pattern.compile("KW[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{22}")); // Kuwait
		ibanLaenderXPattern.put("KZ", Pattern.compile("KZ[0-9]{2}[0-9]{3}[a-zA-Z0-9]{13}")); // Kazakhstan
		ibanLaenderXPattern.put("LB", Pattern.compile("LB[0-9]{2}[0-9]{4}[a-zA-Z0-9]{20}")); // Lebanon
		ibanLaenderXPattern.put("LC", Pattern.compile("LC[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{24}")); // Saint Lucia
		ibanLaenderXPattern.put("MD", Pattern.compile("MD[0-9]{2}[a-zA-Z0-9]{2}[a-zA-Z0-9]{18}")); // Moldova
		ibanLaenderXPattern.put("ME", Pattern.compile("ME[0-9]{2}[0-9]{3}[0-9]{13}[0-9]{2}")); // Montenegro
		ibanLaenderXPattern.put("MK", Pattern.compile("MK[0-9]{2}[0-9]{3}[a-zA-Z0-9]{10}[0-9]{2}")); // Macedonia
		ibanLaenderXPattern.put("MR", Pattern.compile("MR[0-9]{2}[0-9]{5}[0-9]{5}[0-9]{11}[0-9]{2}")); // Mauritania
		ibanLaenderXPattern.put("MU", Pattern.compile("MU[0-9]{2}[A-Z]{4}[0-9]{2}[0-9]{2}[0-9]{12}[0-9]{3}[A-Z]{3}")); // Mauritius
		ibanLaenderXPattern.put("PK", Pattern.compile("PK[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{16}")); // Pakistan
		ibanLaenderXPattern.put("PS", Pattern.compile("PS[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{21}")); // Palestine
		ibanLaenderXPattern.put("QA", Pattern.compile("QA[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{21}")); // Qatar
		ibanLaenderXPattern.put("RS", Pattern.compile("RS[0-9]{2}[0-9]{3}[0-9]{13}[0-9]{2}")); // Serbia
		ibanLaenderXPattern.put("SA", Pattern.compile("SA[0-9]{2}[0-9]{2}[a-zA-Z0-9]{18}")); // Saudi Arabia
		ibanLaenderXPattern.put("SC", Pattern.compile("SC[0-9]{2}[A-Z]{4}[0-9]{2}[0-9]{2}[0-9]{16}[A-Z]{3}")); // Seychelles
		ibanLaenderXPattern.put("ST", Pattern.compile("ST[0-9]{2}[0-9]{4}[0-9]{4}[0-9]{11}[0-9]{2}")); // Sao Tome And Principe
		ibanLaenderXPattern.put("SV", Pattern.compile("SV[0-9]{2}[A-Z]{4}[0-9]{20}")); // El Salvador
		ibanLaenderXPattern.put("TL", Pattern.compile("TL[0-9]{2}[0-9]{3}[0-9]{14}[0-9]{2}")); // Timor-Leste
		ibanLaenderXPattern.put("TN", Pattern.compile("TN[0-9]{2}[0-9]{2}[0-9]{3}[0-9]{13}[0-9]{2}")); // Tunisia
		ibanLaenderXPattern.put("TR", Pattern.compile("TR[0-9]{2}[0-9]{5}[0-9]{1}[a-zA-Z0-9]{16}")); // Turkey
		ibanLaenderXPattern.put("UA", Pattern.compile("UA[0-9]{2}[0-9]{6}[a-zA-Z0-9]{19}")); // Ukraine
		ibanLaenderXPattern.put("VG", Pattern.compile("VG[0-9]{2}[A-Z]{4}[0-9]{16}")); // Virgin Islands, British
		ibanLaenderXPattern.put("XK", Pattern.compile("XK[0-9]{2}[0-9]{4}[0-9]{10}[0-9]{2}")); // Kosov

		// ibanLaenderXPattern.put("NC", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // New Caledonia
		// ibanLaenderXPattern.put("PF", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // French Polynesia
		// ibanLaenderXPattern.put("TF", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // French Southern Territories
		// ibanLaenderXPattern.put("WF", Pattern.compile("FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2}")); // Wallis and Futuna Islands

		// ibanLaenderXPattern.put("IM", Pattern.compile("GB[0-9]{2}[A-Z]{4}[0-9]{6}[0-9]{8}")); // Isle of Man
		// ibanLaenderXPattern.put("JE", Pattern.compile("GB[0-9]{2}[A-Z]{4}[0-9]{6}[0-9]{8}")); // Jersey
		// ibanLaenderXPattern.put("GG", Pattern.compile("GB[0-9]{2}[A-Z]{4}[0-9]{6}[0-9]{8}")); // Guernsey

		LAENDER_X_PATTERN = Collections.unmodifiableMap(ibanLaenderXPattern);
	}

	private IBANinternationalVerifier() {
	}

	public static IBANinternationalVerifier getInstance() {
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
				return CustomFieldTypeErrorUtil.getResult(CustomFieldTypeErrorEnum.IBANINTERNATIONAL_LEERZEICHEN_UNGUELTIG,
						errorMsgLocale,
						CustomFieldTypeErrorKeys.UNGUELTIGE_IBANINTERNATIONAL);
			}
			// use the XML representation
			iban = convertResult.getConvertedValue();
		}
		CustomFieldTypeErrorEnum error = checkIban(iban);
		return CustomFieldTypeErrorUtil.getResult(error, errorMsgLocale, CustomFieldTypeErrorKeys.UNGUELTIGE_IBANINTERNATIONAL);
	}

	/**
	 * Validates the specified IBAN and returns the key of the corresponding error message,
	 * or <code>null</code> if the IBAN is valid.
	 */
	private CustomFieldTypeErrorEnum checkIban(final String iban) {
		Matcher matcher = VALID_IBANINTERNATIONAL.matcher(iban);
		if (!matcher.matches()) {
			return CustomFieldTypeErrorEnum.IBANINTERNATIONAL_FORMALER_FEHLER;
		}

		final String laenderkennung = matcher.group(1);
		if (istLaenderkennungGueltig(laenderkennung)) {
			if (!istLandesformatGueltig(laenderkennung, iban)) {
				return CustomFieldTypeErrorEnum.IBANINTERNATIONAL_LANDESFORMAT_FEHLER;
			}
			if (laenderkennung.contentEquals("DE") && !istDeutschesFormatGueltig(iban)) {
				return CustomFieldTypeErrorEnum.IBANINTERNATIONAL_LANDESFORMAT_FEHLER;
			}
		} else {
			// No error is thrown here. For countries added in the future, only the pattern
			// and the Mod97 check should be performed (see EXT-12475).
		}

		if (!IBANVerifier.istPruefzifferGueltig(iban)) {
			return CustomFieldTypeErrorEnum.IBANINTERNATIONAL_PRUEFZIFFER_FEHLER;
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
	 * “0” in positions 13 to 22 should be rejected as invalid.
	 */
	private boolean istDeutschesFormatGueltig(final String iban) {
		String endziffern = iban.substring(12, 22);
		return !endziffern.equals("0000000000");
	}

	@NonNull @Override
	public ICustomFieldTypeConversionResult convertDisplay2Internal(@NonNull String displayValue) {
		String xmlValue = IBANUtils.convertDisplay2Internal(displayValue);

		if (xmlValue != null) {
			return new CustomFieldTypeConversionResultImpl(xmlValue, null);
		} else {
			return new CustomFieldTypeConversionResultImpl(displayValue, CONVERSION_ERROR_IDENTIFIER);
		}
	}

	@NonNull @Override
	public ICustomFieldTypeConversionResult convertInternal2Display(@NonNull String xmlValue) {
		ICustomFieldTypeValidationParam valParam = new CustomFieldTypeValidationParamImpl(Locale.GERMANY,
				CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH, CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH);
		Optional<ICustomFieldTypeCheckError> checkResult = validate(xmlValue, valParam, false);
		if (checkResult.isPresent()) {
			return new CustomFieldTypeConversionResultImpl(xmlValue,
					"Konvertiere nur gültige IBAN (International). " + checkResult.get().getErrorMessage());
		}
		return new CustomFieldTypeConversionResultImpl(IBANUtils.convertInternal2Display(xmlValue), null);
	}

	// A12K-3589 remove this SuppressWarnings
	@SuppressWarnings("removal")
	@Override
	public @NonNull ICustomFieldTypeConversionResult convertDisplay2Internal(@NonNull String displayValue,
			@NonNull Map<String, Object> configData) {
		return convertDisplay2Internal(displayValue);
	}

	// A12K-3589 remove this SuppressWarnings
	@SuppressWarnings("removal")
	@Override
	public @NonNull ICustomFieldTypeConversionResult convertInternal2Display(@NonNull String internalValue,
			@NonNull Map<String, Object> configData) {
		return convertInternal2Display(internalValue);
	}
}
