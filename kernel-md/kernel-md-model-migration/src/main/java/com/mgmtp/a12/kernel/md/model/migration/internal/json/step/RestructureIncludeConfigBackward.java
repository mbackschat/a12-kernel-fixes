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
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

public final class RestructureIncludeConfigBackward {

	private RestructureIncludeConfigBackward() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, ICustomMigrationConfig customMigConfig) {

		ModelJsonUtil.extractUnexpandedIncludingGroupsByIncludeConfig(model)
				.forEach(it ->
						moveFromIncludeConfig(it.get(ModelJsonUtil.TYPE_GROUP)));

		RestructureIncludeConfig.extractModelReferencesForHandledPurposes(model).forEach(
				RestructureIncludeConfigBackward::insertModelAlias);
	}

	private static void moveFromIncludeConfig(JsonNode includeConfigContainer) {

		var containerAsObj = (ObjectNode) includeConfigContainer;
		moveFromAliasFromNewModelIncludeConfig(containerAsObj);
		((ObjectNode) includeConfigContainer).remove(ModelJsonUtil.MODEL_INCLUDE_CONFIG);
	}

	private static void moveExcludeComputations(JsonNode modelIncludeConfigNd, ObjectNode containerAsObj) {
		var excludeComputations = modelIncludeConfigNd.get(ModelJsonUtil.EXCLUDE_COMPUTATION);
		if (excludeComputations != null) {
			containerAsObj.set(ModelJsonUtil.EXCLUDE_COMPUTATION, excludeComputations);
		}
	}

	private static void moveExcludeRules(JsonNode modelIncludeConfigNd, ObjectNode containerAsObj) {
		var excludeRules = modelIncludeConfigNd.get(ModelJsonUtil.EXCLUDE_RULES);
		if (excludeRules != null) {
			containerAsObj.set(ModelJsonUtil.EXCLUDE_RULES, excludeRules);
		}
	}

	private static void moveIncludeLevel(JsonNode modelIncludeConfigNd, ObjectNode containerAsObj) {
		var includeLevel = modelIncludeConfigNd.get(ModelJsonUtil.INCLUDE_LEVEL);
		if (includeLevel != null) {
			containerAsObj.set(ModelJsonUtil.INCLUDE_LEVEL, includeLevel);
		}
	}

	private static void moveFromAliasFromNewModelIncludeConfig(ObjectNode containerAsObj) {
		ObjectNode modelIncludeConfigNd = (ObjectNode) containerAsObj.get(ModelJsonUtil.MODEL_INCLUDE_CONFIG);
		if (modelIncludeConfigNd == null || !modelIncludeConfigNd.isObject()) {
			throw MigrationUtils.contentNotSupportedException(
					"The document model contains erroneous '" + ModelJsonUtil.MODEL_INCLUDE_CONFIG + "'.");
		}

		var modelRefValue = modelIncludeConfigNd.get(ModelJsonUtil.MODEL_REFERENCE);
		if (modelRefValue == null || !modelRefValue.isString()) {
			throw MigrationUtils.contentNotSupportedException(
					"The document model contains erroneous '" + ModelJsonUtil.MODEL_REFERENCE + "'.");
		}
		containerAsObj.set(ModelJsonUtil.MODEL_ALIAS, modelRefValue);

		moveExcludeRules(modelIncludeConfigNd, containerAsObj);
		moveExcludeComputations(modelIncludeConfigNd, containerAsObj);
		moveIncludeLevel(modelIncludeConfigNd, containerAsObj);
	}

	private static void insertModelAlias(JsonNode modelReferenceContainer) {
		var modelReferenceContainerOBJ = (ObjectNode) modelReferenceContainer;

		modelReferenceContainerOBJ.set(ModelJsonUtil.MR_ALIAS,
				modelReferenceContainer.get(ModelJsonUtil.MR_REFERENCE)
		);
	}
}