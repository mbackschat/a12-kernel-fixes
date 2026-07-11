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
package com.mgmtp.a12.kernel.core.rt.a12internal;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Parameter class for presentation information for fields.
 * It is used in {@link ValidationMode} to specify the presentation of fields in check-methods.
 * If the presentation information is empty or <code>null</code>, the presentation as saved in
 * ExtMP editor is used for validation.
 * <br>
 * Additionally, this class is used as a parameter for the convert-methods
 * {@linkplain IMetaField#convert(String, PresentationInformation, boolean, Object...)}.
 * <p>
 * For example, it is possible to specify the decimal separator for validation. If the decimal
 * separator is specified as "comma", numbers that have a dot as decimal separator will not
 * validate. This happens independently of the setting for the decimal separator for this module
 * type.
 * </p>
 * <p>
 * Following keywords may be used:
 * <ul>
 * <li><code>{@link #DECIMAL_SEPARATOR}</code>:
 * only a dot (".") or comma (",") are allowed. You can also use the constant
 * {@link #LANGUAGE_DEFINED}.</li>
 * <li><code>{@link #THOUSANDS_SEPARATOR}</code>:
 * the optional separator used for large numbers {@literal >= 1000}. You can also use the constant
 * {@link #LANGUAGE_DEFINED}.</li>
 * <b>Note:</b> if a thousands separator is defined, the decimal separator has also to be defined,
 * otherwise an {@link IllegalArgumentException} is thrown.
 * <li><code>{@link PresentationInformation#THOUSANDS_SEPARATOR_APPLY_TO_TRAIT}</code>: this keyword can be set
 * to a trait. The functionality around the {@link #THOUSANDS_SEPARATOR} (if defined),
 * i.e. adding/removing the thousands separator to/from the input, adapting the max length according to the thousands separator and so on,
 * will be applied only to number fields with this trait. By not setting this configuration,
 * the functionality mentioned above will be applied to all number fields no matter their trait. Possible trait values are:
 * <ul>
 * <li>amount: amount of money</li>
 * <li>none: all other numbers that do not belong to one of the other categories above. Currently these are all numbers that are not an amount of money</li>
 * </ul>
 * <li><code>{@link #TRUE_VALUE}</code>: defines the presentation of
 * <code>true</code> for a boolean field.
 * The value is passed as a String.</li>
 * <li><code>{@link #FALSE_VALUE}</code>: defines the presentation of
 * <code>false</code> for a boolean field.
 * The new value is passed as a String.</li>
 * <li><code>{@link #DATE_SEPARATOR}</code>: defines the separator between year/month/day.
 * The value may be any character, or an empty String or the constant {@link #LANGUAGE_DEFINED}.
 * Note: Does not affect formats containing time elements.
 * </li>
 * <li><code>{@link #DATE_ZERO_OPTIONAL}</code>: defines whether for
 * dates containing numbers below 10 leading zeroes must be placed or not. The only values allowed
 * are class specific constants <code>{@link #TRUE}</code>, <code>{@link #FALSE}</code> or
 * {@link #LANGUAGE_DEFINED}. Other values are ignored. Note: Does not affect formats containing
 * time elements.
 * </li>
 * <li><code>{@link #DATE_FRAGMENT_ORDERING}</code>: defines in which order year/month/day are
 * shown.
 * Please use the constants
 * <code>{@link #YEAR_MONTH_DAY},{@link #YEAR_DAY_MONTH},{@link #DAY_YEAR_MONTH},
 * {@link #DAY_MONTH_YEAR}, {@link #MONTH_YEAR_DAY}, {@link #MONTH_DAY_YEAR}</code>
 * </p>
 * <p>
 * For example, {@link #MONTH_DAY_YEAR} specifies "01.21.2016" as January 21,2016. The constant
 * {@link #LANGUAGE_DEFINED} may also be used.</li> Note: Does not affect formats containing time
 * elements.
 * <li><code>{@link #CONVERT_CUSTOM_FIELD_TYPES_LIST}</code>: if this keyword is set to a list of
 * comma separated custom field type names then for all of the corresponding types specifying an UI
 * presentation the UI presentation will be used. An empty string is considered not set.
 * Otherwise they are not converted, i.e. the internal representation is used.</li>
 * <b>Note:</b> If a non-empty {@link #CONVERT_CUSTOM_FIELD_TYPES_LIST} is defined together with
 * {@link #CONVERT_CUSTOM_FIELD_TYPES_ALL} set to <code>{@link #TRUE}</code>, an
 * {@link IllegalArgumentException} is thrown.
 * <li><code>{@link #CONVERT_CUSTOM_FIELD_TYPES_ALL}</code>: if this keyword is set to
 * <code>{@link #TRUE}</code> then for all custom field types specifying an UI presentation the UI
 * presentation will be used.
 * Otherwise they are not converted, i.e. the internal representation is used.
 * The default value of this key is {@link PresentationInformation#FALSE}</li>
 * <b>Note:</b> If a non-empty {@link #CONVERT_CUSTOM_FIELD_TYPES_LIST} is defined together with
 * {@link #CONVERT_CUSTOM_FIELD_TYPES_ALL} set to <code>{@link #TRUE}</code>, an
 * {@link IllegalArgumentException} is thrown.
 * <li><code>{@link #CONVERT_ENUMERATIONS}</code>: if this keyword is set to
 * <code>{@link #TRUE}</code>
 * then for enumeration types specifying an UI presentation the UI presentation will be used.
 * Otherwise they are not converted, i.e. the internal representation is used.</li>
 * <li><code>{@link PresentationInformation#DECIMAL_PLACES_OPTIONAL}</code>: if this keyword is set
 * to <code>{@link PresentationInformation#TRUE}</code> and for a number the value given does have
 * less than the minimal amount of decimal places, the decimal places will be filled with zeroes
 * until the minimal number of decimal places is reached. I.e., for a number with at least n digits
 * after the decimal separator, the following cases are additionally regarded as correct:
 * <ul>
 * <li>no decimal separator e.g. <code>"1"</code></li>
 * <li>a decimal separator, but no digits after the separator, e.g. <code>"1."</code></li>
 * <li>a decimal separator, and 1 up to n-1 digits after the decimal separator, e.g.
 * <code>"1.1"</code>.</li>
 * </ul>
 * The default value of this key is
 * {@link PresentationInformation#FALSE}</li>
 * <li><code>{@link #DATE_TIME_FORMAT}</code>: defines the presentation format for date time. It is
 * independent of other date specifications and is given as a format string for
 * {@link DateTimeFormatter} and will be used to parse or convert a date time value
 * from/to a string representation. In the format, only the pattern letters y, M, d, H, h, m, s, and
 * a are allowed. A year specification (y) must always be included. Only the letter 'T' or
 * whitespace is allowed as separator between date and time.
 * <p>
 * <b>Notes:</b>
 * <ul>
 * <li>The constraints won't be checked in code.</li>
 * <li>Not supported pattern letters or a not supported separator may let the validation fail.</li>
 * <li>The constant {@link #LANGUAGE_DEFINED} may also be used.</li>
 * <li>For only date or only time use the respective configuration key(s).</li>
 * </ul>
 * </li>
 * <li><code>{@link #TIME_FORMAT}</code>: defines the presentation format for time. It is
 * independent of other date (time) specifications and of {@link #DATE_TIME_FORMAT}. It is given as
 * a format string for {@link DateTimeFormatter} and will be used to parse or convert a time value
 * from/to a string representation. In the format, only the pattern letters H, h, m, s, and a are
 * allowed.
 * <p>
 * <b>Notes:</b>
 * <ul>
 * <li>The constraints won't be checked in code.</li>
 * <li>Not supported pattern letters may let the validation fail.</li>
 * <li>The constant {@link #LANGUAGE_DEFINED} may also be used.</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * <p>
 * Keywords are checked via <i>assert</i> statements for correctness. If assertions are disabled,
 * faulty keywords are simply ignored.<br>
 * <p>
 * If assertions are activated, the following is checked in detail:
 * <ul>
 * <li>Key is not known</li>
 * <li>{@link #DATE_SEPARATOR} must only consist of a maximum of one character.</li>
 * <li>The {@link #DATE_SEPARATOR} must not be the blank character.</li>
 * <li>{@link #DATE_RANGE_SEPARATOR} must only consist of exactly one character.</li>
 * <li>{@link #DATE_ZERO_OPTIONAL} was set to true, but no {@link #DATE_SEPARATOR} was specified, or the {@link #DATE_SEPARATOR}
 * was the empty string. This is not allowed.</li>
 * <li>If a value is specified for one of the keys {@link #DATE_SEPARATOR}, {@link #DATE_RANGE_SEPARATOR} or
 * {@link #DATE_FRAGMENT_ORDERING}, a value must be specified for all three keys. Otherwise an exception is thrown.</li>
 * <li>The same character as for the {@link #DATE_RANGE_SEPARATOR} must not be specified as {@link #DATE_SEPARATOR}.</li>
 * </ul>
 * </p>
 * <p>
 * <h3>ExtMP specific information:</h3>
 * <b>Special features in the specification of date formats:</b>
 * <p>
 * <u>Order specification</u><br>
 * For date formats, the order of the day, month and year components can be specified in the presentation information. However, it
 * is possible that the date format for a field in the ExtMP does not have all three date components. For example, date formats
 * are used that only have year information or date formats that do not have year information. The specification of the order of
 * the components in the presentation information is then interpreted in such a way that the components of the date stored in
 * ExtMP must be provided in the specified order.<br>
 * <i>Example:</i> The format 'MM.DD' is interpreted when specifying the order {@link #DAY_MONTH_YEAR} in such a way that the day
 * must be specified first and then the month.<br>
 * </p>
 * <p>
 * <u>Specification of the date separator</u><br>
 * If a date separator is specified in the presentation information, the information for the day, month and year must be separated
 * with the specified separator. The following convention is used - as is usual in German notation:
 * <ul>
 * <li>When three date parts are used (i.e. day, month and year are specified) two separators are used.</li>
 * <li>When using one date part (e.g. only the year), no separator is used.</li>
 * <li>When using two date parts a distinction is made:
 * <ul>
 * <li>If the dot (".") was specified as the {@link #DATE_SEPARATOR} and {@link #DAY_MONTH_YEAR} as the order, and if the date
 * format consists of day and month, the dot is permitted as a separator at the end.
 * This means that both '17.04' and '17.04.' are accepted.</li>
 * <li>In all other cases, exactly one separator - between the two date parts - is used.</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * If no date separator is specified in the display information, the separator specified in the field definition of the field to be
 * checked is used.<br>
 * Date ranges are handled in the same way. It should be noted that only date range specifications are processed for which the date
 * format for the first date and the second date are the same. Other specifications are also not useful.
 * </p>
 * <p>
 * <b>Special format partially known date</b><br>
 * ExtMP supports a special format for date fields with optional information. In contrast to the normal representation of a
 * date, '00' or '0000' can be selected here for the date components month and day. This is only permitted for date fields where
 * not all parts of the date are known.<br>
 * In a validation with set display information, such date specifications are treated like normal date specifications, except that
 * '00' or '0000' is also permitted as a specification of the date component for such fields.
 * </p>
 */
@OnlyForUsage
public class PresentationInformation {
	public static final String DECIMAL_SEPARATOR = "DezimalTrenner";
	public static final String THOUSANDS_SEPARATOR = "ThousandsSeparator";
	public static final String THOUSANDS_SEPARATOR_APPLY_TO_TRAIT = "ThousandsSeparatorApplyToTrait";
	public static final String TRUE_VALUE = "WertWahr";
	public static final String FALSE_VALUE = "WertFalsch";
	public static final String DATE_SEPARATOR = "DatumTrenner";
	public static final String DATE_RANGE_SEPARATOR = "DatumBereichTrenner";
	public static final String DATE_ZERO_OPTIONAL = "DatumNullenOptional";
	public static final String DATE_FRAGMENT_ORDERING = "DatumReihenfolge";
	public static final String CONVERT_CUSTOM_FIELD_TYPES_LIST = "ConvertCustomFieldTypesList";
	public static final String CONVERT_CUSTOM_FIELD_TYPES_ALL = "ConvertCustomFieldTypesAll";
	public static final String CONVERT_ENUMERATIONS = "KonvertiereEnumerationen";
	public static final String DECIMAL_PLACES_OPTIONAL = "DecimalPlacesOptional";
	public static final String DATE_TIME_FORMAT = "DateTimeFormat";
	public static final String TIME_FORMAT = "TimeFormat";

	public static final String YEAR_MONTH_DAY = "JMT";
	public static final String YEAR_DAY_MONTH = "JTM";
	public static final String DAY_YEAR_MONTH = "TJM";
	public static final String DAY_MONTH_YEAR = "TMJ";
	public static final String MONTH_YEAR_DAY = "MJT";
	public static final String MONTH_DAY_YEAR = "MTJ";

	public static final String TRUE = "j";
	public static final String FALSE = "n";

	public static final String TRAIT_AMOUNT = "amount";
	public static final String TRAIT_NONE = "none";

	/**
	 * If this constant is used to specify the {@link #DECIMAL_SEPARATOR}, {@link #DATE_SEPARATOR},
	 * {@link #DATE_ZERO_OPTIONAL} or {@link #DATE_FRAGMENT_ORDERING}, the values as
	 * defined for the specified language are used.
	 * The language is specified in {@link ValidationMode#setLanguage} or
	 * as a parameter in {@link IMetaField#convert}
	 */
	public static final String LANGUAGE_DEFINED = "LANGUAGE_DEFINED";

	Map<String, String> data = new HashMap<>();

	/**
	 * Adds a new key-value pair to the presentation information.
	 *
	 * @param key
	 * 		key for the presentation information.
	 * @param value
	 * 		value to be set.
	 */
	public void add(String key, String value) {
		data.put(key, value);
	}

	public Map<String, String> getPresentationInformation() {
		return data;
	}
}
