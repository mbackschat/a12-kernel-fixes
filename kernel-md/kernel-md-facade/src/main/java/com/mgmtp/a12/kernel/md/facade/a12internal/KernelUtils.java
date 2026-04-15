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

package com.mgmtp.a12.kernel.md.facade.a12internal;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import com.mgmtp.a12.kernel.core.base.utils.internal.CharsetUtil;
import com.mgmtp.a12.kernel.core.base.utils.internal.FieldTypeConstants;
import com.mgmtp.a12.kernel.core.base.utils.internal.charset.LegalCharTester;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelConfig;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelInfo;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV2;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateUtil;
import com.mgmtp.a12.utils.conversion.a12internal.util.PartiallyKnownDatesUtil;

/**
 * Utility class containing low level functionality provided by kernel.
 */
@OnlyForA12InternalUsage
public class KernelUtils {

	private KernelUtils() {
		throw new UnsupportedOperationException("This class must not be instanciaced.");
	}

	/**
	 * Creates a {@link Date}-object for partially known date strings as follows:
	 * <ol>
	 * <li>The given string value is checked syntactically against the pattern created from the
	 * supported date format. If the value does not match the pattern, an
	 * {@link IllegalArgumentException} is thrown.</li>
	 * <li>Unknown date parts are replaced with the corresponding values from '1970-01-01', e.g.
	 * <ul>
	 * <li>2020-02-00 -&gt; 2020-02-01</li>
	 * <li>2020-00-00 -&gt; 2020-01-01</li>
	 * <li>0000-00-00 -&gt; 1970-01-01</li>
	 * </ul>
	 * <li>Strings representing a known date will be converted to the corresponding
	 * {@link Date}-object.</li>
	 * <li>Please note that the given string is not validated.</li>
	 * </ol>
	 *
	 * @param partiallyKnownDate
	 * 		string from which a {@link Date}-object should be created.
	 * 		May not be {@code null} or empty.
	 * @param timeZone
	 * 		the time zone used to interpret the date string in.
	 * @return approximated {@link Date}-object as described above.
	 *
	 * @throws IllegalArgumentException
	 * 		see method description.
	 */
	public static Date getApproximatedDate(String partiallyKnownDate, TimeZone timeZone) {
		String format = FieldTypeConstants.DEFAULT_DATE_FORMAT;
		return PartiallyKnownDatesUtil.parseApproximatedDate(partiallyKnownDate, format, timeZone);
	}

	/**
	 * like {@link #getApproximatedDate(String, TimeZone)}, but for
	 * {@link BasicTypeV2} instead of
	 * {@link BasicTypeV1}; i.e. returns an {@link Instant} where
	 * {@code getApproximatedDate} would return a {@link Date}
	 */
	public static Instant getApproximatedDateV2(String partiallyKnownDate, TimeZone timeZone) {
		return BasicTypeV1V2Conversion.dateToInstant(getApproximatedDate(partiallyKnownDate, timeZone));
	}

	/**
	 * Tries to create a {@link Date}-object as follows:
	 * <ol>
	 *     <li>The given {@code value} is checked syntactically against the pattern created from the given {@code dateFormat}. If
	 *     the value does not match the pattern, a RuntimeException is thrown.</li>
	 *     <li>If partially known dates are allowed and a date part is unknown, the original value is returned.</li>
	 *     <li>Otherwise the {@link Date}-object created from the given value is returned.</li>
	 * </ol>
	 * <b>Note:</b> Due to the syntax check, only the following date format fragments are allowed for a {@code dateFormat}:
	 * <ol>
	 *     <li>{@code yyyy} for the year</li>
	 *     <li>{@code MM} for the month</li>
	 *     <li>{@code dd} for the day</li>
	 *     <li>{@code 'T'} as separator between date and time</li>
	 *     <li>{@code HH} or {@code hh} for the hours</li>
	 *     <li>{@code mm} for the minutes</li>
	 *     <li>{@code ss} for the seconds</li>
	 *     <li>{@code a} for AM/PM marker</li>
	 * </ol>
	 *
	 * @param value
	 * 		string from which a {@link Date}-object should be created. May not be {@code null} or empty.
	 * @param dateFormat
	 * 		format of the date, see method description, specially the <b>Note</b>. May not be {@code null} or empty.
	 * @param baseYear
	 * 		the base year which could be taken from the document model (see {@link IDocumentModelInfo#getBaseYear()}). This year
	 * 		is relevant for {@code dateFormat}s containing {@code MM} and {@code dd} but not {@code yyyy}, in order to determine
	 * 		if the February 29th is a valid input or not.
	 * 		If no base year is defined in the document model, a default value should be given (in A12 kernel the year {@code 2000}
	 * 		is commonly used). May not be 0.
	 * @param arePartiallyKnownDatesAllowed
	 *        {@code true} if partially known dates are allowed, e.g. {@code 2020-00-00}, otherwise {@code false}.
	 * @param timeZone
	 *        {@link TimeZone} which should be considered while creating the {@link Date}-object. This could be taken from the
	 * 		document model (see {@link IDocumentModelConfig#getTimeZone()}). May not be {@code null}.
	 * @return see method description.
	 *
	 * @throws IllegalArgumentException
	 * 		if the value cannot be parsed to a {@link Date}-object (see also method description).
	 */
	public static Object parseDate(String value, String dateFormat, int baseYear,
			boolean arePartiallyKnownDatesAllowed, TimeZone timeZone) {
		// the logic of this method must be as similar as possible to FormatDefinitionDatum.convertToBasicType
		return DateUtil.createDateIfPossible(value, dateFormat, baseYear, arePartiallyKnownDatesAllowed, timeZone);
	}

