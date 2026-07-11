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

package com.mgmtp.a12.kernel.core.calc.internal.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IGroupOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ISetOpOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.GroupOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;

/**
 * Collects the directly referenced entities in a calculation that are operands / potential dependencies of the calculation.
 * Note: the collected operands are currently not only used for the dependencies, but also in DatenFilterImpl during evaluation
 * of calculations (probably to avoid formal-validation errors from unused fields); therefore we cannot omit operands here even
 * if we know that they are dependency-irrelevant for some reason.
 */
public class CalculationOperandsCollector extends ASTVisitor {
	private final String calculatedFieldFullName;
	private final Set<String> fieldReferences = new HashSet<>();
	private final Set<String> normalGroupReferences = new HashSet<>();
	private final Set<String> conditionalGroupReferences = new HashSet<>();
	private final Set<String> indexGroupReferences = new HashSet<>();

	private CalculationOperandsCollector(String calculatedFieldFullName) {
		this.calculatedFieldFullName = calculatedFieldFullName;
	}

	/**
	 * @return the fields (full names) directly referenced in the calculation
	 */
	public Set<String> getFieldReferences() {
		return fieldReferences;
	}

	/**
	 * @return the groups (full names) directly referenced in the calculation "normally" (i.e. not as specified by
	 *        {@link #getConditionalGroupReferences()}).
	 */
	public Set<String> getNormalGroupReferences() {
		return normalGroupReferences;
	}

	/**
	 * @return the ancestor groups (full names) of {@code fieldName} directly referenced in the calculation via a single-argument
	 *        {@code GroupFilled}.
	 */
	public Set<String> getConditionalGroupReferences() {
		return conditionalGroupReferences;
	}

	/**
	 * @return the index groups (full name) referenced in the calculation via parallel iteration or semantic index.
	 */
	public Set<String> getIndexGroupReferences() {
		return indexGroupReferences;
	}

	/**
	 * Collects the operands of the given {@link ICalculationInfo}.
	 *
	 * @param calculationInfo
	 * 		the calculation to collect the operands for
	 * @return a {@link CalculationOperandsCollector} which contains the entities directly referenced in the given calculation,
	 * 		categorized by type
	 */
	public static CalculationOperandsCollector collectOperands(ICalculationInfo calculationInfo) {
		String calculatedFieldFullName = calculationInfo.getCalculatedField().getFullName();
		CalculationOperandsCollector calculationOperandsCollector = new CalculationOperandsCollector(calculatedFieldFullName);
		for (IParseTreeEntity ast : CalculationUtils.getAllPartsOfCalculationInfos(List.of(calculationInfo))) {
			ast.traverse(calculationOperandsCollector);
		}
		return calculationOperandsCollector;
	}

	private void traverseEntityId(IEntityId id) {
		// we can't visit conditional group ids normally, so we need to traverse manually
		if (id == null) {
			return;
		}
		indexGroupReferences.addAll(AnalyseService.getParallelIterationGroupsOfId(id));
		if (id.getFilterCondition() != null) {
			id.getFilterCondition().traverse(this);
		}
		if (id.getSemanticIndex() != null && !id.getSemanticIndex().isEmpty()) {
			int semanticIndexLevel = ArrayUtils.indexOf(id.getIndices(), IParseTreeEntity.SEMANTIC_INDEX);
			indexGroupReferences.add(EntityUtils.getPathUpToLevel(semanticIndexLevel, id.getFullName()));

			for (Object semIdx : id.getSemanticIndex()) {
				if (semIdx instanceof IEntityId semIdxId) {
					semIdxId.traverse(this);
				}
			}
		}
	}

	@Override
	public boolean visit(IEntityId id) {
		String fullName = id.getFullName();
		if (id.isField()) {
			if (!fullName.equals(calculatedFieldFullName)) {
				fieldReferences.add(fullName);
			}
		} else {
			normalGroupReferences.add(fullName);
		}

		traverseEntityId(id);
		return false;
	}

	@Override
	public boolean visit(IEntityListCondition elc) {
		if (elc.getConditionName() != EntityListConditionName.AllGroupsFilled
				|| elc.getEntityList().getEntityIdList().size() != 1) {
			return true;
		}

		IEntityId groupId = elc.getEntityList().getEntityIdList().get(0);
		if (!EntityUtils.isDescendantOrEqual(calculatedFieldFullName, groupId.getFullName())) {
			return true;
		}

		conditionalGroupReferences.add(groupId.getFullName());
		traverseEntityId(groupId);
		return false;
	}

	@Override
	public boolean visit(IRootGroupCondition rgc) {
		String rootGroupPath = EntityUtils.createRootGroupPath(rgc.getRootGroupName());
		if (rgc.getConditionName() == RootGroupConditionName.MindestensEinVordruckAngegeben
				&& EntityUtils.getRootGroupName(calculatedFieldFullName).equals(rgc.getRootGroupName())) {
			conditionalGroupReferences.add(rootGroupPath);
		} else {
			normalGroupReferences.add(rootGroupPath);
		}

		return false;
	}

	@Override
	public boolean visit(ISetOpOperation setOpOperation) {
		// We must traverse the iterationEntity fully (not just its filter condition), even though
		// semantically, the ISetOpOperation (aka SumOfTerms) only evaluates
		// GroupFilled(iterEntity), because:
		// The way that the direct vk api use in extmp prod currently works, the invariant that
		// no repetitions of repeatable groups can disappear does not currently hold
		// (the last filled field of a repetition can (probably) be emptied out by a
		// computation which makes the whole repetition in effect disappear, because the
		// group repetition is not explicitly represented in IData
		// If we consider reintroducing the dependency-elimination for iterationGroup-Only
		// references, also take the note re DatenFilterImpl at the top of this class into
		// account
		setOpOperation.getIterationEntity().traverse(this);
		setOpOperation.getOperation().traverse(this);
		return false;
	}

	@Override
	public void endVisit(INumberOfRootGroupsOperation norgo) {
		String rootGroupPath = EntityUtils.createRootGroupPath(norgo.getRootGroupName());
		normalGroupReferences.add(rootGroupPath);
	}

	@Override
	public boolean visit(IGroupOperation gop) {
		assert gop.getOperatorName() == GroupOperationName.CurrentRepetition : gop.getOperatorName();
		// We don't handle this via visit(IEntityId), because we don't want the direct reference for the group itself.
		// We only want the indirect references for semantic index or parallel iteration.
		traverseEntityId(gop.getGroup());
		return false;
	}
}
