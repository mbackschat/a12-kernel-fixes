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

import java.util.Iterator;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils;

import tools.jackson.databind.JsonNode;

/**
 * The annotation "savedAt" was updated automatically by the Data Modeler and UI Designer when saving a model therein. In its
 * current state, Data Services is not using this annotation anymore. Thus, it should be removed.
 */
public final class RemoveSavedAtAnnotationStep {

	private static final String HEADER = "header";
	private static final String ANNOTATIONS = "annotations";
	private static final String SAVED_AT = "savedAt";
	private static final String NAME = "name";

	private RemoveSavedAtAnnotationStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, MigrationCache cache,
			ICustomMigrationConfig customMigConfig) {
		removeSavedAtAnnotationIfExists(model.path(HEADER).get(ANNOTATIONS));
	}

	private static void removeSavedAtAnnotationIfExists(JsonNode annotationsNode) {
		if (annotationsNode == null) {
			return;
		}

		if (annotationsNode.isArray()) {
			for (Iterator<JsonNode> annotationIterator = annotationsNode.iterator(); annotationIterator.hasNext(); ) {
				JsonNode annotationNode = annotationIterator.next();

				if (SAVED_AT.equals(annotationNode.get(NAME).asString())) {
					annotationIterator.remove();
				}
			}
		} else {
			throw MigrationUtils.contentNotSupportedException("The document model header attribute 'annotations' is not a json array object.");
		}
	}
}
