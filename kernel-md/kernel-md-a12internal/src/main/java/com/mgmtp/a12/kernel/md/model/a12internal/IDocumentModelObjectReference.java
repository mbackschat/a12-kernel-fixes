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

package com.mgmtp.a12.kernel.md.model.a12internal;

import java.io.Serializable;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;

/**
 * Models a {@link DocumentModel} object which is referenced relative to the reference holder.
 * <p>
 * Provides access to the relative path of the object (based off of the reference holder) and to the object itself if and only if it
 * was succesfully retrieved.
 *
 * @param <T>
 * 		specific type of the referenced {@link DocumentModel} object.
 */
@OnlyForA12InternalUsage
public interface IDocumentModelObjectReference<T> extends Serializable {
	/**
	 * @return the relative path to referenced {@link DocumentModel} object from the reference holder. May not be {@code null}.
	 * Note: if the reference holder is a group, the path will be relative to the fullname of the group. It the reference holder
	 * is a rule, computation or field, the path will be relative to the fullname of that rule, computation or field (i.e. it will
	 * not be relative to the parent group of the field, as one might potentially expect).
	 */
	String getRelativePath();

	/**
	 * @param relativePath
	 * 		the relativePath of the referenced {@link DocumentModel} object. May not be {@code null} or blank.
	 */
	void setRelativePath(String relativePath);

	/**
	 * @return the {@link DocumentModel} object which is referenced by its id. It is empty if the
	 * 		object itself has not been retrieved yet (e.g. before expanding the document model)
	 * 		or could not be retrieved (e.g. the object does not exist).
	 */
	Optional<T> getDocumentModelObject();

	/**
	 * @param documentModelObject
	 * 		the actual {@link DocumentModel} object which is referenced by its id. May be
	 *        {@code null} to unset.
	 */
	void setDocumentModelObject(T documentModelObject);

}
