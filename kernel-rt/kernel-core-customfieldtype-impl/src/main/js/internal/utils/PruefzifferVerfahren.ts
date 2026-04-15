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

export class PruefzifferVerfahren {
	static pruefeMitElferVerfahren(
		numberStr: string,
		pruefziffer: number,
		factors: number[]
	): boolean {
		let _sum = 0;
		for (let i = 0; i < factors.length; i++) {
			_sum += factors[i] * Number(numberStr.charAt(i));
		}
		return PruefzifferVerfahren.pruefePruefzifferAusDifferenz(_sum, 11, pruefziffer);
	}

	static pruefeMitZweierVerfahren(
		numberStr: string,
		pruefziffer: number,
		factors: number[],
		summands: number[]
	): boolean {
		let _sum = 0;
		for (let i = 0; i < summands.length; i++) {
			const _sumTemp = summands[i] + Number(numberStr.charAt(i));
			const _quersummeEinstellig = PruefzifferVerfahren._quersummeEinstellig(
				PruefzifferVerfahren.einerstelle(_sumTemp) * factors[i]
			);
			_sum += _quersummeEinstellig;
		}
		return PruefzifferVerfahren.pruefePruefzifferAusDifferenz(_sum, 10, pruefziffer);
	}

	// Pruefziffer: difference between calculated sum and the next bigger number which can be divided by x
	private static pruefzifferAusDifferenz(sum: number, x: number): number {
		let _m = sum % x;
		if (_m !== 0) {
			_m = x - _m;
		}
		return _m;
	}

	static pruefePruefzifferAusDifferenz(sum: number, x: number, pruefziffer: number): boolean {
		return PruefzifferVerfahren.pruefzifferAusDifferenz(sum, x) === pruefziffer;
	}

	private static _quersummeEinstellig(nummer: number): number {
		PruefzifferVerfahren._notNegative(nummer);
		let _quer = nummer;
		while (_quer >= 10) {
			_quer = PruefzifferVerfahren.quersumme(_quer);
		}
		return _quer;
	}

	public static quersumme(number: number): number {
		PruefzifferVerfahren._notNegative(number);
		let _result = 0;
		while (number > 0) {
			_result += number % 10;
			number = Math.floor(number / 10);
		}
		return _result;
	}

	public static einerstelle(number: number): number {
		PruefzifferVerfahren._notNegative(number);
		return number % 10;
	}

	private static _notNegative(number: number): void {
		if (number < 0) {
			throw new Error("Parameter is negative.");
		}
	}
}

// TODO: Fix this the next time the file is edited.
// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace PruefzifferVerfahren {
	export enum Verfahren {
		A1,
		A2,
		A3,
		NRW,
		STNR
	}
}
