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

package com.mgmtp.a12.kernel.core.parser.internal.check.entity;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.RelUpContext;

public class CheckRelUpImpl extends CheckImplAbstractClass {
	public static final int MIN_LEVEL_FOR_UPWARD_PATHS = 0; // we don't allow to climb above root group (-> rel path to other root
	// group is not possible)

	public CheckRelUpImpl(FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(RelUpContext ctx) {
		String ruleName = fullParser.getRule().getFullName();
		// rule on rootGroup => ruleGroupLevel = 0
		int ruleGroupLevel = EntityUtils.getLevel(ruleName) - 1;
		int totalNumUpInRelativeUp = ctx.oneUp().size() + 1; // one '..' is hardcoded in relativeUp outside of relUp
		if (ruleGroupLevel - totalNumUpInRelativeUp < MIN_LEVEL_FOR_UPWARD_PATHS) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_TOO_MANY_PARENT_GROUPS);
			throw createMVKParserError(ctx.getStart(), errMessage);
		}
	}

}
