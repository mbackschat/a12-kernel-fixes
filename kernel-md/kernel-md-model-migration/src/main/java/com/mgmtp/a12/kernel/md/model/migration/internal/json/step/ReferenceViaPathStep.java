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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationError;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationStepConfigType;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.NodePath;

public class ReferenceViaPathStep extends BaseJsonNodeMigrationStep {

	public ReferenceViaPathStep(MigrationCache cache, MigrationStepConfigType stepConfigType,
			ICustomMigrationConfig customMigConfig) {
		super(cache, stepConfigType, customMigConfig);
	}

	@Override
	public void execute(JsonNode model, MigrationLogger migrationLogger) {
		// create following map: field path by field id (fieldPathById)
		Map<String, NodePath> fieldPathById = extractFieldByPathId(model);

		// extract the id of the current DM
		String docModelId = ModelJsonUtil.extractModelId(model);

		//	add fieldPathById to fieldPathByIdMapByDocModelId with the corresponding DM id
		cache.addFieldPathByIdMapByDocModelId(docModelId, fieldPathById);

		updateModel(model);
	}

	public Map<String, NodePath> extractFieldByPathId(JsonNode model) throws MigrationException {
		// extract all including groups (see also UseUuidInTypeDefsStep#collectTypeDefUuidsByIncludedIds)
		Collection<ModelJsonUtil.JsonNodeWithPath> includingGroups = ModelJsonUtil.extractUnexpandedIncludingGroupsWithPath(model);

		// create map: included DM reference by model alias
		Map<String, String> includedDmReferenceByAlias = ModelJsonUtil.createIncludedDmReferenceByAlias(model, true);
		Map<String, NodePath> fieldPathById = ModelJsonUtil.extractAllElementsWithPath(model, ModelJsonUtil.TYPE_FIELD).stream()
				.collect(Collectors.toMap(node -> node.node().get(ModelJsonUtil.FIELD_ID).asText(),
						ModelJsonUtil.JsonNodeWithPath::path));

		// if there is at least one, we do:
		// prepare a mapping of all the ids of fields in an including group to their absolute path (in the current DM)
		// for each including group
		for (ModelJsonUtil.JsonNodeWithPath includingGroup : includingGroups) {
			// 			extract the model alias, group name and id
			String alias = ModelJsonUtil.getModelAlias(includingGroup.node());
			String reference = includedDmReferenceByAlias.get(alias);
			String groupId = ModelJsonUtil.extractGroupId(includingGroup.node());
			NodePath groupPath = includingGroup.path(); // absolute path of (including) group

			// compute the included DM reference based on the model alias
			// if reference is null, we throw an exception
			if (reference == null) {
				throw new MigrationException(new MigrationError(MigrationErrorType.UNKNOWN_REFERENCED_MODEL).withMessage(
						"For the referenced document model '" + alias + "' in the include with the id '"
								+ groupId + "', there is no corresponding model reference in header."));
			}

			// traverse over all included DM candidates (the referenced model)
			// with the reference (DM id) get a map from fieldPathByIdMapByDocModelId
			Map<String, NodePath> fieldPathByIdForDM = cache.getFieldPathByIdMapByDocModelId(reference);

			// if no map found, we ignore it
			// otherwise, we go over all the entries of the map
			fieldPathByIdForDM.forEach((key, value) -> {
				// create a new id <group id>_<key of the map>, which is the "unique" id in the DM
				String id = groupId + '_' + key;

				// remove the root group name from the value of the map (field path)
				NodePath fieldPath = value.removeRootGroup();

				// create a new path <group path>/<result from last point>
				NodePath newFieldPath = groupPath.addSub(fieldPath);

				// add new information (new path + new id) to fieldPathById
				fieldPathById.put(id, newFieldPath);
			});
		}

		return fieldPathById;
	}

