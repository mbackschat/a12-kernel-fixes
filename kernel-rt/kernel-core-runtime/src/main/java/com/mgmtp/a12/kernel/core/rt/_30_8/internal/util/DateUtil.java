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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.FormatDefinitionDatumBereich;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.FormatDefinitionDatumBereich.Jahresinterpretation;

import lombok.NonNull;

/**
 * ATTENTION.
 * For performance optimization please be aware of:
 * SimpleDateFormat: Date formats are not synchronized. It is recommended to create separate format instances for each thread.
 * If multiple threads access a format concurrently, it must be synchronized externally.
 */
public class DateUtil {

	public static final String TIME_ZONE_BERLIN = "Europe/Berlin";
	public static final String YEAR_FORMAT = "yyyy";
	public static final String YEAR_FORMAT_LETTER = "y";

	private DateUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Specifies if the start or the end of a date interval should be used.
	 * For partially known dates, this enumeration is used to specify the earliest or latest possible date.
	 */
	public enum DateInterval {
		INTERVAL_START,
		INTERVAL_END;

		public int getIndex() {
			return (this == INTERVAL_START) ? 0 : 1;
		}

		public static DateInterval create(boolean earliest) {
			return earliest ? INTERVAL_START : INTERVAL_END;
		}

		public String getGivenMonth() {
			return (this == INTERVAL_START) ? "01" : "12";
		}

	}

	/**
	 * Creates a date from the specified value and format. The year information is added if no year information is available.
	 * For the supplementation, the base year is used if specified (in a document model, a base year can be specified), otherwise
	 * 2000.
	 *
	 * @return generated date
	 */
	public static Date getValueAsDate(DateAndFormat dateWithFormat, int baseYear) {
		dateWithFormat.completeWithYear(baseYear);

		return ValidationDateParser.parseDateWithoutSyntaxCheck(dateWithFormat);
	}

	/**
	 * A date is constructed from the passed values if all parameters are valid.
	 */
	public static Date createDate(int day, int month, int year, TimeZone timeZone) {
		try {
			GregorianCalendar calendar = new GregorianCalendar(timeZone);
			calendar.setLenient(false);
			calendar.set(year, month - 1, day); //month is 0-based in the Gregorian calendar
			DateUtil.clearTime(calendar);
			return calendar.getTime();
		} catch (RuntimeException e) {
			// For wrong values, we return null
			return null;
		}
	}

