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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationError;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationStepConfigType;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

public class FillGapsInEnumDisplayValuesStep extends BaseJsonNodeMigrationStep {

	private static final String VALUE = "value";
	private static final String LABEL = "label";
	private static final String LOCALE = "locale";
	private static final String TEXT = "text";

	public FillGapsInEnumDisplayValuesStep(MigrationCache cache, MigrationStepConfigType stepConfigType,
			ICustomMigrationConfig customMigConfig) {
		super(cache, stepConfigType, customMigConfig);
	}

	@Override public void execute(JsonNode model, MigrationLogger migrationLogger) {
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

	private void checkEnumValues(JsonNode values) {
		if (values.isMissingNode() || !values.isArray()) {
			throw new MigrationException(new MigrationError(MigrationErrorType.MODEL_CONTENT_NOT_SUPPORTED)
					.withMessage(
							"The document model contains an enum field without 'values' or the existing 'values' are not a json array."));
		}
	}

	private boolean hasDisplayValueGaps(JsonNode values, List<String> localesInModel) {
		boolean hasAtLeastOneDisplayValue = false;
		boolean isAtLeastOneDisplayValueMissing = false;

		Iterator<JsonNode> valueIt = values.elements();
		while (valueIt.hasNext()) {
			JsonNode value = valueIt.next();
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
					if (labelNodeForLocale.path(TEXT).asText().isEmpty()) {
						isAtLeastOneDisplayValueMissing = true;
					} else {
						hasAtLeastOneDisplayValue = true;
					}
				}
			}
		}
		return hasAtLeastOneDisplayValue && isAtLeastOneDisplayValueMissing;
	}

	private ArrayNode fillGaps(JsonNode values, List<String> localesInModel) {
		Iterator<JsonNode> valueIt = values.elements();
		ArrayNode newValues = MAPPER.createArrayNode();
		while (valueIt.hasNext()) {
			JsonNode value = valueIt.next();
			String internalValue = value.path(VALUE).textValue();
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

	private JsonNode getLabelNodeForLocale(JsonNode labels, String locale) {
		Iterator<JsonNode> labelIt = labels.elements();
		while (labelIt.hasNext()) {
			JsonNode labelEntry = labelIt.next();
			if (labelEntry.path(LOCALE).asText().equals(locale)) {
				// Label found for given locale
				return labelEntry;
			}
		}
		return null;
	}

	private ArrayNode createNewLabel(String internalValue, List<String> localesInModel) {
		ArrayNode newLabel = MAPPER.createArrayNode();
		for (String locale : localesInModel) {
			addLocaleSpecificDisplayValue(newLabel, locale, internalValue);
		}
		return newLabel;
	}

	private void addLocaleSpecificDisplayValue(ArrayNode newLabel, String locale, String text) {
		ObjectNode localeSpecificDisplayValue = MAPPER.createObjectNode();
		localeSpecificDisplayValue.put(LOCALE, locale);
		localeSpecificDisplayValue.put(TEXT, text);
		newLabel.add(localeSpecificDisplayValue);
	}

	private String getTextForLocale(JsonNode labels, String locale, String internalValue) {
		JsonNode labelNodeForLocale = getLabelNodeForLocale(labels, locale);
		if (labelNodeForLocale != null) {
			String displayValue = labelNodeForLocale.path(TEXT).asText();
			return displayValue.isEmpty() ? internalValue : displayValue;
		} else {
			return internalValue;
		}
	}

	private List<String> getLocales(JsonNode model) {
		JsonNode localesNode = model.get("header").get("locales");
		List<String> defaultLocales = List.of(Locale.US.toString());
		if (localesNode == null) { // this should not happen
			return defaultLocales;
		}
		List<String> locales = localesNode.findValues("code").stream()
				.map(JsonNode::asText)
				.toList();
		return locales.isEmpty() ? defaultLocales : locales;
	}

}
