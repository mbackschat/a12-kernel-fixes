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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation;

import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumAddOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.ParseTreeCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.util.CreatorUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.datum.DateAddOperationImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateAddOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.DateAddOperationName;

/**
 * Erzeugt für die Grammatik-Regel
 * <p>
 * <code>
 * datumAddOp: datumAddOpName '(' da=datumAngabe ',' amount=operation ')' ;
 * <p>
 * datumAddOpName: 'AddiereJahre' | 'AddiereMonate' | 'AddiereTage' | 'AddiereStunden' | 'AddiereMinuten' | 'AddiereSekunden';
 * </code>
 * <p>
 * das richtige Parsebaum-Objekt.
 */
public class DatumAddOpCreator {

	private DatumAddOpCreator() {
		throw new IllegalStateException("Utility class");
	}

	public static void create(DatumAddOpContext ctx, ParseTreeCreator tc) {

		DateAddOperationName name = extractOperationNameFromContext(ctx);

		//reverse ordering, because it is a stack
		IOperation amountSpec = tc.popFop();
		IOperation datumAngabe = tc.popFop();

		IDateAddOperation result = new DateAddOperationImpl(name, datumAngabe, amountSpec);

		tc.push(result);
	}

	/**
	 * Konvertiert den Operationsnamen aus dem Kontext in die internen Namen.
	 */
	public static DateAddOperationName extractOperationNameFromContext(DatumAddOpContext ctx) {
		Token t = ctx.datumAddOpName().start;
		int type = t.getType();
		switch (type) {
			case GrammarOnly_ConditionParser.ADDIERE_JAHRE:
				return DateAddOperationName.AddYears;
			case GrammarOnly_ConditionParser.ADDIERE_MONATE:
				return DateAddOperationName.AddMonths;
			case GrammarOnly_ConditionParser.ADDIERE_TAGE:
				return DateAddOperationName.AddDays;
			case GrammarOnly_ConditionParser.ADDIERE_STUNDEN:
				return DateAddOperationName.AddHours;
			case GrammarOnly_ConditionParser.ADDIERE_MINUTEN:
				return DateAddOperationName.AddMinutes;
			case GrammarOnly_ConditionParser.ADDIERE_SEKUNDEN:
				return DateAddOperationName.AddSeconds;
			default:
				throw new IllegalArgumentException(CreatorUtils.getUnknownTokenErrorMessage(t));
		}
	}
}
