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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.utils.conversion.ParseResult;
import com.mgmtp.a12.utils.conversion.impl.DefaultParseResult;
import com.mgmtp.a12.utils.localization.DataFormats;

/**
 * Conversion methods for boolean data types with two value logic (true/null).
 */
public class ConfirmConversion {

	/**
	 * Converts the given value from one format to other format among the same data type.<br>
	 * If the given value cannot be assigned to <code>trueValue</code> from <code>inputFormat</code>, the original value is
	 * returned.
	 *
	 * @param value
	 * 		value to be converted
	 * @param srcDataFormats
	 * 		configuration which describes the given value
	 * @param destDataFormats
	 * 		configuration to which the given value should be converted
	 * @return the converted string value
	 */
	public static String convert(
			String value,
			DataFormats srcDataFormats,
			DataFormats destDataFormats
	) {
		if (value.isEmpty()) {
			return value;
		}
		String srcTrueValue = ConfirmConversion.getTrueValue(srcDataFormats);
		if (srcTrueValue.equals(value)) {
			return ConfirmConversion.getTrueValue(destDataFormats);
		}
		return value;
	}

	/**
	 * See also the description of defaultValueConversion#parseValue.
	 */
	public static ParseResult convertStringToConfirm(
			String value,
			DataFormats srcDataFormats
	) {
		String srcTrueValue = ConfirmConversion.getTrueValue(srcDataFormats);
		if (srcTrueValue.equals(value)) {
			return DefaultParseResult.ofValue(true);
		} else {
			return DefaultParseResult.ofValue(null);
		}
	}

	/**
	 * See also the description of defaultValueConversion#formatValue.
	 */
	public static Optional<String> convertConfirmToString(
			Object value,
			DataFormats destDataFormats
	) {
		// null is already handled
		if (!(value instanceof Boolean)) {
			throw new IllegalArgumentException(
					"The type of the value (" + value.getClass().getSimpleName() + ") is not a boolean");
		} else if (!((boolean) value)) {
			throw new IllegalArgumentException("false is not a valid value for a confirm type.");
		}
		return Optional.of(ConfirmConversion.getTrueValue(destDataFormats));
	}

	/**
	 * @return the definition of the true value if defined, otherwise the default value <code>true</code>.
	 */
	public static String getTrueValue(DataFormats dataType) {
		return !StringUtils.isEmpty(dataType.trueValue()) ? dataType.trueValue() : "true";
	}
}
