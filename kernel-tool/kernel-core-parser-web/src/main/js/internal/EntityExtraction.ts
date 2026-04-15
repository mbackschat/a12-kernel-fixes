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

import {ParserRuleContext, TerminalNode} from "antlr4ForA12Kernel";

import type {IParserParameter, IPosition, Language} from "./api.js";
import type GrammarOnly_ConditionParser from "./gen/GrammarOnly_ConditionParser.js";
import {
    EntityContext,
    EntityHModelContext,
    IdNameContext,
    RegelKontextEntityContext,
    RelativeUpContext
} from "./gen/GrammarOnly_ConditionParser.js";
import {getEntityRuleContexts, getRuleIdx, prepareParser, recreateInputString} from "./Utils.js";

/**
 * Computes the path of the entity at the given position in the condition.
 *
 * The full path of the entity (starting with "/") will be returned if the entity is *not*
 * referenced by shortname, otherwise the shortname (not containing "/") is returned.
 *
 * If the position is not within an entity, undefined is returned.
 *
 * This method does not consider data from the corresponding document model, i.e. it cannot
 * check if the referenced entities actually exist, and does not know about the type of the entities.
 * This has to be done by the caller.
 */
export function getReferencedEntityPath(
	tokens: ITokenForAsyncContextAssist[],
	position: IPosition,
	ruleGroupFullName: string,
	language: Language,
	parserParams: IParserParameter,
	startRule = "bedingung_start"
): string | undefined {
	const originalInputString = recreateInputString(tokens);
	const tokensBeforeCursor = tokens.filter(
		t =>
			t.lineNumber < position.lineNumber ||
			(t.lineNumber === position.lineNumber &&
				t.column + t.stop - t.start < position.column)
	);
	if (tokensBeforeCursor.length >= tokens.length) {
		// position after last token
		return undefined;
	}
	const token = tokens[tokensBeforeCursor.length];
	const pos = token.start + position.column - token.column;
	return extractEntities(
		language,
		parserParams,
		startRule,
		originalInputString,
		pos,
		ruleGroupFullName
	);
}

function extractEntities(
	language: Language,
	parserParams: IParserParameter,
	startRule: string,
	condition: string,
	pos: number,
	ruleGroupFullName: string
): string | undefined {
	const parser = prepareParser(condition, language, parserParams);
	const entityRules = ["entity", "entityHModel", "regelKontextEntity"].map(e =>
		getRuleIdx(parser, e)
	);
	const entityRulesCtx: ParserRuleContext[] = getEntityRuleContexts(parser, startRule, entityRules);
	return getEntityFromCtxs(entityRulesCtx, pos, parser, ruleGroupFullName);
}

function getEntityFromCtxs(
	ctxs: ParserRuleContext[],
	pos: number,
	parser: GrammarOnly_ConditionParser,
	ruleGroupFullName: string
): string | undefined {
	const ctx: ParserRuleContext | undefined = ctxs.find(c => c.start.start <= pos && (c.stop ?? c.start).stop >= pos)
	if (!ctx) {
		return undefined;
	}
	if (ctx instanceof RegelKontextEntityContext) {
		// Do the deescaping for the rule path
		return createFullName(ruleGroupFullName.split("/"), ruleGroupFullName);
	} else {
		const nameParts = extractEntityPartsFromEntityCtx(ctx, parser, pos);
		return createFullName(nameParts, ruleGroupFullName);
	}
}

