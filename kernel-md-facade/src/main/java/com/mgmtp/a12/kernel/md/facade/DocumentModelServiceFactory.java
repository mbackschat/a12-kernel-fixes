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

package com.mgmtp.a12.kernel.md.facade;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.services.migration.IDocumentModelMigrator;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelSearchServiceImpl;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.kernel.md.model.migration.internal.DocumentModelMigratorImpl;
import com.mgmtp.a12.kernel.md.serializer.MDSerializerFactory;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Factory provides access to services related to {@link IDocumentModel}s.
 */
@OnlyForUsage
public class DocumentModelServiceFactory {

	/**
	 * Creates an {@link IDocumentModelSerializer}.
	 *
	 * @return the corresponding instance. Is never {@code null}.
	 */
	public @NonNull IDocumentModelSerializer createDocumentModelSerializer() {
		return new MDSerializerFactory().createDocumentModelSerializer();
	}

	/**
	 * Creates a service that offers functions on an {@link IDocumentModel} like for example:
	 * consistency check, code generation, etc. (for more information see
	 * {@link IDocumentModelService}}).
	 *
	 * @return the corresponding instance. Is never {@code null}.
	 */
	public @NonNull IDocumentModelService createDocumentModelService() {
		return new ExternalDocumentModelService();
	}

	/**
	 * Creates a service which offers search functions for specific elements in an
	 * {@link IDocumentModel}: fields, groups, rules and computations (for more information see
	 * {@link IDocumentModelSearchService}).
	 *
	 * @param documentModel
	 * 		the document model in which you want to search.
	 * @return the corresponding instance. Is never {@code null}.
	 */
	public @NonNull IDocumentModelSearchService createDocumentModelSearchService(IDocumentModel documentModel) {
		return new DocumentModelSearchServiceImpl(documentModel);
	}

	/**
	 * Creates an {@link IDocumentModelMigrator}.
	 *
	 * @param customConfig
	 * 		This parameter can be used to make project-specific configurations to the
	 * 		migration. If this is not necessary, {@code null} is passed.
	 * @return the corresponding instance. Never {@code null}.
	 */
	public @NonNull IDocumentModelMigrator createDocumentModelMigrator(ICustomMigrationConfig customConfig) {
		return DocumentModelMigratorImpl.createForwardMigrator(customConfig);
	}

	/**
	 * Creates an {@link IDocumentModelMigrator} for backward migration.
	 *
	 * @param customConfig
	 * 		This parameter can be used to make project-specific configurations to the
	 * 		migration. If this is not necessary, {@code null} is passed.
	 * @return the corresponding instance.
	 */
	public @NonNull IDocumentModelMigrator createBackwardDocumentModelMigrator(ICustomMigrationConfig customConfig) {
		return DocumentModelMigratorImpl.createBackwardMigrator(customConfig);
	}
}
