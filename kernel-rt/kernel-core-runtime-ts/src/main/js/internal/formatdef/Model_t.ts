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

/**
 * Struktur fuer die Generierung der Laufzeittypinformationen durch den Generator
 *
 * Beschreibt die grundlegenden Attribute des Modells.
 */
export class Model_t {
	constructor(
		private _erVersion: string,
		private _dezimalTrenner: string,
		private _unterstuetzteSprachen: string[],
		private _applBedingungen: Set<string>,
		private _customFieldTypes: Set<string>,
		private _additionalKeyValues: Map<string, any>,
		private _internalAdditionalKeys: Set<string>
	) { }

	public get erVersion(): string {
		return this._erVersion;
	}

	public get dezimalTrenner(): string {
		return this._dezimalTrenner;
	}

	public get unterstuetzteSprachen(): string[] {
		return this._unterstuetzteSprachen;
	}

	public get applBedingungen(): Set<string> {
		return this._applBedingungen;
	}

	public get customFieldTypes(): Set<string> {
		return this._customFieldTypes;
	}

	public get additionalKeyValues(): Map<string, any> {
		return this._additionalKeyValues;
	}
	public getAdditionalValue(key: string): any {
		return this._additionalKeyValues.get(key);
	}

	public get internalAdditionalKeys(): Set<string> {
		return this._internalAdditionalKeys;
	}
}
