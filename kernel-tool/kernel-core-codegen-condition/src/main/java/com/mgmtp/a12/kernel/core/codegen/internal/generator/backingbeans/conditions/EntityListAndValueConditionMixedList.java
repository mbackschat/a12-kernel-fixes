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

import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdList;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICodeOutput;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.RuntimeMethodNameFactory;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;

/**
 * Parameter class for an entityListAndValueList condition if the value list may be
 * a mixture of entities and strings.
 */
public class EntityListAndValueConditionMixedList extends EntityListAndValueListCondition
		implements IEntityListAndValueListCondition {
	private final List<Object> valueList;

	public EntityListAndValueConditionMixedList(IEntityListAndValueListCondition b, EntityIdList entityList,
			List<Object> mixedList) {
		super(b, entityList);
		valueList = mixedList;
	}

	@Override
	public List<Object> getValueListAsMixedList() {
		return valueList;
	}

	public List<String> getValueListCode() {
		return valueList.stream()
				.map(this::getCodeForItem)
				.collect(Collectors.toList());
	}

	private String getCodeForItem(Object item) {
		if (item instanceof String) {
			return new StringParameter((String) item).toCode();
		} else if (item instanceof ICodeOutput) {
			return ((ICodeOutput) item).toCode();
		} else {
			throw new IllegalArgumentException("Undefined object in value list: " + item);
		}
	}

	@Override
	public String getBedingungNameCode() {
		return RuntimeMethodNameFactory.getRuntimeMethodNameFromParseTreeObject(this, "ML");
	}

}
