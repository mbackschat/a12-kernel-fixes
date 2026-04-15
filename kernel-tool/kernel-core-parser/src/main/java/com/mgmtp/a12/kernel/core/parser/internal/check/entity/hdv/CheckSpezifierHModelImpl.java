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
package com.mgmtp.a12.kernel.core.parser.internal.check.entity.hdv;

import org.antlr.v4.runtime.ParserRuleContext;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckHModelCommon;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.LfdNrSpezifierContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.LkonkretContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.LsternContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.SpezifierHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.UsbSpezifierContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ZeileSpezifierContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ZiterationContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ZkonkretContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ZsternContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

import jakarta.validation.constraints.NotNull;

/**
 * Stellt die Prüfungen für <code>checkSpezifierHModel</code> bereit.
 * Überprüfe die Spezifier in (ExtMP) H-Modellen.
 * Folgendes wird überprüft:
 * <ul>
 * <li>Es wurde ein USB angegeben, obwohl es sich nicht um einen USB-wiederholbaren Kontext / Feld
 * handelt.{@link ParserErrorTypesEnumForHModel#MVK_INVALID_USB}
 * <li>Es wurde eine laufende Nummer für einen Kontext spezifiziert. Dies ist nicht erlaubt.
 * {@link ParserErrorTypesEnumForHModel#MVK_H_MODEL_AND_LSPEC_FOR_GROUP}
 * <li>Es wurde eine laufende Nummer mit Stern spezifiziert, obwohl das Feld nicht auf einem
 * Mehrfach-Vordruck liegt. Es wurde die Zeile mit Stern spezifiziert, obwohl das Feld nicht
 * mehrzeilig ist.{@link ParserErrorTypesEnumForHModel#MVK_INVALID_WILDCARD_H_MODEL}
 * <li>Es wurde #Zeile für ein Feld spezifiziert, das ist nicht erlaubt.
 * {@link ParserErrorTypesEnumForHModel#MVK_H_MODEL_INVALID_ZITERATION}
 * <li>Es wurde eine konkrete Zeile oder eine konkrete laufende Nummer spezifiziert, die größer als
 * der MZI oder größer als die laufende Nr. Vordruck ist. Anmerkung: negative Angaben werden durch
 * die Grammatik verhindert. Der Benutzer bekommt dann zwar eine unschöne Fehlermeldung, wer
 * allerdings einen negativen Index angeben möchte, hat es nicht anders verdient.
 * {@link ParserErrorTypesEnumForHModel#MVK_H_MODEL_INDEX_TOO_BIG}
 * {@link ParserErrorTypesEnumForHModel#MVK_H_MODEL_INVALID_INDEX_SPEC}
 * <li>Es wurde ein Stern spezifiziert und dieser Stern wurde eingeschränkt. Die Einschränkung wird
 * auf Gültigkeit geprüft. Es muss sich um ein Intervall handeln, d.h. {@literal Start <= Ende}.
 * Start darf nicht größer als die maximale Wiederholbarkeit sein.
 * {@link ParserErrorTypesEnumForHModel#MVK_ILLEGAL_RESTRICTION_OF_WILDCARD}
 * <li>Es wurde eine Iteration spezifiziert, obwohl das Fehlerfeld auf dieser Ebenen nicht
 * wiederholbar ist. {@link ParserErrorTypesEnumForHModel#MVK_INVALID_U_ITER}
 * {@link ParserErrorTypesEnumForHModel#MVK_INVALID_L_ITER}
 * {@link ParserErrorTypesEnumForHModel#MVK_INVALID_Z_ITER}
 *
 *
 * <ul>
 */
public class CheckSpezifierHModelImpl extends CheckHModelCommon {
	public static final String ZEILE_FM = "die Zeile";
	public static final String ZEILE = "Zeile";
	public static final String LFDNR_FM = "die lfd. Nr";
	public static final String LFDNR_FM_P = "die lfd. Nr.";
	public static final String LFDNR = "lfd. Nr.";