	static String getLastDayOfMonth(DateAndFormat dateWithFormat) {
		Date date = ValidationDateParser.parseDateWithoutSyntaxCheck(dateWithFormat);

		Calendar cal = new GregorianCalendar(dateWithFormat.getTimeZone());
		cal.setLenient(false);
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * A date is constructed from the passed value. If the specified format does not contain a year, it will be added.
	 * For the supplementation, the base year is used if specified, otherwise 2000 as default.
	 * If the specified format does not contain a month specification, either 01 or 12 is specified for the month,
	 * depending on whether the start or end is to be constructed.
	 * If the specified format does not contain a day, either 01 or the last day of the month is specified,
	 * depending on whether the start or end is to be constructed.
	 * A date is created from the possibly supplemented value using the possibly supplemented format.
	 *
	 * @return generated date, never undefined (internal error should not occur)
	 */
	public static Date createDateFromDateFragment(DateAndFormat dateWithFormat, DateInterval intervalSpec, int baseYear) {
		dateWithFormat.completeWithYear(baseYear);
		dateWithFormat.addMonthAndDay(intervalSpec);

		// the date string should be complete now, we can try to parse
		return ValidationDateParser.parseDateWithoutSyntaxCheck(dateWithFormat);
	}

	/**
	 * @return true, if the given <code>format</code> contains only day ("d) and month ("M"), not year ("y"). Otherwise false.
	 */
	public static boolean formatContainsOnlyDayMonth(String format) {
		return format.contains("d") && format.contains("M") && !format.contains(YEAR_FORMAT_LETTER);
	}

	/**
	 * Splits the string using the separator.
	 * If the separator is the empty string, the string is split in the middle.
	 */
	public static String[] splitStringBySeparator(String dateRange, String rangeSeparator) {
		if ("".equals(rangeSeparator)) {
			// no separator, split in the middle
			int dateRangeLength = dateRange.length();
			return new String[] { dateRange.substring(0, dateRangeLength / 2),
					dateRange.substring(dateRangeLength / 2, dateRangeLength) };
		}
		return StringUtils.splitPreserveAllTokens(dateRange, rangeSeparator);
	}

	/**
	 * @return the date separator which is specified in the passed format, otherwise an empty string if none is specified.
	 */
	public static String getDateSeparator(String dateFormat) {
		for (int i = 0; i < dateFormat.length(); i++) {
			char c = dateFormat.charAt(i);
			if (!isDatePart(c)) {
				return String.valueOf(c);
			}
		}
		return "";
	}

	private static boolean isDatePart(char c) {
		return (c == 'y' || c == 'M' || c == 'd');
	}

	/**
	 * @return a date format string, where all day or month placeholders of the given <code>format</code> are set to two digits
	 * 		("dd" and "MM").
	 */
	public static String makeFormatStringDayMonthTwoDigits(String format) {
		if (!format.contains("dd")) {
			format = format.replace("d", "dd");
		}
		if (!format.contains("MM")) {
			format = format.replace("M", "MM");
		}
		return format;
	}

	/**
	 * Changes the format string of a date so that day and month are always optional.<br>
	 * Example: for format "yyyy-MM-dd", "yyyy-M-d" is returned.
	 */
	public static String makeFormatStringDayMonthOptional(String format) {
		return format.replace("dd", "d").replace("MM", "M");
	}

	/**
	 * @return the day format "d" if zeros are optional. Otherwise "dd".
	 */
	static String getDayFormat(boolean zerosOptional) {
		return zerosOptional ? "d" : "dd";
	}

	/**
	 * @return the month format "M" if zeros are optional. Otherwise "MM".
	 */
	static String getMonthFormat(boolean zerosOptional) {
		return zerosOptional ? "M" : "MM";
	}

	/**
	 * @return <code>false</code>, if the {@link Jahresinterpretation} is not set or
	 * 		if the end date lies before the start date. Otherwise true.
	 */
	public static boolean isYearNotBaseYear(String startDate, String endDate, String checkFormat,
			boolean dateRangeSeparatorOptAtEnd, Jahresinterpretation interpretationOfYear, TimeZone timeZone) {
		if (interpretationOfYear == Jahresinterpretation.Standard) {
			return false;
		}

		// To check whether dateRangeStart > dateRangeEnd, a leap year can simply be added.
		DateAndFormat dateRangeStart = new DateAndFormat(startDate, checkFormat, timeZone);
		completeDateAndFormat(dateRangeStart, checkFormat, dateRangeSeparatorOptAtEnd, 2000);
		DateAndFormat dateRangeEnd = new DateAndFormat(endDate, checkFormat, timeZone);
		completeDateAndFormat(dateRangeEnd, checkFormat, dateRangeSeparatorOptAtEnd, 2000);
		try {
			Date dateRangeStartDate = ValidationDateParser.parseDate(dateRangeStart);
			Date dateRangeEndDate = ValidationDateParser.parseDate(dateRangeEnd);
			return dateBefore(dateRangeEndDate, dateRangeStartDate);
		} catch (java.text.ParseException e) {
			// If one of the dates is not valid with a leap year,
			// the error will be reported later; the base year is ok as a year.
			return false;
		}
	}

	/**
	 * Preparation for fine validation through parsing. Date and format are supplemented as follows if necessary:
	 * <ol>
	 * 	<li>In some cases (depending on the configuration of DataFormats), a date separator is allowed at the end of the date value (e.g. 01.01.).
	 * 		In that case, the date and the format are aligned with regard to the separator at the end.</li>
	 * 	<li>If the format contains day and month, but no year, then the format is supplemented with the missing year format
	 * 		and the date string is supplemented with the corresponding year - so that February 29th. is treated correctly.</li>
	 * </ol>
	 */
	public static void completeDateAndFormat(DateAndFormat dateWithFormat, String origFormat,
			boolean dateRangeSeparatorOptionalAtEnd, int baseYear) {
		if (dateRangeSeparatorOptionalAtEnd) {
			// If a date separator is allowed at the end of the value,
			// we make sure that the value and the format match
			dateWithFormat.unifyLastSeparator();
		}
		// If the date does not contain a year, append the base year and change the format.
		// Necessary due to check for February 29th.
		// However, the 29th is only checked if the month and day are included in the format.
		if (formatContainsOnlyDayMonth(origFormat)) {
			dateWithFormat.completeWithYear(baseYear);
		}
	}

	/**
	 * @return true, if a given date <code>d1</code> is before another given date <code>d2</code>. Otherwise false.
	 */
	public static boolean dateBefore(Date d1, Date d2) {
		return d1.before(d2);
	}

	/**
	 * Returns the year for a start and end of a date range to which a date without a year is to be added.
	 * Required for the date range formats DD.MM-DD.MM (InterpretationOfYear == TO) or
	 * DD.MM-DD.MM (InterpretationOfYear == FROM), since the year is <b>NOT</b> the base year by default.
	 */
	public static int[] getSupplementaryYears(String dateFormat, String[] dateRange, Jahresinterpretation interpretationOfYear,
			int baseYear, boolean dateRangeSeparatorOptAtEnd, TimeZone timeZone) {
		int[] supplementaryYears = { baseYear, baseYear };
		if (DateUtil.isYearNotBaseYear(dateRange[0], dateRange[1], dateFormat, dateRangeSeparatorOptAtEnd, interpretationOfYear,
				timeZone)) {
			if (interpretationOfYear == Jahresinterpretation.Vorjahr) {
				supplementaryYears[0]--;
			}
			if (interpretationOfYear == Jahresinterpretation.Folgejahr) {
				supplementaryYears[1]++;
			}
		}
		return supplementaryYears;
	}

	/**
	 * @return the start and end of a date range as DateAndFormat.
	 */
	public static DateAndFormat[] getDateRange(String value, String dateRangeSeparator, String dateFormat,
			Boolean dateRangeSeparatorOptAtEnd, Integer baseYear,
			FormatDefinitionDatumBereich.Jahresinterpretation interpretationOfYear,
			TimeZone timeZone) {
		String[] values = DateUtil.splitStringBySeparator(value, dateRangeSeparator);

		int[] add = DateUtil.getSupplementaryYears(dateFormat, values, interpretationOfYear, baseYear, dateRangeSeparatorOptAtEnd,
				timeZone);

		DateAndFormat[] startAndEnd = new DateAndFormat[2];
		startAndEnd[0] = new DateAndFormat(values[0], dateFormat, timeZone);
		startAndEnd[1] = new DateAndFormat(values[1], dateFormat, timeZone);

		DateUtil.completeDateAndFormat(startAndEnd[0], dateFormat, dateRangeSeparatorOptAtEnd, add[0]);
		DateUtil.completeDateAndFormat(startAndEnd[1], dateFormat, dateRangeSeparatorOptAtEnd, add[1]);

		return startAndEnd;
	}

	/**
	 * Sets the fields {@link Calendar#HOUR_OF_DAY},{@link Calendar#MINUTE}, {@link Calendar#SECOND}
	 * and {@link Calendar#MILLISECOND} of the given calendar to 0.
	 */
	public static void clearTime(Calendar date) {
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
	}

	/**
	 * @return a {@link Date}-object for the data type "TimeType".
	 * 		The date is set to 01.01.1970 with the given values for hour, minute, second and timeZone.
	 */
	public static Date createTime(int hour, int minute, int second, TimeZone timeZone) {
		Calendar cal = new GregorianCalendar(timeZone);
		cal.setLenient(false);
		try {
			cal.set(Calendar.YEAR, 1970);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, second);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime();
		} catch (RuntimeException exc) {
			return null;
		}
	}

