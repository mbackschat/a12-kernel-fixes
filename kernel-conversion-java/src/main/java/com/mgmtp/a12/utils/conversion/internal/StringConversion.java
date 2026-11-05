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

import com.mgmtp.a12.utils.conversion.config.CustomStringConversionConfig;
import com.mgmtp.a12.utils.conversion.config.EnumerationConversionConfig;
import com.mgmtp.a12.utils.conversion.config.StringConversionConfig;

/**
 * Conversion methods for string data types.
 */
public class StringConversion {

	/**
	 * Returns the given value, if it is a string. Otherwise an error is thrown.
	 * It is used for all string conversions (defaultValueConversion#parseValue and defaultValueConversion#formatValue).
	 * See also {@link StringConversionConfig}, {@link EnumerationConversionConfig}, {@link CustomStringConversionConfig}).
	 *
	 * @param value
	 * 		value to be converted
	 * @return the given string value
	 */
	public static String convertStringTypeToString(Object value) {
		if (!(value instanceof String strVal)) {
			throw new IllegalArgumentException(
					"The type of the value (" + value.getClass().getName() + ") is not a String"
			);
		}
		return strVal;
	}

}
