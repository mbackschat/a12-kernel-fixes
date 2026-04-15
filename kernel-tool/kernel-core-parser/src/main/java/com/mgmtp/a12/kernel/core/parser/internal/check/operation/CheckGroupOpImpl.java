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

import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.GroupOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;

import jakarta.validation.constraints.NotNull;

/**
 * Implement the checks for <code>GroupOp</code> (at the moment, this is only the operator
 * <code>CurrentRepetition</code>):
 * <ul>
 * <li>Only one group is allowed as parameter: {@link ParserErrorTypesEnum#MVK_NO_FIELDS_ALLOWED},
 * only one parameter already checked through the grammar
 * <li>The group reference may not contain wildcards:
 * {@link ParserErrorTypesEnum#MVK_NO_WILDCARDS_ALLOWED}</li>
 * <li>The operation is only allowed in A12 models:
 * {@link ParserErrorTypesEnumForHModel#MVK_INVALID_OPERATION_IN_H_MODEL}</li>
 * </ul>
 */
public class CheckGroupOpImpl extends CheckImplAbstractClass {

	public CheckGroupOpImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull GroupOpContext ctx) {
		EntitySpecContext ectx = ctx.entitySpec();
		EntityRef ref = getEntityRef(ectx);
		CheckEntityUtils checker = new CheckEntityUtils(fullParser);
		String opName = ctx.groupOpName().getText();

		// referenced entity must be a group, without a wildcard
		checker.checkIsGroup(ref, opName, ectx);
		checker.checkNoWildCards(ref, opName, ectx);

		// in H models the operation is not allowed
		if (fullParser.isHModel()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_OPERATION_IN_H_MODEL, opName);
			throw createMVKParserError(ctx, errMessage);
		}

		Token t = ctx.groupOpName().start;
		if (t.getType() == GrammarOnly_ConditionParser.AKTUELLE_WIEDERHOLUNG) {
			addNumberOpType(ctx, 0, false);
		} else {
			throwUnknownTokenError(t);
			// never reached. Only for compiler
			throw new IllegalArgumentException();
		}
	}
}
