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
package com.mgmtp.a12.kernel.md.model.migration.internal.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationCode;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationError;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationResult;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationResultType;
import com.mgmtp.a12.kernel.md.model.migration.internal.accessor.SchemaVersionAccessor;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationStepConfigType;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;

/**
 * Executes migration steps in right order, the list of steps will be calculated based on source
 * model version
 */
public class MigrationStepExecutor<T> {

	private final MigrationCache migrationCache;
	private final MigrationConfig migrationConfig;
	private final SchemaVersionAccessor<T> schemaVersionAccessor;
	private final MigrationStepFactory stepFactory;
	private String lastTargetModelVersion;

	/**
	 * Instantiates a new Migration step executor.
	 *
	 * @param migrationConfig
	 * 		the migration config
	 * @param schemaVersionAccessor
	 * 		the schema version accessor
	 */
	public MigrationStepExecutor(final MigrationCache migrationCache, final MigrationConfig migrationConfig,
			final SchemaVersionAccessor<T> schemaVersionAccessor) {
		Validate.notNull(migrationConfig, "migrationConfig must not be null");
		Validate.notNull(schemaVersionAccessor, "schemaVersionAccessor must not be null!");

		this.migrationCache = migrationCache;
		this.migrationConfig = migrationConfig;
		this.schemaVersionAccessor = schemaVersionAccessor;
		this.stepFactory = new MigrationStepFactory();
	}

	/**
	 * Execute all migration steps, that are configured for given source model version.
	 *
	 * @param model
	 * 		the document to migrate
	 * @param migrationResult
	 * 		the migration result
	 */
	public void execute(final T model, final MigrationResult migrationResult, final String targetModelVersion) {
		final String sourceModelVersion = schemaVersionAccessor.getVersion(model);

		// set source model version of result
		migrationResult.setSourceModelVersion(sourceModelVersion);

		if (Objects.equals(targetModelVersion, sourceModelVersion) ||
				Objects.equals(migrationConfig.getFinalModelVersion(), sourceModelVersion)) {
			// if target version equals the current version of the model ==> don't migrate
			// or if model is up-to-date ==> don't migrate
			migrationResult.setTargetModelVersion(sourceModelVersion);
			lastTargetModelVersion = sourceModelVersion;
			migrationResult.setType(MigrationResultType.NOT_REQUIRED);
			return;
		}

		// if model is obsolete ==> execute migration --------------------------------------------------------------

		final List<MigrationStep<T>> migrationSteps = getStepsFor(sourceModelVersion, targetModelVersion);
		for (MigrationStep<T> migrationStep : migrationSteps) {
			// 1.) Validate preconditions
			if (!Objects.equals(schemaVersionAccessor.getVersion(model), migrationStep.getSourceModelVersion())) {
				throw new MigrationException(
						new MigrationError(MigrationErrorType.MODEL_CONTENT_UNEXPECTED_SOURCE_VERSION).withMessage(String
								.format(
										"Precondition of step '%s' violated: Expected source model version = '%s' is not " +
												"equal to document model version '%s'",
										migrationStep.getClass().getSimpleName(),
										migrationStep.getSourceModelVersion(),
										schemaVersionAccessor.getVersion(model))));

			}

			// 2.) Execute custom code before migration
			executeCustomCode(model, migrationStep.getConfigType().getCustomCodeBefore());

			// 3.) Migrate document
			migrationStep.execute(model, migrationResult);

			// 4.) Update document model version
			schemaVersionAccessor.setVersion(model, migrationStep.getTargetModelVersion());

			// 5.) Execute custom code after migration
			executeCustomCode(model, migrationStep.getConfigType().getCustomCodeAfter());

			// 6.) Update target model version of result
			migrationResult.setTargetModelVersion(migrationStep.getTargetModelVersion());
			lastTargetModelVersion = migrationStep.getTargetModelVersion();
		}
	}

	private void executeCustomCode(final T model, ICustomMigrationCode customCode) {
		if (customCode == null) {
			return;
		}
		if (!(model instanceof JsonNode)) {
			throw new IllegalArgumentException("Type is not an instance of JsonNode.");
		}
		customCode.execute((JsonNode) model);
	}

	private List<MigrationStep<T>> getStepsFor(final String sourceModelVersion, final String targetModelVersion) {
		List<MigrationStepConfigType> allMigSteps = migrationConfig.getMigrationSteps();
		if (allMigSteps.isEmpty()) {
			// in case of empty config ==> do nothing
			return Collections.emptyList();
		}
		boolean sourceModelVersionReached = false;
		final ArrayList<MigrationStep<T>> steps = new ArrayList<>();
		for (MigrationStepConfigType migStep : allMigSteps) {
			if (!sourceModelVersionReached && !Objects.equals(sourceModelVersion, migStep.getSourceModelVersion())) {
				continue;
			}
			sourceModelVersionReached = true;

			// use only steps between Source- and TargetModelVersion
			steps.add(stepFactory.create(migrationCache, migStep, migrationConfig.getCustomConfig()));

			if (Objects.equals(targetModelVersion, migStep.getTargetModelVersion())) {
				break;
			}
		}

		if (!sourceModelVersionReached) {
			throw new MigrationException(new MigrationError(MigrationErrorType.UNSUPPORTED_SOURCE_MODEL_VERSION)
					.withMessage(String.format(
							"Source model version: '%s' could not be found in the given migration configuration",
							sourceModelVersion)));
		}

		return steps;
	}

	public String getLastTargetModelVersion() {
		return lastTargetModelVersion;
	}
}
