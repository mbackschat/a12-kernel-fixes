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

import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.utils.localization.DataFormats;

/**
 * It takes the default values from {@link InternalDefaultDataFormats}.
 */
public class DataFormatsBuilder {

	private static final DataFormats DEFAULT_VALUES = new InternalDefaultDataFormats();
	private char decimalSeparator = DEFAULT_VALUES.decimalSeparator();
	private String thousandsSeparator = DEFAULT_VALUES.thousandsSeparator().orElse(null);
	private boolean decimalPlacesOptional = DEFAULT_VALUES.decimalPlacesOptional();
	private String trueValue = DEFAULT_VALUES.trueValue();
	private String falseValue = DEFAULT_VALUES.falseValue();
	private String dateSeparator = DEFAULT_VALUES.dateSeparator();
	private boolean dateZeroOptional = DEFAULT_VALUES.dateZeroOptional();
	private DataFormats.DATE_FRAGMENT_ORDERING_TYPE dateFragmentOrdering = DEFAULT_VALUES.dateFragmentOrdering();
	private String dateRangeSeparator = DEFAULT_VALUES.dateRangeSeparator();
	private String dateTimeFormat = DEFAULT_VALUES.dateTimeFormat();
	private String timeFormat = DEFAULT_VALUES.timeFormat();
	private DataFormats.FormatCharacters formatCharacters = DEFAULT_VALUES.formatCharacters();
	private List<String> convertCustomFieldTypesList = DEFAULT_VALUES.convertCustomFieldTypesList();
	private boolean convertCustomFieldTypesAlL = DEFAULT_VALUES.convertCustomFieldTypesAll();

	public DataFormatsBuilder() {
		// since there are a lot of fields, we do the initialization above
	}

	/**
	 * Seeds the builder from an existing {@link DataFormats}, so callers can copy it and override only the
	 * few fields they care about.
	 */
	public DataFormatsBuilder(DataFormats seed) {
		this.decimalSeparator = seed.decimalSeparator();
		this.thousandsSeparator = seed.thousandsSeparator().orElse(null);
		this.decimalPlacesOptional = seed.decimalPlacesOptional();
		this.trueValue = seed.trueValue();
		this.falseValue = seed.falseValue();
		this.dateSeparator = seed.dateSeparator();
		this.dateZeroOptional = seed.dateZeroOptional();
		this.dateFragmentOrdering = seed.dateFragmentOrdering();
		this.dateRangeSeparator = seed.dateRangeSeparator();
		this.dateTimeFormat = seed.dateTimeFormat();
		this.timeFormat = seed.timeFormat();
		this.formatCharacters = seed.formatCharacters();
		this.convertCustomFieldTypesList = seed.convertCustomFieldTypesList();
		this.convertCustomFieldTypesAlL = seed.convertCustomFieldTypesAll();
	}

	public DataFormatsBuilder decimalSeparator(char decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
		return this;
	}

	public DataFormatsBuilder thousandsSeparator(String thousandsSeparator) {
		this.thousandsSeparator = thousandsSeparator;
		return this;
	}

	public DataFormatsBuilder decimalPlacesOptional(boolean decimalPlacesOptional) {
		this.decimalPlacesOptional = decimalPlacesOptional;
		return this;
	}

	public DataFormatsBuilder trueValue(String trueValue) {
		this.trueValue = trueValue;
		return this;
	}

	public DataFormatsBuilder falseValue(String falseValue) {
		this.falseValue = falseValue;
		return this;
	}

	public DataFormatsBuilder dateSeparator(String dateSeparator) {
		this.dateSeparator = dateSeparator;
		return this;
	}

	public DataFormatsBuilder dateZeroOptional(boolean dateZeroOptional) {
		this.dateZeroOptional = dateZeroOptional;
		return this;
	}

	public DataFormatsBuilder dateFragmentOrdering(DataFormats.DATE_FRAGMENT_ORDERING_TYPE dateFragmentOrdering) {
		this.dateFragmentOrdering = dateFragmentOrdering;
		return this;
	}

	public DataFormatsBuilder dateRangeSeparator(String dateRangeSeparator) {
		this.dateRangeSeparator = dateRangeSeparator;
		return this;
	}

	public DataFormatsBuilder dateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
		return this;
	}

	public DataFormatsBuilder timeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
		return this;
	}

	public DataFormatsBuilder formatCharacters(DataFormats.FormatCharacters formatCharacters) {
		this.formatCharacters = formatCharacters;
		return this;
	}

	public DataFormatsBuilder convertCustomFieldTypesList(List<String> convertCustomFieldTypesList) {
		this.convertCustomFieldTypesList = convertCustomFieldTypesList;
		return this;
	}

	public DataFormatsBuilder convertCustomFieldTypesAlL(boolean convertCustomFieldTypesAll) {
		this.convertCustomFieldTypesAlL = convertCustomFieldTypesAll;
		return this;
	}

	public DataFormats build() {
		return new DefaultDataFormats(decimalSeparator, Optional.ofNullable(thousandsSeparator), decimalPlacesOptional,
				trueValue, falseValue,
				dateSeparator, dateZeroOptional, dateFragmentOrdering, dateRangeSeparator, dateTimeFormat, timeFormat,
				formatCharacters,
				convertCustomFieldTypesList, convertCustomFieldTypesAlL);
	}

}
