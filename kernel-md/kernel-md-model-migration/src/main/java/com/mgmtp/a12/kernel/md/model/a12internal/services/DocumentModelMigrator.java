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

import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.migration.internal.DocumentModelMigratorImpl;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationResultType;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * This service offers migration of document models.
 */
@OnlyForUsage
public class DocumentModelMigrator {

	/**
	 * @param models
	 * 		the collection of document models as string which should be migrated. If a document model references other models, all
	 * 		referenced document models must also be part of this collection (i.e. transitively).
	 * 		The collection must not be {@code null} or empty or contain {@code null} as collection item.
	 * 		All collection items must be valid document model strings with unique ids.
	 * @return a collection with the migration results, see also {@link MigrationResult}. Is never {@code null} or empty. The order
	 * 		of results may not match the order of the given {@code models}.
	 */
	public @NonNull List<@NonNull MigrationResult> migrate(@NonNull List<@NonNull String> models) {
		return DocumentModelMigratorImpl.createForwardMigrator(null).migrateInternal(models, null).stream()
				.map(result -> {
					String docModelId = result.getDocumentModelId();
					MigrationResult.Status resultType = mapResultType(result.getType());
					String resultModel = resultType == MigrationResult.Status.SUCCESS
							? result.getMigratedDocumentModel().orElseThrow()
							: result.getOriginalInputString();
					Optional<String> errorMessage = result.getException().map(Exception::getMessage);
					return new MigrationResult(resultModel, docModelId, resultType, errorMessage);
				})
				.toList();
	}

	private static MigrationResult.Status mapResultType(MigrationResultType type) {
		return switch (type) {
			case SUCCESSFUL -> MigrationResult.Status.SUCCESS;
			case FAILED -> MigrationResult.Status.ERROR;
			case NOT_REQUIRED -> MigrationResult.Status.SKIPPED;
		};
	}
}
