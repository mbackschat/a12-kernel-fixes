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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.AnzahlAngegebenOpHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.AnzahlWertOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ApplikationsBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DateTimeKonstruktContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DateValidityConditionContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumAddOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumAngabeContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumBereichOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumDiffOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumExtractOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumKonstruktContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumParamContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ExtmpKonstanteUSBContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldListeUndWerteListenBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldListenAnzahlOpContext;
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
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.GroupOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.KonstanteContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.KonstanteReferenzjahrContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.PredefinedTypeValidityConditionContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.RundenOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.RundenOperOpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.TimeKonstruktContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.VergleichsBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.WiederholungBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ZeilenBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParserBaseListener;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.ApplikationsBedingungCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.FeldListeUndWerteListenBedingungCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.FeldListenBedingungCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.FeldUndWerteListenBedingungCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.FieldAndValueListConditionCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.FormularBedingungCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.KontextListenBedingungCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.ValidityConditionCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.VergleichsBedingungCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.WiederholungBedingungCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.ZusGesetzteBedingungCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.hdv.ZeilenBedingungCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.AnzahlWertOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.DateTimeKonstruktCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.DatumAddOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.DatumAngabeCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.DatumBereichKonstruktCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.DatumBereichOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.DatumDiffOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.DatumExtractOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.DatumKonstruktCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.DatumParamCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.FeldListenAnzahlOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.FeldListenOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.FeldListenOperOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.FeldOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.FeldOpMitDatumSpecialCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.FeldOpMitParamsCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.FeldOperOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.FeldWertOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.GroupOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.KonstanteCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.RundenOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.RundenOperOpCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.TimeKonstruktCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.ZusGesetzteFeldOperationCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.hdv.AnzahlAngegebenOpHModelCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.hdv.ExtmpKonstanteCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.hdv.FeldListenOpHModelCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.hdv.FeldOpHModelCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.CompositeConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.ConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.CompositeOperationImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.OperationImpl;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ICompositeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;

/**
 * Listener zum Erzeugen des ParseBaum-Objektes.
 * Eine Traversierung sollte nur aufgerufen werden, wenn die Bedingung korrekt
 * geparset wurde.
 * <p>
 * Dieser Visitor bietet die Verbindungsstelle zwischen der antlr-Welt (und den von
 * antlr-abgeleiteten Klassen) und der MVK-Welt, da er aus den antlr-Kontext-Objekten
 * eine IBedingung erzeugt.
 * <p>
 * Die Option <code>expand</code> steuert, ob Kontexte zu den entsprechenden Feldlisten expandiert
 * werden sollen, und ob die Sprachkonstrukte <code>FeldAngegeben</code> und
 * <code>FeldNichtAngegeben</code>
 * normalisiert werden sollen. Vergleich {@link ParseTreeCreator#expand}
 * Ausnahme ist <code>AnzahlAngegebenerKontexte</code> für das das Sprachkonstrukt nicht konvertiert
 * wird und der Kontext als IFeldId zusäztlich vor der expandierten Feld-Liste in der Parameterliste
 * enthalten ist. (Siehe
 * {@link FeldListenAnzahlOpCreator#create(FeldListenAnzahlOpContext, ParseTreeCreator)} für eine
 * genauere Spezifikation.
 */
public class ParseTreeCreator extends GrammarOnly_ConditionParserBaseListener {

	private final Lenient_ConditionParser parser;

	private final Deque<ICondition> bedStack = new ArrayDeque<>();
	private final Deque<IOperation> opStack = new ArrayDeque<>();

	private final ExpandMode expand;

	/**
	 * Hinterlegt die Parameter, die beim Traversieren benötigt werden.
	 *
	 * @param expand
	 * 		spezifiziert, ob beim Traversieren ein expandierter IParse-Baum erzeugt werden
	 * 		soll.
	 */
	private ParseTreeCreator(Lenient_ConditionParser parser, ExpandMode expand) {
		this.parser = parser;
		this.expand = expand;
	}

	@Override
	public void exitGeklammerteBedingung(GrammarOnly_ConditionParser.GeklammerteBedingungContext ctx) {
		//nur wenn es wirklich eine zusammengesetzte Bedingung ist,  wird die Klammerung vermerkt.
		//grammar rule geklammerteBedingung: '(' bedingung ')'
		ICondition b = peekBed();
		if (b instanceof CompositeConditionImpl compCond) {
			compCond.setGeklammert();
		}
	}

