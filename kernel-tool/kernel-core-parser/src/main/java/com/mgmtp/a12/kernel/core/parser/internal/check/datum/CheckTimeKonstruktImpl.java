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
package com.mgmtp.a12.kernel.core.parser.internal.check.datum;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.DateUtils;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumParamContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.TimeKonstruktContext;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;

import jakarta.validation.constraints.NotNull;

/**
 *
 */
public class CheckTimeKonstruktImpl extends CheckBaseDateConstructionImpl {

	private static final int MAX_VALUE_HOUR = 23;
	private static final int MAX_VALUE_MINUTE = 59;
	private static final int MAX_VALUE_SECOND = 59;

	public CheckTimeKonstruktImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull TimeKonstruktContext ctx) {

		checkParam(ctx.stunden, MAX_VALUE_HOUR, GrammarOnly_ConditionParser.STUNDEN_AUS_ZEIT);
		checkParam(ctx.minuten, MAX_VALUE_MINUTE, GrammarOnly_ConditionParser.MINUTEN_AUS_ZEIT);
		checkParam(ctx.sekunden, MAX_VALUE_SECOND, GrammarOnly_ConditionParser.SEKUNDEN_AUS_ZEIT);

		String format = DateUtils.TIME_FORMAT;
		addDateOpType(ctx, format, ctx.getStart().getText());
	}

	private void checkParam(DatumParamContext dp, int maxValue, int expectedDatumExtractOp) {
		if (dp == null) {
			return;
		}
		if (dp.entitySpec() != null) {//specified by a field
			checkParamIfEntity(dp, 2, maxValue, false);
		} else if (dp.datumExtractOp() != null) {
			checkCorrectDatumExtractOp(dp, expectedDatumExtractOp);
		} else { // specified by a constant
			checkConstant(dp, 0, maxValue + 1, IFieldType.UNDEFINED);
		}
	}
}
