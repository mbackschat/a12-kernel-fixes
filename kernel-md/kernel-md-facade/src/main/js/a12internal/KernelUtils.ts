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
import type { DataFormatPlaceholder, Localizable, LocalizableArgs } from "@com.mgmtp.a12.utils/utils-localization";

import {
	DateUtil,
	PartiallyKnownDatesUtil
} from "@com.mgmtp.a12.utils/utils-localization/lib/main/conversion/a12internal/index.js";

import { RuntimeFormalErrorEnum } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { LocalizableUtil } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import { DocumentServiceImpl } from "../internal/DocumentServiceImpl.js";

/**
 * Utility class containing low level functionality provided by kernel.
 * This class may only be used by other A12 products (see 'a12internal' in the path).
 * Breaking changes here will be only agreed with affected A12 products and, with
 * their OK, may be contained in a non-breaking kernel release.
 */

/**
 * Creates a {@link !Date Date}-object for partially known date strings as follows:
 * <ol>
 * <li>The given string value is checked syntactically against the pattern created from the
 * supported date format. If the value does not match the pattern, an {@link !Error Error} is thrown.</li>
 * <li>Unknown date parts are replaced with the corresponding values from '1970-01-01', e.g.
 * <ul>
 * <li>2020-02-00 -&gt; 2020-02-01</li>
 * <li>2020-00-00 -&gt; 2020-01-01</li>
 * <li>0000-00-00 -&gt; 1970-01-01</li>
 * </ul>
 * <li>Strings representing a known date will be converted to the corresponding
 * {@link !Date Date}-object.</li>
 * <li>Please note that the given string is not validated.</li>
 * </ol>
 *
 * @param partiallyKnownDate string from which a {@link !Date Date}-object should be created.
 * @param timeZone the time zone the date should be interpreted in.
 * @return approximated {@link !Date Date}-object as described above.
 * @throws Error
 *             if the value cannot be parsed to a {@link !Date Date}-object (see also method
 *             description).
 */
export function getApproximatedDate(partiallyKnownDate: string, timeZone: string): Date {
	const format: string = DocumentServiceImpl.DEFAULT_DATE_FORMAT;
	return PartiallyKnownDatesUtil.parseApproximatedDate(partiallyKnownDate, format, timeZone);
}

/**
 * Tries to create a {@link !Date Date}-object as follows:
 * <ol>
 *     <li>The given <code>value</code> is checked syntactically against the pattern created from the given <code>dateFormat</code>. If
 *     the value does not match the pattern, an error is created.</li>
 *     <li>If partially known dates are allowed and a date part is unknown, the original value is returned.</li>
 *     <li>Otherwise the {@link !Date Date}-object created from the given value is returned.</li>
 * </ol>
 * If there is an errorHandler given, an error is given to the errorHandler as a {@link Localizable}. Otherwise an {@link !Error Error} is thrown.
 * <b>ATTENTION:</b>If you are using the error handler, you have to account for the following problem:
 * When the localizable error will be localized with a localization function, the DataFormat used in that localization
 * function will be used to determine the date format displayed. If the formats specified in these DataFormats does not 
 * match your given dateFormat, you will get a misleading error message specifying the wrong expected date format.
 *
 * <b>Note:</b> Due to the syntax check, only the following date format fragments are allowed for a <code>dateFormat</code>:
 * <ol>
 *     <li><code>yyyy</code> for the year</li>
 *     <li><code>MM</code> for the month</li>
 *     <li><code>dd</code> for the day</li>
 *     <li><code>'T'</code> as separator between date and time</li>
 *     <li><code>HH</code> or <code>hh</code> for the hours</li>
 *     <li><code>mm</code> for the minutes</li>
 *     <li><code>ss</code> for the seconds</li>
 *     <li><code>a</code> for AM/PM marker</li>
 * </ol>
 *
 * @param value
 * 		string from which a {@link !Date Date}-object should be created. May not be <code>null</code> or empty.
 * @param dateFormat
 * 		format of the date, see method description, specially the <b>Note</b>. May not be <code>null</code> or empty.
 * @param baseYear
 * 		the base year which could be taken from the document model (see {@link DocumentModelInfo.baseYear}). This year
 * 		is relevant for <code>dateFormat</code>s containing <code>MM</code> and <code>dd</code> but not <code>yyyy</code>, in order to determine
 * 		if the February 29th is a valid input or not.
 * 		If no base year is defined in the document model, a default value should be given (in A12 kernel the year <code>2000</code>
 * 		is commonly used). May not be 0.
 * @param arePartiallyKnownDatesAllowed
 * 		<code>true</code> if partially known dates are allowed, e.g. <code>2020-00-00</code>, otherwise <code>false</code>.
 * @param timeZone
 *      the time zone which should be considered while creating the {@link !Date Date}-object.  This could be taken from the
 * 		document model (see {@link DocumentModelConfig#timeZone}). May not be <code>null</code>.
 * @param errorHandler
 * 		accepts a localizable error if there was a parser error. If no errorHandler is given, an Error will be thrown instead.
 *      ATTENTION: When you localize the localizable error with a localization function, the DataFormat used in that localization
 *      function will be used to determine the shown date format. Therefore, if the formats specified in DataFormats does not 
 *      correspond to your given dateFormat, you will get a misleading errormessage specifying the wrong expected date format.
 * @return see method description.
 *
 * @throws Error
 * 		if the base year is not valid or the value cannot be parsed to a {@link !Date Date}-object and no errorHandler was given. (see also method description).
 */
export function parseDate(
	value: string,
	dateFormat: string,
	baseYear: number,
	arePartiallyKnownDatesAllowed: boolean,
	timeZone: string,
	errorHandler?: (l: Localizable[]) => void
): Date | string | undefined {
	if (baseYear === 0) {
		// compared to java, we have to do this check here to have a similar behavior
		throw new Error("The base year 0 is not supported.");
	}
	try {
		// the logic of this method must be as similar as possible to FormatDefinitionDatum.convertToBasicType
		return DateUtil.createDateIfPossible(value, dateFormat, baseYear, arePartiallyKnownDatesAllowed, timeZone);
	} catch (e) {
		if (!errorHandler) {
			throw e;
		}
		errorHandler(createLocalizableError(dateFormat));
		return undefined;
	}
}

/**
 * @param value
 * 		{@link String} or {@link !Date Date} which should be processed. May not be <code>null</code>.
 * @param dateFormat
 * 		format of the date to which the given date should be formatted, see the <b>Note</b> in {@link parseDate}. May not be <code>null</code> or empty.
 * @param timeZone
 * 		the time zone which was considered while creating the given {@link !Date Date}-object. May not be <code>null</code>.
 * @return string representation of the given <code>value</code>.
 *
 * @throws Error
 * 		if the given <code>value</code> is not a {@link !Date Date} or it is a {@link String} without unknown date parts.
 */
export function formatDate(
	value: string | Date,
	dateFormat: string,
	timeZone: string
): string {
	// the logic of this method must be as similar as possible to FormatDefinitionDatum.convertFromBasicType
	return DateUtil.convertDateToString(value, dateFormat, timeZone);
}

function createLocalizableError(dateFormat: string): Localizable[] {
	const dateFormatPlaceholder: DataFormatPlaceholder = {
		type: "dataFormat",
		value: dateFormat,
		properties: {
			type: "date"
		}
	}
	const args: LocalizableArgs = {
		dateFormat: dateFormatPlaceholder
	};
	return [LocalizableUtil.createLocalizableFormalError(
		RuntimeFormalErrorEnum.DATUM_FORMAT,
		args,
	)];
}

