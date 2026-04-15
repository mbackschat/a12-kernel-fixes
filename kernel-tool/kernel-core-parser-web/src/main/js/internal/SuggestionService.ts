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
import type { ILexerConstructor, ITokenForAsyncContextAssist } from "@com.mgmtp.a12.antlrcodeeditor/antlrcodeeditor-core";

import type { ATNState, IntervalSet, Parser, ParserRuleContext, Token } from "antlr4ForA12Kernel";

import type { ICompletionProposal, IParserParameter, IPosition, IRange, ITokenGroup, Language } from "./api.js";
import { TokenKind } from "./api.js";
import type { RuleCallback, RuleCallbacks, RuleCtx } from "./ATNSearchService.js";
import { ATNSearchService } from "./ATNSearchService.js";
import { MultiLanguageHandler } from "./MultiLanguageHandler.js";
import { createLexer } from "./ParserService.js";
import { getBlackList, getEntityRuleContexts, getRuleIdx, prepareParser, recreateInputString } from "./Utils.js";

import { content as predicates } from "./content/predicates.js";
import { content as functions } from "./content/functions.js";
import { content as logicalOperators } from "./content/logicalOperators.js";
import { content as operators } from "./content/operators.js";
import { content as constants } from "./content/constants.js";
import type GrammarOnly_ConditionParser from "./gen/GrammarOnly_ConditionParser.js";

interface FunctionName {
	nameInternal: string;
	nameTargetLang: string;
}

function getFunctionsFromJson(
	propType: TokenKind,
	func: { "de-DE"?: { name: string }; "en-US"?: { name: string } }[],
	language: Language
): InternalCompletion {
	let key: "de-DE" | "en-US";
	switch (language) {
		case "de":
			key = "de-DE";
			break;
		case "en":
			key = "en-US";
			break;
		default:
			return { propType: TokenKind.CONSTANT, values: [] };
	}
	const values: FunctionName[] = func
		.filter(f => f["de-DE"] && f[key])
		.map(f => ({
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			nameInternal: f["de-DE"]!.name,
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			nameTargetLang: f[key]!.name
		}));
	return {
		propType: propType,
		values: values
	};
}

type InternalCompletion = { propType: TokenKind; values: FunctionName[] };

function getAllFunctionsFromJson(language: Language): InternalCompletion[] {
	return [
		getFunctionsFromJson(TokenKind.PREDICATE, predicates, language),
		getFunctionsFromJson(TokenKind.FUNCTION, functions, language),
		getFunctionsFromJson(TokenKind.OPERATOR, operators, language),
		getFunctionsFromJson(TokenKind.LOGICOP, logicalOperators, language),
		getFunctionsFromJson(TokenKind.CONSTANT, constants, language)
	];
}

const ALL_FUNCTIONS = {
	de: getAllFunctionsFromJson("de"),
	en: getAllFunctionsFromJson("en")
};

function toTokenString(tokenId: number, parser: GrammarOnly_ConditionParser): string {
	if (parser.literalNames.length > tokenId && parser.literalNames[tokenId] != null) {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return parser.literalNames[tokenId]!.slice(1, -1);
	}
	// TODO: Fix this the next time the file is edited.
	// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
	return "<" + parser.symbolicNames[tokenId]! + ">";
}

function toTokenStrings(tokenIdxs: IntervalSet, parser: GrammarOnly_ConditionParser): string[] {
	const tokenStrings = [];
	for (let i = 0; i < parser.literalNames.length; i++) {
		if (tokenIdxs.contains(i)) {
			const tokenString = toTokenString(i, parser);
			// We don't want suggestions of Dollars
			if (tokenString === "$") {
				continue;
			}
			tokenStrings.push(tokenString);
		}
	}
	return tokenStrings;
}

function isSpecialToken(token: string) {
	return token.startsWith("<") && token.endsWith(">");
}

const BRACKETS: string[] = ["(", ")", "{", "}", "[", "]"];

export function getProposalType(token: string): TokenKind | undefined {
	const value = ALL_FUNCTIONS["de"].find(ic => ic.values.find(fn => fn.nameTargetLang === token));
	if (value) {
		return value.propType;
	}
	if (token.startsWith(";;")) {
		return TokenKind.COMMENT;
	}
	if (token === "ApplikationsBedingung") {
		return TokenKind.PREDICATE;
	}
	if (token === "<Field>") {
		return TokenKind.FIELD;
	}
	if (isSpecialToken(token)) {
		return undefined;
	}
	if (BRACKETS.includes(token)) {
		return TokenKind.BRACKET;
	}
	if (token.match(/^[^0-9a-zA-Z]+$/)) {
		return TokenKind.SYMBOL;
	} else {
		return TokenKind.CONSTANT;
	}
}

