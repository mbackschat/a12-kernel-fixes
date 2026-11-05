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

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * A12K-4503: Forward migration step. Replaces the structured {@code errorCodesToSuppress} list on every
 * {@code Computation} with the free-text {@code additionalParserInformation} field that holds the complete
 * suppress-warning preamble the author would have written (e.g. {@code @SuppressWarning(A,B)}, or
 * {@code @UnterdrueckeWarnung(A,B)} for a German condition language). A computation whose list is empty (or
 * absent) receives no {@code additionalParserInformation}; the legacy {@code errorCodesToSuppress} property is
 * removed either way.
 */
public final class AddAdditionalParserInformationStep {

	private static final String ERROR_CODES_TO_SUPPRESS = "errorCodesToSuppress";
	private static final String ADDITIONAL_PARSER_INFORMATION = "additionalParserInformation";

	private AddAdditionalParserInformationStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, ICustomMigrationConfig customMigConfig) {
		boolean german = isGermanConditionLanguage(model);
		for (JsonNode element : ModelJsonUtil.extractAllElements(model, ModelJsonUtil.TYPE_COMPUTATION)) {
			JsonNode comp = element.get(ModelJsonUtil.TYPE_COMPUTATION);
			if (comp instanceof ObjectNode compObj) {
				JsonNode removed = compObj.remove(ERROR_CODES_TO_SUPPRESS);
				if (removed instanceof ArrayNode codes && !codes.isEmpty()) {
					compObj.put(ADDITIONAL_PARSER_INFORMATION, buildPreamble(codes, german));
				}
			}
		}
	}

	private static String buildPreamble(ArrayNode codes, boolean german) {
		List<String> codeList = new ArrayList<>();
		codes.forEach(code -> codeList.add(code.asString()));
		return Computation._internal_buildSuppressPreamble(codeList, german);
	}

	private static boolean isGermanConditionLanguage(JsonNode model) {
		JsonNode config = ModelJsonUtil.extractDmConfig(model);
		return config instanceof ObjectNode && config.get("conditionLanguage") instanceof ObjectNode conditionLanguage
				&& "de_DE".equals(conditionLanguage.path("code").asString());
	}
}
