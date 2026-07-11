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
import { HashMap } from "@com.mgmtp.a12.utils/utils-collections";

import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";

/**
 * Struktur fuer die Generierung der Laufzeittypinformationen durch den Generator
 *
 * Pro Feld wird ein Objekt durch den Generator generiert.
 */
export class Feld_t {
	constructor(
		private _interneNr: number,
		private _unqiueName: string,
		private _pfadName: string,
		private _isPflichtFeld: boolean,
		private _isMandatoryForRepeatableGroup: boolean,
		private _formatDef: IFormatDefinition,
		private _javaScriptName: string,
		private _vordruckPflichtFeldLfdNr: number[],
		private _pflichtFeldLfdNr: number[],
		private _additionalKeyValues: Map<string, any>,
		private _internalAdditionalKeys: Set<string>,
		private _berechnungsUebertragServer: boolean,
		private _errorTexts4Req: Map<string, string> | undefined
	) { }

	public get interneNr(): number {
		return this._interneNr;
	}

	public get pflichtFeldLfdNr(): number[] {
		return this._pflichtFeldLfdNr;
	}

	public get vordruckPflichtFeldLfdNr(): number[] {
		return this._vordruckPflichtFeldLfdNr;
	}

	public get javascriptName(): string {
		return this._javaScriptName;
	}

	public get uniqueName(): string {
		return this._unqiueName;
	}

	public get pfadName(): string {
		return this._pfadName;
	}

	public get isPflichtFeld(): boolean {
		return this._isPflichtFeld;
	}

	public get isMandatoryForRepeatableGroup(): boolean {
		return this._isMandatoryForRepeatableGroup;
	}

	public get formatDefintion(): IFormatDefinition {
		return this._formatDef;
	}

	public get isBerechnungsUebertragServer(): boolean {
		return this._berechnungsUebertragServer;
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

	public get errorTexts4Req(): Map<string, string> | HashMap<string, string> {
		return this._errorTexts4Req ?? new HashMap();
	}
}
