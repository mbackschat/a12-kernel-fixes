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

/**
 * Diese Klasse bietet Methoden um bestimmte Informationen am Interface IDefdatenSteuernummer
 * abzufragen. Die Methoden wurden in diese Klasse ausgelagert, um eine einheitliche
 * Fehlerbehandlung bei unerwarteter/fehlerhafter Befüllung des Interfaces IDefdatenSteuernummer zu
 * erreichen.
 */
export class DefdataStnrAccessHelper {
	public static readonly BEZIRKSNUMMER_BEGIN_IDX = 5;

	private constructor() {
		throw Error("Utility class");
	}

	static getPruefziffernverfahrenDefinition(
		defdata: IGermanTaxNumberDefiningData,
		bundeslandnummer: number,
		bundesfinanzamtsnummer: number,
		bezirksnummer: number
	): IGermanTaxNumberCheckDigitDefinition {
		const _verfahrenDef = defdata.getCheckDigitDefinition(
			bundeslandnummer,
			bundesfinanzamtsnummer,
			bezirksnummer
		);
		if (_verfahrenDef == null) {
			throw new Error(
				"In den defininierenden Daten ist kein Prüfziffernverfahren zu den Kriterien " +
				"Bundeslandnummer=" +
				bundeslandnummer +
				" Bundesfinanzamtsnummer=" +
				bundesfinanzamtsnummer +
				" Bezirksnummer=" +
				bezirksnummer +
				" definiert."
			);
		}
		return _verfahrenDef;
	}

	static getIdxBezirkEnd(
		defdata: IGermanTaxNumberDefiningData,
		bundeslandnummer: number
	): number {
		const _stellenBezirksnummer = defdata
			.getFormatting(bundeslandnummer)
			.getNumPositionsDistrict();
		if (
			_stellenBezirksnummer < 0 ||
			DefdataStnrAccessHelper.BEZIRKSNUMMER_BEGIN_IDX + _stellenBezirksnummer > 13
		) {
			// integration error
			throw new Error(
				"Die defininierenden Daten liefern zur " +
				"Bundeslandnummer " +
				bundeslandnummer +
				" die unerwartete Stellenanzahl " +
				_stellenBezirksnummer +
				" der Bezirksnummer . Die Bezirksnummer kann deshalb nicht aus der Steuernummer extrahiert werden."
			);
		}
		return DefdataStnrAccessHelper.BEZIRKSNUMMER_BEGIN_IDX + _stellenBezirksnummer;
	}

	static getSummanden(verfahrenDef: IGermanTaxNumberCheckDigitDefinition): number[] {
		const _summands = verfahrenDef.getSummands();
		if (_summands == null) {
			// integration error
			throw new Error(
				"Fehler in den defininierenden Daten. " +
				"Der Array mit den im Prüfziffernverfahren notwendigen Summanden ist null."
			);
		}
		if (_summands.length !== 12) {
			// integration error
			throw new Error(
				"Fehler in den defininierenden Daten. " +
				"Der Array mit den im Prüfziffernverfahren notwendigen Summanden hat nicht die erwartete Länge 12."
			);
		}
		return _summands;
	}

	static getFaktoren(verfahrenDef: IGermanTaxNumberCheckDigitDefinition): number[] {
		const _factors = verfahrenDef.getFactors();
		if (_factors == null) {
			// integration error
			throw new Error(
				"Fehler in den defininierenden Daten. " +
				"Der Array mit den im Prüfziffernverfahren notwendigen Faktoren ist null."
			);
		}
		if (_factors.length !== 12) {
			// integration error
			throw new Error(
				"Fehler in den defininierenden Daten. " +
				"Der Array mit den im Prüfziffernverfahren notwendigen Faktoren hat nicht die erwartete Länge 12."
			);
		}
		return _factors;
	}
}
