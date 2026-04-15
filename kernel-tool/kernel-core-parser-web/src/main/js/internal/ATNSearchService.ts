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
import type {
	ATNState,
	BufferedTokenStream,
	Parser,
	ParserRuleContext,
	Transition
} from "antlr4ForA12Kernel";
import {
	IntervalSet,
	NotSetTransition,
	RuleTransition,
	Token,
	WildcardTransition
} from "antlr4ForA12Kernel";

export type RuleCallback = (startToken: Token, lastToken: Token, nextToken: Token, openRules: RuleCtx[]) => boolean;
export type RuleCallbacks = Map<number, RuleCallback>;

export type RuleCtx = {
	// the rule number
	rule: number;
	// the follow state for the rule
	followState: number;
	// the start position of the rule
	startToken: Token;
};

type LookState = {
	// The parser the atn is used from
	parser: Parser;
	// the current ctx of the rule
	ctx: ParserRuleContext;
	// the resulting interval set
	result: IntervalSet;
	// the set of states currently visited
	// Used to prevent circles
	lookBusy: Set<number>;
	// The current search rule stack. Can also contain closed rules.
	calledRuleStack: RuleCtx[];
	// The current stack of open rules.
	openRuleStack: number[];
	// The input stream
	input: BufferedTokenStream;
	// The position in the input stream (1 is first symbol)
	inputPos: number;
	// The position at which to search for allowed symbols
	relevantPos: number;
	// Specifies if the relevant pos has been reached.
	// If reached, we do a lookahead, otherwise we search for the given input.
	isSearch: boolean;
	// Callbacks for rule transitions
	ruleCallbacks: RuleCallbacks;
};

type transitionEndRule = [transitions: Transition[], endedRule: number | undefined];

export class ATNSearchService {
	constructor(public parser: Parser) { }

	/**
	 * Update the input position and calculate new look state.
	 *
	 * @param lookState the old state.
	 * @returns the new look state.
	 */
	private advanceInput(lookState: LookState): LookState | undefined {
		const lastToken = lookState.input.LT(lookState.inputPos);
		const nextToken = lookState.input.LT(lookState.inputPos + 1);
		const isSearch = nextToken.stop < lookState.relevantPos && nextToken.type !== -1;
		if (!isSearch) {
			const openRules = lookState.openRuleStack.filter(r =>
				lookState.ruleCallbacks.has(lookState.calledRuleStack[r].rule)
			);
			let result = true;
			for (const openRule of openRules) {
				const rule = lookState.calledRuleStack[openRule];
				result =
					result &&
					// TODO: Fix this the next time the file is edited.
					// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
					lookState.ruleCallbacks.get(rule.rule)!(
						rule.startToken,
						lastToken,
						nextToken,
						lookState.openRuleStack.map(r => lookState.calledRuleStack[r])
					);
			}
			if (!result) {
				return undefined;
			}
		}
		const newLookState: LookState = {
			...lookState,
			// Eating a token breaks circles
			lookBusy: new Set(),
			inputPos: lookState.inputPos + 1,
			isSearch: isSearch
		};
		return newLookState;
	}

	/**
	 * Handle a RuleTransition jumping from one rule to another.
	 *
	 * @param transition the RuleTransition.
	 * @param lookState the current state.
	 * @returns whether we should continue searching. A callback could say "stop".
	 */
	private handleRuleTransition(transition: Transition, lookState: LookState): void {
		const curToken = lookState.input.LT(lookState.inputPos);
		const nextToken = lookState.input.LT(lookState.inputPos + 1);
		if (!lookState.isSearch && lookState.ruleCallbacks.has(transition.target.ruleIndex)) {
			// Call the callback if there is one connected with the rule.
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			const cont = lookState.ruleCallbacks.get(transition.target.ruleIndex)!(
				curToken,
				curToken,
				nextToken,
				lookState.openRuleStack.map(r => lookState.calledRuleStack[r])
			);
			if (!cont) {
				return;
			}
		}
		// We jump to a new rule, which is automatically open.
		lookState.openRuleStack.push(lookState.calledRuleStack.length);
		lookState.calledRuleStack.push({
			rule: transition.target.ruleIndex,
			followState: (<any>transition).followState.stateNumber,
			startToken: curToken
		});
		this._searchInternal(transition.target, lookState);
		// clear the rule stack when backtracking
		lookState.calledRuleStack.pop();
		const ruleIdx = lookState.openRuleStack.pop();
		if (ruleIdx !== lookState.calledRuleStack.length) {
			// The current open rule must always be the current one overall
			throw new Error("Rules got messed up!");
		}
	}

	/**
	 * Handle a predicate transition.
	 * There are two different types of predicates (see antlr4/atn/SemanticContext.js):
	 * <ul>
	 * <li>Predicate: Semantic predicates given by the user (like isExtmp()) and</li>
	 * <li>PrecedencePredicate: For operator precedence handling</li>
	 * </ul>
	 *
	 * The first one can be executed by the parser code. The second one only specifies the
	 * ordering of the parse tree. For suggestions, this ordering is irrelevant and will be ignored.
	 * Therefore, for precedence predicates, we always continue the search.
	 *
	 * @param transition the transition.
	 * @param lookState the current state.
	 */
	private handlePredicate(transition: Transition, lookState: LookState) {
		const pred: any = (<any>transition).getPredicate();
		if (
			(pred.ruleIndex !== undefined && // Predicate
				// @ts-expect-error sempred existis on recognizer, but not in typingss TODO cleaner solution
				lookState.parser.sempred(lookState.ctx, pred.ruleIndex, pred.predIndex)) ||
			pred.precedence !== undefined
		) {
			// PrecedencePredicate
			this._searchInternal(transition.target, lookState);
		}
	}

