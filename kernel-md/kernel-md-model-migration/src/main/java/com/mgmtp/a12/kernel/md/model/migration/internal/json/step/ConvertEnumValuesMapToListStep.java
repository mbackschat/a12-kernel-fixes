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
import java.util.Map.Entry;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The representation of the enumeration values in json differs to the specification in the java
 * api. In json it is a map, in which the {@code map key} is the internal enum value and the
 * {@code map value} is the corresponding multilingual label. In the java api it is a list of
 * {@code EnumValue}s. In this step, we align the json representation to the java specification.
 */
public final class ConvertEnumValuesMapToListStep {

	private ConvertEnumValuesMapToListStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, MigrationCache cache,
			ICustomMigrationConfig customMigConfig) {
		List<JsonNode> enumTypeNodes = model.findValues("EnumerationType");
		for (JsonNode enumType : enumTypeNodes) {
			JsonNode values = enumType.path(ModelJsonUtil.ENUMERATION_VALUES);
			if (values.isMissingNode() || !values.isObject()) {
				throw MigrationUtils.contentNotSupportedException(
						"The document model contains an enum field without 'values' or the existing 'values' are not a json object.");
			}
			ArrayNode newValues = createNewValues(values);
			((ObjectNode) enumType).replace(ModelJsonUtil.ENUMERATION_VALUES, newValues);
		}
	}

	private static ArrayNode createNewValues(JsonNode values) {
		Iterator<Entry<String, JsonNode>> iterator = values.properties().iterator();
		ArrayNode newValues = MAPPER.createArrayNode();
		while (iterator.hasNext()) {
			Entry<String, JsonNode> value = iterator.next();
			ObjectNode newValue = MAPPER.createObjectNode();
			newValue.put("value", value.getKey());
			if (!value.getValue().isEmpty()) {
				newValue.set("label", value.getValue());
			}
			newValues.add(newValue);
		}
		return newValues;
	}

}
