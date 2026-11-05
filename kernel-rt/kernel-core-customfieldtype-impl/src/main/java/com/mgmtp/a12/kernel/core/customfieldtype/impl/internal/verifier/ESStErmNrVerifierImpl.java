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
package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.verifier;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeConversionResult;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.CustomFieldTypeConversionResultImpl;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.CustomFieldTypeValidationParamImpl;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorEnum;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorKeys;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorUtil;

import lombok.NonNull;

/**
 * Validates the ESSt-Ermittlungsnummer.
 * <p>
 * <b>Internal format ({@code FFFF8BBBJJJJUUUUUKKKTTTP}, 24 digits):</b>
 * <ul>
 * <li>{@code FFFF} Bundesfinanzamtsnummer (incl. Länderkennung)</li>
 * <li>{@code 8} Merkzeichen (constant {@code 8} at position 5)</li>
 * <li>{@code BBB} Bezirksnummer</li>
 * <li>{@code JJJJ} Jahr</li>
 * <li>{@code UUUUU} Unterscheidungsnummer</li>
 * <li>{@code KKK} Kennzeichnung für zusammen gehörende Fälle</li>
 * <li>{@code TTT} Unterscheidungsnummer für die einzelne Person (Teilfallnummer)</li>
 * <li>{@code P} Prüfziffer</li>
 * </ul>
 * <b>Display format ({@code FFFF/BBB/JJJJ/UUUUU/KKK/000P}):</b> the Merkzeichen {@code 8} is dropped and
 * the {@code TTT} group is always rendered as {@code 000}.
 * <p>
 * The validity check performs, in this order: a formal check of the 24-digit structure (including the
 * fixed Merkzeichen {@code 8}), a validity check of the Bundesfinanzamtsnummer ({@code FFFF}), a check
 * that the Unterscheidungsnummer ({@code UUUUU}) does not consist solely of zeros, and the Prüfziffer
 * ({@code P}) verification. The check digit uses the following procedure: the Ermittlungsnummer is transcoded to
 * a 13-digit number {@code FFF+BBB+ZZ+UUU+TT} (country code, Merkzeichen and {@code KKK} are
 * dropped, {@code JJJJ} becomes a two-digit year reference relative to 1980, only the last three digits
 * of {@code UUUUU} and the last two digits of {@code TTT} are kept), then each digit is multiplied by a
 * fixed weight and the check digit equals the weighted sum modulo 10.
 */
public class ESStErmNrVerifierImpl implements ICustomFieldValidator {

	/**
	 * The internal 24-digit format. Capturing groups: FFFF, BBB, JJJJ, UUUUU, KKK, TTT, P. The Merkzeichen
	 * at position 5 is required to be {@code 8}.
	 */
	private static final Pattern INTERNAL_FORMAT =
			Pattern.compile("([0-9]{4})8([0-9]{3})([0-9]{4})([0-9]{5})([0-9]{3})([0-9]{3})([0-9])");

	/**
	 * The display format {@code FFFF/BBB/JJJJ/UUUUU/KKK/TTTP}. Capturing groups: FFFF, BBB, JJJJ, UUUUU,
	 * KKK, TTT, P (TTTP is captured in 2 separate groups).
	 */
	private static final Pattern DISPLAY_FORMAT =
			Pattern.compile("([0-9]{4})/([0-9]{3})/([0-9]{4})/([0-9]{5})/([0-9]{3})/([0-9]{3})([0-9])");

	/** The weights of the check-digit procedure (applied to the 13-digit number). */
	private static final int[] CHECK_DIGIT_WEIGHTS = { 4, 8, 9, 1, 2, 3, 6, 7, 3, 2, 1, 0, 0 };

	/** The start year of the two-digit year reference used by the check-digit procedure. */
	private static final int YEAR_REFERENCE_START = 1980;

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static final String CONVERSION_ERROR_IDENTIFIER = "CONVERSION_ERROR";

	private final @NonNull IFadat fadat;

	public ESStErmNrVerifierImpl(@NonNull IFadat fadat) {
		this.fadat = fadat;
	}

