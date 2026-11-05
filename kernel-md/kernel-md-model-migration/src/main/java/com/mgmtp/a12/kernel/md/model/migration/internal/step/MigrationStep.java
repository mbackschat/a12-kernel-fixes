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

import java.util.List;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.DmToMigrate;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationStepConfig;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * Migration step.
 */
@RequiredArgsConstructor
public class MigrationStep {
	protected final MigrationStepConfig stepConfig;
	protected final ICustomMigrationConfig customMigConfig;
	protected final Implementation implementation;

	@FunctionalInterface
	public interface Implementation {
		void execute(@NonNull List<DmToMigrate> dmsToMigrate, @NonNull ICustomMigrationConfig customMigConfig);
	}

	public static Implementation wrapPerDmImplementation(@NonNull MigrationStep.PerDmImplementation perDemImplementation) {
		return (List<DmToMigrate> dmsToMigrate, ICustomMigrationConfig customMigConfig) ->
				dmsToMigrate.forEach(dm ->
						dm.migrationResult().catchAndReportErrorsDuring(() ->
								perDemImplementation.execute(dm.jsonNode(), dm.migrationResult(), customMigConfig)));
	}

	@FunctionalInterface
	public interface PerDmImplementation {
		void execute(@NonNull JsonNode model, @NonNull MigrationLogger migrationLogger, ICustomMigrationConfig customMigConfig);

	}

	public void execute(List<DmToMigrate> dmsToMigrate) {
		implementation.execute(dmsToMigrate, customMigConfig);
	}

	/**
	 * Gets expected source model version.
	 *
	 * @return the source model version
	 */
	public String getSourceModelVersion() {
		return stepConfig.sourceModelVersion();
	}

	/**
	 * Gets version of document model after migration.
	 *
	 * @return the target version
	 */
	public String getTargetModelVersion() {
		return stepConfig.targetModelVersion();
	}

	/**
	 * @return the configuration of this step
	 */
	public MigrationStepConfig getConfig() {
		return stepConfig;
	}

	@Override public String toString() {
		return stepConfig.sourceModelVersion() + " -> " + stepConfig.targetModelVersion();
	}
}
