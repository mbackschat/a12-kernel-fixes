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
package com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.hdv;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckFormularUtilitiesHModel;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckHModelCommon;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularBedingungNameContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularEbenenSpecHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularSpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefForHModel;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;

import jakarta.validation.constraints.NotNull;

/**
 * Stellt die Prüfungen für <code>checkFormularBedingung</code> bereit.
 * <b>Anmerkung</b>:
 * Damit bei der Verwendung von Vordruck-Sprachkonstrukten eine gute Fehlermeldung erzeugt wird,
 * wird mit checkFormularBedingungName abgeprüft, ob wir in einer A12-DV sind. Falls ja wird
 * folgende Fehlermeldung ausgegeben. {@link ParserErrorTypesEnum#MVK_INVALID_ROOT_GROUP_CONSTR}
 * <p>
 * Foglende Prüfungen erfolgen
 * <ul>
 * <li>
 * Für alle Formularbedingungen wird geprüft, dass der Vordruckname gültig ist
 * {@link ParserErrorTypesEnum#MVK_INVALID_ROOT_GROUP}
 * </li>
 * <li>
 * Für alle Formularbedingungen wird geprüft, dass der durch den Vordrucknamen spezifizierte
 * Vordruck mindestens ein Feld enthält {@link ParserErrorTypesEnum.MVK_ROOT_GROUP_IS_EMPTY}
 * </li>
 * <li>Bei <b>MindestensEinVordruckAngegeben</b> oder <b>KeinVordruckAngegeben</b> wird geprüft,
 * dass der referenzierte Vordruck ein Mehrfachvordruck oder USB-Vordruck ist und mindestens einen
 * Stern-Operator verwendet wird.
 * {@link ParserErrorTypesEnum#MVK_CONDITION_WITHOUT_REPEATABLE_ROOTGROUP}
 * </li>
 * <li>Bei <b>VordruckNichtFortlaufend</b> muss der referenzierte Vordruck ein Mehrfachvordruck
 * sein. Für USB und lfdNr dürfen keine Angaben gemacht werden. Da es sich um ein selten verwendetes
 * und unproblematisches Sprachkonstrukt handelt, werden alle Fehlersituationen mit einer
 * Fehlermeldung behandelt. Achtung: Sonderbehandlung in der Verarbeitung. Für die Verarbeitung wird
 * die laufende Nummer auf 1 gesetzt (und nicht Iteration, wie es Standard-mäßig der Fall
 * wäre). {@link ParserErrorTypesEnumForHModel#MVK_L_NO_MULT}
 * </li>
 * <li>Bei <b>UsbNichtFortlaufend</b> muss der referenzierte Vordruck ein USB-Vordruck mit max. USB
 * {@literal > 1} sein. Für USB und lfdNr dürfen keine Angaben gemacht werden. Da es sich um ein
 * selten verwendetes und unproblematisches Sprachkonstrukt handelt, werden alle Fehlersituationen
 * mit einer Fehlermeldung behandelt. Achtung: Sonderbehandlung in der Verarbeitung. Für die
 * Verarbeitung werden der Usb und die laufende Nummer auf 1 gesetzt (und nicht Iteration, wie es
 * Standard-mäßig der Fall wäre). {@link ParserErrorTypesEnumForHModel#MVK_U_NO_MULT}
 * </li>
 * <li>Bei <b>VordruckAngegeben</b> oder <b>VordruckNichtAngegeben</b>: es darf kein Stern-Operator
 * spezifiziert werden{@link ParserErrorTypesEnum#MVK_CONDITION_WITH_WILDCARD}
 * </li>
 * <li>Für alle Formularbedingungen wird geprüft: Wird ein Stern-Operator verwendet, so muss auch
 * die entsprechende Wiederholungsebene echt wiederholbar sein.
 * {@link ParserErrorTypesEnum#MVK_INVALID_WILDCARD}
 * {@link ParserErrorTypesEnumForHModel#MVK_INVALID_USB}
 * </li>
 * </ul>
 */
public class CheckFormularBedingungImpl extends CheckHModelCommon {

