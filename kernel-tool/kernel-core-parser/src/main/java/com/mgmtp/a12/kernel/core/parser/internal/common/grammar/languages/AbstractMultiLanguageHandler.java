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
package com.mgmtp.a12.kernel.core.parser.internal.common.grammar.languages;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.antlr.v4.runtime.Lexer;

/**
 * Handles the translation of the grammar terminals. By changing the token type it is possible to
 * parse for different languages used in the condition text with the same grammar (the grammar uses
 * the german terminals).
 */
public abstract class AbstractMultiLanguageHandler {

	public static Set<Locale> SUPPORTED_CONDITION_LANGUAGES = Set.of(Locale.GERMANY, Locale.US);

	protected Map<String, Integer> terminalIds;
	private Map<Integer, String> terminalNames;

	protected Locale languageUsedInConditionText;

	/**
	 * Creates a multi language handler for the defined language, which can be used to get terminal
	 * names (see {@link #getTerminalName(int)}.
	 *
	 * @param lang
	 * 		the language to use.
	 */
	protected AbstractMultiLanguageHandler(Locale lang) {
		this.languageUsedInConditionText = AbstractMultiLanguageHandler.SUPPORTED_CONDITION_LANGUAGES.contains(lang) ?
				lang :
				Locale.GERMANY;
		this.terminalIds = getMapOfLocalizedTerminalIDs().get(languageUsedInConditionText);
		this.terminalNames = getMapOfLocalizedTerminalNames().get(languageUsedInConditionText);
	}

	protected static void initTerminals(String lexerTerminalsBundleName,
			Map<Locale, Map<String, Integer>> mapOfLocalizedTerminalIDs,
			Map<Locale, Map<Integer, String>> mapOfLocalizedTerminalNames) {
		// Init the terminals for all possible languages
		SUPPORTED_CONDITION_LANGUAGES.forEach(
				l -> initTerminals(l, lexerTerminalsBundleName, mapOfLocalizedTerminalIDs, mapOfLocalizedTerminalNames));
	}

	private static void initTerminals(Locale locale, String lexerTerminalsBundleName,
			Map<Locale, Map<String, Integer>> mapOfLocalizedTerminalIDs,
			Map<Locale, Map<Integer, String>> mapOfLocalizedTerminalNames) {
		synchronized (AbstractMultiLanguageHandler.class) {
			if (!mapOfLocalizedTerminalIDs.containsKey(locale)) {
				Map<String, Integer> terminals = new HashMap<>();
				Map<Integer, String> terminals2 = new HashMap<>();
				ResourceBundle bundle = ResourceBundle.getBundle(lexerTerminalsBundleName, locale);
				for (String key : bundle.keySet().stream()
						.sorted() // WARNING: in case of aliases. We sort in order to make the overrides in alias cases deterministic.
						.toList()
				) {
					terminals.put(key, (Integer) bundle.getObject(key));
					terminals2.put((Integer) bundle.getObject(key), key);
				}
				mapOfLocalizedTerminalIDs.put(locale, terminals);
				mapOfLocalizedTerminalNames.put(locale, terminals2);
			}
		}
	}

	public void setTypeInLexer(Lexer lexer) {
		String token = lexer.getText();
		if (terminalIds.containsKey(token)) {
			lexer.setType(terminalIds.get(token));
		} else {
			lexer.setType(getDefaultTokenType());
		}
	}

	public boolean hasToBeQuoted(String str) {
		return isATerminal(str);
	}

	private boolean isATerminal(String str) {
		return terminalIds.containsKey(str);
	}

	public String getTerminalName(int tokenType) {
		String terminalName = terminalNames.get(tokenType);
		if (terminalName == null) {
			String germanTerminalName = getMapOfLocalizedTerminalNames().get(Locale.GERMANY).get(tokenType);
			throw new IllegalArgumentException(String.format(
					"The token type '%s' is not defined for the language '%s' of condition texts (the german value is '%s'). ",
					tokenType, languageUsedInConditionText, germanTerminalName));
		}

		return terminalName;
	}

	public String getTerminalNameByGermanTerminalName(String germanTerminalName) {
		if (Locale.GERMANY.equals(languageUsedInConditionText)) {
			return germanTerminalName;
		}

		Integer tokenType = getMapOfLocalizedTerminalIDs().get(Locale.GERMANY).get(germanTerminalName);
		if (tokenType == null) {
			throw new IllegalArgumentException(String.format("The german token name '%s' is unknown.", germanTerminalName));
		}
		return getTerminalName(tokenType);
	}

	protected abstract Map<Locale, Map<String, Integer>> getMapOfLocalizedTerminalIDs();

	protected abstract Map<Locale, Map<Integer, String>> getMapOfLocalizedTerminalNames();

	protected abstract int getDefaultTokenType();

	public Locale getLanguageUsedInConditionText() {
		return languageUsedInConditionText;
	}
}
