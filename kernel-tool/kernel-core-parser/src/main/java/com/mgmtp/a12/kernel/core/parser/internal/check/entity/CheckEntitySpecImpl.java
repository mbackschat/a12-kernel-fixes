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

import java.util.Collections;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityListenUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckSetExpressionUtil;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.SetOperOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.SetOperationContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;

import jakarta.validation.constraints.NotNull;

/**
 *
 */
public class CheckEntitySpecImpl extends CheckEntitySpecLenientImpl {

	FullChecks_ConditionParser fullParser;

	public CheckEntitySpecImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
		this.fullParser = parser;
	}

	@Override
	protected void checkCategory(GrammarOnly_ConditionParser.EntitySpecContext ctx, CheckEntityUtils checker, EntityRef ref,
			String categoryName) {
		super.checkCategory(ctx, checker, ref, categoryName);
		//get the name of the operation,  because the notation is only '[', ']' and we
		//want to have nice error messages.
		String feldWertString = getBareErrorMessage(ParserErrorTypesEnum.MVK_CATEGORY_REF_OP);
		checker.checkIsField(ref, feldWertString, ctx);
		checker.checkIsCorrectCategory(ref, categoryName, ctx);
	}

	@Override
	protected void check(GrammarOnly_ConditionParser.EntitySpecContext ctx, EntityRef ref, CheckEntityUtils checker) {
		super.check(ctx, ref, checker);
		//empty groups are not allowed
		EntityRefUtils.checkNoEmptyGroup(ref.getEntity(), ref.getNameBuiltFromConditionText(), fullParser, ctx);

		if (ctx.filterCond != null) {
			checkFilterCondition(ctx, checker, ref);
		}
	}

	@Override
	protected CheckEntityUtils checkCommon(GrammarOnly_ConditionParser.EntitySpecContext ctx, EntityRef ref) {
		super.checkCommon(ctx, ref);
		CheckEntityUtils checker = new CheckEntityUtils(fullParser);
		checkOuter(ctx);
		if (fullParser.isA12Model() && (ctx.entity() != null || ctx.regelKontextEntity() != null)) {
			if (ctx.outer != null) {
				// for outer, we have to check the error entity with the rule error field
				IEntity errorEntity = fullParser.resetErrorEntity();
				checker.checkIndicesForA12Model(ref, ctx);
				fullParser.setErrorEntity(errorEntity);
			} else {
				checker.checkIndicesForA12Model(ref, ctx);
			}
		}

		return checker;
	}

	protected void checkOuter(GrammarOnly_ConditionParser.EntitySpecContext ctx) {
		if (ctx.outer == null) {
			return;
		}
		if (fullParser.isHModel()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_OPERATION_IN_H_MODEL, "$");
			throw createMVKParserError(ctx, errMessage);
		}
		if (!fullParser.isInSetExpression() || findAncestorContext(ctx, c -> c instanceof SetOperationContext) != null) {
			// In the term of SumOfTerms, outer is also not allowed. But it's primarily a matter of not trusting the user to use it correctly
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_OUTER_ITERATION);
			throw createMVKParserError(ctx, errMessage);
		}
	}

	private void checkFilterCondition(GrammarOnly_ConditionParser.EntitySpecContext ctx, CheckEntityUtils checker, EntityRef ref) {
		ConditionMultiLanguageHandler mlh = new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
				fullParser.getPss());
		String opName = mlh.getTerminalName(Base_ConditionLexer.MIT);
		if (fullParser.isHModel()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_OPERATION_IN_H_MODEL, opName);
			throw createMVKParserError(ctx, errMessage);
		}
		// ATTENTION: If the following statement is relaxed and the entity can be a root group,
		// one has to consider the transformation of NumberOfFilledGroups to NumberOfRootGroupsOperation in AnzahlAngegebenOpCreator.
		// Also the ExpandService may need to be adapted, then.
		if (!(ctx.parent instanceof SetOperOpContext)) {
			// In the iteration entity of SetOperOps, we may allow groups without changing the other operations
			checker.checkIsField(ref, opName, ctx);
		}
		checker.checkHasWildCards(ref, opName, ctx);
		new CheckEntityListenUtils(fullParser).checkNoSemanticIndex(Collections.singletonList(ref), ctx, opName);

		CheckSetExpressionUtil setExpressionChecker = new CheckSetExpressionUtil(fullParser);
		setExpressionChecker.checkIndicesInSetExpression(ref, ctx.filterCond, opName, false);
		setExpressionChecker.checkNoDisallowedConstructsInSetExpression(ctx.filterCond, opName);
		ref.setHasFilterCondition(true);
	}

	@Override
	public FullChecks_ConditionParser getParser() {
		return fullParser;
	}
}
