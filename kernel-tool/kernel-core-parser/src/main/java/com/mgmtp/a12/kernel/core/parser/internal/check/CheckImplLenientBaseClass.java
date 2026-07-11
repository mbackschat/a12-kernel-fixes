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

package com.mgmtp.a12.kernel.core.parser.internal.check;

import java.util.function.Predicate;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserError;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;

public class CheckImplLenientBaseClass {

	public static final int MAX_ALLOWED_DIGITS = 15;

	// direct access to the parser
	protected final Lenient_ConditionParser parser;

	public CheckImplLenientBaseClass(Lenient_ConditionParser parser) {
		this.parser = parser;
	}

	public Lenient_ConditionParser getParser() {
		return parser;
	}

	public IRule getRule() {
		return parser.getRule();
	}

	//helper method to get readable code.
	public IEntity getErrorEntity() {
		return parser.getErrorEntity();
	}

	public String getBareErrorMessage(ParserErrorTypesEnum errorType) {
		return parser.getMVKErrorHandler().getBareErrorMessage(errorType);
	}

	public String getErrorMessage(ParserErrorTypesEnum errorType, Object... args) {
		return parser.getMVKErrorHandler().getParserErrorMessage(errorType, args);
	}

	public String getErrorMessage(ParserErrorTypesEnumForHModel errorType, Object... args) {
		return parser.getMVKErrorHandler().getParserErrorMessage(errorType, args);
	}

	public MVKParserError createMVKParserError(Token start, Token end, String msg) {
		return new MVKParserError(start, end, getRule(), msg);
	}

	public MVKParserError createMVKParserError(Token t, String msg) {
		return new MVKParserError(t, t, getRule(), msg);
	}

	public MVKParserError createMVKParserError(ParserRuleContext ctx, String msg) {
		return new MVKParserError(ctx.start, ctx.start, getRule(), msg);
	}

	//helper method to provide a short way to get an entity from the given entity name
	protected IEntity getEntity(String entityName) {
		return parser.getEc().get(entityName);
	}

	protected EntityRef getEntityRef(GrammarOnly_ConditionParser.EntitySpecContext ctx) {
		return parser.getEntityRef(ctx);
	}

	protected EntityRef getEntityRef(GrammarOnly_ConditionParser.FormularSpecContext ctx) {
		return parser.getEntityRef(ctx);
	}

	public void throwUnknownTokenError(Token t) {
		String mess = String.format("internal error: unknown terminal '%s'", t.getText());
		throw new IllegalArgumentException(mess);
	}

	public void moveToThisContext(ParserRuleContext ctx) {
		parser.getOperationResultTypeMap().moveToThisContext(ctx);
	}

	protected GrammarOnly_ConditionParser.EntitySpecContext getEnclosingEntitySpecContext(ParserRuleContext parent) {
		return (GrammarOnly_ConditionParser.EntitySpecContext) findAncestorContext(parent,
				c -> c instanceof GrammarOnly_ConditionParser.EntitySpecContext);
	}

	/**
	 * Go up the parse tree and check all the RuleContexts on the path, if it matches the predicate.
	 *
	 * @param currentContext
	 * 		the context, whose ancestors and itself is checked against the predicate.
	 * @param contextPredicate
	 * 		the predicate applied for the currentContext and all its ancestors.
	 * @return the first <code>RuleContext</code> that matches the predicate on the path up or <code>null</code>
	 */
	protected RuleContext findAncestorContext(RuleContext currentContext, Predicate<RuleContext> contextPredicate) {
		while (currentContext != null && !contextPredicate.test(currentContext)) {
			currentContext = currentContext.getParent();
		}
		return currentContext;
	}
}
