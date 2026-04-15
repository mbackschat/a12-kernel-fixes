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
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationStepConfigType;

/**
 * The representation of the localized text values has changed in json from a map from locale to
 * text to a list of objects with keys locale and text.
 * <p>
 * This is relevant for a few different document model fields which can occur in a few different
 * elements:
 * <ul>
 * <li>label</li>
 * <li>errorMessage</li>
 * <li>internalDescription</li>
 * <li>externalDescription</li>
 * <li>helperText</li>
 * <li>variantErrorMessages (as values of the map)</li>
 * </ul>
 */
public class LocalizedTextToListStep extends BaseJsonNodeMigrationStep {

	private static final String HELPER_TEXT = "helperText";
	private static final String LABEL = "label";
	private static final String ERROR_MESSAGE = "errorMessage";
	private static final String INTERNAL_DESCRIPTION = "internalDescription";
	private static final String EXTERNAL_DESCRIPTION = "externalDescription";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final List<String> relevantFields = List.of(LABEL, ERROR_MESSAGE, INTERNAL_DESCRIPTION, EXTERNAL_DESCRIPTION,
			HELPER_TEXT);

	public LocalizedTextToListStep(MigrationCache cache, MigrationStepConfigType stepConfigType,
			ICustomMigrationConfig customMigConfig) {
		super(cache, stepConfigType, customMigConfig);
	}

	@Override
	public void execute(JsonNode model, MigrationLogger migrationLogger) {
		relevantFields.forEach(f -> this.convertLocalizedTexts(model, f));
		// Variant error messages are inside of a map and therefore have to be handled separately
		model.findValues("Rule").forEach(r -> handleVariantErrorMessages(r.get("variantErrorMessages")));
	}

	private void handleVariantErrorMessages(JsonNode jsonNode) {
		if (jsonNode == null || !jsonNode.isObject()) {
			return;
		}
		Iterator<String> fieldNameIterator = jsonNode.fieldNames();
		while (fieldNameIterator.hasNext()) {
			String fieldName = fieldNameIterator.next();
			convertLocalizedTextSubElement((ObjectNode) jsonNode, fieldName);
		}
	}

	private void convertLocalizedTexts(JsonNode model, String elementName) {
		List<JsonNode> localizedTextParents = model.findParents(elementName);
		for (JsonNode localizedTextParent : localizedTextParents) {
			if (!localizedTextParent.isObject()) {
				continue;
			}
			convertLocalizedTextSubElement((ObjectNode) localizedTextParent, elementName);
		}
	}

	private void convertLocalizedTextSubElement(ObjectNode localizedTextParent, String subElement) {
		JsonNode localizedTextMapNode = localizedTextParent.get(subElement);
		if (localizedTextMapNode == null || !localizedTextMapNode.isObject()) {
			return;
		}
		localizedTextParent.set(subElement, convertLocalizedTextNode(localizedTextMapNode));
	}

	private JsonNode convertLocalizedTextNode(JsonNode localizedTextMapNode) {
		Iterator<Entry<String, JsonNode>> fieldIterator = localizedTextMapNode.properties().iterator();
		ArrayNode newValues = MAPPER.createArrayNode();
		while (fieldIterator.hasNext()) {
			Entry<String, JsonNode> value = fieldIterator.next();
			ObjectNode newValue = MAPPER.createObjectNode();
			newValue.put("locale", value.getKey());
			newValue.set("text", value.getValue());
			newValues.add(newValue);
		}
		return newValues;
	}
}
