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

import { PruefzifferVerfahren } from "./PruefzifferVerfahren.js";

/**
 * Speicher der Bundesland-spezifischen Eigenschaften:
 * - Stelligkeit
 * - Stellen der FA-Nummer
 * - Prüfzifferverfahren
 * - Muster
 * - erlaubte Bundesfinanzamtsnummern
 */
export class EWAzProperties {
	private static readonly _LAENDERNR_STELLIGKEIT: Map<number, number> = new Map<number, number>();
	private static readonly _LAENDERNR_VERFAHREN: Map<number, PruefzifferVerfahren.Verfahren> =
		new Map<number, PruefzifferVerfahren.Verfahren>();
	private static readonly _LAENDERNR_FA_NR_STELLEN: Map<number, number> = new Map<
		number,
		number
	>();
	private static readonly _LAENDERNR_MUSTER: Map<number, RegExp> = new Map<number, RegExp>();
	private static readonly _LAENDERNR_BUFA_NUMMERN: Map<number, Set<number>> = new Map<
		number,
		Set<number>
	>();
	private static readonly _LAENDERNR_FORMATSCHLUESSEL: Map<number, number> = new Map<
		number,
		number
	>();

	private static readonly _TR_REPLACEMENT: string = "[ \\/\\.-]{0,1}";
	private static readonly _Z_REPLACEMENT: string = "\\d";

	private static _getPattern(muster: string): RegExp {
		return new RegExp(
			muster
				.replace(/-/g, EWAzProperties._TR_REPLACEMENT)
				.replace(/Z/g, EWAzProperties._Z_REPLACEMENT)
		);
	}

	private static readonly _LAENDERMUSTER_1: RegExp = EWAzProperties._getPattern(
		"ZZZ-ZZZ-ZZZZ-ZZZ-ZZZ-Z|ZZZ-ZZZ-9ZZ-ZZZZ-ZZZ-Z"
	);
	private static readonly _LAENDERMUSTER_2: RegExp = EWAzProperties._getPattern(
		"ZZ-ZZZ-ZZZZ-ZZZ-ZZZ-Z|ZZ-ZZZ-9ZZ-ZZZZ-ZZZ-Z"
	);
	private static readonly _LAENDERMUSTER_3: RegExp = EWAzProperties._getPattern(
		"ZZ-ZZZ-ZZZZ-ZZZ-ZZZ-Z|ZZ-ZZZ-7-ZZZZZ-ZZZZ-Z"
	);
	private static readonly _LAENDERMUSTER_4: RegExp = EWAzProperties._getPattern("ZZ-ZZZ-ZZZZZ");
	private static readonly _LAENDERMUSTER_5: RegExp =
		EWAzProperties._getPattern("ZZZ-ZZZ-Z-ZZZZZ-Z");
	private static readonly _LAENDERMUSTER_6: RegExp = EWAzProperties._getPattern(
		"ZZ-ZZZ-9-ZZZZZZ-ZZZ-Z|ZZ-ZZZ-ZZZZ-ZZZ-ZZZ-Z|ZZ-ZZZ-7-ZZZZZ-ZZZZ-Z"
	);

