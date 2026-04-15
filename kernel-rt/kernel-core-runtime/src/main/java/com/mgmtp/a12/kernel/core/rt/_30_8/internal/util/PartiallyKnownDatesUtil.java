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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

/**
 * This class helps with the handling of partially known dates. The method require:
 * <ul>
 * <li>the date format must contain the day, month and year part</li>
 * <li>the given value must be syntactically valid, i.e. complies with the regular expression which comes from the format.</li>
 * </ul>
 */
public class PartiallyKnownDatesUtil {

	private PartiallyKnownDatesUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Replaces the '00' in a partially known date, if present, depending on whether start or end is sought.
	 * <p>
	 * <b>Precondition:</b> the date is "purely syntactically" correct, i.e. matches the regular expression
	 * given by the format. May ONLY be used for dates without time.
	 * </p>
	 */
	public static void replaceMonthAndYearIfUnknown(DateAndFormat dateWithFormat, DateUtil.DateInterval intervalSpec) {
		if (StringUtils.isEmpty(dateWithFormat.getDate()) || StringUtils.isEmpty(dateWithFormat.getFormat())) {
			return;
		}

		// First the month must be made concrete so that in case of IntervalEnd
		// the last day of the month can be determined.
		replaceUnknownDatePart(dateWithFormat, DatePart.MONTH, intervalSpec.getGivenMonth());
		if (isDatePartUnknown(dateWithFormat, DatePart.DAY)) {
			replaceDatePart(dateWithFormat, DatePart.DAY, "01");
			if (intervalSpec == DateUtil.DateInterval.INTERVAL_END) {
				String newDay = DateUtil.getLastDayOfMonth(dateWithFormat);
				replaceDatePart(dateWithFormat, DatePart.DAY, newDay);
			}
		}
	}

	/**
	 * Returns 'true' if the year is unknown.
	 */
	public static boolean isYearUnknown(DateAndFormat dateWithFormat) {
		return isDatePartUnknown(dateWithFormat, DatePart.YEAR);
	}

	/**
	 * Returns true if at least one of the date parts is unknown.
	 */
	public static boolean isPartUnknown(DateAndFormat dateWithFormat) {
		return isDatePartUnknown(dateWithFormat, DatePart.DAY) ||
				isDatePartUnknown(dateWithFormat, DatePart.MONTH) ||
				isDatePartUnknown(dateWithFormat, DatePart.YEAR);
	}

	/**
	 * Returns 'true' if the queried DatePart is not known for the passed date.
	 */
	private static boolean isDatePartUnknown(DateAndFormat dateWithFormat, DatePart datePart) {
		String value = getValueOfDatePart(dateWithFormat, datePart);
		return datePart.getUnknownString().equals(value);
	}

	/**
	 * Converts a partially known date into a concrete date. The format remains unchanged.<br>
	 * The first day, first month and year 2000 are set if the respective part of the date is not known.
	 * For example, "00.00.0000" is replaced by "01.01.2000".<br>
	 * A list of the {@link DatePart}s that have been replaced is returned.
	 * <p>
	 * <b>Precondition:</b> the date is "purely syntactically" correct, i.e. matches the regular expression
	 * given by the format. May ONLY be used for dates without time.
	 * </p>
	 */
	public static List<DatePart> replaceUnknownDateParts(DateAndFormat dateWithFormat) {
		List<DatePart> changedDateParts = new ArrayList<>();
		if (replaceUnknownDatePart(dateWithFormat, DatePart.DAY, "01")) {
			changedDateParts.add(DatePart.DAY);
		}
		if (replaceUnknownDatePart(dateWithFormat, DatePart.MONTH, "01")) {
			changedDateParts.add(DatePart.MONTH);
		}
		if (replaceUnknownDatePart(dateWithFormat, DatePart.YEAR, "2000")) {
			changedDateParts.add(DatePart.YEAR);
		}
		return changedDateParts;
	}