function pushToken(
	token: string,
	result: ICompletionProposal[],
	translation: (x: string) => string,
	blackList: Set<string>,
	range: IRange
) {
	if (blackList.has(token)) {
		return;
	}
	const propType = getProposalType(token);
	if (propType === undefined) {
		return;
	}
	let prop = result.find(p => p.proposalType === propType);
	if (!prop) {
		prop = { proposalType: propType, proposalsFromGrammar: [], range };
		result.push(prop);
	}
	if (isSpecialToken(token)) {
		// We do not add special tokens. They may only trigger a proposal type.
		return;
	}
	prop.proposalsFromGrammar.push(translation(token));
}

function convertToProposals(
	tokenList: string[],
	language: Language,
	parserParameters: IParserParameter,
	range: IRange
): ICompletionProposal[] {
	const result: ICompletionProposal[] = [];
	const blackList = getBlackList(parserParameters);
	const mlhTranslations = MultiLanguageHandler.getTranslations();
	const translations: (n: string) => string =
		language === "de"
			? n => n
			: // TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			n => (mlhTranslations.has(n) ? mlhTranslations.get(n)! : n);
	tokenList.forEach(t => pushToken(t, result, translations, blackList, range));
	return result;
}

function findStartState(parser: Parser, ruleName: string): ATNState {
	const ruleIdx = getRuleIdx(parser, ruleName);
	// 2 is RuleStartState
	const states = parser._interp.atn.states.filter(
		s => s.ruleIndex === ruleIdx && s.stateType === 2
	);
	return states[0];
}

const entityListExceptions = {
	feldListenBedingung: ["FELD_ANGEGEBEN", "FELD_NICHT_ANGEGEBEN", "DATUMS_BEREICHE_UEBERLAPPEND"],
	feldListenOp: ["SUMME_VON_PRODUKTEN"],
	feldListenOpHModel: ["MAXIMAL_ANGEGEBENER_ZEILENINDEX"],
	wiederholungBedingung: ["WIEDERHOLUNG_NICHT_EINDEUTIG"],
	zeilenBedingung: ["ZEILE_NICHT_EINDEUTIG"]
};
const extraGroupConditions = {
	feldListenAnzahlOp: ["ANZAHL_ANGEGEBENER_KONTEXTE"]
};

function isInRuleWithName(
	parser: GrammarOnly_ConditionParser,
	openRules: RuleCtx[],
	rulesWithSymbolicNames: { [key: string]: string[] }
): boolean {
	for (const [ruleName, symbolicnames] of Object.entries(rulesWithSymbolicNames)) {
		const ruleIdx = getRuleIdx(parser, ruleName);
		const matchingOpenRules = openRules.filter(
			c =>
				c.rule === ruleIdx &&
				// TODO: Fix this the next time the file is edited.
				// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
				symbolicnames.includes(parser.symbolicNames[c.startToken.type]!)
		);
		if (matchingOpenRules.length > 0) {
			return true;
		}
	}
	return false;
}

function isExtraGroupCondition(parser: GrammarOnly_ConditionParser, openRules: RuleCtx[]): boolean {
	return isInRuleWithName(parser, openRules, extraGroupConditions);
}
function isEntityListException(parser: GrammarOnly_ConditionParser, openRules: RuleCtx[]): boolean {
	return isInRuleWithName(parser, openRules, entityListExceptions);
}

