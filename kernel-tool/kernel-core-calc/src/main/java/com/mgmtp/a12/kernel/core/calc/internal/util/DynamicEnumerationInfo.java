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

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IDynamicEnumerationInfo;

public class DynamicEnumerationInfo implements IDynamicEnumerationInfo {
	private final IEntityId dynamicEnumField;
	private final ICondition preCondition;
	private final List<Pair<ICondition, IOperation>> valueOperations;
	private final String name;

	public DynamicEnumerationInfo(IEntityId dynamicEnumField, ICondition preCondition,
			List<Pair<ICondition, IOperation>> valueOperations, String name) {
		this.dynamicEnumField = dynamicEnumField;
		this.preCondition = preCondition;
		this.valueOperations = valueOperations;
		this.name = name;
	}

	@Override
	public IEntityId getDynamicEnumerationField() {
		return dynamicEnumField;
	}

	@Override
	public ICondition getSafeGuard() {
		return preCondition;
	}

	@Override
	public List<Pair<ICondition, IOperation>> getValueOperations() {
		return valueOperations;
	}

	@Override
	public String getName() {
		return name;
	}

}
