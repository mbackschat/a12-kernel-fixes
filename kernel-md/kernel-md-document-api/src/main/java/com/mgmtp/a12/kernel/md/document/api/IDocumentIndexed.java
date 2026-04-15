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
package com.mgmtp.a12.kernel.md.document.api;

import java.util.Collection;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService;
import com.mgmtp.a12.kernel.md.utils.CustomImplementationIsOptional;

/**
 * An extension of {@link IDocument} which provides additional methods that are useful for gaining
 * performance while searching in the document via {@link IDocumentSearchService}.
 *
 * @deprecated use {@code DocumentV2} instead.
 */
@CustomImplementationIsOptional
@Deprecated(since = "30.0.0")
public interface IDocumentIndexed extends IDocument {

	/**
	 * @param path
	 * 		the path to search for, {@literal null} results in an empty collection, see also
	 *        {@link IEntityInstance#getPath()}.
	 * @return the entity instances with the same path as the given path. If no entity instances were
	 * 		found, an empty collection is returned.
	 */
	Collection<IEntityInstance> getEntityInstancesAtPath(final String path);

	/**
	 * @param path
	 * 		the path to search for, {@literal null} results in an empty collection, see also
	 *        {@link IEntityInstance#getPath()}.
	 * @return all entity instances whose path starts with the given path. If no entity instance
	 * 		were found, an empty collection is returned.
	 */
	Collection<IEntityInstance> getEntityInstancesAtOrBelowPath(final String path);

	/**
	 * @param path
	 * 		the path to search for, {@literal null} results in an empty collection. see also
	 *        {@link IEntityInstance#getPath()}.
	 * @param repetitions
	 * 		the repetitions to search for, {@literal null}
	 * 		results in an empty collection, see also {@link IEntityInstance#getRepetitions()}
	 * 		It should not contain {@value IEntityInstance#ALL_SEMANTIC}. If any repetition
	 * 		index is {@value IEntityInstance#ALL_SEMANTIC}, a {@link RuntimeException} is
	 * 		thrown.
	 * @return the entity instance whose path and repetitions are equal to the input, otherwise an
	 * 		empty optional.
	 */
	Optional<IEntityInstance> getEntityInstance(String path, int[] repetitions);
}
