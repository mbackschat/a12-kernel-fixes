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

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationError;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationStepConfigType;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.BaseJsonNodeMigrationStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

/**
 * In this class, following logic is implemented:
 * <ul>
 *     <li>migration of typeDefs: the existing id should be replaced by an uuid</li>
 *     <li>references to the old typeDef-ids should be updated</li>
 * </ul>
 */
public class UseUuidInTypeDefsStep extends BaseJsonNodeMigrationStep {

	public UseUuidInTypeDefsStep(MigrationCache cache, MigrationStepConfigType stepConfigType,
			ICustomMigrationConfig customMigConfig) {
		super(cache, stepConfigType, customMigConfig);
	}

	/**
	 * assumptions:
	 * - the document models to migrate are topologically sorted -> document models in docModelsById are already migrated and added
	 * to the cache
	 * - document model ids are unique
	 *
	 * @param model
	 * 		the model to migrate
	 * @param migrationLogger
	 * 		the logger to report occurred errors and infos
	 */
	@Override public void execute(JsonNode model, MigrationLogger migrationLogger) {
		String docModelId = ModelJsonUtil.extractModelId(model);

		// ################## local typeDefs ##################
		updateLocalTypeDefs(model, docModelId);

		Collection<JsonNode> typeDefTypes = ModelJsonUtil.extractTypeDefTypes(model);
		if (typeDefTypes.isEmpty()) {
			// no references to typeDefs -> nothing to update
			cache.addDocModelById(docModelId, model);
			return;
		}

		// ################## referenced typeDefs (local and imported) ##################
		List<JsonNode> unresolvedTypeDefTypes = updateTypeDefRefsAndGetUnresolved(docModelId, typeDefTypes);

		// are all TD-refs resolved -> nothing else to migrate
		if (unresolvedTypeDefTypes.isEmpty()) {
			cache.addDocModelById(docModelId, model);
			return;
		}

		// ################## included typeDefs ##################
		IncludedTypeDefsExtractionResult extractionResult = collectTypeDefUuidsByIncludedIds(model);
		Optional<MigrationException> extractionException = extractionResult.getException();
		if (extractionException.isPresent()) {
			cache.addDocModelById(docModelId, model);
			throw extractionException.get();
		}
		List<String> unresolvableTypeDefTypes = updateIncludedTypeDefsAndGetUnresolved(unresolvedTypeDefTypes,
				extractionResult.getTypeDefUuidsByIncludedId());
		if (!unresolvableTypeDefTypes.isEmpty()) {
			cache.addDocModelById(docModelId, model);
			throw new MigrationException(new MigrationError(MigrationErrorType.TYPE_DEF_MISSING)
					.withMessage("Following type definitions [" + String.join(", ", unresolvableTypeDefTypes)
							+ "] cannot be resolved. " +
							"This could be because there might be DMs that are referenced but cannot be resolved."));
		}
		cache.addDocModelById(docModelId, model);
	}

	/**
	 * Update all the local typeDefs (typeDefs which are definied in the document model to migrate) with an uuid and add it to the
	 * caches
	 *
	 * @param model
	 * 		the document model to migrate
	 */
	private void updateLocalTypeDefs(JsonNode model, String docModelId) {
		Set<JsonNode> typeDefs = ModelJsonUtil.extractTypeDefs(model);

		for (JsonNode typeDef : typeDefs) {
			String typeDefId = ModelJsonUtil.getTypeDefId(typeDef);
			String uniqueId = createUniqueId(docModelId, typeDefId);

			if (cache.containsTypeDefUniqueId(uniqueId)) {
				throw new MigrationException(new MigrationError(MigrationErrorType.TYPE_DEF_ID_COLLISION_FOR_NONUNIQE_DM_ID)
						.withMessage("Type definition ids must be unique within the scope of a document model id, but multiple " +
								"type definitions appear to exist for document model id '" + docModelId + "' and type definition id '" + typeDefId + "'. " +
								"It is likely that this problem is related to the presence of multiple document models with the " +
								"same id."));
			}

			String uuid = determineUuid(uniqueId);
			// update caches (also add if the uuid is from the custom type def)
			cache.addTypeDefUuidByUniqueId(uniqueId, uuid);

			// set the uuid in the typeDef
			ModelJsonUtil.setTypeDefId(typeDef, uuid);
		}
	}