function atnSuggestion(
	language: Language,
	parserParams: IParserParameter,
	startRule: string,
	condition: string,
	pos: number,
	range: IRange,
	useEntitySpec: boolean
): ICompletionProposal[] {
	const parser = prepareParser(condition, language, parserParams);

	const additionalProposals: ICompletionProposal[] = [];
	const entityRules = ["entity", "entityHModel", "regelKontextEntity"].map(e =>
		getRuleIdx(parser, e)
	);
	const entityRulesCtx: ParserRuleContext[] = getEntityRuleContexts(parser, startRule, entityRules);
	parser.reset();
	const entitySpecCallback: RuleCallback = (startToken: Token, lastToken: Token, nextToken: Token, openRules: RuleCtx[]) => {
		const entityCtx: ParserRuleContext | undefined = entityRulesCtx.find(
			rc => rc.start.start === startToken.start
		);
		// If we are inside of an entity specification, we want to replace the complete entity, not only the current token.
		const ctxStart = entityCtx?.start ?? startToken;
		// either the ctx contains a parsed token or we use the lastToken (before the current token)
		const ctxEnd = entityCtx?.stop ?? lastToken;
		const entityListRuleNumber: number = getRuleIdx(parser, "entityListe");
		const groupConditionRule = ["groupOp", "kontextListenBedingung"].map(name => getRuleIdx(parser, name));
		const bedingungRule = getRuleIdx(parser, "bedingung");
		const semIndexRule = getRuleIdx(parser, "semanticIndex");
		const lastBedinungIdx = openRules.map(r => r.rule).lastIndexOf(bedingungRule);
		const lastSemIdxIdx = openRules.map(r => r.rule).lastIndexOf(semIndexRule);
		// +1 to avoid "wraparound" behaviour of slice if not found (-1).
		const openRulesInLastOpenBedingung = openRules.slice(
			Math.max(lastBedinungIdx, lastSemIdxIdx) + 1
		);
		const isInEntityList =
			openRulesInLastOpenBedingung.find(c => c.rule === entityListRuleNumber) &&
			!isEntityListException(parser, openRulesInLastOpenBedingung);
		const isInGroupCondition =
			openRulesInLastOpenBedingung.find(c => groupConditionRule.includes(c.rule)) ||
			isExtraGroupCondition(parser, openRulesInLastOpenBedingung);
		const entityRange: IRange = range.startColumn == ctxStart.column && range.startLineNumber == ctxStart.line
			&& range.endColumn == range.startColumn && range.endLineNumber == range.startLineNumber
			// If we are exactly before the entity, we don't want to replace the entity
			// This is analogous to the case before a token, where we also insert
			// Exactly in this case, the range is an empty range at the start of the context
			? range
			: {
				startColumn: ctxStart.column,
				startLineNumber: ctxStart.line,
				endColumn: ctxEnd.column + ctxEnd.stop - ctxEnd.start + 1,
				endLineNumber: ctxEnd.line
			};
		if (additionalProposals.length || !useEntitySpec) {
			// If we already have the field, don't add it again
			return checkForward(ctxEnd, nextToken, pos);
		}
		if (isInEntityList || isInGroupCondition) {
			additionalProposals.push({
				proposalType: TokenKind.GROUP,
				proposalsFromGrammar: [],
				range: entityRange
			});
			additionalProposals.push(
				...convertToProposals(["RegelKontext"], language, parserParams, entityRange)
			);
		}
		if (!isInGroupCondition) {
			additionalProposals.push({
				proposalType: TokenKind.FIELD,
				proposalsFromGrammar: [],
				range: entityRange
			});
		}
		return checkForward(ctxEnd, nextToken, pos);
	};

	const vordruckCallback: RuleCallback = (startToken: Token, _lastToken: Token, nextToken: Token, _openRules: RuleCtx[]) => {
		if (additionalProposals.length || !useEntitySpec) {
			// If we already have the proposal, don't add it again
			return checkForward(startToken, nextToken, pos);
		}
		additionalProposals.push({
			proposalType: TokenKind.ROOT_GROUP_NAME,
			proposalsFromGrammar: [],
			range: range.startColumn == startToken.column && range.startLineNumber == startToken.line
				&& range.endColumn == range.startColumn && range.endLineNumber == range.startLineNumber
				// If we are exactly before the entity, we don't want to replace the entity
				// This is analogous to the case before a token, where we also insert
				// Exactly in this case, the range is an empty range at the start of the token
				? range
				: {
					// formularSpec doesn't have nested forms like Group/Subgroup/Field, so we don't need to find
					// the end column/line from the context
					startColumn: startToken.column,
					startLineNumber: startToken.line,
					endLineNumber: startToken.line,
					endColumn: startToken.column + startToken.stop - startToken.start + 1
				}
		});
		return checkForward(startToken, nextToken, pos);
	};
	const formularRule = getRuleIdx(parser, "formularSpec");
	const ruleCallbacks: RuleCallbacks = new Map(entityRules.map(er => [er, entitySpecCallback]));
	ruleCallbacks.set(formularRule, vordruckCallback);
	const s: ATNState = findStartState(parser, startRule);
	parser.state = s.stateNumber;
	const searchService = new ATNSearchService(parser);
	const tokenIdxs = searchService.search(parser, pos, ruleCallbacks);
	const tokens = toTokenStrings(tokenIdxs, parser);

	return convertToProposals(tokens, language, parserParams, range).concat(additionalProposals);
}

function rangeFromToken(token: ITokenForAsyncContextAssist): IRange {
	return {
		startColumn: token.column,
		startLineNumber: token.lineNumber,
		endColumn: token.column + token.stop - token.start + 1,
		endLineNumber: token.lineNumber
	};
}

