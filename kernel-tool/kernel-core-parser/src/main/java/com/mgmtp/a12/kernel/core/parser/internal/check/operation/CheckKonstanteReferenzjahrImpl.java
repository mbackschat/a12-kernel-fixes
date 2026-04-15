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

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.DateUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.KonstanteReferenzjahrContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.OperationResultType;
import com.mgmtp.a12.kernel.core.parser.internal.util.OperationResultType.OperationResultBaseType;

import jakarta.validation.constraints.NotNull;

/**
 * Checker for <code>konstanteReferenzjahr</code>.
 * <p>
 * Checks, that the model specifies a base year.  {@link ParserErrorTypesEnum#MVK_NO_BASE_YEAR}
 */
public class CheckKonstanteReferenzjahrImpl extends CheckImplAbstractClass {
	public CheckKonstanteReferenzjahrImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull KonstanteReferenzjahrContext ctx) {

		// constant must only be used if the model has a base year
		if (!fullParser.hasBaseYear()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_NO_BASE_YEAR);
			throw createMVKParserError(ctx, errMessage);
		}

		OperationResultType aDate = OperationResultType.createTypeWithFormat(OperationResultBaseType.BASE_YEAR, null,
				DateUtils.DATUM_FORMAT_WITH_YEAR, ctx.getText());
		addOpType(ctx, aDate);
	}
}
