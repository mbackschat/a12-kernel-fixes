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

package com.mgmtp.a12.kernel.core.parser.internal.common.error;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.misc.Interval;

import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserError;

public class ErrorHandlerUtil {
	public static void recover(
			LexerNoViableAltException e,
			CharStream inputStream,
			String errMsgPrefix,
			CommonParserErrorHandler errorHandler,
			int tokenStartCharPositionInLine,
			int tokenStartLine,
			Object origin) {
		String symbol = getSymbol(e, inputStream);
		String errMsg = errMsgPrefix + errorHandler.getParserErrorMessage(CommonParserErrorTypesEnum.MVK_LEXER_STANDARD_ERROR,
				symbol, tokenStartCharPositionInLine + 1);
		throw new MVKParserError(
				tokenStartCharPositionInLine,
				tokenStartCharPositionInLine + 1,
				tokenStartLine,
				tokenStartLine,
				origin,
				errMsg);
	}

	/**
	 * extrahiere das aktuelle Symbol aus der Exception
	 *
	 * @param e
	 * @return Symbol, das nicht erkannt wird, oder Leerzeichen, falls kein Symbol im Exception
	 * 		vorhanden.
	 */
	private static String getSymbol(LexerNoViableAltException e, CharStream inputStream) {
		String symbol = "";
		if (e.getStartIndex() >= 0 && e.getStartIndex() < inputStream.size()) {
			symbol = inputStream.getText(Interval.of(e.getStartIndex(), e.getStartIndex()));
			symbol = org.antlr.v4.runtime.misc.Utils.escapeWhitespace(symbol, false);
		}
		return symbol;
	}
}
