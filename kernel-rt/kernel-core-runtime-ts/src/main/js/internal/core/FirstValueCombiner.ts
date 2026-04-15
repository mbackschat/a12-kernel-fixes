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
import type { VkValue } from "../util/VkValue.js";
import { VkDate } from "../util/VkDate.js";
import { VkBigDecimal } from "../util/VkBigDecimal.js";
import { VkDateRange } from "../util/VkDateRange.js";
import { VkString } from "../util/VkString.js";

import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";

import type { Combiner } from "./Combiner.js";

export abstract class FirstValueCombiner<T extends VkValue> implements Combiner<T, T> {
    protected value: T | null = null;
    protected notGivenFieldBeforeFirstGiven = false;
    protected nonRelevantFieldBeforeFirstGiven = false;
    protected omittedTail = false;

    abstract getNonRelevantValue(): T;
    protected abstract notGivenValue(): T;
    protected abstract createValue(value: T, notGiven: boolean, nonRelevant: boolean): T;

    getValue(containsFilter: boolean): T {
        const notGiven = this.notGivenFieldBeforeFirstGiven || containsFilter || ((this.value === null) && this.omittedTail);
        const nonRelevant = this.nonRelevantFieldBeforeFirstGiven; // analog Kommentar in Java
        const val = this.value !== null ? this.value : this.notGivenValue();

        if (nonRelevant) {
            return this.getNonRelevantValue();
        }
        return this.createValue(val, notGiven, nonRelevant);
    }

    canChange(): boolean {
        return this.value === null && !this.nonRelevantFieldBeforeFirstGiven;
    }

    addValue(value: T, _fd: IFormatDefinition, _testValidity: boolean): void {
        if (this.value === null) {
            if (value.isNichtAngegeben) {
                this.notGivenFieldBeforeFirstGiven = true;
            }
            if (value.isNichtPruefungsrelevant) {
                this.nonRelevantFieldBeforeFirstGiven = true;
                return;
            }
            if (value.hasValue) {
                this.value = value;
            }
        }
    }

    setOmittedTail(_fd: IFormatDefinition): void {
        this.omittedTail = true;
    }
}

// Date
export class FirstValueCombinerDate extends FirstValueCombiner<VkDate> {
    getNonRelevantValue(): VkDate {
        return VkDate.NON_RELEVANT_DATE;
    }
    protected notGivenValue(): VkDate {
        return VkDate.NOT_GIVEN_DATE;
    }
    protected createValue(value: VkDate, notGiven: boolean, nonRelevant: boolean): VkDate {
        return new VkDate(value.date, notGiven, nonRelevant);
    }
}

// BigDecimal/Number
export class FirstValueCombinerNumber extends FirstValueCombiner<VkBigDecimal> {
    getNonRelevantValue(): VkBigDecimal {
        return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
    }
    protected notGivenValue(): VkBigDecimal {
        return VkBigDecimal.NICHT_ANGEGEBENE_ZAHL;
    }
    protected createValue(value: VkBigDecimal, notGiven: boolean, nonRelevant: boolean): VkBigDecimal {
        return new VkBigDecimal(value.getBigDecimalValue(), nonRelevant, notGiven, notGiven);
    }
}

// DateRange
export class FirstValueCombinerDateRange extends FirstValueCombiner<VkDateRange> {
    getNonRelevantValue(): VkDateRange {
        return VkDateRange.NON_RELEVANT_DATE_RANGE;
    }
    protected notGivenValue(): VkDateRange {
        return VkDateRange.NOT_GIVEN_DATE_RANGE;
    }
    protected createValue(value: VkDateRange, notGiven: boolean, nonRelevant: boolean): VkDateRange {
        return new VkDateRange(value.start, value.end, notGiven, nonRelevant);
    }
}

// String
export class FirstValueCombinerString extends FirstValueCombiner<VkString> {
    getNonRelevantValue(): VkString {
        return VkString.NICHT_PRUEF_REL_STRING;
    }
    protected notGivenValue(): VkString {
        return VkString.NICHT_ANGEGEBENER_STRING;
    }
    protected createValue(value: VkString, notGiven: boolean, nonRelevant: boolean): VkString {
        return new VkString(value.wert, notGiven, nonRelevant);
    }
}