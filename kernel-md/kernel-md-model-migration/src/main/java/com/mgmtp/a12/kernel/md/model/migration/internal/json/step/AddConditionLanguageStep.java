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

import static com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig.SUPPORTED_CONDITION_LANGUAGES;
import static com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils.MAPPER;

import java.util.Locale;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * A field conditionLanguage was added to the model config.
 */
public final class AddConditionLanguageStep {

	private static final String CONDITION_LANGUAGE = "conditionLanguage";

	private AddConditionLanguageStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, MigrationCache cache,
			ICustomMigrationConfig customMigConfig) {
		Locale conditionLanguage = getConditionLanguage(model, customMigConfig);
		JsonNode modelConfig = ModelJsonUtil.extractDmConfig(model);
		ObjectNode node = MAPPER.createObjectNode();
		node.put("code", conditionLanguage.toString());
		((ObjectNode) modelConfig).set(CONDITION_LANGUAGE, node);
	}

	private static Locale getConditionLanguage(JsonNode model, ICustomMigrationConfig customMigConfig) {
		String modelId = ModelJsonUtil.extractModelId(model);
		Locale conditionLanguage = customMigConfig.getConditionLanguage(modelId);
		return SUPPORTED_CONDITION_LANGUAGES.contains(conditionLanguage) ? conditionLanguage : Locale.US;
	}
}
