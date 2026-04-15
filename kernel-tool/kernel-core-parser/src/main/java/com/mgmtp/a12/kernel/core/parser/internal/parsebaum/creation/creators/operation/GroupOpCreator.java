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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation;

import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.GroupOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.ParseTreeCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.util.CreatorUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.EntityIdImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.GroupOperationImpl;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.GroupOperationName;

/**
 * Creates the parse tree object for the grammar rule
 * <p>
 * <code>groupOp: groupOpName '(' entitySpec ')';<p>
 * groupOpName: 'AktuelleWiederholung';
 * </code>
 * <p>
 */
public class GroupOpCreator {

	private GroupOpCreator() {
		throw new IllegalStateException("Utility class");
	}

	public static void create(GroupOpContext ctx, ParseTreeCreator tc) {
		GroupOperationName name = extractOperationNameFromContext(ctx);
		EntityRef ref = tc.getParser().getEntityRef(ctx.entitySpec());
		IEntityId groupId = new EntityIdImpl(ref, tc.getParser().isHModel(), tc.getParser().getEc());
		tc.push(new GroupOperationImpl(name, groupId));
	}

	/**
	 * converts the operation name from the context to the internal name
	 */
	private static GroupOperationName extractOperationNameFromContext(GroupOpContext ctx) {
		Token t = ctx.groupOpName().start;
		int type = t.getType();
		if (type == GrammarOnly_ConditionParser.AKTUELLE_WIEDERHOLUNG) {
			return GroupOperationName.CurrentRepetition;
		}
		throw new IllegalArgumentException(CreatorUtils.getUnknownTokenErrorMessage(t));
	}
}
