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
 * Wrapper um für Strings, die bei der Auswertung von Regelbedingungen ermittelt werden, zusätzlich
 * zu speichern, ob ein nicht angegebenes oder nicht prüfungsrelevantes Feld gefunden wurde. Ist das
 * Feld nicht prüfungsrelevant, wird ein umgebender Vergleich zu false ausgewertet. Falls das Feld
 * nicht angegeben wurde, kann es zu Auslassungsfehlern kommen.
 */
export class VkString implements VkValue {
	public static get NICHT_PRUEF_REL_STRING(): VkString {
		return this._createNichtPruefungsrelevantenString();
	}
	public static get NICHT_ANGEGEBENER_STRING(): VkString {
		return this._createNichtAngegebenenString();
	}

	private readonly _wert: string;
	private readonly _nichtAngegeben: boolean;
	private readonly _nichtPruefungsrelevant: boolean;

	constructor(wert: string);
	constructor(wert: string, nichtAngegeben: boolean, nichtPruefungsrelevant: boolean);
	constructor(wert: string, nichtAngegeben?: boolean, nichtPruefungsrelevant?: boolean) {
		this._wert = wert != undefined ? wert : "";
		this._nichtAngegeben = nichtAngegeben || false;
		this._nichtPruefungsrelevant = nichtPruefungsrelevant || false;
	}

	private static _createNichtPruefungsrelevantenString(): VkString {
		return new VkString("", false, true);
	}

	private static _createNichtAngegebenenString(): VkString {
		return new VkString("", true, false);
	}

	public get isNichtAngegeben(): boolean {
		return this._nichtAngegeben;
	}

	public get isNichtPruefungsrelevant(): boolean {
		return this._nichtPruefungsrelevant;
	}

	public get wert(): string {
		return this._wert;
	}

	public get hasValue(): boolean {
		return !!this._wert;
	}

	public length(): number {
		return this._wert.length;
	}

	public add(other: VkString | string): VkString {
		let erg: string;
		let na: boolean;
		let np: boolean;
		if (other instanceof VkString) {
			erg = this._wert + other._wert;
			na = this._nichtAngegeben || other._nichtAngegeben;
			np = this._nichtPruefungsrelevant || other._nichtPruefungsrelevant;
		} else {
			erg = this._wert + other;
			na = this._nichtAngegeben;
			np = this._nichtPruefungsrelevant;
		}
		return new VkString(erg, na, np);
	}

	public equals(other: any): boolean {
		if (other == undefined || !(other instanceof VkString)) {
			return false;
		}
		const o: VkString = other;
		return this._wert === o._wert;
	}

	public hash(): number {
		return DefaultHashFunc(this._wert);
	}

	public toString(): string {
		return (
			"VkString(" +
			this._wert +
			", " +
			this._nichtAngegeben +
			", " +
			this._nichtPruefungsrelevant +
			")"
		);
	}
}
