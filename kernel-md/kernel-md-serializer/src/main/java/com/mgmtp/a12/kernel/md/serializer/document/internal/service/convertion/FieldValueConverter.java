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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service.convertion;

import static com.mgmtp.a12.kernel.md.model.internal.impl.RankedNotificationImpl.error;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.base.utils.internal.FieldTypeConstants;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentService;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IBooleanType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IConfirmType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ICustomFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType.InterpretationOfYear;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAndSearchService;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.utils.conversion.InstantRange;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV2;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateUtil;
import com.mgmtp.a12.utils.conversion.a12internal.util.NumberUtil;

import lombok.NonNull;

/**
 * Implements the functionality for
 * {@link IDocumentService#convertToJavaType(IFieldInstance, String, Consumer)}.
 * <p>
 * This converts field values between {@link BasicTypeV2} and the string representations of our json/xml serialization formats.
 */
public class FieldValueConverter {
	public static final int DEFAULT_BASE_YEAR = 2000;

	private static final String CANNOT_CONVERT_TO = "For the entity instance '%s', the given value is not valid for type %s and was not successfully deserialized.";
	private static final String FIELD_NOT_FOUND = "For the entity instance '%s', the corresponding entity is not a field.";
	private static final String FIELD_TYPE_RESOLUTION_NEEDED = "The field type of '%s' could not be resolved and that's why the corresponding value won't be converted.";
	private static final String TRUE_STRING = "true";
	private static final String FALSE_STRING = "false";
	private static final char A12_DECIMAL_SEPARATOR = '.';

	/**
	 * @param success
	 * 		true if the conversion was successful and convertedValueNullable is valid, false otherwise
	 * @param convertedValueNullable
	 * 		the conversion result as BasicTypeV2 value (possibly null), if success is true, otherwise
	 * 		undefined.
	 */
	public record ConversionResult(boolean success, Object convertedValueNullable) {
		public ConversionResult {
			assert BasicTypeV2.hasValidType(convertedValueNullable) : convertedValueNullable + " was not a valid BasicTypeV2 value";
		}

		public static ConversionResult success(Object convertedValueNullableBasicTypeV2) {
			return new ConversionResult(true, convertedValueNullableBasicTypeV2);
		}

		public static ConversionResult failure() {
			return new ConversionResult(false, null);
		}
	}

	public static @NonNull ConversionResult convertToJavaTypeV2(
			@NonNull String fieldFullName,
			/*nullable*/ String value,
			@NonNull DmAndSearchService dmSS,
			@NonNull Consumer<RankedNotification> notificationReceiver
	) {
		return convertToJavaTypeV2(fieldFullName, null, value, dmSS, notificationReceiver);
	}

	public static @NonNull ConversionResult convertToJavaTypeV2(
			@NonNull String fieldFullName,
			/*nullable*/ List<Integer> indexesOnlyRelevantForErrorReporting,
			/*nullable*/ String value,
			@NonNull DmAndSearchService dmSS,
			@NonNull Consumer<RankedNotification> notificationReceiver
	) {
		Optional<IElement> fieldOptional = dmSS.searchService().getByPath(fieldFullName);
		if (fieldOptional.isEmpty() || !ElementUtils.isField(fieldOptional.get())) {
			notificationReceiver.accept(error(
					String.format(FIELD_NOT_FOUND,
							indexesOnlyRelevantForErrorReporting != null ?
									DocumentPointer.of(fieldFullName, indexesOnlyRelevantForErrorReporting) :
									fieldFullName)));
			return ConversionResult.failure();
		}
		String timeZoneId = dmSS.dm().getContent().getDocumentModelConfig().getTimeZone();
		int baseYear = dmSS.dm().getContent().getDocumentModelInfo().getBaseYear().orElse(DEFAULT_BASE_YEAR);
		return convertToJavaTypeV2(fieldFullName, indexesOnlyRelevantForErrorReporting, (IField) fieldOptional.get(), value,
				baseYear, notificationReceiver, TimeZone.getTimeZone(timeZoneId));
	}

	public static @NonNull ConversionResult convertToJavaTypeV2(
			final String path,
			final List<Integer> indexesOnlyRelevantForErrorReporting,
			final IField field,
			final String value,
			final int baseYear,
			final Consumer<RankedNotification> notificationReceiver,
			TimeZone timeZone
	) {
		return convertToJavaTypeV2Internal(path, indexesOnlyRelevantForErrorReporting, field, value, baseYear, notificationReceiver,
				timeZone);
	}

