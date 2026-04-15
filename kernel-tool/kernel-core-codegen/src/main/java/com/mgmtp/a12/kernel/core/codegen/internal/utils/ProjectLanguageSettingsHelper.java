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

package com.mgmtp.a12.kernel.core.codegen.internal.utils;

import java.util.Arrays;
import java.util.List;

public class ProjectLanguageSettingsHelper {

	private static final List<Character> DATE_FORMAT_CHARS = Arrays.asList('d', 'M', 'y');

	private ProjectLanguageSettingsHelper() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Retrieve the date separator from the format string.
	 * <p>
	 * The first character that is not 'd', 'M', or 'y' will be returned.
	 * <p>
	 * E.g. "dd.MM.yyyy" will result in ".",
	 * or "MM/dd/yyyy" will result in "/".
	 *
	 * @param dateFormat
	 * 		the date format.
	 */
	public static String extractDateSeparator(String dateFormat) {
		for (int i = 0; i < dateFormat.length(); i++) {
			if (!DATE_FORMAT_CHARS.contains(dateFormat.charAt(i))) {
				return String.valueOf(dateFormat.charAt(i));
			}
		}
		return "";
	}

	/**
	 * Extract the date order from a given format int the form used for
	 * DarstellungsInfoDatum.
	 * <p>
	 * E.g. "dd.MM.yyyy" will result in "TMJ",
	 * or "MM/dd/yyyy" will result in "MTJ".
	 *
	 * @param dateFormat
	 * 		the date format.
	 */
	public static String extractDateOrder(String dateFormat) {
		StringBuilder dateOrder = new StringBuilder(3);
		dateOrder.append(dateFormat.charAt(0));
		for (int i = 1; i < dateFormat.length(); i++) {
			char dateFormatChar = dateFormat.charAt(i);
			if (DATE_FORMAT_CHARS.contains(dateFormatChar) &&
					dateOrder.charAt(dateOrder.length() - 1) != dateFormatChar) {
				// For every change of an allowed dateFormat char we add the dateFormat char.
				dateOrder.append(dateFormatChar);
			}
		}
		return dateOrder.toString().replace('d', 'T').replace('y', 'J');
	}
}
