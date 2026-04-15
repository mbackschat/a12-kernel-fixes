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
package com.mgmtp.a12.kernel.core.parser.internal.check.operation.hdv;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckHModelCommon;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldOpHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.SpezifierHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;

/**
 * provides all checks for grammar rule <code>FeldOpHModel</code> <code><p>
 * feldOpHModel: feldOpHModelName '(' entitySpec ')';<p>
 * feldOpHModelName:  'AktuelleVordruckNummer';
 * </code>
 */
public class CheckFeldOpHModelImpl extends CheckHModelCommon {
	public CheckFeldOpHModelImpl(FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(FeldOpHModelContext ctx) {
		addNumberOpType(ctx, 0, false);

		CheckEntityUtils checker = new CheckEntityUtils(fullParser);
		EntitySpecContext ectx = ctx.entitySpec();
		EntityRef ref = getEntityRef(ectx);
		String opName = ctx.feldOpHModelName().getText();

		checker.checkIsField(ref, opName, ctx);
		checker.checkNoWildCards(ref, opName, ctx);
		checker.checkNoCategories(ectx, opName);

		if (ref.getMaxLfdNr() == 1 || ebenenLOrUSpecified(ctx.entitySpec())) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_L_NO_MULT, opName);
			throw createMVKParserError(ctx, errMessage);
		}

	}

	private boolean ebenenLOrUSpecified(EntitySpecContext entitySpec) {
		SpezifierHModelContext fc = entitySpec.entityHModel().spezifierHModel();
		return (fc != null && (fc.lfdNrSpezifier() != null || fc.usbSpezifier() != null));
	}
}
