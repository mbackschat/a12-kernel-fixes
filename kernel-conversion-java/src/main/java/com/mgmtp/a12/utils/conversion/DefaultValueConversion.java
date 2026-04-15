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

package com.mgmtp.a12.utils.conversion;

import java.time.Instant;
import java.util.Optional;

import com.mgmtp.a12.model.utils.Experimental;
import com.mgmtp.a12.utils.conversion.config.BooleanConversionConfig;
import com.mgmtp.a12.utils.conversion.config.ConfirmConversionConfig;
import com.mgmtp.a12.utils.conversion.config.CustomStringConversionConfig;
import com.mgmtp.a12.utils.conversion.config.DateBasicConversionConfig;
import com.mgmtp.a12.utils.conversion.config.DateConversionConfig;
import com.mgmtp.a12.utils.conversion.config.DateFragmentConversionConfig;
import com.mgmtp.a12.utils.conversion.config.DateRangeConversionConfig;
import com.mgmtp.a12.utils.conversion.config.DateTimeConversionConfig;
import com.mgmtp.a12.utils.conversion.config.EnumerationConversionConfig;
import com.mgmtp.a12.utils.conversion.config.NumberConversionConfig;
import com.mgmtp.a12.utils.conversion.config.PartiallyKnownDateConversionConfig;
import com.mgmtp.a12.utils.conversion.config.StringConversionConfig;
import com.mgmtp.a12.utils.conversion.config.TimeConversionConfig;
import com.mgmtp.a12.utils.conversion.config.ValueConversionConfig;
import com.mgmtp.a12.utils.conversion.impl.DefaultParseResult;
import com.mgmtp.a12.utils.conversion.internal.BooleanConversion;
import com.mgmtp.a12.utils.conversion.internal.ConfirmConversion;
import com.mgmtp.a12.utils.conversion.internal.DateConversion;
import com.mgmtp.a12.utils.conversion.internal.DateRangeConversion;
import com.mgmtp.a12.utils.conversion.internal.NumberConversion;
import com.mgmtp.a12.utils.conversion.internal.PartiallyKnownDateConversion;
import com.mgmtp.a12.utils.conversion.internal.StringConversion;
import com.mgmtp.a12.utils.localization.DataFormats;
import com.mgmtp.a12.utils.localization.internal.DataFormatsUtil;
import com.mgmtp.a12.utils.localization.internal.InternalDefaultDataFormats;

