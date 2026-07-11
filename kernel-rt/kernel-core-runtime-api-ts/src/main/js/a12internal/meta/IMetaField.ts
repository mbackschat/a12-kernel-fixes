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

import type { DataFormats, SupportedType } from "@com.mgmtp.a12.utils/utils-localization";

import type { ICustomFieldTypeFactory } from "../../custom/ICustomFieldTypeFactory.js";

import type { IResult } from "../validation/IResult.js";

import type { IMetaEntity } from "./IMetaEntity.js";

/**
 * Interface to meta data of a field.
 * <p>
 * All methods are thread-safe. All methods only provide read-only access.
 * </p>
 * <p>
 * Following information can be retrieved using
 * {@link IMetaEntity.getValue}
 * </p>
 * <p>
 * <ul>
 * <li>{@link IMetaKeys.FIELD_SHORT_NAME}: short name of the field as a string.</li>
 * <li>{@link IMetaKeys.FIELD_FULLNAME}: full name a field</li>
 * <li>{@link IMetaKeys.FIELD_MAX_LENGTH}: maximum length as a number, it always
 * returns some value. If no value is specified <code>999</code> is returned as this is the default.
 * For enumerations the length of the longest value is returned. For Yes and Yes-No fields the
 * length of the longest value is returned. Optionally, a <code>DataFormats</code>
 * object may be passed as additional parameter: in that case the length is considered as in the
 * presentation.
 * If the constant <code>LANGUAGE_DEFINED</code> is used as a key within the presentation
 * information, a locale must be passed to clarify
 * the language. Otherwise an error is thrown.<br>
 * If the field is a string and the property 'noValueValidation' is set to <code>true</code>, the
 * max. length is given in MB.</li>
 * <li>{@link IMetaKeys.FIELD_MIN_LENGTH}: minimum length as a number, it always
 * returns some value. If no
 * value is specified <code>1</code> is returned as this is the default. For enumerations the
 * length of the shortest value is returned. For Yes and Yes-No fields the
 * length of the shortest value is returned. For usage of <code>DataFormats</code>
 * as optional additional parameter see {@link IMetaKeys.FIELD_MAX_LENGTH}
 * </li>
 * <li>{@link IMetaKeys.FIELD_FIELD_TYPE}: field type as a string, one of:
 * <ul>
 * <li>{@link IMetaKeys.FIELD_TYPE_STRING}</li>
 * <li>{@link IMetaKeys.FIELD_TYPE_NUMBER}</li>
 * <li>{@link IMetaKeys.FIELD_TYPE_DATE}</li>
 * <li>{@link IMetaKeys.FIELD_TYPE_MONEY_2}</li>
 * <li>{@link IMetaKeys.FIELD_TYPE_MONEY_0}</li>
 * <li>{@link IMetaKeys.FIELD_TYPE_YES}</li>
 * <li>{@link IMetaKeys.FIELD_TYPE_YESNO}</li>
 * <li>{@link IMetaKeys.FIELD_TYPE_ENUMERATION}</li>
 * <li>{@link IMetaKeys.FIELD_TYPE_EXT_ENUMERATION}</li>
 * </ul>
 * or a project specific custom field type.
 * </li>
 * <li>{@link IMetaKeys.FIELD_PATTERN}: pattern as a string, for
 * fields with fieldtype {@link IMetaKeys.FIELD_TYPE_STRING} and a predefined a pattern for all
 * other cases <code>null</code>.</li>
 * <li>{@link IMetaKeys.FIELD_ERROR_MESSAGE}: error message as a string for fields with
 * fieldtype {@link IMetaKeys.FIELD_TYPE_STRING} and a predefined pattern or
 * {@link IMetaKeys.FIELD_TYPE_ENUMERATION}
 * for all other cases <code>null</code>. The error message is returned as
 * it was defined in the model, i.e. error text parameters are not replaced. A locale passed as
 * a second parameter is interpreted as a language. If a locale is passed that is not
 * supported (see {@link IMetaModel} and key {@link IMetaKeys.MODEL_LANGUAGES}) an
 * Error is thrown. If no additional parameter is passed, german is chosen as a default. If for
 * {@link IMetaKeys.FIELD_TYPE_ENUMERATION} no error message has been defined in the model,
 * depending on the locale passed, a default error message is returned.</li>
 * <li>{@link IMetaKeys.FIELD_WORD_WRAP}: word wrap as a Boolean, only for
 * fields of field type {@link IMetaKeys.FIELD_TYPE_STRING}, all other cases <code>null</code>.</li>
 * <li>{@link IMetaKeys.FIELD_SIGN}: sign as a Boolean only for
 * number, otherwise it returns <code>null</code>.</li>
 * <li>{@link IMetaKeys.FIELD_DECIMAL_PLACES}: Returns the minimum number of decimal
 * places for numbers as an Integer, otherwise it returns <code>null</code>.</li>
 * <li>{@link IMetaKeys.FIELD_MAX_DECIMAL_PLACES}: Returns the maximum number of
 * decimal places for numbers as an Integer, otherwise it returns <code>null</code>.</li>
 * <li>{@link IMetaKeys.FIELD_INTEGER_DIGIT}: number of places before decimal separator
 * for numbers as an Integer if specified in the model, otherwise it returns <code>null</code>. If
 * the number of places before the decimal separator is not defined, <code>null</code> is returned
 * even for numbers.</li>
 * <li>{@link IMetaKeys.FIELD_ZERO_ALLOWED}: zero allowed as a Boolean, only applicable
 * for numbers, otherwise it returns <code>null</code>.</li>
 * <li>{@link IMetaKeys.FIELD_VALUE_TRUE}: returns the value that expresses <i>true</i>
 * in the
 * internal representation, only for fields with field type YES or YES/NO, all other cases
 * <code>null</code>.</li>
 * <li>{@link IMetaKeys.FIELD_VALUE_FALSE}: returns the value that expresses
 * <i>false</i> in the
 * internal representation, only for fields with field type YES/NO, all other cases
 * <code>null</code>.</li>
 * <li>{@link IMetaKeys.FIELD_FORMAT}: returns the date format as a String, only
 * applicable to dates, all other cases <code>null</code>.</li>
 * <li>{@link IMetaKeys.FIELD_MANDATORY_FIELD}: returns whether the field is a
 * mandatory field as a Boolean. The information is extracted from rules including rules generated
 * for fields marked as mandatory.</li>
 * <li>{@link IMetaKeys.FIELD_MANDATORY_GLOBAL_OR_IN_SURROUNDING_REPEATABLE_GROUP}:
 * returns whether the field is globally mandatory or mandatory regarding a surrounding repeatable
 * group as a boolean. The information is extracted from rules including rules generated for fields marked as
 * mandatory.</li>
 * <li>{@link IMetaKeys.FIELD_ROOT_GROUP_MANDATORY_FIELD}: returns whether the
 * field is a root group mandatory field as a Boolean. The information is extracted from rules
 * including rules generated for fields marked as mandatory.</li>
 * <li>{@link IMetaKeys.FIELD_DECIMAL_SEPARATOR}: The symbol used as decimal separator
 * in this model type as a String. Is returned even if the field is not a number.</li>
 * <li><code>{@link IMetaKeys#FIELD_ENUMERATION_VALUES}</code>: If the field is an Enumeration, a String with hint list or an extensible enumeration the
 * following will be returned:
 * <ul>
 * <li>A {@link Locale} can be passed as an additional parameter. The returned value is a
 * {@link List} of {@link String}s containing the enumeration values. If a locale is passed that is not part of the List of
 * supported languages (i.e. the List returned when calling the keyword
 * {@link IMetaKeys.MODEL_LANGUAGES} in {@link IMetaModel}), an Error is
 * thrown. The order in which the values in the list are returned corresponds to the order which was
 * specified in the model for the specific language.</li>
 * <li>ExtMP fallback: If no {@link Locale} is passed, a {@link List} of {@link String}s is
 * returned containing the enumeration values defined for {@link Locale#GERMANY}. The list is empty if this
 * locale is not supported in the model.</li>
 * </ul>
 * In all other cases an empty list is returned.</li>
 * <li>{@link IMetaKeys.FIELD_ENUMERATION_UI_VALUES}: If the field is of type
 * enumeration and UI values have been defined the following will be returned:
 * <ul>
 * <li>A locale has to be passed as an additional parameter. The return value is a
 * list of Pairs of Strings containing the UI value as the first String and the
 * internal value as the second. If a locale is passed that is not part of the List of supported
 * languages (i.e. the List returned when calling the keyword {@link IMetaKeys.MODEL_LANGUAGES} in
 * {@link IMetaModel} an Error is thrown. The order in which the values in the list are returned
 * corresponds to the order which was specified in the model for the specific language.</li>
 * </ul>
 * In all other cases an empty List is returned.
 * </li>
 * <li>{@link IMetaKeys.FIELD_OPERANDS_SET}: returns the operands of the calculation
 * function as a Set of <code>{@link IIdentifier}</code>. If the field is not a calculated field,
 * the Set is empty. The set of operands is used to determine the required operands for the
 * calculation function.
 * The fields referenced in the precondition are also
 * returned in addition to the fields referenced in the respective calculation functions.
 * The rootgroups that are possibly referenced in rootgroup language constructions are ignored.
 * In this case, the set of operands corresponds to the fields referenced in all
 * calculation rules belonging to this field without the calculated field itself.
 * For the respective repeat level, the following is specified in the <code>{@link IIdentifier}</code>
 * <ul>
 * <li>the constant {@link IIdentifier.ALL_INDICES}: if a star operation was specified for this
 * repeat level and this identifier in the underlying rule condition.</li>
 * <li>the constant {@link IIdentifier.ITERATION}: if an iteration was specified for this
 * repeat level and this identifier in the underlying rule condition. To call the calculation
 * function,
 * this repeat level must always be replaced by a specific value. If, for example, a calculation for
 * a sum is offered for each child and each participant, the calculation function must specify for
 * which child and which participant the value is to be calculated.</li>
 * <li>the specific repeat level, if neither an iteration nor a star operation was specified for
 * this repeat level and this identifier.</li>
 * </ul>
 * </li>
 * <li>{@link IMetaKeys.FIELD_TARGET_SET_FOR_CALC}: if the field is the operand of one
 * (or more) calculation(s), all fields calculated by these calculations are returned as a Set of
 * {@link IIdentifier}. If the field is not an operand of a calculation, the
 * returned Set is empty.
 * For the specification of the particular retry level and the operand relation, see the description
 * of the key {@link IMetaKeys.FIELD_OPERANDS_SET}.
 * </li>
 * <li>{@link IMetaKeys.FIELD_ENUMERATION_CATEGORY_VALUES}: if the field is an
 * enumeration and categories are defined with their respective values, this key returns a
 * Map<string, Pair<string,string>[]>. Each entry of the map has a xml value
 * (key of the map entry) and a list of its respective category values per category name (value of
 * the map entry). If the field is not an enumeration or it does not define any categories, an empty
 * map will be returned.
 * </li>
 * <li>{@link IMetaKeys.FIELD_NO_VALUE_VALIDATION}: if the field is a string and the
 * property 'noValueValidation' is set to <code>true</code>, the key returns <code>true</code>,
 * otherwise <code>false</code>.
 * </li>
 * <li>{@link IMetaKeys.FIELD_S_VALUE}: returns the s value (if defined), otherwise the
 * empty string.
 * </li>
 * <li>{@link IMetaKeys.FIELD_EXPLICITLY_MANDATORY}: returns <code>true</code> if the field
 * was explicitly set as mandatory (not through rules, but through the mandatory check box), otherwise
 * <code>false</code>.
 * </li>
 * <li>{@link IMetaKeys.FIELD_DYNAMIC_ENUMERATION}: If the field is a dynamic
 * enumeration, the boolean <code>true</code> will be returned, otherwise
 * <code>false</code>.</li>
 * <li>{@link IMetaKeys.FIELD_SOURCE_FIELDS_FOR_DYNAMIC_ENUM}: if this field is a
 * dynamic enumeration field, the key returns the operand fields necessary to calculate the dynamic
 * enumeration values as a Set&lt;IIdentifier&gt;. As a parameter, an {@link IIdentifier} with
 * concrete indices is expected. This parameter specifies the repetition of the dynamic enumeration
 * field to be calculated. The name of the {@link IIdentifier} should coincide with the name of the
 * current IMetaField.</li>
 * <li>{@link IMetaKeys.FIELD_DYNAMIC_ENUM_VALUES}: With this key, the values of
 * dynamic enumerations are calculated. Three parameters have to be supplied in the given order:
 * <ol>
 * <li>An {@link IIdentifier} specifies the repetition of the dynamic enumeration field to be
 * calculated. The name of the {@link IIdentifier} should coincide with the name of the
 * current IMetaField.</li>
 * <li>An {@link IData} object, which contains the data for the operands (see key
 * {@link IMetaKeys.FIELD_SOURCE_FIELDS_FOR_DYNAMIC_ENUM}).</li>
 * <li>A {@link ValidationMode} object, which specifies the language of the UI values and the
 * interpretation of the data contained in parameter 2. The interpretation is analogous to the
 * validation or calculation.</li>
 * </ol>
 * As result a list of Pair&lt;String, String&gt; will be returned. The first value of the pair will
 * be the UI value, whereas the second value will be the XML value.
 * The sort order is specified by following criteria:
 * <ol>
 * <li>If the base field is of type enumeration and contains UI values, the order is specified by
 * the order of the values of the enumeration for the language given in the {@link ValidationMode}.
 * The UI value will be taken from the UI values of the base enumeration.</li>
 * <li>If the base field is an enumeration without UI values, the order is specified by the order of
 * the XML values of the base enumeration. The UI values will be the same as the XML values in this
 * case.</li>
 * <li>If the base field is not an enumeration, the order is specified by the order given in the
 * rule condition. Here, also the UI values will be the same as the XML values.</li>
 * </ol>
 * </li>
 * <li>{@link IMetaKeys.FIELD_INDEX_FIELD}: returns <code>true</code> if the field is an
 * index field, otherwise <code>false</code>.
 * </li>
 * <li>{@link IMetaKeys.FIELD_META_FIELD_DATE_OF_DATE_RANGE}: for fields of type date
 * range it returns an {@link IMetaField} which is capable of handling a single date, otherwise it
 * returns <code>null</code>.
 * </li>
 * <li>{@link IMetaKeys.FIELD_ANNOTATION}: with the parameter "key of annotation" of type string, it
 * returns:
 * <ul>
 *     <li><code>null</code>, if there is no annotation with the given key.</li>
 *     <li>Empty string, if the annotation key is present but no value is associated with it.</li>
 *     <li>Otherwise, the value associated with the given key.</li>
 * </ul>
 * </li>
 * <li><code>{@link IMetaKeys.FIELD_CUSTOM_ERROR_MESSAGE_FOR_REQUIRED}</code>: error messages for each language defined in the model (LocalizableDefaults)
 * for fields marked explicitly as required, otherwise <code>null</code>. The error message is returned as it was defined in the model,
 * i.e. error text parameters are not replaced.
 * </li>
 * </ul>
 * </p>
 */
