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
import type {IGermanTaxNumberFormatting} from "./IGermanTaxNumberFormatting.js";

export class GermanTaxNumberFormatting implements IGermanTaxNumberFormatting {
	private _numPositionsDistrict: number;
	private _numPositionsTaxOffice: number;
	private _numPositionsDifferentiationNumber: number;
	private _prefix: string;
	private _delimiter1: string;
	private _delimiter2: string;
	private _delimiter3: string;

	public constructor(
		stFaNr: number,
		stBezirkNr: number,
		stUNr: number,
		prefix: string,
		trennz1: string,
		trennz2: string,
		trennz3: string
	) {
		this._numPositionsDistrict = stBezirkNr;
		this._numPositionsTaxOffice = stFaNr;
		this._numPositionsDifferentiationNumber = stUNr;
		this._prefix = prefix == undefined ? "" : prefix;
		this._delimiter1 = trennz1 == undefined ? "" : trennz1;
		this._delimiter2 = trennz2 == undefined ? "" : trennz2;
		this._delimiter3 = trennz3 == undefined ? "" : trennz3;
	}

	public getNumPositionsDistrict(): number {
		return this._numPositionsDistrict;
	}

	public getNumPositionsTaxOffice(): number {
		return this._numPositionsTaxOffice;
	}

	public getNumPositionsDifferentiationNumber(): number {
		return this._numPositionsDifferentiationNumber;
	}

	public getPrefix(): string {
		return this._prefix;
	}

	public getDelimiter1(): string {
		return this._delimiter1;
	}

	public getDelimiter2(): string {
		return this._delimiter2;
	}

	public getDelimiter3(): string {
		return this._delimiter3;
	}
}
