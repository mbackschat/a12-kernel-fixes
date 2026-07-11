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
package com.mgmtp.a12.kernel.core.parser.internal.check.operation;

import java.util.Collections;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityListenUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckOpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckSetExpressionUtil;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.SetOperOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.OperationResultType;

import jakarta.validation.constraints.NotNull;

/**
 * Provides the additional checks for grammar rule <code>setOperOp</code> (language construct <code>SummeVonTermen</code>)
 */
public class CheckSetOperOpImpl extends CheckImplAbstractClass {
	public CheckSetOperOpImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull SetOperOpContext ctx) {

		CheckEntityUtils checker = new CheckEntityUtils(fullParser);
		String opName = ctx.setOperOpName().getText();

		// in HDVs this operation is not allowed
		if (fullParser.isHModel()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_OPERATION_IN_H_MODEL, opName);
			throw createMVKParserError(ctx, errMessage);
		}

		EntityRef iterationGroup = fullParser.getEntityRef(ctx.iterationEntity);
		checker.checkIsGroup(iterationGroup, opName, ctx.iterationEntity);
		checker.checkRepeatableOnLowestLevel(iterationGroup, ctx.iterationEntity);
		checker.checkHasWildcardOnLowestLevel(iterationGroup, ctx.iterationEntity);
		new CheckEntityListenUtils(fullParser).checkNoSemanticIndex(Collections.singletonList(iterationGroup), ctx, opName);

		CheckSetExpressionUtil setExpressionUtil = new CheckSetExpressionUtil(fullParser);
		// CustomCondition and RepetitionNotUnique cannot easily be in an operation, but we check it even though
		setExpressionUtil.checkNoDisallowedConstructsInSetExpression(ctx.setOperation(), opName);
		setExpressionUtil.checkIndicesInSetExpression(iterationGroup, ctx.setOperation(), opName, true);

		setExpressionUtil.adaptIndicesInSetExpression(iterationGroup, ctx.setOperation());

		CheckOpUtils.checkOpIsLikeANumber(ctx.setOperation().operation(), opName, this, ParserErrorTypesEnum.MVK_NO_NUMBER);
		OperationResultType opType = this.getOpType(ctx.setOperation().operation());
		addOpType(ctx, opType);
	}
}
