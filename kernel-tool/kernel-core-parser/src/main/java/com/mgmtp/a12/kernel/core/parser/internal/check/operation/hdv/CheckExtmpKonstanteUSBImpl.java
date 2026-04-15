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
package com.mgmtp.a12.kernel.core.parser.internal.check.operation.hdv;

import java.util.List;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ExtmpKonstanteUSBContext;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;

import jakarta.validation.constraints.NotNull;

/**
 * Stellt die Prüfungen für <code>ExtmpKonstanteUSB</code> bereit.
 * Entspricht <code> ExtmpKonstanteUSB </code>.
 * Es wird geprüft, ob das Modell einen Vordruck mit USB hat. nur ExtMP Welt, nur H-Modelle
 * * {@link ParserErrorTypesEnumForHModel#MVK_NO_USB}
 */
public class CheckExtmpKonstanteUSBImpl extends CheckImplAbstractClass {
	public CheckExtmpKonstanteUSBImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull ExtmpKonstanteUSBContext ctx) {
		addNumberOpType(ctx, 0, false);

		//Fehler,  falls kein USB Vordruck existiert.
		List<IEntity> entities = fullParser.getEc().getDirectlyContainedEntities("/");
		for (IEntity e : entities) {
			if (EntityUtils.hasMaxUsb(e)) {
				return;
			}
		}

		String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_NO_USB);
		throw createMVKParserError(ctx, errMessage);
	}
}
