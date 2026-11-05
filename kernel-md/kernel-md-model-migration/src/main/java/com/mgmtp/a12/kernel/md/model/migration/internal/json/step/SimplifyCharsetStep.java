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

import com.mgmtp.a12.kernel.core.base.utils.internal.CharsetUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

/**
 * A12K-3107: Step to simplify restricted charsets of the supportedCharacters nodes in A12 document models
 */
public final class SimplifyCharsetStep {

	public static final IProblemReporter DONT_REPORT_PROBLEMS = x -> {
	};
	public static final CharsetUtil DEFAULT_CHARSET_UTIL = new CharsetUtil();

	private SimplifyCharsetStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, ICustomMigrationConfig customMigConfig) {
		for (final JsonNode supportedCharactersNode : model.findValues(ModelJsonUtil.SUPPORTED_CHARACTERS)) {
			// All restricted charsets should be string arrays.
			// If we encounter malformed charset definitions, we don't touch anything
			if (supportedCharactersNode.isArray()) {
				boolean weirdValueEncountered = false;
				final List<String> charset = new ArrayList<>();
				for (final JsonNode value : supportedCharactersNode) {
					if (value.isString()) {
						charset.add(value.asString());
					} else {
						weirdValueEncountered = true; // paranoid
						break;
					}
				}
				// BMP would mean no supportedCharactersNode defined
				// So let's not touch supportedCharacters = [] definitions for now
				if (!charset.isEmpty() && !weirdValueEncountered) {
					DEFAULT_CHARSET_UTIL.simplifyCharset(charset, DONT_REPORT_PROBLEMS)
							.ifPresent(simplifiedCharset -> {
								final ArrayNode array = (ArrayNode) supportedCharactersNode;
								array.removeAll();
								simplifiedCharset.forEach(array::add);
							});
				}
			}
		}
	}
}
