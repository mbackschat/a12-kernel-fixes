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
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document.EntityInstance;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document.FieldInstance;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document.GroupInstance;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.FieldMappingOrigin;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.StructuralMappingState;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.reference.GroupReference;

/**
 * A fold resolution strategy in execution.
 */
public class FoldingGroupLevelResolver extends GroupLevelResolver {
	private final GroupReference sourceGroupReference;

	private FoldingGroupLevelResolver(Entity targetGroup, GroupReference sourceGroupReference) {
		super(targetGroup);
		this.sourceGroupReference = sourceGroupReference;
	}

	public static FoldingGroupLevelResolver create(Entity sourceGroup, Entity targetGroup) {
		return new FoldingGroupLevelResolver(
				targetGroup,
				GroupReference.upward(sourceGroup));
	}

	@Override
	public GroupInstance resolve(GroupInstance targetParentGroupInstance, FieldInstance sourceFieldInstance,
			StructuralMappingState structuralMappingState) {
		GroupInstance sourceGroupInstance = sourceGroupReference.resolveInstance(sourceFieldInstance); // always present
		return structuralMappingState.foldCache()
				.get(new StructuralMappingState.FoldCacheKey(this, targetParentGroupInstance, sourceGroupInstance));
	}

	public record FoldOrigin(
			FieldMappingOrigin fieldMappingOrigin,
			GroupInstance sourceGroupInstance
	) implements EntityInstance.Origin {
	}

	@Override
	public GroupInstance reify(GroupInstance targetParentGroupInstance, FieldInstance sourceFieldInstance,
			StructuralMappingState structuralMappingState) {
		GroupInstance sourceGroupInstance = sourceGroupReference.resolveInstance(sourceFieldInstance); // always present
		return structuralMappingState.foldCache().computeIfAbsent(
				new StructuralMappingState.FoldCacheKey(this, targetParentGroupInstance, sourceGroupInstance),
				key -> targetParentGroupInstance.createChildGroupInstance(targetGroup,
						new FoldOrigin(new FieldMappingOrigin(sourceFieldInstance), sourceGroupInstance)));
	}

	@Override
	public String toString() {
		return targetGroup.shortName() + "[fold]";
	}
}