function isWhitespace(token: ITokenForAsyncContextAssist): boolean {
	return token.typeName.startsWith("ws.");
}

/**
 * Get suggestions for a set of tokens.
 *
 * @param tokens the tokens built from the input string.
 * @param position the position, a suggestion should be made for.
 * @param language the language used
 * @param parserParams the parser parameters.
 * @param startRule the start rule (e.g. "bedingung_start", "bedingung", "operation").
 * @returns a set of {@link ICompletionProposal}s.
 */
export function getSuggestions(
	tokens: ITokenForAsyncContextAssist[],
	position: IPosition,
	language: Language,
	parserParams: IParserParameter,
	startRule = "bedingung_start"
): ICompletionProposal[] {
	const tokensBeforeCursor = tokens.filter(
		token =>
			token.lineNumber < position.lineNumber ||
			(token.lineNumber === position.lineNumber &&
				token.column + token.stop - token.start < position.column)
	);
	let rangeToReplaceForward: IRange;
	let pos = 0;
	let rangeToReplaceBackward: IRange | undefined;
	if (tokensBeforeCursor.length >= tokens.length) {
		rangeToReplaceForward = emptyRange(position);
		if (tokens.length) {
			pos = tokens[tokens.length - 1].stop + 1;
			if (!isWhitespace(tokens[tokens.length - 1])) {
				rangeToReplaceBackward = rangeFromToken(tokens[tokens.length - 1]);
			}
		}
	} else {
		const token = tokens[tokensBeforeCursor.length];
		pos = token.start + position.column - token.column;
		if (pos === token.start) {
			// We are between two tokens, therefore we insert and don't replace
			rangeToReplaceForward = emptyRange(position);
		} else {
			rangeToReplaceForward = rangeFromToken(token);
		}
		if (pos === token.start && tokensBeforeCursor.length) {
			const token2 = tokens[tokensBeforeCursor.length - 1];
			// Between tokens, if we have no whitespace on left side we also search backwards
			if (!isWhitespace(token2)) {
				rangeToReplaceBackward = rangeFromToken(token2);
			}
		}
	}
	const originalInputString = recreateInputString(tokens);

	const result = atnSuggestion(
		language,
		parserParams,
		startRule,
		originalInputString,
		pos,
		rangeToReplaceForward,
		true
	);
	if (rangeToReplaceBackward !== undefined) {
		return result.concat(
			atnSuggestion(
				language,
				parserParams,
				startRule,
				originalInputString,
				pos - 1,
				rangeToReplaceBackward,
				false
			)
		);
	}
	return result;
}

function emptyRange(position: IPosition): IRange {
	return {
		startColumn: position.column,
		startLineNumber: position.lineNumber,
		endColumn: position.column,
		endLineNumber: position.lineNumber
	}
}

function addToken(
	symbolicName: string | null,
	literalName: string | null,
	result: ITokenGroup[]
) {
	if (symbolicName === null) {
		return;
	}
	const tokenType = getTokenType(symbolicName, literalName);

	let group = result.find(p => p.tokenType === tokenType);
	if (!group) {
		group = { tokenType: tokenType, tokens: [] };
		result.push(group);
	}

	group.tokens.push(symbolicName);
}

function getTokenType(symbolicName: string | null, literalName: string | null): TokenKind {
	if (literalName === null) {
		if (symbolicName === "Comment") {
			return TokenKind.COMMENT;
		} else {
			return TokenKind.CONSTANT;
		}
	}
	return getProposalType(literalName) ?? TokenKind.CONSTANT;
}

/**
 * Get all groups of tokens. The token names are the symbolic names used in the lexer.
 *
 * @returns a list of {@link ITokenGroup}s.
 */
export function tokenGroups(): ITokenGroup[] {
	const lexerClass: ILexerConstructor = createLexer("de")
	const symbolicNames = lexerClass.symbolicNames;
	const literalNames = lexerClass.literalNames;

	const result: ITokenGroup[] = [];
	symbolicNames.forEach((symbName: string | null, i: number) =>
		addToken(symbName, literalNames[i]?.slice(1, -1) ?? null, result)
	);
	return result;
}

/**
 * Checks if the position is between the lastToken and the nextToken,
 * if nextToken.start - lastToken.stop > 0 then we have whitespaces in between.
 * 
 * @param lastToken the last token that is lexed before the current one
 * @param nextToken the next token after or at the current position
 * @param pos the position to check against
 * @returns true, if pos in ]lastToken.stop, nextToken.start]
 */
function checkForward(lastToken: Token, nextToken: Token, pos: number): boolean {
	return lastToken.stop < pos && nextToken.start >= pos;
}
