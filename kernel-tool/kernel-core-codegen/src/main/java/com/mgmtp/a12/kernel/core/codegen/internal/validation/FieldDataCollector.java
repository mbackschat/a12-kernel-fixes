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

package com.mgmtp.a12.kernel.core.codegen.internal.validation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.FieldUtils;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateRangeType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateRangeType.InterpretationOfYear;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType.EnumerationValue;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.INumberType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IPredefinedType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IYesNoType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IYesType;

/**
 * This class is in charge of gathering the data for the meta model check of a field and inserting
 * it to the given data.
 */
public class FieldDataCollector {
	private static final String PATTERN_FIELDS_GROUP = "/MVK[1]/Fields[%d]/";
	private static final String NAME = "name[1]";
	private static final String PATTERN_FIELD_NAME = PATTERN_FIELDS_GROUP + NAME;

	private static final String MAX_LENGTH = "maxLength[1]";
	private static final String MIN_LENGTH = "minLength[1]";
	private static final String ERROR_MESSAGES_PATTERN = "ErrorMessages[%d]/";

	private final IData data;
	private final Collection<Locale> usedLocales;
	private final Function<Locale, String> localeToLangStrFunction;

	public FieldDataCollector(IData data, Collection<Locale> usedLocales, Function<Locale, String> localeToLangStrFunction) {
		this.data = data;
		this.usedLocales = usedLocales;
		this.localeToLangStrFunction = localeToLangStrFunction;
	}

	public void addField(int idx, IField f) {
		String idPrefix = String.format(PATTERN_FIELDS_GROUP, idx);

		String group = EntityUtils.getParentPath(f.getFullName());
		String name = EntityUtils.getShortName(f.getFullName());
		long maxRows = EntityUtils.getMaxZeile(f);
		String type = extractFieldTypeName(f.getType());
		boolean globalField = f.isGlobal();
		boolean payloadField = !f.isTransient();

		addValue(idPrefix + "group[1]", group);
		addValue(idPrefix + NAME, name);
		addValue(idPrefix + "maxRows[1]", String.valueOf(maxRows));
		addValue(idPrefix + "type[1]", type);
		addRequirednessConfig(f, idPrefix);
		if (globalField) {
			addValue(idPrefix + "globalField[1]", Boolean.TRUE.toString());
		}
		if (payloadField) {
			addValue(idPrefix + "payloadField[1]", Boolean.TRUE.toString());
		}

		addFieldType(idPrefix, f.getType());

		DataCollectorUtils.addAnnotations(data, idPrefix, f.getAnnotations());
	}

	private void addRequirednessConfig(IField f, String idPrefix) {
		String reqConfigIdPrefix = idPrefix + "RequirednessConfig[1]/";
		String reqModeId = reqConfigIdPrefix + "mode[1]";
		Optional<IField.IRequirednessConfig> optReqConfig = f.getRequirednessConfig();
		if (optReqConfig.isPresent()) {
			IField.IRequirednessConfig reqConfig = optReqConfig.get();
			addValue(reqModeId, reqConfig.getMode().name());
			String reqErrorMsgIdPattern = reqConfigIdPrefix + ERROR_MESSAGES_PATTERN;
			AtomicInteger errorLangIdx = new AtomicInteger();
			usedLocales.stream()
					.filter(l -> reqConfig.getErrorMessage(l).isPresent())
					.forEach(
							l -> DataCollectorUtils.addErrorMessage(data,
									String.format(reqErrorMsgIdPattern, errorLangIdx.incrementAndGet()),
									localeToLangStrFunction.apply(l), reqConfig.getErrorMessage(l).get()));
		} else {
			addValue(reqModeId, "notRequired");
		}
	}

	/**
	 * The field name is set in the method {@link FieldDataCollector#addField(int, IField)}. In case
	 * of the need to change it, this method can be used.
	 * <p>
	 * For example: by mapping an eXML model to an uXML, the field types in the eXML model must be
	 * checked if they are applicable to the mapped uXML (host-based) model, in this case we need
	 * names which are correct: seven digits.
	 *
	 * @param idx
	 * 		index of the field for which the given name should be set.
	 * @param name
	 * 		field name to be set.
	 */
	public void setFieldName(int idx, String name) {
		String id = String.format(PATTERN_FIELD_NAME, idx);
		addValue(id, name);
	}

