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

package com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency;

import static com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation.TRAIT_AMOUNT;

import java.math.BigDecimal;
import java.util.Locale;

import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.BooleanType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.ConfirmType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.CustomFieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateFragmentType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateRangeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateTimeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.NumberType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TimeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.UnspecifiedType;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.internal.service.Identifier;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12.views._ct_field_type_attributes_1.Data;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12.views._ct_field_type_attributes_1._data.dateFormat;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12.views._ct_field_type_attributes_1._data.dateRangeFormat;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12.views._ct_field_type_attributes_1._data.falseValue;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12.views._ct_field_type_attributes_1._data.interpretationOfYear;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12.views._ct_field_type_attributes_1._data.optionalDateType;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12.views._ct_field_type_attributes_1._data.trueValue;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12.views._ct_field_type_attributes_1._data.type;
import com.mgmtp.a12.kernel.mmvalidators.extmpinternal.codegen_mm_smm_fieldtypechecks__0031.Factory_codegen_mm_smm_fieldtypechecks__0031;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_smm_fieldtypechecks_1_a12.views.MM_SMM_FieldTypeChecks_1;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_smm_fieldtypechecks_1_a12.views._mm_smm_fieldtypechecks_1._smm_fieldtypechecks.Source;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_smm_fieldtypechecks_1_a12.views._mm_smm_fieldtypechecks_1._smm_fieldtypechecks.Target;

import lombok.NonNull;

public class SMMFieldTypeCheckUtils {
	private static final String META_MODEL_CODEGEN_DM_NAME = "extmpinternal/codegen-mm_smm_fieldtypechecks-1.json";

	private SMMFieldTypeCheckUtils() {
		throw new UnsupportedOperationException("Utility Class");
	}

	public static boolean isFieldTypeAssignable(
			@NonNull FieldType sourceFieldType,
			@NonNull FieldType targetFieldType
	) {
		return checkFieldTypeAssignability("/irrelevant/source", sourceFieldType, "/irrelevant/target", targetFieldType,
				Locale.US).noErrorOccurred();
	}

	public static IDocumentValidationResult checkFieldTypeAssignability(
			@NonNull String sourceFieldFullName,
			@NonNull FieldType sourceFieldType,
			@NonNull String targetFieldFullName,
			@NonNull FieldType targetFieldType,
			@NonNull Locale locale
	) {
		Source sourceData = Source._viewOf(extractFieldTypeAttributes(sourceFieldFullName, sourceFieldType)._unwrap());
		Target targetData = Target._viewOf(extractFieldTypeAttributes(targetFieldFullName, targetFieldType)._unwrap());
		MM_SMM_FieldTypeChecks_1 doc = MM_SMM_FieldTypeChecks_1._empty()
				._with(MM_SMM_FieldTypeChecks_1._pointer().sMM_FieldTypeChecks().get(1).source(), sourceData)
				._with(MM_SMM_FieldTypeChecks_1._pointer().sMM_FieldTypeChecks().get(1).target(), targetData);
		return validate(doc, locale);
	}

