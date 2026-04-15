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

import java.util.Collections;
import java.util.List;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListAndValueListConditionName;

/**
 * parameter class for the common part of an EntityListAndValueListCondition
 * subclasses handle the different kind of value lists.
 */
public abstract class EntityListAndValueListCondition extends CodeGenCondition implements IEntityListAndValueListCondition {

	protected final EntityIdList entityList;
	private final EntityListAndValueListConditionName conditionName;
	private int paramNr;

	public EntityListAndValueListCondition(IEntityListAndValueListCondition b, EntityIdList entityList) {
		this.entityList = entityList;
		conditionName = b.getConditionName();
	}

	@Override
	public EntityListAndValueListConditionName getConditionName() {
		return conditionName;
	}

	@Override
	public IEntityIdList getEntityList() {
		return entityList;
	}

	@Override
	public IEntityIdList getValueListAsEntityList() {
		return new EntityIdList();
	}

	@Override
	public List<String> getValueListAsStringList() {
		return Collections.emptyList();
	}

	@Override
	public List<Object> getValueListAsMixedList() {
		return Collections.emptyList();
	}

	public abstract String getBedingungNameCode();

	public String getFeldListenCode() {
		return entityList.toCode();
	}

	public void setParamNr(int paramNr) {
		this.paramNr = paramNr;
	}

	public int getParamNr() {
		return paramNr;
	}

}