	private String extractFieldTypeName(IFieldType type) {
		if (type instanceof INumberType numType) {
			if (numType.isNumberTraitAmount()) {
				if (numType.getMaxFractionalDigits() > 0 || numType.getMinFractionalDigits() > 0) {
					return "GeldBetragMitCent";
				} else {
					return "GeldBetragOhneCent";
				}
			} else {
				return "Zahl";
			}
		} else if (type instanceof IPredefinedType) {
			return "Vordefiniert";
		} else {
			return FieldUtils.getFieldTypeName(type);
		}
	}

	private void addFieldType(String parentPrefix, IFieldType type) {
		String idPrefix = parentPrefix + "FieldType[1]/";

		if (type instanceof IDateRangeType dateRangeType) {
			addDateRangeType(idPrefix, dateRangeType);
		} else if (type instanceof IEnumerationType enumType) {
			addEnumerationType(idPrefix, enumType);
		} else if (type instanceof INumberType numberType) {
			addNumberType(idPrefix, numberType);
		} else if (type instanceof IStringType stringType && stringType.isExtensibleEnum()) {
			addExtensibleEnumType(idPrefix, stringType);
			addStringType(idPrefix, stringType);
		} else if (type instanceof IStringType stringType) {
			addStringType(idPrefix, stringType);
		} else if (type instanceof IYesType yesType) {
			addYesType(idPrefix, yesType);
		} else if (type instanceof IYesNoType yesNoType) {
			addYesNoType(idPrefix, yesNoType);
		} else if (type instanceof IPredefinedType predefinedType) {
			addPredefinedType(idPrefix, predefinedType);
		} else if (type instanceof IDateType dateType) {
			addDateType(idPrefix, dateType);
		} else {
			throw new IllegalArgumentException("Invalid field type " + type.getClass().getCanonicalName());
		}
	}

	private void addExtensibleEnumType(String parentPrefix, IStringType type) {
		String idPrefix = parentPrefix + "ExtensibleEnumerationType[1]/";
		int maxLength = type.getMaxLength();
		// This ExtMP-specific extensible enumeration only allows german hint lists.
		addValueCollection(idPrefix + "DisplayValues[%d]/displayValue", type.getHintList(Locale.GERMANY));

		// even maxLength is already handled in addStringType, they have different prefixes
		if (maxLength != -1) {
			addValue(idPrefix + "maxLength", String.valueOf(maxLength));
		}
	}

	private void addDateType(String parentPrefix, IDateType type) {
		String idPrefix = parentPrefix + "DateType[1]/";

		String format = type.getFormat();
		String partlyKnown = type.partlyKnownDate().name();

		addValue(idPrefix + "format[1]", format);
		addValue(idPrefix + "optionalDateType[1]", partlyKnown);
	}

	private void addPredefinedType(String parentPrefix, IPredefinedType type) {
		String idPrefix = parentPrefix + "PredefinedType[1]/";
		addValue(idPrefix + "codeName[1]", type.getName());

		String errorIdPattern = idPrefix + ERROR_MESSAGES_PATTERN;
		AtomicInteger errorLangIdx = new AtomicInteger();
		usedLocales.forEach(l -> DataCollectorUtils.addErrorMessage4PredefinedType(data,
				String.format(errorIdPattern, errorLangIdx.incrementAndGet()), localeToLangStrFunction.apply(l),
				type.getErrorMessage(l)));

		String regex = type.getPattern().orElseGet(() -> null);
		addValue(idPrefix + "regex[1]", regex);

		int minLength = type.getMinLength();
		int maxLength = type.getMaxLength();
		if (minLength != -1) {
			addValue(idPrefix + MIN_LENGTH, String.valueOf(minLength));
		}
		if (maxLength != -1) {
			addValue(idPrefix + MAX_LENGTH, String.valueOf(maxLength));
		}
	}

	private void addYesType(String parentPrefix, IYesType type) {
		addValue(parentPrefix + "YesNoType[1]/yesValue[1]", type.getTrueValue());
	}

	private void addYesNoType(String parentPrefix, IYesNoType type) {
		addValue(parentPrefix + "YesNoType[1]/yesValue[1]", type.getTrueValue());
		addValue(parentPrefix + "YesNoType[1]/noValue[1]", type.getFalseValue());
	}

