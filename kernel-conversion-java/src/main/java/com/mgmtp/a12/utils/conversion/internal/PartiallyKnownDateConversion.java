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

import static com.mgmtp.a12.utils.conversion.internal.DateConversion.getParseError;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.utils.conversion.DatePrecision;
import com.mgmtp.a12.utils.conversion.ParseError;
import com.mgmtp.a12.utils.conversion.ParseResult;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateAndFormat;
import com.mgmtp.a12.utils.conversion.a12internal.util.DatePart;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateUtil;
import com.mgmtp.a12.utils.conversion.a12internal.util.PartiallyKnownDatesUtil;
import com.mgmtp.a12.utils.conversion.a12internal.util.ValidationDateParser;
import com.mgmtp.a12.utils.conversion.config.DateBasicConversionConfig;
import com.mgmtp.a12.utils.conversion.config.DateConversionConfig;
import com.mgmtp.a12.utils.conversion.config.PartiallyKnownDateConversionConfig;
import com.mgmtp.a12.utils.conversion.impl.DefaultParseResult;
import com.mgmtp.a12.utils.conversion.internal.config.PartiallyKnownDateConversionConfigBuilder;
import com.mgmtp.a12.utils.localization.DataFormats;
import com.mgmtp.a12.utils.localization.internal.DataFormatsUtil;

/**
 * Conversion methods for date related data types (date, date fragment, date time and time).
 */
public class PartiallyKnownDateConversion {

