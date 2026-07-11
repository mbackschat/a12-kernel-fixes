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

import java.util.List;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationError;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

public final class AddRequirednessConfigStep {

	private AddRequirednessConfigStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, MigrationCache cache,
			ICustomMigrationConfig customMigConfig) {
		// remove Group.required
		List<JsonNode> groupNodes = ModelJsonUtil.extractAllGroups(model);
		groupNodes.stream()
				.map(g -> g.get(ModelJsonUtil.TYPE_GROUP))
				.filter(g -> g.has(ModelJsonUtil.REQUIRED))
				.map(ObjectNode.class::cast)
				.forEach(g -> g.remove(ModelJsonUtil.REQUIRED));

		// remove Field.required and add requirednessConfig if needed
		String modelId = ModelJsonUtil.extractModelId(model);
		List<ModelJsonUtil.JsonNodeWithPath> fields = ModelJsonUtil.extractAllFieldsWithPath(model);
		fields.stream()
				.map(f -> new ModelJsonUtil.JsonNodeWithPath(f.node().get(ModelJsonUtil.TYPE_FIELD), f.path()))
				.filter(f -> f.node().has(ModelJsonUtil.REQUIRED))
				.forEach(f -> removeRequiredAndAddRequirednessConfig(f, modelId, customMigConfig));
	}

	private static void removeRequiredAndAddRequirednessConfig(ModelJsonUtil.JsonNodeWithPath field, String modelId,
			ICustomMigrationConfig customMigConfig) {
		String fieldPath = field.path().toString();
		String requirednessMode = customMigConfig.getRequirednessMode(modelId, fieldPath);
		if (requirednessMode == null ||
				(!requirednessMode.equals(ICustomMigrationConfig.DEFAULT_REQUIREDNESS_MODE) && !requirednessMode.equals(
						ICustomMigrationConfig.REQUIREDNESS_MODE_RELATIVE_TO_PARENT))) {
			throw new MigrationException(new MigrationError(MigrationErrorType.CONFIG_INVALID)
					.withMessage(
							"The configuration of the requiredness mode for the field '" + fieldPath + "' in the DM '" + modelId + "' is invalid (=" + requirednessMode + ")."));
		}
		ObjectNode fieldObj = (ObjectNode) field.node();
		fieldObj.remove(ModelJsonUtil.REQUIRED);
		ObjectNode requirednessConfigNode = MAPPER.createObjectNode();
		requirednessConfigNode.put(ModelJsonUtil.REQUIREDNESS_MODE, requirednessMode);
		fieldObj.set(ModelJsonUtil.REQUIREDNESS_CONFIG, requirednessConfigNode);
	}
}