	public CheckFormularBedingungImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull FormularBedingungContext ctx) {

		CheckFormularUtilitiesHModel checker = new CheckFormularUtilitiesHModel(this);
		FormularSpecContext fCtx = ctx.formularSpec();

		EntityRef ref = checker.putEntityForRootGroupToMap(fCtx);
		IEntity e = ref.getEntity();

		FormularBedingungNameContext opCtx = ctx.formularBedingungName();

		if (opCtx.getStart().getType() == Base_ConditionLexer.VORDRUCK_NICHT_FORTLAUFEND) {
			handleSpecialCaseVordruckNichtFortlaufend(e, ctx);
			return;
		}

		if (opCtx.getStart().getType() == Base_ConditionLexer.USB_NICHT_FORTLAUFEND) {
			handleSpecialCaseUsbNichtFortlaufend(e, ctx);
			return;
		}
		//Zeilen müssen für Formular-Sprachkonstrukte nicht überprüft werden,  da
		//durch Grammatik bereits verhindert wird, dass Zeilen-Spezifikationen eingegeben werden.
		checker.setAndCheckUSB(ref, fCtx);
		checker.setAndCheckL(ref, fCtx);

		//if the condition is only valid for repeatable groups, check if the
		// root group is repeatable and a wildcard is specified
		if (conditionOnlyValidForRepeatableRootGroups(opCtx)) {
			CheckEntityUtils eChecker = new CheckEntityUtils(fullParser);
			eChecker.checkRootGroupIsRepeatable(e, getConditionName(opCtx), ctx);
			if (!checker.hasWildCardSpecified(ctx.formularSpec())) {
				throwException(ctx, ParserErrorTypesEnum.MVK_CONDITION_WITHOUT_REPEATABLE_ROOTGROUP);
			}
		} else {
			//condition is only valid for one instance, check that no wildcard is specified
			if (checker.hasWildCardSpecified(ctx.formularSpec())) {
				throwException(ctx, ParserErrorTypesEnum.MVK_CONDITION_WITH_WILDCARD);
			}
		}

	}

	/**
	 * VordruckNichtFortlaufend: Vordruck muss wiederholbar sein (betrifft nur lfd. Nr) und
	 * darf keine spezifische Wiederholung spezifizieren.
	 * Aufgrund der besonderen Behandlung in der Codegenerierung wird die laufende Nummer auf 1
	 * gesetzt.
	 */
	private void handleSpecialCaseVordruckNichtFortlaufend(IEntity e, FormularBedingungContext ctx) {
		if (EntityUtils.getMaxLfdNr(e) == 1 || isFormularEbenenSpecified(ctx)) {
			throwException(ctx, ParserErrorTypesEnumForHModel.MVK_L_NO_MULT);
		}

		//Sonderbehandlung für laufende Nummer --> wird auf 1 gesetzt,  und nicht Iteration!!
		EntityRefForHModel ref = (EntityRefForHModel) getEntityRef(ctx.formularSpec());
		ref.setLfdNr(1);
	}

	/**
	 * UsbNichtFortlaufend: Vordruck muss wiederholbar sein (betrifft nur USB) und
	 * darf keine spezifische Wiederholung spezifizieren(nicht für USB und nicht für LfdNr).
	 * Aufgrund der besonderen Behandlung in der Codegenerierung wird die laufende Nummer und
	 * der USB auf 1 gesetzt.
	 */
	private void handleSpecialCaseUsbNichtFortlaufend(IEntity e, FormularBedingungContext ctx) {
		if (EntityUtils.getMaxUsb(e) < 2 || isFormularEbenenSpecified(ctx)) {
			throwException(ctx, ParserErrorTypesEnumForHModel.MVK_U_NO_MULT);
		}

		//Sonderbehandlung für USB und laufende Nummer --> wird auf 1 gesetzt,  und nicht Iteration!!
		EntityRefForHModel ref = (EntityRefForHModel) getEntityRef(ctx.formularSpec());
		ref.setLfdNr(1);
		ref.setUsb(1);
	}

	/**
	 * @param ctx
	 * 		die zu prüfende FormularBedingung
	 * @return true, falls ctx eine Spezifikation für laufende Nr. oder USB enthält
	 */
	private boolean isFormularEbenenSpecified(FormularBedingungContext ctx) {
		FormularEbenenSpecHModelContext spec = ctx.formularSpec().formularEbenenSpecHModel();
		return (spec != null && (spec.formularEbeneLfdNr() != null || spec.usbSpezifier() != null));
	}

	private void throwException(FormularBedingungContext ctx, ParserErrorTypesEnum error) {
		FormularBedingungNameContext nCtx = ctx.formularBedingungName();
		String errMessage = getErrorMessage(error, getConditionName(nCtx));
		throw createMVKParserError(nCtx, errMessage);
	}

	private void throwException(FormularBedingungContext ctx, ParserErrorTypesEnumForHModel error) {
		FormularBedingungNameContext nCtx = ctx.formularBedingungName();
		String errMessage = getErrorMessage(error, getConditionName(nCtx));
		throw createMVKParserError(nCtx, errMessage);
	}

	private String getConditionName(FormularBedingungNameContext nCtx) {
		return nCtx.start.getText();
	}

	/**
	 * Checks, if the condition is only true for repeatable root groups
	 */
	private boolean conditionOnlyValidForRepeatableRootGroups(FormularBedingungNameContext ctx) {
		return ctx.start.getType() == Base_ConditionLexer.MINDESTENS_EIN_VORDRUCK_ANGEGEBEN ||
				ctx.start.getType() == Base_ConditionLexer.KEIN_VORDRUCK_ANGEGEBEN;
	}

	/**
	 * Provide a nice error message, if the construct is used in an A12 model.
	 */
	public void checkFormularBedingungName(FormularBedingungNameContext ctx) {
		if (fullParser.isA12Model()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_ROOT_GROUP_CONSTR, getConditionName(ctx));
			throw createMVKParserError(ctx, errMessage);
		}
	}

}
