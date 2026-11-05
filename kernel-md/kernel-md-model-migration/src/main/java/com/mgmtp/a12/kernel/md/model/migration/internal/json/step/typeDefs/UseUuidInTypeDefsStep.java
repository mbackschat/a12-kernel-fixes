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

package com.mgmtp.a12.kernel.md.model.migration.internal.json.step.typeDefs;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.CustomMigrationConfigForTests;
import com.mgmtp.a12.kernel.md.model.migration.internal.DmToMigrate;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationError;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.DmsTopologicalSorter;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;

/**
 * In this class, following logic is implemented:
 * <ul>
 *     <li>migration of typeDefs: the existing id should be replaced by an uuid</li>
 *     <li>references to the old typeDef-ids should be updated</li>
 * </ul>
 */
public final class UseUuidInTypeDefsStep {

	private UseUuidInTypeDefsStep() {
	}

	public static void execute(List<DmToMigrate> dmsToMigrate, ICustomMigrationConfig customMigConfig) {

		//To avoid complicated test cases, we need to access a test specfic map, to pre initialize the cache here
		Map<String, String> customTypeDefUuidsByUniqueId = customMigConfig instanceof CustomMigrationConfigForTests customMigrationConfig
				? customMigrationConfig.typeDefUuidsByUniqueIdForTest()
				: Map.of();
		MigrationCache cache = new MigrationCache(customTypeDefUuidsByUniqueId);
		DmsTopologicalSorter.forEachDmSorted(dmsToMigrate, (dm, dmsById) ->
				dm.migrationResult().catchAndReportErrorsDuring(() -> migrateDm(dm, dmsById, cache)));
	}

	private static void migrateDm(DmToMigrate dm, Map<String, List<DmToMigrate>> dmsById, MigrationCache cache) {
		String docModelId = ModelJsonUtil.extractModelId(dm.jsonNode());

		// ################## local typeDefs ##################
		updateLocalTypeDefs(dm, docModelId, cache);

		Collection<JsonNode> typeDefTypes = ModelJsonUtil.extractTypeDefTypes(dm.jsonNode());
		if (typeDefTypes.isEmpty()) {
			// no references to typeDefs -> nothing to update
			return;
		}

		// ################## referenced typeDefs (local and imported) ##################
		List<JsonNode> unresolvedTypeDefTypes = updateTypeDefRefsAndGetUnresolved(docModelId, typeDefTypes, cache);

		// are all TD-refs resolved -> nothing else to migrate
		if (unresolvedTypeDefTypes.isEmpty()) {
			return;
		}

		// ################## included typeDefs ##################
		IncludedTypeDefsExtractionResult extractionResult = collectTypeDefUuidsByIncludedIds(dm.jsonNode(), dmsById, cache);
		Optional<MigrationError> extractionError = extractionResult.getError();
		if (extractionError.isPresent()) {
			throw new MigrationException(extractionError.get());
		}
		List<String> unresolvableTypeDefTypes = updateIncludedTypeDefsAndGetUnresolved(unresolvedTypeDefTypes,
				extractionResult.getTypeDefUuidsByIncludedId());
		if (!unresolvableTypeDefTypes.isEmpty()) {
			throw new MigrationException(new MigrationError(MigrationErrorType.TYPE_DEF_MISSING,
					"Following type definitions [" + String.join(", ", unresolvableTypeDefTypes)
							+ "] cannot be resolved. " +
							"This could be because there might be DMs that are referenced but cannot be resolved."));
		}
	}

	/**
	 * Update all the local typeDefs (typeDefs which are definied in the document model to migrate) with an uuid and add it to the
	 * caches
	 */
	private static void updateLocalTypeDefs(DmToMigrate dm, String docModelId, MigrationCache cache) {
		Set<JsonNode> typeDefs = ModelJsonUtil.extractTypeDefs(dm.jsonNode());

		for (JsonNode typeDef : typeDefs) {
			String typeDefId = ModelJsonUtil.getTypeDefId(typeDef);
			String uniqueId = createUniqueId(docModelId, typeDefId);

			if (cache.containsTypeDefUniqueId(uniqueId)) {
				throw new MigrationException(new MigrationError(MigrationErrorType.TYPE_DEF_ID_COLLISION_FOR_NONUNIQE_DM_ID,
						"Type definition ids must be unique within the scope of a document model id, but multiple " +
								"type definitions appear to exist for document model id '" + docModelId + "' and type definition id '" + typeDefId + "'. " +
								"It is likely that this problem is related to the presence of multiple document models with the " +
								"same id."));
			}

			String uuid = determineUuid(uniqueId, cache);
			// update caches (also add if the uuid is from the custom type def)
			cache.addTypeDefUuidByUniqueId(uniqueId, uuid);

			// set the uuid in the typeDef
			ModelJsonUtil.setTypeDefId(typeDef, uuid);
		}
	}

