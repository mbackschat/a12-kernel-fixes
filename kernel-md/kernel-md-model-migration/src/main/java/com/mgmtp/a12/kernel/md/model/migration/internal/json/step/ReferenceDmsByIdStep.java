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

import java.util.Objects;
import java.util.stream.StreamSupport;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.IdReferenceConverter;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

public final class ReferenceDmsByIdStep {

	private ReferenceDmsByIdStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, ICustomMigrationConfig customMigConfig) {
		if (!ModelJsonUtil.referencesOtherDm(model)) {
			return;
		}
		ModelJsonUtil.extractReferencesToDms(model).stream().forEach(ReferenceDmsByIdStep::migrateModelRef);
		// in case there are expanded document models
		ModelJsonUtil.extractTypeDefs(model).stream()
				.map(td -> td.get(ModelJsonUtil.MODEL_REFERENCES))
				.filter(Objects::nonNull)
				.filter(JsonNode::isArray)
				.map(ArrayNode.class::cast)
				.flatMap(an -> StreamSupport.stream(an.spliterator(), false))
				.forEach(ReferenceDmsByIdStep::migrateModelRef);
	}

	private static void migrateModelRef(JsonNode modelRef) {
		String reference = ModelJsonUtil.extractReferenceFromModelReference(modelRef);
		if (reference == null) { // should not happen
			throw MigrationUtils.contentNotSupportedException(
					"The document model contains a model-reference object with a null or a non-textual reference.");
		}
		String newReference = IdReferenceConverter.createIdReference(reference);
		((ObjectNode) modelRef).set(ModelJsonUtil.MR_REFERENCE, StringNode.valueOf(newReference));
	}

}
