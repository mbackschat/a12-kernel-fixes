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

package com.mgmtp.a12.kernel.md.model.migration.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.core.utils.internal.TopologicalSortHelper;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelMigrator;
import com.mgmtp.a12.kernel.md.model.api.services.migration.IDocumentModelMigrator;
import com.mgmtp.a12.kernel.md.model.api.services.migration.IDocumentModelMigratorResult;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.api.services.migration.exceptions.DocumentModelMigratorException;
import com.mgmtp.a12.kernel.md.model.migration.internal.builder.JsonMigratorBuilder;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationStepConfigType;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigratorConfigLoader;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.DocumentModelMigratorResultImpl;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.DmsTopologicalSorter;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.ObjectMapperProvider;

import lombok.NonNull;

public class DocumentModelMigratorImpl implements IDocumentModelMigrator {

	private final MigrationCache migrationCache;
	private final List<String> supportedTargetVersions;
	private final String oldestSupportedSourceVersion;
	private final MigrationConfig mergedConfig;

	/**
	 * @param customConfig
	 *        {@code null} if no customization is needed.
	 */
	public DocumentModelMigratorImpl(ICustomMigrationConfig customConfig) {
		this(new MigrationCache(), customConfig);
	}

	/**
	 * @param migrationCache
	 * 		must not be {@code null}.
	 * @param customConfig
	 *        {@code null} if no customization is needed.
	 */
	public DocumentModelMigratorImpl(MigrationCache migrationCache, ICustomMigrationConfig customConfig) {
		this(migrationCache, "/internal/json/config/MigratorConfig.json", customConfig);
	}

	/**
	 * Note: This constructor is only needed for testing purposes.
	 *
	 * @param customConfig
	 *        {@code null} if no customization is needed.
	 * @param internalConfigFile
	 */
	DocumentModelMigratorImpl(MigrationCache migrationCache, String internalConfigFile, ICustomMigrationConfig customConfig) {
		this.migrationCache = migrationCache;
		this.mergedConfig = new MigratorConfigLoader().load(internalConfigFile, customConfig);
		List<String> targetVersions = mergedConfig.getMigrationSteps().stream()
				.filter(m -> !m.isInternalVersion())
				.map(MigrationStepConfigType::getTargetModelVersion)
				.collect(Collectors.toList());
		this.oldestSupportedSourceVersion = mergedConfig.getMigrationSteps().get(0).getSourceModelVersion();
		targetVersions.add(0, oldestSupportedSourceVersion);
		this.supportedTargetVersions = Collections.unmodifiableList(targetVersions);
	}

	@Override
	public synchronized @NonNull Collection<IDocumentModelMigratorResult> migrate(@NonNull Collection<String> docModelsToMigrate,
			String targetVersion) {
		DocumentModelMigrator.FailableDocModelsById loadedDMs = createDocModelsById(docModelsToMigrate);

		if (loadedDMs.hasMigrationErrors()) {
			return loadedDMs.migrationResults();
		} else {
			return migrate(loadedDMs.docModelsById(), targetVersion);
		}
	}

	public Collection<IDocumentModelMigratorResult> migrate(Map<String, JsonNode> docModelsById, String targetVersion) {
		// create a valid migration order for the document models
		DmsTopologicalSorter sorter = DmsTopologicalSorter.ofByIdMap(docModelsById);
		TopologicalSortHelper.TopologicalSortResult<String> sortResult = sorter.sort();
		if (!sortResult.isSuccessful()) {
			throw new IllegalArgumentException("Following document models " +
					"create a cycle: " + sortResult.getCycle().stream().collect(Collectors.joining(" -> ")));
		}

		// do the migration of document models in the determined order
		List<IDocumentModelMigratorResult> result = new ArrayList<>();
		for (String docModelId : sortResult.getSort()) {
			JsonNode docModel = docModelsById.get(docModelId);
			MigrationResult migrationResult;
			try {
				migrationResult = migrate(docModel, targetVersion);
			} catch (DocumentModelMigratorException e) {
				result.add(new DocumentModelMigratorResultImpl(docModelId, e));
				continue;
			}
			MigrationResultType resultType = migrationResult.getType();
			switch (resultType) {
				case SUCCESSFUL, NOT_REQUIRED ->
						result.add(new DocumentModelMigratorResultImpl(docModelId, write(docModel), resultType));
				case FAILED -> result.add(new DocumentModelMigratorResultImpl(docModelId,
						new DocumentModelMigratorException(createErrorMessage(migrationResult))));
				default -> result.add(new DocumentModelMigratorResultImpl(docModelId, new DocumentModelMigratorException(
						String.format("Unknown migration return type '%s'.", resultType))));
			}
		}
		return result;
	}

