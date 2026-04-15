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

package com.mgmtp.a12.utils.conversion.internal;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.utils.conversion.ParseError;
import com.mgmtp.a12.utils.conversion.ParseResult;
import com.mgmtp.a12.utils.conversion.a12internal.util.NumberUtil;
import com.mgmtp.a12.utils.conversion.config.NumberConversionConfig;
import com.mgmtp.a12.utils.conversion.impl.DefaultParseResult;
import com.mgmtp.a12.utils.conversion.internal.util.LocalizableUtil;
import com.mgmtp.a12.utils.localization.DataFormatPlaceholder;
import com.mgmtp.a12.utils.localization.DataFormats;
import com.mgmtp.a12.utils.localization.Placeholder;
import com.mgmtp.a12.utils.localization.internal.DefaultDataFormatPlaceholder;

/**
 * Conversion methods for number data types.
 */
public class NumberConversion {

	// List of valid separators
	private static final char[] VALID_SEPARATORS = new char[] { '.', ',' };

	public static final int MAX_ALLOWED_NUMBER_OF_DIGITS = 15;

	/**
	 * Converts the given value from one format to other format among the same data type.<br>
	 * <code>minFractionalDigits</code> from <code>inputFormat</code> is ignored.<br>
	 * <code>decimalPlacesOptional</code> and <code>minFractionalDigits</code> work together:
	 * Only if <code>decimalPlacesOptional==true</code> in <code>srcDataType</code> and <code>decimalPlacesOptional==false</code> in
	 * <code>destDataType</code>,
	 * missing zeroes are added in the decimal part in order to fulfill <code>minFractionalDigits</code> from
	 * <code>destDataType</code>.</li>
	 *
	 * @param value
	 * 		value to be converted
	 * @param srcDataType
	 * 		configuration which describes the given value
	 * @param destDataType
	 * 		configuration to which the given value should be converted
	 * @return the converted string value
	 */
	public static String convert(
			String value,
			NumberConversionConfig srcDataType,
			DataFormats srcDataFormats,
			NumberConversionConfig destDataType,
			DataFormats destDataFormats
	) {
		if (value.isEmpty()) {
			return value;
		}
		char inputDecimalSeparator = srcDataFormats.decimalSeparator();
		Optional<String> srcThousandsSeparator = srcDataFormats.thousandsSeparator();
		if (srcThousandsSeparator.isPresent()) {
			value = value.replaceAll(srcThousandsSeparator.get(), "");
		}
		char outputDecimalSeparator = destDataFormats.decimalSeparator();
		value = value.replace(inputDecimalSeparator, outputDecimalSeparator);
		// just in the case that in the source optional decimal places are allowed and in the destination not, we add missing 0's
		if (srcDataFormats.decimalPlacesOptional() &&
				!destDataFormats.decimalPlacesOptional()
		) {
			int outputMinFractionalDigits = destDataType.minFractionalDigits();
			value = NumberUtil.addDecimalPlacesIfNecessary(
					value,
					String.valueOf(outputDecimalSeparator),
					NumberConversion.decimalIndexForValidation(outputDecimalSeparator),
					outputMinFractionalDigits
			);
		}
		Optional<String> destThousandsSeparator = destDataFormats.thousandsSeparator();
		if (destThousandsSeparator.isPresent()) {
			value = NumberUtil.addThousandsSeparator(
					value,
					destThousandsSeparator.get(),
					outputDecimalSeparator
			);
		}
		return value;
	}

	/**
	 * See also the description of defaultValueConversion#parseValue.
	 */
	public static Object convertStringToNumber(
			String value,
			NumberConversionConfig srcDataType,
			DataFormats srcDataFormats
	) {
		char decimalSeparator = srcDataFormats.decimalSeparator();
		boolean leadingZerosAllowed = srcDataType.leadingZerosAllowed();
		return NumberUtil.createBigDecimalIfPossible(
				leadingZerosAllowed,
				value,
				decimalSeparator
		);
	}

	/**
	 * See also the description of defaultValueConversion#parseValue.
	 */
	public static ParseResult parseNumber(
			String value,
			NumberConversionConfig srcDataType,
			DataFormats srcDataFormats
	) {
		if (StringUtils.isEmpty(value)) {
			return DefaultParseResult.ofValue(null);
		}
		String thousandsSeparator = srcDataFormats.thousandsSeparator().orElse(null);
		// the implementation in the sanity checks is based on the value without thousands separator
		if (!StringUtils.isEmpty(thousandsSeparator)) {
			value = value.replace(thousandsSeparator, "");
		}
		Optional<ParseError> error = NumberConversion.sanityCheck(value, srcDataFormats);
		if (error.isEmpty()) {
			return DefaultParseResult.ofValue(NumberConversion.convertStringToNumber(value, srcDataType, srcDataFormats));
		} else {
			return DefaultParseResult.ofError(error.get());
		}
	}

