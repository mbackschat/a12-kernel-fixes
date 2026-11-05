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

package com.mgmtp.a12.kernel.md.model.api.services.migration;

import java.util.Collection;
import java.util.List;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * This service provides migration of JSON representations of {@link IDocumentModel}s.
 */
@OnlyForUsage
public interface IDocumentModelMigrator {

	/**
	 * @param docModelsToMigrate
	 * 		the collection of {@link IDocumentModel}s as string which should be migrated. All the {@link IDocumentModel}s
	 * 		contained in the workspace are required, since with Kernel version 28.0.0, there is a migration step which needs the
	 * 		information of the referenced {@link IDocumentModel}s.
	 * 		The collection must not be {@code null} or empty or contain {@code null} as collection item.
	 * @param targetVersion
	 * 		Version of the returned {@link IDocumentModel}s. Pass {@code null} if you want to
	 * 		migrate to the last element in the list provided by {@link #getSupportedTargetVersions()}. If you pass another value,
	 * 		the value must be included in the list, otherwise a {@link IllegalArgumentException} is thrown.
	 *
	 * @return a collection with the migration results, see also {@link IDocumentModelMigratorResult}. Is never {@code null} nor
	 * 		empty.
	 */
	@NonNull Collection<IDocumentModelMigratorResult> migrate(@NonNull Collection<String> docModelsToMigrate, String targetVersion);

	/**
	 * @return the list of target versions to which this service can migrate JSON representations of
	 * 		the {@link IDocumentModel}s. Never {@code null}. If there are no supported target
	 * 		versions, the list is empty.<br>
	 * 		<b>Attention:</b> these versions are not kernel versions. These are versions of the
	 *        {@link IDocumentModel} that
	 * 		are compatible with specific kernel versions. Versions of internal intermediate
	 * 		states are not included here
	 * 		(these occur when several migration steps are necessary within a development cycle).
	 * 		If this is a forward migrator, the list is ordered from oldest to newest, in case of a backwards migrator,
	 * 		from newest to oldest supported target version.
	 */
	@NonNull List<String> getSupportedTargetVersions();

	/**
	 * @return the first supported version of the {@link IDocumentModel} to be migrated from.
	 * 		For forward migration this yields the oldest supported source version.
	 * 		For backward migration this yields the newest supported source version.
	 */
	String getFirstSupportedSourceVersion();
}