/**
 * This implementation <b>does not</b> support conversion between different types of {@link ValueConversionConfig},
 * e.g. from {@link NumberConversionConfig} to {@link DateConversionConfig},
 * or even from {@link DateConversionConfig} to {@link DateFragmentConversionConfig}.
 * <p>
 * <b>General notes:</b>
 * <ul>
 *   <li>In general the parsing (specially to a {@link Instant} value) is a little more strict about checking the given value than the formatting.
 *       During formatting, a specific object is given and we don't expect this to be erroneous.</li>
 *   <li>Boolean (see also {@link BooleanConversionConfig})
 *   <ul>
 *     <li>If <code>trueValue</code> is not defined, the default value is used.</li>
 *     <li>If <code>falseValue</code> is not defined, the default value is used.</li>
 *   </ul>
 *   </li>
 *   <li>Confirm (see also {@link ConfirmConversionConfig})
 *   <ul>
 *     <li>If <code>trueValue</code> is not defined, the default value is used.</li>
 *   </ul>
 *   </li>
 *   <li>Common notes for date related types
 *   <ul>
 *     <li>Be aware how the configuration in {@link DataFormats} influence <code>format</code> in the corresponding <code>ConversionConfig</code>:
 *     <ul>
 *         <li>{@link DataFormats#dateTimeFormat()} will be considered if {@link DateTimeConversionConfig#format()} contains a date part (recognized by the month 'MM') and a time part (recognized by the hours 'HH' or 'hh').</li>
 *         <li>{@link DataFormats#timeFormat()} will be considered if {@link TimeConversionConfig#format()} contains a time part (recognized by the hours 'HH' or 'hh'), but no date part (recognized by the month 'MM').</li>
 *         <li>All other date configurations in {@link DataFormats} affect <code>format</code> in {@link DateConversionConfig}, {@link DateFragmentConversionConfig} and {@link DateRangeConversionConfig}.</li>
 *     </ul>
 *     </li>
 *     <li>If <code>baseYear</code> is not defined, the default value is used.
 *       This configuration is used when dealing with date formats which don't define the year, e.g. <code>MM-dd</code>.</li>
 *     <li>While parsing a value with only time, the value <code>1970-01-01</code> is used for the date part.</li>
 *     <li>If <code>timeZone</code> is not defined, the default value is used.</li>
 *     <li>We rely on the behavior of a third party library regarding the parsing and formatting of a date.</li>
 *   </ul>
 *   </li>
 *   <li>Date (see also {@link DateConversionConfig})
 *   <ul>
 *     <li>See also "Common notes for date related types".</li>
 *     <li>If <code>datePrecision</code> is not defined, the default value is used.</li>
 *   </ul>
 *   </li>
 *   <li>DateFragment, DateTime and Time (see also {@link DateFragmentConversionConfig}, {@link DateTimeConversionConfig}, {@link TimeConversionConfig})
 *   <ul>
 *     <li>See also "Common notes for date related types".</li>
 *   </ul>
 *   </li>
 *   <li>DateRange (see also {@link DateRangeConversionConfig})
 *   <ul>
 *     <li>See also "Common notes for date related types".</li>
 *     <li>If <code>rangeSeparator</code> is not defined, the default value is used.</li>
 *   </ul>
 *   </li>
 *   <li>Number (see also {@link NumberConversionConfig})
 *   <ul>
 *     <li>If <code>decimalSeparator</code> is not defined, the default value is used.</li>
 *     <li>If <code>decimalPlacesOptional</code> is not defined, the default value is used.</li>
 *     <li>If <code>minFractionalDigits</code> is not defined, the default value is used.</li>
 *     <li>If <code>leadingZerosAllowed</code> is not defined, the default value is used.</li>
 *   </ul>
 *   </li>
 *   <li>String (see also {@link StringConversionConfig}, {@link EnumerationConversionConfig}, {@link CustomStringConversionConfig})
 *   <ul>
 *     <li>The is no conversion at all, the original value is returned.</li>
 *   </ul>
 *   </li>
 * </ul>
 */
@Experimental
public class DefaultValueConversion implements ValueConversion {

	private final DataFormats dataFormats;

	public DefaultValueConversion(DataFormats dataFormats) {
		this.dataFormats = dataFormats;
	}

