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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldOderStringContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FieldAndValueListConditionContext;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.ParseTreeCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.util.CreatorUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.util.FeldIdCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.EntityIdListImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.EntityListAndValueListConditionImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListAndValueListConditionName;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

/**
 * Erzeugt für die Grammatik-Regel
 * <p>
 * <code>fieldAndValueListCondition:<p>
 * fieldAndValueListConditionName '(' entitySpec toOp feldUndStringListenSpezifier
 * ')'{checkFieldAndValueListCondition(_localctx);};<p>
 * fieldAndValueListConditionName: 'WertNichtKonsistent';<p>
 * feldUndStringListenSpezifier: feldOderString (',' feldOderString)*;<p>
 * feldOderString: entitySpec | StringLiteral;<p>
 * </code>
 * <p>
 * das richtige Parsebaum-Objekt.
 */
public class FieldAndValueListConditionCreator {

	private FieldAndValueListConditionCreator() {
		throw new IllegalStateException("Utility class");
	}

	public static void create(FieldAndValueListConditionContext ctx, ParseTreeCreator tc) {
		EntityListAndValueListConditionName name = createBedingungNameFromContext(ctx);

		IEntityId id = FeldIdCreator.create(ctx.entitySpec(), tc);
		IEntityIdList ids = new EntityIdListImpl(Collections.singletonList(id));

		List<Object> mixedList = ctx.feldUndStringListenSpezifier().feldOderString().stream()
				.map(x -> convertValue(x, tc))
				.collect(Collectors.toList());

		EntityListAndValueListConditionImpl result = new EntityListAndValueListConditionImpl(name, ids, null, null, mixedList);

		tc.push(result);
	}

	private static Object convertValue(FeldOderStringContext ctx, ParseTreeCreator tc) {
		if (ctx.StringLiteral() != null) {
			return CommonUtils.removeAndUnescapeQuotesAndRemoveCrFromCrLf(ctx.StringLiteral().getText());
		} else {
			return FeldIdCreator.create(ctx.entitySpec(), tc);
		}
	}

	/**
	 * Konvertiert den Bedingungsnamen aus dem Kontext in die internen Namen.
	 */
	private static EntityListAndValueListConditionName createBedingungNameFromContext(FieldAndValueListConditionContext ctx) {
		Token t = ctx.fieldAndValueListConditionName().start;
		int type = t.getType();
		if (type == GrammarOnly_ConditionParser.WERT_NICHT_KONSISTENT) {
			return EntityListAndValueListConditionName.ValueNotConsistent;
		}
		throw new IllegalArgumentException(CreatorUtils.getUnknownTokenErrorMessage(t));
	}

}