	private void addStringType(String parentPrefix, IStringType type) {
		String idPrefix = parentPrefix + "StringType[1]/";

		String pattern = type.getPattern();
		boolean lineBreakPermitted = type.hasLineBreaksPermitted();
		boolean noValueValidation = type.isNoValueValidation();

		addValue(idPrefix + "PatternProperty[1]/pattern[1]", pattern);
		if (lineBreakPermitted) {
			addValue(idPrefix + "linebreaksPermitted[1]", Boolean.TRUE.toString());
		}
		if (noValueValidation) {
			addValue(idPrefix + "noValueValidation[1]", Boolean.TRUE.toString());
		}

		int minLength = type.getMinLength();
		int maxLength = type.getMaxLength();
		if (minLength != -1) {
			addValue(idPrefix + MIN_LENGTH, String.valueOf(minLength));
		}
		if (maxLength != -1) {
			addValue(idPrefix + MAX_LENGTH, String.valueOf(maxLength));
		}

		String errorIdPattern = idPrefix + ERROR_MESSAGES_PATTERN;
		AtomicInteger langIdx = new AtomicInteger();
		AtomicInteger hintListIdx = new AtomicInteger();
		String hintListPattern = idPrefix + "HintLists[%d]/";
		usedLocales.forEach(l -> {
					DataCollectorUtils.addErrorMessage(data, String.format(errorIdPattern, langIdx.incrementAndGet()),
							localeToLangStrFunction.apply(l), type.getErrorMessage(l));
					List<String> hintList = type.getHintList(l);
					if (hintList != null && !hintList.isEmpty()) {
						DataCollectorUtils.addHintList(data, String.format(hintListPattern, hintListIdx.incrementAndGet()),
								localeToLangStrFunction.apply(l), hintList);
					}
				}
		);
	}

	private void addNumberType(String parentPrefix, INumberType type) {
		String idPrefix = parentPrefix + "NumberType[1]/";

		// Values in NumberType
		Optional<Integer> predecimalPlaces = type.getMaxNumberOfDigitsBeforeDecimalPlace();
		int fractionalDigits = type.getMinFractionalDigits();
		int maxFractionalDigits = type.getMaxFractionalDigits();
		Optional<Double> minValue = type.getMinValue();
		Optional<Double> maxValue = type.getMaxValue();
		boolean signMode = type.hasSign();
		boolean leadingZerosAllowed = type.leadingZerosAllowed();
		boolean zeroAllowed = type.isZeroAllowed();

		if (predecimalPlaces.isPresent()) {
			addValue(idPrefix + "predecimalPlaces[1]", String.valueOf(predecimalPlaces.get()));
		}
		if (fractionalDigits != -1) {
			addValue(idPrefix + "fractionalDigits[1]", String.valueOf(fractionalDigits));
		}
		if (maxFractionalDigits != -1) {
			addValue(idPrefix + "maxFractionalDigits[1]", String.valueOf(maxFractionalDigits));
		}
		if (minValue.isPresent()) {
			addValue(idPrefix + "minimalValue[1]", convertToPrintable(fractionalDigits, maxFractionalDigits, minValue.get()));
		}
		if (maxValue.isPresent()) {
			addValue(idPrefix + "maximalValue[1]", convertToPrintable(fractionalDigits, maxFractionalDigits, maxValue.get()));
		}
		if (signMode) {
			addValue(idPrefix + "signMode[1]", Boolean.TRUE.toString());
		}
		if (leadingZerosAllowed) {
			addValue(idPrefix + "leadingZerosAllowed[1]", Boolean.TRUE.toString());
		}
		if (zeroAllowed) {
			addValue(idPrefix + "zeroAllowed[1]", Boolean.TRUE.toString());
		}

		// Values in parent
		Optional<Integer> minLength = type.getMinLength();
		Optional<Integer> maxLength = type.getMaxLength();

		if (minLength.isPresent()) {
			addValue(idPrefix + MIN_LENGTH, String.valueOf(minLength.get()));
		}
		if (maxLength.isPresent()) {
			addValue(idPrefix + MAX_LENGTH, String.valueOf(maxLength.get()));
		}
	}

	private String convertToPrintable(int nachkomma, int nachkommaBis, Double d) {
		if (nachkomma == nachkommaBis && nachkomma > -1) {
			return String.format(Locale.US, "%." + nachkomma + "f", d);
		} else {
			return new BigDecimal(d.toString()).stripTrailingZeros().toPlainString();
		}
	}