	public CheckSpezifierHModelImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	/*
	 * checks all precondition for the grammar rule
	 * spezifierHModelFeld:
	 * l=lfdNrSpezifier? z=zeileSpezifier? u=usbSpezifier? {checkSpezifierHModelFeld(_localctx);}
	 */
	public void check(@NotNull SpezifierHModelContext ctx) {
		EntitySpecContext eCtx = (EntitySpecContext) ctx.getParent().getParent();
		EntityRefForHModel ref = (EntityRefForHModel) getEntityRef(eCtx);

		//check all three repetition layers and set the index in the entity ref if necessary
		setLfdNrSpec(ctx, ref);
		setZeileSpec(ctx, ref);
		setUsbSpec(ctx, ref);
	}

	/**
	 * checks the value for the usb - if any exists - and sets the index
	 * <ul>
	 * <li>if an usb-specification exists, the grammar allows only wildcards. check, if the entity
	 * defines an usb and set
	 * the usb to {@link IParseTreeEntity#WILDCARD}
	 * <li>if no usb-specification exists:
	 * <ul>
	 * <li>if the entity defines an usb, the missing usb-specification
	 * defines an iteration. Thus, the error field must define an usb as well, because the error
	 * field controls the iteration. Note: in H models the error field can be in a different root
	 * group. Note: max usb is always > 1.
	 * <li>if the entity defines no usb,
	 * everything is fine.
	 * </ul>
	 * </ul>
	 *
	 * @param aCtx
	 * 		context to be analysed
	 * @param ref
	 * 		reference to be checked.
	 */
	private void setUsbSpec(SpezifierHModelContext aCtx, EntityRefForHModel ref) {

		UsbSpezifierContext ctx = aCtx.usbSpezifier();
		boolean hasUsb = EntityUtils.hasMaxUsb(ref.getEntity());

		if (ctx != null) { // usb  has been specified, only wildcard possible
			//check the specification and set the indices:
			if (hasUsb) {
				ref.setUsb(IParseTreeEntity.WILDCARD);
			} else {
				throwMVKParserError(ctx, ParserErrorTypesEnumForHModel.MVK_INVALID_USB, ref);
			}
		} else {// no usb specification
			//the error field requires an usb as well, otherwise iteration is not correct.
			if (hasUsb && !EntityUtils.hasMaxUsb(getErrorEntity()) && !checkNotLenient()) {
				throwMVKParserError(aCtx, ParserErrorTypesEnumForHModel.MVK_INVALID_U_ITER, ref);
			}
		}
	}

	private void throwMVKParserError(ParserRuleContext ctx, ParserErrorTypesEnumForHModel err, EntityRefForHModel ref) {
		String errMessage = getErrorMessage(err, ref.getNameBuiltFromConditionText());
		throw createMVKParserError(ctx, errMessage);
	}