	private static @NonNull ConversionResult convertToJavaTypeV2Internal(
			final String path,
			final List<Integer> indexesOnlyRelevantForErrorReporting,
			final IField field,
			String value,
			final int baseYear,
			final Consumer<RankedNotification> notificationReceiver,
			TimeZone timeZone
	) {
		final Optional<IFieldType> optionalType = field.getEffectiveType();
		if (optionalType.isEmpty()) {
			notificationReceiver.accept(error(String.format(FIELD_TYPE_RESOLUTION_NEEDED, path)));
			return ConversionResult.failure();
		}
		IFieldType type = optionalType.get();
		return switch (type) {
			case IBooleanType bType -> parseBoolean(path, indexesOnlyRelevantForErrorReporting, value, notificationReceiver);
			case IDateType dType ->
					parseDate(dType.getFormat(), dType.arePartiallyKnownDatesAllowed(), path, indexesOnlyRelevantForErrorReporting,
							value, baseYear,
							notificationReceiver, timeZone);
			case IDateFragmentType dfType ->
					parseDate(dfType.getFormatOfFragment(), false, path, indexesOnlyRelevantForErrorReporting, value, baseYear,
							notificationReceiver,
							timeZone);
			case INumberType numberType ->
					parseNumber(path, indexesOnlyRelevantForErrorReporting, value, numberType.isLeadingZerosAllowed(),
							notificationReceiver);
			case IStringType stringType -> ConversionResult.success(value);
			case IEnumerationType enumerationType -> ConversionResult.success(value);
			case IConfirmType confirmType -> parseConfirm(path, indexesOnlyRelevantForErrorReporting, value, notificationReceiver);
			case ICustomFieldType customFieldType -> ConversionResult.success(value);
			case IDateRangeType dateRange ->
					parseDateRange(dateRange, path, indexesOnlyRelevantForErrorReporting, value, baseYear, notificationReceiver,
							timeZone);
			default -> throw new IllegalArgumentException(
					"Encountered unknown field data type '" + type.getClass().getSimpleName() + "'.");
		};
	}

	/**
	 * This conversion logic must be similar (or even equal) to 'FormatDefinitionDatum#convertToBasicType'.
	 */
	private static @NonNull ConversionResult parseDate(final String dateFormat, final boolean arePartiallyKnownDatesAllowed,
			final String path,
			final List<Integer> indexesOnlyRelevantForErrorReporting, final String value, int baseYear,
			final Consumer<RankedNotification> notificationReceiver,
			final TimeZone timeZone) {
		// treat "" like null, convert both to null (without reporting an error for "", despite the non-standard representation)
		if (StringUtils.isEmpty(value)) {
			return ConversionResult.success(null);
		}

		try {
			Object converted = DateUtil.createDateIfPossible(value, dateFormat, baseYear, arePartiallyKnownDatesAllowed, timeZone);
			assert converted != null : "did not expect non-empty string to be converted to null date/time";
			return ConversionResult.success(BasicTypeV1V2Conversion.basicTypeV1ToV2(converted));
		} catch (RuntimeException e) {
			notificationReceiver.accept(error(String.format(CANNOT_CONVERT_TO,
					indexesOnlyRelevantForErrorReporting != null ?
							DocumentPointer.of(path, indexesOnlyRelevantForErrorReporting) :
							path, "date representation")));
			return ConversionResult.failure();
		}
	}

	/**
	 * This conversion logic must call the same logic as in 'FormatDefinitionDatumBereich#convertToBasicType'.
	 *
	 * @return a {@link ConversionResult} that contains a java.util.Date[] or null if successful
	 */
	private static @NonNull ConversionResult parseDateRange(IDateRangeType drType, final String path,
			final List<Integer> indexesOnlyRelevantForErrorReporting,
			final String value, int baseYear, final Consumer<RankedNotification> notificationReceiver, TimeZone timeZone) {
		// treat "" like null, convert both to null (without reporting an error for "", despite the non-standard representation)
		if (StringUtils.isEmpty(value)) {
			return ConversionResult.success(null);
		}

		try {
			com.mgmtp.a12.utils.conversion.InterpretationOfYear jint = getInterpretationOfYear(
					drType.getInterpretationOfYear().orElse(null));
			java.util.Date[] converted = DateUtil.createDateRange(value, drType.getRangeSeparator(), drType.getFormat(), jint,
					baseYear, true, timeZone);
			return ConversionResult.success(BasicTypeV1V2Conversion.basicTypeV1ToV2(converted));
		} catch (RuntimeException e) {
			notificationReceiver.accept(error(
					String.format(CANNOT_CONVERT_TO, indexesOnlyRelevantForErrorReporting != null ?
									DocumentPointer.of(path, indexesOnlyRelevantForErrorReporting) :
									path,
							"date range representation")));
			return ConversionResult.failure();
		}
	}

	private static com.mgmtp.a12.utils.conversion.InterpretationOfYear getInterpretationOfYear(
			InterpretationOfYear interpretationOfYear) {
		if (interpretationOfYear == InterpretationOfYear.FROM) {
			return com.mgmtp.a12.utils.conversion.InterpretationOfYear.FROM;
		}
		if (interpretationOfYear == InterpretationOfYear.TO) {
			return com.mgmtp.a12.utils.conversion.InterpretationOfYear.TO;
		}
		return com.mgmtp.a12.utils.conversion.InterpretationOfYear.STANDARD;
	}

