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
import type { RtIdentifierTemplate } from "../util/RtIdentifierTemplate.js";

import type { Identifier_t } from "./Identifier_t.js";

/**
 * Struktur fuer die Generierung der Laufzeittypinformationen durch den Generator
 *
 * Diese Klasse repräsentiert eine Regel.
 */
export class Regel_t {
	constructor(
		private _name: string,
		private _pfadName: string,
		private _fehlercode: string,
		private _fehlertexte: Map<string, string>,
		private _parameterMapping: Map<string, string>,
		private _regelArt: string,
		private _refIdentifier: Identifier_t[],
		private _refAuslassungsIdentifier: Identifier_t[],
		private _refEntity: RtIdentifierTemplate[],
		private _refOmissionEntity: RtIdentifierTemplate[],
		private _refAuslassungsVordrucke: string[],
		private _fehlerfeld: string,
		private _serverBerechnungsRegel: boolean,
		private _dynamicEnumerationRule: boolean,
		private _additionalKeyValues: Map<string, any>,
		private _internalAdditionalKeys: Set<string>
	) { }

	public get refAuslassungsIdentifier(): Identifier_t[] {
		return this._refAuslassungsIdentifier;
	}

	public getRefAuslassungsVordrucke(): Set<string> {
		if (this._refAuslassungsVordrucke == undefined) {
			return new Set<string>();
		}
		const result: Set<string> = new Set<string>();
		for (const element of this._refAuslassungsVordrucke) {
			result.add(element);
		}
		return result;
	}

	public get name(): string {
		return this._name;
	}

	public get pfadName(): string {
		return this._pfadName;
	}

	public get fehlercode(): string {
		return this._fehlercode;
	}

	public get fehlertexte(): Map<string, string> {
		return this._fehlertexte;
	}

	public get parameterMapping(): Map<string, string> {
		return this._parameterMapping;
	}

	public get regelArt(): string {
		return this._regelArt;
	}

	public get refIdentifier(): Identifier_t[] {
		return this._refIdentifier;
	}

	/**
	 * The referenced entities (may contain groups) used for the V2 possibly-causing-entities API.
	 * Empty if the rule has none.
	 */
	public get refEntity(): RtIdentifierTemplate[] {
		return this._refEntity ?? [];
	}

	/**
	 * The referenced entities that could cause omission errors (may contain groups) used for the V2
	 * possibly-causing-entities API. Empty if the rule has none.
	 */
	public get refOmissionEntity(): RtIdentifierTemplate[] {
		return this._refOmissionEntity ?? [];
	}

	public get fehlerFeld(): string {
		return this._fehlerfeld;
	}

	public get isServerBerechnungsRegel(): boolean {
		return this._serverBerechnungsRegel;
	}

	public get isDynamicEnumerationRule(): boolean {
		return this._dynamicEnumerationRule;
	}

	public getAdditionalValue(key: string): any {
		return this._additionalKeyValues.get(key);
	}

	public get additionalKeys(): string[] {
		return Array.from(this._additionalKeyValues.keys());
	}

	public get internalAdditionalKeys(): Set<string> {
		return this._internalAdditionalKeys;
	}
}
