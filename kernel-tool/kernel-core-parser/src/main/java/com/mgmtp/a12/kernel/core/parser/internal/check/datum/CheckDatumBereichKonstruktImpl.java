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

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.FieldUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumBereichKonstruktContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.util.DateFormat;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

import jakarta.validation.constraints.NotNull;

/**
 * Checker class for <code>checkDatumBereichKonstrukt</code>.
 * <p>
 * Checks, that both fields specify dates and that their formats have the same components.
 */
public class CheckDatumBereichKonstruktImpl extends CheckImplAbstractClass {
	public CheckDatumBereichKonstruktImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(DatumBereichKonstruktContext ctx) {
		GrammarOnly_ConditionParser.EntitySpecContext e1ctx = ctx.date1;
		GrammarOnly_ConditionParser.EntitySpecContext e2ctx = ctx.date2;

		String opName = getOperationName();
		String date1FormatStr = checkDateFieldAndReturnFormat(e1ctx, opName);
		String date2FormatStr = checkDateFieldAndReturnFormat(e2ctx, opName);

		DateFormat date1Format = DateFormat.of(date1FormatStr);
		DateFormat date2Format = DateFormat.of(date2FormatStr);

		checkSameComponents(date1Format, date2Format, e2ctx);

		// if date1Format doesn't contain a year but date2Format does, we use date2Format
		DateFormat resultFormat = date1Format.hasAllComponents(date2Format) ? date1Format : date2Format;

		addDateRangeOpType(ctx, resultFormat, ctx.getStart().getText());
	}

	private String getOperationName() {
		return new ConditionMultiLanguageHandler(parser.getLanguageUsedInConditionText(),
				parser.getPss()).getTerminalName(Base_ConditionLexer.DATUM_BEREICH);
	}

	private String checkDateFieldAndReturnFormat(GrammarOnly_ConditionParser.EntitySpecContext eCtx, String opName) {
		CheckEntityUtils checker = new CheckEntityUtils(fullParser);
		EntityRef ref = getEntityRef(eCtx);

		IField f = checker.checkIsField(ref, opName, eCtx);
		checker.checkNoWildCards(ref, opName, eCtx);

		if (!FieldUtils.isDate(f) || FieldUtils.isDatePartialKnown(f)) {
			throwError(eCtx, opName, ref);
		}
		IDateType dateType = (IDateType) f.getType();
		String formatStr = dateType.getFormat();

		DateFormat dateFormat = DateFormat.of(formatStr);
		if (!DateFormat.isFormatWithDateSpecification(dateFormat) || DateFormat.isFormatWithTimeSpecification(dateFormat)) {
			throwError(eCtx, getOperationName(), ref);
		}

		return formatStr;
	}

	private void throwError(GrammarOnly_ConditionParser.EntitySpecContext eCtx, String opName, EntityRef ref) {
		String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_WRONG_DATE_FORMAT_FOR_OP, opName,
				ref.getNameBuiltFromConditionText());
		throw createMVKParserError(eCtx, errMessage);
	}

	private void checkSameComponents(DateFormat dateFormat1, DateFormat dateFormat2,
			GrammarOnly_ConditionParser.EntitySpecContext ctx) {
		// We don't want to include the year addition semantics, therefore we set hasBaseYear always to false
		if (!dateFormat1.hasSameComponents(dateFormat2, false)) {
			EntityRef ref = getEntityRef(ctx);
			throwError(ctx, getOperationName(), ref);
		}
	}
}
