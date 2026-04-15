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

package com.mgmtp.a12.kernel.md.model.api.docs;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.migration.IDocumentModelMigrator;
import com.mgmtp.a12.kernel.md.model.api.services.migration.IDocumentModelMigratorResult;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationCode;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;

import lombok.NonNull;

public class DocumentModelMigrator {
	@Test
	void migrate() {
		String targetVersion = "";

		// tag::migration[]
		// Your own implementation of ICustomMigrationConfig
		ICustomMigrationConfig myCustomConfig = new ICustomMigrationConfig() {
			@Override
			public Optional<ICustomMigrationCode> getCustomCodeBefore(@NonNull String targetVersion) {
				return Optional.empty(); // Your implementation
			}

			@Override
			public Optional<ICustomMigrationCode> getCustomCodeAfter(@NonNull String targetVersion) {
				return Optional.empty(); // Your implementation
			}

			@Override
			public @NonNull TimeZone getTimezone(@NonNull String modelId) {
				return TimeZone.getTimeZone("UTC"); // Your implementation
			}
		};

		IDocumentModelMigrator migrator = new DocumentModelServiceFactory().createDocumentModelMigrator(myCustomConfig);

		// A collection of deserialized DMs
		Collection<String> docModelsToMigrate = List.of(
				// add your models here
		);

		// targetVersion is the version to which the model must be migrated to.
		Collection<IDocumentModelMigratorResult> migrationResult = migrator.migrate(docModelsToMigrate, targetVersion);
		// end::migration[]
	}
}
