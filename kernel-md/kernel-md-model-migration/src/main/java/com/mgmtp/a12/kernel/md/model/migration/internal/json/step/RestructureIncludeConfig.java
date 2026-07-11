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

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import lombok.NonNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

public final class RestructureIncludeConfig {

	private RestructureIncludeConfig() {
	}

	static Stream<JsonNode> extractModelReferencesForHandledPurposes(JsonNode model) {
		Set<String> modelHeaderPurposesToHandle = Set.of(ModelJsonUtil.PURPOSE_INCLUDE, ModelJsonUtil.PURPOSE_TRANSITIVE_INCLUDE
				, ModelJsonUtil.PURPOSE_IMPORT/* aka typeDefinitions*/);

		return ModelJsonUtil.extractReferencesToDms(model).stream()
				.filter(jsonNode -> modelHeaderPurposesToHandle.contains(
						Optional.ofNullable(jsonNode.get(ModelJsonUtil.MODEL_PURPOSE)).map(JsonNode::asString)
								.orElse("")
				));
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, MigrationCache cache,
			ICustomMigrationConfig customMigConfig) {

		Map<String, @NonNull String> modelReferencesByAlias =
				extractModelReferencesForHandledPurposes(model)
						.collect(Collectors.toMap(
								RestructureIncludeConfig::getModelAlias,
								RestructureIncludeConfig::getModelReference));

		ModelJsonUtil.extractUnexpandedIncludingGroups(model)
				.forEach(it ->
						moveToIncludeConfig((ObjectNode) it.get(ModelJsonUtil.TYPE_GROUP), modelReferencesByAlias));

		extractModelReferencesForHandledPurposes(model).forEach(RestructureIncludeConfig::deleteModelAlias);

		removeDuplicateReferences(model);
	}

	private record ModelRefKey(String dmId, String modelType, String purpose) {

		public static ModelRefKey of(JsonNode referenceNode) {
			var dmId = referenceNode.get(ModelJsonUtil.MR_REFERENCE).asString();
			var modelType = referenceNode.get(ModelJsonUtil.MODEL_TYPE).asString();
			var purpose = referenceNode.get(ModelJsonUtil.MODEL_PURPOSE).asString();
			return new ModelRefKey(dmId, modelType, purpose);
		}
	}

	private static void removeDuplicateReferences(JsonNode model) {

		JsonNode header = model.get(ModelJsonUtil.HEADER);
		if (header == null)
			return;

		JsonNode refs = header.get(ModelJsonUtil.MODEL_REFERENCES);
		if (refs == null)
			return;

		ArrayNode modelReferences = (ArrayNode) refs;
		var knownRefs = new HashSet<ModelRefKey>();

		for (int i = modelReferences.size() - 1; i >= 0; i--) {

			JsonNode referenceNode = modelReferences.get(i);
			if (!ModelJsonUtil.isSupportedModelReference(referenceNode))
				continue;//we merge model references in the header for purposes that kernel deals with

			var modelKey = ModelRefKey.of(referenceNode);
			if (!knownRefs.add(modelKey)) {
				modelReferences.remove(i);
			}
		}
	}

	private static void moveToIncludeConfig(ObjectNode groupNode,
			Map<String, @NonNull String> modelReferencesByAlias) {

		var modelIncludeConfigNd = moveModelAliasToNewModelIncludeConfig(groupNode, modelReferencesByAlias);

		moveExcludeRules(modelIncludeConfigNd, groupNode);
		moveExcludeComputations(modelIncludeConfigNd, groupNode);
		moveIncludeLevel(modelIncludeConfigNd, groupNode);

	}

	private static void moveExcludeComputations(ObjectNode modelIncludeConfigNd, ObjectNode containerAsObj) {
		var excludeComputations = containerAsObj.get(ModelJsonUtil.EXCLUDE_COMPUTATION);
		if (excludeComputations != null) {
			modelIncludeConfigNd.set(ModelJsonUtil.EXCLUDE_COMPUTATION, excludeComputations);
			containerAsObj.remove(ModelJsonUtil.EXCLUDE_COMPUTATION);
		}
	}

	private static void moveExcludeRules(ObjectNode modelIncludeConfigNd, ObjectNode containerAsObj) {
		var excludeRules = containerAsObj.get(ModelJsonUtil.EXCLUDE_RULES);
		if (excludeRules != null) {
			modelIncludeConfigNd.set(ModelJsonUtil.EXCLUDE_RULES, excludeRules);
			containerAsObj.remove(ModelJsonUtil.EXCLUDE_RULES);
		}
	}

	private static void moveIncludeLevel(ObjectNode modelIncludeConfigNd, ObjectNode containerAsObj) {
		var includeLevel = containerAsObj.get(ModelJsonUtil.INCLUDE_LEVEL);
		if (includeLevel != null) {
			modelIncludeConfigNd.set(ModelJsonUtil.INCLUDE_LEVEL, includeLevel);
			containerAsObj.remove(ModelJsonUtil.INCLUDE_LEVEL);
		}
	}

	private static ObjectNode moveModelAliasToNewModelIncludeConfig(ObjectNode groupNode,
			Map<String, @NonNull String> modelReferencesByAlias) {
		JsonNode modelAliasNode = groupNode.get(ModelJsonUtil.MODEL_ALIAS);
		if (modelAliasNode == null || !modelAliasNode.isString()) {
			throw MigrationUtils.contentNotSupportedException("The document model contains erroneous '" + ModelJsonUtil.MODEL_ALIAS + "'.");
		}

		var alias = modelAliasNode.asString();
		if (!modelReferencesByAlias.containsKey(alias)) {
			throw MigrationUtils.contentNotSupportedException("The document model contains unresolved '" + ModelJsonUtil.MODEL_ALIAS + " " + alias + "'.");
		}

		String modelReference = modelReferencesByAlias.get(alias);

		var modelIncludeConfigNd = new ObjectNode(JsonNodeFactory.instance);
		modelIncludeConfigNd.put(ModelJsonUtil.MODEL_REFERENCE,
				modelReference);

		groupNode.remove(ModelJsonUtil.MODEL_ALIAS);
		groupNode.set(ModelJsonUtil.MODEL_INCLUDE_CONFIG, modelIncludeConfigNd);

		return modelIncludeConfigNd;
	}

	private static void deleteModelAlias(JsonNode modelReferenceContainer) {
		var containerAsObj = (ObjectNode) modelReferenceContainer;
		containerAsObj.remove(ModelJsonUtil.MR_ALIAS);
	}

	private static String getModelAlias(JsonNode modelReferenceContainer) {
		var containerAsObj = (ObjectNode) modelReferenceContainer;
		return Optional.ofNullable(
				containerAsObj.get(ModelJsonUtil.MR_ALIAS)).map(JsonNode::asString).orElse(null);
	}

	private static String getModelReference(JsonNode modelReferenceContainer) {
		var containerAsObj = (ObjectNode) modelReferenceContainer;
		return containerAsObj.get(ModelJsonUtil.MR_REFERENCE).asString();
	}
}