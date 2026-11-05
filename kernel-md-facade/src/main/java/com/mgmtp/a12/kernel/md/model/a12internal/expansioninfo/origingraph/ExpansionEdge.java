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

package com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Pairs an {@link ExpansionEdgeLabel} (describing <em>how</em> an expansion step was applied)
 * with the {@link ExpansionNode} of the element in the source model (describing <em>what</em>
 * the element was in that source model). The source model ID is carried by the
 * {@link ExpansionNode} itself via {@link ExpansionNode#modelId()}.
 *
 * <p>A chain of {@link ExpansionEdge} values reachable via
 * {@link ExpansionNode#previousOrigins()} forms the full provenance path of an element from its
 * ultimate definition site up to the current composed model.</p>
 */
@Value(staticConstructor = "of") @Accessors(fluent = true)
public class ExpansionEdge {
	/**
	 * The expansion connection that describes how the element was brought into the
	 * current model (e.g. include, decoration, additive, combination); never {@code null}.
	 */
	@NonNull ExpansionEdgeLabel label;

	/**
	 * The {@link ExpansionNode} of the element as it existed in the source model; never {@code null}.
	 */
	@NonNull ExpansionNode previousNode;
}
