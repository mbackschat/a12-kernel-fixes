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
package com.mgmtp.a12.kernel.core.parser.internal.grammar.languages;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.core.parser.internal.common.grammar.languages.AbstractMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;

/**
 * Handles the translation of the grammar terminals for rule conditions.
 */
public class ConditionMultiLanguageHandler extends AbstractMultiLanguageHandler {

	protected static final String LEXER_TERMINALS_BUNDLE = ConditionMultiLanguageHandler.class.getPackage()
			.getName() + ".LexerTerminals";

	private static final Map<Locale, Map<String, Integer>> MAP_OF_LOCALIZED_TERMINAL_IDS = new HashMap<>();
	private static final Map<Locale, Map<Integer, String>> MAP_OF_LOCALIZED_TERMINAL_NAMES = new HashMap<>();

	private static final int DEFAULT_TOKEN_TYPE = Base_ConditionLexer.NameOhnePunktSTRAmAnfang;

	static {
		initTerminals(LEXER_TERMINALS_BUNDLE, MAP_OF_LOCALIZED_TERMINAL_IDS, MAP_OF_LOCALIZED_TERMINAL_NAMES);
	}

	// The following token types are used in calculations.
	// It is already planned to extend the grammar.
	public enum SpecialTokenType {
		IF,
		ROUND_UP,
		ROUND_DOWN
	}

	public ConditionMultiLanguageHandler(Locale conditionLanguage, IProjectSpecificSettings pss) {
		this(conditionLanguage);
	}

	/**
	 * @param lang
	 * 		the language to use.
	 */
	public ConditionMultiLanguageHandler(Locale lang) {
		super(lang);
	}

	public String getTerminalName(SpecialTokenType specialTokenType) {
		switch (specialTokenType) {
			case IF:
				return Locale.GERMANY.equals(languageUsedInConditionText) ? "Wenn" : "If";
			case ROUND_UP:
				return Locale.GERMANY.equals(languageUsedInConditionText) ? "Aufrunden" : "RoundDown";
			case ROUND_DOWN:
				return Locale.GERMANY.equals(languageUsedInConditionText) ? "Abrunden" : "RoundUp";
			default:
				throw new IllegalArgumentException(String.format("The token type '%s' is not supported", specialTokenType));
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
