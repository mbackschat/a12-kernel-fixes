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
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class ArrayUtils {

	private ArrayUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * @param array
	 * 		the array to search.
	 * @param value
	 * 		the value to search for.
	 * @return the first <code>i</code> so that <code>array[i] == value</code> or -1 if no such
	 * 		<code>i</code> exists.
	 */
	public static int indexOf(long[] array, long value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				return i;
			}
		}
		return -1;
	}

	public static boolean contains(long[] array, long elem) {
		return LongStream.of(array).anyMatch(x -> x == elem);
	}

	/**
	 * Erzeuge einen Long-Array aus seiner Stringdarstellung
	 */
	public static long[] erzeugeLongArray(String s) {
		String[] teile = s.split(",");

		long[] result = new long[teile.length];
		for (int i = 0; i < teile.length; i++) {
			result[i] = Long.parseLong(teile[i]);
		}
		return result;
	}

	public static boolean sindGleichAbPosition(long[] array1, long[] array2, int pos) {
		if (array1.length != array2.length || array1.length <= pos) {
			return false;
		}
		for (int i = pos; i < array1.length; i++) {
			if (array1[i] != array2[i]) {
				return false;
			}
		}
		return true;
	}

	public static List<Long> toList(long[] longArray) {
		return Arrays.stream(longArray).boxed().collect(Collectors.toList());
	}

	/**
	 * return a copy of array with element inserted at index insertIndex
	 */
	public static long[] inserted(int insertIndex, long element, long[] array) {
		// is there really no standard library function for this?
		// is it really impossible to implement generically with
		// java arrays?
		long[] result = new long[array.length + 1];
		System.arraycopy(array, 0, result, 0, insertIndex);
		result[insertIndex] = element;
		System.arraycopy(array, insertIndex, result, insertIndex + 1, array.length - insertIndex);
		return result;
	}

	/**
	 * return a copy of a with e inserted at the front
	 */
	public static long[] prepended(long e, long[] a) {
		return inserted(0, e, a);
	}

}
