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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.EntityListAndValueConditionFieldList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListAndValueListConditionName;

/**
 * Helper class to create initialization code for parameters for long value lists that can be
 * optimized.
 * See also {@link EntityListAndValueConditionFieldList#shouldBeOptimized()} and
 * {@link InitEntityListAndValueListConditionParam}
 * and the corresponding string template file.
 */
public class EntityListAndValueListCreator {

	private EntityListAndValueListCreator() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * The parse tree is modified to contain the variable name of the entity list.
	 *
	 * @return information to create the initialization code for parameters for long value lists
	 * 		that will be optimized
	 */
	public static List<InitEntityListAndValueListConditionParam> postprocessParseTree(IParseTreeEntity part) {
		List<InitEntityListAndValueListConditionParam> initCodes = new ArrayList<>();

		AtomicInteger paramNr = new AtomicInteger(0);
		class Visitor extends com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor {

			@Override
			public boolean visit(IEntityListAndValueListCondition ic) {
				if (ic instanceof EntityListAndValueConditionFieldList) {
					EntityListAndValueConditionFieldList cc = (EntityListAndValueConditionFieldList) ic;
					if (cc.shouldBeOptimized()) {
						int i = paramNr.getAndIncrement();
						cc.setParamNr(i);
						cc.setIsOptimize();
						boolean isAtLeastOneValueConstruct = ic.getConditionName() == EntityListAndValueListConditionName.AtLeastOneFieldValueIncludedInValueList;
						initCodes.add(new InitEntityListAndValueListConditionParam(i, isAtLeastOneValueConstruct, cc));
					}
				}
				return false;
			}
		}
		part.traverse(new Visitor());
		return initCodes;
	}
}