function extractEntityPartsFromEntityCtx(ctx: ParserRuleContext, parser: GrammarOnly_ConditionParser, pos: number): string[] {
	const result: string[] = [];
	if (ctx.getText().startsWith("/")) {
		// An empty part at the beginning ultimately adds a leading "/"
		result.push("");
	} else if (ctx instanceof EntityContext) {
		// An A12-model entity can contain relative upward references
		result.push(...extractRelUp(ctx, parser, pos))
	}
	// The name parts of the entity are constructed by idNames (except the ones in relativeUp)
	const idNameCtxs: ParserRuleContext[] = []
	extractContexts(ctx,
		c => c instanceof IdNameContext &&
			// the idName in "..idName" has to be omitted
			!(c.parentCtx instanceof RelativeUpContext) &&
			c.start.start <= pos,
		idNameCtxs
	);
	result.push(...idNameCtxs.map(c => c.getText()));
	// A correctly referenced H-Model entity can end with /number when referencing a specific lfdNr and that's why we have to remove this specification.
	if (isHModelEntityWithSpecificLfdNrReference(ctx, parser, result)) {
		result.pop()
	}
	return result;
}

function isHModelEntityWithSpecificLfdNrReference(ctx: ParserRuleContext, parser: GrammarOnly_ConditionParser, result: string[]): boolean {
	if (!(ctx instanceof EntityHModelContext)) {
		return false;
	}
	// We have to take into account that fields in H-Models can never be referenced with a path
	if (result.length === 1) {
		// this is a correct reference to a H-Model entity without specification of LfdNr, e.g. FeldAngegeben(1234567)
		return false;
	}
	// we don't care about the wrongly referenced H-Model entities, e.g. FeldAngegeben(Group/1234567), that's why we don't check further
	return /^\d+$/.test(result[result.length - 1]);
}

function extractContexts(ctx: any, filter: (c: any) => boolean, result: any[]): void {
	if (filter(ctx)) {
		result.push(ctx);
		return;
	}
	if (ctx instanceof ParserRuleContext) {
		for (let i = 0; i < ctx.getChildCount(); i++) {
			extractContexts(ctx.getChild(i), filter, result);
		}
	}
}

function extractRelUp(ctx: ParserRuleContext, parser: GrammarOnly_ConditionParser, pos: number): string[] {
	const relativeUpCtx = ctx.getChild(0);
	if (relativeUpCtx instanceof RelativeUpContext) {
		const upTokens: TerminalNode[] = []
		extractContexts(relativeUpCtx, t => t instanceof TerminalNode && t.symbol.text === ".." && t.symbol.start <= pos, upTokens)
		return upTokens.map(t => t.symbol.text)
	} else {
		return [];
	}
}

/**
 * Create the full name from the entity but keep short names.
 * 
 * @param entity the entity as given in the condition.
 * @param ruleGroupFullName the absolute path of the rule group containing the condition.
 * @returns an absolute path to the entity or a short name.
 */
function createFullName(entityParts: string[], ruleGroupFullName: string): string | undefined {
	// Remove all whitespace inside
	entityParts = entityParts.map(e => normalizePathPart(e));
	if (entityParts.length === 1 && entityParts[0] === "") {
		// We return undefined instead of an empty string ([""].join("/") returns the empty string)
		return undefined;
	}
	// absolute or short name references shouldn't be modified
	if (entityParts[0] === "" || (entityParts.length === 1 && entityParts[0] !== "..")) {
		return entityParts.join("/");
	}
	const ruleGroupFullNameParts = ruleGroupFullName.split("/").map(p => normalizePathPart(p))
	// extract the first relative downward reference
	let firstDown = 0;
	while (firstDown < entityParts.length && entityParts[firstDown].startsWith("..")) {
		firstDown++;
	}
	if (firstDown >= ruleGroupFullNameParts.length) {
		// Cannot go up beyond root
		return undefined;
	}
	// For every .., we remove from the back of the rule path and from the front of entityParts
	const fullEntityParts = ruleGroupFullNameParts.slice(0, ruleGroupFullNameParts.length - firstDown).concat(entityParts.slice(firstDown, entityParts.length))
	// Unescape parts
	return fullEntityParts.join("/");
}

function normalizePathPart(part: string) {
	const partWoSpace = part.replace(/\s/g, "");
	return (partWoSpace.startsWith("'") && partWoSpace.endsWith("'")) ?
		partWoSpace.slice(1, partWoSpace.length - 1) : partWoSpace;
}