	/**
	 * See also the description of defaultValueConversion#formatValue.
	 */
	public static String convertNumberToString(
			Object value,
			NumberConversionConfig destDataType,
			DataFormats destDataFormats
	) {
		// null is already handled
		char decimalSeparator = destDataFormats.decimalSeparator();
		boolean leadingZerosAllowed = destDataType.leadingZerosAllowed();
		if (
				value instanceof String strVal &&
						NumberUtil.areAllowedLeadingZerosPresent(
								leadingZerosAllowed,
								strVal,
								decimalSeparator
						)
		) {
			return strVal;
		}
		if (!(value instanceof BigDecimal bigDecimal)) {
			throw new IllegalArgumentException(
					"The type of the value (" + value.getClass().getSimpleName() + ") is not a BigDecimal"
			);
		}
		String thousandsSeparator = destDataFormats.thousandsSeparator().orElse(null);
		NumberConversion.checkSeparators(decimalSeparator, thousandsSeparator);
		int fractionalDigits = NumberConversion.determineFractionalDigits(bigDecimal, destDataType, destDataFormats);
		String resultWithDot = bigDecimal.setScale(fractionalDigits).toPlainString();
		String result = resultWithDot.replace('.', decimalSeparator);
		if (!StringUtils.isEmpty(thousandsSeparator)) {
			result = NumberUtil.addThousandsSeparator(
					result,
					thousandsSeparator,
					decimalSeparator
			);
		}
		return result;
	}

	private static int determineFractionalDigits(
			BigDecimal bigDecimal,
			NumberConversionConfig numberDataType,
			DataFormats dataFormats
	) {
		int minScale = bigDecimal.stripTrailingZeros().scale();
		int minFractionalDigits = dataFormats.decimalPlacesOptional()
				? 0
				: numberDataType.minFractionalDigits();
		int currentFractionalDigits = Math.max(minScale, minFractionalDigits);
		// in case the number is too big (more than 15 digits)
		int maxAllowedFractionalDigits = Math.max(
				0,
				NumberConversion.MAX_ALLOWED_NUMBER_OF_DIGITS - (bigDecimal.precision() - minScale)
		);
		return Math.min(currentFractionalDigits, maxAllowedFractionalDigits);
	}

	private static Optional<ParseError> sanityCheck(
			String value,
			DataFormats srcDataFormats
	) {
		char decimalSeparator = srcDataFormats.decimalSeparator();
		String thousandsSeparator = srcDataFormats.thousandsSeparator().orElse(null);
		NumberConversion.checkSeparators(decimalSeparator, thousandsSeparator);

		int index = NumberConversion.decimalIndexForValidation(decimalSeparator);
		if (!NumberUtil.DECIMAL_PLACES_OPTIONAL_PRECHECK_PATTERN[index].matcher(value).matches()) {
			Map<Locale, String> errorTextDefaults = Map.of(
					Locale.GERMAN, "Es sind nur Zahlen erlaubt.",
					Locale.ENGLISH, "Only numbers are allowed.",
					Locale.FRENCH, "Seuls les nombres sont autorisés.",
					Locale.of("nl"), "Gelieve een getal invullen."
			);
			return Optional.of(NumberConversion.createParseErrorForNumber(
					"ZAHL_MIT_UNGUELTIGEN_ZEICHEN",
					"zahlHatUngueltigeZeichen",
					errorTextDefaults,
					decimalSeparator));
		}

		// We just want to check the number of digits, 15 are the maximum allowed
		// Decimal separators and signs must be filtered out
		String digits = value
				.replace(String.valueOf(decimalSeparator), "")
				.replace("-", "");
		if (digits.length() > NumberConversion.MAX_ALLOWED_NUMBER_OF_DIGITS) {
			Map<Locale, String> errorTextDefaults = Map.of(
					Locale.GERMAN, "Es sind maximal 15 Stellen erlaubt.",
					Locale.ENGLISH, "Only values with up to 15 digits are allowed.",
					Locale.FRENCH, "Seules les valeurs contenant jusqu'à 15 chiffres sont autorisées.",
					Locale.of("nl"), "Er mogen alleen waardes van maximaal 15 cijfers worden ingevuld."
			);
			return Optional.of(NumberConversion.createParseErrorForNumber(
					"STELLIGKEIT_ZU_LANG",
					"stelligkeitZuLang",
					errorTextDefaults,
					decimalSeparator));
		}
		return Optional.empty();
	}

	private static void checkSeparators(
			char decimalSeparator,
			String thousandsSeparator
	) {
		if (NumberConversion.VALID_SEPARATORS[0] != decimalSeparator &&
				NumberConversion.VALID_SEPARATORS[1] != decimalSeparator) {
			throw new IllegalArgumentException(
					"Invalid number conversion configuration: The decimal separator '" +
							decimalSeparator + "' is invalid."
			);
		}

		if (!StringUtils.isEmpty(thousandsSeparator) &&
				thousandsSeparator.charAt(0) == decimalSeparator) {
			throw new IllegalArgumentException("Decimal separator and thousands separator must not be the same!");
		}
	}

	/**
	 * Return the index (dot or comma) for validation.
	 * In the arrays the dot notation is always stored in value 0 and
	 * the comma notation in value 1.
	 *
	 * @return index that returns the relevant representation in the array.
	 */
	private static int decimalIndexForValidation(char decimalSeparator) {
		return '.' == decimalSeparator ? 0 : 1;
	}

	private static ParseError createParseErrorForNumber(
			String errorKey, //statt RuntimeFormalErrorEnum,
			String errorId,
			Map<Locale, String> errorTextDefaults,
			char decimalSeparator
	) {
		DataFormatPlaceholder decimalSeparatorPlaceholder = new DefaultDataFormatPlaceholder(decimalSeparator,
				DataFormatPlaceholder.DataFormat.decimalSeparator);
		Map<String, Placeholder> args = Map.of("decimalSeparator", decimalSeparatorPlaceholder);
		return LocalizableUtil.createParseError(
				errorKey,
				errorId,
				args,
				errorTextDefaults
		);
	}

}