	@Override
	public void exitBRACKET_OP(GrammarOnly_ConditionParser.BRACKET_OPContext ctx) {

		//nur wenn es wirklich eine zusammengesetzte FeldOperation ist,  wird die Klammerung vermerkt.
		IOperation op = peekFop();
		if (op instanceof ICompositeOperation) {
			((CompositeOperationImpl) op).setGeklammert();
		}
		addCommentsToOperation(ctx.before, ctx.after, op);
	}

	@Override public void exitPOWER_OP(GrammarOnly_ConditionParser.POWER_OPContext ctx) {
		ZusGesetzteFeldOperationCreator.createPower(this);
	}

	@Override
	public void exitMULTDIV_OP(GrammarOnly_ConditionParser.MULTDIV_OPContext ctx) {
		ZusGesetzteFeldOperationCreator.create(ctx, this);
	}

	@Override
	public void exitADDSUB_OP(GrammarOnly_ConditionParser.ADDSUB_OPContext ctx) {
		ZusGesetzteFeldOperationCreator.create(ctx, this);
	}

	@Override
	public void exitBedingung(GrammarOnly_ConditionParser.BedingungContext ctx) {
		ZusGesetzteBedingungCreator.create(ctx, this);
	}

	public Lenient_ConditionParser getParser() {
		return parser;
	}

	/*
	 * ***************************
	 * Zugriffe auf die Stacks
	 */
	public ICondition popBedingung() {
		return bedStack.pop();
	}

	public void push(ICondition item) {
		bedStack.push(item);
	}

	public IOperation popFop() {
		return opStack.pop();
	}

	public void push(IOperation item) {
		opStack.push(item);
	}

	public IOperation peekFop() {
		return opStack.peek();
	}

	public ICondition peekBed() {
		return bedStack.peek();
	}

	@Override
	public void exitFeldListenBedingung(GrammarOnly_ConditionParser.FeldListenBedingungContext ctx) {
		FeldListenBedingungCreator.create(ctx, this);
	}

	@Override
	public void exitKontextListenBedingung(GrammarOnly_ConditionParser.KontextListenBedingungContext ctx) {
		KontextListenBedingungCreator.create(ctx, this);
	}

	@Override
	public void exitFeldListeUndWerteListenBedingung(FeldListeUndWerteListenBedingungContext ctx) {
		FeldListeUndWerteListenBedingungCreator.create(ctx, this);
	}

	@Override
	public void exitFieldAndValueListCondition(FieldAndValueListConditionContext ctx) {
		FieldAndValueListConditionCreator.create(ctx, this);
	}

	@Override
	public void exitFeldUndWerteListenBedingung(FeldUndWerteListenBedingungContext ctx) {
		FeldUndWerteListenBedingungCreator.create(ctx, this);
	}

	@Override
	public void exitApplikationsBedingung(ApplikationsBedingungContext ctx) {
		ApplikationsBedingungCreator.create(ctx, this);
	}

	@Override
	public void exitDateValidityCondition(DateValidityConditionContext ctx) {
		ValidityConditionCreator.create(ctx, this);
	}

	@Override
	public void exitPredefinedTypeValidityCondition(PredefinedTypeValidityConditionContext ctx) {
		ValidityConditionCreator.create(ctx, this);
	}

	@Override
	public void exitVergleichsBedingung(VergleichsBedingungContext ctx) {
		VergleichsBedingungCreator.create(ctx, this);
	}

	@Override
	public void exitWiederholungBedingung(WiederholungBedingungContext ctx) {
		WiederholungBedingungCreator.create(ctx, this);
	}

	@Override
	public void exitZeilenBedingung(ZeilenBedingungContext ctx) {
		ZeilenBedingungCreator.create(ctx, this);
	}

	@Override
	public void exitFormularBedingung(FormularBedingungContext ctx) {
		FormularBedingungCreator.create(ctx, this);
	}

	@Override
	public void exitKonstante(KonstanteContext ctx) {
		KonstanteCreator.create(ctx, this);
	}

	@Override
	public void exitFeldWertOp(FeldWertOpContext ctx) {
		FeldWertOpCreator.create(ctx, this);
	}

