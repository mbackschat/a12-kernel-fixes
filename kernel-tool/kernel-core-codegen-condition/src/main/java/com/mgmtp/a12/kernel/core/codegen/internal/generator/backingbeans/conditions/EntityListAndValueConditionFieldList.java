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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdList;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.RuntimeMethodNameFactory;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListAndValueListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FeldIdUtilities;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FindAnyAst;

/**
 * Parameter class for an entityListAndValue condition if the value list is
 * a fieldList
 */
public class EntityListAndValueConditionFieldList extends EntityListAndValueListCondition
		implements IEntityListAndValueListCondition {
	private final EntityIdList valueList;
	private boolean optimize = false;

	private static final int OPTIMIZATION_BOUND_FOR_VALUE_LIST_LENGTH = 20;
	private static final int OPTIMIZATION_BOUND_FOR_ITERATION = 10;

	public EntityListAndValueConditionFieldList(IEntityListAndValueListCondition b, EntityIdList entityList,
			EntityIdList valueList) {
		super(b, entityList);
		this.valueList = valueList;
	}

	@Override
	public IEntityIdList getValueListAsEntityList() {
		return valueList;
	}

	public String getListCode() {
		return valueList.toCode();
	}

	/**
	 * In case of long value lists (usually built by wildcards for levels with high repeatability)
	 * without iteration, at runtime we build the set of values only once and not for each
	 * iteration.
	 */
	public boolean shouldBeOptimized() {
		switch (getConditionName()) {
			case AtLeastOneDateRangeOverlaps:
				return false;
			case NoFieldValueIncludedInValueList:
			case AtLeastOneFieldValueIncludedInValueList:
			case NotAllFieldValuesIncludedInValueList:
				break;
			default:
				throw new IllegalStateException("Unknown EntityListAndValueCondition " + getConditionName());
		}
		if (!FindAnyAst.findeInParseBaumEntity(entityList,
				p -> p instanceof EntityId && FeldIdUtilities.getNumberOfIterations(
						(EntityId) p) >= OPTIMIZATION_BOUND_FOR_ITERATION, false).isPresent()) {
			return false;
		}
		//lists without filtercondition, without iteration and with WILDCARD with at least 50 repetitions will be optimized
		if (FindAnyAst.findeInParseBaumEntity(valueList, p -> p instanceof EntityId && ((EntityId) p).getFilterCondition() != null,
				false).isPresent()) {
			return false;
		}
		if (FindAnyAst.findeInParseBaumEntity(valueList,
				p -> p instanceof EntityId && FeldIdUtilities.isIteration((EntityId) p), false).isPresent()) {
			return false;
		}
		if (FindAnyAst.findeInParseBaumEntity(valueList, p -> p instanceof EntityId && FeldIdUtilities.getLengthAsList(
				(EntityId) p) >= OPTIMIZATION_BOUND_FOR_VALUE_LIST_LENGTH, false).isPresent()) {
			return true;
		}

		return false;
	}

	public boolean isOptimize() {
		return optimize;
	}

	public void setIsOptimize() {
		this.optimize = true;
	}

	public boolean isAtLeastOneValueConstruct() {
		return getConditionName() == EntityListAndValueListConditionName.AtLeastOneFieldValueIncludedInValueList;
	}

	@Override
	public String getBedingungNameCode() {
		String suffix = isOptimize() ? "OL" : "FL";
		return RuntimeMethodNameFactory.getRuntimeMethodNameFromParseTreeObject(this, suffix);
	}

}