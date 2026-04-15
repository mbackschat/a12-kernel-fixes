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

import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumBereichKonstruktContext;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.ParseTreeCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.util.FeldIdCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.datum.DateRangeConstructionOperationImpl;
import com.mgmtp.a12.kernel.core.parser.internal.util.OperationResultType;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;

/**
 * Creates an {@link com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateRangeConstructionOperation}.
 * <p>
 * For the grammar rule
 * <p>
 * <code>
 * datumBereichKonstrukt: 'DatumBereich' '(' date1=datumAngabe2 ',' date2=datumAngabe2 ')'
 * </code>
 * <p>
 * the correct parse tree object will be created.
 * <p>
 * </ul>
 */
public class DatumBereichKonstruktCreator {

	private static final String DEFAULT_FORMAT = "dd.MM.yyyy";

	private DatumBereichKonstruktCreator() {
		throw new IllegalStateException("Utility class");
	}

	public static void create(DatumBereichKonstruktContext ctx, ParseTreeCreator tc) {
		IEntityId date1Id = FeldIdCreator.create(ctx.date1, tc);
		IEntityId date2Id = FeldIdCreator.create(ctx.date2, tc);

		// We always create the result type from a field type and therefore have a concrete format
		OperationResultType type = tc.getParser().getOperationResultTypeMap().getOpType(ctx);
		String format;
		if (type == null) {
			if (!tc.getParser().isLenient()) {
				IRule rule = tc.getParser().getRule();
				String ruleName = rule != null ? rule.getFullName() : "unknown";
				throw new IllegalArgumentException(
						"Unexpected nonexistent type information in full parser in rule " + ruleName);
			}
			// For the lenient parser, we might not have the information
			format = DEFAULT_FORMAT;
		} else {
			format = type.getDateFormat().getConcretFormatIfExists().orElseThrow();
		}

		tc.push(new DateRangeConstructionOperationImpl(date1Id, date2Id, format));
	}
}
