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

package com.mgmtp.a12.kernel.md.model.migration.internal.json.step;

import static com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils.MAPPER;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public final class FillGapsInEnumDisplayValuesStep {
	private static final String VALUE = "value";
	private static final String LABEL = "label";
	private static final String LOCALE = "locale";
	private static final String TEXT = "text";

	private FillGapsInEnumDisplayValuesStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, ICustomMigrationConfig customMigConfig) {
		List<JsonNode> enumTypeNodes = model.findValues("EnumerationType");
		for (JsonNode enumType : enumTypeNodes) {
			JsonNode values = enumType.path(ModelJsonUtil.ENUMERATION_VALUES);
			checkEnumValues(values);

			List<String> localesInModel = getLocales(model);
			ArrayNode newValues = hasDisplayValueGaps(values, localesInModel) ?
					fillGaps(values, localesInModel) :
					(ArrayNode) values;
			((ObjectNode) enumType).replace(ModelJsonUtil.ENUMERATION_VALUES, newValues);
		}
	}

	private static void checkEnumValues(JsonNode values) {
		if (values.isMissingNode() || !values.isArray()) {
			throw MigrationUtils.contentNotSupportedException(
					"The document model contains an enum field without 'values' or the existing 'values' are not a json array.");
		}
	}

	private static boolean hasDisplayValueGaps(JsonNode values, List<String> localesInModel) {
		boolean hasAtLeastOneDisplayValue = false;
		boolean isAtLeastOneDisplayValueMissing = false;

		for (JsonNode value : values) {
			JsonNode labels = value.get(LABEL);

			if (labels == null) {
				isAtLeastOneDisplayValueMissing = true;
				continue;
			}
			for (String locale : localesInModel) {
				JsonNode labelNodeForLocale = getLabelNodeForLocale(labels, locale);

				if (labelNodeForLocale == null) {
					isAtLeastOneDisplayValueMissing = true;
				} else {
					JsonNode textJsonNode = labelNodeForLocale.path(TEXT);
					if (textJsonNode.isMissingNode() || textJsonNode.asString().isEmpty()) {
						isAtLeastOneDisplayValueMissing = true;
					} else {
						hasAtLeastOneDisplayValue = true;
					}
				}
			}
		}
		return hasAtLeastOneDisplayValue && isAtLeastOneDisplayValueMissing;
	}

	private static ArrayNode fillGaps(JsonNode values, List<String> localesInModel) {
		Iterator<JsonNode> valueIt = values.iterator();
		ArrayNode newValues = MAPPER.createArrayNode();
		while (valueIt.hasNext()) {
			JsonNode value = valueIt.next();
			String internalValue = value.path(VALUE).stringValue();
			ObjectNode newValue = MAPPER.createObjectNode();
			newValue.put(VALUE, internalValue);

			ArrayNode newLabel = MAPPER.createArrayNode();
			JsonNode labels = value.get(LABEL);
			if (labels == null) {
				newValue.set(LABEL, createNewLabel(internalValue, localesInModel));
				newValues.add(newValue);
				continue;
			}

			for (String locale : localesInModel) {
				String text = getTextForLocale(labels, locale, internalValue);
				addLocaleSpecificDisplayValue(newLabel, locale, text);
				newValue.set(LABEL, newLabel);
			}
			newValues.add(newValue);
		}
		return newValues;
	}

	private static JsonNode getLabelNodeForLocale(JsonNode labels, String locale) {
		Iterator<JsonNode> labelIt = labels.iterator();
		while (labelIt.hasNext()) {
			JsonNode labelEntry = labelIt.next();
			if (labelEntry.path(LOCALE).asString().equals(locale)) {
				// Label found for given locale
				return labelEntry;
			}
		}
		return null;
	}

	private static ArrayNode createNewLabel(String internalValue, List<String> localesInModel) {
		ArrayNode newLabel = MAPPER.createArrayNode();
		for (String locale : localesInModel) {
			addLocaleSpecificDisplayValue(newLabel, locale, internalValue);
		}
		return newLabel;
	}

	private static void addLocaleSpecificDisplayValue(ArrayNode newLabel, String locale, String text) {
		ObjectNode localeSpecificDisplayValue = MAPPER.createObjectNode();
		localeSpecificDisplayValue.put(LOCALE, locale);
		localeSpecificDisplayValue.put(TEXT, text);
		newLabel.add(localeSpecificDisplayValue);
	}

	private static String getTextForLocale(JsonNode labels, String locale, String internalValue) {
		JsonNode labelNodeForLocale = getLabelNodeForLocale(labels, locale);
		if (labelNodeForLocale != null && !labelNodeForLocale.path(TEXT).isMissingNode()) {
			String displayValue = labelNodeForLocale.path(TEXT).asString();
			return displayValue.isEmpty() ? internalValue : displayValue;
		} else {
			return internalValue;
		}
	}

	private static List<String> getLocales(JsonNode model) {
		JsonNode localesNode = model.get("header").get("locales");
		List<String> defaultLocales = List.of(Locale.US.toString());
		if (localesNode == null) { // this should not happen
			return defaultLocales;
		}
		List<String> locales = localesNode.findValues("code").stream()
				.map(JsonNode::asString)
				.toList();
		return locales.isEmpty() ? defaultLocales : locales;
	}

}
