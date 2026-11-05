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

package com.mgmtp.a12.kernel.md.model.migration.internal.json;

import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.services.migration.IDocumentModelMigratorResult;
import com.mgmtp.a12.kernel.md.model.api.services.migration.exceptions.DocumentModelMigratorException;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationResult;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationResultType;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public class DocumentModelMigratorResultImpl implements IDocumentModelMigratorResult {
	private final @NonNull String documentModelId;
	private final @NonNull String originalInputString;
	private final @NonNull MigrationResultType type;
	private final String migratedDocumentModel;
	private final MigrationResult migrationResult;
	private final DocumentModelMigratorException exception;

	public static DocumentModelMigratorResultImpl successfulOrNotRequired(
			@NonNull String documentModelId,
			@NonNull String originalInputString,
			@NonNull MigrationResultType successOrSkip,
			@NonNull String migratedDocModel,
			@NonNull MigrationResult migrationResult
	) {
		return new DocumentModelMigratorResultImpl(
				documentModelId, originalInputString, successOrSkip, migratedDocModel, migrationResult, null);
	}

	public static DocumentModelMigratorResultImpl failed(
			@NonNull String documentModelId,
			@NonNull String originalInputString,
			@NonNull DocumentModelMigratorException exception
	) {
		return new DocumentModelMigratorResultImpl(
				documentModelId, originalInputString, MigrationResultType.FAILED, null, null, exception);
	}

	@Override public Optional<String> getMigratedDocumentModel() {
		return Optional.ofNullable(migratedDocumentModel);
	}

	@Override public Optional<DocumentModelMigratorException> getException() {
		return Optional.ofNullable(exception);
	}
}
