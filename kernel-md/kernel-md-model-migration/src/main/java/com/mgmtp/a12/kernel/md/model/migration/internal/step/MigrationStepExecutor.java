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

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationCode;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationError;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationResult;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationResultType;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * Executes migration steps in right order, the list of steps will be calculated based on source
 * model version
 */
@RequiredArgsConstructor
public class MigrationStepExecutor {
	private final @NonNull MigrationConfig migrationConfig;

	/**
	 * Execute all migration steps, that are configured for given source model version.
	 *
	 * @param model
	 * 		the document to migrate
	 * @param migrationResult
	 * 		the migration result
	 */
	public void execute(final JsonNode model, final MigrationResult migrationResult, final String targetModelVersion) {
		final String sourceModelVersion = ModelJsonUtil.extractModelVersion(model);

		// set source model version of result
		migrationResult.setSourceModelVersion(sourceModelVersion);

		if (Objects.equals(targetModelVersion, sourceModelVersion) ||
				Objects.equals(migrationConfig.getMigrationSteps().getLast().getTargetModelVersion(), sourceModelVersion)) {
			// if target version equals the current version of the model ==> don't migrate
			// or if model is up-to-date ==> don't migrate
			migrationResult.setTargetModelVersion(sourceModelVersion);
			migrationResult.setType(MigrationResultType.NOT_REQUIRED);
			return;
		}

		// if model is obsolete ==> execute migration --------------------------------------------------------------

		final List<MigrationStep> migrationSteps = getStepsFor(sourceModelVersion, targetModelVersion);
		for (MigrationStep migrationStep : migrationSteps) {
			// 1.) Validate preconditions
			if (!Objects.equals(ModelJsonUtil.extractModelVersion(model), migrationStep.getSourceModelVersion())) {
				throw new MigrationException(
						new MigrationError(MigrationErrorType.MODEL_CONTENT_UNEXPECTED_SOURCE_VERSION).withMessage(String
								.format(
										"Precondition of step '%s' violated: Expected source model version = '%s' is not " +
												"equal to document model version '%s'",
										migrationStep.getClass().getSimpleName(),
										migrationStep.getSourceModelVersion(),
										ModelJsonUtil.extractModelVersion(model))));

			}

			// 2.) Execute custom code before migration
			executeCustomCode(model, migrationStep.getConfig().customCodeBefore());

			// 3.) Migrate document
			migrationStep.execute(model, migrationResult);

			// 4.) Update document model version
			ModelJsonUtil.setVersion(model, migrationStep.getTargetModelVersion());

			// 5.) Execute custom code after migration
			executeCustomCode(model, migrationStep.getConfig().customCodeAfter());

			// 6.) Update target model version of result
			migrationResult.setTargetModelVersion(migrationStep.getTargetModelVersion());
		}
	}

	private void executeCustomCode(JsonNode model, ICustomMigrationCode customCode) {
		if (customCode != null) {
			customCode.execute(model);
		}
	}

	private List<MigrationStep> getStepsFor(final String sourceModelVersion, final String targetModelVersion) {
		List<MigrationStep> allMigSteps = migrationConfig.getMigrationSteps();
		if (allMigSteps.isEmpty()) {
			// in case of empty config ==> do nothing
			return Collections.emptyList();
		}
		boolean sourceModelVersionReached = false;
		final ArrayList<MigrationStep> steps = new ArrayList<>();
		for (MigrationStep migStep : allMigSteps) {
			if (!sourceModelVersionReached && !Objects.equals(sourceModelVersion, migStep.getSourceModelVersion())) {
				continue;
			}
			sourceModelVersionReached = true;

			// use only steps between Source- and TargetModelVersion
			steps.add(migStep);

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
}
