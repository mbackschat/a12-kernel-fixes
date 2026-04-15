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
import type {ITokenForAsyncContextAssist} from "@com.mgmtp.a12.antlrcodeeditor/antlrcodeeditor-core";

import type {Lexer, Parser, ParserRuleContext, ParseTreeListener} from "antlr4ForA12Kernel";
import {CharStream, CommonTokenStream} from "antlr4ForA12Kernel";

import type {ILexerConstructor, IParserParameter, Language} from "./api.js";
import type GrammarOnly_ConditionParser from "./gen/GrammarOnly_ConditionParser.js";
import {createLexer, createParser} from "./ParserService.js";

export function recreateInputString(tokens: ITokenForAsyncContextAssist[]): string {
	let curPos = 0;
	let str = "";
	for (const token of tokens) {
		if (token.start < curPos) {
			// This should not happen
			// and we want anything instead of an error
			continue;
		}
		str += " ".repeat(token.start - curPos);
		curPos += token.start - curPos;
		str += token.text;
		curPos += token.text.length;
	}
	return str;
}

export function getRuleIdx(parser: Parser, ruleName: string): number {
	// @ts-expect-error parser.ruleNames is not present in the typings, but appears to be always there when we need it
	// TODO cleanup. Alternatively, we might be able to change the type to GrammarOnly_ConditionParser, which does have ruleNames
	// (parser as GrammarOnly_ConditionParser).ruleNames
	return parser.ruleNames.indexOf(ruleName);
}

export function prepareLexer(inputString: string, language: Language): Lexer {
	const lexerClass: ILexerConstructor & (new (...args: any[]) => Lexer) /* todo dedup this type */ = createLexer(language);
	const inputStream: CharStream = new CharStream(inputString);
	return new lexerClass(inputStream);
}

export function prepareParser(
	inputString: string,
	language: Language,
	parserParameters: IParserParameter
): GrammarOnly_ConditionParser {
	const lexer = prepareLexer(inputString, language);
	const parserClass = createParser(parserParameters);
	const tokenStream: CommonTokenStream = new CommonTokenStream(lexer);
	return new parserClass(tokenStream) as GrammarOnly_ConditionParser;
}

class EntitySpecEndListener implements ParseTreeListener {
	private _ctx: ParserRuleContext[] = [];
	constructor(private _entityRules: number[]) { }

	public exitEveryRule(ctx: ParserRuleContext) {
		// @ts-expect-error ctx.ruleIndex is not declared on ParserRuleContext, but seems to be present in all generated subclasses
		// TODO maybe make a pr @antlr4?
		if (this._entityRules.includes(ctx.ruleIndex)) {
			this._ctx.push(ctx);
		}
	}

	public get ctx() {
		return this._ctx;
	}

	public enterEveryRule() {
		// Nothing to do
	}

	public visitErrorNode() {
		// Nothing to do
	}

	public visitTerminal() {
		// Nothing to do
	}
}

export function getEntityRuleContexts(
	parser: Parser,
	startRule: string,
	entityRules: number[]
): ParserRuleContext[] {
	parser.reset();
	parser.removeErrorListeners();
	const listener = new EntitySpecEndListener(entityRules);
	parser.addParseListener(listener);
	(<any>parser)[startRule]();

	return listener.ctx;
}
export function getBlackList(parserParams: IParserParameter): Set<string> {
	if (!parserParams.extmp) {
		return new Set<string>([
			// NOTE this blacklist is currently bigger than the a12 blacklist in java,
			// because it also contains entries that are not blacklisted in A12 but prevented by
			// isA12Model / isHModel in the parser. This is currently necessary as a workaround,
			// so that test-suggestions.checkTranslations knows which names to ignore.
			"AktuelleVordruckNummer",
			"AnzahlAngegebenerVordrucke",
			"AnzahlAngegebeneUsb",
			"KeinVordruckAngegeben",
			"MaximalAngegebenerZeilenindex",
			"MindestensEinVordruckAngegeben",
			"UsbNichtFortlaufend",
			"VordruckAngegeben",
			"VordruckNichtAngegeben",
			"VordruckNichtFortlaufend",
			"ZeileNichtEindeutig",
			"ZeilenLueckenlos",
			"ZeilenNichtAufsteigend",
			"#Zeile",
			"WertNichtKonsistent"
		]);
	}
	return new Set();
}
