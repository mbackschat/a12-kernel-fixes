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

import java.util.List;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import lombok.NonNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * A12K-3986: Remove variantFilter and variantErrorMessages properties from Rules, Fields, and Computations.
 * These properties are no longer supported by the document model.
 */
public class RemoveVariantPropertiesStep {

	private static final String VARIANT_FILTER = "variantFilter";
	private static final String VARIANT_ERROR_MESSAGES = "variantErrorMessages";

	private RemoveVariantPropertiesStep() {
	}

	@SuppressWarnings("Unused")
	public static void execute(@NonNull JsonNode model,  MigrationLogger migrationLogger,   MigrationCache cache,
			 ICustomMigrationConfig customMigConfig) {
		removeVariantPropertiesFromElements(ModelJsonUtil.extractAllElements(model, ModelJsonUtil.TYPE_RULE),
				ModelJsonUtil.TYPE_RULE);
		removeVariantPropertiesFromElements(ModelJsonUtil.extractAllElements(model, ModelJsonUtil.TYPE_FIELD),
				ModelJsonUtil.TYPE_FIELD);
		removeVariantPropertiesFromElements(ModelJsonUtil.extractAllElements(model, ModelJsonUtil.TYPE_COMPUTATION),
				ModelJsonUtil.TYPE_COMPUTATION);
	}

	private static void removeVariantPropertiesFromElements(List<JsonNode> elements, String elementType) {
		for (JsonNode element : elements) {
			JsonNode typeNode = element.get(elementType);
			if (typeNode instanceof ObjectNode objectNode) {
				objectNode.remove(VARIANT_FILTER);
				objectNode.remove(VARIANT_ERROR_MESSAGES);
			}
		}
	}
}
