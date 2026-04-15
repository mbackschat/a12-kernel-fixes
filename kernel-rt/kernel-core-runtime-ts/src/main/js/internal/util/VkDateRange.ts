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
 * Wrapper um für Datumsbereiche, die bei der Auswertung von Regelbedingungen ermittelt werden,
 * zusätzlich zu speichern, ob ein nicht angegebenes oder nicht prüfungsrelevantes Feld gefunden
 * wurde. Ist das Feld nicht prüfungsrelevant, wird ein umgebender Vergleich zu false ausgewertet.
 * Falls das Feld nicht angegeben wurde, kann es zu Auslassungsfehlern kommen.
 */
export class VkDateRange implements VkValue {
	public static get NON_RELEVANT_DATE_RANGE(): VkDateRange {
		return new VkDateRange(undefined, undefined, false, true);
	}
	public static get NOT_GIVEN_DATE_RANGE(): VkDateRange {
		return new VkDateRange(undefined, undefined, true, false);
	}
	/**
	 * Does not satisfy the otherwise-probably-true "null => nichtAngegeben"
	 * invariant. Should only be used in controlled contexts where e.g.
	 * withNichtAngebenOred() is called before the VkDateRange is released.
	 */
	public static get NULL_DATE_RANGE(): VkDateRange {
		return new VkDateRange(undefined, undefined, false, false);
	}

	private readonly _start: Date | undefined;
	private readonly _end: Date | undefined;
	private readonly _nichtAngegeben: boolean;
	private readonly _nichtPruefungsrelevant: boolean;

	constructor(start: Date | undefined, end: Date | undefined);
	constructor(start: Date | undefined, end: Date | undefined, nichtAngegeben: boolean, nichtPruefungsrelevant: boolean);
	constructor(start: Date | undefined, end: Date | undefined, nichtAngegeben?: boolean, nichtPruefungsrelevant?: boolean) {
		this._start = start;
		this._end = end;
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
		return this._start !== undefined && this._end !== undefined;
	}

	public get start(): Date | undefined {
		return this._start;
	}

	public get end(): Date | undefined {
		return this._end;
	}

	/**
	 * create a copy of this VkDate with nichtAngegeben set to true
	 *
	 * (setting nichtAngegeben to false is not supported because
	 *  the resulting object could become inconsistent
	 *  if this._date == undefined)
	 */
	public withNichtAngegeben(): VkDateRange {
		const nichtAngegeben = true;
		return new VkDateRange(this._start, this._end, nichtAngegeben, this._nichtPruefungsrelevant);
	}

	private static isEqual(date1: Date | undefined, date2: Date | undefined): boolean {
		if (date1 === undefined && date2 === undefined) {
			return true;
		}
		if (date1 === undefined || date2 === undefined) {
			return false;
		}
		return date1.valueOf === date2.valueOf;
	}

	public equals(other: any): boolean {
		if (other == undefined || !(other instanceof VkDateRange)) {
			return false;
		}
		return VkDateRange.isEqual(this._start, other._start) && VkDateRange.isEqual(this._end, other._end);
	}

	public hash(): number {
		return DefaultHashFunc(31 * (this._start?.valueOf() ?? 0) + (this._end?.valueOf() ?? 0));
	}

	public toString(): string {
		return `${this._start?.toString()}-${this._end?.toString()} (nichtAngegeben = ${this._nichtAngegeben}, nichtPrüfungsrelevant = ${this._nichtPruefungsrelevant})`;
	}
}
