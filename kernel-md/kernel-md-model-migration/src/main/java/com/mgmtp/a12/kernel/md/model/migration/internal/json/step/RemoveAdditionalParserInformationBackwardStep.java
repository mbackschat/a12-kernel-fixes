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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * A12K-4503: Backward migration step. Reverses {@link AddAdditionalParserInformationStep}: the free-text
 * {@code additionalParserInformation} field on a {@code Computation} is parsed back into the structured
 * {@code errorCodesToSuppress} list known to the older document model version, then removed.
 * <p>
 * The older version can only represent a plain suppress-warning preamble. If the field holds anything else
 * (e.g. a future parser directive), the migration fails with a clear error rather than silently dropping data.
 */
public final class RemoveAdditionalParserInformationBackwardStep {

	private static final String ADDITIONAL_PARSER_INFORMATION = "additionalParserInformation";
	private static final String ERROR_CODES_TO_SUPPRESS = "errorCodesToSuppress";

	/**
	 * Matches a string that consists of nothing but a single suppress-warning preamble, capturing the
	 * comma-separated code list. Surrounding whitespace is tolerated.
	 */
	private static final Pattern SUPPRESS_PREAMBLE_ONLY =
			Pattern.compile("^\\s*@\\s*(?:SuppressWarning|UnterdrueckeWarnung)\\s*\\(([^)]*)\\)\\s*$");

	private RemoveAdditionalParserInformationBackwardStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, ICustomMigrationConfig customMigConfig) {
		for (JsonNode element : ModelJsonUtil.extractAllElements(model, ModelJsonUtil.TYPE_COMPUTATION)) {
			JsonNode comp = element.get(ModelJsonUtil.TYPE_COMPUTATION);
			if (comp instanceof ObjectNode compObj) {
				JsonNode removed = compObj.remove(ADDITIONAL_PARSER_INFORMATION);
				if (removed instanceof StringNode preamble) {
					compObj.set(ERROR_CODES_TO_SUPPRESS, toErrorCodes(preamble.stringValue()));
				}
			}
		}
	}

	private static ArrayNode toErrorCodes(String preamble) {
		if (preamble != null && preamble.isBlank()) {
			return MAPPER.createArrayNode();
		}
		Matcher matcher = preamble == null ? null : SUPPRESS_PREAMBLE_ONLY.matcher(preamble);
		if (matcher == null || !matcher.matches()) {
			throw MigrationUtils.contentNotSupportedException(
					"Cannot migrate the computation's 'additionalParserInformation' backward: the older document model"
							+ " version can only represent a plain @SuppressWarning(...) / @UnterdrueckeWarnung(...)"
							+ " preamble, but the field holds: " + preamble);
		}
		ArrayNode codes = MAPPER.createArrayNode();
		String codeList = matcher.group(1).trim();
		if (!codeList.isEmpty()) {
			for (String code : codeList.split(",")) {
				String trimmed = code.trim();
				if (!trimmed.isEmpty()) {
					codes.add(trimmed);
				}
			}
		}
		return codes;
	}
}
