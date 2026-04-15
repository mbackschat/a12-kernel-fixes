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

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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

import lombok.NonNull;

/**
 * Validates the ID number (IdNr). Starting from 01.01.2008, the Federal Ministry of Finance introduced
 * a uniform IDNr (identification number) according to § 139 AO. This identification feature is intended
 * to replace the old tax number and is unique for each taxpayer.
 * <p>
 * <b>Structure of the IDNr:</b><br>
 * The identification feature is an eleven-digit number sequence, the eleventh digit is a
 * check digit. Furthermore, it must also meet the following criteria:
 * <ul>
 * <li>No leading zeros allowed, unless it is a test identification number (only allowed with a valid
 * test marker).</li>
 * <li>Positions 2 to 10 consist of digits between 0 and 9.</li>
 * <li>The eleventh digit is a check digit and is calculated from positions 1 – 10 using the
 * check algorithm specified below.</li>
 * <li>In the first 10 positions of the identification number, exactly 1 digit must appear twice; the
 * position of the repetition is arbitrary. From 1.1.2016 (implemented in ER-23), this one digit
 * may also appear three times instead of just twice. The digits of the remaining 7 positions
 * must then not repeat, analogous to the double occurrence rule.</li>
 * </ul>
 * </p>
 * <p>
 * To distinguish between the previous tax number and the identification number, no identification
 * number with leading digits 0, 1, 2, or 3 will be issued for a certain transition period.
 * </p>
 * <b>Examples:</b>
 * <table style="border: 1px solid black; border-collapse: separate; border-spacing: 0.2em;">
 * <tr>
 * <th style="border: 1px solid black; padding: 0.2em 0.5em;">ID number without check digit</th>
 * <th style="border: 1px solid black; padding: 0.2em 0.5em;">Check digit</th>
 * <th style="border: 1px solid black; padding: 0.2em 0.5em;">Double/Triple digit</th>
 * <th style="border: 1px solid black; padding: 0.2em 0.5em;">Single digits</th>
 * </tr>
 * <tr>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">4123450896</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">7</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">4</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">1,2,3,5,0,8,9,6</td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">5549267083</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">6</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">5</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">4,9,2,6,7,0,8,3</td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">0247629135(TestID)</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">8</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">2</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">0,4,7,6,9,1,3,5</td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">6195362647</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">7</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">6 (triple)</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">1,9,5,3,2,4,7</td>
 * </tr>
 * </table>
 */
@SuppressWarnings("removal")
public class IdNrVerifier implements ICustomFieldValidator {

	private static final Pattern RE_VALID_IDNR = Pattern.compile("[0-9]{11}");

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static final String CONVERSION_ERROR_IDENTIFIER = "CONVERSION_ERROR";

	private final boolean acceptTestIdNrs;

	private IdNrVerifier(boolean acceptTestIdNrs) {
		this.acceptTestIdNrs = acceptTestIdNrs;
	}

	/**
	 * @return the instance of {@link ICustomFieldValidator}.
	 *
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use {@link #create(boolean)}} instead.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	public static IdNrVerifier getInstance() {
		return new IdNrVerifier(false);
	}

	/**
	 * @param acceptTestIdNrs
	 *        {@code true} ensures that a German ID number starting with "0" is considered valid. This is only
	 * 		appropriate for testing purposes. {@code false} ensures that such test numbers are not recognized as valid,
	 * 		which is the standard behavior.
	 * @return the instance of {@link ICustomFieldValidator}.
	 */
	public static ICustomFieldValidator create(boolean acceptTestIdNrs) {
		return new IdNrVerifier(acceptTestIdNrs);
	}

