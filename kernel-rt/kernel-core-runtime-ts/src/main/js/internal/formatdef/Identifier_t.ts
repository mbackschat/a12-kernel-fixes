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
export enum ReferenzTyp {
	OPERAND,
	OPERAND_UEBERTRAG,
	BERECHNETES_FELD,
	BERECHNETES_FELD_UEBERTRAG,
	KEINE_BERECHNUNG
}

/**
 * Represents meta information for fields referenced by rules.
 */
export class Identifier_t {
	constructor(
		private _feldIndex: number,
		private _indices: number[],
		private _refType: ReferenzTyp,
		private _semanticIndexValue: string,
		private _semanticIndex: Identifier_t
	) { }

	public get feldIndex(): number {
		return this._feldIndex;
	}

	public get indices(): number[] {
		return this._indices;
	}

	public get isBerechnetesFeldUebertrag(): boolean {
		return this._refType === ReferenzTyp.BERECHNETES_FELD_UEBERTRAG;
	}
	public get isBerechnetesFeld(): boolean {
		return (
			this._refType === ReferenzTyp.BERECHNETES_FELD ||
			this._refType === ReferenzTyp.BERECHNETES_FELD_UEBERTRAG
		);
	}
	public get isOperand(): boolean {
		return this._refType === ReferenzTyp.OPERAND || this._refType === ReferenzTyp.OPERAND_UEBERTRAG;
	}
	public get isOperandUebertrag(): boolean {
		return this._refType === ReferenzTyp.OPERAND_UEBERTRAG;
	}

	public get semanticIndexValue(): string {
		return this._semanticIndexValue;
	}

	public get semanticIndex(): Identifier_t {
		return this._semanticIndex;
	}
}