	private static String determineUuid(String uniqueId, MigrationCache cache) {
		// look first in the custom uuids
		if (cache.containsCustomTypeDefUuid(uniqueId)) {
			return cache.getCustomTypeDefUuid(uniqueId);
		} else {
			// otherwise generate new id typedef_<uuid>
			return createUUID();
		}
	}

	/**
	 * Update all the typeDef references (local and imported - defined in TDMs) with the corresponding uuid
	 *
	 * @return a list of typeDefTypes with unresolved typeDef reference
	 */
	private static List<JsonNode> updateTypeDefRefsAndGetUnresolved(String docModelId, Collection<JsonNode> typeDefTypes,
			MigrationCache cache) {
		// the typeDefRefs are grouped by if there is a UUID present or not
		return typeDefTypes.stream()
				.map(typeDefType -> {
					Optional<String> uuid = findCorrespondingUuid(typeDefType, docModelId, cache);
					if (uuid.isPresent()) {
						ModelJsonUtil.setTypeDefRefId(typeDefType, uuid.get());
						return null;
					}
					// cannot be resolved
					return typeDefType;
				})
				.filter(Objects::nonNull)
				.toList();
	}

	private static Optional<String> findCorrespondingUuid(JsonNode typeDefType, String docModelId, MigrationCache cache) {
		String typeDefRefId = ModelJsonUtil.getTypeDefRefId(typeDefType);
		// first with the original ref (case of imported typeDefs)
		if (cache.containsTypeDefUniqueId(typeDefRefId)) {
			return Optional.of(cache.getTypeDefUuid((typeDefRefId)));
		}
		// otherwise look with the unique id (case of local typeDefs)
		String uniqueId = createUniqueId(docModelId, typeDefRefId);
		return Optional.ofNullable(cache.getTypeDefUuid(uniqueId));
	}

	private static MigrationError toMigrationError(MigrationErrorType type, String message) {
		return new MigrationError(type, message);
	}

	/**
	 * Collect all the (transitive) typeDef included ids: the "path" of the including group-ids,
	 * e.g. include group with id incl_123 whose included model also contains a further including group with id incl_456
	 * the last included model contains a TD with id typeDef_789,
	 * then the typeDef included id would be incl_123_incl_456_typeDef_789
	 *
	 * @return a map of typeDef uuid to typeDef included id
	 */
	private static IncludedTypeDefsExtractionResult collectTypeDefUuidsByIncludedIds(JsonNode model,
			Map<String, List<DmToMigrate>> dmsById, MigrationCache cache) {
		Collection<JsonNode> includingGroups = ModelJsonUtil.extractUnexpandedIncludingGroupsByModelAlias(model);
		String docModelId = ModelJsonUtil.extractModelId(model);
		Map<String, String> includedDmReferenceByAlias = ModelJsonUtil.createIncludedDmReferenceByAlias(model, false);

		IncludedTypeDefsExtractionResult result = new IncludedTypeDefsExtractionResult();
		for (JsonNode includingGroup : includingGroups) {
			var error =
					processIncludingGroup(includingGroup, includedDmReferenceByAlias, docModelId, dmsById, cache, result);
			if (error.isPresent()) {
				return new IncludedTypeDefsExtractionResult(error.get());
			}
		}
		return result;
	}

