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

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;

/**
 * Determine the group that contains the calculated field for which a subgroup or itself is
 * referenced in all Or-Paths of the calculation rule in a GroupFilled construct.
 * We call this conditional calculation group.
 */
class ConditionalCalculationGroupVisitor extends ASTVisitor {

	public static String getConditionalCalculationGroup(ICondition condition, String calculatedFieldName) {
		ConditionalCalculationGroupVisitor visitor = new ConditionalCalculationGroupVisitor(calculatedFieldName);
		condition.traverse(visitor);
		return visitor.conditionalCalculationGroup;
	}

	private String conditionalCalculationGroup = "";
	private final String calculatedFieldName;

	private ConditionalCalculationGroupVisitor(String calculatedFieldName) {
		this.calculatedFieldName = calculatedFieldName;
	}

	@Override
	public boolean visit(ICompositeCondition cc) {
		if (cc.getLogOp() == LogOp.Or) {
			conditionalCalculationGroup = cc.getConditionList().stream()
					.map(c -> getConditionalCalculationGroup(c, calculatedFieldName))
					.reduce((g1, g2) -> g1.length() < g2.length() ? g1 : g2)
					.orElse("");
		} else {
			conditionalCalculationGroup = cc.getConditionList().stream()
					.map(c -> getConditionalCalculationGroup(c, calculatedFieldName))
					.reduce((g1, g2) -> g1.length() > g2.length() ? g1 : g2)
					.orElse("");
		}
		return false;
	}

	@Override
	public boolean visit(IEntityListCondition entityListCondition) {
		if (entityListCondition.getConditionName() != EntityListConditionName.AllGroupsFilled
				|| entityListCondition.getEntityList().getEntityIdList().size() != 1) {
			return false;
		}
		String groupName = entityListCondition.getEntityList().getEntityIdList().get(0).getFullName();
		if (!EntityUtils.isDescendantOrEqual(calculatedFieldName, groupName)) {
			return false;
		}
		// This cannot be set in an other subtree, because ICompositeCondition cuts all subtrees
		this.conditionalCalculationGroup = groupName;
		return false;
	}

	@Override
	public boolean visit(IRootGroupCondition rgc) {
		if (rgc.getConditionName() != RootGroupConditionName.MindestensEinVordruckAngegeben) {
			return false;
		}
		String rootGroupName = EntityUtils.getRootGroupName(calculatedFieldName);
		if (!rootGroupName.equals(rgc.getRootGroupName())) {
			return false;
		}
		this.conditionalCalculationGroup = EntityUtils.createRootGroupPath(rootGroupName);
		return false;
	}
}
