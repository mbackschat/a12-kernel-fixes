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

package com.mgmtp.a12.utils.localization.internal;

import static com.mgmtp.a12.utils.localization.DataFormats.DATE_FRAGMENT_ORDERING_TYPE.YEAR_MONTH_DAY;

import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.utils.localization.DataFormats;

public class InternalDefaultDataFormats implements DataFormats {

	@Override public char decimalSeparator() {
		return '.';
	}

	@Override public Optional<String> thousandsSeparator() {
		return Optional.empty();
	}

	@Override public boolean decimalPlacesOptional() {
		return false;
	}

	@Override public String trueValue() {
		return "true";
	}

	@Override public String falseValue() {
		return "false";
	}

	@Override public String dateSeparator() {
		return "-";
	}

	@Override public boolean dateZeroOptional() {
		return false;
	}

	@Override public DATE_FRAGMENT_ORDERING_TYPE dateFragmentOrdering() {
		return YEAR_MONTH_DAY;
	}

	@Override public String dateRangeSeparator() {
		return "/";
	}

	@Override public String dateTimeFormat() {
		return "yyyy-MM-dd'T'HH:mm:ss";
	}

	@Override public String timeFormat() {
		return "HH:mm:ss";
	}

	@Override public FormatCharacters formatCharacters() {
		return new FormatCharacters("y", "M", "d");
	}

	@Override public List<String> convertCustomFieldTypesList() {
		return List.of();
	}

	@Override public boolean convertCustomFieldTypesAll() {
		return false;
	}

}
