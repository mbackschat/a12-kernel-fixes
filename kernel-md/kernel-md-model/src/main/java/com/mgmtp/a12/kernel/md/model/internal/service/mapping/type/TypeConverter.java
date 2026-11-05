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
package com.mgmtp.a12.kernel.md.model.internal.service.mapping.type;

import static com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType.UNDEFINED;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IBooleanType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IConfirmType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ICustomFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType.DatePrecision;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType.IEnumValue;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ITypeDefType;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.type.NumberTypeImpl.NumberTypeImplBuilder;

/**
 * Provides conversion functionality from {@link IDocumentModel}'s {@link IFieldType}s to
 * {@link com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType}s from {@link IEC}.
 */
public class TypeConverter {

	private static final String NUMBER_TYPE_TRAIT_AMOUNT = "amount";

	private final boolean considerNotInD;

	public TypeConverter(boolean considerNotInD) {
		this.considerNotInD = considerNotInD;
	}

	/**
	 * Converts a {@link IFieldType} to a {@link com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType}.
	 *
	 * @param fieldType
	 * 		the type from the {@link IDocumentModel}
	 * @param modelLocales
	 * 		the locales of the {@link IDocumentModel}
	 * @param typeDefs
	 * 		the type definitions from the {@link IDocumentModel} by id
	 * @param pr
	 * 		a {@link IProblemReporter}, an error is reported to, if a type definition cannot be resolved.
	 * @return the respective {@link IFieldType}
	 */
	public com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType convert(final IFieldType fieldType,
			final List<Locale> modelLocales,
			Map<String, IFieldTypeDefinition> typeDefs,
			IProblemReporter pr,
			final com.mgmtp.a12.kernel.md.model.api.IField field
	) {
		return switch (fieldType) {
			case IDateType dateType -> convertDate(dateType);
			case IDateFragmentType dateFragmentType -> convertDateFragment(dateFragmentType);
			case IEnumerationType enumerationType -> convertEnumeration(enumerationType, modelLocales);
			case INumberType numberType -> convertNumber(numberType);
			case IBooleanType booleanType -> convertBoolean(booleanType);
			case IConfirmType confirmType -> convertConfirm(confirmType);
			case IStringType stringType -> convertString(stringType);
			case ITypeDefType typeDefType -> {
				IFieldType effectiveFieldType = getEffectiveFieldType(typeDefType, typeDefs, pr, field);
				if (effectiveFieldType == null) {
					// The type definition could not be found — the problem was already reported via IProblemReporter.
					// Return a dummy StringType so that error collection can continue for the remaining fields.
					yield StringTypeImpl.builder().build();
				}
				yield convert(effectiveFieldType, modelLocales, typeDefs, pr, field);
			}
			case IDateRangeType dateRangeType -> convertDateRange(dateRangeType);
			case ICustomFieldType customFieldType -> convertCustomType(customFieldType);
			default ->
					throw new IllegalArgumentException("Cannot convert data type '" + fieldType.getClass().getSimpleName() + "'.");
		};
	}

	private IFieldType getEffectiveFieldType(ITypeDefType fieldType, Map<String, IFieldTypeDefinition> typeDefs,
			IProblemReporter pr, IField field) {
		var typeDefDmo = fieldType.getTypeDefinition();
		if (typeDefDmo.isPresent()) {
			return typeDefDmo.get().getFieldType();
		}
		// fallback: sometimes, we want to convert document models with imported type definitions whose references are not
		// resolved yet, but contained in the type-definition list in the document model
		IFieldTypeDefinition typeDef = typeDefs.get(fieldType.getId());
		if (typeDef == null) {
			pr.reportProblem(new MVKCoreError("Typedefinition with id '" + fieldType.getId() + "' could not be found.", field));
			return null;
		}
		return typeDef.getFieldType();
	}

	private PredefinedTypeImpl convertCustomType(IFieldType dataType) {
		ICustomFieldType cusType = (ICustomFieldType) dataType;
		var builder = PredefinedTypeImpl.builder().name(cusType.getName());
		cusType.getMinLength().ifPresent(builder::minLength);
		cusType.getMaxLength().ifPresent(builder::maxLength);
		return builder.build();
	}

	private DateRangeTypeImpl convertDateRange(final IDateRangeType dType) {
		return new DateRangeTypeImpl(
				considerNotInD
						? dType.getNotInDCustomFormat().orElse(dType.getFormat())
						: dType.getFormat(),
				considerNotInD
						? dType.getNotInDCustomRangeSeparator().orElse(dType.getRangeSeparator())
						: dType.getRangeSeparator(),
				dType.getInterpretationOfYear().orElse(null));
	}

	private DateTypeImpl convertDate(final IDateType dateType) {
		return new DateTypeImpl(
				considerNotInD
						? dateType.getNotInDCustomFormat().orElse(dateType.getFormat())
						: dateType.getFormat(),
				dateType.getDatePrecision());
	}