	private void addEnumerationType(String parentPrefix, IEnumerationType type) {
		String idPrefix = parentPrefix + "EnumerationType[1]/";

		List<String> xmlValues = type.getXMLValues();
		addValueCollection(idPrefix + "InternalValues[%d]/internalValue[1]", xmlValues);

		Map<String, List<String>> categoryValues = type.getCategoryValues();
		addCategories(idPrefix, categoryValues, xmlValues);

		String idPattern = idPrefix + "ValuesPerLanguage[%d]/";
		AtomicInteger langIdx = new AtomicInteger();
		usedLocales.stream()
				.filter(l -> type.getDisplayValues(l) != null)
				.forEach(l -> addValuesPerLanguage(String.format(idPattern, langIdx.incrementAndGet()), type.getDisplayValues(l),
						l));

		addValue(idPrefix + "sValue[1]", type.getsValue());

		String errorIdPattern = idPrefix + ERROR_MESSAGES_PATTERN;
		AtomicInteger errorLangIdx = new AtomicInteger();
		usedLocales.forEach(
				l -> DataCollectorUtils.addErrorMessage(data, String.format(errorIdPattern, errorLangIdx.incrementAndGet()),
						localeToLangStrFunction.apply(l), type.getErrorMessage(l)));

		type.getTableName().ifPresent(t -> addValue(idPrefix + "tableName[1]", t));
		type.getColumnName().ifPresent(c -> addValue(idPrefix + "columnName[1]", c));
		addValueCollection(idPrefix + "DependentColumns[%d]/columnName[1]", type.getDependentColumns());
	}

	private void addCategories(String parentPrefix, Map<String, List<String>> categoryValues, List<String> xmlValues) {
		if (categoryValues == null || categoryValues.isEmpty()) {
			return;
		}
		String idPrefixPattern = parentPrefix + "Categories[%d]/";
		AtomicInteger idx = new AtomicInteger();
		categoryValues.entrySet().forEach(e -> {
			String idPrefix = String.format(idPrefixPattern, idx.incrementAndGet());
			addValue(idPrefix + NAME, e.getKey());
			addValueCollection(idPrefix + "Values[%d]/internalValue[1]", xmlValues);
			addValueCollection(idPrefix + "Values[%d]/categoryValue[1]", e.getValue());
		});
	}

	private void addValuesPerLanguage(String parentPrefix, List<EnumerationValue> displayValues, Locale locale) {
		addValue(parentPrefix + "language", localeToLangStrFunction.apply(locale));

		String idPattern = parentPrefix + "Values[%d]/";
		AtomicInteger idx = new AtomicInteger();
		displayValues.forEach(dv -> addDisplayValue(String.format(idPattern, idx.incrementAndGet()), dv));
	}

	private void addDisplayValue(String parentPrefix, EnumerationValue dv) {
		addValue(parentPrefix + "internalValue[1]", dv.getXmlValue());
		addValue(parentPrefix + "displayValue[1]", dv.getDisplayValue());
	}

	private void addDateRangeType(String parentPrefix, IDateRangeType type) {
		String idPrefix = parentPrefix + "DateType[1]/";

		InterpretationOfYear interpretationOfYear = type.getInterpretationOfYear();
		String separator = type.getSeparator();
		String format = type.getFormatOfDate() + separator + type.getFormatOfDate();

		addValue(idPrefix + "formatDateRange[1]", format);
		addValue(idPrefix + "interpretationOfYear[1]", mapInterpretationOfYear(interpretationOfYear));
		addValue(idPrefix + "dateRangeSeparator[1]", separator);
	}

	private String mapInterpretationOfYear(InterpretationOfYear interpretationOfYear) {
		switch (interpretationOfYear) {
			case STANDARD:
				return "standard";
			case PREVIOUS:
				return "from";
			case SUBSEQUENT:
				return "to";
			default:
				throw new IllegalArgumentException("Unknown interpretation of year " + interpretationOfYear);
		}
	}

	private void addValue(String id, String value) {
		DataCollectorUtils.addValue(data, id, value);
	}

	private void addValueCollection(String idPattern, Collection<String> values) {
		DataCollectorUtils.addValueCollection(data, idPattern, values);
	}

}
