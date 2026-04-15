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

import {GermanTaxNumberCheckDigitDefinition} from "./GermanTaxNumberCheckDigitDefinition.js";
import type {IGermanTaxNumberCheckDigitDefinition} from "./IGermanTaxNumberCheckDigitDefinition.js";
import type {IGermanTaxNumberFormatting} from "./IGermanTaxNumberFormatting.js";
import {GermanTaxNumberDefiningData} from "./GermanTaxNumberDefiningData.js";

export class GermanTaxNumberDefiningDataBuilder {
	// Key ist jeweils die Bundeslandnummer
	private _federalStateCodeMap: Map<number, string>;
	private _formattingMap: Map<number, IGermanTaxNumberFormatting>;
	private _negativePatternMap: Map<number, string[]>;
	// Key ist die VerfahrenId
	private _methodMap: Map<string, IGermanTaxNumberCheckDigitDefinition>;
	// Key ist zusammengesetzt aus Bundeslandnummer, Bundesfinanzamtsnummer und
	// Bezirksnummer. Wert ist die VerfahrenId.
	private _methodMappingMap: Map<string, string>;

	public constructor() {
		this._federalStateCodeMap = new Map();
		this._formattingMap = new Map();
		this._negativePatternMap = new Map();
		this._methodMap = new Map();
		this._methodMappingMap = new Map();
	}

	addFormatting(federalStateNumber: number, formatting: IGermanTaxNumberFormatting): void {
		this._formattingMap.set(federalStateNumber, formatting);
	}

	addMethodDefinition(id: string, name: string, faktoren: number[], summanden?: number[]): void {
		if (!this._methodMap.has(id)) {
			const _methodDef: GermanTaxNumberCheckDigitDefinition =
				new GermanTaxNumberCheckDigitDefinition(name, faktoren, summanden);
			this._methodMap.set(id, _methodDef);
		} else {
			// Integrationsproblem
			throw new Error(
				'Das  Prüfziffernverfahren  mit der Id "' + id + '" existiert bereits..'
			);
		}
	}

	addMethodMapping(
		bundeslandnr: number,
		bufanr: number,
		bezirkVon: string,
		bezirkBis: string,
		verfahrenId: string
	): void {
		if (!this._methodMap.has(verfahrenId)) {
			throw new Error('Es existiert kein Verfahren mit der Id "' + verfahrenId + '".');
		}
		const _keys: Set<string> = this._getAllKeysMethodMapping(
			bundeslandnr,
			bufanr,
			bezirkVon,
			bezirkBis
		);
		for (const key of _keys) {
			if (this._methodMappingMap.has(key)) {
				// Integrationsproblem
				throw new Error(
					'Dem Schluessel "' + key + '" wurde schon ein Verfahren zugeordnet.'
				);
			}
			this._methodMappingMap.set(key, verfahrenId);
		}
	}

	private _getAllKeysMethodMapping(
		bundeslandnr: number,
		bufanr: number,
		bezirkVon: string,
		bezirkBis: string
	): Set<string> {
		const _keys: Set<string> = new Set<string>();
		if (this._isBlankString(bezirkVon) && this._isBlankString(bezirkBis)) {
			_keys.add(GermanTaxNumberDefiningData.getKeyMethodMapping(bundeslandnr, bufanr));
			return _keys;
		}
		if (!this._isBlankString(bezirkVon) && !this._isBlankString(bezirkBis)) {
			const _iBezirkVon = Number(bezirkVon);
			const _iBezirkBis = Number(bezirkBis);
			if (_iBezirkVon > _iBezirkBis) {
				// Integrationsproblem
				throw new Error(
					'Bei der Zuordnung des Prüfziffernverfahrens zur Bundesfinanzamtsnummer "' +
						bufanr +
						'" ist die untere Grenze der Bezirke groesser als die obere Grenze.'
				);
			}
			let bezirk = _iBezirkVon;
			for (let i = _iBezirkVon; i <= _iBezirkBis; i++) {
				_keys.add(
					GermanTaxNumberDefiningData.getKeyMethodMapping(bundeslandnr, bufanr, bezirk)
				);
				bezirk++;
			}
			return _keys;
		}
		// Integrationsproblem
		throw new Error(
			'Bei der Zuordnung des Prüfziffernverfahrens zur Bundesfinanzamtsnummer "' +
				bufanr +
				'" wurde nur die untere oder die obere Grenze der Bezirke gesetzt. Es muessen ' +
				"entweder beide Werte oder keiner gesetzt werden."
		);
	}

	addFederalStateCode(bundeslandnummer: number, laenderkuerzel: string): void {
		this._federalStateCodeMap.set(bundeslandnummer, laenderkuerzel);
	}

	addNegativePattern(bundeslandnummer: number, negativMuster: string): void {
		if (this._negativePatternMap.has(bundeslandnummer)) {
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			this._negativePatternMap.get(bundeslandnummer)!.push(negativMuster);
		} else {
			const _set: string[] = [];
			_set.push(negativMuster);
			this._negativePatternMap.set(bundeslandnummer, _set);
		}
	}

	private _isBlankString(str: string): boolean {
		return str == undefined || str.trim() === "";
	}

	getMethodMappingMap(): Map<string, string> {
		return this._methodMappingMap;
	}
	getMethodMap(): Map<string, IGermanTaxNumberCheckDigitDefinition> {
		return this._methodMap;
	}
	getNegativePatternMap(): Map<number, string[]> {
		return this._negativePatternMap;
	}
	getFormattingMap(): Map<number, IGermanTaxNumberFormatting> {
		return this._formattingMap;
	}
	getFederalStateCodeMap(): Map<number, string> {
		return this._federalStateCodeMap;
	}
}
