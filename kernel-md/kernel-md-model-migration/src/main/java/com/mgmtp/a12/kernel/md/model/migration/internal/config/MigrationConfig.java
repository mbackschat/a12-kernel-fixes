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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationCode;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.internal.InternalConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.internal.InternalStep;

import lombok.Getter;

@Getter
public class MigrationConfig {

	static class Builder {
		private InternalConfig internalConfig;
		private ICustomMigrationConfig customConfig;

		Builder internalConfig(InternalConfig internalConfig) {
			this.internalConfig = internalConfig;
			return this;
		}

		Builder customConfig(ICustomMigrationConfig customConfig) {
			this.customConfig = customConfig;
			return this;
		}

		/**
		 * If internal intermediate steps are executed on the way to a version, the custom code to
		 * be executed beforehand must be executed before the first of these internal intermediate
		 * steps. This is necessary because external projects do not know the internal intermediate
		 * steps; the custom code is always assigned to externally visible versions.
		 *
		 * @return a map with key targetModelVersion of an step and value equal to the code which should be executed before this
		 * 		step.
		 */
		private Map<String, ICustomMigrationCode> assignCustomCodeBeforeToFittingVersion() {
			Map<String, ICustomMigrationCode> map = new HashMap<>();

			String firstVersionOfSeriesOfInternalVersions = null;
			for (InternalStep step : internalConfig.getMigrationSteps()) {
				boolean isInternalVersion = step.getIsInternalVersion(); // Default is 'null' auto-unboxing is thus safe.
				if (isInternalVersion) {
					if (firstVersionOfSeriesOfInternalVersions == null) {
						firstVersionOfSeriesOfInternalVersions = step.getTargetModelVersion();
					}
					continue;
				}
				// Access customConfig only with externally visible target versions.
				Optional<ICustomMigrationCode> ccBefore = customConfig.getCustomCodeBefore(step.getTargetModelVersion());
				if (ccBefore.isPresent()) {
					String v = firstVersionOfSeriesOfInternalVersions == null ? step.getTargetModelVersion()
							: firstVersionOfSeriesOfInternalVersions;
					map.put(v, ccBefore.get());
				}
				firstVersionOfSeriesOfInternalVersions = null;
			}
			return map;
		}

		MigrationConfig build() {
			String finalModelVersion = internalConfig.getMigrationSteps()
					.get(internalConfig.getMigrationSteps().size() - 1).getTargetModelVersion();

			Map<String, ICustomMigrationCode> ccBeforeMap = assignCustomCodeBeforeToFittingVersion();
			List<MigrationStepConfigType> migrationSteps = new ArrayList<>();

			for (InternalStep step : internalConfig.getMigrationSteps()) {
				ICustomMigrationCode ccBefore = ccBeforeMap.get(step.getTargetModelVersion());
				boolean isInternalVersion = step.getIsInternalVersion();
				// Access customConfig only with externally visible target versions.
				Optional<ICustomMigrationCode> customCodeAfter = isInternalVersion ? Optional.empty()
						: customConfig.getCustomCodeAfter(step.getTargetModelVersion());
				ICustomMigrationCode ccAfter = customCodeAfter.orElse(null);

				migrationSteps.add(new MigrationStepConfigType(
						step.getBean(),
						step.getSourceModelVersion(),
						step.getTargetModelVersion(),
						step.getIsInternalVersion(),
						ccBefore,
						ccAfter));
			}

			return new MigrationConfig(finalModelVersion, migrationSteps, customConfig);
		}
	}

	static Builder builder() {
		return new Builder();
	}

	private final String finalModelVersion;
	private final List<MigrationStepConfigType> migrationSteps;
	private final ICustomMigrationConfig customConfig;

	private MigrationConfig(String finalModelVersion, List<MigrationStepConfigType> migrationSteps,
			ICustomMigrationConfig customConfig) {
		this.finalModelVersion = finalModelVersion;
		this.migrationSteps = Collections.unmodifiableList(migrationSteps);
		this.customConfig = customConfig;
	}

	@Override
	public String toString() {
		return String.format("MigrationConfig [migrationSteps=%s]", migrationSteps);
	}
}
