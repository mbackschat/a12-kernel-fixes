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

import stnrVerifier from "../../../resources/internal/StNrVerifier.json" with { type: "json" };

import { GermanTaxNumberDefiningDataBuilder } from "./GermanTaxNumberDefiningDataBuilder.js";
import { GermanTaxNumberFormatting } from "./GermanTaxNumberFormatting.js";

export class StnrVerifierDeserializer {
	/**
	 * Deserialisierung der StnrVerifier-Daten:
	 * Die Datei "StNrVerifier.json" liegt im Verzeichnis src/main/resources/internal und wird von dort gelesen.
	 * Das JSON wurde aus dem Original-XML konvertiert, denn für JS ist JSON besser einzulesen.
	 * Das Einlesen der StNrVerifier-Information sollte nur einmal passieren. Da die
	 * Datei in diesem Modul eingecheckt wird können sich diese Daten nicht ändern
	 * (in den vergangenen 10 Jahren gab es in dieser Datei nur eine Änderung, das Länderkürzel
	 * von Niedersachsen wurde von "ND" nach "NI" geändert. Vor diesem Hintergrund lohnt es
	 * nicht, dass die Projekte diese Daten an Kernel übergeben müssen.
	 */
	public static deserialize(): GermanTaxNumberDefiningDataBuilder {
		const _dataBuilder: GermanTaxNumberDefiningDataBuilder =
			new GermanTaxNumberDefiningDataBuilder();
		this._traverseMethods(stnrVerifier.verfahren, _dataBuilder);
		this._traverseBundeslaender(stnrVerifier.bundeslaender, _dataBuilder);
		return _dataBuilder;
	}

	private static _traverseMethods(
		methods: any,
		container: GermanTaxNumberDefiningDataBuilder
	): void {
		for (const _method of methods) {
			const _id: string = this._deriveRequiredFieldValue(_method.id, "id");
			const _name: string = this._deriveRequiredFieldValue(_method.name, "name");
			const _arrFaktoren: number[] = this._traverseStellen(_method.faktoren);
			const _arrSummanden: number[] | undefined =
				_method.summanden != undefined
					? this._traverseStellen(_method.summanden)
					: undefined;
			container.addMethodDefinition(_id, _name, _arrFaktoren, _arrSummanden);
		}
	}

	private static _traverseStellen(node: any): number[] {
		const _arr: number[] = new Array<number>(12);
		_arr[0] = this._deriveRequiredFieldValueAsInt(node.stelle1, "stelle1");
		_arr[1] = this._deriveRequiredFieldValueAsInt(node.stelle2, "stelle2");
		_arr[2] = this._deriveRequiredFieldValueAsInt(node.stelle3, "stelle3");
		_arr[3] = this._deriveRequiredFieldValueAsInt(node.stelle4, "stelle4");
		_arr[4] = this._deriveRequiredFieldValueAsInt(node.stelle5, "stelle5");
		_arr[5] = this._deriveRequiredFieldValueAsInt(node.stelle6, "stelle6");
		_arr[6] = this._deriveRequiredFieldValueAsInt(node.stelle7, "stelle7");
		_arr[7] = this._deriveRequiredFieldValueAsInt(node.stelle8, "stelle8");
		_arr[8] = this._deriveRequiredFieldValueAsInt(node.stelle9, "stelle9");
		_arr[9] = this._deriveRequiredFieldValueAsInt(node.stelle10, "stelle10");
		_arr[10] = this._deriveRequiredFieldValueAsInt(node.stelle11, "stelle11");
		_arr[11] = this._deriveRequiredFieldValueAsInt(node.stelle12, "stelle12");
		return _arr;
	}

