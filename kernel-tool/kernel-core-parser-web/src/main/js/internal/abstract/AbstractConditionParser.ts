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
import type { TokenStream } from "antlr4ForA12Kernel";
import { Parser } from "antlr4ForA12Kernel";

import type {
	AnzahlAngegebenOpHModelContext,
	AnzahlWertOpContext,
	BedingungContext,
	DateTimeKonstruktContext,
	DateValidityConditionContext,
	DatumAddOpContext,
	DatumAngabeContext,
	DatumBereichKonstruktContext,
	DatumBereichOpContext,
	DatumDiffOpContext,
	DatumExtractOpContext,
	DatumKonstruktContext,
	EinfacheOperationContext,
	EntitySpecContext,
	ExtmpKonstanteUSBContext,
	FeldListenAnzahlOpContext,
	FeldListenBedingungContext,
	FeldListenOpContext,
	FeldListenOperOpContext,
	FeldListenOpHModelContext,
	FeldListeUndWerteListenBedingungContext,
	FeldOpContext,
	FeldOperOpContext,
	FeldOpHModelContext,
	FeldOpMitDatumSpecialContext,
	FeldOpMitParamsContext,
	FeldUndWerteListenBedingungContext,
	FeldWertOpContext,
	FieldAndValueListConditionContext,
	FormularBedingungContext,
	FormularBedingungNameContext,
	GroupOpContext,
	IdNameRefContext,
	IdNameRefHModelContext,
	KonstanteContext,
	KonstanteReferenzjahrContext,
	KontextListenBedingungContext,
	OperationContext,
	PreambleContext,
	PredefinedTypeValidityConditionContext,
	RegelKontextEntityContext,
	RelativeUpContext,
	RelUpContext,
	RundenOpContext,
	RundenOperOpContext,
	SemanticIndexContext,
	SpezifierHModelContext,
	TimeKonstruktContext,
	VergleichsBedingungContext,
	WiederholungBedingungContext,
	ZeilenBedingungContext
} from "../gen/GrammarOnly_ConditionParser.js";

export default class AbstractConditionParser extends Parser {
	private _isExtmp: boolean;
	private _isHModel: boolean;

	constructor(input: TokenStream, isExtmp = true, isHModel = false) {
		super(input);
		this._isExtmp = isExtmp;
		this._isHModel = isHModel;
	}

	public initialize(isExtmp: boolean, isHModel: boolean): void {
		this._isExtmp = isExtmp;
		this._isHModel = isHModel;
	}

	public reset(): void {
		super.reset();
		this.state = -1;
	}

	public isExtmp(): boolean {
		return this._isExtmp;
	}

	public isHModel(): boolean {
		return this._isHModel;
	}

	public isA12Model(): boolean {
		return !this._isHModel;
	}

	protected isGroupFromHModel(): boolean {
		// is always true to allow '/' for the model, because we don't know the group context
		return true;
	}