	private DateTypeImpl convertDateFragment(final IDateFragmentType dateFragmentType) {
		return new DateTypeImpl(
				considerNotInD
						? dateFragmentType.getNotInDCustomFormat().orElse(dateFragmentType.getFormatOfFragment())
						: dateFragmentType.getFormatOfFragment(),
				DatePrecision.FULL);
	}

	private EnumerationTypeImpl convertEnumeration(final IEnumerationType enumerationType,
			final List<Locale> modelLocales) {
		final List<String> xmlValues = new ArrayList<>();
		final Map<Locale, List<com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType.EnumerationValue>> values = new HashMap<>();
		Map<Locale, String> errorMessages = enumerationType.getErrorMessage();

		for (final IEnumValue enumValueType : enumerationType.getValues()) {
			xmlValues.add(enumValueType.getValue());

			if (!enumValueType.getLabel().isEmpty()) {
				for (final Locale locale : modelLocales) {
					addDisplayValueForLocale(values, locale, enumValueType.getValue(), enumValueType.getLabel());
				}
			}
		}

		final Map<String, List<String>> categoryValues = enumerationType.getCategories().stream()
				.collect(Collectors.toMap(IEnumerationType.IEnumCategory::getName, IEnumerationType.IEnumCategory::getValues));

		String sValue = enumerationType.getsValue().orElse("");

		return new EnumerationTypeImpl(errorMessages, values, categoryValues, xmlValues, sValue);
	}

	private NumberTypeImpl convertNumber(final INumberType numberType) {
		final NumberTypeImplBuilder resultBuilder = NumberTypeImpl.builder();
		resultBuilder.minFractionalDigits(numberType.getMinFractionalDigits());
		resultBuilder.maxFractionalDigits(numberType.getMaxFractionalDigits());

		boolean isAmount = numberType.getTrait().filter(NUMBER_TYPE_TRAIT_AMOUNT::equals).isPresent();
		if (isAmount) {
			resultBuilder.isAmount(true);
		}

		Optional<Integer> maxIntegerDigits = numberType.getMaxIntegerDigits();
		if (maxIntegerDigits.isPresent()) {
			resultBuilder.maxDigitsBeforeDecimalPlace(maxIntegerDigits.get());
		} else if (isAmount) {
			resultBuilder.maxDigitsBeforeDecimalPlace(
					15 - numberType.getMaxFractionalDigits()); // we set the old value here until we have done the ExtMP extension to use annotations
		}

		return resultBuilder.zeroAllowed(!numberType.isZeroNotAllowed())
				.minValue(numberType.getMinValue()
						.map(BigDecimal::doubleValue)
						.orElse(null))
				.maxValue(numberType.getMaxValue()
						.map(BigDecimal::doubleValue)
						.orElse(null))
				.hasSign(!numberType.isPositivesOnly())
				.minLength(numberType.getMinLength().orElse(null))
				.maxLength(numberType.getMaxLength().orElse(null))
				.leadingZerosAllowed(numberType.isLeadingZerosAllowed())
				.build();
	}

	private YesNoTypeImpl convertBoolean(IBooleanType booleanType) {
		return new YesNoTypeImpl(
				considerNotInD
						? booleanType.getNotInDCustomTrueValue().orElse("true")
						: "true",
				considerNotInD
						? booleanType.getNotInDCustomFalseValue().orElse("false")
						: "false");
	}

	private YesTypeImpl convertConfirm(IConfirmType confirmType) {
		return new YesTypeImpl(
				considerNotInD
						? confirmType.getNotInDCustomTrueValue().orElse("true")
						: "true");
	}

	private StringTypeImpl convertString(final IStringType stringType) {
		Map<Locale, List<String>> hintListMap = stringType.getHintList().stream()
				.filter(l -> l.getValues() != null)
				.filter(l -> !l.getValues().isEmpty())
				.collect(Collectors.toMap(
						IStringType.IHintList::getLocale,
						IStringType.IHintList::getValues
				));
		return StringTypeImpl.builder()
				.linebreaksPermitted(stringType.isLineBreaksPermitted())
				.minLength(stringType.getMinLength().orElse(UNDEFINED))
				.maxLength(stringType.getMaxLength().orElse(UNDEFINED))
				.pattern(stringType.getPattern().orElse(null))
				.errorMessages(stringType.getErrorMessage())
				.noValueValidation(stringType.isNoValueValidation())
				.hintLists(hintListMap)
				.build();
	}

	/**
	 * Adds the display values to the enumeration values.
	 *
	 * @param values
	 * 		enumeration values to which the display value is to be added
	 * @param locale
	 * 		locale for which the display value is to be added
	 * @param xmlValue
	 * 		xml value to which the display value belongs
	 * @param localizedTextMap
	 * 		localized map holding the display values
	 */
	private void addDisplayValueForLocale(
			final Map<Locale, List<com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType.EnumerationValue>> values,
			final Locale locale,
			final String xmlValue,
			final ILocalizedTextMap localizedTextMap) {
		String valueForLang = localizedTextMap.get(locale);
		values.computeIfAbsent(locale, k -> new ArrayList<>());
		values.get(locale).add(new EnumerationTypeImpl.EnumerationValueImpl(valueForLang, xmlValue));
	}
}
