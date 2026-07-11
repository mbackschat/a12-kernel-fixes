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
package com.mgmtp.a12.kernel.core.parser.internal.grammar.gen;

import java.util.function.Predicate;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.AnzahlAngegebenOpHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.AnzahlWertOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.BedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DateTimeKonstruktContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DateValidityConditionContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumAddOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumAngabeContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumBereichKonstruktContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumBereichOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumDiffOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumExtractOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumKonstruktContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EinfacheOperationContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ExtmpKonstanteUSBContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldListeUndWerteListenBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldListenAnzahlOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldListenBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldListenOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldListenOpHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldListenOperOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldOpHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldOpMitDatumSpecialContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldOpMitParamsContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldOperOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldUndWerteListenBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldWertOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FieldAndValueListConditionContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularBedingungNameContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.GroupOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.IdNameRefContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.IdNameRefHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.KonstanteContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.KonstanteReferenzjahrContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.KontextListenBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.OperationContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.PreambleContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.PredefinedTypeValidityConditionContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.RegelKontextEntityContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.RelUpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.RelativeUpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.RundenOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.RundenOperOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.SemanticIndexContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.SetOperOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.SpezifierHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.TimeKonstruktContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.VergleichsBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.WiederholungBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ZeilenBedingungContext;

/**
 * Erweiterungen des generierten Parsers, die die in der Grammatik referenzierten checkMethoden an
 * die verschiedenen Checker-Klassen weiter leitet.
 */
public abstract class AbstractConditionParser extends Parser {
	private boolean isExtmp;

	private boolean isHModel;
	private Predicate<ParserRuleContext> isGroupFromHModelCallback;

	/**
	 * @param isExtmp
	 * @param isHModel
	 * @param isGroupFromHModelCallback
	 * 		required if isHModel == true: a function/method that takes the parser's current context
	 * 		(from {@link Parser#getContext()}) and returns true if the corresponding entitySpec refers to a group (as opposed to a
	 * 		field) in the DVL. This is required because in H-DVLs, a path like /RG/f/1 would be ambiguous - the "/1" suffix couuld be
	 * 		a field name or a repetition index.
	 */
	public void initParser(
			boolean isExtmp,
			boolean isHModel,
			Predicate<ParserRuleContext> isGroupFromHModelCallback
	) {
		this.isExtmp = isExtmp;
		this.isHModel = isHModel;
		if (isHModel && isGroupFromHModelCallback == null) {
			throw new IllegalArgumentException("for H-Models, the parser needs to be able to tell if a path belongs to a group or" +
					" field. (otherwise, a reference like /RG/G/f/1 would be ambiguous");
		}
		this.isGroupFromHModelCallback = isGroupFromHModelCallback;
	}

	public AbstractConditionParser(TokenStream tokens) {
		super(tokens);
	}

	/**
	 * This method should never be used explicitly. It is only used by the grammar rule
	 * entityHModel.
	 */
	public boolean isGroupFromHModel() {
		if (!isHModel) {
			return false;
		} else {
			return this.isGroupFromHModelCallback.test(getContext());
		}
	}

	public boolean isExtmp() {
		return isExtmp;
	}

	/**
	 * @return true, for an H model rule condition; false, for an A12 model
	 */
	public boolean isHModel() {
		return this.isHModel;
	}

	/**
	 * @return true, for an A12 model; false, for an H model
	 */
	public boolean isA12Model() {
		return !isHModel();
	}

	// active hooks
	// @formatter:off
	protected void resetSetExpression(EntitySpecContext entitySpecCtx) {}
	protected void initSetExpression(EntitySpecContext ctx) {}
	// @formatter:on

