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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.util;

import java.util.Arrays;
import java.util.List;

/**
 * Hilfsklasse für allgemeine Runtimehilfsmethoden.
 */
public class RuntimeUtils {

	private RuntimeUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static long[] nonRepeatableDescendentIndices(long[] indices, int numLevelsToAdd) {
		long[] result = Arrays.copyOf(indices, indices.length + numLevelsToAdd);
		Arrays.fill(result, indices.length, result.length, 1L);
		return result;
	}

	public static List<Long> parentIndices(long[] indices) {
		return createListForSubArray(indices, 0, indices.length - 1);
	}

	public static List<Long> grandParentIndices(long[] indices) {
		return createListForSubArray(indices, 0, indices.length - 2);
	}

	/**
	 * Create a List from a portion of an array.
	 *
	 * @param array
	 * 		the array to be converted.
	 * @param start
	 * 		the start index for the array range. Must be {@literal >= 0}.
	 * @param end
	 * 		the end index of the array range (excluding).
	 * @return a List created from the original array range.
	 */
	public static List<Long> createListForSubArray(long[] array, int start, int end) {
		int realEnd = Math.min(array.length, end);
		return Arrays.stream(array, start, realEnd).boxed().toList();
	}

	/**
	 * Remove surrounding quotes from a string.
	 *
	 * @param s
	 * 		the string with quotes.
	 * @return the string without the quotes.
	 */
	public static String removeQuotes(String s) {
		assert s.startsWith("\"");
		assert s.endsWith("\"");

		if (s.length() < 2) {
			return "";
		}
		return s.substring(1, s.length() - 1);
	}

	/**
	 * @return a copy of `stringValue` where all occurrences of `\r\n` are replaced with `\n`.
	 */
	public static String normalizeLinebreaks(String stringValue) {
		return stringValue.replace("\r\n", "\n");
	}
}
