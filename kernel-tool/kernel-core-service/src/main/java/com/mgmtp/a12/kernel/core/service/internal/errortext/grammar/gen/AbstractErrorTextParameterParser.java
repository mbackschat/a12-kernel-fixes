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

package com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen;

import java.util.function.Predicate;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.FieldContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.GroupIndexContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.IdNameContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.IndexFieldContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.LfdNrContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.RelUpContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.RelativeUpContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.SemanticIndexContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.VordrucknameContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.VordruckzeileContext;

/**
 * This is the parent class for the antlr-generated class {@link GrammarOnly_ErrorTextParameterParser}. It declares all methods
 * that are referenced in the corresponding grammar file, with a minimal implementation for those callback methods. The full
 * implementation of the methods is in the subclass {@link FullChecks_ErrorTextParameterParser}.
 */
public abstract class AbstractErrorTextParameterParser extends Parser {
	private boolean isHModel;
	private Predicate<ParserRuleContext> isGroupFromHModelCallback;

	protected AbstractErrorTextParameterParser(TokenStream input) {
		super(input);
	}

	/**
	 * @param isHModel
	 * @param isGroupFromHModelCallback
	 * 		required if isHModel == true: a function/method that takes the parser's current context
	 * 		(from {@link Parser#getContext()}) and returns true if the corresponding entitySpec refers to a group (as opposed to a
	 * 		field) in the DVL. This is required because in H-DVLs, a path like /RG/f/1 would be ambiguous - the "/1" suffix couuld be
	 * 		a field name or a repetition index.
	 */
	public void initParser(boolean isHModel, Predicate<ParserRuleContext> isGroupFromHModelCallback) {
		this.isHModel = isHModel;
		if (isHModel && isGroupFromHModelCallback == null) {
			throw new IllegalArgumentException("for H-Models, the parser needs to be able to tell if a path belongs to a group or" +
					" field. (otherwise, a reference like /RG/G/f/1 would be ambiguous");
		}
		this.isGroupFromHModelCallback = isGroupFromHModelCallback;
	}

	protected boolean isA12Model() {
		return !isHModel;
	}

	protected boolean isHModel() {
		return isHModel;
	}

	protected boolean isGroupFromHModel() {
		if (!isHModel) {
			return false;
		} else {
			return this.isGroupFromHModelCallback.test(getContext());
		}
	}

	protected void checkFieldRef(FieldContext ctx) {
	}

	protected void checkIndexFieldRef(IndexFieldContext ctx) {
	}

	protected void checkVordruckzeile(VordruckzeileContext ctx) {
	}

	protected void checkVordruckname(VordrucknameContext ctx) {
	}

	protected void checkLevelParam(GroupIndexContext ctx) {
	}

	protected void checkReferenzjahr() {
	}

	protected void checkZeile() {
	}

	protected void checkUSB() {
	}

	protected void checkFehlerEnum(LfdNrContext ctx) {
	}

	protected void checkSemanticIndex(SemanticIndexContext ctx) {
	}

	protected void checkIdName(IdNameContext ctx) {
	}

	protected void checkRelativeUp(RelativeUpContext context) {
	}

	protected void checkRelUp(RelUpContext context) {
	}
}
