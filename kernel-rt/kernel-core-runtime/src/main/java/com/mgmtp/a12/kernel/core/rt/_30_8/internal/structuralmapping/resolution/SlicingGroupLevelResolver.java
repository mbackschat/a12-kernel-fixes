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

import java.util.Objects;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document.EntityInstance;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document.FieldInstance;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document.GroupInstance;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.FieldMappingOrigin;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.StructuralMappingState;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.reference.FieldReference;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.reference.GroupReference;

/**
 * A slice resolution strategy in execution.
 */
public class SlicingGroupLevelResolver extends GroupLevelResolver {
	private final GroupReference sourceGroupReference;
	private final FieldReference sliceSourceFieldReference;
	private final FieldReference sliceTargetFieldReference;

	private SlicingGroupLevelResolver(Entity targetGroup, GroupReference sourceGroupReference,
			FieldReference sliceSourceFieldReference, FieldReference sliceTargetFieldReference) {
		super(targetGroup);
		this.sourceGroupReference = sourceGroupReference;
		this.sliceSourceFieldReference = sliceSourceFieldReference;
		this.sliceTargetFieldReference = sliceTargetFieldReference;
	}

	public static SlicingGroupLevelResolver create(Entity sourceGroup, Entity targetGroup, Entity sliceSourceField,
			Entity sliceTargetField) {
		return new SlicingGroupLevelResolver(
				targetGroup,
				GroupReference.upward(sourceGroup),
				FieldReference.between(sourceGroup, sliceSourceField),
				FieldReference.between(targetGroup, sliceTargetField)
		);
	}

	@Override
	public GroupInstance resolve(GroupInstance targetParentGroupInstance, FieldInstance sourceFieldInstance,
			StructuralMappingState structuralMappingState) {
		FieldInstance sliceSourceFieldInstance = resolveSliceSourceFieldInstance(sourceFieldInstance);
		if (sliceSourceFieldInstance.isFilled()) {
			return resolveTargetGroupInstance(targetParentGroupInstance, sliceSourceFieldInstance.value());
		}
		return null;
	}

	public record SliceOrigin(
			FieldMappingOrigin fieldMappingOrigin,
			FieldInstance sliceSourceFieldInstance
	) implements EntityInstance.Origin {
	}

	@Override
	public GroupInstance reify(GroupInstance targetParentGroupInstance, FieldInstance sourceFieldInstance,
			StructuralMappingState structuralMappingState) {
		FieldInstance sliceSourceFieldInstance = resolveSliceSourceFieldInstance(sourceFieldInstance);
		if (sliceSourceFieldInstance.isFilled()) {
			Object sliceValue = sliceSourceFieldInstance.value();
			GroupInstance targetGroupInstance = resolveTargetGroupInstance(targetParentGroupInstance, sliceValue);
			if (targetGroupInstance == null) {
				SliceOrigin origin = new SliceOrigin(new FieldMappingOrigin(sourceFieldInstance), sliceSourceFieldInstance);
				targetGroupInstance = targetParentGroupInstance.createChildGroupInstance(targetGroup, origin);
				FieldInstance sliceTargetFieldInstance = sliceTargetFieldReference.resolveInstance(targetGroupInstance)
						.createIfAbsent(origin);
				sliceTargetFieldInstance.setValue(sliceValue, origin);
				structuralMappingState.fieldInstanceWasMapped(sliceSourceFieldInstance, sliceTargetFieldInstance);
			}
			return targetGroupInstance;
		}
		return null;
	}

	private FieldInstance resolveSliceSourceFieldInstance(FieldInstance sourceFieldInstance) {
		return sliceSourceFieldReference.resolveInstance(sourceGroupReference.resolveInstance(sourceFieldInstance));
	}

	private GroupInstance resolveTargetGroupInstance(GroupInstance targetParentGroupInstance, Object sliceValue) {
		// We might want to introduce a cache here if slicing ever becomes a performance bottleneck. For now, even with decently
		// sized input it does not noticeably impact performance and keeps the code much simpler.
		for (GroupInstance targetGroupInstance : targetParentGroupInstance.getChildGroupInstancesInOrder(targetGroup)) {
			FieldInstance sliceTargetFieldInstance = sliceTargetFieldReference.resolveInstance(targetGroupInstance);
			if (Objects.equals(sliceValue, sliceTargetFieldInstance.value())) {
				return targetGroupInstance;
			}
		}
		return null;
	}

	@Override
	public boolean canReifyFor(FieldInstance sourceFieldInstance) {
		return resolveSliceSourceFieldInstance(sourceFieldInstance).isFilled();
	}

	@Override
	public boolean canAlwaysBeReified() {
		return false;
	}

	@Override
	public String toString() {
		return targetGroup.shortName() + "[slice]";
	}
}
