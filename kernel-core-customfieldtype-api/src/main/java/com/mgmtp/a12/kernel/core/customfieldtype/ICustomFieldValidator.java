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
package com.mgmtp.a12.kernel.core.customfieldtype;

import java.util.Locale;
import java.util.Optional;

import lombok.NonNull;

/**
 * Common interface for custom field types.<br>
 * Custom Field Types can be used for a project-specific field validation that cannot be adequately checked using regular
 * expressions, etc., because, for example, a check digit procedure is used. Examples are credit card number, IBAN, BIC, etc.
 * <p>
 * <b>Attention: </b>The kernel validation and thus especially custom field types (as part of it) are designed exclusively
 * for the validation of values of a document. Further validations should be implemented at the appropriate extension point
 * (e.g. in Data Services).
 */
public interface ICustomFieldValidator {

	/**
	 * Validates the value and returns a result with all the information required to create an
	 * error.
	 *
	 * @param value
	 * 		the value to be validated.
	 * @param valParam
	 * 		Wrapper for further parameters, such as {@link Locale}.
	 * 		Please note that the implementation of this method should not use the values given
	 * 		by {@link ICustomFieldTypeValidationParam#getMinLength()} and
	 *        {@link ICustomFieldTypeValidationParam#getMaxLength()} for custom field types with
	 * 		constant min. and max. length. For details see the javadoc of those methods.
	 * @param isDisplayValue
	 * 		The user of the validation/computation API can use the class
	 * 		'PresentationInformation' to specify if conversion should be executed for certain
	 * 		custom field types (see key 'CONVERT_CUSTOM_FIELD_TYPES_ALL' or
	 * 		'CONVERT_CUSTOM_FIELD_TYPES_LIST' in javadoc of 'PresentationInformation'). This
	 * 		should be set if differing formats of internal and
	 * 		display value exist in this application (e.g. it is possible, that you just
	 * 		have differing values on client-side, but on server-side you just have internal
	 * 		values). If nothing is specified in 'PresentationInformation' for this custom
	 * 		field type, this parameter will be {@code false}.<br>
	 * 		If {@code true} the parameter {@code value} has to be converted from display
	 * 		format to internal format by using {@link #convertDisplay2Internal(String)}
	 * 		before the actual validation can be performed.
	 * @return If an error occurred an instance of {@link ICustomFieldTypeCheckError} describing the
	 * 		error, otherwise empty.
	 */
	@NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String value,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	);

	/**
	 * Converts a value from display format to the internal format.
	 * For custom field types, the conversion is only necessary in rare cases (e.g. IBAN with blanks vs. without blanks).
	 * If there are no two different formats, the given value is returned.
	 *
	 * @param displayValue
	 * 		Must not be {@code null}.
	 * @return {@link ICustomFieldTypeConversionResult} containing the following data respectively:
	 * 		<ul>
	 * 		<li>If this custom field type supports only one format (no different display format),
	 * 		it contains the given value and <code>{@link Optional#empty()}</code>.</li>
	 * 		<li>If this custom field type supports differing formats for internal and display
	 * 		value:</li>
	 * 		<ul>
	 * 		<li>if the value can be converted, it contains the converted value and
	 * 		<code>{@link Optional#empty()}.</code></li>
	 * 		<li>if the value cannot be converted, it contains the given value and the resp. error
	 * 		message.</li>
	 * 		</ul>
	 * 		</ul>
	 * 		Never returns {@code null}.
	 */
	default @NonNull ICustomFieldTypeConversionResult convertDisplay2Internal(@NonNull String displayValue) {
		// this custom field type has no differing display format
		return noConversionNeededResult(displayValue);
	}

	/**
	 * Converts an internal value to its display format.
	 * For custom field types, the conversion is only necessary in rare cases (e.g. IBAN with blanks vs. without blanks).
	 * If there are no two different formats, the given value is returned.
	 *
	 * @param internalValue
	 * 		Must not be {@code null}.
	 * @return {@link ICustomFieldTypeConversionResult} containing the following data respectively:
	 * 		<ul>
	 * 		<li>If this custom field type supports only one format (no different display format),
	 * 		it contains the given value and <code>{@link Optional#empty()}</code>.</li>
	 * 		<li>If this custom field type supports differing formats for internal and display
	 * 		value:</li>
	 * 		<ul>
	 * 		<li>if the value can be converted, it contains the converted value and
	 * 		<code>{@link Optional#empty()}.</code></li>
	 * 		<li>if the value cannot be converted, it contains the given value and the resp. error
	 * 		message.</li>
	 * 		</ul>
	 * 		</ul>
	 * 		Never returns {@code null}.
	 */
	default @NonNull ICustomFieldTypeConversionResult convertInternal2Display(@NonNull String internalValue) {
		// this custom field type has no differing display format
		return noConversionNeededResult(internalValue);
	}

	private static ICustomFieldTypeConversionResult noConversionNeededResult(String value) {
		return new ICustomFieldTypeConversionResult() {
			@Override
			public @NonNull String getConvertedValue() {
				return value;
			}

			@Override
			public @NonNull Optional<String> getErrorMessage() {
				return Optional.empty();
			}
		};
	}
}
