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

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplLenientBaseClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;

import jakarta.validation.constraints.NotNull;

public class CheckEntitySpecLenientImpl extends CheckImplLenientBaseClass {

	public CheckEntitySpecLenientImpl(@NotNull Lenient_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull EntitySpecContext ctx) {
		EntityRef ref = getEntityRef(ctx);

		CheckEntityUtils checker = checkCommon(ctx, ref);

		// this is in order to update the condition text (in case an entity was moved or/and renamed) without formating it
		ref.setStartAndStopIdxInCondition(ctx.getStart().getStartIndex(), getEntityTokenStopIndex(ctx));

		check(ctx, ref, checker);

		if (ctx.categoryName == null || StringUtils.isEmpty(ctx.categoryName.getText())) {
			return; //no category defined, no checks needed.
		}
		String categoryName = ctx.categoryName.getText();
		checkCategory(ctx, checker, ref, categoryName);
		ref.setCategoryName(categoryName);
	}

	protected void checkCategory(GrammarOnly_ConditionParser.EntitySpecContext ctx, CheckEntityUtils checker, EntityRef ref,
			String categoryName) {
		// No checks with lenient parser
	}

	protected void check(GrammarOnly_ConditionParser.EntitySpecContext ctx, EntityRef ref, CheckEntityUtils checker) {
		if (ctx.filterCond != null) {
			ref.setHasFilterCondition(true);
		}
		// No checks with lenient parser
	}

	protected CheckEntityUtils checkCommon(GrammarOnly_ConditionParser.EntitySpecContext ctx, EntityRef ref) {
		if (ctx.outer != null) {
			ref.setOuter(true);
		}
		// No checks with lenient parser
		return null;
	}

	private int getEntityTokenStopIndex(GrammarOnly_ConditionParser.EntitySpecContext ctx) {
		if (ctx.entity() != null) {
			// In tokens, the stopIndex is inclusive, therefore +1
			return ctx.entity().getStop().getStopIndex() + 1;
		} else if (ctx.entityHModel() != null) {
			return ctx.entityHModel().getStop().getStopIndex() + 1;
		} else if (ctx.regelKontextEntity() != null) {
			return ctx.regelKontextEntity().getStop().getStopIndex() + 1;
		} else {
			return 0;
		}
	}
}