	/**
	 * Handle a wildcard transition (equivalent to regexp ".").
	 *
	 * @param transition the transition.
	 * @param lookState the current state.
	 */
	private handleWildcard(transition: Transition, lookState: LookState): void {
		if (lookState.isSearch) {
			const newLookState: LookState | undefined = this.advanceInput(lookState);
			if (newLookState !== undefined) {
				this._searchInternal(transition.target, newLookState);
			}
		} else {
			lookState.result.addRange(
				Token.MIN_USER_TOKEN_TYPE,
				(<any>lookState.parser._interp.atn).maxTokenType
			);
		}
	}

	/**
	 * Handle a token set transition.
	 *
	 * @param t the transition.
	 * @param lookState the current state.
	 */
	private handleTokenSet(t: Transition, lookState: LookState): void {
		let set: IntervalSet = (<any>t).label;
		if (set === null) {
			// If we have no tokenset, we can do nothing but backtrack.
			return;
		}
		if (t instanceof NotSetTransition) {
			set = set.complement(
				Token.MIN_USER_TOKEN_TYPE,
				(<any>lookState.parser._interp.atn).maxTokenType
			);
		}
		if (lookState.isSearch) {
			// In a search, we check the current input token against the token set
			if (set.contains(lookState.input.LA(lookState.inputPos))) {
				const newLookState: LookState | undefined = this.advanceInput(lookState);
				if (newLookState !== undefined) {
					this._searchInternal(t.target, newLookState);
				}
			}
		} else {
			// In case of look ahead, we add all available tokens.
			lookState.result.addSet(set);
		}
	}

	/**
	 * Calculate the available transitions.
	 *
	 * Only in case of a RuleStopState, we have to filter the transitions for the follow state given
	 * when entering the rule.
	 *
	 * @param s the RuleStopState
	 * @param lookState the state of the search
	 * @returns the available transitions and the ended rule, if any.
	 */
	private availableTransitions(s: ATNState, lookState: LookState): transitionEndRule {
		if (s.stateType !== 7) {
			// 7 is RuleStopState
			// If we do not end a rule, everything is normal
			return [s.transitions, undefined];
		}
		if (lookState.openRuleStack.length === 0) {
			// If this is a rulecontext outside our entering, we can do nothing but backtrack
			// Should not happen any more if only called from ANTSearchService.
			return [[], undefined];
		}

		// if a rule ends, it is not open any more...
		const endedRuleIdx = lookState.openRuleStack[lookState.openRuleStack.length - 1];
		const endedRule: RuleCtx = lookState.calledRuleStack[endedRuleIdx];
		if (s.ruleIndex !== endedRule.rule) {
			throw new Error("Rule " + s.ruleIndex + " ended with " + endedRule.rule);
		}
		// ... and we backtrack to the followState given during rule entering.
		const transitionResult = s.transitions.filter(
			t => t.target.stateNumber === endedRule.followState
		);
		return [transitionResult, endedRuleIdx];
	}

	/**
	 * Code adapted from antlr4/LL1Analyzer.js _LOOK.
	 */
	private _searchInternal(s: ATNState, lookState: LookState): void {
		if (!lookState.isSearch && lookState.lookBusy.has(s.stateNumber)) {
			return;
		}
		lookState.lookBusy.add(s.stateNumber);
		const [transitions, endedRule] = this.availableTransitions(s, lookState);
		if (endedRule !== undefined) {
			// If a rule ends, it is not open any more
			lookState.openRuleStack.pop();
		}
		for (const t of transitions) {
			if (t instanceof RuleTransition) {
				this.handleRuleTransition(t, lookState);
			} else if ("getPredicate" in t) {
				this.handlePredicate(t, lookState);
			} else if ((<any>t).isEpsilon) {
				// Transition without token change -> fast forward
				this._searchInternal(t.target, lookState);
			} else if (t instanceof WildcardTransition) {
				this.handleWildcard(t, lookState);
			} else {
				this.handleTokenSet(t, lookState);
			}
		}
		if (endedRule !== undefined) {
			// When backtracking a RuleEndState we get back into the rule
			lookState.openRuleStack.push(endedRule);
		}
	}

	/**
	 * Code adapted from antlr4/LL1Analyzer.js.
	 *
	 * Traverse the ATN of the parser, search the remaining tokens in the input stream until
	 * relevant position is reached and return all possible tokens usable instead of the symbol at
	 * the relevant position.
	 *
	 * RuleCallbacks can be used to setup special behavior during rule change.
	 * E.g. when entering the rule "entitySpec", a special result is added and the search backtracks.
	 *
	 * @param parser the parser to traverse.
	 * @param relevantPos the position to search replacements for.
	 * @param ruleCallbacks the rule callbacks for special rule behavior.
	 * @returns an IntervalSet containing all possible replacement tokens.
	 */
	public search(parser: Parser, relevantPos: number, ruleCallbacks: RuleCallbacks): IntervalSet {
		const r = new IntervalSet();
		const ctx: ParserRuleContext | null = parser._ctx || null;
		const s: ATNState = parser._interp.atn.states[parser.state];
		const input: BufferedTokenStream = parser.getTokenStream() as BufferedTokenStream; // i think we always get a bufferedtokenstream (TokenStream.js contains an empty class body), but the typings don't declare that
		const token: Token = input.LT(1);
		const lookState: LookState = {
			parser: parser,
			ctx: ctx,
			result: r,
			lookBusy: new Set(),
			calledRuleStack: [],
			openRuleStack: [],
			input: input,
			inputPos: 1,
			isSearch: token.type !== -1 && token.stop < relevantPos,
			relevantPos: relevantPos,
			ruleCallbacks: ruleCallbacks
		};
		this._searchInternal(s, lookState);
		return r;
	}
}