	private static Optional<MigrationError> processIncludingGroup(JsonNode includingGroup,
			Map<String, String> includedDmReferenceByAlias, String docModelId, Map<String, List<DmToMigrate>> dmsById,
			MigrationCache cache, IncludedTypeDefsExtractionResult result) {
		String refOrAlias = ModelJsonUtil.getModelAlias(includingGroup);
		String groupId = ModelJsonUtil.extractGroupId(includingGroup);
		var reference = includedDmReferenceByAlias.get(refOrAlias);
		if (reference == null) {
			return Optional.of(toMigrationError(MigrationErrorType.UNKNOWN_REFERENCED_MODEL,
					"For the referenced document model '" + refOrAlias + "' in the include with the id '"
							+ groupId + "', there is no corresponding model reference in header."));
		}

		// If no model with the referenced id is found, we'll still continue (and only fail if a referenced typedef
		// cannot be resolved (which might be a result of the non-resolvable DM reference)
		// If ids are ambiguous, there might be more than one match - multiple candidates should not normally occur,
		// but we might be able to continue anyway...
		for (DmToMigrate includedDm : dmsById.getOrDefault(reference, List.of())) {
			var error = gatherLocalTypeDefs(includedDm.jsonNode(), groupId, refOrAlias, reference, docModelId, cache, result);
			if (error.isPresent()) {
				return error;
			}
			error = gatherTransitivelyIncludedTypeDefs(includedDm.jsonNode(), groupId, dmsById, cache, result);
			if (error.isPresent()) {
				return error;
			}
		}
		return Optional.empty();
	}

	private static Optional<MigrationError> gatherLocalTypeDefs(JsonNode includedDm, String groupId,
			String refOrAlias, String reference, String docModelId, MigrationCache cache, IncludedTypeDefsExtractionResult result) {
		for (JsonNode typeDef : ModelJsonUtil.extractTypeDefs(includedDm)) {
			// already migrated
			String typeDefUuid = ModelJsonUtil.getTypeDefId(typeDef);
			if (!cache.containsTypeDefUuid(typeDefUuid)) {
				return Optional.of(toMigrationError(MigrationErrorType.TYPE_DEF_MISSING,
						"Type definition with id '" + typeDefUuid + "' in included document model with id  '" + refOrAlias
								+ "' cannot be resolved, please make sure to migrate the entire workspace at once."));
			}
			String uniqueId = cache.getTypeDefUniqueId(typeDefUuid);
			String typeDefId = getTypeDefId(uniqueId, reference);
			String typeDefIdViaIncluded = createUniqueId(groupId, typeDefId);
			if (result.containsKey(typeDefIdViaIncluded) && !result.get(typeDefIdViaIncluded).equals(typeDefUuid)) {
				return Optional.of(toMigrationError(MigrationErrorType.TYPE_DEF_ID_COLLISION_FOR_NONUNIQE_DM_ID,
						"Type definition ids must be unique within the scope of a document model id, but multiple typedefs appear "
								+ "to exist for document model id '" + docModelId + "' and type definition id '"
								+ typeDefId + "'. It is likely that this problem is related to the presence " +
								"of multiple document models with the same id."));
			}
			result.put(typeDefIdViaIncluded, typeDefUuid);
		}
		return Optional.empty();
	}

	private static Optional<MigrationError> gatherTransitivelyIncludedTypeDefs(JsonNode includedDm,
			String groupId, Map<String, List<DmToMigrate>> dmsById, MigrationCache cache, IncludedTypeDefsExtractionResult result) {
		//recursive call
		IncludedTypeDefsExtractionResult tmpResult = collectTypeDefUuidsByIncludedIds(includedDm, dmsById, cache);
		if (tmpResult.getError().isPresent()) {
			return tmpResult.getError();
		}
		for (var entry : tmpResult.getTypeDefUuidsByIncludedId().entrySet()) {
			String typeDefIdViaTransitiveInclude = entry.getKey();
			String typeDefUuid = entry.getValue();
			String typeDefIdViaInclude = createUniqueId(groupId, typeDefIdViaTransitiveInclude);
			if (result.containsKey(typeDefIdViaInclude) && !result.get(typeDefIdViaInclude).equals(typeDefUuid)) {
				return Optional.of(toMigrationError(MigrationErrorType.INCLUDED_TYPE_DEF_ID_COLLISION,
						"Cannot unambiguously resolve id of included type definition " + typeDefIdViaInclude +
								": candidates are " + typeDefUuid + " and " + result.get(typeDefIdViaInclude) +
								". This is likely due to invalid " + "data where multiple document models have the" +
								" same id"));
			}
			result.put(typeDefIdViaInclude, typeDefUuid);
		}
		return Optional.empty();
	}