	/**
	 * Diese Methode befüllt eine Liste mit den Nummern aller Bundeslaender. Diese Liste wird zu
	 * Validierungen verwendet.
	 *
	 * @param doc
	 * @param container
	 */
	private static _traverseBundeslaender(
		bundeslaender: any,
		container: GermanTaxNumberDefiningDataBuilder
	): void {
		for (const _bundesland of bundeslaender) {
			const _kuerzel: string = _bundesland.kuerzel;

			// 1. Nummern des aktuellen Bundeslands
			const _myBundeslandNrn: Set<number> = this._getBundeslandnummern(
				container,
				_bundesland.nr,
				_kuerzel
			);

			// 2. Verfahren zuordnen
			this._assignMethod(container, _bundesland.verfahren, _myBundeslandNrn);

			// 3. Format
			this._traverseFormatting(container, _bundesland.format, _myBundeslandNrn);

			// 4. Negativmuster
			this._traverseNegativePattern(container, _bundesland.negativmuster, _myBundeslandNrn);
		}
	}

	private static _getBundeslandnummern(
		container: GermanTaxNumberDefiningDataBuilder,
		bundeslandNummern: any,
		kuerzel: string
	): Set<number> {
		const _innerBundeslandNrn: Set<number> = new Set();
		for (const _bundeslandNummer of bundeslandNummern) {
			const _bundeslandNumber = Number(_bundeslandNummer);
			_innerBundeslandNrn.add(_bundeslandNumber);
			container.addFederalStateCode(_bundeslandNumber, kuerzel);
		}
		return _innerBundeslandNrn;
	}

	private static _assignMethod(
		container: GermanTaxNumberDefiningDataBuilder,
		methods: any,
		myBundeslandNrn: Set<number>
	): void {
		for (const _method of methods) {
			const _verfahrenId: string = this._deriveRequiredFieldValue(
				_method.verfahrenId,
				"verfahrenId"
			);
			const _bufanr = Number(_method.bufanr);
			const _bezirkVon: string = _method.bezirkVon;
			const _bezirkBis: string = _method.bezirkBis;

			myBundeslandNrn.forEach(bundeslandNr => {
				container.addMethodMapping(
					bundeslandNr,
					_bufanr,
					_bezirkVon,
					_bezirkBis,
					_verfahrenId
				);
			});
		}
	}

	private static _traverseFormatting(
		container: GermanTaxNumberDefiningDataBuilder,
		format: any,
		myBundeslandNrn: Set<number>
	): void {
		const _stFaNr: number = this._deriveRequiredFieldValueAsInt(
			format.stellenFinanzamtsnr,
			"stellenFinanzamtsnr"
		);
		const _stBezirkNr: number = this._deriveRequiredFieldValueAsInt(
			format.stellenBezirksnr,
			"stellenBezirksnr"
		);
		const _stUNr: number = this._deriveRequiredFieldValueAsInt(
			format.stellenUnterscheidungsnr,
			"stellenUnterscheidungsnr"
		);
		const _prefix: string = format.prefix;
		const _trennz1: string = format.trennzeichen1;
		const _trennz2: string = format.trennzeichen2;
		const _trennz3: string = format.trennzeichen3;
		const _formatting: GermanTaxNumberFormatting = new GermanTaxNumberFormatting(
			_stFaNr,
			_stBezirkNr,
			_stUNr,
			_prefix,
			_trennz1,
			_trennz2,
			_trennz3
		);

		myBundeslandNrn.forEach(bundeslandNr => {
			container.addFormatting(bundeslandNr, _formatting);
		});
	}

	private static _traverseNegativePattern(
		container: GermanTaxNumberDefiningDataBuilder,
		negativePattern: any,
		myBundeslandNrn: Set<number>
	): void {
		for (const _pattern of negativePattern) {
			const _wert: string = this._deriveRequiredFieldValue(_pattern.wert, "wert");
			myBundeslandNrn.forEach(bundeslandNr => {
				container.addNegativePattern(bundeslandNr, _wert);
			});
		}
	}

	private static _deriveRequiredFieldValue(node: string, fieldName: string): string {
		if (node !== undefined) {
			return node;
		}
		// Integrationsproblem
		throw new Error('Das Element muss das Attribut "' + fieldName + '" enthalten.');
	}

	private static _deriveRequiredFieldValueAsInt(node: any, fieldName: string): number {
		if (node !== undefined) {
			return Number(node);
		}
		// Integrationsproblem
		throw new Error('Das Element muss das Attribut "' + fieldName + '" enthalten.');
	}
}
