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
import type { IMetaEntity } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { IMetaEntityInternal } from "./IMetaEntityInternal.js";

/**
 * Gemeinsame Basisklasse für alle MetaKlassen.
 *
 */
export abstract class MetaEntity implements IMetaEntity, IMetaEntityInternal {
	private _metadaten: Map<string, any> = new Map<string, any>();
	private _gueltigeSchluessel: Set<string>;
	private _internalKeys: Set<string>;

	constructor(in_gueltigeSchluessel: Set<string>, in_internalKeys: Set<string>) {
		this._gueltigeSchluessel = new Set<string>(in_gueltigeSchluessel);
		this._internalKeys = new Set(in_internalKeys);
	}

	public addWert(schluessel: string, wert: any): void {
		this._pruefeSchluessel(schluessel);
		this._metadaten.set(schluessel, wert);
	}

	protected addAdditionalKey(key: string): void {
		this._gueltigeSchluessel.add(key);
	}

	protected addAdditionalInternalKey(key: string): void {
		this._internalKeys.add(key);
	}

	protected removeKey(key: string): void {
		this._gueltigeSchluessel.delete(key);
	}

	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	public getValue(key: string, ...params: any[]): any {
		return this._getValueIntern(key);
	}


	protected _getAnnotation(key: string, parameter: any[]): any {
		if (parameter == null || parameter.length !== 1 || typeof parameter[0] !== "string") {
			throw new Error(`For lookups with key '${key}', a single parameter of type string must be given.`);
		}

		const ants = this._getValueIntern(key);
		if (ants == undefined || !(ants instanceof Map)) {
			return undefined;
		}

		const antKey = parameter[0];
		if (!ants.has(antKey)) {
			return undefined;
		}

		const val = ants.get(antKey);
		return val == undefined ? "" : val;

	}

	private _getValueIntern(key: string): any {
		if (this._metadaten.has(key)) {
			return this._metadaten.get(key);
		} else {
			throw new Error("Invalid key: " + key);
		}
	}

	private _pruefeSchluessel(schluessel: string): void {
		if (!this._gueltigeSchluessel.has(schluessel) && !this._internalKeys.has(schluessel)) {
			throw new Error("Invalid key: " + schluessel);
		}
	}

	public getValidKeys(): string[] {
		return Array.from(this._gueltigeSchluessel);
	}

	public getValidKeysInternal(): Set<string> {
		return this._internalKeys;
	}

	/**
	 * Überprüft ob dass die optionalen Parameter angegeben sind
	 * Es wird unterschieden ob einer der Parameter gar nicht angegeben ist,
	 * oder ob undefined für ihn angegeben ist. In Beiden Fällen wird false
	 * zurückgegeben.
	 *
	 * @param key 	wird nur zur Log-Ausgabe verwendet
	 * @param anzahlParameter 	Anzahl der erwarteten Parameter
	 * @param parameter 	zu prüfender Parameter
	 * @return Ergebnis der Prüfung
	 */
	protected checkParameterAngegeben(
		key: string,
		anzahlParameter: number,
		parameter: any[]
	): boolean {
		if (parameter == undefined || parameter.length < anzahlParameter) {
			// wir tolerieren, dass zu viele Parameter angegeben wurden.
			return false;
		}
		for (const param of parameter) {
			if (param == undefined) {
				return false;
			}
		}
		return true;
	}

}
