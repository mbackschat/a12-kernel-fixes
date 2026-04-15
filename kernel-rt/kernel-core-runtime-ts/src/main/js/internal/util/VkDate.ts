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
import { DefaultHashFunc } from "@com.mgmtp.a12.utils/utils-collections";

import type { VkValue } from "./VkValue.js";

/**
 * Wrapper um für Datumsangaben, die bei der Auswertung von Regelbedingungen ermittelt werden,
 * zusätzlich zu speichern, ob ein nicht angegebenes oder nicht prüfungsrelevantes Feld gefunden
 * wurde. Ist das Feld nicht prüfungsrelevant, wird ein umgebender Vergleich zu false ausgewertet.
 * Falls das Feld nicht angegeben wurde, kann es zu Auslassungsfehlern kommen.
 */
export class VkDate implements VkValue {
	public static get NON_RELEVANT_DATE(): VkDate {
		return new VkDate(undefined, false, true);
	}
	public static get NOT_GIVEN_DATE(): VkDate {
		return new VkDate(undefined, true, false);
	}
	/**
	 * Does not satisfy the otherwise-probably-true "null => nichtAngegeben"
	 * invariant. Should only be used in controlled contexts where e.g.
	 * withNichtAngebenOred() is called before the VkDate is released.
	 */
	public static get NULL_DATE(): VkDate {
		return new VkDate(undefined, false, false);
	}

	private readonly _date: Date | undefined;
	private readonly _nichtAngegeben: boolean;
	private readonly _nichtPruefungsrelevant: boolean;

	constructor(date: Date | undefined);
	constructor(date: Date | undefined, nichtAngegeben: boolean, nichtPruefungsrelevant: boolean);
	constructor(date: Date | undefined, nichtAngegeben?: boolean, nichtPruefungsrelevant?: boolean) {
		this._date = date;
		this._nichtAngegeben = nichtAngegeben || false;
		this._nichtPruefungsrelevant = nichtPruefungsrelevant || false;
	}

	/**
	 * isNichtAngegeben should be similar to the semantics of kannGroesserWerden/kannKleinerWerden
	 * in VkBigDecimal, but less precise, as nichtAngegeben does not keep the lower/upper bounds on
	 * the value that kannGroesserWerden/kannKleinerWerden provide
	 *
	 * Note that even when nichtAngegeben == true, date may be set and != undefined (e.g. a MaxWert where
	 * some fields are set)
	 *
	 * @return true if this date or any of the fields that it is based on are nichtAngegeben
	 */
	public get isNichtAngegeben(): boolean {
		return this._nichtAngegeben;
	}

	public get isNichtPruefungsrelevant(): boolean {
		return this._nichtPruefungsrelevant;
	}

	public get hasValue(): boolean {
		return this._date !== undefined;
	}

	public get date(): Date | undefined {
		return this._date;
	}

	public max(other: VkDate): VkDate {
		return this._combine(other, (d1: Date, d2: Date) => (d1 >= d2 ? d1 : d2));
	}

	public min(other: VkDate): VkDate {
		return this._combine(other, (d1: Date, d2: Date) => (d1 <= d2 ? d1 : d2));
	}

	private _combine(other: VkDate, select: (d1: Date, d2: Date) => Date): VkDate {
		const nichtAngegeben: boolean = this._nichtAngegeben || other._nichtAngegeben;
		const nichtPruefungsrelevant: boolean =
			this._nichtPruefungsrelevant || other._nichtPruefungsrelevant;

		if (nichtPruefungsrelevant) {
			return VkDate.NON_RELEVANT_DATE;
		}

		let value: Date | undefined;
		if (this.date != undefined && other.date != undefined) {
			value = select(this.date, other.date);
		} else if (this.date != undefined) {
			value = this.date;
		} else if (other.date != undefined) {
			value = other.date;
		}

		return new VkDate(value, nichtAngegeben, nichtPruefungsrelevant);
	}

	/**
	 * create a copy of this VkDate with nichtAngegeben set to true
	 *
	 * (setting nichtAngegeben to false is not supported because
	 *  the resulting object could become inconsistent
	 *  if this._date == undefined)
	 */
	public withNichtAngegeben(): VkDate {
		const nichtAngegeben = true;
		return new VkDate(this._date, nichtAngegeben, this._nichtPruefungsrelevant);
	}

	public equals(other: any): boolean {
		if (other == undefined || !(other instanceof VkDate)) {
			return false;
		}
		const dThis: Date | undefined = this._date;
		const dOther: Date | undefined = other._date;
		if (dThis === undefined && dOther === undefined) {
			return true;
		}
		if (dThis === undefined || dOther === undefined) {
			return false;
		}
		return dThis.valueOf() === dOther.valueOf();
	}

	public hash(): number {
		return DefaultHashFunc(this._date !== undefined ? this._date.valueOf() : 0);
	}

	public toString(): string {
		const postFix = " (nichtAngegeben = " + this._nichtAngegeben + ", nichtPruefungsrelevant = " + this._nichtPruefungsrelevant + ")";
		if (this._date === undefined) {
			return "null" + postFix;
		}
		return this._date.toString() + postFix;
	}
}
