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

import com.mgmtp.a12.kernel.md.model.migration.internal.step.MigrationStep;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record InternalStep(
		@NonNull String oldVersion,
		@NonNull String newVersion,
		String description, // only for us internally
		MigrationStep.Implementation forwardMigration,
		MigrationStep.Implementation backwardMigration,
		boolean newVersionIsInternal
) {
	public static InternalStepBuilder builder(String oldVersion, String newVersion) {
		return new InternalStepBuilder().oldVersion(oldVersion).newVersion(newVersion);
	}

	public static class InternalStepBuilder {
		public InternalStepBuilder forwardMigrationPerDm(@NonNull MigrationStep.PerDmImplementation migrationFunc) {
			return forwardMigration(MigrationStep.wrapPerDmImplementation(migrationFunc));
		}

		public InternalStepBuilder backwardMigrationPerDm(@NonNull MigrationStep.PerDmImplementation migrationFunc) {
			return backwardMigration(MigrationStep.wrapPerDmImplementation(migrationFunc));
		}
	}

	public @NonNull String getTargetVersion(boolean isForwardMigration) {
		return isForwardMigration ? newVersion : oldVersion;
	}

	public @NonNull String getSourceVersion(boolean isForwardMigration) {
		return isForwardMigration ? oldVersion : newVersion;
	}
}
