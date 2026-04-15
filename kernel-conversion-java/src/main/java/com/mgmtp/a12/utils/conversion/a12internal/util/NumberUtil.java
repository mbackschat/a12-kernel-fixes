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
package com.mgmtp.a12.utils.conversion.a12internal.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.model.utils.Experimental;

/**
 * Utility class for handling number types.
 */
@Experimental
public class NumberUtil {

	/**
	 * Regular Expression which should be matched for numbers (optional minus sign and dot or comma as decimal separator).
	 * Plus signs are not allowed.
	 */
	public static final Pattern[] NUMBER_PATTERN = {
			Pattern.compile("[-]?[0-9]+(\\.[0-9]+)?"),
			Pattern.compile("[-]?[0-9]+(,[0-9]+)?")
	};

	public static final Pattern[] DECIMAL_PLACES_OPTIONAL_PRECHECK_PATTERN = {
			Pattern.compile("[-]?[0-9]+(\\.[0-9]*)?"),
			Pattern.compile("[-]?[0-9]+(,[0-9]*)?")
	};

	private NumberUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * @param value
	 * 		value to check and enhance if necessary.
	 * @param decimalSeparator
	 * 		decimal separator which the value could contain.
	 * @param index
	 * 		the index representing the dot notation (0) or the comma notation (1).
	 * @param minFractionalDigits
	 * 		the minimum number of fractional digits.
	 * @return the given value with added zeros as decimal places, if the specified minimum of fractional digits is > 0.
	 */
	public static String addDecimalPlacesIfNecessary(
			String value,
			String decimalSeparator,
			int index,
			int minFractionalDigits
	) {
		// If the input is not a valid number before adding zeroes it will also not be valid afterwards.
		// Hence we should just return the original input s.t. the resulting error message does not change.
		Pattern regexp = NumberUtil.DECIMAL_PLACES_OPTIONAL_PRECHECK_PATTERN[index];
		if (!regexp.matcher(value).matches()) {
			return value;
		}

		//Escape character since "." would not work.
		String[] parts = value.split("\\" + decimalSeparator);
		if (minFractionalDigits > 0) {
			if (parts.length == 2) {
				// If the given number has a decimal separator, we add zeroes if necessary.
				return value + StringUtils.repeat("0", minFractionalDigits - parts[1].length());
			} else if (parts.length == 1) {
				// If there is no decimal separator so far we need to add one before adding zeroes.
				return parts[0] + decimalSeparator + StringUtils.repeat("0", minFractionalDigits);
			}
		}
		// If we did not return anything yet, there is no need to add additional decimal places
		// (or there is something seriously wrong with the input string, but this case should be handled by validiereFormat)
		return value;
	}

	/**
	 * @param value
	 * 		value to check and enhance if necessary.
	 * @param thousandsSeparator
	 * 		thousands separator which the returned value should contain.
	 * @param decimalSeparator
	 * 		decimal separator which the value could contain.
	 * @return the given value with the given thousands separators added to it.
	 */
	public static String addThousandsSeparator(String value, String thousandsSeparator, char decimalSeparator) {
		int decimalIndex = value.indexOf(decimalSeparator) != -1 ? value.indexOf(decimalSeparator) : value.length();
		// here we determine where the number starts (without minus)
		int numberStart = value.indexOf("-") + 1;
		StringBuilder sb = new StringBuilder(value);
		// Add the separators from back to front to not shuffle the places
		for (int i = decimalIndex - 3; i > numberStart; i -= 3) {
			sb.insert(i, thousandsSeparator);
		}

		return sb.toString();
	}

	/**
	 * @param value
	 * 		value to check
	 * @param decimalSeparator
	 * 		decimal separator which the value could contain
	 * @return {@code true} if <b>all</b> following conditions are met:
	 * 		<ol>
	 * 		<li>the character after the sign (if present) is {@code 0}</li>
	 * 		<li>there are more characters after the {@code 0} found in the previous step</li>
	 * 		<li>the character right after the {@code 0} from the first step is not the decimal
	 * 		separator</li>
	 * 		</ol>
	 * 		Otherwise {@code false}
	 */
	public static boolean containsLeadingZeros(String value, char decimalSeparator) {
		int indexAfterSign = (value.charAt(0) == '+' || value.charAt(0) == '-') ? 1 : 0;
		return value.charAt(indexAfterSign) == '0'
				&& value.length() > indexAfterSign + 1
				&& value.charAt(indexAfterSign + 1) != decimalSeparator;
	}

	/**
	 * @return {@code true} if leading zeros are allowed and the value contains leading zeros (see
	 * 		also {@link #containsLeadingZeros(String, char)}, otherwise {@code false}.
	 */
	public static boolean areAllowedLeadingZerosPresent(boolean leadingZerosAllowed, String value, char decimalSeparator) {
		return leadingZerosAllowed && containsLeadingZeros(value, decimalSeparator);
	}

	/**
	 * @return the corresponding {@link BigDecimal} taking into account the decimal set in the given
	 *        {@code metaData}, or {@code null} if the given value is empty.
	 */
	public static BigDecimal createBigDecimal(String value, char decimalSeparator) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		String valueWithDot = value.replace(decimalSeparator, '.');
		return new BigDecimal(valueWithDot);
	}

	/**
	 * Tries to creates a {@link BigDecimal} as follows:
	 * <ol>
	 * <li>check if the given value is a valid number at all</li>
	 * <li>if {@code leadingZerosAllowed} is 'true' and the value contains leading zeros, the
	 * original string value will be returned</li>
	 * <li>otherwise the corresponding {@link BigDecimal}</li>
	 * </ol>
	 *
	 * @return the corresponding {@link BigDecimal} or the original string value (see method
	 * 		description).
	 */
	public static Object createBigDecimalIfPossible(boolean leadingZerosAllowed, String value, char decimalSeparator) {
		/*
		 * first we try to convert the given value to ensure that it is a valid number,
		 * we have to intercept inputs like "000test" which would be a wrong input
		 */
		BigDecimal result = createBigDecimal(value, decimalSeparator); // we convert the value first to test if it is a valid number
		if (result != null && areAllowedLeadingZerosPresent(leadingZerosAllowed, value, decimalSeparator)) {
			return value;
		} else {
			return result;
		}
	}

}
