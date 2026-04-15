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

import java.util.stream.IntStream;

/**
 * Represents a part of a date (year, month, day, hour, minute and second).
 */
public enum DatePart {
	YEAR('y', "0000"),
	MONTH('M', "00"),
	DAY('d', "00"),
	HOUR('H', "00", 'h'),
	MINUTE('m', "00"),
	SECOND('s', "00");
	private final char formatName;
	private final String unknownString;
	private final char[] alternativeFormats;

	/**
	 * Represents a part of a date.
	 *
	 * @param formatName
	 * 		the name of the date part (e.g. "y" for year)
	 * @param unknownString
	 * 		the string with which an unknown date part should be replaced
	 * @param alternativeFormats
	 * 		an optional array of alternative names of the date part for formatting
	 */
	DatePart(char formatName, String unknownString, char... alternativeFormats) {
		this.formatName = formatName;
		this.unknownString = unknownString;
		this.alternativeFormats = alternativeFormats;
	}

	/**
	 * @return true if the formatName (e.g. "y" for year) is present in the given format. Otherwise false.
	 */
	boolean isInFormat(String format) {
		return format.indexOf(formatName) != -1 ||
				IntStream.range(0, alternativeFormats.length).mapToObj(i -> alternativeFormats[i]).anyMatch(altFormatName ->
						format.indexOf(altFormatName) != -1);
	}

	/**
	 * @return the name of the date part (e.g. "y" for year)
	 */
	char getFormatName() {
		return this.formatName;
	}

	/**
	 * @return the corresponding date part (year, month or day) to the given order fragment string.
	 * 		If the given order fragment string is not "YEAR", "MONTH" or "DAY" an error is thrown.
	 */
	public static DatePart parseFragmentOrder(char orderFragment) {
		if (orderFragment == 'J') {
			return DatePart.YEAR;
		}
		if (orderFragment == 'M') {
			return DatePart.MONTH;
		}
		if (orderFragment == 'T') {
			return DatePart.DAY;
		}
		throw new IllegalArgumentException(String.format("The parameter '%s' is invalid.", orderFragment));
	}

	/**
	 * @return the string with which an unknown date part should be replaced
	 */
	String getUnknownString() {
		return unknownString;
	}
}
