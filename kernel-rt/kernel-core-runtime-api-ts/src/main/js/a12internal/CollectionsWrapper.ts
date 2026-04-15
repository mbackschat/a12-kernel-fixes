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
import type { HashFunction } from "@com.mgmtp.a12.utils/utils-collections";
import { ComparativeSortedSet, HashMap as HashMapExt, HashSet as HashSetExt } from "@com.mgmtp.a12.utils/utils-collections";

export interface IHashCode {
	hashCode(): number;
}

export interface IEquatable {
	equals(x: this): boolean;
}

export type IHashAndEquals = IHashCode & IEquatable;

export function numbersconv(k: any): number {
	const str = typeof k === "string" ? k : JSON.stringify(k);
	return stringconv(str);
}

export function stringconv(str: string): number {
	if (str.length === 0) {
		return 0;
	}
	return Array.from(str).reduce((hash, ch) => ((hash << 5) - hash + (ch.codePointAt(0) ?? 0)), 0);
}

export function arraysEqual(a: any, b: any): boolean {
	if (a === b) {
		return true;
	}
	if (!Array.isArray(a) || !Array.isArray(b) || a.length !== b.length) {
		return false;
	}
	return a.every((element: any, index: number) => element === b[index]);
}

export function hashFuncNumberArray(object: any): number {
	if (object == null) {
		return 0;
	}
	let result = 1;
	for (let i = 0; i < object.length; i++) {
		result = 31 * result + object[i];
	}
	return result;
}

export class HashSet<T extends IHashAndEquals> extends HashSetExt<T> {
	constructor(values?: Iterable<T> | null) {
		super(
			n => n.hashCode(),
			(a, b) => a.equals(b)
		);

		if (values) {
			for (const value of values) {
				this.add(value);
			}
		}
	}
}

export class ArraySet<T> extends HashSetExt<T> {
	constructor() {
		super(hashFuncNumberArray, arraysEqual);
	}
}

/**
 * A hashing set for number arrays that uses custom hashing and equality function to only compare
 * the contained number arrays up to a given index.
 */
export class NumberSubArraySet extends HashSetExt<number[]> {
	constructor(upTo: number) {
		super(numberArrayHashUpToFunc(upTo), numberArraysEqualUpToFunc(upTo));
	}
}

function numberArrayHashUpToFunc(inclRight: number): HashFunction<number[]> {
	return function numberArrayHashUpTo(array) {
		if (array == undefined) {
			return 0;
		}
		let result = 1;
		for (let i = 0; i <= inclRight; i++) {
			result = 31 * result + array[i];
		}
		return result;
	}
}

function numberArraysEqualUpToFunc(inclRight: number): (a: number[], b: number[]) => boolean {
	return function numberArraysEqualUpTo(a, b) {
		if (a === b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		for (let i = 0; i <= inclRight; ++i) {
			if (a[i] !== b[i]) {
				return false;
			}
		}
		return true;
	}
}

export class HashMap<T extends IHashAndEquals, V> extends HashMapExt<T, V> {
	constructor(entries?: Iterable<[T, V]> | null) {
		super(
			n => n.hashCode(),
			(a: IEquatable, b: IEquatable) => a.equals(b)
		);

		if (entries) {
			for (const [key, value] of entries) {
				this.set(key, value);
			}
		}
	}
}

export class ArrayMap<T, V> extends HashMapExt<T, V> {
	constructor() {
		super(hashFuncNumberArray, arraysEqual);
	}
}

export class SortedSet<T> extends ComparativeSortedSet<T> {
	constructor() {
		super((a, b) => {
			if (a < b) {
				return -1;
			} else if (a === b) {
				return 0;
			} else {
				return 1;
			}
		});
	}
}