	/**
	 * Create a date time from the date specified in param date and the time specified in the param
	 * time (only hour, minute and second are used).
	 *
	 * @return a new {@link Date}-Object with the specified values set, or <code>null</code> if any
	 * 		of the given date objects is <code>null</code> or values are illegal.
	 */
	public static Date createDateTime(Date date, Date time, TimeZone timeZone) {
		if (date == null || time == null) {
			return null;
		}
		Calendar cal = getCalendarForDate(date, timeZone);
		Calendar timeCal = getCalendarForDate(time, timeZone);
		int[] timeFields = { Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND };
		try {
			for (int timeField : timeFields) {
				cal.set(timeField, timeCal.get(timeField));
			}
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime();
		} catch (RuntimeException exc) {
			return null;
		}
	}

	public static Calendar getCalendarForDate(Date date, TimeZone timeZone) {
		Calendar result = new GregorianCalendar(timeZone);
		result.setLenient(false);
		result.setTime(date);
		return result;
	}

	/**
	 * Tries to create a {@link Date}-object as follows:
	 * <ol>
	 *     <li>The given <code>value</code> is checked syntactically against the pattern created from the given <code>dateFormat</code>. If
	 *     the value does not match the pattern, an error is created.</li>
	 *     <li>If partially known dates are allowed and a date part is unknown, the original value is returned.</li>
	 *     <li>Otherwise the {@link Date}-object created from the given value is returned.</li>
	 * </ol>
	 *
	 * <b>Note:</b> Due to the syntax check, only the following date format fragments are allowed for a <code>dateFormat</code>:
	 * <ol>
	 *     <li><code>yyyy</code> for the year</li>
	 *     <li><code>MM</code> for the month</li>
	 *     <li><code>dd</code> for the day</li>
	 *     <li><code>'T'</code> as separator between date and time</li>
	 *     <li><code>HH</code> or <code>hh</code> for the hours</li>
	 *     <li><code>mm</code> for the minutes</li>
	 *     <li><code>ss</code> for the seconds</li>
	 *     <li><code>a</code> for AM/PM marker</li>
	 * </ol>
	 *
	 * @param value
	 * 		string from which a {@link Date}-object should be created. May not be <code>null</code> or empty.
	 * @param dateFormat
	 * 		format of the date, see method description, specially the <b>Note</b>. May not be <code>null</code> or empty.
	 * @param baseYearOrDefault
	 * 		the base year which could be taken from the document model (see DocumentModelInfo.baseYear). This year
	 * 		is relevant for <code>dateFormat</code>s containing <code>MM</code> and <code>dd</code> but not <code>yyyy</code>, in
	 * 		order
	 * 		to determine
	 * 		if the February 29th is a valid input or not.
	 * 		If no base year is defined in the document model, a default value should be given (in A12 kernel the year
	 * 		<code>2000</code>
	 * 		is commonly used). May not be 0.
	 * @param arePartiallyKnownDatesAllowed
	 * 		<code>true</code> if partially known dates are allowed, e.g. <code>2020-00-00</code>, otherwise <code>false</code>.
	 * @param timeZone
	 * 		the time zone which should be considered while creating the {@link Date}-object.  This could be taken from the
	 * 		document model (see DocumentModelConfig#timeZone). May not be <code>null</code>.
	 * @return see method description.
	 *
	 * @throws Error
	 * 		if the base year is not valid or the value cannot be parsed to a {@link Date}-object (see also method description).
	 */
	public static Object createDateIfPossible(String value, String dateFormat, int baseYearOrDefault,
			boolean arePartiallyKnownDatesAllowed, TimeZone timeZone) {
		DateAndFormat dateWithFormat = new DateAndFormat(value, dateFormat, timeZone);
		if (!ValidationDateParser.isSyntacticallyCorrect(dateWithFormat)) {
			throw new IllegalArgumentException(
					"The given value is syntactically wrong and that's why it cannot be converted to a date-object.");
		}
		if (arePartiallyKnownDatesAllowed && PartiallyKnownDatesUtil.isPartUnknown(dateWithFormat)) {
			return value;
		}
		return DateUtil.getValueAsDate(dateWithFormat, baseYearOrDefault);
	}