	/**
	 * Converts the string representation of a partially known date into a concrete date with the given format.<br>
	 * The first day, first month and year 1970 are set if the respective Date part is not known.
	 * For example, "00.00.0000" is replaced by "01.01.1970".<br>
	 * A date with a given format is returned.
	 * <p>
	 * <b>Precondition:</b> May ONLY be used for dates without time.
	 * </p>
	 */
	public static Date parseApproximatedDate(String partiallyKnownDate, String format, TimeZone timeZone) {

		DateAndFormat dateWithFormat = new DateAndFormat(partiallyKnownDate, format, timeZone);
		if (!ValidationDateParser.isSyntacticallyCorrect(dateWithFormat)) {
			throw new IllegalArgumentException(
					"The given value is syntactically wrong and that's why it cannot be converted to the corresponding approximated date-object.");
		}
		replaceUnknownDatePart(dateWithFormat, DatePart.DAY, "01");
		replaceUnknownDatePart(dateWithFormat, DatePart.MONTH, "01");
		replaceUnknownDatePart(dateWithFormat, DatePart.YEAR, "1970");
		return ValidationDateParser.parseDateWithoutSyntaxCheck(dateWithFormat);
	}

	/**
	 * Creates a {@link Calendar}-instance based on the given date and format. If a date part is
	 * unknown, it will be replaced as follows:
	 * <ul>
	 * <li>year 0000 -&gt; 4000</li>
	 * <li>month 00 -&gt; 12</li>
	 * <li>day 00 -&gt; &lt;last day of the month&gt;</li>
	 * </ul>
	 * If at least one part of the date is unknown the time is set some millisecond before the next
	 * day.
	 * Note: the given value should be formally correct
	 */
	public static Calendar createCalendar(DateAndFormat dateAndFormat) {
		Calendar cal = new GregorianCalendar(dateAndFormat.getTimeZone());
		if (DatePart.HOUR.isInFormat(dateAndFormat.getFormat())) {
			// for the field types "date time" and "time" all parts are known -> no replacements needed
			Date date = ValidationDateParser.parseDateWithoutSyntaxCheck(dateAndFormat);
			cal.setTime(date);
		} else {
			int initialGap = 4;
			int millisecondGap = initialGap - setDatePart(cal, dateAndFormat, DatePart.YEAR, 4000);
			millisecondGap = millisecondGap - setDatePart(cal, dateAndFormat, DatePart.MONTH, 12);
			int lastDayOfTheMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			millisecondGap = millisecondGap - setDatePart(cal, dateAndFormat, DatePart.DAY, lastDayOfTheMonth);
			if (millisecondGap < initialGap) {
				// if one part of the date is unknown, we set the time some milliseconds before the next day
				setTimeToMaximum(cal, millisecondGap);
			} else {
				// if all date parts are known, we set the time to 0
				DateUtil.clearTime(cal);
			}
		}
		return cal;
	}

