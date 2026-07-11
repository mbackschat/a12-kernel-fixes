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
package com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.languages;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.core.parser.internal.common.grammar.languages.AbstractMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.BaseErrorTextParameterLexer;

/**
 * Handles the translation of the grammar terminals for error text parameters.
 */
public class ErrortextParameterMultiLanguageHandler extends AbstractMultiLanguageHandler {

	protected static final String LEXER_TERMINALS_BUNDLE = ErrortextParameterMultiLanguageHandler.class.getPackage()
			.getName() + ".LexerTerminals";

	private static final Map<Locale, Map<String, Integer>> MAP_OF_LOCALIZED_TERMINAL_IDS = new HashMap<>();
	private static final Map<Locale, Map<Integer, String>> MAP_OF_LOCALIZED_TERMINAL_NAMES = new HashMap<>();

	private static final int DEFAULT_TOKEN_TYPE = BaseErrorTextParameterLexer.NameWithoutDotSTRAtBeginning;

	static {
		initTerminals(LEXER_TERMINALS_BUNDLE, MAP_OF_LOCALIZED_TERMINAL_IDS, MAP_OF_LOCALIZED_TERMINAL_NAMES);
	}

	public ErrortextParameterMultiLanguageHandler(Locale language) {
		super(language);
	}

	@Override
	public boolean hasToBeQuoted(String str) {
		if (!terminalIds.containsKey(str)) {
			return false;
		}
		int tokenType = terminalIds.get(str);
		/*
		 * See parser rule 'reservedKeyWordCurrentlyAllowedWithinEntityName': for those keywords
		 * that historically are allowed to be unquoted inside error text parameters, we don't add
		 * quotes while formatting.
		 * This list may only be changed together with the parser rule
		 * 'reservedKeyWordCurrentlyAllowedWithinEntityName'.
		 */
		switch (tokenType) {
			case BaseErrorTextParameterLexer.VALUE:
			case BaseErrorTextParameterLexer.VORDRUCKZEILE:
			case BaseErrorTextParameterLexer.ROOT_GROUP_INDEX:
			case BaseErrorTextParameterLexer.LINE:
			case BaseErrorTextParameterLexer.USB:
			case BaseErrorTextParameterLexer.INDEX:
				return false;
			default:
				return super.hasToBeQuoted(str);
		}
	}

	@Override
	protected Map<Locale, Map<String, Integer>> getMapOfLocalizedTerminalIDs() {
		return MAP_OF_LOCALIZED_TERMINAL_IDS;
	}

	@Override
	protected Map<Locale, Map<Integer, String>> getMapOfLocalizedTerminalNames() {
		return MAP_OF_LOCALIZED_TERMINAL_NAMES;
	}

	@Override
	protected int getDefaultTokenType() {
		return DEFAULT_TOKEN_TYPE;
	}

}
