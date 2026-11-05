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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.core;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionString;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IterationState;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtIdentifierTemplate;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkBigDecimal;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkString;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;

import lombok.NonNull;

public class DynamicEnumerationController extends RuntimeController {

	private static final String ERROR_IN_DYN_ENUM_VALUE = "Error in calculation of dynamic enumeration value!";

	public DynamicEnumerationController(
			@NonNull IResult ergebnisse,
			@NonNull ValidationCache valCache,
			@NonNull ValidationMode modus,
			@NonNull IMetaDataValidierung metaData
	) {
		super(ergebnisse, valCache, modus, metaData);
	}

	@Override
	protected void logNichtAusgewertet(String fehlermeldung) {
		throw new UnsupportedOperationException("The method should only be called in connection with USBs, "
				+ "which are not allowed for Dynamic Enumerations");
	}

	public List<Pair<String, String>> handleEnumerationValue(String value) {
		if (StringUtils.isEmpty(value)) {
			throw new ValidatorException(ERROR_IN_DYN_ENUM_VALUE);
		}
		return Collections.singletonList(new Pair<>(null, value));
	}

	public List<Pair<String, String>> handleEnumerationValue(VkString value) {
		if (!value.hasValue()) {
			throw new ValidatorException(ERROR_IN_DYN_ENUM_VALUE);
		}
		return Collections.singletonList(new Pair<>(null, value.getWert()));
	}

	public List<Pair<String, String>> handleEnumerationValue(VkBigDecimal value) {
		if (value.wertIstUngueltig()) {
			// We want to return an empty list for the overall result, not for this partial value
			throw new ValidatorException(ERROR_IN_DYN_ENUM_VALUE);
		} else {
			int minScale = value.getBigDecimalValue().stripTrailingZeros().scale();
			if (minScale > 0) {
				// If we have fractional digits, something went wrong.
				throw new ValidatorException(ERROR_IN_DYN_ENUM_VALUE);
			}
			String strWert = value.getBigDecimalValue().setScale(0, RoundingMode.UNNECESSARY).toPlainString();
			return Collections.singletonList(new Pair<>(null, strWert));
		}
	}

	public List<Pair<String, String>> handleEnumerationValue(List<Pair<String, String>> values) {
		return values;
	}

	/**
	 * @param index
	 * 		current iteration state
	 * @param entityList
	 * 		the one element list containing only the tested field.
	 * @param valueListAsMixedList
	 * 		the list of values. Possible types are {@link String} or
	 *        {@link RtIdentifierTemplate}. The latter will be dereferenced.
	 * @return the list of enumeration values as a {@link Pair} of UI- and XML-value.
	 */
	public List<Pair<String, String>> getUiXmlValuesForTableEnum(IterationState index, List<RtIdentifierTemplate> entityList,
			Object... valueListAsMixedList) {
		RtIdentifierTemplate entity = entityList.get(0);

		FormatDefinitionString fd = (FormatDefinitionString) metaData.getIFormatDefinition(entity.getUniqueName());
		List<String> valuesAsString = getMixedListAsStrings(index, valueListAsMixedList);
		if (valuesAsString.stream().anyMatch(StringUtils::isEmpty)) {
			return Collections.emptyList();
		}
		return fd.getOflXmlValuesForDependentColumnValues(valuesAsString);
	}

	/**
	 * @param inValues
	 * 		the values as returned by handleEnumerationValue.
	 * @param id
	 * 		the id of the field the values are for.
	 * @return the inValues with added UI values. For enumerations with UI-values (with and without
	 * 		table reference), the UI-values are added if not already present.
	 */
	public List<Pair<String, String>> addUIValues(List<Pair<String, String>> inValues, RtInternalIdentifier id) {
		// If the values are empty or already contain UI values give them back as they are
		if (inValues.isEmpty() || inValues.get(0).getEntry1() != null) {
			return inValues;
		}
		Set<String> xmlValues = inValues.stream()
				.map(Pair::getEntry2)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		IMetaField field = metaData.getMetaField(id.getName());
		if (!IMetaKeys.FIELD_TYPE_ENUMERATION.equals(field.getValue(IMetaKeys.FIELD_FIELD_TYPE))) {
			List<Pair<String, String>> result = new ArrayList<>();
			// The simple case
			for (String xmlValue : xmlValues) {
				result.add(new Pair<>(xmlValue, xmlValue));
			}
			return result;
		}
		return convertToUiXmlEnumValuePair(id, xmlValues, field);
	}

	private List<Pair<String, String>> convertToUiXmlEnumValuePair(RtInternalIdentifier id, Set<String> xmlValues,
			IMetaField field) {
		List<Pair<String, String>> result = new ArrayList<>();
		FormatDefinitionString formatDefinition = (FormatDefinitionString) metaData.getIFormatDefinition(id.getName());
		List<Pair<String, String>> enumValuesForLanguage = formatDefinition.getEnumOberflaechenWerte(this.modus.getLanguage());
		if (enumValuesForLanguage == null) {
			// We have no UI-values, so use the xml values of the enum for sorting
			@SuppressWarnings("unchecked")
			List<String> xmlEnumValues = (List<String>) field.getValue(IMetaKeys.FIELD_ENUMERATION_VALUES);
			for (String xmlEnumValue : xmlEnumValues) {
				if (xmlValues.contains(xmlEnumValue)) {
					result.add(new Pair<>(xmlEnumValue, xmlEnumValue));
				}
			}
		} else {
			for (Pair<String, String> enumValue : enumValuesForLanguage) {
				if (xmlValues.contains(enumValue.getEntry2())) {
					result.add(new Pair<>(enumValue.getEntry1(), enumValue.getEntry2()));
				}
			}
		}
		return result;
	}
}