	@Override
	public @NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String ermnr,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	) {
		Locale errorMsgLocale = valParam.getErrorMsgLocale();
		if (isDisplayValue) {// the value must be converted to the internal format before validation
			ICustomFieldTypeConversionResult convertResult = convertDisplay2Internal(ermnr);
			if (convertResult.getErrorMessage().isPresent()) {// conversion error
				return CustomFieldTypeErrorUtil.getResult(CustomFieldTypeErrorEnum.ESSTERMNR_FORMAT_FEHLER, errorMsgLocale,
						CustomFieldTypeErrorKeys.UNGUELTIGE_ESSTERMNR);
			}
			ermnr = convertResult.getConvertedValue();
		}
		CustomFieldTypeErrorEnum error = checkErmNr(ermnr);
		return CustomFieldTypeErrorUtil.getResult(error, errorMsgLocale, CustomFieldTypeErrorKeys.UNGUELTIGE_ESSTERMNR);
	}

	/**
	 * Checks the given Ermittlungsnummer (internal format) and returns the matching error, or
	 * {@link CustomFieldTypeErrorEnum#KEIN_FEHLER} if it is valid.
	 */
	private CustomFieldTypeErrorEnum checkErmNr(String ermnr) {
		Matcher matcher = INTERNAL_FORMAT.matcher(ermnr);
		if (!matcher.matches()) {
			return CustomFieldTypeErrorEnum.ESSTERMNR_FORMAT_FEHLER;
		}

		int federalTaxOfficeNumber = Integer.parseInt(matcher.group(1));
		if (!FadatLookupUtil.existsFederalTaxOfficeNumber(fadat, federalTaxOfficeNumber)) {
			return CustomFieldTypeErrorEnum.ESSTERMNR_FINANZAMT_FEHLER;
		}

		String unterscheidungsnummer = matcher.group(4);
		if ("00000".equals(unterscheidungsnummer)) {
			return CustomFieldTypeErrorEnum.ESSTERMNR_UNTERSCHEIDUNGSNUMMER_FEHLER;
		}

		if (!isCheckDigitCorrect(matcher)) {
			return CustomFieldTypeErrorEnum.ESSTERMNR_PRUEFZIFFER_FEHLER;
		}
		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Verifies the check digit ({@code P}) of a matched internal Ermittlungsnummer.
	 */
	private static boolean isCheckDigitCorrect(Matcher matcher) {
		String ffff = matcher.group(1);
		String bbb = matcher.group(2);
		String jjjj = matcher.group(3);
		String uuuuu = matcher.group(4);
		String ttt = matcher.group(6);
		int checkDigit = Integer.parseInt(matcher.group(7));

		// Drop the country code (first digit of FFFF), keep BBB, encode the year
		// as a two-digit reference relative to 1980, keep only the last three digits of UUUUU and the last
		// two digits of TTT. The Merkzeichen and KKK do not contribute to the check digit.
		String fff = ffff.substring(1);
		int year = Integer.parseInt(jjjj);
		int yearReference = Math.max(0, year - YEAR_REFERENCE_START);
		// The year reference is two digits; years before 1980 map to 0. Years from 2080 onwards are
		// outside the specified range and wrap around (kept two-digit so the number stays 13 digits).
		String zz = String.format("%02d", yearReference % 100);
		String uuu = uuuuu.substring(2);
		String tt = ttt.substring(1);

		String nrw = fff + bbb + zz + uuu + tt;

		int sum = 0;
		for (int i = 0; i < CHECK_DIGIT_WEIGHTS.length; i++) {
			sum += CHECK_DIGIT_WEIGHTS[i] * (nrw.charAt(i) - '0');
		}
		return checkDigit == sum % 10;
	}

	@NonNull @Override
	public ICustomFieldTypeConversionResult convertDisplay2Internal(@NonNull String displayValue) {
		Matcher matcher = DISPLAY_FORMAT.matcher(displayValue);
		if (!matcher.matches()) {
			return new CustomFieldTypeConversionResultImpl(displayValue, CONVERSION_ERROR_IDENTIFIER);
		}
		// Reinsert the Merkzeichen "8" after FFFF; the remaining groups (incl. KKK and TTTP) keep their order.
		String internalValue = matcher.group(1) + '8' + matcher.group(2) + matcher.group(3)
				+ matcher.group(4) + matcher.group(5) + matcher.group(6) + matcher.group(7);
		return new CustomFieldTypeConversionResultImpl(internalValue, null);
	}

	@NonNull @Override
	public ICustomFieldTypeConversionResult convertInternal2Display(@NonNull String internalValue) {
		ICustomFieldTypeValidationParam valParam = new CustomFieldTypeValidationParamImpl(Locale.GERMANY,
				CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH,
				CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH);
		Optional<ICustomFieldTypeCheckError> checkResult = validate(internalValue, valParam, false);
		if (checkResult.isPresent()) {
			return new CustomFieldTypeConversionResultImpl(internalValue,
					"Konvertiere nur gültige Ermittlungsnummern. " + checkResult.get().getErrorMessage());
		}
		Matcher matcher = INTERNAL_FORMAT.matcher(internalValue);
		matcher.matches(); // true
		// Drop the Merkzeichen "8" and always render the TTT group as "000".
		return new CustomFieldTypeConversionResultImpl(
				matcher.group(1) + '/' + matcher.group(2) + '/' + matcher.group(3) + '/'
						+ matcher.group(4) + '/' + matcher.group(5) + "/000" + matcher.group(7),
				null);
	}
}