	/**
	 * Sets the time part to 23:59:59.(100 - &lt;gap&gt;)
	 */
	private static void setTimeToMaximum(Calendar cal, int millisecondGap) {
		cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY) - 1);
		cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE) - 1);
		cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND) - 1);
		cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND) - millisecondGap);
	}

	/**
	 * Sets the date part in the {@link Calendar}-instance with the corresponding value of the given
	 * date. If the value of the given date part is unknown, this part is set with the given default
	 * value.
	 *
	 * @return <code>1</code> if value of the date part was unknown, otherwise <code>0</code>.
	 */
	private static int setDatePart(Calendar cal, DateAndFormat dateAndFormat, DatePart datePart, int defaultValue) {
		String datePartValue = getValueOfDatePart(dateAndFormat, datePart);
		int calendarField = getCalendarField(datePart);
		if (datePart.getUnknownString().endsWith(datePartValue)) {
			cal.set(calendarField, defaultValue);
			return 1;
		} else {
			cal.set(calendarField, Integer.parseInt(datePartValue));
			return 0;
		}
	}

	/**
	 * Maps the values of {@link DatePart} to the corresponding calendar fields. Throws an
	 * {@link IllegalArgumentException} if for the given date part no mapping is defined.
	 */
	private static int getCalendarField(DatePart datePart) {
		switch (datePart) {
			case DAY:
				return Calendar.DAY_OF_MONTH;
			case MONTH:
				return Calendar.MONTH;
			case YEAR:
				return Calendar.YEAR;
			default:
				throw new IllegalArgumentException(
						String.format("Given date part '%s' is not supported yet.", datePart.getFormatName()));
		}
	}

	/**
	 * Replaces the specified date part in the date with the new value if the date part is not known (i.e. "00" or "0000").
	 * The format remains unchanged.<br>
	 * Returns <code>true</code> if a replacement was made, <code>false</code> otherwise.
	 * <p>
	 * <b>Precondition:</b> The new value is always two digits for day and month, and always four digits for year.
	 * </p>
	 */
	private static boolean replaceUnknownDatePart(DateAndFormat dateWithFormat, DatePart datePart,
			String newValue) {

		String format = dateWithFormat.getFormat();
		if (StringUtils.isEmpty(format) || !datePart.isInFormat(format) || !isDatePartUnknown(dateWithFormat, datePart)) {
			return false;
		}

		return replaceDatePart(dateWithFormat, datePart, newValue);
	}

	private static boolean replaceDatePart(DateAndFormat dateWithFormat, DatePart datePart,
			String newDatePart) {
		String format = dateWithFormat.getFormat();
		String dateSeparator = DateUtil.getDateSeparator(format);

		String value = dateWithFormat.getDate();
		String newValue;
		if (!dateSeparator.isEmpty()) {
			String[] parts = StringUtils.splitPreserveAllTokens(value, dateSeparator);
			int index = getIndexOfDatePartInFormat(format, dateSeparator, datePart);

			parts[index] = newDatePart;
			newValue = StringUtils.join(parts, dateSeparator);

		} else {
			int pos = format.indexOf(datePart.getFormatName());

			StringBuilder sb = new StringBuilder(value);
			sb.replace(pos, pos + newDatePart.length(), newDatePart);
			newValue = sb.toString();
		}
		dateWithFormat.setDate(newValue);
		return !newValue.equals(value);
	}

	/**
	 * Replaces the specified date parts with "00" (for day and month) or "0000" (for year).
	 * <p>
	 * <b>Precondition:</b> the date is "purely syntactically" correct, i.e. matches the regular expression
	 * given by the format. May ONLY be used for dates without time.
	 * </p>
	 */
	static void setDatePartsToUnknown(DateAndFormat dateWithFormat, Collection<DatePart> dateParts) {
		for (DatePart datePart : dateParts) {
			replaceDatePart(dateWithFormat, datePart, datePart.getUnknownString());
		}
	}

	/**
	 * @return the value of the given {@link DatePart} of the given {@link DateAndFormat}.
	 * 		If the given <code>datePart</code> is not present in the <code>dateWithFormat</code> an empty string is returned.
	 */
	public static String getValueOfDatePart(DateAndFormat dateWithFormat, DatePart datePart) {
		String format = dateWithFormat.getFormat();
		if (!datePart.isInFormat(format)) {
			return "";
		}

		String dateSeparator = DateUtil.getDateSeparator(format);
		String value = dateWithFormat.getDate();
		if (!dateSeparator.isEmpty()) {
			// use date separator for splitting
			String[] teile = StringUtils.splitPreserveAllTokens(value, dateSeparator);
			int index = getIndexOfDatePartInFormat(format, dateSeparator, datePart);

			if (index >= teile.length) {
				return "";
			}
			return teile[index]; // fits for dates with and without optional zeros
		} else {
			// No date separator, i.e. it can't be a date with optional zeros
			int pos = format.indexOf(datePart.getFormatName());
			int end = format.lastIndexOf(datePart.getFormatName());
			return value.substring(pos, end + 1);
		}
	}

	private static int getIndexOfDatePartInFormat(String format, String separator, DatePart datePart) {
		String[] formatTeile = StringUtils.splitPreserveAllTokens(format, separator);

		for (int i = 0; i < formatTeile.length; i++) {
			if (datePart.isInFormat(formatTeile[i])) {
				return (i);
			}
		}

		throw new IllegalArgumentException(
				String.format("Invalid partially known date with format '%s' and date separator '%s'.", format, separator));
	}
}
