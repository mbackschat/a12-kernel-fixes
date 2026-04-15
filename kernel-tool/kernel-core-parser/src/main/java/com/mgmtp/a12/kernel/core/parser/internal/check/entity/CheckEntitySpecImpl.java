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

package com.mgmtp.a12.kernel.core.parser.internal.check.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.parser.internal.ParserService;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityListenUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParserBaseListener;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.EntityIdFormatterUtil;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IUnspecifiedType;

import jakarta.validation.constraints.NotNull;

/**
 *
 */
public class CheckEntitySpecImpl extends CheckEntitySpecLenientImpl {

	FullChecks_ConditionParser fullParser;

	public CheckEntitySpecImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
		this.fullParser = parser;
	}

	@Override
	protected void checkCategory(GrammarOnly_ConditionParser.EntitySpecContext ctx, CheckEntityUtils checker, EntityRef ref,
			String categoryName) {
		super.checkCategory(ctx, checker, ref, categoryName);
		//get the name of the operation,  because the notation is only '[', ']' and we
		//want to have nice error messages.
		String feldWertString = getBareErrorMessage(ParserErrorTypesEnum.MVK_CATEGORY_REF_OP);
		checker.checkIsField(ref, feldWertString, ctx);
		checker.checkIsCorrectCategory(ref, categoryName, ctx);
	}

	@Override
	protected void check(GrammarOnly_ConditionParser.EntitySpecContext ctx, EntityRef ref, CheckEntityUtils checker) {
		super.check(ctx, ref, checker);
		//empty groups are not allowed
		EntityRefUtils.checkNoEmptyGroup(ref.getEntity(), ref.getNameBuiltFromConditionText(), fullParser, ctx);

		if (ctx.filterCond != null) {
			checkFilterCondition(ctx, checker, ref);
		}
	}

	@Override
	protected CheckEntityUtils checkCommon(GrammarOnly_ConditionParser.EntitySpecContext ctx, EntityRef ref) {
		super.checkCommon(ctx, ref);
		CheckEntityUtils checker = new CheckEntityUtils(fullParser);
		if (fullParser.isHModel()) {
			checkUnspecifiedType(ctx, ref);
		}
		checkOuter(ctx);
		if (fullParser.isA12Model() && (ctx.entity() != null || ctx.regelKontextEntity() != null)) {
			if (ctx.outer != null) {
				// for outer, we have to check the error entity with the rule error field
				IEntity errorEntity = fullParser.getErrorEntity();
				fullParser.resetErrorEntity();
				checker.checkIndicesForA12Model(ref, ctx);
				fullParser.setErrorField(errorEntity);
			} else {
				checker.checkIndicesForA12Model(ref, ctx);
			}
		}

		return checker;
	}

	protected void checkOuter(GrammarOnly_ConditionParser.EntitySpecContext ctx) {
		if (ctx.outer == null) {
			return;
		}
		if (fullParser.isHModel()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_OPERATION_IN_H_MODEL, "$");
			throw createMVKParserError(ctx, errMessage);
		}
		if (!fullParser.isInFilterCondition()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_OUTER_ITERATION);
			throw createMVKParserError(ctx, errMessage);
		}
	}

	private void checkFilterCondition(GrammarOnly_ConditionParser.EntitySpecContext ctx, CheckEntityUtils checker, EntityRef ref) {
		ConditionMultiLanguageHandler mlh = new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
				fullParser.getPss());
		String opName = mlh.getTerminalName(Base_ConditionLexer.MIT);
		if (fullParser.isHModel()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_OPERATION_IN_H_MODEL, opName);
			throw createMVKParserError(ctx, errMessage);
		}
		// ATTENTION: If the following statement is relaxed and the entity can be a root group,
		// one has to consider the transformation of NumberOfFilledGroups to NumberOfRootGroupsOperation in AnzahlAngegebenOpCreator.
		// Also the ExpandService may need to be adapted, then.
		checker.checkIsField(ref, opName, ctx);
		checker.checkHasWildCards(ref, opName, ctx);
		new CheckEntityListenUtils(fullParser).checkNoSemanticIndex(Collections.singletonList(ref), ctx, opName);

		checkIndicesInFilterCondition(ref, ctx);
		checkNoDisallowedConstructsInFilterCondition(ctx.filterCond);
		ref.setHasFilterCondition(true);
	}

	/**
	 * Checks that neither ApplikationsBedingung nor RepetitionNotUnique nor the filter operator is
	 * used
	 *
	 * @param filterCond
	 * 		The condition to check.
	 */
	private void checkNoDisallowedConstructsInFilterCondition(final GrammarOnly_ConditionParser.BedingungContext filterCond) {
		ParseTreeWalker walker = new ParseTreeWalker();
		ParseTreeListener entityListener = new GrammarOnly_ConditionParserBaseListener() {
			@Override
			public void enterEntitySpec(GrammarOnly_ConditionParser.EntitySpecContext ctx) {
				if (ctx.filterCond != null) {
					String construct =
							new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
									fullParser.getPss()).getTerminalName(
									Base_ConditionLexer.MIT);
					createError(construct);
				}
			}

			@Override
			public void enterApplikationsBedingung(GrammarOnly_ConditionParser.ApplikationsBedingungContext ctx) {
				String construct = new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
						fullParser.getPss()).getTerminalName(
						Base_ConditionLexer.APPLIKATIONS_BEDINGUNG);
				createError(construct);
			}

			@Override
			public void enterWiederholungBedingung(GrammarOnly_ConditionParser.WiederholungBedingungContext ctx) {
				String construct = new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
						fullParser.getPss()).getTerminalName(
						Base_ConditionLexer.WIEDERHOLUNG_NICHT_EINDEUTIG);
				createError(construct);
			}

			private void createError(String construct) {
				String msg = getErrorMessage(ParserErrorTypesEnum.MVK_DISALLOWED_CONSTRUCT_IN_FILTER_CONDITION, construct);
				throw createMVKParserError(filterCond, msg);
			}
		};
		walker.walk(entityListener, filterCond);
	}

	/**
	 * In filter conditions, for the specified indices of the referenced entities that do not specify rule iteration the
	 * following is checked:
	 * <ul>
	 * <li>iteration is only allowed for groups (incl. root group) on the path of the filtered
	 * entity</li>
	 * <li>for at least one entity in the filter condition, iteration must be specified for a level
	 * for which the filtered entity specifies wildcard</li>
	 * <li>iteration below wildcard is not allowed</li>
	 * <li>semantic index is not allowed</li>
	 * </ul>
	 * In addition, in a filter condition it is not allowed to specify parallel iteration. This is
	 * checked for the filtered entity in {@link ParserService#parseAndCheckCondition}.
	 */
	private void checkIndicesInFilterCondition(EntityRef refFilteredEntity, GrammarOnly_ConditionParser.EntitySpecContext ctx) {
		List<Pair<EntityRef, GrammarOnly_ConditionParser.EntitySpecContext>> refsInFilterCondition = extractFilterIterationEntityRefs(
				ctx.filterCond);
		String filteredEntityFullName = refFilteredEntity.getEntity().getFullName();
		boolean iterationForWildcardSeen = false;
		for (Pair<EntityRef, GrammarOnly_ConditionParser.EntitySpecContext> curEntry : refsInFilterCondition) {
			EntityRef curRef = curEntry.getKey();
			List<Long> curIndices = curRef.getIndices();
			boolean isWildCardSetForRef = false;
			for (int level = 0; level < curIndices.size(); level++) {
				Long index = curIndices.get(level);
				if (index == IParseTreeEntity.ITERATION) {
					String fullPath = curRef.getEntity().getFullName();
					String groupFullName = EntityUtils.getPathUpToLevel(level, fullPath);
					if (!EntityUtils.isDescendantOrEqual(filteredEntityFullName,
							groupFullName)) {//iteration is allowed only for groups on the path of the filtered entity
						String errParam = getErrParamA12Model(curRef, level);
						String msg = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_ITERATION_IN_FILTER_CONDITION, errParam,
								curRef.getNameBuiltFromConditionText());
						throw createMVKParserError(curEntry.getValue(), msg);
					}
					if (isWildCardSetForRef) {//iteration below wildcard -> error
						String errParam = getErrParamA12Model(curRef, level);
						String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_NO_WILDCARD, errParam,
								curRef.getNameBuiltFromConditionText());
						throw createMVKParserError(ctx, errMessage);
					}
					if (refFilteredEntity.getIndices().get(level) == IParseTreeEntity.WILDCARD) {
						iterationForWildcardSeen = true;
					}
				} else if (index == IParseTreeEntity.SEMANTIC_INDEX) {
					String construct = new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
							fullParser.getPss()).getTerminalName(
							Base_ConditionLexer.MIT);
					String msg = getErrorMessage(ParserErrorTypesEnum.MVK_SEMANTIC_INDEX_NOT_ALLOWED, construct);
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
		if (!iterationForWildcardSeen) {
			String msg = getErrorMessage(ParserErrorTypesEnum.MVK_NO_ITERATION_FOR_WILDCARD);
			throw createMVKParserError(ctx, msg);
		}
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

	private List<Pair<EntityRef, GrammarOnly_ConditionParser.EntitySpecContext>> extractFilterIterationEntityRefs(
			GrammarOnly_ConditionParser.BedingungContext condition) {
		final List<Pair<EntityRef, GrammarOnly_ConditionParser.EntitySpecContext>> result = new ArrayList<>();
		ParseTreeWalker walker = new ParseTreeWalker();
		ParseTreeListener entityListener = new GrammarOnly_ConditionParserBaseListener() {
			@Override
			public void enterEntitySpec(GrammarOnly_ConditionParser.EntitySpecContext ctx) {
				if (ctx.outer != null) {
					return;
				}
				EntityRef ref = fullParser.getEntityRef(ctx);
				result.add(Pair.of(ref, ctx));
			}
		};
		walker.walk(entityListener, condition);
		return result;
	}

	private void checkUnspecifiedType(GrammarOnly_ConditionParser.EntitySpecContext ctx, EntityRef ref) {
		if (ref.getEntity() instanceof IField field && field.getType() instanceof IUnspecifiedType) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_UNSPECIFIED_TYPE);
			throw createMVKParserError(ctx, errMessage);
		}
	}

	@Override
	public FullChecks_ConditionParser getParser() {
		return fullParser;
	}
}