	/**
	 * checks the specification for L and sets the index if necessary.
	 * <ul>
	 * <li>if no specification for l exists: this defines an iteration if max lfd. Nr > 1. In this
	 * case we have to check, that the error field is in a root group with max lfd. Nr. > 1, because
	 * the error
	 * field controls the iteration. Note: in H models the error field can be in a different root
	 * group.
	 * <li>a specification exists:
	 * <ul>
	 * <li>a specification for L is not allowed for groups.
	 * <li>if the specification is a concrete number, it must be a valid, i.e. between 1 and max
	 * lfd. Nr.
	 * set the index accordingly.
	 * <li>if the specification is a wildcard, the entity must specify a max lfd. Nr. > 1. Set the A
	 * wildcard
	 * index to {@link IParseTreeEntity#WILDCARD}, and the wildcard interval begin to 1, and the
	 * interval end to the max lfd. Nr.
	 * The wildcard interval can be restricted. The restrictions are checked and the interval is set
	 * accordingly
	 * </ul>
	 * </ul>
	 *
	 * @param aCtx
	 * 		context to be analysed
	 * @param ref
	 * 		reference to be checked.
	 */
	private void setLfdNrSpec(SpezifierHModelContext aCtx, EntityRefForHModel ref) {
		LfdNrSpezifierContext ctx = aCtx.lfdNrSpezifier();
		long maxLfdNr = ref.getMaxLfdNr();

		//no specification of lfdNr.
		//==========================
		if (ctx == null) {
			//nothing is specified,  if root group is repeatable, the error field must be in a repeatable root group as well.
			//note:  in difference to A12 models:  the error field is not required to be in the same repeatable root group.
			if (maxLfdNr > 1 && EntityUtils.getMaxLfdNr(getErrorEntity()) == 1) {
				throwMVKParserError(aCtx, ParserErrorTypesEnumForHModel.MVK_INVALID_L_ITER, ref);
			}
			return;
		}

		//a specification for lfd. Nr is not allowed for groups.
		if (!ref.getEntity().isField()) {//for groups:  no further specification allowed!
			throwMVKParserError(ctx, ParserErrorTypesEnumForHModel.MVK_H_MODEL_AND_LSPEC_FOR_GROUP, ref);
		}

		// entity is a field:
		if (ctx instanceof LkonkretContext) { // concrete specification for L.
			String ls = ((LkonkretContext) ctx).ZiffernFolge().getText();
			long l = checkIndex(ls, ctx, maxLfdNr, LFDNR, ref.getNameBuiltFromConditionText());

			//set the lfd. Nr.
			ref.setLfdNr(l);
		}

		//sepcification of wildcard
		if (ctx instanceof LsternContext) {
			setLWildCard((LsternContext) ctx, ref);
		}
	}

	/**
	 * checks the specification for Z and sets the index if necessary.
	 * <ul>
	 * <li>if no specification for Z exists: this defines an iteration if Z > 1. In this
	 * case we have to check, that the error field is in a root group with max Z > 1, because
	 * the error
	 * field controls the iteration.
	 * <li>a specification exists:
	 * <ul>
	 * <li>if the specification is a concrete number, it must be a valid, i.e. between 1 and max Z
	 * set the index accordingly.
	 * <li>wildcard specification is not allowed for groups
	 * <li>if the specification is a wildcard, the entity must specify a max Z > 1. Set the
	 * wildcard
	 * index to {@link IParseTreeEntity#WILDCARD}, and the wildcard interval begin to 1, and the
	 * interval end to the max Z
	 * The wildcard interval can be restricted. The restrictions are checked and the interval is set
	 * accordingly
	 * </ul>
	 * </ul>
	 *
	 * @param aCtx
	 * 		context to be analysed
	 * @param ref
	 * 		reference to be checked.
	 */
	private void setZeileSpec(SpezifierHModelContext aCtx, EntityRefForHModel ref) {

		ZeileSpezifierContext ctx = aCtx.zeileSpezifier();
		IEntity e = ref.getEntity();
		String nameUsedForErrorOutput = ref.getNameBuiltFromConditionText();

		//keine Spezifikation:
		//falls Feld mehrzeilig muss auch das Fehlerfeld mehrzeilig sein, da sonst Iteration
		//nicht korrekt.
		// falls Kontext --> korrekt
		if (ctx == null) {
			//Nichts für Zeile spezifiziert:  d.h. Iteration, falls maxZeile > 1, Fehlerfeld muss mehrzeilig sein.
			if (e.isField() && EntityUtils.getMaxZeile((IField) e) > 1 && EntityUtils.getMaxZeile((IField) getErrorEntity()) == 1) {
				String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_Z_ITER, nameUsedForErrorOutput);
				throw createMVKParserError(aCtx, errMessage);
			}
			return; //keine weiteren Prüfungen notwendig.
		}

		//spezielle Notation #Zeile ist nur für Kontexte erlaubt.
		if (ctx instanceof ZiterationContext) {
			if (!e.isGroup()) { //  Spec von #Zeile nur für Kontexte erlaubt
				ParserErrorTypesEnumForHModel err = ParserErrorTypesEnumForHModel.MVK_H_MODEL_INVALID_ZITERATION;
				String errMessage = getErrorMessage(err, nameUsedForErrorOutput);
				throw createMVKParserError(ctx, errMessage);
			}

			//Überprüfe ob die Zeilen-Iteration für den Kontext korrekt ist.
			EntityRefUtils.checkKontextZIteration(aCtx, ref, this);
			ref.setZeile(IParseTreeEntity.ITERATION);
			return;
		}

