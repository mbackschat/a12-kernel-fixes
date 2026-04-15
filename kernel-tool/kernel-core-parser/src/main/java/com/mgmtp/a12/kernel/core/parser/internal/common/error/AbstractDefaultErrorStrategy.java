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

import java.util.Locale;

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.text.translate.UnicodeUnescaper;

import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserError;

/**
 * Implementation of the {@link ANTLRErrorStrategy} to handle overall error situations.
 */
public abstract class AbstractDefaultErrorStrategy extends DefaultErrorStrategy {

	private final CommonParserErrorHandler errorHandler;

	protected String errMsgPrefix = "";

	protected AbstractDefaultErrorStrategy(Locale languageUsedInConditionText) {
		super();
		this.errorHandler = new CommonParserErrorHandler(languageUsedInConditionText);
	}

	/**
	 * Von Fehlermeldungen soll sich nicht erholt werden, daher werden die Recover-Methoden
	 * überschrieben vergl. antlr4 Reference S. 173
	 */
	@Override
	public void recover(Parser recognizer, RecognitionException recognitionException) {
		reportError(recognizer, recognitionException);
	}

	@Override
	public Token recoverInline(Parser recognizer) {
		reportError(recognizer, new InputMismatchException(recognizer));
		//wird nie zurückgegeben, da reportError eine Exception wirft, das weiß die Methode aber nicht...
		return null;
	}

	/**
	 * Erhole dich auch nicht von Problemen in Sub-Regeln. - vergl. antlr4 Reference S. 173
	 */
	@Override
	public void sync(Parser recognizer) {
		// at the moment, we don't do anything here
	}

	@Override
	public void reportError(Parser recognizer, RecognitionException recognitionException) {
		String message = "";

		if (recognitionException instanceof NoViableAltException) {
			NoViableAltException ec = (NoViableAltException) recognitionException;
			Token token = ec.getOffendingToken();
			String actual = getTokenErrorDisplay(token);

			if (token == null || token.getType() == Token.EOF) {
				message = getErrorMessage(CommonParserErrorTypesEnum.MVK_INCOMPLETE_INPUT);
			} else {
				message = getErrorMessage(CommonParserErrorTypesEnum.MVK_UNEXPECTED_TOKEN, actual);
			}
			throwException(message, token);
		} else if (recognitionException instanceof InputMismatchException) {
			InputMismatchException ec = (InputMismatchException) recognitionException;
			Token offendingToken = ec.getOffendingToken();

			IntervalSet expectedTokens = ec.getExpectedTokens();
			Vocabulary voc = ec.getRecognizer().getVocabulary();
			String expected = new UnicodeUnescaper().translate(
					expectedTokens.toString(voc)); // since between antlr 4.9.3 and 4.10.1,
			// unicode appears to be reported as the escape sequence now, instead of the uniqcode character. e.g.
			// "'NichtF\u00FCr'" instead of "'NichtFür'"
			String actual = getTokenErrorDisplay(offendingToken);

			if (offendingToken.getType() == Token.EOF) {
				message = getErrorMessage(CommonParserErrorTypesEnum.MVK_EXPECTED_TOKEN_NOT_FOUND, expected);
			} else if (expectedTokens.contains(Token.EOF) || expectedTokens.isNil()) {
				message = getErrorMessage(CommonParserErrorTypesEnum.MVK_UNEXPECTED_TOKEN, actual);
			} else {
				message = getErrorMessage(CommonParserErrorTypesEnum.MVK_UNEXPECTED_TOKEN, actual);
			}
			throwException(message, offendingToken);
		} else {
			message = getSimpleErrorMessage(CommonParserErrorTypesEnum.MVK_COMMON_PARSER_ERROR,
					recognitionException.getClass().getName());
			throwException(message, null);
		}

	}

	private String getExpectedString(Parser recognizer) {
		IntervalSet expecting = getExpectedTokens(recognizer);
		Vocabulary voc = recognizer.getVocabulary();
		return expecting.toString(voc);
	}

	@Override
	protected void reportMissingToken(Parser recognizer) {

		Token token = recognizer.getCurrentToken();
		String actual = getTokenErrorDisplay(token);

		String errorMsg;
		String expected = getExpectedString(recognizer);

		if (token == null || token.getText() == null || token.getType() == Token.EOF) {
			errorMsg = getErrorMessage(CommonParserErrorTypesEnum.MVK_EXPECTED_TOKEN_NOT_FOUND, expected);
		} else {
			errorMsg = getErrorMessage(CommonParserErrorTypesEnum.MVK_UNEXPECTED_TOKEN, actual);
		}
		throwException(errorMsg, token);
	}

	@Override
	protected void reportUnwantedToken(Parser recognizer) {
		reportMissingToken(recognizer);
	}

	private String getErrorMessage(CommonParserErrorTypesEnum err, Object... args) {
		Object[] newArgs = ArrayUtils.insert(0, args, getInputTypeFragment());
		return errMsgPrefix + errorHandler.getParserErrorMessage(err, newArgs);
	}

	private String getSimpleErrorMessage(CommonParserErrorTypesEnum err, Object... args) {
		return errMsgPrefix + errorHandler.getParserErrorMessage(err, args);
	}

	protected abstract String getInputTypeFragment();

	private void throwException(String message, Token token) {
		if (token == null) {
			throw new MVKParserError(getOrigin(), message);
		}
		throw new MVKParserError(token, token, getOrigin(), message);
	}

	protected abstract Object getOrigin();

}