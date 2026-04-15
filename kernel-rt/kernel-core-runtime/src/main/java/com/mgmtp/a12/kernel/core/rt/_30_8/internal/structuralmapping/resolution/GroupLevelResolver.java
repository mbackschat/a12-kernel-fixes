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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.resolution;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document.FieldInstance;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document.GroupInstance;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.StructuralMappingState;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Represents a single resolution strategy in execution. Responsible for determining the group instance of a single ancestor level
 * of the target field, going by the source field instance and the ancestor's parent group instance.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GroupLevelResolver {
	protected final Entity targetGroup;

	/**
	 * Determine a direct child of {@code targetParentGroupInstance} without modifying the document.
	 *
	 * @param targetParentGroupInstance
	 * 		the target group instance within which to find a direct child. Must not be {@code null}.
	 * @param sourceFieldInstance
	 * 		the source group instance that may augment how the child is chosen. Must not be {@code null}.
	 * @param structuralMappingState
	 * 		the execution state for the structural mapping. Must not be {@code null}.
	 * @return a child of {@code targetParentGroupInstance} or {@code null} if no appropriate child could be found (either because
	 * 		the document would have to be modified, or because it's not possible).
	 */
	public abstract GroupInstance resolve(GroupInstance targetParentGroupInstance, FieldInstance sourceFieldInstance,
			StructuralMappingState structuralMappingState);

	/**
	 * Determine a direct child of {@code targetParentGroupInstance}, modifying the document if necessary.
	 *
	 * @param targetParentGroupInstance
	 * 		the target group instance within which to find a direct child. Must not be {@code null}.
	 * @param sourceFieldInstance
	 * 		the source group instance that may augment how the child is chosen. Must not be {@code null}.
	 * @param structuralMappingState
	 * 		the execution state for the structural mapping. Must not be {@code null}.
	 * @return a child of {@code targetParentGroupInstance} or {@code null} if reification is not possible (see
	 *        {@link #canReifyFor(FieldInstance)}).
	 */
	public abstract GroupInstance reify(GroupInstance targetParentGroupInstance, FieldInstance sourceFieldInstance,
			StructuralMappingState structuralMappingState);

	/**
	 * @param sourceFieldInstance
	 * 		the source field instance to test reification for. Must not be {@code null}.
	 * @return {@code true} if reification would succeed for the given {@code sourceFieldInstance} (i.e. {@link #reify} does not
	 * 		return {@code null}).
	 */
	public boolean canReifyFor(FieldInstance sourceFieldInstance) {
		return true;
	}

	/**
	 * @return {@code true} if reification is always guaranteed to succeed (i.e. {@link #reify} never returns {@code null}),
	 * 		otherwise {@code false}.
	 */
	public boolean canAlwaysBeReified() {
		return true;
	}
}
