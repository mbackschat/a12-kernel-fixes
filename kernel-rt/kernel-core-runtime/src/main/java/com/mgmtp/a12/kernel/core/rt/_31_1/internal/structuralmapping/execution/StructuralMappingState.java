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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.document.FieldInstance;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.document.GroupInstance;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.resolution.FoldingGroupLevelResolver;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class StructuralMappingState {
	@Getter private final Map<FieldInstance, FieldInstance> targetFieldInstanceToSourceFieldInstance = new HashMap<>();
	@Getter private final Map<FieldInstance, List<FieldInstance>> targetFieldInstanceToCollidingSourceFieldInstances = new HashMap<>();
	@Getter private final Map<FoldCacheKey, GroupInstance> foldCache = new HashMap<>();
	@Getter private final Collection<GroupInstance> clearedGroupInstances = new HashSet<>();

	@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY) @ToString
	@RequiredArgsConstructor
	@Accessors(fluent = true) @Getter
	public static class FoldCacheKey {
		@NonNull private final FoldingGroupLevelResolver resolver;
		@NonNull private final GroupInstance targetParentGroupInstance;
		@NonNull private final GroupInstance sourceGroupInstance;
	}

	public void fieldInstanceWasMapped(FieldInstance from, FieldInstance to) {
		assert !targetFieldInstanceToSourceFieldInstance.containsKey(to)
				: "This function should never be called when the target field instance was already mapped into, but " + to + " was already mapped into";
		targetFieldInstanceToSourceFieldInstance.put(to, from);
	}

	public void fieldInstanceWasNotMappedDueToCollision(FieldInstance from, FieldInstance to) {
		assert targetFieldInstanceToSourceFieldInstance.containsKey(to)
				: "This function should never be called when the target field instance was not yet mapped into, but " + to + " was not yet mapped into";
		targetFieldInstanceToCollidingSourceFieldInstances
				.computeIfAbsent(to, key -> new ArrayList<>())
				.add(from);
	}

	public void groupInstanceWasCleared(GroupInstance groupInstance) {
		assert !clearedGroupInstances.contains(groupInstance)
				: "This function should never be called twice for the same group instance, but " + groupInstance + " was already cleared before";
		clearedGroupInstances.add(groupInstance);
	}
}