	/**
	 * See also the description of {@link DefaultValueConversion}.
	 * Please be aware of the following additional notes for this method:
	 * <ul>
	 *   <li>Boolean (see also {@link BooleanConversionConfig})
	 *   <ul>
	 *     <li>If the given value cannot be assigned to either <code>trueValue</code> or <code>falseValue</code> from <code>inputFormat</code>,
	 *       <code>null</code> is returned.</li>
	 *   </ul>
	 *   </li>
	 *   <li>Confirm (see also {@link ConfirmConversionConfig})
	 *   <ul>
	 *     <li>If the given value cannot be assigned to <code>trueValue</code> from <code>inputFormat</code>, <code>null</code> is returned.</li>
	 *   </ul>
	 *   </li>
	 *   <li>Date (see also {@link DateConversionConfig})
	 *   <ul>
	 *     <li>If at least one date part is unknown (e.g. 1980-01-00), the original value is returned.</li>
	 *   </ul>
	 *   </li>
	 *   <li>DateFragment, DateTime and Time (see also {@link DateFragmentConversionConfig}, {@link DateTimeConversionConfig} and {@link TimeConversionConfig})
	 *   <ul>
	 *     <li>There are no additional notes.</li>
	 *   </ul>
	 *   </li>
	 *   <li>DateRange (see also {@link DateRangeConversionConfig})
	 *   <ul>
	 *     <li>If <code>singleDate</code> is set to <code>"allowed"</code> or <code>"only"</code>, the <code>rangeSeparator</code> must not be empty.</li>
	 *   </ul>
	 *   </li>
	 *   <li>Number (see also {@link NumberConversionConfig})
	 *   <ul>
	 *     <li>If the given value contains leading zeroes and <code>leadingZerosAllowed==true</code>, the original value is returned.</li>
	 *   </ul>
	 *   </li>
	 *   <li>String (see also {@link StringConversionConfig}, {@link EnumerationConversionConfig}, {@link CustomStringConversionConfig})
	 *   <ul>
	 *     <li>There are no additional notes.</li>
	 *   </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param value
	 * 		string value to be parsed
	 * @param inputFormat
	 * 		configuration which describes the given value
	 * @return the corresponding typed value or a {@link ParseError} occurred while parsing the given value, see also the method
	 * 		description
	 */
	@Override public ParseResult parseValue(String value, ValueConversionConfig inputFormat) {
		DataFormats df = DataFormatsUtil.mergeDataFormats(new InternalDefaultDataFormats(), dataFormats);
		if (inputFormat instanceof BooleanConversionConfig) {
			return BooleanConversion.convertStringToBoolean(value, df);
		} else if (inputFormat instanceof ConfirmConversionConfig) {
			return ConfirmConversion.convertStringToConfirm(value, df);
		} else if (inputFormat instanceof NumberConversionConfig numberConversion) {
			return NumberConversion.parseNumber(value, numberConversion, df);
		} else if (inputFormat instanceof DateRangeConversionConfig drConversion) {
			return DateRangeConversion.parseDateRange(value, drConversion, df);
		} else if (inputFormat instanceof PartiallyKnownDateConversionConfig pkdConversion && PartiallyKnownDateConversion.isPartiallyKnownDateAllowed(
				pkdConversion)) {
			return PartiallyKnownDateConversion.parseDate(value, pkdConversion, df);
		} else if (inputFormat instanceof DateBasicConversionConfig dateConversion) {
			return DateConversion.parseDate(value, dateConversion, df);
		} else if (inputFormat instanceof StringConversionConfig
				|| inputFormat instanceof EnumerationConversionConfig
				|| inputFormat instanceof CustomStringConversionConfig) {
			return DefaultParseResult.ofValue(value);
		} else {
			throw new IllegalArgumentException(
					"The provided value conversion config is not supported (" + inputFormat.getClass().getCanonicalName() + ").");
		}
	}

