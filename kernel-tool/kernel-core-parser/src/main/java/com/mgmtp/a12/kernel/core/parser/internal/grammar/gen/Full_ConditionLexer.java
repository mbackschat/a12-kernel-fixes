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
package com.mgmtp.a12.kernel.core.parser.internal.grammar.gen;

import java.util.Locale;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.LexerNoViableAltException;

import com.mgmtp.a12.kernel.core.parser.internal.common.error.CommonParserErrorHandler;
import com.mgmtp.a12.kernel.core.parser.internal.common.error.ErrorHandlerUtil;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;

/**
 * Fehler-Handling im Lexer
 */
public class Full_ConditionLexer extends Base_ConditionLexer {

	private CommonParserErrorHandler errorHandler;

	protected String errMsgPrefix = "";

	private Object originForErrorsInLexer;

	public Full_ConditionLexer(CharStream input) {
		super(input);
	}

	public void initLexer(Object originForErrorsInLexer, Locale conditionLanguage, IProjectSpecificSettings pss) {
		super.initLexer(new ConditionMultiLanguageHandler(conditionLanguage, pss));
		this.errorHandler = new CommonParserErrorHandler(conditionLanguage);
		this.originForErrorsInLexer = originForErrorsInLexer;
	}

	/**
	 * Keine recover-Strategie beim Lexer.
	 */
	@Override
	public void recover(LexerNoViableAltException e) {
		ErrorHandlerUtil.recover(e, getInputStream(), errMsgPrefix, errorHandler,
				_tokenStartCharPositionInLine, _tokenStartLine,
				getOrigin());
	}

	protected Object getOrigin() {
		return originForErrorsInLexer;
	}
}
