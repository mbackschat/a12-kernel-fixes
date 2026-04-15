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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * ACHTUNG:
 * Bei Performance-Optimierungen in dieser Klasse muss folgendes beachtet werden:
 * Die Java-API der Klasse java.text.SimpleDateFormat sagt zum Thema Synchronization folgendes:
 * Date formats are not synchronized. It is recommended to create separate format instances for each
 * thread.
 * If multiple threads access a format concurrently, it must be synchronized externally.
 * <p>
 * Der Versuch das SimpleDateFormat jeweils nur einmal zu erzeugen, in einer Map zu halten,
 * den Aufruf "formatter.parse(value)" in eine eigene Methode zu extrahieren und diese zu
 * synchronisieren hat im Lasttest zu einer laengeren Ausfuerungsdauer gefuehrt.
 */
public class ValidationDateParser {

	private static final Map<String, Pattern> SIMPLE_DATE_PATTERN = new ConcurrentHashMap<>();
	private static final String TWO_DIGIT_PATTERN = "[0-9]{2}";
	private static final String TWO_DIGIT_OPT_PATTERN = "[0-9]{1,2}";
	private static final String FOUR_DIGIT_PATTERN = "[0-9]{4}";
	private static final String AM_PM_PATTERN = "(AM|PM)";

	private ValidationDateParser() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Returns the Date object that corresponds to the passed string in terms of format.
	 * Throws an error if the string does not represent a valid date based on the format.
	 * In contrast to Java's SimpleDateFormat, the entire string is parsed;
	 * it does not stop at a valid starting string.<br>
	 * The following applies to the meaning of the date format:
	 * <ul>
	 * 	<li>Exactly one 'd' in the format means that days can be specified with a single-digit numerical value
	 * 		with or without a leading 0, "dd" means that the day must always be specified with two digits,
	 * 		if necessary with a leading 0.</li>
	 * 	<li>Exactly one 'M' in the format means that months can be specified with a single-digit numerical value
	 * 		with or without a leading 0, "MM" means that the month must always be specified with two digits,
	 * 		if necessary with a leading 0.</li>
	 * 	<li>In the date format the year must always be specified as "yyyy".
	 * 		A not exactly four-digit year may not be parsed, an error must be thrown.</li>
	 * 	<li>If exactly one 'd' or 'M' occurs in the format, the format must contain a date separator, i.e. a character
	 * 		that is not 'd', 'M' or 'y'. The caller of this method must ensure that.
	 * 		This means that we don't have to worry about formats such as "dM" when parsing, where the value "111"
	 * 		is not unique as a date.</li>
	 * </ul>
	 */
	public static Date parseDate(DateAndFormat param) throws ParseException {
		return parseDate(param.getDate(), param.getFormat(), param.getTimeZone(), true);
	}

	/**
	 * Creates a Date object that corresponds to the date string from the passed parameter in terms of format.
	 * Correct parsing of the date string is only guaranteed if the date string matches the regular expression
	 * generated from the format string, since there is no check against this regular expression.<br>
	 * In particular, the method should only be used if this check has already been carried out beforehand.
	 * <p>If the date string cannot be parsed with the SimpleDateFormat heuristic, an error is thrown
	 * because this is unexpected behavior.</p>
	 */
	public static Date parseDateWithoutSyntaxCheck(DateAndFormat param) {
		try {
			return parseDate(param.getDate(), param.getFormat(), param.getTimeZone(), false);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static Date parseDate(String value, String checkFormat, TimeZone timeZone, boolean checkSyntax) throws ParseException {
		if (checkSyntax && !isSyntacticallyCorrect(value, checkFormat)) {
			throw new ParseException(value, 0);
		}
		// SimpleDateFormat is not thread-safe, therefore create an instance each time
		// otherwise a synchronized access is needed
		SimpleDateFormat formatter = new SimpleDateFormat(checkFormat, Locale.US);
		formatter.setTimeZone(timeZone);
		formatter.setLenient(false);
		return formatter.parse(value);
	}

	/**
	 * Format a date to a string representation in the given format.
	 *
	 * @param value
	 * 		the date value.
	 * @param checkFormat
	 * 		the format for the date string.
	 * @return the string representation of the date.
	 */
	public static String formatDate(Date value, String checkFormat, TimeZone timeZone) {
		SimpleDateFormat formatter = new SimpleDateFormat(checkFormat, Locale.US);
		formatter.setTimeZone(timeZone);
		return formatter.format(value);
	}

	/**
	 * Returns <code>true</code>, if the value matches the regular expression, otherwise <code>false</code>.<br>
	 * The date parts are replaced in the format as follows: 'd' for the day, 'M' for the month and 'y' for the year.
	 * 'd' or 'M' means that a day or month with one or two digits is allowed.
	 * "dd" or "MM" means that a day or month has to have two digits.<br>
	 * The check does not include details, for example it is not checked whether the day exists for the month.
	 */
	public static boolean isSyntacticallyCorrect(DateAndFormat param) {
		return isSyntacticallyCorrect(param.getDate(), param.getFormat());
	}

	private static boolean isSyntacticallyCorrect(String value, String checkFormat) {
		Pattern pattern = SIMPLE_DATE_PATTERN.computeIfAbsent(checkFormat, ValidationDateParser::getDatePattern);
		return pattern.matcher(value).matches();
	}

	private static Pattern getDatePattern(String format) {
		String s = format.replace("MM", TWO_DIGIT_PATTERN).replace("M", TWO_DIGIT_OPT_PATTERN)
				.replace("dd", TWO_DIGIT_PATTERN).replace("d", TWO_DIGIT_OPT_PATTERN)
				.replace("yyyy", FOUR_DIGIT_PATTERN).replace(".", "\\.")
				// difference to VBA: time is only used in Java, C++ and Javascript_VK
				.replace("HH", TWO_DIGIT_PATTERN).replace("H", TWO_DIGIT_OPT_PATTERN)
				.replace("mm", TWO_DIGIT_PATTERN).replace("ss", TWO_DIGIT_PATTERN)
				.replace("hh", TWO_DIGIT_PATTERN).replace("h", TWO_DIGIT_OPT_PATTERN)
				.replace("a", AM_PM_PATTERN).replaceAll("'([^']*)'", "$1");
		return Pattern.compile(s);
	}

	/**
	 * Returns the string representation of the date in the passed format,
	 * where the day and month are always given in two digits.<br>
	 * Ex: for the date April 7, 1999 and the format "yyyy-M-d" it returns "1999-04-07".
	 */
	public static String makeStringDayMonthTwoDigits(Date date, String format, TimeZone timeZone) {
		format = DateUtil.makeFormatStringDayMonthTwoDigits(format);
		return formatDate(date, format, timeZone);
	}
}
