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

package com.mgmtp.a12.kernel.core.parser.internal.check.bedingung;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DateValidityConditionContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumKonstruktContext;

import jakarta.validation.constraints.NotNull;

/**
 * Checker class for
 * {@link FullChecks_ConditionParser#checkDateValidityCondition}
 */
public class CheckDateValidityConditionImpl extends CheckImplAbstractClass {

	public CheckDateValidityConditionImpl(FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull DateValidityConditionContext ctx) {
		DatumKonstruktContext dateCtx = ctx.date;
		if (containsOnlyConstants(dateCtx)) {
			String opName = ctx.validityConditionName().getText();
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_ONLY_CONSTANTS_USED, opName);
			throw createMVKParserError(dateCtx, errMessage);
		}
	}

	private boolean containsOnlyConstants(DatumKonstruktContext dateCtx) {
		return dateCtx.dp1.entitySpec() == null && dateCtx.dp2.entitySpec() == null &&
				(dateCtx.dp3 == null || dateCtx.dp3.entitySpec() == null) &&
				(dateCtx.dp4 == null || dateCtx.dp4.entitySpec() == null);
	}
}