	/**
	 * @param value
	 *        {@link String} or {@link Date} which should be processed. May not be <code>null</code>.
	 * @param dateFormat
	 * 		format of the date to which the given date should be formatted. May not be <code>null</code> or empty.
	 * @param timeZone
	 * 		the time zone which was considered while creating the given {@link Date}-object. May not be <code>null</code>.
	 * @return string representation of the given <code>value</code>.
	 *
	 * @throws Error
	 * 		if the given <code>value</code> is not a {@link Date} or it is a {@link String} without unknown date parts.
	 */
	public static String convertDateToString(@NonNull Object value, @NonNull String dateFormat,
			@NonNull TimeZone timeZone) {
		if (value instanceof String strVal) {
			if (strVal.isEmpty()) {
				return null;
			}
			DateAndFormat dateWithFormat = new DateAndFormat(strVal, dateFormat, timeZone);
			if (PartiallyKnownDatesUtil.isPartUnknown(dateWithFormat)) {
				return (String) value;
			}
		}
		if (!(value instanceof Date)) {
			throw new IllegalArgumentException("The type of the value (" + value.getClass().getCanonicalName() + ") is not a Date");
		}
		return ValidationDateParser.formatDate((Date) value, dateFormat, timeZone);
	}

	/**
	 * @return the start and end of a date range as Date objects.
	 */
	public static Date[] createDateRange(String internalValue, String separator, String format,
			Jahresinterpretation interpretationOfYear, int baseYearOrDefault, TimeZone timeZone) {
		return createDateRange(internalValue, separator, format, interpretationOfYear, baseYearOrDefault, false, timeZone);
	}