export interface IMetaField extends IMetaEntity {

    /**
     * Convert the given value to a basic type, only checking that the given type can be parsed
     * correctly and unambiguously.
     *
     * @param value
     *            the value to be converted.
     * @param dataFormats
     *            defines the external representation. If this value is
     *            <code>null</code>,
     *            no conversion is performed.
     * @param customFieldTypeFactory
     *            required for the conversion of custom field types if required by `dataFormats` (either
     *            via {@link DataFormats.convertCustomFieldTypesAll} or {@link DataFormats.convertCustomFieldTypesList})
     * @param result
     *            will contain error messages, if they occur.
     *
     * @return the converted value. The field type to value type conversion will be:
     *         <ul>
     *         <li>string, enumeration, or custom field type: string</li>
     *         <li>number: number or string when the value contains at least one leading zero and such zeros are allowed</li>
     *         <li>date: Date (time zone of the model will be considered) or string for partially known dates; if the value contains
     *         unknown date parts, the string containing the value in internal date format will be
     *         returned</li>
     *         <li>date fragment, dateTime, time: Date (time zone of the model will be considered)</li>
     *         <li>date range: A Date (time zone of the model will be considered) array of size 2</li>
     *         <li>confirm or boolean: boolean (empty values will be converted to null)</li>
     *         </ul>
     *
     * @throws Error
     *             if additional information is required for the conversion of the value and the
     *             needed parameters are not found.
     *             Or if an unsupported locale was given.
     */
    convertWithOnlyBasicTypeCheck(
        value: string,
        // A12K-2645, depending on the presence of dataFormats, this method is also related to conversion between internal and external string representation
        // but I think we cannot remove it yet, because some projects still rely on it according to MGM Wiki Shortlink x/F7QoF
        //
        // Also, there are several uses in A12 as of ca 2024-04-09 according to livegrep with query:
        // ?q=convertWithOnlyBasicTypeCheck%5C(%7CconvertFromBasicType%5C(%20-repo%3Akernel&fold_case=auto&regex=true&context=true
        //
        // Anyway, it should be rather uncritical to keep this method for now, because it is a12internal since recently, therefore
        // any future removal would not actually count as "breaking", i.e. we are not necessarily tied to the once-a-year breaking releases
        // A12K-2826: do we still need this method, and do we still need the dataFormats parameter?
        dataFormats: DataFormats | null,
        result: IResult,
        customFieldTypeFactory?: ICustomFieldTypeFactory,
    ): SupportedType;

