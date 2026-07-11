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

package com.mgmtp.a12.kernel.md.model.migration.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationCode;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.step.MigrationStep;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public class MigrationConfig {
	private final List<MigrationStep> migrationSteps;
	private final ICustomMigrationConfig customConfig;

	public static @NonNull MigrationConfig create(
			@NonNull List<InternalStep> internalSteps,
			@NonNull MigrationCache cache,
			ICustomMigrationConfig customConfigOrNull,
			boolean isForward
	) {
		var chunks = groupStepsIntoExternallyVisibleChunks(internalSteps);
		if (!isForward) {
			chunks = chunks.reversed();
			chunks.forEach(Collections::reverse);
		}

		ICustomMigrationConfig customConfig = customConfigOrNull != null ? customConfigOrNull : new ICustomMigrationConfig() {
		};

		List<MigrationStep> migrationSteps = new ArrayList<>();
		chunks.forEach(chunk ->
					migrationSteps.addAll(processChunk(cache, isForward, customConfig, chunk))
		);

		return new MigrationConfig(migrationSteps, customConfig);
	}


	/**
	 * create a {@link MigrationStep} for {@link InternalStep} and assign custom codes from custom config, where appropriate.
	 * Background: If internal intermediate steps are executed on the way to a version, the custom code to be executed beforehand must be
	 * executed before the first of these internal intermediate steps, i.e. before processing the first step of a chunk.
	 * This is necessary because external projects do not know the internal intermediate steps;
	 * the custom code is always assigned to externally visible versions.
	 */
	private static List<MigrationStep> processChunk(MigrationCache cache, boolean isForward, ICustomMigrationConfig customConfig,
			List<InternalStep> chunk) {

		List<MigrationStep> migrationSteps = new ArrayList<>();

		String externalTargetVersionOfChunk = chunk.getLast().getTargetVersion(isForward);
		ICustomMigrationCode customCodeBefore = customConfig.getCustomCodeBefore(externalTargetVersionOfChunk).orElse(null);
		ICustomMigrationCode customCodeAfter = customConfig.getCustomCodeAfter(externalTargetVersionOfChunk).orElse(null);

		Java8Utils.forEachIndexed(chunk, (i, step) -> {

			var targetVersionOfStep = step.getTargetVersion(isForward);
			var targetVersionOfStepIsInternal = ! (externalTargetVersionOfChunk.equals(targetVersionOfStep));

			MigrationStepConfig stepConfig =
					MigrationStepConfig.builder()
					.sourceModelVersion(step.getSourceVersion(isForward))
					.targetModelVersion( targetVersionOfStep)
					.targetVersionIsInternal(targetVersionOfStepIsInternal)
					.customCodeBefore(i == 0 ? customCodeBefore : null)
					.customCodeAfter(i == chunk.size() - 1 ? customCodeAfter : null)
					.build();
			var implementation = isForward ? step.forwardMigration() : step.backwardMigration();
			if (implementation != null) {
				migrationSteps.add(new MigrationStep(cache, stepConfig, customConfig, implementation));
			}
		});
		return migrationSteps;
	}

	/**
	 * consider all internal steps for a forward migration and group them into chunks, each each chunk consisting of intermediat
	 * internal target versions, and ending with a step towards an external target version
	 * (this is needed, to decide at which steps user custom code should be executed) 
	 */
	private static List<List<InternalStep>> groupStepsIntoExternallyVisibleChunks(List<InternalStep> internalSteps) {
		if (internalSteps.isEmpty()) {
			return List.of();
		}

		List<List<InternalStep>> result = new ArrayList<>();
		List<InternalStep> current = new ArrayList<>();

		for (InternalStep step : internalSteps) {
			current.add(step);
			if (!step.newVersionIsInternal()) {
				result.add(current);
				current = new ArrayList<>();
			}
		}

		if (!current.isEmpty()) {
			result.add(current);
		}

		return result;
	}

	@Override
	public String toString() {
		return String.format("MigrationConfig [migrationSteps=%s]", migrationSteps);
	}
}