	public static Date[] createDateRange(String internalValue, String separator, String format,
			Jahresinterpretation interpretationOfYear, int baseYearOrDefault, boolean syntaxCheck, TimeZone timeZone) {
		DateAndFormat[] dates = DateUtil.getDateRange(internalValue, separator, format, true, baseYearOrDefault,
				interpretationOfYear, timeZone);
		if (syntaxCheck) {
			for (var dateWithFormat : dates) {
				if (!ValidationDateParser.isSyntacticallyCorrect(dateWithFormat)) {
					throw new IllegalArgumentException(
							"The given value is syntactically wrong and that's why it cannot be converted to a date-object.");
				}
			}
		}
		Date rangeStart = getValueAsDate(dates[0], baseYearOrDefault);
		Date rangeEnd = getValueAsDate(dates[1], baseYearOrDefault);
		return new Date[] { rangeStart, rangeEnd };
	}

	/**
	 * Formats the given date range.
	 *
	 * @param value
	 * 		the supported type should be Date[] of size 2 or null, otherwise an error is thrown.
	 * @param rangeSeparator
	 * 		the separator between the dates of the date range.
	 * @param dateFormat
	 * 		the date format in which the dates should be formatted.
	 * @param timeZone
	 * 		the time zone the date should be interpreted in.
	 * @return the formatted date range as string.
	 */
	public static String convertDateRangeToString(Object value, String rangeSeparator, String dateFormat,
			TimeZone timeZone) {
		if (value == null || (value instanceof String strVal && strVal.isEmpty())) {
			return null;
		}
		if (!(value instanceof Date[] input)) {
			throw new IllegalArgumentException(
					"The type of the value (" + value.getClass().getCanonicalName() + ") is not a Date[]");
		}

		if (input.length != 2) {
			throw new IllegalArgumentException("Expected a Date [] of size 2 but the size is: " + input.length);
		}

		return ValidationDateParser.formatDate(input[0], dateFormat, timeZone) + rangeSeparator + ValidationDateParser.formatDate(
				input[1],
				dateFormat, timeZone);
	}
}
