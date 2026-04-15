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
import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import { VkDate } from "../util/VkDate.js";

import type { Combiner } from "./Combiner.js";

/**
 * Abstract implementation of a {@link Combiner} for {@link VkDate}s.
 */
export abstract class DateCombiner implements Combiner<VkDate, VkDate> {
	protected value: VkDate = VkDate.NULL_DATE;
	protected omittedTail = false;

	public getNonRelevantValue(): VkDate {
		return VkDate.NON_RELEVANT_DATE;
	}

	public getValue(containsFilter: boolean): VkDate {
		return containsFilter || this.omittedTail ? this.value.withNichtAngegeben() : this.value;
	}

	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	public addValue(val: VkDate, fd: IFormatDefinition | undefined, testValidity: boolean): void {
		this.value = this.combineWith(val);
	}

	abstract combineWith(d: VkDate): VkDate;

	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	public setOmittedTail(fd: IFormatDefinition | undefined): void {
		this.omittedTail = true;
	}

    public canChange(): boolean {
        return true;
    }
}

export class MaxValueCombiner extends DateCombiner {
	public combineWith(d2: VkDate): VkDate {
		return this.value.max(d2);
	}
}

export class MinValueCombiner extends DateCombiner {
	public combineWith(d2: VkDate): VkDate {
		return this.value.min(d2);
	}
}
