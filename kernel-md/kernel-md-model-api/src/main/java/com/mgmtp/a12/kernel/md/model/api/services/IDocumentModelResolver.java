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

package com.mgmtp.a12.kernel.md.model.api.services;

import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.header.Header;

import lombok.NonNull;

/**
 * Provides resolution of already expanded {@link IDocumentModel}s.
 * <p>
 * <b>Note:</b> The caller has to provide an implementation of this interface.
 */
public interface IDocumentModelResolver {

	/**
	 * This method is used to retrieve {@link IDocumentModel}s by {@link Header#getId()}.
	 *
	 * @param id
	 * 		the {@link IDocumentModel} id. Never {@code null} or blank.
	 * @return the already expanded {@link IDocumentModel} with the given id.
	 */
	IDocumentModel getDocumentModelById(@NonNull String id);

	/**
	 * This method is used to retrieve {@link IDocumentModelSearchService}s by
	 * {@link Header#getId()}.
	 * Providing an implementation of this method that returns an cached instance leads to
	 * significantly
	 * better performance especially when deserializing more documents for the model.<br>
	 * An instance of {@link IDocumentModelSearchService} can be created using
	 * {@code DocumentModelServiceFactory#createDocumentModelSearchService(IDocumentModel documentModel)}.
	 *
	 * @param documentModelId
	 * 		the {@link IDocumentModel} id. Never {@code null} or blank.
	 * @return the {@link IDocumentModelSearchService} for the given id. The default implementation
	 * 		returns {@link Optional#empty()}, the instance is then created e.g. for each document
	 * 		deserialization. Never returns {@code null}.
	 */
	default @NonNull Optional<IDocumentModelSearchService> getDocumentModelSearchService(@NonNull String documentModelId) {
		return Optional.empty();
	}
}
