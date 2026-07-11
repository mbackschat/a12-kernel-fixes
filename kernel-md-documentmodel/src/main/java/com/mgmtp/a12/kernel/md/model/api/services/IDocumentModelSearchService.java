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
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * This service offers the search functionality in {@link IDocumentModel}s.
 * <p>
 * <b>Important note:</b> To create an instance see
 * DocumentModelServiceFactory#createDocumentModelSearchService(IDocumentModel).
 */
@OnlyForUsage
public interface IDocumentModelSearchService {

	/**
	 * Searches for an {@link IElement} by the given id.
	 *
	 * @param id
	 * 		element id. May not be {@code null} or empty.
	 * @return the corresponding {@link IElement} or an empty Optional instance if no
	 *        {@link IElement} was found.
	 */
	Optional<IElement> getById(@NonNull String id);

	/**
	 * Searches for an {@link IElement} by the given path.
	 *
	 * @param path
	 * 		element path. May not be {@code null} or empty.
	 * @return the corresponding {@link IElement} or an empty Optional instance if no
	 *        {@link IElement} was found.
	 */
	Optional<IElement> getByPath(@NonNull String path);

	/**
	 * Searches for a group or field path by the given id.
	 *
	 * @param id
	 * 		element id. May not be {@code null} or empty.
	 * @return the corresponding path or an empty Optional instance if no
	 *        {@link IGroup} or {@link IField} was found.
	 */
	Optional<String> getGroupOrFieldPathById(String id);
}
