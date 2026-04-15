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

package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung;

import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DateValidityConditionContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.PredefinedTypeValidityConditionContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ValidityConditionNameContext;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.ParseTreeCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.util.CreatorUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.util.FeldIdCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.DateValidityConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.PredefinedTypeValidityConditionImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ValidityConditionName;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

/**
 * Creates the parse tree objects for the grammar rules
 *
 * <p>
 * <code>dateValidityCondition: validityConditionName '(' date=datumKonstrukt ')';</code>
 * </p>
 * and
 * <p>
 * <code>predefinedTypeValidityCondition: validityConditionName '(' feld=entitySpec ','
 * predefinedType=StringLiteral ')';</code>
 * </p>
 * where
 * <p>
 * <code>validityConditionName: 'Gueltig' | 'Ungueltig';</code>
 * </p>
 */
public class ValidityConditionCreator {

	private ValidityConditionCreator() {
		throw new IllegalStateException("Utility class");
	}

	public static void create(DateValidityConditionContext ctx, ParseTreeCreator tc) {
		ValidityConditionName opName = extractOperationNameFromContext(ctx.validityConditionName());
		tc.push(new DateValidityConditionImpl(tc.popFop(), opName));
	}

	public static void create(PredefinedTypeValidityConditionContext ctx, ParseTreeCreator tc) {
		ValidityConditionName opName = extractOperationNameFromContext(ctx.validityConditionName());
		IEntityId field = FeldIdCreator.create(ctx.feld, tc);
		String predefinedTypeName = CommonUtils.removeAndUnescapeQuotesAndRemoveCrFromCrLf(ctx.predefinedType.getText());
		tc.push(new PredefinedTypeValidityConditionImpl(field, opName, predefinedTypeName));
	}

	private static ValidityConditionName extractOperationNameFromContext(ValidityConditionNameContext ctx) {
		Token t = ctx.start;
		int type = t.getType();
		switch (type) {
			case GrammarOnly_ConditionParser.GUELTIG:
				return ValidityConditionName.Valid;
			case GrammarOnly_ConditionParser.UNGUELTIG:
				return ValidityConditionName.Invalid;
			default:
				throw new IllegalArgumentException(CreatorUtils.getUnknownTokenErrorMessage(t));
		}
	}
}
