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

package com.mgmtp.a12.kernel.core.rt.a12internal.validation;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Contains the causing entities.
 * <p>
 * This class should not be implemented outside kernel.
 * <p>
 * NOTE: currently the only implementation is ErrorCause and some kernel code relies on this.
 */
@OnlyForUsage
public interface ICause {
	/**
	 * Returns the causing entities. Groups of entities are gathered to prioritize them.
	 * <p>
	 * The current implementation uses following group assignments.
	 * <ul><li>For {@link IResult.ErrorType#OMISSION_ERROR} the first group of entities will consist of empty entities
	 * building the entities causing the omision error. The second group will consist of the given entities.</li>
	 * <li>For {@link IResult.ErrorType#VALUE_ERROR} all potentially causing entities will be returned as a single group.</li>
	 * </ul>
	 * <p>
	 * Future implementations may create other or more groups. But we always try to follow the rule: Fields of the earlier groups
	 * should be more probable to cause the errors from a technical point of view.
	 */
	List<Set<IIdentifier>> getCausingEntityGroups();

	/**
	 * @return the entity specifying the iteration, the error occurred in.
	 * 		<p>
	 * 		For generated code prior to 28.2.0, null is returned.
	 */
	default IIdentifier getIterationEntity() {
		return null;
	}

	/**
	 * return an ICause that is like the one that the method gets called on, but map all identifiers in getCausingEntityGroups and
	 * getIterationEntity according to the parameter {@code mapIdentifiers}
	 */
	// NOTE: the default implementation for this method can probably be removed after the method has existed for a few releases
	default ICause withMappedIdentifiers(Function<IIdentifier, IIdentifier> mapIdentifiers) {
		throw new RuntimeException("withMappedIdentifiers is not implemented. This should not happen");
		// we expect that ICause is only implemented by kernel (currently only ErrorCause from kernel-core-runtime),
		// and all implementations in kernel are already updated to implement withMappedIdentifiers
	}
}
