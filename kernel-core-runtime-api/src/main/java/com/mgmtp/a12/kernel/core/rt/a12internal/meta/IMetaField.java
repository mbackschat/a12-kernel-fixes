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
package com.mgmtp.a12.kernel.core.rt.a12internal.meta;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Interface to metadata of a field.
 * <p>
 * All methods are thread-safe. All methods only provide read-only access.
 * <p>
 * Information can be retrieved using <code>{@link IMetaEntity#getValue(String, Object...)}</code>.
 * The accepted meta keys and documentation of their behavior can be found in {@link IMetaKeys} (all keys with prefix "FIELD_",
 * except for those with prefix "FIELD_TYPE_").
 */
@OnlyForUsage
public interface IMetaField extends IMetaEntity {

	/**
	 * To set the conversion to non-lenient (see {@link IMetaField#convertWithOnlyBasicTypeCheck})
	 */
	String NON_LENIENT_CONVERSION = "non-lenient";

	/**
	 * Converts the given value from the UI representation to the internal representation or vice
	 * versa.
	 * <p>
	 * <b>Notes:</b>
	 * <ul>
	 * <li>The following assurances are given:
	 * <ul>
	 * <li><b>field type {@link IMetaKeys#FIELD_TYPE_YES}</b>:
	 * <ul>
	 * <li>A value has been assigned to the key {@link PresentationInformation#TRUE_VALUE}.</li>
	 * </ul>
	 * </li>
	 * <li><b>field type {@link IMetaKeys#FIELD_TYPE_YESNO}</b>:
	 * <ul>
	 * <li>A value has been assigned to the key {@link PresentationInformation#TRUE_VALUE}.</li>
	 * <li>A value has been assigned to the key {@link PresentationInformation#FALSE_VALUE}.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * <li>Validation always takes place before conversion.</li>
	 * </ul>
	 * </p>
	 *
	 * @param data
	 * 		the value to be converted.
	 * @param representationInfo
	 * 		defines the external representation. If this value is
	 * 		<code>null</code>,
	 * 		no conversion is performed.
	 * @param fromInternal
	 * 		this value specifies whether the given data is to be transformed
	 * 		from internal to
	 * 		the external representation or vice versa.
	 * @param parameter
	 * 		additional parameters, if necessary.
	 * 		<ul>
	 * 		<li>An implementation of {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory} can be specified where you
	 * 		can create and configure your {@link ICustomFieldValidator} objects.
	 * 		</li>
	 * 		<li>An instance of Map can be provided to pass additional information to your implementation of {@link ICustomFieldValidator}.
	 * 		This must be used if some configuration data is not static (might change during runtime).
	 * 		The keys and values of the map must match the requested data of your {@link ICustomFieldValidator} implementation.
	 * 		This feature is deprecated and will be removed in the next breaking release.
	 * 		Please pass a {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory}	parameter instead.
	 * 		<li>Additionally, a {@link Locale} can be specified as
	 * 		language. This will be used for the conversion to language specific UI
	 * 		representations like enumeration types. The validation which is performed before
	 * 		the conversion takes place, will also use this parameter for all types of fields
	 * 		and the generated error messages.
	 * 		If a {@link Locale} is given which is not contained in the list of supported
	 * 		languages (returned by {@link IMetaModel} for the key "languages"), an {@link IllegalArgumentException} will be thrown.
	 * 		If no {@link Locale} is given, {@link Locale#GERMANY} will be used.</li>
	 * 		<li>On top, an {@link IResult}-parameter can be specified, which takes potential
	 * 		validation errors regarding the internal validation of the value. If such a
	 * 		parameter is specified, no {@link IllegalArgumentException} will be
	 * 		thrown for erroneous values.</li>
	 * 		</ul>
	 * @return the converted value.
	 *
	 * @throws IllegalArgumentException
	 * 		if <code>data</code> doesn't contain a valid value for the
	 * 		field. In this case, the message of the exception will contain the error text of
	 * 		the formal error. If the parameters contain a {@link Locale}, the error text will
	 * 		be in the specified language, otherwise it will be in German.
	 * 		The exception is only thrown if no {@link IResult}-parameter was specified in the
	 * 		parameters.
	 */
	String convert(String data, PresentationInformation representationInfo,
			boolean fromInternal, Object... parameter);

	/**
	 * Convert the given value to a basic type, only checking that the given type can be parsed
	 * correctly and unambiguously.<p/>
	 * Note: the returned value will be suitable for {@code IDocument} fields; for {@code DocumentV2} fields, it must first be
	 * converted via {@code com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion#basicTypeV1ToV2}. This conversion is
	 * necessary
	 * because of the different representation of dates, times and date ranges in V2 values (The conversion cannot be done
	 * automatically in IMetaField, because the conversion logic and {@code InstantRange} type are currently not available in
	 * the core-runtime api and core-runtime, but only in the "md" API of kernel.)
	 *
	 * @param value
	 * 		the value to be converted.
	 * @param presentationInformation
	 * 		defines the external representation. If this value is
	 * 		<code>null</code>,
	 * 		no conversion is performed.
	 * @param parameters
	 * 		additional parameters, if necessary.
	 * 		<ul>
	 * 		<li>An implementation of {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory} can be specified where you
	 * 		can create and configure your {@link ICustomFieldValidator} objects.
	 * 		</li>
	 * 		<li>An instance of Map can be provided to pass additional information to your implementation of {@link ICustomFieldValidator}.
	 * 		This must be used if some configuration data is not static (might change during runtime).
	 * 		The keys and values of the map must match the requested data of your {@link ICustomFieldValidator} implementation.
	 * 		This feature is deprecated and will be removed in the next breaking release.
	 * 		Please pass a {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory}	parameter instead.
	 * 		</li>
	 * 		<li>Additionally, a {@link Locale} can be specified as
	 * 		language. This will be used for the conversion to language specific UI
	 * 		representations like enumeration types. The validation which is performed before
	 * 		the conversion takes place, will also use this parameter for all types of fields
	 * 		and the generated error messages.
	 * 		If a {@link Locale} is given which is not contained in the list of supported
	 * 		languages (returned by {@link IMetaModel} for the key "languages"), an {@link IllegalArgumentException} will be thrown.
	 * 		If no {@link Locale} is given, {@link Locale#GERMANY} will be used.</li>
	 * 		<li>The string constant {@link IMetaField#NON_LENIENT_CONVERSION} can be also
	 * 		passed. If the string was passed, wrong placed thousands
	 * 		separators end up in a validation error and no conversion takes place. Otherwise,
	 * 		the conversion will be lenient: wrong placed thousands separators will be ignored
	 * 		and
	 * 		the conversion takes places.
	 * 		<b>Restriction:</b> this parameter only affects the conversion
	 * 		of <b>number fields</b>.</li>
	 * 		</ul>
	 * @param result
	 * 		will contain error messages, if they occur.
	 * @return the converted value. The field type to value type conversion will be:
	 * 		<dl>
	 * 		<dt>string, enumeration, or custom field</dt>
	 * 		<dd>{@link String}</dd>
	 * 		<dt>number</dt>
	 * 		<dd>{@link BigDecimal} or {@link String} when the value contains at least one
	 * 		leading zero and such zeros are allowed</dd>
	 * 		<dt>date</dt>
	 * 		<dd>{@link Date} (time zone of the model will be considered) or {@link String} for
	 * 		partially known dates; if the value contains
	 * 		unknown date parts, the string containing the value in internal date format will be
	 * 		returned</li>
	 * 		<dt>date fragment, dateTime, time</dt>
	 * 		<dd>{@link Date} (time zone of the model will be considered)</dd>
	 * 		<dt>date range</dt>
	 * 		<dd>A {@link Date} (time zone of the model will be considered) array of size 2</dd>
	 * 		<dt>confirm or boolean</dt>
	 * 		<dd>{@link Boolean} (empty values will be converted to null)</dd>
	 * 		</dl>
	 *
	 * @throws IllegalArgumentException
	 * 		if additional information is required for the conversion of the value and the
	 * 		needed parameters are not found.
	 * 		Or if an unsupported locale was given.
	 */
	Object convertWithOnlyBasicTypeCheck(String value,
			PresentationInformation presentationInformation,
			@NonNull IResult result,
			Object... parameters);

	/**
	 * Convert an object representation to a string representation. For this type of conversion,
	 * only checks necessary to ensure convertibility are performed. E.g. string patterns or correct
	 * decimal places are not checked, as long as the given value can be represented correctly as
	 * base type.<p/>
	 * Note: the value object is expected to have a type that matches the IDocument field types. If the value is a DocumentV2 field
	 * value, it must first be converted via
	 * {@code com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion#basicTypeV2ToV1}.
	 * See also the similar note in {@link #convertWithOnlyBasicTypeCheck(String, PresentationInformation, IResult, Object...)}
	 *
	 * @param value
	 * 		the value as object.
	 * @param presentationInformation
	 * 		defines the external representation. If this value is
	 * 		<code>null</code>,
	 * 		the internal string representation will be generated.
	 * @param parameters
	 * 		additional parameters, if necessary.
	 * 		<ul>
	 * 		<li>An implementation of {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory} can be specified where you
	 * 		can create and configure your {@link ICustomFieldValidator} objects.
	 * 		</li>
	 * 		<li>An instance of Map can be provided to pass additional information to your implementation of {@link ICustomFieldValidator}.
	 * 		This must be used if some configuration data is not static (might change during runtime).
	 * 		The keys and values of the map must match the requested data of your {@link ICustomFieldValidator} implementation.
	 * 		This feature is deprecated and will be removed in the next breaking release.
	 * 		Please pass a {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory}	parameter instead.
	 * 		</li>
	 * 		<li>Additionally, a {@link Locale} can be specified as
	 * 		language. This will be used for the conversion to language specific UI
	 * 		representations like enumeration types. The validation which is performed before
	 * 		the conversion takes place, will also use this parameter for all types of fields
	 * 		and the generated error messages.
	 * 		If a {@link Locale} is given which is not contained in the list of supported
	 * 		languages (returned by {@link IMetaModel} for the key "languages"), an {@link IllegalArgumentException} will be thrown.
	 * 		If no {@link Locale} is given, {@link Locale#GERMANY} will be used.
	 * 		</ul>
	 * @param result
	 * 		will contain error messages if the conversion creates some.
	 * @return the string representation specified by <code>presentationInformation</code> if
	 * 		present,
	 * 		otherwise the internal string representation. For date ranges, the string
	 * 		representation of each date part concatenated with the range separator is returned.
	 *
	 * @throws IllegalArgumentException,
	 * 		if the given value does not correspond with the field type
	 * 		(see
	 *        {@link #convertWithOnlyBasicTypeCheck(String, PresentationInformation, IResult, Object...)}).
	 */
	String convertFromBasicType(Object value,
			PresentationInformation presentationInformation,
			@NonNull IResult result,
			Object... parameters);
}