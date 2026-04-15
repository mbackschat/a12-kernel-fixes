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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.EntityImpl;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.model.ResolutionStrategy;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.reference.GroupReference;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public record MappingBlockExecutionNode(
		GroupReference groupReference, // reference from the parent execution node's source group to this node's source group
		List<MappingBlockExecutionNode> children,
		List<FieldMapper> fieldMappers
) {
	public static Builder builder() {
		return new Builder(GroupReference.between(EntityImpl.ROOT, EntityImpl.ROOT));
	}

	@RequiredArgsConstructor @Accessors(fluent = true)
	public static class Builder {
		private final GroupReference groupReference;
		private final Map<ResolutionStrategy, Builder> resolutionStrategyToChild = new HashMap<>();
		private final List<FieldMapper> fieldMappers = new ArrayList<>();

		public void fieldMapper(FieldMapper fieldMapper) {
			fieldMappers.add(fieldMapper);
		}

		public Builder childBuilder(ResolutionStrategy resolutionStrategy) {
			if (resolutionStrategy == null) {
				return this;
			}
			return resolutionStrategyToChild.computeIfAbsent(resolutionStrategy,
					key -> new Builder(GroupReference.between(group(),
							group().isAncestorOfOrEqualTo(resolutionStrategy.sourceGroup())
									? resolutionStrategy.sourceGroup()
									: group())));
		}

		public Entity group() {
			return groupReference.referencedEntity();
		}

		public MappingBlockExecutionNode build() {
			assert resolutionStrategyToChild.size() ==
					resolutionStrategyToChild.keySet().stream().map(ResolutionStrategy::rank).distinct().count()
					: "resolution strategy ranks should be unique";
			var childNodes = resolutionStrategyToChild.entrySet().stream()
					.sorted(Comparator.comparing(entry -> entry.getKey().rank()))
					.map(entry -> entry.getValue().build())
					.toList();
			return new MappingBlockExecutionNode(groupReference, childNodes, List.copyOf(fieldMappers));
		}
	}
}
