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
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * Model aliases could erroneously contain a .json suffix.
 */
public final class RemoveModelAliasSuffix {

	private RemoveModelAliasSuffix() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, MigrationCache cache,
			ICustomMigrationConfig customMigConfig) {
		ModelJsonUtil.extractUnexpandedIncludingGroups(model)
				.forEach(it -> updateModelAlias(it.get(ModelJsonUtil.TYPE_GROUP), ModelJsonUtil.MODEL_ALIAS));
		// we don't use ModelJsonUtil#extractReferencesToDms since the suffix comes from the data modeler
		// and the typeDef import was implemented after having retired the data modeler (only includes are relevant)
		ModelJsonUtil.extractReferencesToIncludedDms(model, false).forEach(it -> updateModelAlias(it, ModelJsonUtil.MR_ALIAS));
	}

	private static void updateModelAlias(JsonNode aliasContainer, String aliasRef) {
		JsonNode referenceNode = aliasContainer.get(aliasRef);
		if (referenceNode == null || !referenceNode.isString()) {
			throw MigrationUtils.contentNotSupportedException("The document model contains erroneous '" + aliasRef + "'.");
		}
		String alias = referenceNode.asString();
		if (alias.endsWith(ModelJsonUtil.JSON_EXTENSION)) {
			String newAlias = alias.substring(0, alias.length() - ModelJsonUtil.JSON_EXTENSION.length());
			((ObjectNode) aliasContainer).replace(aliasRef, new StringNode(newAlias));
		}
	}
}