	@Override
	public @NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String idnr,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	) {
		Locale errorMsgLocale = valParam.getErrorMsgLocale();
		if (isDisplayValue) {// the value must be converted to XML format before validation
			ICustomFieldTypeConversionResult convertResult = convertDisplay2Internal(idnr);

			if (convertResult.getErrorMessage().isPresent()) {// conversion error
				return CustomFieldTypeErrorUtil.getResult(CustomFieldTypeErrorEnum.IDNR_LEERZEICHEN_UNGUELTIG, errorMsgLocale,
						CustomFieldTypeErrorKeys.UNGUELTIGE_ID_NUMMER);
			}
			// use the XML representation
			idnr = convertResult.getConvertedValue();
		}
		CustomFieldTypeErrorEnum error = checkIdNr(idnr);
		return CustomFieldTypeErrorUtil.getResult(error, errorMsgLocale,
				(CustomFieldTypeErrorEnum.IDNR_NUR_FUER_TEST == error) ?
						CustomFieldTypeErrorKeys.TEST_ID_NUMMER :
						CustomFieldTypeErrorKeys.UNGUELTIGE_ID_NUMMER);
	}

	/**
	 * Validates the specified IdNr and returns the key of the corresponding error message,
	 * or <code>null</code> if the IdNr is valid.
	 */
	private CustomFieldTypeErrorEnum checkIdNr(String idnr) {
		if (idnr.length() != 11) {
			return CustomFieldTypeErrorEnum.IDNR_LAENGE_UNGUELTIG;
		}
		if (!RE_VALID_IDNR.matcher(idnr).matches()) {
			return CustomFieldTypeErrorEnum.IDNR_NICHT_NUMERISCH;
		}
		if (!checkZiffernfolge(idnr)) {
			return CustomFieldTypeErrorEnum.IDNR_ZIFFERNFOLGE_FEHLER;
		}
		if (!checkKorrektePruefziffer(idnr)) {
			return CustomFieldTypeErrorEnum.IDNR_PRUEFZIFFER_UNGUELTIG;
		}
		if (idnr.startsWith("0")) {
			if (acceptTestIdNrs) {
				return CustomFieldTypeErrorEnum.KEIN_FEHLER;
			} else {
				// This remains for compatibility reasons and will be removed with A12K-3589.
				// After that, a normal error should be returned and all special handling in the runtime can be eliminated.
				return CustomFieldTypeErrorEnum.IDNR_NUR_FUER_TEST;
			}
		}

		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	private boolean checkKorrektePruefziffer(String idnr) {
		final int ten = 10;
		final int eleven = 11;

		char[] idnrChars = idnr.toCharArray();

		int remainderModTen = 0;
		int remainderModEleven = ten;
		int digit = 0;
		for (int counter = 0; counter < 10; counter++) {
			digit = Character.getNumericValue(idnrChars[counter]);
			remainderModTen = (digit + remainderModEleven) % ten;
			if (remainderModTen == 0) {
				remainderModTen = ten;
			}
			remainderModEleven = (2 * remainderModTen) % eleven;
		}

		int checkCipher = eleven - remainderModEleven;
		if (checkCipher == 10) {
			checkCipher = 0;
		}
		return checkCipher == Character.getNumericValue(idnrChars[10]);
	}

	private boolean checkZiffernfolge(String idnr) {
		char[] w = idnr.toCharArray();
		// The repetitions of the digits
		int[] wiederholungen = new int[10];

		// 11th position is the check digit and is not checked
		for (int i = 0; i < 10; i++) {
			int ziffer = Character.getNumericValue(w[i]);
			wiederholungen[ziffer]++;
		}

		final int notSet = -1;
		int wiederholteZiffer = notSet;
		for (int i = 0; i < 10; i++) {
			if (wiederholungen[i] < 2) {
				continue;
			}
			if (wiederholungen[i] > 3) {
				// Only up to triple repetition allowed
				return false;
			}
			if (wiederholteZiffer != notSet) {
				// Only one digit may appear twice or three times
				return false;
			} else {
				wiederholteZiffer = i;
			}
		}
		if (wiederholteZiffer == notSet) {
			// No repeated digit.
			return false;
		}

		String idnrOhnePruefziffer = idnr.substring(0, (idnr.length() - 1));
		if (idnrOhnePruefziffer.indexOf(String.format("%d%d%d", wiederholteZiffer, wiederholteZiffer, wiederholteZiffer)) != -1) {
			// If three identical digits exist at positions 1 to 10,
			// these identical digits must never be at directly consecutive positions
			// (2 identical digits may, however, be directly consecutive).
			return false;
		}

		return true;
	}

	@NonNull @Override
	public ICustomFieldTypeConversionResult convertDisplay2Internal(@NonNull String displayValue) {
		String xmlValue;

		if (displayValue.indexOf(' ') != -1) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < displayValue.length(); i++) {
				if (i == 2 || i == 6 || i == 10) {
					if (displayValue.charAt(i) != ' ') {
						return new CustomFieldTypeConversionResultImpl(displayValue, CONVERSION_ERROR_IDENTIFIER);
					}
				} else {
					if (displayValue.charAt(i) == ' ') {
						return new CustomFieldTypeConversionResultImpl(displayValue, CONVERSION_ERROR_IDENTIFIER);
					}
					sb.append(displayValue.charAt(i));
				}
			}
			xmlValue = sb.toString();
		} else {
			xmlValue = displayValue;
		}
		return new CustomFieldTypeConversionResultImpl(xmlValue, null);
	}

	@NonNull @Override
	public ICustomFieldTypeConversionResult convertInternal2Display(@NonNull String xmlValue) {
		ICustomFieldTypeValidationParam valParam = new CustomFieldTypeValidationParamImpl(Locale.GERMANY,
				CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH, CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH);
		Optional<ICustomFieldTypeCheckError> checkResult = validate(xmlValue, valParam, false);
		if (checkResult.isPresent() &&
				!checkResult.get().getErrorKey().equals(CustomFieldTypeErrorKeys.TEST_ID_NUMMER)) {
			return new CustomFieldTypeConversionResultImpl(xmlValue,
					"Konvertiere nur gültige Identifikationsnummern. " + checkResult.get().getErrorMessage());
		}
		return new CustomFieldTypeConversionResultImpl(
				xmlValue.substring(0, 2) + ' ' + xmlValue.substring(2, 5) + ' ' + xmlValue.substring(5, 8)
						+ ' ' + xmlValue.substring(8), null);
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
