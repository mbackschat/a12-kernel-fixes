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
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.utils.conversion.a12internal.util.DateUtil;
import com.mgmtp.a12.utils.localization.DataFormats;

public class DataFormatsUtil {

	public static String calculateDateFormat(
			String fieldDateFormat,
			DataFormats dataFormats
	) {
		return calculateDateFormat(fieldDateFormat, dataFormats, false);
	}

	public static String calculateDateFormat(
			String fieldDateFormat,
			DataFormats dataFormats,
			boolean ignoreDateZeroOptional
	) {
		// if the field format string contains time:
		// return the respective dateTime or time format of the given dataFormats object
		if (DateUtil.containsTimePart(fieldDateFormat)) {
			if (DateUtil.containsDatePart(fieldDateFormat)) {
				// DateTime
				// for empty information in 'dataFormats', use given format
				return Optional.ofNullable(dataFormats.dateTimeFormat())
						.filter(f -> !f.isEmpty())
						.orElse(fieldDateFormat);
			} else {
				// Time
				// for empty information in 'dataFormats', use given format
				return Optional.ofNullable(dataFormats.timeFormat())
						.filter(f -> !f.isEmpty())
						.orElse(fieldDateFormat);
			}
		}

		boolean considerZeroOptional = dataFormats.dateZeroOptional() && !ignoreDateZeroOptional;
		String separator = Optional.ofNullable(dataFormats.dateSeparator())
				.filter(f -> !f.isEmpty())
				.orElse(getDateSeparator(fieldDateFormat));
		String separatorAtEnd = DateUtil.hasSeparatorAtEnd(fieldDateFormat)
				? separator
				: "";
		if (dataFormats.dateFragmentOrdering() == null) {
			if (considerZeroOptional) {
				return (DateUtil.makeFormatStringDayMonthOptional(fieldDateFormat) + separatorAtEnd);
			} else {
				return fieldDateFormat;
			}
		}

		boolean hasDay = fieldDateFormat.contains("d");
		boolean hasMonth = fieldDateFormat.contains("M");
		boolean hasYear = fieldDateFormat.contains("y");

		String newFormat = trimSeparators(dataFormats.dateFragmentOrdering().name()
				.replace("YEAR", hasYear ? "y".repeat(4) : "")
				.replace("MONTH", hasMonth ? "M".repeat(2) : "")
				.replace("DAY", hasDay ? "d".repeat(2) : "")
		).replace("_", separator);

		return (considerZeroOptional
				? DateUtil.makeFormatStringDayMonthOptional(newFormat)
				: newFormat) + separatorAtEnd;
	}

	private static String getDateSeparator(String formatString) {
		// find the first character in the given string that is not a d,M or y
		return formatString.replace("y", "").replace("M", "").replace("d", "");
	}

	private static String trimSeparators(String formatString) {
		return formatString.replaceAll("__+", "_").replaceAll("_+$", "").replaceAll("^_+", "");
	}

	/**
	 * The merge process works as follows: if the property in {@code second} is not empty, this will be taken, otherwise the
	 * property in {@code first} will be taken.
	 *
	 * @param first
	 * 		base of the result
	 * @param second
	 * 		overriding values (if not empty).
	 * @return the result after the merge process.
	 */
	public static DataFormats mergeDataFormats(final DataFormats first, final DataFormats second) {
		char decimalSeparator = second.decimalSeparator();
		Optional<String> thousandsSeparator = second.thousandsSeparator().or(first::thousandsSeparator);
		String trueValue = !StringUtils.isEmpty(second.trueValue()) ?
				second.trueValue() : first.trueValue();
		String falseValue = !StringUtils.isEmpty(second.falseValue()) ?
				second.falseValue() : first.falseValue();
		String dateSeparator = second.dateSeparator() != null ?
				second.dateSeparator() : first.dateSeparator();
		String dateRangeSeparator = second.dateRangeSeparator();
		String dateTimeFormat = !StringUtils.isEmpty(second.dateTimeFormat()) ?
				second.dateTimeFormat() : first.dateTimeFormat();
		String timeFormat = !StringUtils.isEmpty(second.timeFormat()) ?
				second.timeFormat() : first.timeFormat();
		List<String> convertCustomFieldTypesList = Stream.concat(first.convertCustomFieldTypesList().stream(),
				second.convertCustomFieldTypesList().stream()).distinct().toList();
		return new DefaultDataFormats(
				decimalSeparator,
				thousandsSeparator,
				second.decimalPlacesOptional(),
				trueValue,
				falseValue,
				dateSeparator,
				second.dateZeroOptional(),
				second.dateFragmentOrdering(),
				dateRangeSeparator,
				dateTimeFormat,
				timeFormat,
				second.formatCharacters(),
				convertCustomFieldTypesList,
				false
		);
	}

}
