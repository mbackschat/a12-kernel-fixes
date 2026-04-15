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
export class ArrayUtils {
	/**
	 * @param array 	the array to search.
	 * @param value 	the value to search for.
	 * @return the first <code>i</code> so that <code>array[i] == value</code> or -1 if no such
	 *         <code>i</code> exists.
	 */
	public static indexOf(array: number[], value: number): number {
		for (let i = 0; i < array.length; i++) {
			if (array[i] === value) {
				return i;
			}
		}
		return -1;
	}

	public static contains(array: number[], elem: number): boolean {
		return array.includes(elem);
	}

	/**
	 * Copies the specified array, truncating or padding with zeros if necessary the parameter "to"
	 * is greater that the length of the original array.
	 * @param array 	array from which elements will be copied.
	 * @param from 	index at the original array from which the elements begin to be copied. If not given,
	 * 			  	0 will be taken.
	 * @param to 	this defines together with the parameter "from" the length of the returned array.
	 * 			  	If the given value is less or equal to the length of the original array, to this index
	 * 			  	the elements will be copied. If it is greater, the extra elements will be 0.
	 */
	public static copyOf(array: number[], from?: number, to?: number): number[] {
		const result: number[] = [];
		const _from: number = from ? from : 0;
		const _to: number = to ? to : array.length;
		for (let i: number = _from; i < _to; i++) {
			if (i < array.length) {
				result.push(array[i]);
			} else {
				result.push(0);
			}
		}
		return result;
	}

	public static sindGleichAbPosition(array1: number[], array2: number[], pos: number): boolean {
		if (array1.length !== array2.length || array1.length <= pos) {
			return false;
		}
		for (let i: number = pos; i < array1.length; i++) {
			if (array1[i] !== array2[i]) {
				return false;
			}
		}
		return true;
	}

	public static removeIdx(pos: number, array: number[]): number[] {
		const result = array.slice();
		result.splice(pos, 1);
		return result;
	}
}
