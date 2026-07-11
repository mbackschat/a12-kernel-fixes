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

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import lombok.NonNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Migration step for CDM query root documents. If the document model header contains an annotation
 * named {@code cdm.queryRoot}, all model references with purpose {@code include} are copied with
 * purpose {@code composedData} and appended to the existing model references.
 */
public class CopyAnnotationsForCDMStep {

	private CopyAnnotationsForCDMStep() {
	}

	public static void execute(@NonNull JsonNode model, MigrationLogger migrationLogger, MigrationCache cache,
			ICustomMigrationConfig customMigConfig) {
		JsonNode header = model.get(ModelJsonUtil.HEADER);
		JsonNode annotations = header.get(ModelJsonUtil.ANNOTATIONS);

		if (annotations == null || !annotations.isArray()) {
			return;
		}
		boolean hasCdmQueryRoot = false;
		for (JsonNode annotation : annotations) {
			if ("cdm.queryRoot".equals(annotation.path(ModelJsonUtil.ANNOTATION_NAME).asString())) {
				hasCdmQueryRoot = true;
				break;
			}
		}
		if (!hasCdmQueryRoot) {
			return;
		}

		JsonNode modelReferences = header.get(ModelJsonUtil.MODEL_REFERENCES);
		if (modelReferences == null || !modelReferences.isArray()) {
			return;
		}

		List<JsonNode> copies = new ArrayList<>();
		for (JsonNode ref : modelReferences) {
			if (ModelJsonUtil.PURPOSE_INCLUDE.equals(ref.path(ModelJsonUtil.MODEL_PURPOSE).asString())) {
				JsonNode copy = ref.deepCopy();
				((ObjectNode) copy).put(ModelJsonUtil.MODEL_PURPOSE, "composedData");
				copies.add(copy);
			}
		}

		ArrayNode refsArray = (ArrayNode) modelReferences;
		for (JsonNode copy : copies) {
			refsArray.add(copy);
		}
	}
}
