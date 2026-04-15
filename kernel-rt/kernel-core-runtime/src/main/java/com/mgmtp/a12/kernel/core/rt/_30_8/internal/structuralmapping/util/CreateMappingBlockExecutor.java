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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.util;

import java.util.HashMap;
import java.util.Map;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.FieldMapper;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.MappingBlockExecutionNode;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.MappingBlockExecutor;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.model.FieldMapping;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.model.Fold;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.model.MappingBlock;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.model.ResolutionStrategy;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.model.Slice;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.resolution.FixedGroupLevelResolver;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.resolution.FoldingGroupLevelResolver;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.resolution.GroupLevelResolver;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.resolution.SlicingGroupLevelResolver;

public class CreateMappingBlockExecutor {
	private final Map<ResolutionStrategy, GroupLevelResolver> resolutionStrategyToGroupLevelResolver = new HashMap<>();

	public static MappingBlockExecutor run(MappingBlock mappingBlock) {
		return new CreateMappingBlockExecutor().create(mappingBlock);
	}

	private MappingBlockExecutor create(MappingBlock mappingBlock) {
		var rootExecutionNodeBuilder = MappingBlockExecutionNode.builder();
		for (FieldMapping fieldMapping : mappingBlock.fieldMappings()) {
			MappingBlockExecutionNode.Builder builder = rootExecutionNodeBuilder;
			for (ResolutionStrategy resolutionStrategy : fieldMapping.resolutionStrategiesByTargetLevel()) {
				// Note: resolutionStrategy may be null here, but childBuilder() handles this case
				builder = builder.childBuilder(resolutionStrategy);
			}
			FieldMapper fieldMapper = createFieldMapper(fieldMapping, builder.group());
			builder.fieldMapper(fieldMapper);
		}
		return new MappingBlockExecutor(rootExecutionNodeBuilder.build());
	}

	private FieldMapper createFieldMapper(FieldMapping fieldMapping, Entity lowestSourceGroup) {
		var groupLevelResolvers = fieldMapping.targetField().ancestors().stream()
				.map(targetLayer -> {
					var rs = fieldMapping.resolutionStrategiesByTargetLevel().get(targetLayer.levelIndex());
					return rs != null
							? getOrCreateGroupLevelResolver(rs)
							: FixedGroupLevelResolver.create(targetLayer);
				})
				.toArray(GroupLevelResolver[]::new);
		return FieldMapper.create(
				fieldMapping.sourceField(),
				fieldMapping.targetField(),
				lowestSourceGroup,
				groupLevelResolvers,
				fieldMapping.groupToClear());
	}

	private GroupLevelResolver getOrCreateGroupLevelResolver(ResolutionStrategy resolutionStrategy) {
		return resolutionStrategyToGroupLevelResolver.computeIfAbsent(resolutionStrategy, this::createGroupLevelResolver);
	}

	private GroupLevelResolver createGroupLevelResolver(ResolutionStrategy resolutionStrategy) {
		if (resolutionStrategy instanceof Fold fold) {
			return FoldingGroupLevelResolver.create(
					fold.sourceGroup(),
					fold.targetGroup());
		} else if (resolutionStrategy instanceof Slice slice) {
			return SlicingGroupLevelResolver.create(
					slice.sourceGroup(),
					slice.targetGroup(),
					slice.sourceField(),
					slice.targetField());
		} else {
			throw new IllegalStateException();
		}
	}
}
