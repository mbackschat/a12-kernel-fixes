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

import type { Localizable } from "@com.mgmtp.a12.utils/utils-localization";

import type { ICustomFieldTypeCheckError } from "./ICustomFieldTypeCheckError.js";
import type { ICustomFieldTypeConversionResult } from "./ICustomFieldTypeConversionResult.js";
import type { ICustomFieldTypeValidationParam } from "./ICustomFieldTypeValidationParam.js";

/**
 * Common interface for custom field types.
 * 
 * Custom Field Types can be used for a project-specific field validation that cannot be adequately checked using regular
 * expressions, etc., because, for example, a check digit procedure is used. Examples are credit card number, IBAN, BIC, etc.
 * 
 * **Attention:** The kernel validation and thus especially custom field types (as part of it) are designed exclusively
 * for the validation of values of a document. Further validations should be implemented at the appropriate extension point
 * (e.g. in Form Engine). The interface is intended for fast, synchronous validations. More time-consuming validations or
 * those that require access to third-party systems should not be placed here.
 */
export interface ICustomFieldValidator {
	/**
	 * Validates the value and returns a result with all the information required to create an error.
	 *
	 * @param value the value to be validated.
	 * @param valParam Wrapper for further parameters. Please note that the implementation of this method should not use
	 * 		the values given by {@link ICustomFieldTypeValidationParam#getMinLength} and
	 * {@link ICustomFieldTypeValidationParam#getMaxLength} for custom field types with constant min. and max. length.
	 * 		For details see the javadoc of those methods.
	 * @param isDisplayValue `false` specifies `value` is in internal format. If `true` and the internal and display format
	 * 		are not the same for the implemented custom field type, `value` has to be converted from display format to
	 * 		internal format before the actual validation can be performed.
	 * @return `undefined` if the value is valid, otherwise an instance of {@link ICustomFieldTypeCheckError} describing the
	 *      error ({@link customFieldTypeCheckError} can be used to create an instance).
	 */
	validate(
		value: string,
		valParam: ICustomFieldTypeValidationParam,
		isDisplayValue: boolean // A12K-2826: once this is no longer used, the isDisplayValue parameter should be removed
	): ICustomFieldTypeCheckError | undefined;

	/**
	 * Converts a value from presentation format to the internal format. Use {@link customFieldTypeConversionResult} for a default implementation.
	 *
	 * @param displayValue value in presentation format
	 * @return the conversion result with {@link ICustomFieldTypeConversionResult.getConvertedValue} in internal format
	 */
	// A12K-2826: once this is no longer used, this method should be deprecated and later removed
	convertDisplay2Internal(displayValue: string): ICustomFieldTypeConversionResult;

	/**
	 * Converts an internal value to its presentation format. Use {@link customFieldTypeConversionResult} for a default implementation.
	 *
	 * @param internalValue the value in internal format
	 * @return the conversion result with {@link ICustomFieldTypeConversionResult.getConvertedValue} in presentation format
	 */
	// A12K-2826: once this is no longer used, this method should be deprecated and later removed
	convertInternal2Display(internalValue: string): ICustomFieldTypeConversionResult;
}

/**
 * @param errorKey the error key
 * @param errorMessage the error message
 * @returns the custom field type check error information
 */
export function customFieldTypeCheckError(errorKey: string, errorMessage: Localizable[]): ICustomFieldTypeCheckError {
	return {
		getErrorMessage: () => errorMessage,
		getErrorKey: () => errorKey,
	};
}

/**
 * @param convertedValue the result of the conversion. If there is an `errorMessage` or no conversion is necessary this can be the unconverted value.
 * @param errorMessage the error message if the conversion was not successful.
 * @returns the conversion result
 */
export function customFieldTypeConversionResult(convertedValue: string, errorMessage?: string): ICustomFieldTypeConversionResult {
	return {
		getConvertedValue: () => convertedValue,
		getErrorMessage: () => errorMessage,
	};
}