    /**
     * Convert an object representation to a string representation. For this type of conversion,
     * only checks necessary to ensure convertibility are performed. E.g. string patterns or correct
     * decimal places are not checked, as long as the given value can be represented correctly as
     * base type.
     *
     * @param value
     *            the value as object.
     * @param dataFormats
     *            defines the external representation. If this value is
     *            <code>null</code>,
     *            the internal string representation will be generated.
     * @param result
     *            will contain error messages if the conversion creates some.
     * @param customFieldTypeFactory
     *            required for the conversion of custom field types if required by `dataFormats` (either
     *            via {@link DataFormats.convertCustomFieldTypesAll} or {@link DataFormats.convertCustomFieldTypesList})
     *
     * @return the string representation specified by <code>dataFormats</code> if
     *         present, otherwise the internal string representation. For date ranges, the string
     *         representation of each date part concatenated with the range separator is returned.
     * @throws Error,
     *             if the given value does not correspond with the field type (see
     *             {@link convertWithOnlyBasicTypeCheck}).
     */
    convertFromBasicType(
        value: SupportedType,
        // A12K-2645, depending on the presence of dataFormats, this method is also related to conversion between internal and external string representation
        // but I think we cannot remove it yet, because some projects still rely on it according to MGM Wiki Shortlink x/F7QoF
        //
        // Also, there are several uses in A12 as of ca 2024-04-09 according to livegrep with query:
        // ?q=convertWithOnlyBasicTypeCheck%5C(%7CconvertFromBasicType%5C(%20-repo%3Akernel&fold_case=auto&regex=true&context=true
        //
        // Anyway, it should be rather uncritical to keep this method for now, because it is a12internal since recently, therefore
        // any future removal would not actually count as "breaking", i.e. we are not necessarily tied to the once-a-year breaking releases
        // A12K-2826: do we still need this method, and do we still need the dataFormats parameter?
        dataFormats: DataFormats | null,
        result: IResult,
        customFieldTypeFactory?: ICustomFieldTypeFactory,
    ): string | null;

    hashCode(): number;

    equals(obj: this): boolean;
}