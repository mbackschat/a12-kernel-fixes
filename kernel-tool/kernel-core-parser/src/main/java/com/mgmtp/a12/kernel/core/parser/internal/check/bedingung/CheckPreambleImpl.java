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

import java.util.Set;

import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.PreambleContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;

import jakarta.validation.constraints.NotNull;

/**
 * Implements the strict checks for <code>checkPreamble</code>.
 * <p>
 * Each warning specified in the preamble is validated to be supported; an unsupported warning is
 * reported as {@code MVK_INVALID_SUPPRESSED_WARNING}. The supported warnings are then registered on
 * the parser via {@link CheckPreambleLenientImpl}. Multiple warnings can be specified as a
 * comma-separated list.
 * <p>
 * Currently, the following are supported:
 * - {@code MVK_INVALID_COMPARE_DEC_PLACES}
 * - {@code MVK_ERROR_INVALID_ENUM_CALCULATION}
 */
public class CheckPreambleImpl extends CheckPreambleLenientImpl {

	/**
	 * The names of the warnings that may be suppressed. {@code MVK_ERROR_INVALID_ENUM_CALCULATION} is
	 * a calculation error and thus cannot be referenced as an enum constant here, because the parser
	 * must not depend on the calculation module.
	 */
	private static final Set<String> SUPPORTED_WARNINGS = Set.of(
			ParserErrorTypesEnum.MVK_INVALID_COMPARE_DEC_PLACES.name(),
			"MVK_ERROR_INVALID_ENUM_CALCULATION");

	public CheckPreambleImpl(@NotNull Lenient_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull PreambleContext ctx) {
		for (Token warning : ctx.n) {
			if (!SUPPORTED_WARNINGS.contains(warning.getText())) {
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_SUPPRESSED_WARNING, warning.getText());
				throw createMVKParserError(warning, errMessage);
			}
		}
		super.check(ctx);
	}
}
