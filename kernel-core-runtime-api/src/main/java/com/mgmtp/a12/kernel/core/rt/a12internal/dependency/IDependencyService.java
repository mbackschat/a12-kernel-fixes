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

package com.mgmtp.a12.kernel.core.rt.a12internal.dependency;

import java.util.List;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.model.utils.Experimental;

/**
 * Runtime service for the dependency modeling feature: it exposes the entities relevant for computing
 * hidden entities, computes the hidden entities for a given data set and post-processes a validation
 * {@link IResult} so that errors caused solely by hidden entities are handled accordingly.
 * <p>
 * The show/hide of the entities themselves is done by the caller; the kernel only
 * provides the information which entities are hidden and adapts the validation result.
 * <p>
 * A default (no-op) implementation is returned by {@link IDependencyFactory#getDependencyService()}
 * for generated code that does not carry a dependency model.
 */
@Experimental
public interface IDependencyService {

	/**
	 * Returns all input fields whose values are required to compute the hidden entities: the trigger
	 * fields plus the index fields needed to resolve semantic indices. The caller observes these fields
	 * and passes their values to {@link #getHiddenEntities(IData)}.
	 *
	 * @return the set of input-field identifiers; empty if no dependency model is present.
	 */
	Set<IIdentifier> getInputFields();

	/**
	 * Computes the hidden entities for the given data set, as one {@link IHiddenEntityGroup} per firing
	 * trigger instance. The kernel does not suppress hiding for already-given dependents - the caller
	 * decides per group (see {@link IHiddenEntityGroup}).
	 *
	 * @param inputFieldValues
	 * 		the values of the fields returned by {@link #getInputFields()} (and any further data the
	 * 		caller supplies); must contain the complete data relevant for the computation, because hidden
	 * 		entities are computed in a single pass over this data.
	 * @return the hidden entities grouped by trigger firing; empty if no dependency model is present.
	 */
	List<IHiddenEntityGroup> getHiddenEntities(IData inputFieldValues);

	/**
	 * Wraps the given validation result so that messages caused solely by hidden entities are
	 * post-processed: causing entities that are hidden and unset are removed from the cause, and, if
	 * a cause changed, the rule's alternative error message is used instead. A message is never
	 * dropped.
	 *
	 * @param original
	 * 		the validation result to post-process into.
	 * @param hiddenEntities
	 * 		the hidden entities as computed by {@link #getHiddenEntities(IData)}.
	 * @return a result that applies the dependency post-processing while delegating to
	 *        {@code original}; the {@code original} result itself if no dependency model is present.
	 */
	IResult wrapForDependencyPostprocessing(IResult original, Set<IIdentifier> hiddenEntities);
}
