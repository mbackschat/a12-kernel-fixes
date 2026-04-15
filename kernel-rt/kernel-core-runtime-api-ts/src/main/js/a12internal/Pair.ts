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

/**
 * Container class to represent a 2 element pair.
 *
 * @param <E1> 	the type of the 1st element
 * @param <E2> 	the type of the 2nd element
 */
export class Pair<E1, E2> {
	private _entry1: E1;
	private _entry2: E2;

	// Typescript doesn't provide multiple constructors as in Java. Therefore overloading is used here.
	// The usage of extensions of the generic types doesn't need to be declared
	constructor(other: Pair<E1, E2>);
	constructor(_entry1: E1, _entry2: E2);
	constructor(pairOr_Entry1: E1 | Pair<E1, E2>, _entry2?: E2) {
		if (typeof _entry2 === "undefined") {
			const pair = <Pair<E1, E2>>pairOr_Entry1;
			this._entry1 = pair.entry1;
			this._entry2 = pair.entry2;
		} else {
			const entry = <E1>pairOr_Entry1;
			this._entry1 = entry;
			this._entry2 = _entry2;
		}
	}

	set entry1(entry1: E1) {
		this._entry1 = entry1;
	}

	get entry1(): E1 {
		return this._entry1;
	}

	set entry2(entry2: E2) {
		this._entry2 = entry2;
	}

	get entry2(): E2 {
		return this._entry2;
	}
}