	/**
	 * Converts the given value from one format to other format among the same data type.<br>
	 * Only <code>baseYear</code> from <code>inputFormat</code> is considered,
	 * this means <code>baseYear</code> from <code>outputFormat</code> is ignored.<br>
	 * Conversion between different time zones is not supported.<br>
	 * Only <code>datePrecision</code> from <code>inputFormat</code> is considered,
	 * this means <code>datePrecision</code> from <code>outputFormat</code> is ignored.
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
			DateBasicConversionConfig srcDataType,
			DataFormats srcDataFormats,
			DateBasicConversionConfig destDataType,
			DataFormats destDataFormats
	) {
		if (value.isEmpty()) {
			return value;
		}
		if (!srcDataType.timeZone().equals(destDataType.timeZone())) {
			throw new IllegalArgumentException("conversion between different time zones is not supported");
		}
		int baseYear = PartiallyKnownDateConversion.getBaseYear(srcDataType);
		TimeZone timeZone = srcDataType.timeZone();
		String srcFormat = DataFormatsUtil.calculateDateFormat(srcDataType.format(), srcDataFormats);
		DateAndFormat srcDateAndFormat = new DateAndFormat(value, srcFormat, timeZone);

		boolean isPartiallyKnownDateAllowed = PartiallyKnownDateConversion.isPartiallyKnownDateAllowed(srcDataType);
		List<DatePart> changedUnknownDateParts = new ArrayList<>();
		if (isPartiallyKnownDateAllowed) {
			changedUnknownDateParts = PartiallyKnownDatesUtil.replaceUnknownDateParts(srcDateAndFormat);
		}
		String destFormat = DataFormatsUtil.calculateDateFormat(destDataType.format(), destDataFormats);
		String destValue = PartiallyKnownDateConversion.convertDateInternal(
				srcDateAndFormat,
				destFormat,
				baseYear
		);
		DateAndFormat destDateAndFormat = new DateAndFormat(
				destValue,
				destFormat,
				timeZone
		);

		PartiallyKnownDatesUtil.setDatePartsToUnknown(
				destDateAndFormat,
				changedUnknownDateParts
		);

		return destDateAndFormat.getDate();
	}

	/**
	 * See also the description of defaultValueConversion#parseValue.
	 */
	public static Object convertStringToDate(
			String value,
			DateBasicConversionConfig srcDataType,
			DataFormats srcDataFormats,
			boolean isPartUnknown
	) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		DateAndFormat dateWithFormat = PartiallyKnownDateConversion.getDateWithFormat(value, srcDataType, srcDataFormats);
		// if isPartUnknown is given, we already checked isPartUnknown in parseDate and don't need to check again
		boolean partUnknown = isPartUnknown ? isPartUnknown : PartiallyKnownDatesUtil.isPartUnknown(dateWithFormat);
		if (partUnknown) {
			return value;
		}
		int baseYear = PartiallyKnownDateConversion.getBaseYear(srcDataType);
		return DateUtil.getValueAsDate(dateWithFormat, baseYear);
	}

	/**
	 * See also the description of defaultValueConversion#parseValue.
	 */
	public static ParseResult parseDate(
			String value,
			DateBasicConversionConfig srcDataType,
			DataFormats srcDataFormats
	) {
		if (StringUtils.isEmpty(value)) {
			return DefaultParseResult.ofValue(null);
		}
		DateAndFormat dateWithFormat = PartiallyKnownDateConversion.getDateWithFormat(value, srcDataType, srcDataFormats
		);
		boolean isPartUnknown = PartiallyKnownDatesUtil.isPartUnknown(dateWithFormat);
		if (isPartUnknown) {
			return DefaultParseResult.ofValue(value);
		}

		int baseYear = getBaseYear(srcDataType);
		dateWithFormat.completeWithYear(baseYear);
		Optional<ParseError> error = PartiallyKnownDateConversion.sanityCheck(dateWithFormat);
		return error.<ParseResult>map(DefaultParseResult::ofError).orElseGet(() -> DefaultParseResult.ofValue(
				PartiallyKnownDateConversion.convertStringToDate(value, srcDataType, srcDataFormats, isPartUnknown)));
	}

	/**
	 * See also the description of defaultValueConversion#formatValue.
	 */
	public static Optional<String> convertDateToString(
			Object value,
			DateBasicConversionConfig destDataType,
			DataFormats destDataFormats
	) {
		return DateConversion.convertDateToString(value, destDataType, destDataFormats);
	}

	/**
	 * Creates the data type {@link DateConversionConfig} for dates and partially known dates.
	 *
	 * @param format
	 * 		the date format (e.g. "yyyy-MM-dd")
	 * @param baseYear
	 * 		the base year to supplement partialy known dates. This year is relevant for <code>dateFormat</code>s
	 * 		containing <code>MM</code> and <code>dd</code> but not <code>yyyy</code>, in order to determine
	 * 		if the February 29th is a valid input or not.
	 * 		Default value is `2000`.
	 * @param timeZone
	 * 		the time zone the date should be interpreted in. Default value is `"UTC"`.
	 * @param datePrecision
	 * 		the new date precision. Default value is {@link DatePrecision#FULL}
	 * @return the converted string value
	 */
	public static PartiallyKnownDateConversionConfig createDateConversionConfig(
			String format,
			Integer baseYear,
			TimeZone timeZone,
			DatePrecision datePrecision
	) {
		PartiallyKnownDateConversionConfigBuilder builder = new PartiallyKnownDateConversionConfigBuilder();
		builder.withFormat(format);
		if (baseYear != null) {
			builder.withBaseYear(baseYear);
		}
		if (timeZone != null) {
			builder.withTimeZone(timeZone);
		}
		if (datePrecision != null) {
			builder.withDatePrecision(datePrecision);
		}
		return builder.build();
	}

	private static Optional<ParseError> sanityCheck(DateAndFormat dateWithFormat) {
		try {
			// more exact check while parsing
			ValidationDateParser.parseDate(dateWithFormat);
		} catch (ParseException e) {
			return Optional.of(PartiallyKnownDateConversion.createParseErrorForDate(dateWithFormat.getFormat()));
		}
		return Optional.empty();
	}

	/**
	 * Converts a DateAndFormat to the given target format.
	 * As a side effect the given parameter dateWithFormat is changed.
	 */
	public static String convertDateInternal(
			DateAndFormat dateWithFormat,
			String targetFormat,
			int supplementaryYear
	) {
		// we need the original format for the error message in an error situation
		String srcFormat = dateWithFormat.getFormat();

		// unify the given date if it does not contain the time part
		if (!targetFormat.contains("HH") && !srcFormat.contains("HH")
		) {
			dateWithFormat.unifyLastSeparator();
		}

		// if the year is not contained in the format, we add it to handle the Feb 29th
		if (DateUtil.formatContainsOnlyDayMonth(srcFormat)) {
			dateWithFormat.completeWithYear(supplementaryYear);
		}

		// parse the date and format it to the target format
		Date targetDate =
				ValidationDateParser.parseDateWithoutSyntaxCheck(dateWithFormat);
		return ValidationDateParser.makeStringDayMonthTwoDigits(
				targetDate,
				targetFormat,
				dateWithFormat.getTimeZone()
		);
	}

	/**
	 * @return the defined base year if present, otherwise the default value <code>2000</code>.
	 */
	public static int getBaseYear(DateBasicConversionConfig dataType) {
		return dataType.baseYear();
	}

	public static boolean isPartiallyKnownDateAllowed(DateBasicConversionConfig dataType) {
		if (dataType instanceof PartiallyKnownDateConversionConfig dateType) {
			return dateType.datePrecision()
					.map(dp -> dp != DatePrecision.FULL)
					.orElse(false);
		} else {
			return false;
		}
	}

	private static DateAndFormat getDateWithFormat(
			String value,
			DateBasicConversionConfig dataType,
			DataFormats dataFormats
	) {
		TimeZone timeZone = dataType.timeZone();
		String format = DataFormatsUtil.calculateDateFormat(dataType.format(), dataFormats);
		return new DateAndFormat(value, format, timeZone);
	}

	public static ParseError createParseErrorForDate(String format) {
		return getParseError(format);
	}

}
