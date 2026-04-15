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

package com.mgmtp.a12.kernel.md.model.a12internal.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.api.services.migration.IDocumentModelMigratorResult;
import com.mgmtp.a12.kernel.md.model.migration.internal.DocumentModelMigratorImpl;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationResultType;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.DocumentModelMigratorResultImpl;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.model.migration.MigrationResult;
import com.mgmtp.a12.model.migration.MigrationTool;

/**
 * This service offers migration of document models.
 */
@OnlyForUsage
public class DocumentModelMigrator implements MigrationTool {

	private final Map<String, String> customTypeDefUuidsByUniqueId;

	public DocumentModelMigrator() {
		this(new HashMap<>());
	}

	/**
	 * @param customTypeDefUuidsByUniqueId
	 * 		Intended to be used in tests to get deterministic results.
	 */
	DocumentModelMigrator(Map<String, String> customTypeDefUuidsByUniqueId) {
		this.customTypeDefUuidsByUniqueId = customTypeDefUuidsByUniqueId;
	}

	public record FailableDocModelsById(LinkedHashMap<String, JsonNode> docModelsById,
										List<IDocumentModelMigratorResult> migrationResults) {
		public boolean hasMigrationErrors() {
			return !migrationResults.isEmpty();
		}
	}

	/**
	 * @param models
	 * 		the collection of document models as string which should be migrated. If a document model references other models, all
	 * 		referenced document models must also be part of this collection (i.e. transitively).
	 * 		The collection must not be {@code null} or empty or contain {@code null} as collection item.
	 * 		All collection items must be valid document model strings with unique ids.
	 * @return a collection with the migration results, see also {@link MigrationResult}. Is never {@code null} or empty. The order
	 * 		of results may not match the order of the given {@code models}.
	 */
	@Override
	public List<MigrationResult> migrate(List<String> models) {
		FailableDocModelsById loadedDMs = DocumentModelMigratorImpl.createDocModelsById(models);

		if (loadedDMs.hasMigrationErrors()) {
			return loadedDMs.migrationResults().stream().map(result ->
							new MigrationResult(null, result.getDocumentModelId(), MigrationResult.Status.ERROR,
									result.getException().map(Throwable::getMessage)))
					.toList();
		}

		LinkedHashMap<String, JsonNode> modelJsonsById = loadedDMs.docModelsById();
		Map<String, String> modelsById = getModelsById(models, modelJsonsById.keySet());

		DocumentModelMigratorImpl internalMigrator = new DocumentModelMigratorImpl(new MigrationCache(customTypeDefUuidsByUniqueId),
				null);
		List<MigrationResult> results = new ArrayList<>();
		for (IDocumentModelMigratorResult result : internalMigrator.migrate(modelJsonsById, null)) {
			DocumentModelMigratorResultImpl internalResult = (DocumentModelMigratorResultImpl) result;
			String docModelId = internalResult.getDocumentModelId();
			MigrationResult.Status resultType = mapResultType(internalResult.getType());
			String resultModel = resultType == MigrationResult.Status.SUCCESS
					? internalResult.getMigratedDocumentModel().orElseThrow()
					: modelsById.get(docModelId);
			Optional<String> errorMessage = internalResult.getException().map(Exception::getMessage);

			results.add(new MigrationResult(resultModel, docModelId, resultType, errorMessage));
		}

		return results;
	}

	/**
	 * Create a map from model id to input string. Relies on the Set provided via {@code modelIdsInSameOrder}
	 * being iterable in the same order as {@code models}.
	 */
	private static Map<String, String> getModelsById(List<String> models, Set<String> modelIdsInSameOrder) {
		Map<String, String> modelsById = new HashMap<>();
		int modelIndex = 0;
		for (String docModelId : modelIdsInSameOrder) {
			modelsById.put(docModelId, models.get(modelIndex));
			modelIndex++;
		}
		return modelsById;
	}

	private static MigrationResult.Status mapResultType(MigrationResultType type) {
		return switch (type) {
			case SUCCESSFUL -> MigrationResult.Status.SUCCESS;
			case FAILED -> MigrationResult.Status.ERROR;
			case NOT_REQUIRED -> MigrationResult.Status.SKIPPED;
		};
	}
}
