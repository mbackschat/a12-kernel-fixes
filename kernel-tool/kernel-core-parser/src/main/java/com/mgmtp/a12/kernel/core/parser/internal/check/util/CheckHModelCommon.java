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
package com.mgmtp.a12.kernel.core.parser.internal.check.util;

import org.antlr.v4.runtime.ParserRuleContext;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EinschraenkungContext;

/**
 * Hilfsmethoden für H-Models.
 * Gemeinsam verwendet zum Überprüfen der vordruck-Sprachkonstrukte und der Entitäten.
 */
public class CheckHModelCommon extends CheckImplAbstractClass {

	protected CheckHModelCommon(FullChecks_ConditionParser parser) {
		super(parser);
		checkNotLenient();
	}

	/**
	 * @return false if the parser is not lenient.
	 */
	protected boolean checkNotLenient() {
		if (getParser().isLenient()) {
			throw new IllegalArgumentException("Lenient HModel parsing not allowed!");
		}
		return false;
	}

	/**
	 * get the wildcard restrictions.
	 *
	 * @param ctx
	 * 		context to be analysed, is never null
	 * @param max
	 * 		max index, required to check if the given interval start is valid
	 * @param nameUsedForErrorOutput
	 * 		used only for nice error messages
	 * @param indexArt
	 * 		used for nice error messages - method is used for "zeile" and "lfd. Nr."
	 * @return the restriction interval.
	 */
	protected long[] getWildCardRestriction(EinschraenkungContext ctx, long max, String nameUsedForErrorOutput, String indexArt) {
		//get the interval
		long[] restriction = new long[2];

		restriction[0] = (ctx.z1 == null) ? 1 : Long.parseLong(ctx.z1.getText());
		restriction[1] = (ctx.z2 == null) ? max : Long.parseLong(ctx.z2.getText());

		//check if intervall is correct
		if (checkNotLenient()) {
			return restriction;
		}
		if (restriction[0] < 1 ||
				restriction[0] > restriction[1] ||
				restriction[0] > max) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_ILLEGAL_RESTRICTION_OF_WILDCARD,
					nameUsedForErrorOutput, indexArt);
			throw createMVKParserError(ctx, errMessage);
		}
		return restriction;
	}

	/**
	 * Überprüft, ob der übergebene String ein korrekter Index ist.
	 * Gibt den Index als long zurück.
	 *
	 * @param numberAsString
	 * 		zu prüfender Ziffernfolge als String
	 * @param ctx
	 * 		benötigt für Fehlermeldungen
	 * @param max
	 * 		der maximal erlaubte Index
	 * @param indexArt
	 * 		benötigt für Fehlermeldung
	 * @param fullNameForErrorMessage
	 * 		benötigt für Fehlermeldung
	 * @return den Index als Zahl
	 */
	public long checkIndex(String numberAsString, ParserRuleContext ctx, long max, String indexArt,
			String fullNameForErrorMessage) {
		if (numberAsString.startsWith("0")) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_H_MODEL_INVALID_INDEX_SPEC,
					fullNameForErrorMessage, indexArt);
			throw createMVKParserError(ctx, errMessage);
		}

		//grammar assures that the string can be transformed to a number
		long index = Long.parseLong(numberAsString);
		if (index > max && !checkNotLenient()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_H_MODEL_INDEX_TOO_BIG, fullNameForErrorMessage,
					index, indexArt, max);
			throw createMVKParserError(ctx, errMessage);
		}
		return index;
	}

}
