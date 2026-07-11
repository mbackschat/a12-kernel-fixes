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

import org.antlr.v4.runtime.TokenStream;

import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckDateValidityConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckFeldListeUndWerteListenBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckFeldListenBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckFeldUndWerteListenBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckFieldAndValueListConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckKontextListenBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckPreambleImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckPredefinedTypeValidityConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckVergleichsBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckWiederholungBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.hdv.CheckFormularBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.hdv.CheckZeilenBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.datum.CheckDateTimeKonstruktImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.datum.CheckDatumAddOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.datum.CheckDatumAngabeImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.datum.CheckDatumBereichKonstruktImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.datum.CheckDatumBereichOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.datum.CheckDatumDiffOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.datum.CheckDatumExtractOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.datum.CheckDatumKonstruktImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.datum.CheckFeldOpMitDatumSpecialImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.datum.CheckTimeKonstruktImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckEntitySpecImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckRegelKontextEntityImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckRelUpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.hdv.CheckIdNameRefHModelImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.hdv.CheckSpezifierHModelImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.semanticIndex.CheckSemanticIndexImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckAnzahlWertOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckEinfacheOperationImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckFeldListenAnzahlOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckFeldListenOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckFeldListenOperOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckFeldOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckFeldOpMitParamsImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckFeldOperOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckFeldWertOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckGroupOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckKonstanteImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckKonstanteReferenzjahrImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckOperationImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckRundenOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckRundenOperOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.CheckSetOperOpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.hdv.CheckAnzahlAngegebenOpHModelImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.hdv.CheckExtmpKonstanteUSBImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.hdv.CheckFeldListenOpHModelImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.hdv.CheckFeldOpHModelImpl;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/**
 * Erweiterungen des generierten Parsers, die die in der Grammatik referenzierten checkMethoden an
 * die verschiedenen Checker-Klassen weiter leitet.
 */
public class FullChecks_ConditionParser extends Lenient_ConditionParser {

	/**
	 * Init the parser for parsing a rule.
	 *
	 * @param rule
	 * 		only relevant for rule conditions
	 * @param modelSpecificSettings
	 * 		only relevant for rule conditions
	 * @param ec
	 * 		only relevant for rule conditions
	 * @param pss
	 * @param pm
	 */
	public void initParser(IRule rule, IModelSpecificSettings modelSpecificSettings, IEC ec, IProjectSpecificSettings pss,
			IProblemReporter pm) {

		super.initParser(rule, modelSpecificSettings, ec, pss, pm);
	}