	static _constructor = (() => {
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(9, 18); // Bayern
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(30, 18); // Brandenburg
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(40, 18); // Mecklenburg-Vorpommern
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(27, 18); // Rheinland-Pfalz
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(10, 18); // Saarland
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(32, 18); // Sachsen
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(31, 18); // Sachsen-Anhalt
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(41, 18); // Thüringen
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(28, 18); // Baden-Württemberg
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(23, 18); // Niedersachsen
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(11, 12); // Berlin
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(24, 12); // Bremen
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(22, 12); // Hamburg
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(21, 12); // Schleswig-Holstein
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(26, 18); // Hessen
		EWAzProperties._LAENDERNR_STELLIGKEIT.set(5, 14); // Nordrhein-Westfalen

		EWAzProperties._LAENDERNR_VERFAHREN.set(9, PruefzifferVerfahren.Verfahren.A1); // Bayern
		EWAzProperties._LAENDERNR_VERFAHREN.set(30, PruefzifferVerfahren.Verfahren.A1); // Brandenburg
		EWAzProperties._LAENDERNR_VERFAHREN.set(40, PruefzifferVerfahren.Verfahren.A1); // Mecklenburg-Vorpommern
		EWAzProperties._LAENDERNR_VERFAHREN.set(27, PruefzifferVerfahren.Verfahren.A1); // Rheinland-Pfalz
		EWAzProperties._LAENDERNR_VERFAHREN.set(10, PruefzifferVerfahren.Verfahren.A1); // Saarland
		EWAzProperties._LAENDERNR_VERFAHREN.set(32, PruefzifferVerfahren.Verfahren.A1); // Sachsen
		EWAzProperties._LAENDERNR_VERFAHREN.set(31, PruefzifferVerfahren.Verfahren.A1); // Sachsen-Anhalt
		EWAzProperties._LAENDERNR_VERFAHREN.set(41, PruefzifferVerfahren.Verfahren.A1); // Thüringen
		EWAzProperties._LAENDERNR_VERFAHREN.set(28, PruefzifferVerfahren.Verfahren.A2); // Baden-Württemberg
		EWAzProperties._LAENDERNR_VERFAHREN.set(23, PruefzifferVerfahren.Verfahren.A3); // Niedersachsen
		EWAzProperties._LAENDERNR_VERFAHREN.set(11, PruefzifferVerfahren.Verfahren.STNR); // Berlin
		EWAzProperties._LAENDERNR_VERFAHREN.set(24, PruefzifferVerfahren.Verfahren.STNR); // Bremen
		EWAzProperties._LAENDERNR_VERFAHREN.set(22, PruefzifferVerfahren.Verfahren.STNR); // Hamburg
		EWAzProperties._LAENDERNR_VERFAHREN.set(21, PruefzifferVerfahren.Verfahren.STNR); // Schleswig-Holstein
		EWAzProperties._LAENDERNR_VERFAHREN.set(26, PruefzifferVerfahren.Verfahren.A2); // Hessen
		EWAzProperties._LAENDERNR_VERFAHREN.set(5, PruefzifferVerfahren.Verfahren.NRW); // Nordrhein-Westfalen

		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(9, 3); // Bayern
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(30, 3); // Brandenburg
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(40, 3); // Mecklenburg-Vorpommern
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(27, 3); // Rheinland-Pfalz
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(10, 3); // Saarland
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(32, 3); // Sachsen
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(31, 3); // Sachsen-Anhalt
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(41, 3); // Thüringen
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(28, 2); // Baden-Württemberg
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(23, 2); // Niedersachsen
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(11, 2); // Berlin
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(24, 2); // Bremen
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(22, 2); // Hamburg
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(21, 2); // Schleswig-Holstein
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(26, 2); // Hessen
		EWAzProperties._LAENDERNR_FA_NR_STELLEN.set(5, 3); // Nordrhein-Westfalen

		EWAzProperties._LAENDERNR_MUSTER.set(9, EWAzProperties._LAENDERMUSTER_1); // Bayern
		EWAzProperties._LAENDERNR_MUSTER.set(30, EWAzProperties._LAENDERMUSTER_1); // Brandenburg
		EWAzProperties._LAENDERNR_MUSTER.set(40, EWAzProperties._LAENDERMUSTER_1); // Mecklenburg-Vorpommern
		EWAzProperties._LAENDERNR_MUSTER.set(27, EWAzProperties._LAENDERMUSTER_1); // Rheinland-Pfalz
		EWAzProperties._LAENDERNR_MUSTER.set(10, EWAzProperties._LAENDERMUSTER_1); // Saarland
		EWAzProperties._LAENDERNR_MUSTER.set(32, EWAzProperties._LAENDERMUSTER_1); // Sachsen
		EWAzProperties._LAENDERNR_MUSTER.set(31, EWAzProperties._LAENDERMUSTER_1); // Sachsen-Anhalt
		EWAzProperties._LAENDERNR_MUSTER.set(41, EWAzProperties._LAENDERMUSTER_1); // Thüringen
		EWAzProperties._LAENDERNR_MUSTER.set(28, EWAzProperties._LAENDERMUSTER_6); // Baden-Württemberg
		EWAzProperties._LAENDERNR_MUSTER.set(23, EWAzProperties._LAENDERMUSTER_2); // Niedersachsen
		EWAzProperties._LAENDERNR_MUSTER.set(11, EWAzProperties._LAENDERMUSTER_4); // Berlin
		EWAzProperties._LAENDERNR_MUSTER.set(24, EWAzProperties._LAENDERMUSTER_4); // Bremen
		EWAzProperties._LAENDERNR_MUSTER.set(22, EWAzProperties._LAENDERMUSTER_4); // Hamburg
		EWAzProperties._LAENDERNR_MUSTER.set(21, EWAzProperties._LAENDERMUSTER_4); // Schleswig-Holstein
		EWAzProperties._LAENDERNR_MUSTER.set(26, EWAzProperties._LAENDERMUSTER_3); // Hessen
		EWAzProperties._LAENDERNR_MUSTER.set(5, EWAzProperties._LAENDERMUSTER_5); // Nordrhein-Westfalen

		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			91,
			EWAzProperties._getValidBundesfinanzamtnummern(91)
		); // Bayern
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			92,
			EWAzProperties._getValidBundesfinanzamtnummern(92)
		); // Bayern
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			30,
			EWAzProperties._getValidBundesfinanzamtnummern(30)
		); // Brandenburg
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			40,
			EWAzProperties._getValidBundesfinanzamtnummern(40)
		); // Mecklenburg-Vorpommern
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			27,
			EWAzProperties._getValidBundesfinanzamtnummern(27)
		); // Rheinland-Pfalz
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			10,
			EWAzProperties._getValidBundesfinanzamtnummern(10)
		); // Saarland
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			32,
			EWAzProperties._getValidBundesfinanzamtnummern(32)
		); // Sachsen
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			31,
			EWAzProperties._getValidBundesfinanzamtnummern(31)
		); // Sachsen-Anhalt
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			41,
			EWAzProperties._getValidBundesfinanzamtnummern(41)
		); // Thüringen
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			28,
			EWAzProperties._getValidBundesfinanzamtnummern(28)
		); // Baden-Württemberg
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			23,
			EWAzProperties._getValidBundesfinanzamtnummern(23)
		); // Niedersachsen
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			11,
			EWAzProperties._getValidBundesfinanzamtnummern(11)
		); // Berlin
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			24,
			EWAzProperties._getValidBundesfinanzamtnummern(24)
		); // Bremen
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			22,
			EWAzProperties._getValidBundesfinanzamtnummern(22)
		); // Hamburg
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			21,
			EWAzProperties._getValidBundesfinanzamtnummern(21)
		); // Schleswig-Holstein
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			26,
			EWAzProperties._getValidBundesfinanzamtnummern(26)
		); // Hessen
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			51,
			EWAzProperties._getValidBundesfinanzamtnummern(51)
		); // Nordrhein-Westfalen
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			52,
			EWAzProperties._getValidBundesfinanzamtnummern(52)
		); // Nordrhein-Westfalen
		EWAzProperties._LAENDERNR_BUFA_NUMMERN.set(
			53,
			EWAzProperties._getValidBundesfinanzamtnummern(53)
		); // Nordrhein-Westfalen

		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(9, 4); // Bayern
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(30, 4); // Brandenburg
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(40, 4); // Mecklenburg-Vorpommern
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(27, 4); // Rheinland-Pfalz
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(10, 4); // Saarland
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(32, 4); // Sachsen
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(31, 4); // Sachsen-Anhalt
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(41, 4); // Thüringen
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(28, 4); // Baden-Württemberg
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(23, 4); // Niedersachsen
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(11, 0); // Berlin
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(24, 0); // Bremen
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(22, 0); // Hamburg
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(21, 0); // Schleswig-Holstein
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(26, 4); // Hessen
		EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.set(5, 5); // Nordrhein-Westfalen
	})();

	private constructor() {
		// at the moment, we don't need to do something
	}

	public static getStelligkeit(buLaNr: number): number {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return EWAzProperties._LAENDERNR_STELLIGKEIT.get(buLaNr)!;
	}

	public static getVerfahrenDef(buLaNr: number): PruefzifferVerfahren.Verfahren {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return EWAzProperties._LAENDERNR_VERFAHREN.get(buLaNr)!;
	}

	public static getFANrStellen(buLaNr: number): number {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return EWAzProperties._LAENDERNR_FA_NR_STELLEN.get(buLaNr)!;
	}

	public static getMuster(buLaNr: number): RegExp {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return EWAzProperties._LAENDERNR_MUSTER.get(buLaNr)!;
	}

	public static getBundesfinanzamtsnummern(buLaNr: number): Set<number> {
		if (EWAzProperties._LAENDERNR_BUFA_NUMMERN.has(buLaNr)) {
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			return EWAzProperties._LAENDERNR_BUFA_NUMMERN.get(buLaNr)!;
		} else {
			return new Set<number>();
		}
	}

	public static getFormatschluessel(buLaNr: number): number {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return EWAzProperties._LAENDERNR_FORMATSCHLUESSEL.get(buLaNr)!;
	}

	private static _getValidBundesfinanzamtnummern(buLaNr: number): Set<number> {
		const bundesfinanzamtsnummernSet: Set<number> = new Set();
		switch (buLaNr) {
			case 22: // Hamburg
				// Nur FA 16
				bundesfinanzamtsnummernSet.add(2216);
				break;
			case 21: // Schleswig-Holstein
				// Nur FÄ 71 bis 92
				EWAzProperties._getRangeClosed(2171, 2192).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			case 24: // Bremen
				// Nur FÄ 57 und 77
				bundesfinanzamtsnummernSet.add(2457);
				bundesfinanzamtsnummernSet.add(2477);
				break;
			case 11: // Berlin
				// Alle FÄ mit Ausnahme von 15, 28, 38 und 91
				EWAzProperties._getRangeClosed(1100, 1114).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(1116, 1127).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(1129, 1137).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(1139, 1190).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(1192, 1199).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			case 91: // Bayern
				// FA 102 - 115, 117, 119, 121, 123 - 127, 131 - 132, 134, 138 - 142, 151 - 154, 156 - 157, 159, 161 - 163, 168 - 171,
				EWAzProperties._getRangeClosed(9102, 9115).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(9117);
				bundesfinanzamtsnummernSet.add(9119);
				bundesfinanzamtsnummernSet.add(9121);
				EWAzProperties._getRangeClosed(9123, 9127).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(9131, 9132).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(9134);
				EWAzProperties._getRangeClosed(9138, 9142).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(9151, 9154).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(9156, 9157).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(9159);
				EWAzProperties._getRangeClosed(9161, 9163).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(9168, 9171).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			case 92: // Bayern
				// 201 - 208, 211 - 212, 216 - 218, 220 - 223, 227 - 231, 235, 241, 244, 247 - 249, 252, 254 - 255, 257 - 259
				EWAzProperties._getRangeClosed(9201, 9208).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(9211, 9212).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(9216, 9218).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(9220, 9223).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(9227, 9231).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(9235);
				bundesfinanzamtsnummernSet.add(9241);
				bundesfinanzamtsnummernSet.add(9244);
				EWAzProperties._getRangeClosed(9247, 9249).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(9252);
				EWAzProperties._getRangeClosed(9254, 9255).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(9257, 9259).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			case 10: // Saarland
				// 010, 055, 060
				bundesfinanzamtsnummernSet.add(1010);
				bundesfinanzamtsnummernSet.add(1055);
				bundesfinanzamtsnummernSet.add(1060);
				break;
			case 23: // Niedersachsen
				// 313 - 323, 326 - 331, 333 - 336, 338, 340 - 341, 343 - 361, 363 - 370, 375, 376, 378, 379, 388
				EWAzProperties._getRangeClosed(2313, 2323).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2326, 2331).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2333, 2336).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(2338);
				EWAzProperties._getRangeClosed(2340, 2341).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2343, 2361).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2363, 2370).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2375, 2376).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2378, 2379).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(2388);
				break;
			case 26: // Hessen
				// Nur FÄ 01 bis 47
				EWAzProperties._getRangeClosed(2601, 2647).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			case 27: // Rheinland-Pfalz
				// 701 - 702, 706, 708 - 710, 719, 722 - 724, 726 - 727, 729 - 732, 735, 740 - 744
				EWAzProperties._getRangeClosed(2701, 2702).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(2706);
				EWAzProperties._getRangeClosed(2708, 2710).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(2719);
				EWAzProperties._getRangeClosed(2722, 2724).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2726, 2727).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2729, 2732).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(2735);
				EWAzProperties._getRangeClosed(2740, 2744).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			case 28: // Baden-Württemberg
				// 801, 804 - 812, 814 - 816, 818 - 823, 830 - 859, 861 - 865, 869-871, 874, 876 - 891, 899
				bundesfinanzamtsnummernSet.add(2801);
				EWAzProperties._getRangeClosed(2804, 2812).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2814, 2816).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2818, 2823).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2830, 2859).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2861, 2865).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(2869, 2871).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(2874);
				EWAzProperties._getRangeClosed(2876, 2891).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(2899);
				break;
			case 30: // Brandenburg
				// 046, 048 - 053, 056, 057, 061, 062, 064, 065
				bundesfinanzamtsnummernSet.add(3046);
				EWAzProperties._getRangeClosed(3048, 3053).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(3056, 3057).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(3061, 3062).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(3064, 3065).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			case 31: // Sachsen-Anhalt
				// 102 - 103, 105 - 108, 110, 112, 114 - 119
				EWAzProperties._getRangeClosed(3102, 3103).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(3105, 3108).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(3110);
				bundesfinanzamtsnummernSet.add(3112);
				EWAzProperties._getRangeClosed(3114, 3119).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			case 32: // Sachsen
				// 202, 204, 207 - 210, 213 - 214, 217 - 218, 220, 222 - 224, 227 - 228, 232, 236 - 239
				bundesfinanzamtsnummernSet.add(3202);
				bundesfinanzamtsnummernSet.add(3204);
				EWAzProperties._getRangeClosed(3207, 3210).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(3213, 3214).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(3217, 3218).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(3220);
				EWAzProperties._getRangeClosed(3222, 3224).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(3227, 3228).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(3232);
				EWAzProperties._getRangeClosed(3236, 3239).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			case 40: // Mecklenburg-Vorpommern
				// 072, 075, 079 - 082, 084, 086, 087, 090
				bundesfinanzamtsnummernSet.add(4072);
				bundesfinanzamtsnummernSet.add(4075);
				EWAzProperties._getRangeClosed(4079, 4082).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(4084);
				EWAzProperties._getRangeClosed(4086, 4087).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(4090);
				break;
			case 41: // Thüringen
				// 151, 154 - 157, 159, 161 - 162, 165, 166, 171
				bundesfinanzamtsnummernSet.add(4151);
				EWAzProperties._getRangeClosed(4154, 4157).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(4159);
				EWAzProperties._getRangeClosed(4161, 4162).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				EWAzProperties._getRangeClosed(4165, 4166).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				bundesfinanzamtsnummernSet.add(4171);
				break;
			case 51: // Nordrhein-Westfalen (OFD Düsseldorf)
				// Nur FÄ 01 bis 83
				EWAzProperties._getRangeClosed(5101, 5183).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			case 52: // Nordrhein-Westfalen (OFD Köln)
				// Nur FÄ 01 bis 83
				EWAzProperties._getRangeClosed(5201, 5283).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			case 53: // Nordrhein-Westfalen (OFD Münster)
				// Nur FÄ 01 bis 84
				EWAzProperties._getRangeClosed(5301, 5384).forEach(bufaNr =>
					bundesfinanzamtsnummernSet.add(bufaNr)
				);
				break;
			default:
				throw new Error(
					'Die übergebene Bundeslandnummer "' + buLaNr + '" ist nicht gültig'
				);
		}
		return bundesfinanzamtsnummernSet;
	}

	private static _getRangeClosed(from: number, to: number): number[] {
		const arr: number[] = [];
		for (let i = from; i <= to; i++) {
			arr.push(i);
		}
		return arr;
	}
}
