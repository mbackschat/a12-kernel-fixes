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

package com.mgmtp.a12.kernel.core.service.internal;

import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.prefixes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.NotImplementedException;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParserBaseListener;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser;

/**
 * find all entities references (all cases of entitySpec) and get their fullname (+ maybe additional info, if relevant for
 * MoveSupportInternal)
 */
public class MoveSupportParseTreeListener extends GrammarOnly_ConditionParserBaseListener {
	private final String ruleGroupFullname;
	private final List<EntityReference> collectedReferences = new ArrayList<>();

	public MoveSupportParseTreeListener(String ruleGroupFullname) {
		this.ruleGroupFullname = ruleGroupFullname;
	}

	public List<EntityReference> getCollectedReferences() {
		return collectedReferences;
	}

	@Override public void enterRegelKontextEntity(GrammarOnly_ConditionParser.RegelKontextEntityContext ctx) {
		enterRegelKontextEntity(ctx, this.ruleGroupFullname, this.collectedReferences);
	}

	public static void enterRegelKontextEntity(
			ParserRuleContext ctx,
			String ruleGroupFullname,
			List<EntityReference> collectedReferences
	) {
		collectedReferences.add(new EntityReference(
				ruleGroupFullname,
				new HashSet<>(),
				false,
				false, // use "false" as default for explicit turning point: arbitrary, but the autocomplete
				// suggestions in the condition editor use this notation by default too
				new Range(ctx.start.getStartIndex(), ctx.start.getStopIndex() + 1)));
	}

	@Override public void enterEntity(GrammarOnly_ConditionParser.EntityContext ctx) {
		// NOTE: this method is textually near-identical to MoveSupportErrorTextParseTreeListener.enterEntity
		boolean isAbsolute = ctx.slash != null;
		List<GrammarOnly_ConditionParser.IdNameRefContext> idNameRefs = ctx.idNameRef();
		var downwardPathPartsWithStar = Java8Utils.mapList(idNameRefs,
				inr -> new PathPartWithStar(MoveSupportParseTreeListener.idNameRef2PathPart(inr), inr.stern != null));
		int relUpLevels = getRelUpLevels(ctx);
		Token startToken = ctx.start;
		Token stopToken = ctx.stop;
		assert stopToken != null;

		this.collectedReferences.add(MoveSupportParseTreeListener.makeEntityReference(
				isAbsolute,
				hasExplicitTurningPoint(ctx),
				this.ruleGroupFullname,
				downwardPathPartsWithStar,
				relUpLevels,
				startToken, stopToken));
	}

	public record PathPartWithStar(String pathPart, boolean hasStar) {
	}

	static EntityReference makeEntityReference( // not sure if this is better than two fully independent impls for
			// conditions and errortext parameters
			boolean isAbsolute,
			boolean hasExplicitTurningPoint,
			String ruleGroupFullname,
			List<PathPartWithStar> downwardPathPartsWithStar,
			int relUpLevels,
			Token startToken, Token stopToken
	) {
		var ruleGroupPathParts = Arrays.asList(EntityUtils.getPathParts(ruleGroupFullname));
		List<PathPartWithStar> fullPathPartsWithStar = isAbsolute
				? downwardPathPartsWithStar
				: Stream.concat(
				ruleGroupPathParts.subList(0, ruleGroupPathParts.size() - relUpLevels).stream()
						.map(pathPart -> new PathPartWithStar(pathPart, false)),
				downwardPathPartsWithStar.stream()
		).toList();
		Set<String> starredGroupFullNames = prefixes(fullPathPartsWithStar).stream()
				.filter(p -> !p.isEmpty() && p.getLast().hasStar())
				.map(MoveSupportParseTreeListener::pathPartsWithStarToAbspath)
				.collect(Collectors.toSet());
		return new EntityReference(
				pathPartsWithStarToAbspath(fullPathPartsWithStar),
				starredGroupFullNames,
				isAbsolute,
				hasExplicitTurningPoint,
				new Range(startToken.getStartIndex(), stopToken.getStopIndex() + 1)
		);
	}

	/**
	 * Note: this method should be kept analog to {@link MoveSupportErrorTextParseTreeListener#hasExplicitTurningPoint}
	 */
	static boolean hasExplicitTurningPoint(GrammarOnly_ConditionParser.EntityContext ctx) {
		return ctx.relativeUp() != null && (ctx.relativeUp().idName() != null);
	}

	/**
	 * Note: this method should be kept analog to {@link MoveSupportErrorTextParseTreeListener#getRelUpLevels}
	 */
	static int getRelUpLevels(GrammarOnly_ConditionParser.EntityContext ctx) {
		int relUpLevels = 0;
		if (ctx.relativeUp() != null) {
			relUpLevels += 1; // one '..' is directly in relativeUp in the grammar
			if (ctx.relativeUp().relUp() != null) {
				relUpLevels += ctx.relativeUp().relUp().oneUp().size();
			}
		}
		return relUpLevels;
	}

	private static String pathPartsWithStarToAbspath(List<PathPartWithStar> pathPartWithStars) {
		return ElementPathUtils.toAbsPathString(pathPartWithStars.stream().map(PathPartWithStar::pathPart).toList());
	}

	static String idNameRef2PathPart(GrammarOnly_ConditionParser.IdNameRefContext idNameRef) {
		return idNameRef.idName().getText();
	}

	public static String idNameRef2PathPart(GrammarOnly_ErrorTextParameterParser.IdNameRefContext idNameRef) {
		return idNameRef.idName().getText();
	}

	public record EntityReference(
			String fullName,
			// contains all ancestor groups of the entity, that are starred
			Set<String> starredGroupFullNames,
			boolean isAbsolute,
			boolean hasExplicitTurningPoint,
			Range rangeInCondition
	) {	}

	public record Range(int start, int endExclusive) {
	}

	@Override public void enterEntityHModel(GrammarOnly_ConditionParser.EntityHModelContext ctx) {
		throw new NotImplementedException("H-Models are not supported in MoveSupportParseTreeListener");
	}
}
