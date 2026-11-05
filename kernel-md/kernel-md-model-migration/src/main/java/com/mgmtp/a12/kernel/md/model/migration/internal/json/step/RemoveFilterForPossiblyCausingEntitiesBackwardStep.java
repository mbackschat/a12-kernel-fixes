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

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * A12K-4496: Backward migration step from 29.7.0 to 29.6.0. Removes the optional
 * {@code filterForPossiblyCausingEntities} property from every Rule element, because the property
 * is unknown to document model version 29.6.0.
 */
public class RemoveFilterForPossiblyCausingEntitiesBackwardStep {

	private static final String FILTER_FOR_POSSIBLY_CAUSING_ENTITIES = "filterForPossiblyCausingEntities";

	private RemoveFilterForPossiblyCausingEntitiesBackwardStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, ICustomMigrationConfig customMigConfig) {
		for (JsonNode element : ModelJsonUtil.extractAllElements(model, ModelJsonUtil.TYPE_RULE)) {
			JsonNode rule = element.get(ModelJsonUtil.TYPE_RULE);
			if (rule instanceof ObjectNode obj) {
				obj.remove(FILTER_FOR_POSSIBLY_CAUSING_ENTITIES);
			}
		}
	}
}