	private String determineUuid(String uniqueId) {
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
	private List<JsonNode> updateTypeDefRefsAndGetUnresolved(String docModelId, Collection<JsonNode> typeDefTypes) {
		// the typeDefRefs are grouped by if there is a UUID present or not
		return typeDefTypes.stream()
				.map(typeDefType -> {
					Optional<String> uuid = findCorrespondingUuid(typeDefType, docModelId);
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

	private Optional<String> findCorrespondingUuid(JsonNode typeDefType, String docModelId) {
		String typeDefRefId = ModelJsonUtil.getTypeDefRefId(typeDefType);
		// first with the original ref (case of imported typeDefs)
		if (cache.containsTypeDefUniqueId(typeDefRefId)) {
			return Optional.of(cache.getTypeDefUuid((typeDefRefId)));
		}
		// otherwise look with the unique id (case of local typeDefs)
		String uniqueId = createUniqueId(docModelId, typeDefRefId);
		return Optional.ofNullable(cache.getTypeDefUuid(uniqueId));
	}

	/**
	 * Collect all the (transitive) typeDef included ids: the "path" of the including group-ids,
	 * e.g. include group with id incl_123 whose included model also contains a further including group with id incl_456
	 * the last included model contains a TD with id typeDef_789,
	 * then the typeDef included id would be incl_123_incl_456_typeDef_789
	 *
	 * @return a map of typeDef uuid to typeDef included id
	 */
	private IncludedTypeDefsExtractionResult collectTypeDefUuidsByIncludedIds(JsonNode model) {
		Collection<JsonNode> includingGroups = ModelJsonUtil.extractUnexpandedIncludingGroups(model);
		String docModelId = ModelJsonUtil.extractModelId(model);

		IncludedTypeDefsExtractionResult result = new IncludedTypeDefsExtractionResult();

		Map<String, String> includedDmReferenceByAlias = ModelJsonUtil.createIncludedDmReferenceByAlias(model, false);

		for (JsonNode includingGroup : includingGroups) {
			String alias = ModelJsonUtil.getModelAlias(includingGroup);
			String reference = includedDmReferenceByAlias.get(alias);
			String groupId = ModelJsonUtil.extractGroupId(includingGroup);
			if (reference == null) {
				return new IncludedTypeDefsExtractionResult(MigrationErrorType.UNKNOWN_REFERENCED_MODEL,
						"For the referenced document model '" + alias + "' in the include with the id '"
								+ groupId + "', there is no corresponding model reference in header.");
			}

			List<JsonNode> includedDmCandidates = cache.getDocModels(reference);
			// If no model with the referenced id is found, we'll still continue (and only fail if a referenced typedef
			// cannot be resolved (which might be a result of the non-resolvable DM reference)
			// If ids are ambiguous, there might be more than one match
			for (JsonNode includedDm : includedDmCandidates) { // multiple candidates should not normally occur, but we might be able to continue anyway...
				// first gather the local typeDefs in includedDm
				for (JsonNode typeDef : ModelJsonUtil.extractTypeDefs(includedDm)) {
					// already migrated
					String typeDefUuid = ModelJsonUtil.getTypeDefId(typeDef);

					if (!cache.containsTypeDefUuid(typeDefUuid)) {
						return new IncludedTypeDefsExtractionResult(MigrationErrorType.TYPE_DEF_MISSING,
								"Type definition with id '" + typeDefUuid + "' in included document model with id  '" + reference
										+ "' cannot be resolved, please make sure to migrate the entire workspace at once.");
					}
					String uniqueId = cache.getTypeDefUniqueId(typeDefUuid);
					String typeDefId = getTypeDefId(uniqueId, reference);
					String typeDefIdViaIncluded = createUniqueId(groupId, typeDefId);
					if (result.containsKey(typeDefIdViaIncluded) && !result.get(typeDefIdViaIncluded).equals(typeDefUuid)) {
						return new IncludedTypeDefsExtractionResult(MigrationErrorType.TYPE_DEF_ID_COLLISION_FOR_NONUNIQE_DM_ID,
								"Type definition ids must be unique within the scope of a document model id, but multiple typedefs appear "
										+ "to exist for document model id '" + docModelId + "' and type definition id '"
										+ typeDefId + "'. It is likely that this problem is related to the presence " +
										"of multiple document models with the same id.");
					}
					result.put(typeDefIdViaIncluded, typeDefUuid);
				}

				// gather transitively included typeDefs
				IncludedTypeDefsExtractionResult tmpResult = collectTypeDefUuidsByIncludedIds(includedDm);
				if (tmpResult.getException().isPresent()) {
					return tmpResult;
				}
				for (var entry : tmpResult.getTypeDefUuidsByIncludedId().entrySet()) {
					String typeDefIdViaTransitiveInclude = entry.getKey();
					String typeDefUuid = entry.getValue();

					String typeDefIdViaInclude = createUniqueId(groupId, typeDefIdViaTransitiveInclude);
					if (result.containsKey(typeDefIdViaInclude) && !result.get(typeDefIdViaInclude).equals(typeDefUuid)) {
						return new IncludedTypeDefsExtractionResult(MigrationErrorType.INCLUDED_TYPE_DEF_ID_COLLISION,
								"Cannot unambiguously resolve id of included type definition " + typeDefIdViaInclude +
										": candidates are " + typeDefUuid + " and " + result.get(typeDefIdViaInclude) +
										". This is likely due to invalid " + "data where multiple document models have the" +
										" same id");
					}

					result.put(typeDefIdViaInclude, typeDefUuid);
				}
			}

		}

		return result;
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
		private MigrationException exception;

		IncludedTypeDefsExtractionResult(MigrationErrorType errorType, String errorMsg) {
			this.typeDefUuidsByIncludedId = new HashMap<>();
			this.exception = new MigrationException(new MigrationError(errorType).withMessage(errorMsg));
		}

		IncludedTypeDefsExtractionResult() {
		}

		void put(String typeDefIncludedId, String typeDefUuid) {
			this.typeDefUuidsByIncludedId.put(typeDefIncludedId, typeDefUuid);
		}

		Map<String, String> getTypeDefUuidsByIncludedId() {
			return typeDefUuidsByIncludedId;
		}

		Optional<MigrationException> getException() {
			return Optional.ofNullable(exception);
		}

		String get(String key) {
			return typeDefUuidsByIncludedId.get(key);
		}

		boolean containsKey(String key) {
			return typeDefUuidsByIncludedId.containsKey(key);
		}
	}

}
