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
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.internal.service.Identifier;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12types.views._ct_field_type_attributes_1.Data;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12types.typedefs.DATE_FORMAT_2.DATE_FORMAT_2;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12types.typedefs.DATE_RANGE_FORMAT_2.DATE_RANGE_FORMAT_2;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12types.typedefs.FALSE_VALUE_2.FALSE_VALUE_2;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12types.typedefs.INTERPRETATION_OF_YEAR_2.INTERPRETATION_OF_YEAR_2;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12types.typedefs.OPTIONAL_DATE_TYPE_1.OPTIONAL_DATE_TYPE_1;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12types.typedefs.TRUE_VALUE_2.TRUE_VALUE_2;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.ct_field_type_attributes_1_a12types.views._ct_field_type_attributes_1._data.Type;
import com.mgmtp.a12.kernel.mmvalidators.extmpinternal.codegen.mm_smm_fieldtypechecks__0031.Factory_mm_smm_fieldtypechecks__0031;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_smm_fieldtypechecks_1_a12.views.MM_SMM_FieldTypeChecks_1;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_smm_fieldtypechecks_1_a12.views._mm_smm_fieldtypechecks_1._smm_fieldtypechecks.Source;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_smm_fieldtypechecks_1_a12.views._mm_smm_fieldtypechecks_1._smm_fieldtypechecks.Target;

import lombok.NonNull;

public class SMMFieldTypeCheckUtils {
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
						._with(Data._pointer().type(), Type.Escaped_Boolean)
						._with(Data._pointer().trueValue(), booleanType.getNotInDCustomTrueValue()
								.map(TRUE_VALUE_2::_of).orElse(TRUE_VALUE_2.Escaped_true))
						._with(Data._pointer().falseValue(), booleanType.getNotInDCustomFalseValue()
								.map(FALSE_VALUE_2::_of).orElse(FALSE_VALUE_2.Escaped_false));
			}
			case ConfirmType confirmType -> {
				return data
						._with(Data._pointer().type(), Type.Confirm)
						._with(Data._pointer().trueValue(), confirmType.getNotInDCustomTrueValue()
								.map(TRUE_VALUE_2::_of).orElse(TRUE_VALUE_2.Escaped_true));
			}
			case CustomFieldType customFieldType -> {
				return data
						._with(Data._pointer().type(), Type.CustomFieldType)
						._with(Data._pointer().codeName(), customFieldType.getName())
						._with(Data._pointer().minLength(), customFieldType.getMinLength().map(BigDecimal::valueOf).orElse(null))
						._with(Data._pointer().maxLength(), customFieldType.getMaxLength().map(BigDecimal::valueOf).orElse(null));
			}
			case DateFragmentType dateFragmentType -> {
				return data
						._with(Data._pointer().type(), Type.DateFragment)
						._with(Data._pointer().dateFormat(), DATE_FORMAT_2._of(dateFragmentType.getNotInDCustomFormat().orElse(dateFragmentType.getFormatOfFragment())));
			}
			case DateRangeType dateRangeType -> {
				String dateFormat = dateRangeType.getNotInDCustomFormat().orElse(dateRangeType.getFormat());
				String separator = dateRangeType.getNotInDCustomRangeSeparator().orElse(dateRangeType.getRangeSeparator());
				return data
						._with(Data._pointer().type(), Type.DateRange)
						._with(Data._pointer().dateRangeFormat(), DATE_RANGE_FORMAT_2._of(dateFormat + separator + dateFormat))
						._with(Data._pointer().interpretationOfYear(), dateRangeType.getInterpretationOfYear()
								.map(ioy -> switch (ioy) {
									case FROM -> INTERPRETATION_OF_YEAR_2.from;
									case TO -> INTERPRETATION_OF_YEAR_2.to;
								}).orElse(INTERPRETATION_OF_YEAR_2.standard));
			}
			case DateTimeType dateTimeType -> {
				return data
						._with(Data._pointer().type(), Type.DateTime)
						._with(Data._pointer().dateFormat(), DATE_FORMAT_2._of(dateTimeType.getFormat()));
			}
			case DateType dateType -> {
				return data
						._with(Data._pointer().type(), Type.Date)
						._with(Data._pointer().dateFormat(),
								DATE_FORMAT_2._of(dateType.getNotInDCustomFormat().orElse(dateType.getFormat())))
						._with(Data._pointer().optionalDateType(), switch (dateType.getDatePrecision()) {
							case FULL -> OPTIONAL_DATE_TYPE_1.FULL;
							case DAY_OPTIONAL -> OPTIONAL_DATE_TYPE_1.DAY_OPTIONAL;
							case MONTH_OPTIONAL -> OPTIONAL_DATE_TYPE_1.MONTH_OPTIONAL;
							case YEAR_OPTIONAL -> OPTIONAL_DATE_TYPE_1.YEAR_OPTIONAL;
						});
			}
			case EnumerationType enumerationType -> {
				return data
						._with(Data._pointer().type(), Type.Enumeration);
				// Note: enum values are deliberately not checked and hence not part of the extracted field type attributes
			}
			case NumberType numberType -> {
				return data
						._with(Data._pointer().type(), numberType.getTrait()
								.filter(TRAIT_AMOUNT::equals)
								.map(t -> numberType.getMaxFractionalDigits() > 0
										? Type.GeldBetragMitCent
										: Type.GeldBetragOhneCent)
								.orElse(Type.Number))
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
								stringType.getHintList().isEmpty() ? Type.String : Type.ExtensibleEnumeration)
						._with(Data._pointer().minLength(), stringType.getMinLength().map(BigDecimal::valueOf).orElse(null))
						._with(Data._pointer().maxLength(), stringType.getMaxLength().map(BigDecimal::valueOf).orElse(null))
						._with(Data._pointer().linebreaksPermitted(), stringType.isLineBreaksPermitted() ? true : null)
						._with(Data._pointer().noValueValidation(), stringType.isNoValueValidation() ? true : null)
						._with(Data._pointer().pattern(), stringType.getPattern().orElse(null));
			}
			case TimeType timeType -> {
				return data
						._with(Data._pointer().type(), Type.Time)
						._with(Data._pointer().dateFormat(), DATE_FORMAT_2._of(timeType.getFormat()));
			}
			case TypeDefType typeDefType -> throw new IllegalArgumentException(
					"Encountered an unexpanded type definition (%s). The Document Model must be expanded."
							.formatted(typeDefType.getId()));
			default -> throw new IllegalArgumentException(
					"Cannot check field type of unknown field type '%s'".formatted(fieldType.getClass().getSimpleName()));
		}
	}

	private static IDocumentValidationResult validate(@NonNull MM_SMM_FieldTypeChecks_1 document, @NonNull Locale locale) {
		// we only ever create the document manually, we already have exactly the codegen-variant structure
		return LowKernelApiUtils.callValidator(
				LowKernelApiUtils.ValidationMethod.FULL_VALIDATION,
				Factory_mm_smm_fieldtypechecks__0031.getInstance().getValidator(),
				Factory_mm_smm_fieldtypechecks__0031.getInstance().getMetaObject(),
				document._unwrap(),
				null,
				DocumentProcessingConfig.builder(locale).build(),
				id -> new Identifier(id.getName(), id.getIndices()).toString());
	}
}