	/**
	 * like {@link #parseDate(String, String, int, boolean, TimeZone)}, but for
	 * {@link BasicTypeV2} instead of
	 * {@link BasicTypeV1}; i.e. returns an {@link Instant} where {@code parseDate}
	 * would return a {@link Date}
	 */
	public static Object parseDateV2(String value, String dateFormat, int baseYear,
			boolean arePartiallyKnownDatesAllowed, TimeZone timeZone) {
		return BasicTypeV1V2Conversion.basicTypeV1ToV2(
				parseDate(value, dateFormat, baseYear, arePartiallyKnownDatesAllowed, timeZone));
	}

	/**
	 * The main difference to {@link #parseDate(String, String, int, boolean, TimeZone)} is that a {@link Date}-object is also
	 * created in case that partially known dates are allowed, see also {@link #getApproximatedDate(String, TimeZone)}.
	 *
	 * @param value
	 * 		value to convert.
	 * @param field
	 *        {@link IField} in which is defined how the date should be parsed. May not be {@code null}.
	 * @param documentModel
	 *        {@link IDocumentModel} in which the field is defined. May not be {@code null}.
	 * @return the {@link Date}-object created from the given {@code value}. {@code null} is returned if the given {@code value} is
	 * 		also {@code null}.
	 *
	 * @throws IllegalArgumentException
	 * 		if the type of the given {@code value} cannot be converted to a {@link Date}-object or if the field type of the given
	 *        {@link IField} is not dte-like.
	 */
	public static Date parseDate(Object value, IField field, IDocumentModel documentModel) {
		Objects.requireNonNull(field);
		Objects.requireNonNull(documentModel);
		return parseDateInternal(value, field, documentModel);
	}

	/**
	 * like {@link #parseDate(Object, IField, IDocumentModel)}, but for
	 * {@link BasicTypeV2} instead of
	 * {@link BasicTypeV1}; i.e. returns an {@link Instant} where {@code parseDate}
	 * would return a {@link Date}
	 */
	public static Instant parseDateV2(Object value, IField field, IDocumentModel documentModel) {
		return BasicTypeV1V2Conversion.dateToInstant(parseDate(value, field, documentModel));
	}

	protected static Date parseDateInternal(Object value, IField field, IDocumentModel documentModel) {
		if (value == null) {
			return null;
		}
		if (value instanceof Date date) {
			return date;
		}
		if (!(value instanceof String)) {
			throw new IllegalArgumentException(
					String.format("The type '%s' of the given value is not supported for the conversion to a date-object.",
							value.getClass().getName()));
		}
		IFieldType fieldType = field.getEffectiveType().orElseThrow(() -> new IllegalArgumentException(
				String.format("The given field '%s' is defined from a type definition which couldn't be resolved.",
						ElementUtils.getPath(field))));
		if (!(fieldType instanceof IDateType)
				&& !(fieldType instanceof IDateFragmentType)) {
			throw new IllegalArgumentException(
					String.format("The field type '%s' of the given field '%s' is not date-like.", fieldType.getClass().getName(),
							ElementUtils.getPath(field)));
		}
		boolean arePartiallyKnownDatesAllowed = fieldType instanceof IDateType dateType && dateType.arePartiallyKnownDatesAllowed();
		String dateFormat = getDateFormat(fieldType);
		TimeZone timeZone = TimeZone.getTimeZone(documentModel.getContent().getDocumentModelConfig().getTimeZone());
		int baseYear = documentModel.getContent().getDocumentModelInfo().getBaseYear().orElse(2000);
		if (arePartiallyKnownDatesAllowed) {
			return getApproximatedDate((String) value, timeZone);
		} else {
			return (Date) parseDate((String) value, dateFormat, baseYear, false, timeZone);
		}
	}

	private static String getDateFormat(IFieldType fieldType) {
		if (fieldType instanceof IDateType dateType) {
			return dateType.getFormat();
		} else {
			// we already checked that fieldType can be only these two types
			return ((IDateFragmentType) fieldType).getFormatOfFragment();
		}
	}