	static Data extractFieldTypeAttributes(@NonNull String fieldFullName, @NonNull FieldType fieldType) {
		Data data = Data._empty()._with(Data._pointer().name(), fieldFullName);
		switch (fieldType) {
			case BooleanType booleanType -> {
				return data
						._with(Data._pointer().type(), type.Escaped_Boolean)
						._with(Data._pointer().trueValue(), booleanType.getNotInDCustomTrueValue()
								.map(trueValue::_of).orElse(trueValue.Escaped_true))
						._with(Data._pointer().falseValue(), booleanType.getNotInDCustomFalseValue()
								.map(falseValue::_of).orElse(falseValue.Escaped_false));
			}
			case ConfirmType confirmType -> {
				return data
						._with(Data._pointer().type(), type.Confirm)
						._with(Data._pointer().trueValue(), confirmType.getNotInDCustomTrueValue()
								.map(trueValue::_of).orElse(trueValue.Escaped_true));
			}
			case CustomFieldType customFieldType -> {
				return data
						._with(Data._pointer().type(), type.CustomFieldType)
						._with(Data._pointer().codeName(), customFieldType.getName())
						._with(Data._pointer().minLength(), customFieldType.getMinLength().map(BigDecimal::valueOf).orElse(null))
						._with(Data._pointer().maxLength(), customFieldType.getMaxLength().map(BigDecimal::valueOf).orElse(null));
			}
			case DateFragmentType dateFragmentType -> {
				return data
						._with(Data._pointer().type(), type.DateFragment)
						._with(Data._pointer().dateFormat(), dateFormat._of(sanitizeDateFormat(
								dateFragmentType.getNotInDCustomFormat().orElse(dateFragmentType.getFormatOfFragment()))))
						._with(Data._pointer().younger1900(), dateFragmentType.isYoungerThan1900Check() ? true : null);
			}
			case DateRangeType dateRangeType -> {
				String dateFormat = sanitizeDateFormat(dateRangeType.getNotInDCustomFormat().orElse(dateRangeType.getFormat()));
				String separator = dateRangeType.getNotInDCustomRangeSeparator().orElse(dateRangeType.getRangeSeparator());
				return data
						._with(Data._pointer().type(), type.DateRange)
						._with(Data._pointer().dateRangeFormat(), dateRangeFormat._of(dateFormat + separator + dateFormat))
						._with(Data._pointer().younger1900(), dateRangeType.isYoungerThan1900Check() ? true : null)
						._with(Data._pointer().interpretationOfYear(), dateRangeType.getInterpretationOfYear()
								.map(ioy -> switch (ioy) {
									case FROM -> interpretationOfYear.from;
									case TO -> interpretationOfYear.to;
								}).orElse(interpretationOfYear.standard));
			}
			case DateTimeType dateTimeType -> {
				return data
						._with(Data._pointer().type(), type.DateTime)
						._with(Data._pointer().dateFormat(), dateFormat._of(sanitizeDateFormat(dateTimeType.getFormat())));
			}
			case DateType dateType -> {
				return data
						._with(Data._pointer().type(), type.Date)
						._with(Data._pointer().dateFormat(),
								dateFormat._of(sanitizeDateFormat(dateType.getNotInDCustomFormat().orElse(dateType.getFormat()))))
						._with(Data._pointer().younger1900(), dateType.isYoungerThan1900Check() ? true : null)
						._with(Data._pointer().optionalDateType(), switch (dateType.getDatePrecision()) {
							case FULL -> optionalDateType.FULL;
							case DAY_OPTIONAL -> optionalDateType.DAY_OPTIONAL;
							case MONTH_OPTIONAL -> optionalDateType.MONTH_OPTIONAL;
							case YEAR_OPTIONAL -> optionalDateType.YEAR_OPTIONAL;
						});
			}
			case EnumerationType enumerationType -> {
				return data
						._with(Data._pointer().type(), type.Enumeration);
				// Note: enum values are deliberately not checked and hence not part of the extracted field type attributes
			}
			case NumberType numberType -> {
				return data
						._with(Data._pointer().type(), numberType.getTrait()
								.filter(TRAIT_AMOUNT::equals)
								.map(t -> numberType.getMaxFractionalDigits() > 0
										? type.GeldBetragMitCent
										: type.GeldBetragOhneCent)
								.orElse(type.Number))
						._with(Data._pointer().minFractionalDigits(), BigDecimal.valueOf(numberType.getMinFractionalDigits()))
						._with(Data._pointer().maxFractionalDigits(), BigDecimal.valueOf(numberType.getMaxFractionalDigits()))
						._with(Data._pointer().minimalValue(), numberType.getMinValue().orElse(null))
						._with(Data._pointer().maximalValue(), numberType.getMaxValue().orElse(null))
						._with(Data._pointer().zeroAllowed(), numberType.isZeroNotAllowed() ? null : true)
						._with(Data._pointer().positivesOnly(), numberType.isPositivesOnly() ? true : null)
						._with(Data._pointer().minLength(), numberType.getMinLength().map(BigDecimal::valueOf).orElse(null))
						._with(Data._pointer().maxLength(), numberType.getMaxLength().map(BigDecimal::valueOf).orElse(null))
						._with(Data._pointer().predecimalPlaces(), numberType.getMaxIntegerDigits()
								.map(BigDecimal::valueOf).orElse(null))
						._with(Data._pointer().leadingZerosAllowed(), numberType.isLeadingZerosAllowed() ? true : null);
			}
			case StringType stringType -> {
				return data
						._with(Data._pointer().type(),
								stringType.getHintList().isEmpty() ? type.String : type.ExtensibleEnumeration)
						._with(Data._pointer().minLength(), stringType.getMinLength().map(BigDecimal::valueOf).orElse(null))
						._with(Data._pointer().maxLength(), stringType.getMaxLength().map(BigDecimal::valueOf).orElse(null))
						._with(Data._pointer().linebreaksPermitted(), stringType.isLineBreaksPermitted() ? true : null)
						._with(Data._pointer().noValueValidation(), stringType.isNoValueValidation() ? true : null)
						._with(Data._pointer().pattern(), stringType.getPattern().orElse(null));
			}
			case TimeType timeType -> {
				return data
						._with(Data._pointer().type(), type.Time)
						._with(Data._pointer().dateFormat(), dateFormat._of(timeType.getFormat()));
			}
			case TypeDefType typeDefType -> throw new IllegalArgumentException(
					"Encountered an unexpanded type definition (%s). The Document Model must be expanded."
							.formatted(typeDefType.getId()));
			case UnspecifiedType unspecifiedType -> {
				return data
						._with(Data._pointer().type(), type.Unspecified);
			}
			default -> throw new IllegalArgumentException(
					"Cannot check field type of unknown field type '%s'".formatted(fieldType.getClass().getSimpleName()));
		}
	}

	private static String sanitizeDateFormat(String dateFormat) {
		// There appears to be a mismatch between the date formats used in the DM and the date formats used in the metamodel where
		// the latter uses uppercase for the year and day components, whereas the former uses lowercase. For example the DM uses
		// 'yyyy-MM-dd' where the metamodel would use 'YYYY-MM-DD'.
		return dateFormat.replace('y', 'Y').replace('d', 'D');
	}

	private static IDocumentValidationResult validate(@NonNull MM_SMM_FieldTypeChecks_1 document, @NonNull Locale locale) {
		// we only ever create the document manually, we already have exactly the codegen-variant structure
		return LowKernelApiUtils.callValidator(
				LowKernelApiUtils.ValidationMethod.FULL_VALIDATION,
				Factory_codegen_mm_smm_fieldtypechecks__0031.getInstance().getValidator(),
				Factory_codegen_mm_smm_fieldtypechecks__0031.getInstance().getMetaObject(),
				document._unwrap(),
				null,
				DocumentProcessingConfig.builder(locale).build(),
				id -> new Identifier(id.getName(), id.getIndices()).toString());
	}
}
