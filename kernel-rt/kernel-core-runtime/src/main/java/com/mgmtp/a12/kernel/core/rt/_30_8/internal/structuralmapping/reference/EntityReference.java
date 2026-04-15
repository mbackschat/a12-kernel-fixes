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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.reference;

import java.util.Iterator;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document.EntityInstance;

/**
 * A reference from one or more entities to a target entity.
 * <p>
 * Implementations may assume only valid input for functions (i.e. caller is responsible to ensure correct input).
 */
public interface EntityReference<T extends EntityInstance> {
	/**
	 * @return the referenced entity. Never {@code null}.
	 */
	Entity referencedEntity();

	/**
	 * Retrieve an instance of {@link #referencedEntity} as if it was non-repeatably referenced (i.e. choose repetition
	 * {@literal 1} for every downward step taken on the path to the referenced entity). The document is not modified at any point.
	 * <p>
	 * If there is no instance present in the document (see {@link EntityInstance#isPresent()}), an immutable virtual instance is
	 * created instead (i.e. not inserted into the document).
	 *
	 * @param from
	 * 		the instance to use as a starting point. Must be a valid starting point for this reference. Must not be {@code null}.
	 * @return the instance of {@link #referencedEntity} that was referenced from the given entity instance. If the referenced
	 * 		instance is not present in the document, an immutable virtual instance is returned instead. Never {@code null}.
	 */
	T resolveInstance(EntityInstance from);

	/**
	 * Retrieve the instances of {@link #referencedEntity} present in the document that were referenced from the given starting
	 * point (i.e. iterate all repetitions for every downward step taken on the path to the referenced entity). The document is not
	 * modified at any point.
	 *
	 * @param from
	 * 		the instance to use as a starting point. Must be a valid starting point for this reference. Must not be {@code null}.
	 * @return an iterator over the instances of {@link #referencedEntity} present in the document that were referenced from the
	 * 		given entity instance.
	 */
	Iterator<T> resolvePresentInstances(EntityInstance from);
}
