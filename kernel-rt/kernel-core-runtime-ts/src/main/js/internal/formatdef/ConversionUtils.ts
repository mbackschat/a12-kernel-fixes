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

import type {
    BooleanConversionConfig,
    ConfirmConversionConfig,
    DataFormats,
    DateConversionConfig,
    DateRangeConversionConfig,
    NumberConversionConfig
} from "@com.mgmtp.a12.utils/utils-localization";

import {
    DateUtil
} from "@com.mgmtp.a12.utils/utils-localization/a12internal";

export class ConversionUtils {

    static createBooleanConversionConfig(
        conversionConfig: BooleanConversionConfig,
        trueValue: string,
        falseValue: string
    ): BooleanConversionConfig & DataFormats {
        return {
            type: conversionConfig.type,
            trueValue: trueValue,
            falseValue: falseValue,
            // everything else is not important for boolean
        } as BooleanConversionConfig & DataFormats;
    }

    static createConfirmConversionConfig(
        conversionConfig: ConfirmConversionConfig,
        trueValue: string,
    ): ConfirmConversionConfig & DataFormats {
        return {
            type: conversionConfig.type,
            trueValue: trueValue
            // everything else is not important for confirm
        } as ConfirmConversionConfig & DataFormats;
    }

    static createDateConversionConfig(
        conversionConfig: DateConversionConfig,
        baseYear: number,
        timeZone: string,
        format: string,
        dateFragmentOrdering: string,
        dateZeroOptional: boolean,
        dateTimeFormat: string,
        timeFormat: string,
        formatCharacters: {
            day: string,
            month: string,
            year: string
        }
    ): DateConversionConfig & DataFormats {
        return {
            type: conversionConfig.type,
            format: format,
            baseYear: baseYear,
            timeZone: timeZone,
            datePrecision: conversionConfig.datePrecision,
            dateFragmentOrdering: dateFragmentOrdering,
            dateSeparator: DateUtil.getDateSeparator(format),
            dateZeroOptional: dateZeroOptional,
            dateTimeFormat: dateTimeFormat,
            timeFormat: timeFormat,
            formatCharacters: formatCharacters
            // everything else is not important for date
        } as DateConversionConfig & DataFormats;
    }

    static createDateRangeConversionConfig(
        conversionConfig: DateRangeConversionConfig,
        baseYear: number,
        timeZone: string,
        format: string,
        dateFragmentOrdering: string,
        dateZeroOptional: boolean,
        formatCharacters: {
            day: string,
            month: string,
            year: string
        },
        dateRangeSeparator: string
    ): DateRangeConversionConfig & DataFormats {
        return {
            type: conversionConfig.type,
            format: format,
            baseYear: baseYear,
            timeZone: timeZone,
            interpretationOfYear: conversionConfig.interpretationOfYear,
            singleDate: conversionConfig.singleDate,
            dateFragmentOrdering: dateFragmentOrdering,
            dateSeparator: DateUtil.getDateSeparator(format),
            dateZeroOptional: dateZeroOptional,
            dateRangeSeparator: dateRangeSeparator,
            formatCharacters: formatCharacters
            // everything else is not important for date range
        } as DateRangeConversionConfig & DataFormats;
    }

    static createNumberConversionConfig(
        conversionConfig: NumberConversionConfig,
        decimalSeparator: string,
        decimalPlacesOptional: boolean,
        thousandsSeparator?: string
    ): NumberConversionConfig & DataFormats {
        return {
            type: conversionConfig.type,
            minFractionalDigits: conversionConfig.minFractionalDigits,
            leadingZerosAllowed: conversionConfig.leadingZerosAllowed,
            decimalSeparator: decimalSeparator,
            decimalPlacesOptional: decimalPlacesOptional,
            thousandsSeparator: thousandsSeparator,
            // everything else is not important for number
        } as NumberConversionConfig & DataFormats;
    }

} 