	@Override
	public void exitFeldListenOp(FeldListenOpContext ctx) {
		FeldListenOpCreator.create(ctx, this);
	}

	@Override
	public void exitFeldListenOperOp(FeldListenOperOpContext ctx) {
		FeldListenOperOpCreator.create(ctx, this);
	}

	@Override
	public void exitFeldOp(FeldOpContext ctx) {
		FeldOpCreator.create(ctx, this);
	}

	@Override
	public void exitFeldOperOp(FeldOperOpContext ctx) {
		FeldOperOpCreator.create(ctx, this);
	}

	@Override
	public void exitRundenOp(RundenOpContext ctx) {
		RundenOpCreator.create(ctx, this);
	}

	@Override
	public void exitRundenOperOp(RundenOperOpContext ctx) {
		RundenOperOpCreator.create(ctx, this);
	}

	@Override
	public void exitFeldListenAnzahlOp(FeldListenAnzahlOpContext ctx) {
		FeldListenAnzahlOpCreator.create(ctx, this);
	}

	@Override
	public void exitAnzahlWertOp(AnzahlWertOpContext ctx) {
		AnzahlWertOpCreator.create(ctx, this);
	}

	@Override
	public void exitFeldOpMitParams(FeldOpMitParamsContext ctx) {
		FeldOpMitParamsCreator.create(ctx, this);
	}

	@Override
	public void exitDatumAddOp(DatumAddOpContext ctx) {
		DatumAddOpCreator.create(ctx, this);
	}

	@Override
	public void exitDatumDiffOp(DatumDiffOpContext ctx) {
		DatumDiffOpCreator.create(ctx, this);
	}

	@Override
	public void exitDatumAngabe(DatumAngabeContext ctx) {
		DatumAngabeCreator.create(ctx, this);
	}

	@Override
	public void exitDatumExtractOp(DatumExtractOpContext ctx) {
		DatumExtractOpCreator.create(ctx, this);
	}

	@Override
	public void exitDatumBereichOp(DatumBereichOpContext ctx) {
		DatumBereichOpCreator.create(ctx, this);
	}

	@Override
	public void exitDatumParam(DatumParamContext ctx) {
		DatumParamCreator.create(ctx, this);
	}

	@Override
	public void exitDateTimeKonstrukt(DateTimeKonstruktContext ctx) {
		DateTimeKonstruktCreator.create(this);
	}

	@Override
	public void exitDatumBereichKonstrukt(GrammarOnly_ConditionParser.DatumBereichKonstruktContext ctx) {
		DatumBereichKonstruktCreator.create(ctx, this);
	}

	@Override
	public void enterTimeKonstrukt(TimeKonstruktContext ctx) {
		TimeKonstruktCreator.addAnEmptyTimeKonstrukt(this);
	}

	@Override
	public void exitTimeKonstrukt(TimeKonstruktContext ctx) {
		TimeKonstruktCreator.create(this);
	}

	@Override
	public void enterDatumKonstrukt(DatumKonstruktContext ctx) {
		DatumKonstruktCreator.addAnEmptyDatumKonstrukt(this);
	}

	@Override
	public void exitDatumKonstrukt(DatumKonstruktContext ctx) {
		DatumKonstruktCreator.create(this);
	}

	@Override
	public void exitKonstanteReferenzjahr(KonstanteReferenzjahrContext ctx) {
		KonstanteCreator.createBaseYearConstant(ctx, this);
	}

	@Override
	public void exitExtmpKonstanteUSB(ExtmpKonstanteUSBContext ctx) {
		ExtmpKonstanteCreator.create(this);
	}

	@Override
	public void exitFeldOpMitDatumSpecial(FeldOpMitDatumSpecialContext ctx) {
		FeldOpMitDatumSpecialCreator.create(ctx, this);
	}

	@Override
	public void exitFeldListenOpHModel(FeldListenOpHModelContext ctx) {
		FeldListenOpHModelCreator.create(ctx, this);
	}

	@Override
	public void exitFeldOpHModel(FeldOpHModelContext ctx) {
		FeldOpHModelCreator.create(ctx, this);
	}

	@Override
	public void exitAnzahlAngegebenOpHModel(AnzahlAngegebenOpHModelContext ctx) {
		AnzahlAngegebenOpHModelCreator.create(ctx, this);
	}

	@Override
	public void exitGroupOp(GroupOpContext ctx) {
		GroupOpCreator.create(ctx, this);
	}

