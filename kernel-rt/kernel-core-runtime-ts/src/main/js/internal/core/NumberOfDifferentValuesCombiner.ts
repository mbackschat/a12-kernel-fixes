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
import { HashSet } from "@com.mgmtp.a12.utils/utils-collections";

import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import { VkBigDecimal } from "../util/VkBigDecimal.js";

import type { VkValue } from "../util/VkValue.js";

import type { Combiner } from "./Combiner.js";

/**
 * Implementation of a {@link Combiner} for {@link VkValue} subclasses that returns the number of
 * distinct non-null values.
 * Distinctness is based on the equality/hashcode definitions that are used by {@link HashSet}
 */
export class NumberOfDifferentValuesCombiner<T extends VkValue>
	implements Combiner<T, VkBigDecimal> {
	private _values: HashSet<T> = new HashSet(
		e => e.hash(),
		(a, b) => a.equals(b)
	);
	private _omittedEmptyValue = false;

	public getNonRelevantValue(): VkBigDecimal {
		return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
	}

	public getValue(containsFilter: boolean): VkBigDecimal {
		const kannGroesserWerden: boolean =
			containsFilter || this._omittedEmptyValue || this.nichtAngegeben();
		const kannKleinerWerden: boolean = containsFilter || this.nichtAngegeben(); // if any non-empty values have "nichtAngegeben"
		// (more accurate name: "veränderungMöglichBeiWeiterenAngaben"), some pair of distinct values may become equal
		// -> kannKleinerWerden must be set to true.
		// Note that this does not mean that kannKleinerWerden == true if any input value to the combiner
		// was based on a Field that is nichtAngegeben -- that value would be empty (hasValue() == false)
		// and not added to .values at all
		return new VkBigDecimal(
			this._values.size,
			this.nichtPruefungsrelevant(),
			kannGroesserWerden,
			kannKleinerWerden
		);
	}

	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	public addValue(value: T, fd: IFormatDefinition, testValidity: boolean): void {
		if (value.hasValue) {
			this._values.add(value);
		} else {
			this._omittedEmptyValue = true;
		}
	}

	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	public setOmittedTail(fd: IFormatDefinition): void {
		this._omittedEmptyValue = true;
	}

	private nichtAngegeben(): boolean {
		return [...this._values].find(e => e.isNichtAngegeben) !== undefined;
	}

	private nichtPruefungsrelevant(): boolean {
		return [...this._values].find(e => e.isNichtPruefungsrelevant) !== undefined;
	}

    public canChange(): boolean {
        return true;
    }
}