	@Override protected void checkBedingung(BedingungContext ctx) {
		CheckBedingungImpl checker = new CheckBedingungImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldListenBedingung(FeldListenBedingungContext ctx) {
		CheckFeldListenBedingungImpl checker = new CheckFeldListenBedingungImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFormularBedingung(FormularBedingungContext ctx) {
		CheckFormularBedingungImpl checker = new CheckFormularBedingungImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkKontextListenBedingung(KontextListenBedingungContext ctx) {
		CheckKontextListenBedingungImpl checker = new CheckKontextListenBedingungImpl(this);
		checker.check(ctx);
	}

	public FullChecks_ConditionParser(TokenStream tokens) {
		super(tokens);
	}

	@Override protected void checkSpezifierHModel(SpezifierHModelContext ctx) {
		CheckSpezifierHModelImpl checker = new CheckSpezifierHModelImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkIdNameRefHModel(IdNameRefHModelContext ctx) {
		CheckIdNameRefHModelImpl checker = new CheckIdNameRefHModelImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkRegelKontextEntity(RegelKontextEntityContext ctx) {
		CheckRegelKontextEntityImpl checker = new CheckRegelKontextEntityImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkVergleichsBedingung(VergleichsBedingungContext ctx) {
		CheckVergleichsBedingungImpl checker = new CheckVergleichsBedingungImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkZeilenBedingung(ZeilenBedingungContext ctx) {
		CheckZeilenBedingungImpl checker = new CheckZeilenBedingungImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkWiederholungBedingung(WiederholungBedingungContext ctx) {
		CheckWiederholungBedingungImpl checker = new CheckWiederholungBedingungImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkEinfacheOperation(EinfacheOperationContext ctx) {
		CheckEinfacheOperationImpl checker = new CheckEinfacheOperationImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFieldAndValueListCondition(FieldAndValueListConditionContext ctx) {
		CheckFieldAndValueListConditionImpl checker = new CheckFieldAndValueListConditionImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldListeUndWerteListenBedingung(FeldListeUndWerteListenBedingungContext ctx) {
		CheckFeldListeUndWerteListenBedingungImpl checker = new CheckFeldListeUndWerteListenBedingungImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldUndWerteListenBedingung(FeldUndWerteListenBedingungContext ctx) {
		CheckFeldUndWerteListenBedingungImpl checker = new CheckFeldUndWerteListenBedingungImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkAnzahlAngegebenOpHModel(AnzahlAngegebenOpHModelContext ctx) {
		CheckAnzahlAngegebenOpHModelImpl checker = new CheckAnzahlAngegebenOpHModelImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldOpHModel(FeldOpHModelContext ctx) {
		CheckFeldOpHModelImpl checker = new CheckFeldOpHModelImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldOpMitDatumSpecial(FeldOpMitDatumSpecialContext ctx) {
		CheckFeldOpMitDatumSpecialImpl checker = new CheckFeldOpMitDatumSpecialImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkDatumKonstrukt(DatumKonstruktContext ctx) {
		CheckDatumKonstruktImpl checker = new CheckDatumKonstruktImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkDateValidityCondition(DateValidityConditionContext ctx) {
		CheckDateValidityConditionImpl checker = new CheckDateValidityConditionImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkPredefinedTypeValidityCondition(PredefinedTypeValidityConditionContext ctx) {
		CheckPredefinedTypeValidityConditionImpl checker = new CheckPredefinedTypeValidityConditionImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkDateTimeKonstrukt(DateTimeKonstruktContext ctx) {
		CheckDateTimeKonstruktImpl checker = new CheckDateTimeKonstruktImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkDatumBereichKonstrukt(DatumBereichKonstruktContext ctx) {
		CheckDatumBereichKonstruktImpl checker = new CheckDatumBereichKonstruktImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkTimeKonstrukt(TimeKonstruktContext ctx) {
		CheckTimeKonstruktImpl checker = new CheckTimeKonstruktImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkDatumBereichOp(DatumBereichOpContext ctx) {
		CheckDatumBereichOpImpl checker = new CheckDatumBereichOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkDatumExtractOp(DatumExtractOpContext ctx) {
		CheckDatumExtractOpImpl checker = new CheckDatumExtractOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkDatumAddOp(DatumAddOpContext ctx) {
		CheckDatumAddOpImpl checker = new CheckDatumAddOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkDatumDiffOp(DatumDiffOpContext ctx) {
		CheckDatumDiffOpImpl checker = new CheckDatumDiffOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldOpMitParams(FeldOpMitParamsContext ctx) {
		CheckFeldOpMitParamsImpl checker = new CheckFeldOpMitParamsImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldWertOp(FeldWertOpContext ctx) {
		CheckFeldWertOpImpl checker = new CheckFeldWertOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldListenOp(FeldListenOpContext ctx) {
		CheckFeldListenOpImpl checker = new CheckFeldListenOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkAnzahlWertOp(AnzahlWertOpContext ctx) {
		CheckAnzahlWertOpImpl checker = new CheckAnzahlWertOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldListenOperOp(FeldListenOperOpContext ctx) {
		CheckFeldListenOperOpImpl checker = new CheckFeldListenOperOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldOperOp(FeldOperOpContext ctx) {
		CheckFeldOperOpImpl checker = new CheckFeldOperOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldListenAnzahlOp(FeldListenAnzahlOpContext ctx) {
		CheckFeldListenAnzahlOpImpl checker = new CheckFeldListenAnzahlOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldOp(FeldOpContext ctx) {
		CheckFeldOpImpl checker = new CheckFeldOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkRundenOp(RundenOpContext ctx) {
		CheckRundenOpImpl checker = new CheckRundenOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkRundenOperOp(RundenOperOpContext ctx) {
		CheckRundenOperOpImpl checker = new CheckRundenOperOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkSetOperOp(SetOperOpContext ctx) {
		CheckSetOperOpImpl checker = new CheckSetOperOpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkOperation(OperationContext ctx) {
		CheckOperationImpl checker = new CheckOperationImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkExtmpKonstanteUSB(ExtmpKonstanteUSBContext ctx) {
		CheckExtmpKonstanteUSBImpl checker = new CheckExtmpKonstanteUSBImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkKonstanteReferenzjahr(KonstanteReferenzjahrContext ctx) {
		CheckKonstanteReferenzjahrImpl checker = new CheckKonstanteReferenzjahrImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFeldListenOpHModel(FeldListenOpHModelContext ctx) {
		CheckFeldListenOpHModelImpl checker = new CheckFeldListenOpHModelImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkFormularBedingungName(FormularBedingungNameContext ctx) {
		CheckFormularBedingungImpl checker = new CheckFormularBedingungImpl(this);
		checker.checkFormularBedingungName(ctx);
	}

	@Override protected void checkKonstante(KonstanteContext ctx) {
		CheckKonstanteImpl checker = new CheckKonstanteImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkDatumAngabe(DatumAngabeContext ctx) {
		CheckDatumAngabeImpl checker = new CheckDatumAngabeImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkEntitySpec(EntitySpecContext ctx) {
		CheckEntitySpecImpl checker = new CheckEntitySpecImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkSemanticIndex(SemanticIndexContext ctx) {
		CheckSemanticIndexImpl checker = new CheckSemanticIndexImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkPreamble(PreambleContext ctx) {
		CheckPreambleImpl checker = new CheckPreambleImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkRelUp(RelUpContext ctx) {
		CheckRelUpImpl checker = new CheckRelUpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkGroupOp(GroupOpContext ctx) {
		CheckGroupOpImpl checker = new CheckGroupOpImpl(this);
		checker.check(ctx);
	}

	@Override
	public boolean isLenient() {
		return false;
	}

}