	/**
	 * Update all the included typeDefs (defined in DMs) with an uuid
	 *
	 * @return the list of unresolvable typeDefTypes
	 */
	private static List<String> updateIncludedTypeDefsAndGetUnresolved(List<JsonNode> typeDefTypes,
			Map<String, String> typeDefUuidsByIncludedId) {
		return typeDefTypes.stream()
				.map(typeDefType -> {
					String typeDefRefId = ModelJsonUtil.getTypeDefRefId(typeDefType);
					if (typeDefUuidsByIncludedId.containsKey(typeDefRefId)) {
						ModelJsonUtil.setTypeDefRefId(typeDefType, typeDefUuidsByIncludedId.get(typeDefRefId));
						return null;
					}
					// cannot be resolved
					return typeDefRefId;
				})
				.filter(Objects::nonNull)
				.toList();
	}

	/**
	 * like ImportTypeDefsVisitor#updateId
	 */
	private static String createUniqueId(String docModelId, String typeDefId) {
		return docModelId + "_" + typeDefId;
	}

	private static String createUUID() {
		return "typedef_" + UUID.randomUUID();
	}

	private static String getTypeDefId(String typeDefUniqueId, String docModelId) {
		return typeDefUniqueId.replace(docModelId + "_", "");
	}

	public static class IncludedTypeDefsExtractionResult {

		private Map<String, String> typeDefUuidsByIncludedId = new HashMap<>();
		private MigrationError error;

		IncludedTypeDefsExtractionResult(MigrationError error) {
			this.typeDefUuidsByIncludedId = new HashMap<>();
			this.error = error;
		}

		IncludedTypeDefsExtractionResult() {
		}

		void put(String typeDefIncludedId, String typeDefUuid) {
			this.typeDefUuidsByIncludedId.put(typeDefIncludedId, typeDefUuid);
		}

		Map<String, String> getTypeDefUuidsByIncludedId() {
			return typeDefUuidsByIncludedId;
		}

		Optional<MigrationError> getError() {
			return Optional.ofNullable(error);
		}

		String get(String key) {
			return typeDefUuidsByIncludedId.get(key);
		}

		boolean containsKey(String key) {
			return typeDefUuidsByIncludedId.containsKey(key);
		}
	}

	private static class MigrationCache {
		/**
		 * Since type definitions should have a corresponding uuid, we cache the type definition uuids based on the original unique
		 * id (model id + local type def id).
		 */
		private final Map<String, String> typeDefUuidsByUniqueId = new HashMap<>();
		/**
		 * like the previous ona, only the other way around.
		 */
		private final Map<String, String> typeDefUniqueIdsByUuid = new HashMap<>();

		/**
		 * Intended to be used in tests to get deterministic results.
		 */
		private Map<String, String> customTypeDefUuidsByUniqueId = new HashMap<>();

		public MigrationCache(Map<String, String> customTypeDefUuidsByUniqueId) {
			this.customTypeDefUuidsByUniqueId = customTypeDefUuidsByUniqueId;
		}

		public void addTypeDefUuidByUniqueId(String uniqueId, String uuid) {
			this.typeDefUuidsByUniqueId.put(uniqueId, uuid);
			this.typeDefUniqueIdsByUuid.put(uuid, uniqueId);
		}

		public String getTypeDefUuid(String uniqueId) {
			return typeDefUuidsByUniqueId.get(uniqueId);
		}

		public boolean containsTypeDefUniqueId(String uniqueId) {
			return typeDefUuidsByUniqueId.containsKey(uniqueId);
		}

		public String getTypeDefUniqueId(String uuid) {
			return typeDefUniqueIdsByUuid.get(uuid);
		}

		public boolean containsTypeDefUuid(String uuid) {
			return typeDefUniqueIdsByUuid.containsKey(uuid);
		}

		public boolean containsCustomTypeDefUuid(String uniqueId) {
			return customTypeDefUuidsByUniqueId.containsKey(uniqueId);
		}

		public String getCustomTypeDefUuid(String uniqueId) {
			return customTypeDefUuidsByUniqueId.get(uniqueId);
		}

	}

}
