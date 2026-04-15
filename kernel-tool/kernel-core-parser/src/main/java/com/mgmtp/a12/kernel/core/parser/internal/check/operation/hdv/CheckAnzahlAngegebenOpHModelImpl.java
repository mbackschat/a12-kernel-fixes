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

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckFormularUtilitiesHModel;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.AnzahlAngegebenOpHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularEbenenSpecHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularSpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;

import jakarta.validation.constraints.NotNull;

/**
 * Stellt die Prüfungen für <code>AnzahlAngegebenOpHModel</code> bereit.
 * <ul>
 * Entspricht: <b>AnzahlAngegebenerVordrucke</b> Es wird geprüft:
 * <p>
 * <ul>
 * <li>Vordrucksprachkonstrukte dürfen in A12-Modellen nicht verwendet werden (damit es eine
 * vernünftige Fehlermeldung gibt, wird dies nicht schon in der Grammatik verboten).
 * {@link ParserErrorTypesEnum#MVK_INVALID_ROOT_GROUP_CONSTR}</li>
 * <li>Vordruckname ist gültig.{@link ParserErrorTypesEnum#MVK_INVALID_ROOT_GROUP}</li>
 * <li>Für den USB darf kein Stern spezifiziert werden.
 * {@link ParserErrorTypesEnumForHModel#MVK_INVALID_USB_WILDCARD}</li>
 * <li>Die laufende Nummer darf nicht spezifiziert werden.
 * {@link ParserErrorTypesEnumForHModel#MVK_INVALID_L}</li>
 * </ul>
 */
public class CheckAnzahlAngegebenOpHModelImpl extends CheckImplAbstractClass {
	public CheckAnzahlAngegebenOpHModelImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	/**
	 * checks that
	 * <ul>
	 * <li>lfdNr is not specified. In contrast to other conditions, this is interpreted like a
	 * wildcard. To handle all conditions the same way, the wildcard is set in the entity map.
	 * <li>usb may not specify a wildcard.
	 * </ul>
	 *
	 * @param ctx
	 * 		context to be analysed
	 */
	public void check(@NotNull AnzahlAngegebenOpHModelContext ctx) {
		String nameOfOp = ctx.getStart().getText();

		if (fullParser.isA12Model()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_ROOT_GROUP_CONSTR, nameOfOp);
			throw createMVKParserError(ctx, errMessage);
		}

		addNumberOpType(ctx, 0, false);

		FormularSpecContext fCtx = ctx.formularSpec();
		CheckFormularUtilitiesHModel checker = new CheckFormularUtilitiesHModel(this);
		EntityRef ref = checker.putEntityForRootGroupToMap(fCtx);

		// laufende Nummer darf nicht spezifiziert sein.
		if (isLfdNrEbenenSpecified(fCtx)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_L, nameOfOp);
			throw createMVKParserError(ctx, errMessage);
		}

		// usb darf nicht spezifiziert sein.
		if (isUsbEbenenSpecified(fCtx)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_USB_WILDCARD, nameOfOp);
			throw createMVKParserError(ctx, errMessage);
		}
		//no need to set usb as default is correct.

		//Sonderbehandlung für laufende Nummer --> wird auf Wildcard gesetzt,  und nicht Iteration!!
		ref.setLfdNr(IParseTreeEntity.WILDCARD);
	}

	/**
	 * @param ctx
	 * 		die zu prüfende Vordruckspezifikation
	 * @return true, falls ctx eine Spezifikation für laufende Nr. enthält
	 */
	private boolean isLfdNrEbenenSpecified(FormularSpecContext ctx) {
		FormularEbenenSpecHModelContext spec = ctx.formularEbenenSpecHModel();
		return (spec != null && spec.formularEbeneLfdNr() != null);
	}

	private boolean isUsbEbenenSpecified(FormularSpecContext ctx) {
		FormularEbenenSpecHModelContext spec = ctx.formularEbenenSpecHModel();
		return (spec != null && spec.usbSpezifier() != null);
	}

}
