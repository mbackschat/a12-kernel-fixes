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

package com.mgmtp.a12.utils.conversion;

import java.util.Optional;

import com.mgmtp.a12.model.utils.Experimental;
import com.mgmtp.a12.utils.conversion.config.ValueConversionConfig;

@Experimental
public interface ValueConversion {

	/**
	 * Parses the given string value to the corresponding typed value.
	 *
	 * @param value
	 * 		string value to be parsed
	 * @param inputFormat
	 * 		configuration which describes the given value
	 * @return the corresponding typed value or a {@link ParseError} occurred while parsing the given value
	 */
	ParseResult parseValue(String value, ValueConversionConfig inputFormat);

	/**
	 * Formats the given typed value.
	 *
	 * @param value
	 * 		typed value to be formatted
	 * @param outputFormat
	 * 		configuration based on which the given value should be formatted
	 * @return the corresponding formatted value
	 */
	Optional<String> formatValue(Object value, ValueConversionConfig outputFormat);

}