	/**
	 * @param value
	 *        {@link String} or {@link Date} which should be processed. May not be {@code null}.
	 * @param dateFormat
	 * 		format of the date as defined in {@link SimpleDateFormat} to which the given date should be formatted. May not be
	 *        {@code null} or empty.
	 * @param timeZone
	 *        {@link TimeZone} which was considered while creating the given {@link Date}-object. This could be taken from the
	 * 		document model (see {@link IDocumentModelConfig#getTimeZone()}). May not be {@code null}.
	 * @return string representation of the given {@code value}.
	 *
	 * @throws IllegalArgumentException
	 * 		if the given {@code value} is not a {@link Date} or it is a {@link String} without unknown date parts.
	 */
	public static String formatDate(Object value, String dateFormat, TimeZone timeZone) {
		// the logic of this method must be as similar as possible to FormatDefinitionDatum.convertFromBasicType
		return DateUtil.convertDateToString(value, dateFormat, timeZone);
	}

	/**
	 * like {@link #formatDate(Object, String, TimeZone)}, but for
	 * {@link BasicTypeV2} instead of
	 * {@link BasicTypeV1}; i.e. accepts an {@link Instant} where {@code formatDate}
	 * would accept a {@link Date}
	 */
	public static String formatDateV2(Object value, String dateFormat, TimeZone timeZone) {
		// the logic of this method must be as similar as possible to FormatDefinitionDatum.convertFromBasicType
		return DateUtil.convertDateToString(BasicTypeV1V2Conversion.basicTypeV2ToV1(value), dateFormat, timeZone);
	}

	/**
	 * Validates a restricted charset.
	 * <ul>
	 *     <li>Parses the charset internally into ranges and combined graphemes</li>
	 *     <li>If parsable: Checks that there are no surrogates contained</li>
	 *     <li>If parsable: Checks the length restriction that all graphemes have length <= {@link CharsetUtil#MAX_GRAPHEME_LENGTH}</li>
	 *     <li>If parsable: Checks the overlapping constraint, if there are concatenated graphemes in the charset</li>
	 * </ul>
	 * <p>
	 * All errors reported return the problematic index in the original charset in {@link IProblem#getSourceStart()} and {@link IProblem#getSourceEnd()}
	 * such that {@code restrictedCharset.get(problem.getSourceStart())} is the cause of the error.
	 *
	 * @param restrictedCharset
	 * 		the charset definition
	 * @param locale
	 * 		the locale in which possible error messages get localized for
	 * @param problemReporter
	 * 		the problem reporter receiving error messages.
	 */
	public static void validateCharset(final List<String> restrictedCharset,
			final Locale locale,
			final IProblemReporter problemReporter) {
		new CharsetUtil(locale).validate(restrictedCharset, problemReporter);
	}

	/**
	 * Simplifies a given restricted charset by:
	 * <ul>
	 *     <li>parsing ranges including single characters as ranges of length 1</li>
	 *     <li>sorting ranges ascending by interval start</li>
	 *     <li>merging ranges if they overlap or are directly neighboring each other</li>
	 *     <li>parsing all combined graphemes removing duplicates</li>
	 *     <li>return the concatenation of all ranges (collapsing ranges of length 1 back into one character)
	 *     and combined graphemes in ascending order</li>
	 * </ul>
	 * Therefore, a simplified charset has the following form:
	 * <ul>
	 *     <li>first come all single character and character ranges in ascending order</li>
	 *     <li>afterwards come all combined graphemes in ascending order</li>
	 * </ul>
	 * So there always exists a range or single character, after which all combined graphemes follow.
	 * This form should be canonical, so a simplified charset does not need to be simplified again.
	 *
	 * @param restrictedCharset
	 * 		the charset definition
	 * @param locale
	 * 		the locale in which possible error messages get localized for
	 * @param problemReporter
	 * 		the problem reporter receiving error messages.
	 * @return the simplified charset, or an empty list if the charset was not parseable
	 */
	public static List<String> simplifyCharset(final List<String> restrictedCharset,
			final Locale locale,
			final IProblemReporter problemReporter) {
		return new CharsetUtil(locale)
				.simplifyCharset(restrictedCharset, problemReporter)
				.orElse(Collections.emptyList());
	}

	/**
	 * Precondition: the {@code restrictedCharset} must be simplified (see
	 * {@link KernelUtils#simplifyCharset(List, Locale, IProblemReporter)}).
	 *
	 * @param value
	 * 		value which should be tested. Must not be {@code null}.
	 * @param restrictedCharset
	 * 		list of the supported characters (single characters, ranges and combining characters are allowed). Must not be
	 *        {@code null}.
	 * @return {@code true} if the given {@code value} contains only characters in the given
	 *        {@code restrictedCharset}, otherwise <code>false</code>.
	 */
	public static boolean isValidValue(String value, List<String> restrictedCharset) {
		if (restrictedCharset.isEmpty()) {
			return true;
		}
		return LegalCharTester.fromGraphemeSet(restrictedCharset)
				.map(matcher -> matcher.pruefeZeichensatz(value))
				.orElse(true);
	}
}