	public void updateModel(JsonNode model) throws MigrationException {
		// 	update references within the current DM
		//		extract the json nodes which reference a field
		//			if there is none, there is nothing to migrate
		//			otherwise, traverse all those field references
		//			update them with help of fieldPathById
		//				the important note at the Design of Solution in A12-15368 should be taken into account.
		//				identify references which could NOT be resolved
		List<String> nonResolvableFieldIds = new ArrayList<>(migrateIndexFields(model));
		nonResolvableFieldIds.addAll(migrateSortFields(model));
		nonResolvableFieldIds.addAll(migrateComputedFields(model));
		nonResolvableFieldIds.addAll(migrateErrorFields(model));

		// if there are no references which could NOT be resolved, we are done with the migration
		// if there are references which could NOT be resolved, throw an exception
		if (!nonResolvableFieldIds.isEmpty()) {
			throw new MigrationException(new MigrationError(MigrationErrorType.UNKNOWN_FIELD_ID).withMessage(
					String.format("The following field IDs could not be resolved: [%s]",
							String.join(", ", nonResolvableFieldIds))));
		}
	}

	private List<String> migrateIndexFields(JsonNode model) {
		String docModelId = ModelJsonUtil.extractModelId(model);
		Map<String, NodePath> fieldPathById = cache.getFieldPathByIdMapByDocModelId(docModelId);

		// Group.indexFieldId → Group.indexFieldName
		return ModelJsonUtil.extractAllElementsWithPath(model, ModelJsonUtil.TYPE_GROUP).stream()
				.filter(elem -> elem.node().get(ModelJsonUtil.TYPE_GROUP).has(ModelJsonUtil.INDEX_FIELD_ID))
				.map(elem -> {
					ObjectNode node = (ObjectNode) elem.node().get(ModelJsonUtil.TYPE_GROUP);
					String indexFieldId = node.get(ModelJsonUtil.INDEX_FIELD_ID).asText();
					NodePath resolvedField = fieldPathById.get(indexFieldId);

					// we collect the non resolvable field but continue with the migration
					if (resolvedField == null) {
						return indexFieldId;
					} else {
						// the index field in a group can only be in the group itself, therefore the path is just the field name
						TextNode value = new TextNode(resolvedField.getElementName());
						ModelJsonUtil.replaceProperty(node, ModelJsonUtil.INDEX_FIELD_ID, ModelJsonUtil.INDEX_FIELD_NAME,
								value);
						return null;
					}
				}).filter(Objects::nonNull).toList();
	}

	private List<String> migrateSortFields(JsonNode model) {
		String docModelId = ModelJsonUtil.extractModelId(model);
		Map<String, NodePath> fieldPathById = cache.getFieldPathByIdMapByDocModelId(docModelId);

		// Group.SortField.fieldId → Group.SortField.name
		return ModelJsonUtil.extractAllElementsWithPath(model, ModelJsonUtil.TYPE_GROUP).stream()
				.filter(elem -> elem.node().get(ModelJsonUtil.TYPE_GROUP).has(ModelJsonUtil.GROUP_SORT_FIELDS))
				.flatMap(elem -> {
					ArrayNode node = (ArrayNode) elem.node().get(ModelJsonUtil.TYPE_GROUP).get(ModelJsonUtil.GROUP_SORT_FIELDS);

					return StreamSupport.stream(node.spliterator(), false).map(jsonNode -> {
						ObjectNode sortField = (ObjectNode) jsonNode;
						String sortFieldId = sortField.get(ModelJsonUtil.SORT_FIELD_ID).asText();
						if (sortFieldId.isEmpty()) {
							ModelJsonUtil.replaceProperty(sortField, ModelJsonUtil.SORT_FIELD_ID, ModelJsonUtil.SORT_FIELD_NAME,
									new TextNode(""));
							return null;
						}

						NodePath resolvedField = fieldPathById.get(sortFieldId);

						if (resolvedField == null) {
							return sortFieldId;
						} else {
							// the sort field/s in a group can only be in the group itself, therefore the path is just the field name
							ModelJsonUtil.replaceProperty(sortField, ModelJsonUtil.SORT_FIELD_ID, ModelJsonUtil.SORT_FIELD_NAME,
									new TextNode(resolvedField.getElementName()));
							return null;
						}
					});
				}).filter(Objects::nonNull).toList();
	}