		//Angabe einer konkreten Zeile
		if (ctx instanceof ZkonkretContext) {
			//In case of a specification of a group,  the MZI of all fields contained in the group must be equal.
			ParserErrorTypesEnumForHModel err = ParserErrorTypesEnumForHModel.MVK_INVALID_Z_FOR_GROUP;
			long maxZeile = EntityRefUtils.getMaxZeileApplicableForGroupsAlso(aCtx, ref, err, this);
			String zs = ((ZkonkretContext) ctx).ZiffernFolge().getText();
			long z = checkIndex(zs, ctx, maxZeile, ZEILE, e.getFullName());
			ref.setZeile(z);
			return;
		}

		// wildcard.
		if (ctx instanceof ZsternContext) {
			//wildcard is not allowed for groups.
			if (!e.isField()) {
				ParserErrorTypesEnumForHModel err = ParserErrorTypesEnumForHModel.MVK_INVALID_Z_WILDCARD_FOR_GROUP;
				String errMessage = getErrorMessage(err, nameUsedForErrorOutput);
				throw createMVKParserError(ctx, errMessage);
			} else {
				setZWildcard((ZsternContext) ctx, ref, EntityUtils.getMaxZeile((IField) e));
			}
		}
	}

	/**
	 * sets the wildcard and its restrictions for Z.
	 * A wildcard is only allowed if max Z > 1
	 *
	 * @param aCtx
	 * 		context to be analysed
	 * @param ref
	 * 		reference to be checked.
	 */
	private void setZWildcard(ZsternContext sContext, EntityRefForHModel ref, long maxZeile) {
		String nameUsedForErrorOutput = ref.getNameBuiltFromConditionText();

		//check if wildcard is allowed
		if (maxZeile == 1 && !checkNotLenient()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_WILDCARD_H_MODEL, nameUsedForErrorOutput,
					ZEILE_FM, ZEILE_FM);
			throw createMVKParserError(sContext, errMessage);
		}
		ref.setZeile(IParseTreeEntity.WILDCARD);

		//set the restrictions
		long[] restr = { 1, maxZeile };

		if (sContext != null && sContext.einschraenkung() != null) {
			restr = getWildCardRestriction(sContext.einschraenkung(), maxZeile, nameUsedForErrorOutput, ZEILE_FM);
		}

		ref.setZeileSternStart(restr[0]);
		ref.setZeileSternEnd(restr[1]);

	}

	/**
	 * sets the wildcard and its restrictions for L.
	 * A wildcard is only allowed if max Lfd. Nr > 1
	 *
	 * @param aCtx
	 * 		context to be analysed
	 * @param ref
	 * 		reference to be checked.
	 */
	private void setLWildCard(LsternContext ctx, EntityRefForHModel ref) {
		long maxLfdNr = ref.getMaxLfdNr();
		String nameUsedForErrorOutput = ref.getNameBuiltFromConditionText();

		//check if wildcard is allowed
		if (maxLfdNr == 1 && !checkNotLenient()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_WILDCARD_H_MODEL, nameUsedForErrorOutput,
					LFDNR_FM_P, LFDNR_FM_P);
			throw createMVKParserError(ctx, errMessage);
		}
		ref.setLfdNr(IParseTreeEntity.WILDCARD);

		//check the wildcard restriction
		long[] restr = { 1, maxLfdNr };
		if (ctx != null && ctx.einschraenkung() != null) {//get the restrictions
			restr = getWildCardRestriction(ctx.einschraenkung(), maxLfdNr, nameUsedForErrorOutput, LFDNR_FM);
		}
		ref.setLfdNrSternStart(restr[0]);
		ref.setLfdNrSternEnd(restr[1]);
	}

}
