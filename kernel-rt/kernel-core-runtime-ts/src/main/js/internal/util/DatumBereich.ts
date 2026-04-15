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
import { BedingungsOperatorHelper, VglOp } from "../core/BedingungsOperatorHelper.js";

export class DatumBereich {
	private _start: Date;
	private _ende: Date;

	constructor(start: Date, ende: Date) {
		if (start == null || ende == null) {
			throw new Error(
				"The constructor of the class 'DatumBereich' may not be called with the value undefined."
			);
		}
		this._start = start;
		this._ende = ende;
	}

	/**
	 * Liefert true, wenn dieser Datumsbereich sich mit einem Datumsbereich aus der Menge
	 * ueberlappt.
	 * Dabei gehoeren sowohl Anfangs- als auch und Enddatum zum Datumsbereich.
	 * Ist einer der Datumsbereich ungueltig (d.h. start liegt nach ende), so wird false
	 * zurückgegeben.
	 */
	public ueberlapptMitEinemAusMenge(datumBereiche: DatumBereich[]): boolean {
		return datumBereiche.some((datBer: DatumBereich) => this.ueberlappt(datBer));
	}

	/**
	 * Liefert true, wenn dieser Datumsbereich sich mit other ueberlappt.
	 * Dabei gehoeren sowohl Anfangs- als auch und Enddatum zum Datumsbereich.
	 * Ist einer der Datumsbereich ungueltig (d.h. start liegt nach ende), so wird false
	 * zurückgegeben.
	 */
	public ueberlappt(other: DatumBereich): boolean {
		if (other == undefined) {
			return false;
		}

		if (!this.startVorEnde() || !other.startVorEnde()) {
			// einer der Datumsbereiche ungueltig
			return false;
		}
		/*
		 * Nicht ueberlappend ist aequivalent mit "Ende des einen Datumsbereichs liegt vor dem
		 * Anfang des
		 * anderen", d.h. "ende < other.start oder other.ende < start".
		 * Also ist ueberlappend aequivalent mit "ende >= other.start und other.ende >= start"
		 */
		return (
			BedingungsOperatorHelper.vergleicheDATUM(VglOp.GE, this._ende, other._start) &&
			BedingungsOperatorHelper.vergleicheDATUM(VglOp.GE, other._ende, this._start)
		);
	}

	private startVorEnde(): boolean {
		return BedingungsOperatorHelper.vergleicheDATUM(VglOp.LE, this._start, this._ende);
	}
}
