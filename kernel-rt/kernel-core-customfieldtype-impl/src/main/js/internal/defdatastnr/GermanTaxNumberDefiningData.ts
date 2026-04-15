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

import type { IGermanTaxNumberCheckDigitDefinition } from "./IGermanTaxNumberCheckDigitDefinition.js";
import type { IGermanTaxNumberDefiningData } from "./IGermanTaxNumberDefiningData.js";
import type { IGermanTaxNumberFormatting } from "./IGermanTaxNumberFormatting.js";
import { StnrVerifierDeserializer } from "./StnrVerifierDeserializer.js";

/**
 * Repräsentiert eine IDefdatenSteuernummer.
 * Alleine die Menge der BUFA-Nummern ist hier veränderlich.
 * Alle anderen Werte werden unveränderlich in diesem Objekt gehalten.
 */
export class GermanTaxNumberDefiningData implements IGermanTaxNumberDefiningData {
	// Key ist jeweils die Bundeslandnummer
	private readonly _federalStateCodeMap: Map<number, string>;
	private readonly _formattingMap: Map<number, IGermanTaxNumberFormatting>;
	private readonly _negativePatternMap: Map<number, string[]>;
	// Key ist die VerfahrenId
	private readonly _methodMap: Map<string, IGermanTaxNumberCheckDigitDefinition>;
	// Key ist zusammengesetzt aus Bundeslandnummer, Bundesfinanzamtsnummer und
	// Bezirksnummer. Wert ist die VerfahrenId.
	private readonly _methodMappingMap: Map<string, string>;

	private static _instance: IGermanTaxNumberDefiningData;

	public static get instance(): IGermanTaxNumberDefiningData {
		return this._instance ??= new GermanTaxNumberDefiningData();
	}

	private constructor() {
		const dataBuilder = StnrVerifierDeserializer.deserialize();
		this._federalStateCodeMap = dataBuilder.getFederalStateCodeMap();
		this._formattingMap = dataBuilder.getFormattingMap();
		this._negativePatternMap = dataBuilder.getNegativePatternMap();
		this._methodMap = dataBuilder.getMethodMap();
		this._methodMappingMap = dataBuilder.getMethodMappingMap();
	}

	public getFormatting(federalStateNumber: number): IGermanTaxNumberFormatting {
		if (!this._existsBundeslandnummer(federalStateNumber)) {
			throw new Error('Die Bundeslandnummer "' + federalStateNumber + '" ist nicht bekannt.');
		}
		const _formatting: IGermanTaxNumberFormatting | undefined =
			this._formattingMap.get(federalStateNumber);
		if (_formatting == undefined) {
			throw new Error(
				'Zur Bundeslandnummer "' + federalStateNumber + '" existiert keine Formatierung.'
			);
		}
		return _formatting;
	}

	public existsFederalStateCode(federalStateCode: string): boolean {
		return Array.from(this._federalStateCodeMap.values()).includes(federalStateCode);
	}

	private _existsBundeslandnummer(federalStateNumber: number): boolean {
		return this._federalStateCodeMap.has(federalStateNumber);
	}

	public getNegativePattern(federalStateNumber: number): string[] {
		if (!this._existsBundeslandnummer(federalStateNumber)) {
			throw new Error('Die Bundeslandnummer "' + federalStateNumber + '" ist nicht bekannt.');
		}
		const _negativePattern: string[] | undefined =
			this._negativePatternMap.get(federalStateNumber);
		if (_negativePattern == undefined) {
			throw new Error(
				'Für die Bundeslandnummer="' +
				federalStateNumber +
				'" wurde kein Negativmuster definiert.'
			);
		}
		return _negativePattern;
	}

	public getCheckDigitDefinition(
		federalStateNumber: number,
		federalTaxOfficeNumber: number,
		districtNumber: number
	): IGermanTaxNumberCheckDigitDefinition {
		// Prüfen, ob das Verfahren über den Bezirk - als Teil des Finanzamts - zugeordnet ist.
		// Zuordnung hängt an der Bezirksnummer
		let _methodId: string | undefined = this._methodMappingMap.get(
			GermanTaxNumberDefiningData.getKeyMethodMapping(
				federalStateNumber,
				federalTaxOfficeNumber,
				districtNumber
			)
		);
		// Zuordnung hängt an der Bundesfinanzamtsnummer
		_methodId =
			_methodId != undefined
				? _methodId
				: this._methodMappingMap.get(
					GermanTaxNumberDefiningData.getKeyMethodMapping(
						federalStateNumber,
						federalTaxOfficeNumber
					)
				);
		// Zuordnung hängt an der Bundeslandnummer
		_methodId =
			_methodId != undefined
				? _methodId
				: this._methodMappingMap.get(
					GermanTaxNumberDefiningData.getKeyMethodMapping(federalStateNumber)
				);
		if (_methodId == undefined) {
			throw new Error(
				'Den Daten Bundeslandnummer="' +
				federalStateNumber +
				'", federalTaxOfficeNumber="' +
				federalTaxOfficeNumber +
				'" und Bezirksnummer="' +
				districtNumber +
				'" wurde keine Verfahrensdefinition zugeordnet.'
			);
		}
		const _retVal: IGermanTaxNumberCheckDigitDefinition | undefined =
			this._methodMap.get(_methodId);
		if (_retVal == undefined) {
			throw new Error(
				'Den Daten Bundeslandnummer="' +
				federalStateNumber +
				'", federalTaxOfficeNumber="' +
				federalTaxOfficeNumber +
				'" und Bezirksnummer="' +
				districtNumber +
				'" wurde das Verfahren mit der Id="' +
				_methodId +
				'" zugeordnet. ' +
				"Zum Verfahren mit dieser Id existieren aber keine Daten."
			);
		}
		return _retVal;
	}

	public static getKeyMethodMapping(
		bundeslandnr?: number,
		bufanr?: number,
		bezirknr?: number
	): string {
		let _retval = "";
		_retval =
			bundeslandnr != undefined && !isNaN(bundeslandnr) && bundeslandnr !== 0
				? _retval + "#" + bundeslandnr
				: _retval + "#";
		_retval =
			bufanr != undefined && !isNaN(bufanr) && bufanr !== 0
				? _retval + "#" + bufanr
				: _retval + "#";
		_retval =
			bezirknr != undefined && !isNaN(bezirknr) ? _retval + "#" + bezirknr : _retval + "#";
		return _retval;
	}
}
