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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICodeOutput;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ISetOpOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;

/**
 * Collect the code to generate the parameters necessary to call the RuntimeController methods, e.g.
 * the implementations of the predicates.
 */
@StringTemplateGroup("entityid")
public class EntityIdsCollector extends ASTVisitor {

	private static final String ENTITYID_PREFIX = "rtIdCon";
	private static final String ENTITYIDLIST_PREFIX = "allRtIdCon";
	private static final String GROUPEDENTITYIDLIST_PREFIX = "grpAllRtIdCon";

	private final AtomicInteger counterEntityIds = new AtomicInteger(0);
	private final List<EntityId> entityIds = new ArrayList<>();

	private final AtomicInteger counterEntityIdLists = new AtomicInteger(0);
	private final List<EntityIdList> entityIdLists = new ArrayList<>();

	private final AtomicInteger counterGroupedEntityIdLists = new AtomicInteger(0);
	private final List<GroupedEntityIdList> groupedEntityIdLists = new ArrayList<>();

	private final String globalPrefix;

	public EntityIdsCollector(String globalPrefix) {
		this.globalPrefix = globalPrefix;
	}

	private void addIfNecessaryAndSetVarName(GroupedEntityIdList groupedEntityIdList) {
		int idx = groupedEntityIdLists.indexOf(groupedEntityIdList);
		if (idx == -1) {
			String varName = globalPrefix + GROUPEDENTITYIDLIST_PREFIX + counterGroupedEntityIdLists.incrementAndGet();
			groupedEntityIdList.setVarName(varName);
			groupedEntityIdLists.add(groupedEntityIdList);
		} else {
			groupedEntityIdList.setVarName(groupedEntityIdLists.get(idx).getVariableName());
		}
	}

	private void addIfNecessaryAndSetVarName(EntityIdList entityIdList) {
		int idx = entityIdLists.indexOf(entityIdList);
		if (idx == -1) {
			String varName = globalPrefix + ENTITYIDLIST_PREFIX + counterEntityIdLists.incrementAndGet();
			entityIdList.setVarName(varName);
			entityIdLists.add(entityIdList);
		} else {
			entityIdList.setVarName(entityIdLists.get(idx).getVariableName());
		}
	}

	private void addIfNecessaryAndSetVarName(EntityId entityId) {
		int idx = entityIds.indexOf(entityId);
		if (idx == -1) {
			String varName = globalPrefix + ENTITYID_PREFIX + counterEntityIds.incrementAndGet();
			entityId.setVarName(varName);
			entityIds.add(entityId);
		} else {
			entityId.setVarName(entityIds.get(idx).getVariableName());
		}
	}

	public List<ICodeOutput> getAll() {
		List<ICodeOutput> retVal = new ArrayList<>();
		retVal.addAll(entityIds);
		retVal.addAll(entityIdLists);
		retVal.addAll(groupedEntityIdLists);
		return retVal;
	}

	// IEntityId.traverse already ensures that if a semIdx exists and references an entiyId, that entity id
	// is traversed *before* endVisit on the original EntityId

	@Override
	public void endVisit(IEntityId entityId) {
		addIfNecessaryAndSetVarName((EntityId) entityId);
	}

	@Override
	public boolean visit(IEntityIdList entityIdList) {
		if (entityIdList instanceof GroupedEntityIdList) {
			addIfNecessaryAndSetVarName((GroupedEntityIdList) entityIdList);
		} else {
			addIfNecessaryAndSetVarName((EntityIdList) entityIdList);
		}
		return true;
	}

	@Override
	public boolean visitNext(int i, ISetOpOperation setOp) {
		// We want the iteration entity in the Set, but the inner set operation is handled separately
		// See e.g. CodeGenRuleDefinitionBuilder#getSetOpEntityIdsCollector
		return false;
	}

	public List<EntityId> getEntityIds() {
		return Collections.unmodifiableList(entityIds);
	}
}