	public static DocumentModelMigrator.FailableDocModelsById createDocModelsById(Collection<String> docModelsToMigrate) {
		LinkedHashMap<String, JsonNode> docModelsById = new LinkedHashMap<>();
		List<IDocumentModelMigratorResult> migrationErrors = new ArrayList<>();

		int i = 0;
		for (String docModelStr : docModelsToMigrate) {
			JsonNode docModel;
			i++;
			try {
				docModel = ModelJsonUtil.parseModel(docModelStr);
			} catch (IOException e) {
				migrationErrors.add(new DocumentModelMigratorResultImpl("inputModel" + i,
						new DocumentModelMigratorException("Could not read JSON representation of DocumentModel.", e)));
				continue;
			}
			if (!ModelJsonUtil.isValidDocumentModel(docModel)) {
				migrationErrors.add(new DocumentModelMigratorResultImpl("inputModel" + i,
						new DocumentModelMigratorException("The following is not a valid DocumentModel:\n" + docModelStr)));
				continue;
			}
			docModelsById.put(ModelJsonUtil.extractModelId(docModel), docModel);
		}
		return new DocumentModelMigrator.FailableDocModelsById(docModelsById, migrationErrors);
	}

	private String write(JsonNode docModel) {
		try {
			StringWriter writer = new StringWriter();
			ObjectMapperProvider.createPreconfiguredObjectMapper().writeValue(writer, docModel);
			return writer.toString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private String createErrorMessage(MigrationResult result) {
		StringBuilder sb = new StringBuilder("Migration failed!");
		if (!result.getErrors().isEmpty()) {
			sb.append("\nErrors:");
			for (final MigrationError error : result.getErrors()) {
				sb.append("\n" + error.toString());
			}
		}
		if (!result.getInfos().isEmpty()) {
			sb.append("\nInfos:");
			for (final MigrationInfo info : result.getInfos()) {
				sb.append("\n" + info);
			}
		}
		return sb.toString();
	}

	public MigrationResult migrateToLatestVersion(final JsonNode jsonModel) throws DocumentModelMigratorException {
		String latestVersion = supportedTargetVersions.get(supportedTargetVersions.size() - 1);
		return migrate(jsonModel, latestVersion);
	}

	public MigrationResult migrate(final JsonNode jsonModel, final String targetVersion) throws DocumentModelMigratorException {
		if (targetVersion != null && !supportedTargetVersions.contains(targetVersion)) {
			throw new IllegalArgumentException(
					String.format("Target version '%s' is not supported. Please use one of the following values: '%s'",
							targetVersion, supportedTargetVersions.stream().collect(Collectors.joining(", "))));
		}
		// no schema validation for json
		return new JsonMigratorBuilder()
				.withMigrationCache(migrationCache)
				.withMigrationConfig(mergedConfig)
				.build()
				.migrate(jsonModel, targetVersion);
	}

	@Override
	public @NonNull List<String> getSupportedTargetVersions() {
		return supportedTargetVersions;
	}

	@Override
	public String getOldestSupportedSourceVersion() {
		return oldestSupportedSourceVersion;
	}
}
