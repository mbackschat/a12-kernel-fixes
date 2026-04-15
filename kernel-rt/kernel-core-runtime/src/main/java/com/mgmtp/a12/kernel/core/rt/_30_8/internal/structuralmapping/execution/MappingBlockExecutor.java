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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document.Document;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document.GroupInstance;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.model.MappingBlock;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.util.CreateMappingBlockExecutor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MappingBlockExecutor {
	private final MappingBlockExecutionNode rootExecutionNode;

	private record ExecutionStackEntry(
			GroupInstance sourceGroupInstance,
			MappingBlockExecutionNode executionNode
	) {
	}

	public static MappingBlockExecutor fromMappingBlock(MappingBlock mappingBlock) {
		return CreateMappingBlockExecutor.run(mappingBlock);
	}

	public void execute(Document sourceDocument, Document targetDocument, StructuralMappingState structuralMappingState) {
		Deque<ExecutionStackEntry> executionStack = new ArrayDeque<>();
		executionStack.add(new ExecutionStackEntry(sourceDocument, rootExecutionNode));
		while (!executionStack.isEmpty()) {
			ExecutionStackEntry executionStackEntry = executionStack.removeLast();
			MappingBlockExecutionNode executionNode = executionStackEntry.executionNode;
			GroupInstance sourceGroupInstance = executionStackEntry.sourceGroupInstance;

			// execute the field mappings
			for (FieldMapper fieldMapper : executionNode.fieldMappers()) {
				fieldMapper.execute(sourceGroupInstance, targetDocument, structuralMappingState);
			}

			// add the children to the stack (reverse order since the stack is LIFO)
			for (int childIndex = executionNode.children().size() - 1; childIndex >= 0; childIndex--) {
				MappingBlockExecutionNode child = executionNode.children().get(childIndex);
				List<GroupInstance> childGroupInstances = new ArrayList<>();
				child.groupReference().resolvePresentInstances(sourceGroupInstance).forEachRemaining(childGroupInstances::add);

				// add child instances to the stack (reverse order since the stack is LIFO)
				for (int i = childGroupInstances.size() - 1; i >= 0; i--) {
					executionStack.addLast(new ExecutionStackEntry(childGroupInstances.get(i), child));
				}
			}
		}
	}
}
