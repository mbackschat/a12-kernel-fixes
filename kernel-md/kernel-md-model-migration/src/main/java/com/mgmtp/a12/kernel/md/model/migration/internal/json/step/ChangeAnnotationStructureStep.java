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

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeType;
import tools.jackson.databind.node.ObjectNode;

public final class ChangeAnnotationStructureStep {

	private ChangeAnnotationStructureStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, MigrationCache cache,
			ICustomMigrationConfig customMigConfig) {
		JsonNode typeDefsNode = model.findValue(ModelJsonUtil.TYPE_DEFS);
		if (typeDefsNode != null) {
			if (!typeDefsNode.isArray()) {
				throw MigrationUtils.contentNotSupportedException("The 'typeDefinitions' node is not an array.");
			}
			migrateAnnotationsInArrayNode(typeDefsNode);
		}

		JsonNode rootGroupsNode = model.findValue(ModelJsonUtil.ROOT_GROUPS);
		if (rootGroupsNode != null) {
			if (!rootGroupsNode.isArray()) {
				throw MigrationUtils.contentNotSupportedException("The 'rootGroups' node is not an array.");
			}
			migrateAnnotationsInArrayNode(rootGroupsNode);
		}

		List<JsonNode> elementsNodes = ModelJsonUtil.extractAllElementsNodes(model);
		migrateAnnotationsInElementsNodes(elementsNodes);

		List<JsonNode> fieldTypeNodes = model.findValues(ModelJsonUtil.FIELD_FIELD_TYPE);
		for (JsonNode fieldTypeNode : fieldTypeNodes) {
			migrateAnnotations(fieldTypeNode);
		}
	}

	/**
	 * only for groups since they are the only elements which have elements
	 */
	private static void migrateAnnotationsInElementsNodes(List<JsonNode> elementsNodes) {
		for (JsonNode elementsNode : elementsNodes) {
			if (!elementsNode.isArray()) {
				throw MigrationUtils.contentNotSupportedException(
						"The document model contains at least one group whose 'elements' node is not an array.");
			}
			migrateAnnotationsInArrayNode(elementsNode);
		}
	}

	/**
	 * for an array whose items might contain the 'annotations' node
	 */
	private static void migrateAnnotationsInArrayNode(JsonNode arrayNode) {
		Iterator<JsonNode> nodes = arrayNode.iterator();
		while (nodes.hasNext()) {
			JsonNode node = nodes.next();
			migrateAnnotations(node);
		}
	}

	private static void migrateAnnotations(JsonNode jsonNode) {
		JsonNode annotationsNode = jsonNode.path(ModelJsonUtil.ANNOTATIONS);
		if (annotationsNode.isMissingNode() // no annotations
				|| annotationsNode.isArray() // this node is already in the form we want
		) {
			return;
		}
		ArrayNode newAnnotations = createNewAnnotations(annotationsNode);
		((ObjectNode) jsonNode).replace(ModelJsonUtil.ANNOTATIONS, newAnnotations);
	}

	private static ArrayNode createNewAnnotations(JsonNode annotationsNode) {
		Iterator<Entry<String, JsonNode>> iterator = annotationsNode.properties().iterator();
		ArrayNode newAnnotations = MAPPER.createArrayNode();
		while (iterator.hasNext()) {
			Entry<String, JsonNode> annotation = iterator.next();
			if (annotation.getValue().getNodeType() != JsonNodeType.NULL
					&& !annotation.getValue().isString()) {
				throw MigrationUtils.contentNotSupportedException(
						"The document model contains at least an annotation whose value is not text.");
			}
			ObjectNode newAnnotation = MAPPER.createObjectNode();
			newAnnotation.put(ModelJsonUtil.ANNOTATION_NAME, annotation.getKey());
			if (annotation.getValue().getNodeType() != JsonNodeType.NULL) {
				newAnnotation.put(ModelJsonUtil.ANNOTATION_VALUE, annotation.getValue().asString());
			}
			newAnnotations.add(newAnnotation);
		}
		return newAnnotations;
	}
}
