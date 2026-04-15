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

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParserBaseListener;

import lombok.Getter;

/**
 * find all entities references (all cases of entitySpec) and get their fullname (+ maybe additional info, if relevant for
 * MoveSupportInternal)
 */
public class MoveSupportErrorTextParseTreeListener extends GrammarOnly_ErrorTextParameterParserBaseListener {
	private final String ruleGroupFullname;
	@Getter private final List<MoveSupportParseTreeListener.EntityReference> collectedReferences = new ArrayList<>();

	public MoveSupportErrorTextParseTreeListener(String ruleGroupFullname) {
		this.ruleGroupFullname = ruleGroupFullname;
	}

	@Override public void enterRegelKontextEntity(GrammarOnly_ErrorTextParameterParser.RegelKontextEntityContext ctx) {
		MoveSupportParseTreeListener.enterRegelKontextEntity(ctx, this.ruleGroupFullname, this.collectedReferences);
	}

	@Override public void enterEntity(GrammarOnly_ErrorTextParameterParser.EntityContext ctx) {
		// NOTE: this method is textually near-identical to MoveSupportParseTreeListener.enterEntity
		boolean isAbsolute = ctx.slash != null;
		List<GrammarOnly_ErrorTextParameterParser.IdNameRefContext> idNameRefs = ctx.idNameRef();
		boolean starred = false; // paths in error text params are never starred
		var downwardPathPartsWithStar = Java8Utils.mapList(idNameRefs,
				inr -> new MoveSupportParseTreeListener.PathPartWithStar(MoveSupportParseTreeListener.idNameRef2PathPart(inr),
						starred));
		int relUpLevels = getRelUpLevels(ctx);

		Token startToken = ctx.start;
		Token stopToken = ctx.stop;
		if (!isIndexFieldContext(ctx)) {//index fields are not handled, because we only allow short references for index fields
			this.collectedReferences.add(MoveSupportParseTreeListener.makeEntityReference(
					isAbsolute,
					hasExplicitTurningPoint(ctx),
					this.ruleGroupFullname,
					downwardPathPartsWithStar,
					relUpLevels,
					startToken, stopToken));
		}
	}

	/**
	 * @return whether the parent.parent is an IndexFieldContext, i.e. it was specified with 'index(<entitySpec>)' or similar.
	 */
	private boolean isIndexFieldContext(GrammarOnly_ErrorTextParameterParser.EntityContext ctx) {
		return ctx.getParent() != null && ctx.getParent().getParent()
				instanceof GrammarOnly_ErrorTextParameterParser.IndexFieldContext;
	}

	/**
	 * Note: this method should be kept analog to {@link MoveSupportParseTreeListener#hasExplicitTurningPoint}
	 */
	static boolean hasExplicitTurningPoint(GrammarOnly_ErrorTextParameterParser.EntityContext ctx) {
		return ctx.relativeUp() != null && (ctx.relativeUp().idName() != null);
	}

	/**
	 * Note: this method should be kept analog to {@link MoveSupportParseTreeListener#getRelUpLevels}
	 */
	static int getRelUpLevels(GrammarOnly_ErrorTextParameterParser.EntityContext ctx) {
		int relUpLevels = 0;
		if (ctx.relativeUp() != null) {
			relUpLevels += 1; // one '..' is directly in relativeUp in the grammar
			if (ctx.relativeUp().relUp() != null) {
				relUpLevels += ctx.relativeUp().relUp().oneUp().size();
			}
		}
		return relUpLevels;
	}

	@Override public void enterEntityHModel(GrammarOnly_ErrorTextParameterParser.EntityHModelContext ctx) {
		throw new IllegalArgumentException("Not allowed for H-Models. Reliably detecting referenced entities in H-Models would " +
				"require an IEC or similar to distinguish fields from '/2' repetition specifiers");
	}
}
