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

package com.mgmtp.a12.kernel.core.parser.internal.check.entity.semanticIndex;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplLenientBaseClass;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.SemanticIndexContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.SemanticIndexRef;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

public class CheckSemanticLenientIndexImpl extends CheckImplLenientBaseClass {

	public CheckSemanticLenientIndexImpl(Lenient_ConditionParser parser) {
		super(parser);
	}

	public void check(SemanticIndexContext ctx) {
		EntitySpecContext valueSpecContext = getEnclosingEntitySpecContext(ctx.getParent());
		EntityRef valueSpecRef = getEntityRef(valueSpecContext);

		EntityRef semIdxEntity = null;
		if (ctx.e != null) {
			semIdxEntity = getEntityRef(ctx.e);
		}

		doChecks(ctx, valueSpecRef, valueSpecContext, semIdxEntity);

		if (ctx.e != null) {
			valueSpecRef.setSemanticIndex(new SemanticIndexRef(semIdxEntity));
		} else {
			String constant = CommonUtils.removeAndUnescapeQuotesAndRemoveCrFromCrLf(ctx.konst.getText());
			valueSpecRef.setSemanticIndex(new SemanticIndexRef(constant));
		}
	}

	protected void doChecks(GrammarOnly_ConditionParser.SemanticIndexContext ctx, EntityRef valueSpecRef,
			GrammarOnly_ConditionParser.EntitySpecContext valueSpecContext, EntityRef semIdxEntity) {
		// This is only needed for FullChecks_ConditionParser
	}

}
