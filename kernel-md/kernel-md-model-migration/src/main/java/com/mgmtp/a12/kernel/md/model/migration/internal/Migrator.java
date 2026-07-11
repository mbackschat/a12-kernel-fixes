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

import static com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType.UNKNOWN_ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;
import com.mgmtp.a12.kernel.md.model.migration.internal.step.MigrationStepExecutor;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * This class orchestrates the migration process of document model.
 */
@RequiredArgsConstructor
public class Migrator {

	protected static final Logger LOG = LoggerFactory.getLogger(Migrator.class);

	private final @NonNull MigrationConfig config;

	/**
	 * Conduct the actual migration. Migrates the model document in-place.
	 */
	public MigrationResult migrate(final JsonNode model, final String targetModelVersion) {
		final MigrationStepExecutor stepExecutor = new MigrationStepExecutor(config);

		try {
			// 2.) validate source model
			if (!ModelJsonUtil.isValidDocumentModel(model)) {
				throw new MigrationException(
						new MigrationError(MigrationErrorType.MODEL_CONTENT_NOT_SUPPORTED)
								.withMessage("The model does not seem to be a valid document model."));
			}

			final MigrationResult migrationResult = new MigrationResult(MigrationResultType.SUCCESSFUL);

			// 3.) execute migration steps
			stepExecutor.execute(model, migrationResult, targetModelVersion);

			// Note: we do not verify the result in any way. This should be done using the consistency check.
			return migrationResult;
		} catch (MigrationException migExcp) {
			return MigrationResult.createWithError(migExcp.getError());
		} catch (Exception e) {
			LOG.warn("", e);
			return MigrationResult.createWithError(new MigrationError(UNKNOWN_ERROR).withMessage(e.getMessage()));
		}
	}

}
