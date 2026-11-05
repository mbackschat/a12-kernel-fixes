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

import static com.mgmtp.a12.utils.conversion.config.DateRangeConversionConfig.SingleDate;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.utils.conversion.ParseError;
import com.mgmtp.a12.utils.conversion.ParseResult;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateAndFormat;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateUtil;
import com.mgmtp.a12.utils.conversion.config.DateConversionConfig;
import com.mgmtp.a12.utils.conversion.config.DateRangeConversionConfig;
import com.mgmtp.a12.utils.conversion.impl.DefaultParseResult;
import com.mgmtp.a12.utils.conversion.internal.util.LocalizableUtil;
import com.mgmtp.a12.utils.localization.DataFormatPlaceholder;
import com.mgmtp.a12.utils.localization.DataFormats;
import com.mgmtp.a12.utils.localization.Placeholder;
import com.mgmtp.a12.utils.localization.internal.DataFormatsUtil;
import com.mgmtp.a12.utils.localization.internal.DateRangeValue;
import com.mgmtp.a12.utils.localization.internal.DefaultDataFormatPlaceholder;
import com.mgmtp.a12.utils.localization.internal.DefaultDateRangeFormat;

/**
 * Conversion methods for date range data types.
 */
public class DateRangeConversion {

	/**
	 * Converts the given value from one format to other format among the same data type.<br>
	 * Only <code>baseYear</code> from <code>inputFormat</code> is considered,
	 * this means <code>baseYear</code> from <code>outputFormat</code> is ignored.<br>
	 * Conversion between different time zones is not supported.<br>
	 * Only <code>interpretationOfYear</code> from <code>inputFormat</code> is considered,
	 * this means <code>interpretationOfYear</code> from <code>outputFormat</code> is ignored.
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
			DateRangeConversionConfig srcDataType,
			DataFormats srcDataFormats,
			DateRangeConversionConfig destDataType,
			DataFormats destDataFormats
	) {
		if (value.isEmpty()) {
			return value;
		}
		if (!srcDataType.timeZone().equals(destDataType.timeZone())) {
			throw new IllegalArgumentException("conversion between different time zones is not supported");
		}
		String inputRangeSeparator = DateRangeConversion.getDateRangeSeparator(srcDataFormats);
		String[] dateRangeParts = DateUtil.splitStringBySeparator(
				value,
				inputRangeSeparator
		);

		TimeZone timeZone = srcDataType.timeZone();
		String srcFormat = DataFormatsUtil.calculateDateFormat(srcDataType.format(), srcDataFormats);
		int[] yearsToAdd = DateUtil.getSupplementaryYears(
				srcFormat,
				dateRangeParts,
				srcDataType.interpretationOfYear(),
				DateConversion.getBaseYear(srcDataType),
				DateRangeConversion.isDateSeparatorAtTheEndOptional(srcFormat),
				timeZone
		);

		String destFormat = DataFormatsUtil.calculateDateFormat(destDataType.format(), destDataFormats);
		String startDestDate = DateConversion.convertDateInternal(
				new DateAndFormat(dateRangeParts[0], srcFormat, timeZone),
				destFormat,
				yearsToAdd[0]
		);
		String endDestDate = DateConversion.convertDateInternal(
				new DateAndFormat(dateRangeParts[1], srcFormat, timeZone),
				destFormat,
				yearsToAdd[1]
		);

		String outputRangeSeparator =
				DateRangeConversion.getDateRangeSeparator(destDataFormats);
		return startDestDate + outputRangeSeparator + endDestDate;
	}

	/**
	 * See also the description of defaultValueConversion#parseValue.
	 */
	public static ParseResult convertStringToDateRange(
			String value,
			DateRangeConversionConfig srcDataType,
			DataFormats srcDataFormats,
			boolean syntaxCheck
	) {
		if (StringUtils.isEmpty(value)) {
			return DefaultParseResult.ofValue(null);
		}
		int baseYear = DateConversion.getBaseYear(srcDataType);
		TimeZone timeZone = srcDataType.timeZone();
		String rangeSeparator = DateRangeConversion.getDateRangeSeparator(srcDataFormats);
		String format = DataFormatsUtil.calculateDateFormat(srcDataType.format(), srcDataFormats);
		Date[] dateRange;
		try {
			dateRange = DateUtil.createDateRange(
					value,
					rangeSeparator,
					format,
					srcDataType.interpretationOfYear(),
					baseYear,
					syntaxCheck,
					timeZone
			);
		} catch (Exception e) {
			return DefaultParseResult.ofError(DateRangeConversion.createParseErrorForDateRange(
					"datumBereichFormatFalsch",
					format,
					rangeSeparator));
		}
		return DefaultParseResult.ofValue(dateRange);
	}

