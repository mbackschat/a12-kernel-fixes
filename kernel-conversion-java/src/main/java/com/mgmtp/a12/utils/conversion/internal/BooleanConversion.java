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

public class BooleanConversion {

	/**
	 * Converts the given value from one format to other format among the same data type.<br>
	 * If the given value cannot be assigned to either <code>trueValue</code> or <code>falseValue</code> from
	 * <code>inputFormat</code>,
	 * the original value is returned.
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
		String srcTrueValue = BooleanConversion.getTrueValue(srcDataFormats);
		if (srcTrueValue.equals(value)) {
			return BooleanConversion.getTrueValue(destDataFormats);
		}
		String srcFalseValue = BooleanConversion.getFalseValue(srcDataFormats);
		if (srcFalseValue.equals(value)) {
			return BooleanConversion.getFalseValue(destDataFormats);
		}
		return value;
	}

	/**
	 * See also the description of defaultValueConversion#parseValue.
	 */
	public static ParseResult convertStringToBoolean(
			String value,
			DataFormats srcDataFormats
	) {
		String srcTrueValue = BooleanConversion.getTrueValue(srcDataFormats);
		if (srcTrueValue.equals(value)) {
			return DefaultParseResult.ofValue(true);
		}
		String srcFalseValue = BooleanConversion.getFalseValue(srcDataFormats);
		if (srcFalseValue.equals(value)) {
			return DefaultParseResult.ofValue(false);
		}
		return DefaultParseResult.ofValue(null);
	}

	/**
	 * See also the description of defaultValueConversion#formatValue.
	 */
	public static Optional<String> convertBooleanToString(
			Object value,
			DataFormats destDataFormats
	) {
		// null is already handled
		if (!(value instanceof Boolean)) {
			throw new IllegalArgumentException(
					"The type of the value (" + value.getClass().getSimpleName() + ") is not a boolean");
		}
		if ((boolean) value) {
			return Optional.of(BooleanConversion.getTrueValue(destDataFormats));
		} else {
			return Optional.of(BooleanConversion.getFalseValue(destDataFormats));
		}
	}

	/**
	 * @return the definition of the true value if defined, otherwise the default value <code>true</code>.
	 */
	public static String getTrueValue(DataFormats dataType) {
		return !StringUtils.isEmpty(dataType.trueValue()) ? dataType.trueValue() : "true";
	}

	/**
	 * @return the definition of the false value if defined, otherwise the default value <code>false</code>.
	 */
	public static String getFalseValue(DataFormats dataType) {
		return !StringUtils.isEmpty(dataType.falseValue()) ? dataType.falseValue() : "false";
	}

}