	@Override public void exitSINGLE_OP(GrammarOnly_ConditionParser.SINGLE_OPContext ctx) {
		addCommentsToOperation(ctx.before, ctx.after, peekFop());
	}

	private void addCommentsToOperation(Token commentBefore, Token commentAfter, IOperation op) {
		if ((commentBefore == null && commentAfter == null) ||
				!(op instanceof OperationImpl opImpl)) {
			return;
		}
		if (commentBefore != null) {
			opImpl.setCommentBefore(commentBefore.getText());
		}
		if (commentAfter != null) {
			opImpl.setCommentAfter(commentAfter.getText());
		}
	}

	@Override public void exitTeilBedingung(GrammarOnly_ConditionParser.TeilBedingungContext ctx) {
		addCommentsToCondition(ctx.before, ctx.after, peekBed());
	}

	private void addCommentsToCondition(Token commentBefore, Token commentAfter, ICondition condition) {
		if ((commentBefore == null && commentAfter == null) ||
				!(condition instanceof ConditionImpl condImpl)) {
			return;
		}
		if (commentBefore != null) {
			condImpl.setCommentBefore(commentBefore.getText());
		}
		if (commentAfter != null) {
			condImpl.setCommentAfter(commentAfter.getText());
		}
	}

	@Override
	public void exitEntitySpec(EntitySpecContext ctx) {
		EntityRef eRef = parser.getEntityRef(ctx);
		if (ctx.filterCond == null) {
			return;
		}
		ICondition cond = popBedingung();
		eRef.setFilterCondition(cond);
	}

	public ExpandMode getExpandMode() {
		return expand;
	}

	/**
	 * Erzeuge einen das IBedingungs-Objekt, das den Parse-Baum der im parser-Objekt
	 * übergebenen Bedingung repräsentiert.
	 * Je nachdem, wie der Expand Mode gesetzt ist,
	 * wird expandiert, expandiert und normalisiert oder nicht.
	 *
	 * @param parserWithParseTree
	 * 		the parser containing the antlr parse tree. May not be <code>null</code>
	 * @param expand
	 * 		the expansion mode to use.
	 * @return created IBedingung Object.
	 */
	public static ICondition createBedingung(Lenient_ConditionParser parserWithParseTree, ExpandMode expand) {
		if (parserWithParseTree == null) {
			return null;
		}
		// Parsing is correct -> construct the Tree.
		ParseTreeCreator tc = walkParseTree(parserWithParseTree, expand);

		//if everything is correct, only one element in bedStack remains.
		assert tc.bedStack.size() == 1 : "Unexpected entries found in  " + tc.logBedStack();
		assert tc.opStack.isEmpty() : "Unexpected entries found in " + tc.logOpStack();
		ICondition result = tc.popBedingung();
		if (!parserWithParseTree.getSuppressedWarnings().isEmpty() && result instanceof ConditionImpl condResult) {
			condResult.setSuppressedWarnings(parserWithParseTree.getSuppressedWarnings());
		}
		return result;
	}

	public static IOperation createOperation(Lenient_ConditionParser parserWithParseTree, ExpandMode expand) {
		if (parserWithParseTree == null) {
			return null;
		}
		// Parsing is correct -> construct the Tree.
		ParseTreeCreator tc = walkParseTree(parserWithParseTree, expand);

		//if everything is correct, only one element in opStack remains.
		assert tc.opStack.size() == 1 : "Unexpected entries found in  " + tc.logBedStack(); // Note: we could also throw an
		// IllegalArgumentException or similar, it seems not plausible that the assertions could be not satisfied in any valid case
		assert tc.bedStack.isEmpty() : "Unexpected entries found in " + tc.logOpStack();
		return tc.popFop();
	}

	private static ParseTreeCreator walkParseTree(Lenient_ConditionParser parser, ExpandMode expand) {
		ParseTreeWalker walker = new ParseTreeWalker();
		ParseTreeCreator tc = new ParseTreeCreator(parser, expand);
		walker.walk(tc, parser.getParseTree());
		return tc;
	}

	private String logStack(Deque<?> stack) {
		return stack.stream().map(Object::toString).collect(Collectors.joining("\n"));
	}

	public String logOpStack() {
		return logStack(opStack);
	}

	public String logBedStack() {
		return logStack(bedStack);
	}

}