	/**
	 * This conversion logic must call the same logic as in 'FormatDefinitionZahl#convertToBasicType(..)'.
	 */
	private static @NonNull ConversionResult parseNumber(final String path,
			final List<Integer> indexesOnlyRelevantForErrorReporting, final String value,
			final boolean leadingZerosAllowed,
			final Consumer<RankedNotification> notificationReceiver) {
		// treat "" like null, convert both to null (without reporting an error for "", despite the non-standard representation)
		if (StringUtils.isEmpty(value)) {
			return ConversionResult.success(null);
		}

		try {
			// we don't use the decimal separator from the document model, since that configuration is only to document
			// which decimal separator comes from ExtMP
			Object bigDecimalIfPossible = NumberUtil.createBigDecimalIfPossible(leadingZerosAllowed, value, A12_DECIMAL_SEPARATOR);
			assert bigDecimalIfPossible != null : "the result should never be null here because that should only be possible if value was empty, but we've already checked for that";
			return ConversionResult.success(bigDecimalIfPossible);
		} catch (final NumberFormatException nfe) {
			notificationReceiver.accept(error(
					String.format(CANNOT_CONVERT_TO, indexesOnlyRelevantForErrorReporting != null ?
							DocumentPointer.of(path, indexesOnlyRelevantForErrorReporting) :
							path, "number")));
			return ConversionResult.failure();
		}
	}

	/**
	 * @return a {@link ConversionResult} with Boolean (or null) value if successful
	 */
	private static @NonNull ConversionResult parseConfirm(final String path,
			final List<Integer> indexesOnlyRelevantForErrorReporting, final String value,
			final Consumer<RankedNotification> notificationReceiver) {
		// treat "" like null, convert both to null (without reporting an error for "", despite the non-standard representation)
		if (StringUtils.isEmpty(value)) {
			return ConversionResult.success(null);
		}
		if (TRUE_STRING.equalsIgnoreCase(value)) {
			return ConversionResult.success(Boolean.TRUE);
		} else {
			notificationReceiver.accept(error(
					String.format(CANNOT_CONVERT_TO, indexesOnlyRelevantForErrorReporting != null ?
							DocumentPointer.of(path, indexesOnlyRelevantForErrorReporting) :
							path, "confirm")));
			return ConversionResult.failure();
		}
	}

	/**
	 * @return a {@link ConversionResult} with Boolean (or null) value if successfull
	 */
	private static @NonNull ConversionResult parseBoolean(final String path,
			final List<Integer> indexesOnlyRelevantForErrorReporting, final String value,
			final Consumer<RankedNotification> notificationReceiver) {
		// treat "" like null, convert both to null (without reporting an error for "", despite the non-standard representation)
		if (StringUtils.isEmpty(value)) {
			return ConversionResult.success(null);
		}

		if (TRUE_STRING.equalsIgnoreCase(value)) {
			return ConversionResult.success(Boolean.TRUE);
		} else if (FALSE_STRING.equalsIgnoreCase(value)) {
			return ConversionResult.success(Boolean.FALSE);
		} else {
			notificationReceiver.accept(error(
					String.format(CANNOT_CONVERT_TO, indexesOnlyRelevantForErrorReporting != null ?
									DocumentPointer.of(path, indexesOnlyRelevantForErrorReporting) :
									path,
							"boolean")));
			return ConversionResult.failure();
		}
	}

	/**
	 * The value is used for xml values and is also used in json for Instant and InstantRange.
	 */
	public static String serializeXmlValueFromJavaTypeV2(final IField field, final TimeZone timeZone, final Object value) {
		return switch (BasicTypeV2.checkType(value)) {
			case String val -> val;
			case Boolean b -> b.toString();
			case BigDecimal d -> d.toString();
			case Instant i -> {
				String dateFormat = ElementUtils.getDateFormatOrThrow(field);
				yield serializeInstant(i, dateFormat, timeZone);
			}
			case InstantRange i -> {
				String dateFormat = ElementUtils.getDateFormatOrThrow(field);
				String rangeSeparator = ElementUtils.getDateRangeSeparator(field);
				yield serializeInstantRange(i,
						rangeSeparator != null ? rangeSeparator : FieldTypeConstants.A12_DATE_RANGE_SEPARATOR, dateFormat,
						timeZone);
			}
			case null -> null;
			// already checked by BasicTypeV2.checkType, should not occur
			default -> throw new IllegalArgumentException();
		};
	}

	public static String serializeInstant(final @NonNull Instant date, final @NonNull String dateFormatStr,
			@NonNull TimeZone timeZone) {
		Validate.notBlank(dateFormatStr);
		final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(dateFormatStr).withZone(timeZone.toZoneId());
		return dateFormat.format(date);
	}

	public static String serializeInstantRange(final InstantRange dates, final String separator, final String dateFormatStr,
			TimeZone timeZone) {
		return serializeInstant(dates.start(), dateFormatStr, timeZone) + separator + serializeInstant(dates.end(), dateFormatStr,
				timeZone);
	}
}
