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

package com.mgmtp.a12.kernel.core.parser.internal.check.bedingung;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.FieldUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.PredefinedTypeValidityConditionContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

import jakarta.validation.constraints.NotNull;

/**
 * Checker class for
 * {@link FullChecks_ConditionParser#checkPredefinedTypeValidityCondition}.
 */
public class CheckPredefinedTypeValidityConditionImpl extends CheckImplAbstractClass {

	public CheckPredefinedTypeValidityConditionImpl(FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull PredefinedTypeValidityConditionContext ctx) {
		EntitySpecContext ectx = ctx.feld;
		EntityRef ref = getEntityRef(ectx);

		CheckEntityUtils checker = new CheckEntityUtils(fullParser);

		String opName = ctx.validityConditionName().getText();
		IField f = checker.checkIsField(ref, opName, ectx);
		checker.checkNoWildCards(ref, opName, ectx);
		checker.checkIsValueValidation(ref, opName, ectx);
		if (!FieldUtils.isStringEnumOrExtensibleEnum(f)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_NO_STRING_OR_ENUM_OR_EXT_ENUM, opName);
			throw createMVKParserError(ctx, errMessage);
		}
		String codeName = CommonUtils.removeAndUnescapeQuotesAndRemoveCrFromCrLf(ctx.predefinedType.getText());
		if (StringUtils.isEmpty(codeName)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_NO_EMPTY, opName);
			throw createMVKParserError(ctx, errMessage);
		}
	}
}
