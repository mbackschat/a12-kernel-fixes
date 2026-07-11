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

package com.mgmtp.a12.kernel.core.parser.internal.check.util;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.parser.internal.ParserService;
import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParserBaseListener;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.EntityIdFormatterUtil;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;

public class CheckSetExpressionUtil extends CheckImplAbstractClass {
	public CheckSetExpressionUtil(FullChecks_ConditionParser parser) {
		super(parser);
	}

	/**
	 * In set expressions, i.e. filter conditions or set operations (SumOfTerms), for the specified indices of the referenced
	 * entities that do not specify rule iteration the following is checked:
	 * <ul>
	 * <li>iteration is only allowed for groups (incl. root group) on the path of the iterations entity</li>
	 * <li>for at least one entity in the expression, iteration must be specified for a level for which the iteration-entity specifies wildcard</li>
	 * <li>if <code>lowestLevelMustBeIterated</code>, even the lowest level of the iteration-entity must be iterated over in the expression</li>
	 * <li>iteration below wildcard is not allowed</li>
	 * <li>semantic index is not allowed</li>
	 * </ul>
	 * In addition, in a set expression it is not allowed to specify parallel iteration. This is
	 * checked for the set expression in {@link ParserService#parseAndCheckCondition}.
	 */
	public void checkIndicesInSetExpression(EntityRef refPathWithAsterisk, ParserRuleContext expressionCtx, String opName,
			boolean lowestLevelMustBeIterated) {
		List<Pair<EntityRef, EntitySpecContext>> refsInSetExpression = extractSetExpressionReferencedEntities(expressionCtx);
		String pathWithAsterisks = refPathWithAsterisk.getEntity().getFullName();
		int levelOfLowestWildcard = refPathWithAsterisk.getIndices().lastIndexOf(IParseTreeEntity.WILDCARD);
		assert refPathWithAsterisk.getEntity().isField() || levelOfLowestWildcard == refPathWithAsterisk.getIndices().size() - 1;
		boolean iterationForWildcardSeen = false;
		boolean iterationForLowestLevelSeen = false;
		for (Pair<EntityRef, EntitySpecContext> curEntry : refsInSetExpression) {
			EntityRef curRef = curEntry.getKey();
			List<Long> curIndices = curRef.getIndices();
			boolean isWildCardSetForRef = false;
			for (int level = 0; level < curIndices.size(); level++) {
				Long index = curIndices.get(level);
				if (index == IParseTreeEntity.ITERATION) {
					String fullPath = curRef.getEntity().getFullName();
					String groupFullName = EntityUtils.getPathUpToLevel(level, fullPath);
					if (!EntityUtils.isDescendantOrEqual(pathWithAsterisks,
							groupFullName)) {//iteration is allowed only for groups on the path of the filtered entity
						String errParam = getErrParamA12Model(curRef, level);
						String msg = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_ITERATION_IN_SET_EXPRESSION, errParam,
								curRef.getNameBuiltFromConditionText(), opName);
						throw createMVKParserError(curEntry.getValue(), msg);
					}
					if (level == levelOfLowestWildcard) {
						iterationForLowestLevelSeen = true;
					}
					if (isWildCardSetForRef) {//iteration below wildcard -> error
						String errParam = getErrParamA12Model(curRef, level);
						String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_NO_WILDCARD, errParam,
								curRef.getNameBuiltFromConditionText());
						throw createMVKParserError(expressionCtx, errMessage);
					}
					if (refPathWithAsterisk.getIndices().get(level) == IParseTreeEntity.WILDCARD) {
						iterationForWildcardSeen = true;
					}
				} else if (index == IParseTreeEntity.SEMANTIC_INDEX) {
					String msg = getErrorMessage(ParserErrorTypesEnum.MVK_SEMANTIC_INDEX_NOT_ALLOWED, opName);
					throw createMVKParserError(curEntry.getValue(), msg);
				} else if (index == IParseTreeEntity.WILDCARD) {
					isWildCardSetForRef = true;
				}
				/*
				 * IParseTreeEntity#PARALLEL_ITERATION is not possible as the indices for parallel
				 * iteration are set after complete parsing of the condition
				 */
			}
		}
		if (lowestLevelMustBeIterated && !iterationForLowestLevelSeen) {
			String msg = getErrorMessage(ParserErrorTypesEnum.MVK_NO_ITERATION_FOR_LOWEST_WILDCARD, opName);
			throw createMVKParserError(expressionCtx, msg);
		}
		if (!iterationForWildcardSeen) {
			String msg = getErrorMessage(ParserErrorTypesEnum.MVK_NO_ITERATION_FOR_WILDCARD);
			throw createMVKParserError(expressionCtx, msg);
		}
	}

	/**
	 * In setExpressions, the internal iteration indices are first created with {@link IParseTreeEntity#ITERATION}, but they should
	 * be marked as inner iteration with {@link IParseTreeEntity#INTERNAL_ITERATION}. This change is done here.
	 * Inner iteration indices are these, that iterate over the wildcards of the starred path.
	 * <p>
	 * This method assumes that all checks in {@link #checkIndicesInSetExpression(EntityRef, ParserRuleContext, String, boolean)}
	 * are successfully performed already.
	 */
	public void adaptIndicesInSetExpression(EntityRef refPathWithWildcards, ParserRuleContext expressionCtx) {
		List<Pair<EntityRef, EntitySpecContext>> refsInSetExpression = extractSetExpressionReferencedEntities(expressionCtx);
		for (Pair<EntityRef, EntitySpecContext> curEntry : refsInSetExpression) {
			EntityRef curRef = curEntry.getKey();
			List<Long> curIndices = curRef.getIndices();
			for (int level = 0; level < curIndices.size(); level++) {
				Long index = curIndices.get(level);
				if (index == IParseTreeEntity.ITERATION &&
						refPathWithWildcards.getIndices().get(level) == IParseTreeEntity.WILDCARD) {
					curRef.setIndex(level, IParseTreeEntity.INTERNAL_ITERATION);
				}
			}
		}
	}

	private List<Pair<EntityRef, EntitySpecContext>> extractSetExpressionReferencedEntities(ParseTree expression) {
		final List<Pair<EntityRef, EntitySpecContext>> result = new ArrayList<>();
		ParseTreeWalker walker = new ParseTreeWalker();
		ParseTreeListener entityListener = new GrammarOnly_ConditionParserBaseListener() {
			@Override
			public void enterEntitySpec(EntitySpecContext ctx) {
				if (ctx.outer != null) {
					return;
				}
				EntityRef ref = fullParser.getEntityRef(ctx);
				result.add(Pair.of(ref, ctx));
			}
		};
		walker.walk(entityListener, expression);
		return result;
	}

	private String getErrParamA12Model(EntityRef ref, int level) {
		String entityFullname = ref.getEntity().getFullName();
		String ruleFullname = getRule().getFullName();
		String groupFullName = EntityUtils.getPathUpToLevel(level, entityFullname);
		ConditionMultiLanguageHandler mlh = new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
				fullParser.getPss());
		return EntityIdFormatterUtil.formatEntityForCondationParserMessageA12DV(ruleFullname, groupFullName,
				ref.getIndices(), ref.getPathNotationConfig(), mlh);
	}

	/**
	 * Checks that neither ApplikationsBedingung nor RepetitionNotUnique nor the filter operator nor SumOfTerms is used.
	 *
	 * @param setExpression
	 * 		The condition to check.
	 */
	public void checkNoDisallowedConstructsInSetExpression(final ParserRuleContext setExpression,
			String opName) {
		ParseTreeWalker walker = new ParseTreeWalker();
		ParseTreeListener entityListener = new GrammarOnly_ConditionParserBaseListener() {
			@Override
			public void enterEntitySpec(GrammarOnly_ConditionParser.EntitySpecContext ctx) {
				if (ctx.filterCond != null) {
					String construct =
							new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
									fullParser.getPss()).getTerminalName(Base_ConditionLexer.MIT);
					createError(construct);
				}
			}

			@Override
			public void enterApplikationsBedingung(GrammarOnly_ConditionParser.ApplikationsBedingungContext ctx) {
				String construct = new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
						fullParser.getPss()).getTerminalName(Base_ConditionLexer.APPLIKATIONS_BEDINGUNG);
				createError(construct);
			}

			@Override
			public void enterWiederholungBedingung(GrammarOnly_ConditionParser.WiederholungBedingungContext ctx) {
				String construct = new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
						fullParser.getPss()).getTerminalName(Base_ConditionLexer.WIEDERHOLUNG_NICHT_EINDEUTIG);
				createError(construct);
			}

			@Override public void enterSetOperOp(GrammarOnly_ConditionParser.SetOperOpContext ctx) {
				String construct = new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
						fullParser.getPss()).getTerminalName(Base_ConditionLexer.SUMME_VON_TERMEN);
				createError(construct);
			}

			private void createError(String construct) {
				String msg = getErrorMessage(ParserErrorTypesEnum.MVK_DISALLOWED_CONSTRUCT_IN_SET_EXPRESSION, construct, opName);
				throw createMVKParserError(setExpression, msg);
			}
		};
		walker.walk(entityListener, setExpression);
	}
}
