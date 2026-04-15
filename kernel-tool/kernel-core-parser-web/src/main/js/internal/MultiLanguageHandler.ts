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

import type Base_ConditionLexer from "./gen/Base_ConditionLexer.js";
import GrammarOnly_ConditionParser from "./gen/GrammarOnly_ConditionParser.js";

import { content as constants } from "./content/constants.js";
import { content as functions } from "./content/functions.js";
import { content as logicalOperators } from "./content/logicalOperators.js";
import { content as operators } from "./content/operators.js";
import { content as predicates } from "./content/predicates.js";

export class MultiLanguageHandler {
	terminalIds: Map<string, number>;

	constructor(language: "de" | "en", lexerClassLiteralNames: (string | null)[]) {
		this.terminalIds = MultiLanguageHandler.getTokens(language, lexerClassLiteralNames);
	}

	static getTokens(language: "de" | "en", lexerClassLiteralNames: (string | null)[]): Map<string, number> {
		const literals: Map<string, number> = new Map();
		lexerClassLiteralNames.forEach((v, idx) => {
			if (v !== null) {
				literals.set(MultiLanguageHandler.removeQuotes(v), idx);
			}
		});
		if (language === "de") {
			return literals;
		}

		const translations: Map<string, string> = MultiLanguageHandler.getTranslations();
		const result: Map<string, number> = new Map();
		literals.forEach((v, k) => {
			const newKey = translations.get(k);
			result.set(newKey ? newKey : k, v);
		});

		return result;
	}

	static getTranslations(): Map<string, string> {
		const result: Map<string, string> = new Map();
		this.addTranslations(predicates, result);
		this.addTranslations(functions, result);
		this.addTranslations(logicalOperators, result);
		this.addTranslations(operators, result);
		this.addTranslations(constants, result);
		return result;
	}

	private static addTranslations(
		func: { "de-DE"?: { name: string }; "en-US"?: { name: string } }[],
		result: Map<string, string>
	) {
		for (const f of func) {
			if (f["de-DE"] && f["en-US"]) {
				result.set(f["de-DE"]["name"], f["en-US"]["name"]);
			}
			if (!f["de-DE"] && f["en-US"]) {
				throw Error('No german translation for language construct "${f["en-US"]["name"]}"');
			}
		}
	}

	static removeQuotes(str: string): string {
		return str[0] === "'" ? str.slice(1, -1) : str;
	}

	public setTypeInLexer(lexer: Base_ConditionLexer): void {
		const token: string = lexer.text;
		if (this.terminalIds.has(token)) {
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			lexer._type = this.terminalIds.get(token)!;
		} else {
			lexer._type = GrammarOnly_ConditionParser.NameOhnePunktSTRAmAnfang;
		}
	}
}