	/**
	 * See also the description of {@link DefaultValueConversion}.
	 * Please be aware of the following additional notes for this method:
	 * <ul>
	 *   <li>Boolean (see also {@link BooleanConversionConfig})
	 *   <ul>
	 *     <li>Only <code>boolean</code> and <code>null</code> values are supported.</li>
	 *   </ul>
	 *   </li>
	 *   <li>Confirm (see also {@link ConfirmConversionConfig})
	 *   <ul>
	 *     <li>Only <code>boolean</code> and <code>null</code> values are supported.</li>
	 *     <li>If the given value is <code>false</code>, <code>null</code> is returned.</li>
	 *   </ul>
	 *   </li>
	 *   <li>Date (see also {@link DateConversionConfig})
	 *   <ul>
	 *     <li><code>baseYear</code> and <code>datePrecision</code> are ignored.</li>
	 *     <li>Only <code>Date</code>, <code>string</code> and <code>null</code> values are supported.</li>
	 *     <li>A <code>string</code> value is supported, only if at least one date part is unknown (e.g. 1980-01-00).
	 *       In this case, the original value is returned.</li>
	 *     <li>Day and month of a date are always formatted with two digits.<br>
	 *       Example: for the date April 7th, 1999 and the format "yyyy-M-d" it returns "1999-04-07".</li>
	 *   </ul>
	 *   </li>
	 *   <li>DateFragment, DateTime and Time (see also {@link DateFragmentConversionConfig}, {@link DateTimeConversionConfig}, {@link TimeConversionConfig})
	 *   <ul>
	 *     <li><code>baseYear</code> is ignored.</li>
	 *     <li>Only <code>Date</code> and <code>null</code> values are supported.</li>
	 *     <li>Day and month of a date are always formatted with two digits.<br>
	 *       Example: for the date April 7th, 1999 and the format "yyyy-M-d" it returns "1999-04-07".</li>
	 *   </ul>
	 *   </li>
	 *   <li>DateRange (see also {@link DateRangeConversionConfig})
	 *   <ul>
	 *     <li><code>baseYear</code> and <code>interpretationOfYear</code> (if present) are ignored.</li>
	 *     <li>Only <code>Date[]</code>, <code>Date</code> and <code>null</code> values are supported.</li>
	 *     <li>Day and month of a date are always formatted with two digits.<br>
	 *       Example: for the date April 7th, 1999 and the format "yyyy-M-d" it returns "1999-04-07".</li>
	 *   </ul>
	 *   </li>
	 *   <li>Number (see also {@link NumberConversionConfig})
	 *   <ul>
	 *     <li>Only <code>number</code>, <code>string</code> and <code>null</code> values are supported.</li>
	 *     <li>A <code>string</code> value is supported, only if the value contains leading zeroes and <code>leadingZerosAllowed==true</code>.
	 *       In this case, the original value is returned.</li>
	 *     <li><code>decimalPlacesOptional</code> and <code>minFractionalDigits</code> work together:
	 *       Only if <code>decimalPlacesOptional==false</code>,
	 *         missing zeroes are added in the decimal part in order to fulfill <code>minFractionalDigits</code>.
	 *       Based on <code>minFractionalDigits</code>, exceeding fractional digits are <b>not</b> truncated.</li>
	 *   </ul>
	 *   </li>
	 *   <li>String (see also {@link StringConversionConfig}, {@link EnumerationConversionConfig}, {@link CustomStringConversionConfig})
	 *   <ul>
	 *     <li>There are no additional notes.</li>
	 *   </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param value
	 * 		typed value to be formatted
	 * @param outputFormat
	 * 		configuration based on which the given value should be formatted
	 * @return the corresponding formatted value, see also the method description
	 */
	@Override public Optional<String> formatValue(Object value, ValueConversionConfig outputFormat) {
		if (value == null) {
			return Optional.empty();
		}
		DataFormats df = DataFormatsUtil.mergeDataFormats(new InternalDefaultDataFormats(), dataFormats);
		if (outputFormat instanceof BooleanConversionConfig) {
			return BooleanConversion.convertBooleanToString(value, df);
		} else if (outputFormat instanceof ConfirmConversionConfig) {
			return ConfirmConversion.convertConfirmToString(value, df);
		} else if (outputFormat instanceof NumberConversionConfig numberConversion) {
			return Optional.of(NumberConversion.convertNumberToString(value, numberConversion, df));
		} else if (outputFormat instanceof DateRangeConversionConfig drConversion) {
			return DateRangeConversion.convertDateRangeToString(value, drConversion, df);
		} else if (outputFormat instanceof PartiallyKnownDateConversionConfig dateConversion) {
			return PartiallyKnownDateConversion.convertDateToString(value, dateConversion, df);
		} else if (outputFormat instanceof DateBasicConversionConfig dateConversion) {
			return DateConversion.convertDateToString(value, dateConversion, df);
		} else if (outputFormat instanceof StringConversionConfig
				|| outputFormat instanceof EnumerationConversionConfig
				|| outputFormat instanceof CustomStringConversionConfig) {
			return Optional.of(StringConversion.convertStringTypeToString(value));
		} else {
			throw new IllegalArgumentException(
					"The provided value conversion config is not supported (" + outputFormat.getClass().getCanonicalName() + ").");
		}
	}

}