	protected checkPreamble(_ctx: PreambleContext): void { } //NOSONAR
	protected checkBedingung(_ctx: BedingungContext): void { } //NOSONAR //NOSONAR
	protected checkFeldListenBedingung(_ctx: FeldListenBedingungContext): void { } //NOSONAR //NOSONAR
	protected checkKontextListenBedingung(_ctx: KontextListenBedingungContext): void { } //NOSONAR //NOSONAR
	protected checkFeldListeUndWerteListenBedingung(_ctx: FeldListeUndWerteListenBedingungContext): void { } //NOSONAR //NOSONAR
	protected checkFieldAndValueListCondition(_ctx: FieldAndValueListConditionContext): void { } //NOSONAR //NOSONAR
	protected checkFeldUndWerteListenBedingung(_ctx: FeldUndWerteListenBedingungContext): void { } //NOSONAR //NOSONAR
	protected checkDateValidityCondition(_ctx: DateValidityConditionContext): void { } //NOSONAR //NOSONAR
	protected checkPredefinedTypeValidityCondition(_ctx: PredefinedTypeValidityConditionContext): void { } //NOSONAR
	protected checkVergleichsBedingung(_ctx: VergleichsBedingungContext): void { } //NOSONAR
	protected checkWiederholungBedingung(_ctx: WiederholungBedingungContext): void { } //NOSONAR
	protected checkOperation(_ctx: OperationContext): void { } //NOSONAR
	protected checkEinfacheOperation(_ctx: EinfacheOperationContext): void { } //NOSONAR
	protected checkFeldWertOp(_ctx: FeldWertOpContext): void { } //NOSONAR
	protected checkFeldListenOp(_ctx: FeldListenOpContext): void { } //NOSONAR
	protected checkFeldListenOperOp(_ctx: FeldListenOperOpContext): void { } //NOSONAR
	protected checkFeldOp(_ctx: FeldOpContext): void { } //NOSONAR
	protected checkFeldOperOp(_ctx: FeldOperOpContext): void { } //NOSONAR
	protected checkRundenOp(_ctx: RundenOpContext): void { } //NOSONAR
	protected checkRundenOperOp(_ctx: RundenOperOpContext): void { } //NOSONAR
	protected checkFeldListenAnzahlOp(_ctx: FeldListenAnzahlOpContext): void { } //NOSONAR
	protected checkAnzahlWertOp(_ctx: AnzahlWertOpContext): void { } //NOSONAR
	protected checkFeldOpMitParams(_ctx: FeldOpMitParamsContext): void { } //NOSONAR
	protected checkKonstante(_ctx: KonstanteContext): void { } //NOSONAR
	protected checkKonstanteReferenzjahr(_ctx: KonstanteReferenzjahrContext): void { } //NOSONAR
	protected checkExtmpKonstanteUSB(_ctx: ExtmpKonstanteUSBContext): void { } //NOSONAR
	protected checkDatumDiffOp(_ctx: DatumDiffOpContext): void { } //NOSONAR
	protected checkDatumAddOp(_ctx: DatumAddOpContext): void { } //NOSONAR
	protected checkDatumExtractOp(_ctx: DatumExtractOpContext): void { } //NOSONAR
	protected checkDatumKonstrukt(_ctx: DatumKonstruktContext): void { } //NOSONAR
	protected checkTimeKonstrukt(_ctx: TimeKonstruktContext): void { } //NOSONAR
	protected checkDateTimeKonstrukt(_ctx: DateTimeKonstruktContext): void { } //NOSONAR
	protected checkDatumBereichOp(_ctx: DatumBereichOpContext): void { } //NOSONAR
	protected checkDatumAngabe(_ctx: DatumAngabeContext): void { } //NOSONAR
	protected checkFeldOpMitDatumSpecial(_ctx: FeldOpMitDatumSpecialContext): void { } //NOSONAR
	protected checkEntitySpec(_ctx: EntitySpecContext): void { } //NOSONAR
	protected initFilterCondition(_ctx: EntitySpecContext): void { } //NOSONAR
	protected resetFilterCondition(_ctx: EntitySpecContext): void { } //NOSONAR
	protected checkRegelKontextEntity(_ctx: RegelKontextEntityContext): void { } //NOSONAR
	protected checkRelativeUp(_ctx: RelativeUpContext): void { } //NOSONAR
	protected checkRelUp(_ctx: RelUpContext): void { } //NOSONAR
	protected checkIdNameRef(_ctx: IdNameRefContext): void { } //NOSONAR
	protected checkSemanticIndex(_ctx: SemanticIndexContext): void { } //NOSONAR
	protected checkFormularBedingung(_ctx: FormularBedingungContext): void { } //NOSONAR
	protected checkFormularBedingungName(_ctx: FormularBedingungNameContext): void { } //NOSONAR
	protected checkZeilenBedingung(_ctx: ZeilenBedingungContext): void { } //NOSONAR
	protected checkFeldListenOpHModel(_ctx: FeldListenOpHModelContext): void { } //NOSONAR
	protected checkFeldOpHModel(_ctx: FeldOpHModelContext): void { } //NOSONAR
	protected checkAnzahlAngegebenOpHModel(_ctx: AnzahlAngegebenOpHModelContext): void { } //NOSONAR
	protected checkIdNameRefHModel(_ctx: IdNameRefHModelContext): void { } //NOSONAR
	protected checkSpezifierHModel(_ctx: SpezifierHModelContext): void { } //NOSONAR
	protected checkGroupOp(_ctx: GroupOpContext): void { } //NOSONAR
	protected checkDatumBereichKonstrukt(_ctx: DatumBereichKonstruktContext): void { }
}
