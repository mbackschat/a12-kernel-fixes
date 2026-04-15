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

package com.mgmtp.a12.utils.localization;

import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.model.utils.Experimental;

/**
 * A configuration object to hold data formats for a specific locale.
 * All properties are optional.
 * <p>
 * The various properties control how user input (usually coming from a UI) is parsed and
 * how data is formatted for presentation (also usually for a UI).
 * <p>
 * This configuration is especially useful when used in combination with
 * A12 Kernel validations, computations, parsing and formatting.
 */
@Experimental
public interface DataFormats {

	// numbers
	char decimalSeparator();

	Optional<String> thousandsSeparator();

	boolean decimalPlacesOptional();

	// boolean
	String trueValue();

	String falseValue();

	// date, date time, time
	String dateSeparator();

	boolean dateZeroOptional();

	enum DATE_FRAGMENT_ORDERING_TYPE {
		YEAR_MONTH_DAY,
		YEAR_DAY_MONTH,
		DAY_YEAR_MONTH,
		DAY_MONTH_YEAR,
		MONTH_YEAR_DAY,
		MONTH_DAY_YEAR
	}

	DATE_FRAGMENT_ORDERING_TYPE dateFragmentOrdering();

	String dateRangeSeparator();

	/**
	 * expects format string of SimpleDateFormat API
	 */
	String dateTimeFormat();

	/**
	 * expects format string of SimpleDateFormat API
	 */
	String timeFormat();

	FormatCharacters formatCharacters();

	record FormatCharacters(String year, String month, String day) {
	}

	// conversion options
	List<String> convertCustomFieldTypesList();

	boolean convertCustomFieldTypesAll();

}