	/**
	 * See also the description of defaultValueConversion#parseValue.
	 */
	public static ParseResult parseDateRange(
			String value,
			DateRangeConversionConfig srcDataType,
			DataFormats srcDataFormats
	) {
		if (StringUtils.isEmpty(value)) {
			return DefaultParseResult.ofValue(null);
		}
		String rangeSeparator = DateRangeConversion.getDateRangeSeparator(srcDataFormats);
		SingleDate singleDate = srcDataType.singleDate();
		boolean singleDateAllowed = singleDate == SingleDate.allowed || singleDate == SingleDate.only;
		if (StringUtils.isEmpty(rangeSeparator) && singleDateAllowed
		) {
			throw new IllegalArgumentException(
					"Invalid date range conversion configuration: if a single date is allowed, the range separator must not be empty."
			);
		}
		String format = DataFormatsUtil.calculateDateFormat(srcDataType.format(), srcDataFormats);
		if (!value.contains(rangeSeparator)) {
			if (singleDateAllowed) {
				DateConversionConfig dateConfig = DateConversion.createDateConversionConfig(
						srcDataType.format(),
						srcDataType.baseYear(),
						srcDataType.timeZone()
				);
				return DateConversion.parseDate(value, dateConfig, srcDataFormats);
			} else {
				return DefaultParseResult.ofError(DateRangeConversion.createParseErrorForDateRange(
						"datumBereichTrennerFehlt",
						format,
						rangeSeparator));
			}
		}
		if (singleDate == SingleDate.only) {
			return DefaultParseResult.ofError(DateConversion.createParseErrorForDate(format));
		}
		// split the range using the range separator
		String[] dateRange = DateUtil.splitStringBySeparator(
				value,
				rangeSeparator
		);
		if (dateRange.length != 2 ||
				dateRange[0].isEmpty() ||
				dateRange[1].isEmpty()
		) {
			return DefaultParseResult.ofError(DateRangeConversion.createParseErrorForDateRange(
					"datumBereichFormatFalsch",
					format,
					rangeSeparator));
		}
		return DateRangeConversion.convertStringToDateRange(
				value,
				srcDataType,
				srcDataFormats,
				true
		);
	}

	/**
	 * See also the description of defaultValueConversion#formatValue.
	 * If "singleDate" is set to "allowed" in the <code>destDataType</code>, also single Dates can be formatted.
	 * If "singleDate" is set to "only" in the <code>destDataType</code>, only single Dates can be formatted.
	 */
	public static Optional<String> convertDateRangeToString(
			Object value,
			DateRangeConversionConfig destDataType,
			DataFormats destDataFormats
	) {
		SingleDate singleDate = destDataType.singleDate();
		boolean singleDateAllowed = singleDate == SingleDate.allowed || singleDate == SingleDate.only;
		if (value instanceof Date) {
			if (singleDateAllowed) {
				DateConversionConfig dateConfig = DateConversion.createDateConversionConfig(
						destDataType.format(),
						destDataType.baseYear(),
						destDataType.timeZone()
				);
				return DateConversion.convertDateToString(value, dateConfig, destDataFormats);
			}
		}
		if (singleDate == SingleDate.only) {
			throw new IllegalArgumentException(
					"The type of the value (" + value.getClass().getSimpleName() + ") is not a Date");
		}
		// null is already handled
		TimeZone timeZone = destDataType.timeZone();
		String rangeSeparator = DateRangeConversion.getDateRangeSeparator(destDataFormats);
		String destFormat = DataFormatsUtil.calculateDateFormat(destDataType.format(), destDataFormats, true);
		return Optional.ofNullable(DateUtil.convertDateRangeToString(
				value,
				rangeSeparator,
				destFormat,
				timeZone
		));
	}

	private static String getDateRangeSeparator(DataFormats dataFormats) {
		return dataFormats.dateRangeSeparator();
	}

	/**
	 * @return <code>true</code> if a date separator at the end of the value is allowed (but optional). This is the case, if
	 * 		<ul>
	 * 		<li>the point is defined as date separtor in the format</li>
	 * 		<li>the format contains only day and month, no year</li>
	 * 		<li>in the format, the day is defined before the month</li>
	 * 		</ul>
	 * 		In other words, if the format is "dd.MM", otherwise <code>false</code>.
	 */
	private static boolean isDateSeparatorAtTheEndOptional(String format) {
		return (format.contains(".") &&
				DateUtil.formatContainsOnlyDayMonth(format) &&
				DateUtil.isDayDefinedBeforeMonth(format)
		);
	}

	private static ParseError createParseErrorForDateRange(
			String errorId,
			String format,
			String separator
	) {
		DateRangeValue value = new DefaultDateRangeFormat(format, separator);
		DataFormatPlaceholder timeIntervalFormatPlaceholder = new DefaultDataFormatPlaceholder(value,
				DataFormatPlaceholder.DataFormat.dateRange);
		Map<String, Placeholder> args = Map.of("timeIntervalFormat", timeIntervalFormatPlaceholder);
		Map<Locale, String> errorTextDefaults = Map.of(
				Locale.GERMAN, "Es sind nur Datumsbereiche vom Format $timeIntervalFormat$ erlaubt.",
				Locale.ENGLISH, "Only date ranges of the format $timeIntervalFormat$ are allowed.",
				Locale.FRENCH, "Seules les plages de dates au format $timeIntervalFormat$ sont autorisées.",
				Locale.of("nl"), "Alleen datumbereiken van het formaat $timeIntervalFormat$ zijn toegestaan."
		);
		return LocalizableUtil.createParseError(
				"DATUM_BEREICH_FORMAT",
				errorId,
				args,
				errorTextDefaults
		);
	}

}
