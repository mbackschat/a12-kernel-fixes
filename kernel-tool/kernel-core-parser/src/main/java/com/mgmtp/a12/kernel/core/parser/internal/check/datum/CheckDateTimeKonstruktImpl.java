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

import org.antlr.v4.runtime.ParserRuleContext;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.DateUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DateTimeKonstruktContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumAngabeContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.DateFormat;
import com.mgmtp.a12.kernel.core.parser.internal.util.OperationResultType;

import jakarta.validation.constraints.NotNull;

/**
 * Checker class for <code>checkDateTimeKonstruktOp</code>.
 * <p>
 * Checks, that the first parameter is a date specifying all date fields without unknown fields and
 * no time fields.
 * Also checks that the second parameter is a time specifying only time fields.
 */
public class CheckDateTimeKonstruktImpl extends CheckImplAbstractClass {

	private static final String DATE_TIME = "DateTime";

	public CheckDateTimeKonstruktImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull DateTimeKonstruktContext ctx) {
		OperationResultType timeResultType = getOpType(ctx.time);

		if (ctx.date.datumAngabe() != null) { // for feldOpMitDatumSpecial, we always have a correct date
			OperationResultType dateResultType = getOpType(ctx.date.datumAngabe());
			checkDateParam(ctx.date.datumAngabe(), dateResultType);
		}
		checkTimeParam(ctx.time, timeResultType);

		addDateOpType(ctx, DateUtils.DATETIME_FORMAT, ctx.getStart().getText());
	}

	private void checkDateParam(DatumAngabeContext dateCtx, OperationResultType dateResultType) {
		DateFormat dateFormat = dateResultType.getDateFormat();
		if (!DateFormat.formatsConsistentRegardingFormat(dateFormat, DateFormat.of("dd.MM.yyyy"), getParser().hasBaseYear())) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_WRONG_DATE_FORMAT_FOR_OP, DATE_TIME,
					dateResultType.getDateNameForErrorMessaging());
			throw createMVKParserError(dateCtx, errMessage);
		}
		if (!DateFormat.isFormatWithDateSpecification(dateFormat) || DateFormat.isFormatWithTimeSpecification(dateFormat)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_WRONG_DATE_FORMAT_FOR_OP, DATE_TIME,
					dateResultType.getDateNameForErrorMessaging());
			throw createMVKParserError(dateCtx, errMessage);
		}
		DateUtils.checkDateCompletelySpecified(dateCtx, this);
	}

	private void checkTimeParam(ParserRuleContext timeCtx, OperationResultType timeResultType) {
		DateFormat timeFormat = timeResultType.getDateFormat();
		if (DateFormat.isFormatWithDateSpecification(timeFormat) || !DateFormat.isFormatWithTimeSpecification(timeFormat)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_WRONG_DATE_FORMAT_FOR_OP, DATE_TIME,
					timeResultType.getDateNameForErrorMessaging());
			throw createMVKParserError(timeCtx, errMessage);
		}
	}
}