	// check hooks (they do store stuff to the entity map though, in the full parser
	// @formatter:off // don't let the autoformatter double the screen space that gets taken
	// by the stub impls
	protected void checkBedingung(BedingungContext ctx) {}
	protected void checkFeldListenBedingung(FeldListenBedingungContext ctx) {}
	protected void checkFormularBedingung(FormularBedingungContext ctx) {}
	protected void checkKontextListenBedingung(KontextListenBedingungContext ctx) {}
	protected void checkSpezifierHModel(SpezifierHModelContext ctx) {}
	protected void checkIdNameRefHModel(IdNameRefHModelContext ctx) {}
	protected void checkIdNameRef(IdNameRefContext ctx) {}
	protected void checkRegelKontextEntity(RegelKontextEntityContext ctx) {}
	protected void checkVergleichsBedingung(VergleichsBedingungContext ctx) {}
	protected void checkZeilenBedingung(ZeilenBedingungContext ctx) {}
	protected void checkWiederholungBedingung(WiederholungBedingungContext ctx) {}
	protected void checkEinfacheOperation(EinfacheOperationContext ctx) {}
	protected void checkFieldAndValueListCondition(FieldAndValueListConditionContext ctx) {}
	protected void checkFeldListeUndWerteListenBedingung(FeldListeUndWerteListenBedingungContext ctx) {}
	protected void checkFeldUndWerteListenBedingung(FeldUndWerteListenBedingungContext ctx) {}
	protected void checkAnzahlAngegebenOpHModel(AnzahlAngegebenOpHModelContext ctx) {}
	protected void checkFeldOpHModel(FeldOpHModelContext ctx) {}
	protected void checkFeldOpMitDatumSpecial(FeldOpMitDatumSpecialContext ctx) {}
	protected void checkDatumKonstrukt(DatumKonstruktContext ctx) {}
	protected void checkDateValidityCondition(DateValidityConditionContext ctx) {}
	protected void checkPredefinedTypeValidityCondition(PredefinedTypeValidityConditionContext ctx) {}
	protected void checkDateTimeKonstrukt(DateTimeKonstruktContext ctx) {}
	protected void checkTimeKonstrukt(TimeKonstruktContext ctx) {}
	protected void checkDatumBereichOp(DatumBereichOpContext ctx) {}
	protected void checkDatumExtractOp(DatumExtractOpContext ctx) {}
	protected void checkDatumAddOp(DatumAddOpContext ctx) {}
	protected void checkDatumDiffOp(DatumDiffOpContext ctx) {}
	protected void checkFeldOpMitParams(FeldOpMitParamsContext ctx) {}
	protected void checkFeldWertOp(FeldWertOpContext ctx) {}
	protected void checkFeldListenOp(FeldListenOpContext ctx) {}
	protected void checkAnzahlWertOp(AnzahlWertOpContext ctx) {}
	protected void checkFeldListenOperOp(FeldListenOperOpContext ctx) {}
	protected void checkFeldOperOp(FeldOperOpContext ctx) {}
	protected void checkFeldListenAnzahlOp(FeldListenAnzahlOpContext ctx) {}
	protected void checkFeldOp(FeldOpContext ctx) {}
	protected void checkRundenOp(RundenOpContext ctx) {}
	protected void checkRundenOperOp(RundenOperOpContext ctx) {}
	protected void checkOperation(OperationContext ctx) {}
	protected void checkExtmpKonstanteUSB(ExtmpKonstanteUSBContext ctx) {}
	protected void checkKonstanteReferenzjahr(KonstanteReferenzjahrContext ctx) {}
	protected void checkFeldListenOpHModel(FeldListenOpHModelContext ctx) {}
	protected void checkFormularBedingungName(FormularBedingungNameContext ctx) {}
	protected void checkKonstante(KonstanteContext ctx) {}
	protected void checkDatumAngabe(DatumAngabeContext ctx) {}
	protected void checkEntitySpec(EntitySpecContext ctx) {}
	protected void checkSemanticIndex(SemanticIndexContext ctx) {}
	protected void checkPreamble(PreambleContext ctx) {}
	protected void checkRelativeUp(RelativeUpContext ctx) {}
	protected void checkRelUp(RelUpContext ctx) {}
	protected void checkGroupOp(GroupOpContext ctx) {}
	protected void checkDatumBereichKonstrukt(DatumBereichKonstruktContext ctx) {}
	protected void checkSetOperOp(SetOperOpContext ctx) {}
	// @formatter:on
}