	private List<String> migrateComputedFields(JsonNode model) {
		String docModelId = ModelJsonUtil.extractModelId(model);
		Map<String, NodePath> fieldPathById = cache.getFieldPathByIdMapByDocModelId(docModelId);

		// Computation.computedFieldId → Computation.computedFieldRelPath
		return ModelJsonUtil.extractAllElementsWithPath(model, ModelJsonUtil.TYPE_COMPUTATION).stream()
				.filter(elem -> elem.node().get(ModelJsonUtil.TYPE_COMPUTATION).has(ModelJsonUtil.COMPUTED_FIELD_ID))
				.map(elem -> {
					ObjectNode node = (ObjectNode) elem.node().get(ModelJsonUtil.TYPE_COMPUTATION);
					String computedFieldId = node.get(ModelJsonUtil.COMPUTED_FIELD_ID).asText();
					if (computedFieldId.isEmpty()) {
						ModelJsonUtil.replaceProperty(node, ModelJsonUtil.COMPUTED_FIELD_ID, ModelJsonUtil.COMPUTED_FIELD_REL_PATH,
								new TextNode(""));
						return null;
					}

					// absolute path of field in current dm
					NodePath absolutePathInDM = fieldPathById.get(computedFieldId);

					if (absolutePathInDM == null) {
						return computedFieldId;
					} else {
						// absolute path of group in current dm
						NodePath computationPath = elem.path();

						// relative path to field starting from current group
						String relativeFieldPath = absolutePathInDM.relativize(computationPath);
						ModelJsonUtil.replaceProperty(node, ModelJsonUtil.COMPUTED_FIELD_ID, ModelJsonUtil.COMPUTED_FIELD_REL_PATH,
								new TextNode(relativeFieldPath));

						return null;
					}
				}).filter(Objects::nonNull).toList();
	}

	private List<String> migrateErrorFields(JsonNode model) {
		String docModelId = ModelJsonUtil.extractModelId(model);
		Map<String, NodePath> fieldPathById = cache.getFieldPathByIdMapByDocModelId(docModelId);

		// Rule.errorEntityId → Rule.errorEntityRelPath
		return ModelJsonUtil.extractAllElementsWithPath(model, ModelJsonUtil.TYPE_RULE).stream()
				.filter(elem -> elem.node().get(ModelJsonUtil.TYPE_RULE).has(ModelJsonUtil.ERROR_ENTITY_ID))
				.map(elem -> {
					ObjectNode node = (ObjectNode) elem.node().get(ModelJsonUtil.TYPE_RULE);
					String errorFieldId = node.get(ModelJsonUtil.ERROR_ENTITY_ID).asText();
					if (errorFieldId.isEmpty()) {
						ModelJsonUtil.replaceProperty(node, ModelJsonUtil.ERROR_ENTITY_ID, ModelJsonUtil.ERROR_ENTITY_REL_PATH,
								new TextNode(""));
						return null;
					}

					// absolute path of field in current dm
					NodePath absolutePathInDM = fieldPathById.get(errorFieldId);

					if (absolutePathInDM == null) {
						return errorFieldId;
					} else {
						// absolute path of group in current dm
						NodePath computationPath = elem.path();

						// relative path to field starting from current group
						String relativeFieldPath = absolutePathInDM.relativize(computationPath);
						ModelJsonUtil.replaceProperty(node, ModelJsonUtil.ERROR_ENTITY_ID, ModelJsonUtil.ERROR_ENTITY_REL_PATH,
								new TextNode(relativeFieldPath));

						return null;
					}
				}).filter(Objects::nonNull).toList();
	}
}
