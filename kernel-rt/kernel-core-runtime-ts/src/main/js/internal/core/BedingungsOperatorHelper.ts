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
import { Big } from "big.js";

import {
	TimeZonedDate
} from "@com.mgmtp.a12.utils/utils-localization/lib/main/conversion/a12internal/index.js";

import { VkBigDecimal } from "../util/VkBigDecimal.js";
import { VkDate } from "../util/VkDate.js";


import type { VkDateRange } from "../util/VkDateRange.js";

import { ValidierungsErgebnis } from "./ValidierungsErgebnis.js";

type UnitOfTime = "day" | "month" | "year" | "hour" | "minute" | "second";

export enum VglOp {
	GT,
	NE,
	LT,
	EQ,
	GE,
	LE,
	NE10,
	NE5,
	NE2,
	NE1,
	NM,
	WM
}

export class BedingungsOperatorHelper {
	private static _patternMap: Map<string, RegExp> = new Map<string, RegExp>();

	/**
	 * Lazy Initalisation der verwendeten Patterns. Falls das Pattern noch nicht
	 * in der Map vorhanden, wird es in die Map eingefügt und zurückgegeben.
	 *
	 * @param regex	regulärer Ausdruck
	 * @return Pattern zu der Map.
	 */
	public static getPattern(regex: string): RegExp {
		let result: RegExp | undefined = BedingungsOperatorHelper._patternMap.get(regex);
		if (result === undefined) {
			result = BedingungsOperatorHelper._initPattern(regex);
		}
		return result;
	}

	/**
	 * Nur das Erzeugen und einfügen des Patterns in die Map ist synchronisiert.
	 * Falls ein zweiter Thread hier wartet, ist durch die if-Klausel sichergestellt,
	 * dass das Pattern nicht erneut erzeugt wird.
	 */
	private static _initPattern(regex: string): RegExp {
		let result: RegExp | undefined = BedingungsOperatorHelper._patternMap.get(regex);
		if (result === undefined) {
			result = new RegExp("^(?:" + regex + ")$");
			BedingungsOperatorHelper._patternMap.set(regex, result);
		}
		return result;
	}

	/**
	 * Vergleich zwischen zwei Datumsangaben.
	 *
	 * @param op	Vergleichsoperation
	 * @param date1
	 * @param date2
	 * @return true falls die Bedingung zutrifft, in allen anderen Fällen (auch
	 *         Fehlerfällen) wird false zurückgegeben. Andere Fälle sind
	 *         insbesondere: kein Wert für Feld, oder UNKNOWN als Wert.
	 */
	public static vergleicheDATUM(op: VglOp, date1: Date | undefined, date2: Date | undefined): boolean {
		// Wenn eine der Datumsangaben undefined ist -> gib immer false zurück
		if (!date1 || !date2) {
			return false;
		}

		const date1Numeric = date1.getTime()
		const date2Numeric = date2.getTime()
		switch (op) {
			case VglOp.GT:
				return date1Numeric > date2Numeric;
			case VglOp.NE:
				return date1Numeric !== date2Numeric;
			case VglOp.LT:
				return date1Numeric < date2Numeric;
			case VglOp.EQ:
				return date1Numeric === date2Numeric;
			case VglOp.GE:
				return date1Numeric >= date2Numeric;
			case VglOp.LE:
				return date1Numeric <= date2Numeric;
			default:
				throw new Error("Error during code generation: Unknown comparison operator <" + op + ">.");
		}
	}

	public static vergleicheDATERANGE(op: VglOp, d1: VkDateRange | undefined, d2: VkDateRange | undefined): boolean {
		// Wenn eine der Datumsangaben undefined ist -> gib immer false zurück
		if (!d1 || !d2) {
			return false;
		}

		const vergleichStart = BedingungsOperatorHelper.vergleicheDATUM(op, d1.start, d2.start);
		const vergleichEnd = BedingungsOperatorHelper.vergleicheDATUM(op, d1.end, d2.end);

		switch (op) {
			case VglOp.NE:
				return vergleichStart || vergleichEnd;
			case VglOp.EQ:
				return vergleichStart && vergleichEnd;
			case VglOp.GT:
			case VglOp.LT:
			case VglOp.GE:
			case VglOp.LE:
				throw new Error(
					"Error during code generation: For DateRanges only the comparison operators '==' and '!=' are allowed."
				);
			case VglOp.NE10:
			case VglOp.NE5:
			case VglOp.NE2:
			case VglOp.NE1:
				throw new Error(
					"Error during code generation: Invocation of a comparison with tolerance on DateRange parameters."
				);
			default:
				throw new Error("Error during code generation: Unknown comparison operator <" + op + ">.");
		}
	}

	public static vergleicheSTRING(op: VglOp, s1: string, s2: string): boolean {
		if (!s1 || !s2) {
			return false;
		}

		const result: number = s1.localeCompare(s2);
		switch (op) {
			case VglOp.NE:
				return result !== 0;
			case VglOp.EQ:
				return result === 0;
			case VglOp.GT:
			case VglOp.LT:
			case VglOp.GE:
			case VglOp.LE:
				throw new Error(
					"Error during code generation: For Strings only the comparison operators '==' and '!=' are allowed."
				);
			case VglOp.NE10:
			case VglOp.NE5:
			case VglOp.NE2:
			case VglOp.NE1:
				throw new Error(
					"Error during code generation: Invocation of a comparison with tolerance on String parameters."
				);
			default:
				throw new Error("Error during code generation: Unknown comparison operator <" + op + ">.");
		}
	}

	public static vergleiche(op: VglOp, s1: VkBigDecimal, s2: VkBigDecimal): ValidierungsErgebnis {
		if (
			s1 === undefined ||
			s2 === undefined ||
			s1.wertIstUngueltig ||
			s2.wertIstUngueltig ||
			s1.isNichtPruefungsrelevant ||
			s2.isNichtPruefungsrelevant
		) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		const value1WithDefaultScale = s1.setScale(
			VkBigDecimal.DEFAULT_SCALE,
			Big.roundHalfUp
		);
		const value2WithDefaultScale = s2.setScale(
			VkBigDecimal.DEFAULT_SCALE,
			Big.roundHalfUp
		);

		const result: number = value1WithDefaultScale.compareTo(value2WithDefaultScale);
		switch (op) {
			case VglOp.GT:
				if (result > 0) {
					if (s1.kannKleinerWerden || s2.kannGroesserWerden) {
						return ValidierungsErgebnis.TRUE_AF;
					}
					return ValidierungsErgebnis.TRUE_WF;
				}
				break;
			case VglOp.NE:
				if (result !== 0) {
					return BedingungsOperatorHelper._getFehlerFuerNE(result, s1, s2);
				}
				break;
			case VglOp.LT:
				if (result < 0) {
					if (s1.kannGroesserWerden || s2.kannKleinerWerden) {
						return ValidierungsErgebnis.TRUE_AF;
					}
					return ValidierungsErgebnis.TRUE_WF;
				}
				break;
			case VglOp.EQ:
				if (result === 0) {
					if (
						s1.kannKleinerWerden ||
						s2.kannKleinerWerden ||
						s1.kannGroesserWerden ||
						s2.kannGroesserWerden
					) {
						return ValidierungsErgebnis.TRUE_AF;
					}
					return ValidierungsErgebnis.TRUE_WF;
				}
				break;
			case VglOp.GE:
				if (result >= 0) {
					if (s1.kannKleinerWerden || s2.kannGroesserWerden) {
						return ValidierungsErgebnis.TRUE_AF;
					}
					return ValidierungsErgebnis.TRUE_WF;
				}
				break;
			case VglOp.LE:
				if (result <= 0) {
					if (s1.kannGroesserWerden || s2.kannKleinerWerden) {
						return ValidierungsErgebnis.TRUE_AF;
					}
					return ValidierungsErgebnis.TRUE_WF;
				}
				break;
			case VglOp.NE10:
				if (
					value1WithDefaultScale
						.subtract(value2WithDefaultScale)
						.abs()
						.compareTo(new VkBigDecimal(10)) > 0
				) {
					return BedingungsOperatorHelper._getFehlerFuerNE(result, s1, s2);
				}
				break;
			case VglOp.NE5:
				if (
					value1WithDefaultScale
						.subtract(value2WithDefaultScale)
						.abs()
						.compareTo(new VkBigDecimal(5)) > 0
				) {
					return BedingungsOperatorHelper._getFehlerFuerNE(result, s1, s2);
				}
				break;
			case VglOp.NE2:
				if (
					value1WithDefaultScale
						.subtract(value2WithDefaultScale)
						.abs()
						.compareTo(new VkBigDecimal(2)) > 0
				) {
					return BedingungsOperatorHelper._getFehlerFuerNE(result, s1, s2);
				}
				break;
			case VglOp.NE1:
				if (
					value1WithDefaultScale
						.subtract(value2WithDefaultScale)
						.abs()
						.compareTo(new VkBigDecimal(1)) > 0
				) {
					return BedingungsOperatorHelper._getFehlerFuerNE(result, s1, s2);
				}
				break;
			default:
				throw new Error("Error during code generation: Unknown comparison operator <" + op + ">.");
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	private static _getFehlerFuerNE(
		result: number,
		s1: VkBigDecimal,
		s2: VkBigDecimal
	): ValidierungsErgebnis {
		if (result < 0) {
			if (s1.kannGroesserWerden || s2.kannKleinerWerden) {
				return ValidierungsErgebnis.TRUE_AF;
			}
			return ValidierungsErgebnis.TRUE_WF;
		} else {
			// result > 0
			if (s1.kannKleinerWerden || s2.kannGroesserWerden) {
				return ValidierungsErgebnis.TRUE_AF;
			}
			return ValidierungsErgebnis.TRUE_WF;
		}
	}

	/**
	 * Gibt die Differenz zwischen zwei Datumsangaben zurück. Die Differenz wird berechnet entweder
	 * - in Jahren, oder
	 * - in Monaten, oder
	 * - in Tagen
	 *
	 * Kleinere Zeitangaben (Minuten, Sekunden etc.) bleiben bei der Differenzbildung
	 * unberücksichtigt. Liegt das erste Datum nach dem zweiten Datum, so wird die negative
	 * Differenz zurückgegeben.
	 *
	 * @param date1	erstes Datum
	 * @param date2	zweites Datum
	 * @param calendarField einer der strings "year", "month", "day"
	 * @return	die Differenz
	 */
	public static getDatumsDiff(date1: VkDate, date2: VkDate, calendarField: string, timeZone: string): VkBigDecimal {
		const nichtPruefungsrelevant: boolean =
			date1.isNichtPruefungsrelevant || date2.isNichtPruefungsrelevant;
		const kannGroesserWerden: boolean = date1.isNichtAngegeben || date2.isNichtAngegeben;
		const kannKleinerWerden: boolean = date1.isNichtAngegeben || date2.isNichtAngegeben;

		if (nichtPruefungsrelevant) {
			return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
		}

		let value: number;
		if (date1.date === undefined || date2.date === undefined) {
			value = VkBigDecimal.DEFAULT_VALUE;
		} else {
			const moment1: TimeZonedDate = TimeZonedDate.createFromDate(date1.date, timeZone);
			const moment2: TimeZonedDate = TimeZonedDate.createFromDate(date2.date, timeZone);
			value = BedingungsOperatorHelper._getCalendarDiff(moment1, moment2, calendarField);
		}
		return new VkBigDecimal(value, nichtPruefungsrelevant, kannGroesserWerden, kannKleinerWerden);
	}

	private static _getCalendarDiff(
		date1: TimeZonedDate,
		date2: TimeZonedDate,
		calendarField: string,
	): number {
		let datum1: TimeZonedDate;
		let datum2: TimeZonedDate;
		let multiplikator = 1;
		if (date1.isBefore(date2)) {
			datum1 = date1;
			datum2 = date2;
		} else {
			datum1 = date2;
			datum2 = date1;
			multiplikator = -1;
		}

		if (calendarField === "month") {
			return multiplikator * BedingungsOperatorHelper._getDifferenzInMonaten(datum1, datum2);
		}
		if (calendarField === "year") {
			return multiplikator * BedingungsOperatorHelper._getDifferenzInJahren(datum1, datum2);
		}
		if (calendarField === "hour") {
			return multiplikator * BedingungsOperatorHelper._getDifferenceInHours(datum1, datum2);
		}
		if (calendarField === "minute") {
			return multiplikator * BedingungsOperatorHelper._getDifferenceInMinutes(datum1, datum2);
		}
		if (calendarField === "second") {
			return multiplikator * BedingungsOperatorHelper._getDifferenceInSeconds(datum1, datum2);
		}

		return multiplikator * BedingungsOperatorHelper._getDifferenzInTagen(datum1, datum2);
	}

	/**
	 * Gibt die Differenz zwischen zwei Datumswerten in Jahren zurück.
	 * Dabei wird angenommen, dass datum1 nicht nach datum2 liegt.
	 */
	private static _getDifferenzInJahren(datum1: TimeZonedDate, datum2: TimeZonedDate): number {
		// berechne zunächst die grobe Jahresdifferenz; da datum2 nicht mehr vor datum1 liegen kann,
		// ist dieser Wert immer nicht-negativ
		// TODO: return DateUtil.diff(datum2, datum1, "year");
		const diff: number = datum2.year() - datum1.year();
		const tmp: TimeZonedDate = BedingungsOperatorHelper._addiereJahre(datum1, diff);
		if (tmp.isAfter(datum2)) {
			// eins zu weit gezählt
			return diff - 1;
		}
		return diff;
	}

	private static _addiereJahre(datum: TimeZonedDate, jahre: number): TimeZonedDate {
		let result: TimeZonedDate = datum.add(jahre, "year");

		if (datum.isTwentyEigthFebruary() && result.isTwentyEigthFebruary()) {
			if (!datum.isInLeapYear() && result.isInLeapYear()) {
				/*
				 * Wir wollen 28.2.1999 + 1 Jahr = 29.2.2000, Calendar macht aber 28.2.2000; das wird hier
				 * korrigiert
				 */
				result = result.add(1, "day");
			} else if (datum.isInLeapYear() && !result.isInLeapYear()) {
				/*
				 * Da 2000 ein Schaltjahr ist, wollen wir 28.2.2000T00:00:01 + 1 Jahr =
				 * 28.2.2001T00:00:00,
				 * Calendar macht aber 28.2.2001T00:00:01; das wird hier korrigiert
				 */
				result = result.clearTime();
			}
		}
		return result;
	}

	/**
	 * Gibt die Differenz zwischen zwei Datumswerten in Monaten zurück.
	 * Dabei wird angenommen, daß datum1 nicht nach datum2 liegt.
	 */
	private static _getDifferenzInMonaten(datum1: TimeZonedDate, datum2: TimeZonedDate): number {
		// Die Monatsdifferenz wird über Jahre*12 berechnet
		const jahreDiff: number = BedingungsOperatorHelper._getDifferenzInJahren(datum1, datum2);
		let monthDiff: number = 12 * jahreDiff;
		/*eslint no-constant-condition: ["error", { "checkLoops": false }]*/
		while (true) {
			// addiere zu datum1 so viele Monate, daß es später als datum2 ist
			// Monate sollen nicht einzeln hochgezählt werden, da es zu Fehlern führt:
			// (z.B. 31.1.2008 + 1 Monat + 1 Monat = 29.2.2008 + 1 Monat = 29.3.2008, aber 31.1.2008 + 2 Monate = 31.3.2008)
			// deshalb wird immer auf einer "frischen" Kopie von datum1 gerechnet und die bisher berechnete
			// Mindestdifferenz dazuaddiert
			const tmp: TimeZonedDate = BedingungsOperatorHelper._addiereMonate(datum1, monthDiff);
			if (tmp.isAfter(datum2)) {
				return monthDiff - 1; // eins zu weit gezählt, ziehe es wieder ab
			}
			monthDiff++;
		}
	}

	private static _addiereMonate(datum: TimeZonedDate, monate: number): TimeZonedDate {
		let result: TimeZonedDate = datum.add(monate, "month");
		if (
			datum.isTwentyEigthFebruary() && datum.isInLeapYear() &&
			result.isTwentyEigthFebruary() && !result.isInLeapYear()
		) {
			/*
			 * Da 2000 ein Schaltjahr ist, wollen wir 28.2.2000T00:00:01 + 1 Jahr =
			 * 28.2.2001T00:00:00,
			 * Calendar macht aber 28.2.2001T00:00:01; das wird hier korrigiert
			 */
			result = result.clearTime();
		}
		return result;
	}

	/**
	 * Gibt die Differenz zwischen zwei Datumswerten in Tagen zurück.
	 * Dabei wird angenommen, daß datum1 nicht nach datum2 liegt.
	 */
	private static _getDifferenzInTagen(datum1: TimeZonedDate, datum2: TimeZonedDate): number {
		const jahreDiff: number = BedingungsOperatorHelper._getDifferenzInJahren(datum1, datum2);
		let tageDiff: number = jahreDiff * 365;
		datum1 = datum1.add(tageDiff, "day");
		while (!datum1.isAfter(datum2)) {
			datum1 = datum1.add(1, "day");
			tageDiff++;
		}
		return tageDiff - 1; // 1 zu weit gezählt, da beim Schleifenabbruch
		// datum1 zum ersten mal nach datum2 lag
	}

	private static _getDifferenceInHours(datum1: TimeZonedDate, datum2: TimeZonedDate): number {
		return datum2.diff(datum1, "hour");
	}

	private static _getDifferenceInMinutes(datum1: TimeZonedDate, datum2: TimeZonedDate): number {
		return datum2.diff(datum1, "minute");
	}

	private static _getDifferenceInSeconds(datum1: TimeZonedDate, datum2: TimeZonedDate): number {
		return datum2.diff(datum1, "second");
	}

	public static addTime(date: VkDate, amount: VkBigDecimal, calendarField: UnitOfTime, timeZone: string): VkDate {
		const nichtPruefungsrelevant: boolean =
			date.isNichtPruefungsrelevant || amount.isNichtPruefungsrelevant;
		const nichtAngegeben: boolean =
			date.isNichtAngegeben || amount.kannGroesserWerden || amount.kannKleinerWerden;
		if (nichtPruefungsrelevant) {
			return VkDate.NON_RELEVANT_DATE;
		} else if (date.date === undefined || amount.wertIstUngueltig) {
			return new VkDate(undefined, nichtAngegeben, nichtPruefungsrelevant);
		} else {
			const moment1: TimeZonedDate = TimeZonedDate.createFromDate(date.date, timeZone);
			const result: TimeZonedDate = BedingungsOperatorHelper._addTimeInternal(
				moment1,
				amount.intValue(),
				calendarField
			);
			return new VkDate(result.toDate(), nichtAngegeben, nichtPruefungsrelevant);
		}
	}

	private static _addTimeInternal(date: TimeZonedDate, amount: number, calendarField: UnitOfTime) {
		if (calendarField === "year") {
			return BedingungsOperatorHelper._addiereJahre(date, amount);
		} else if (calendarField === "month") {
			return BedingungsOperatorHelper._addiereMonate(date, amount);
		} else {
			return date.add(amount, calendarField);
		}
	}

	/**
	 * Gibt Tag, Monat, Jahr, Stunden, Minuten oder Sekunden eines Datums zurück.
	 *
	 * @param datum	das Datum, aus dem ein Teil extrahiert wird
	 * @param calendarField	einer der Werte "YEAR", "MONTH", "DAY", "HOUR", "MINUTE", "SECOND"
	 * @return der gesuchte Datumsteil
	 */
	public static getDatumsTeil(datum: VkDate, calendarField: string, timeZone: string): VkBigDecimal {
		const nichtPruefungsrelevant: boolean = datum.isNichtPruefungsrelevant;
		const kannGroesserWerden: boolean = datum.isNichtAngegeben;
		const kannKleinerWerden: boolean = datum.isNichtAngegeben;

		if (nichtPruefungsrelevant) {
			return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
		}

		let value: number;
		if (datum.date === undefined) {
			value = VkBigDecimal.DEFAULT_VALUE;
		} else {
			const cal: TimeZonedDate = TimeZonedDate.createFromDateAsUtc(datum.date, timeZone);
			if (calendarField === "month") {
				value = cal.month()
			} else if (calendarField === "year") {
				value = cal.year();
			} else if (calendarField === "day") {
				value = cal.date();
			} else if (calendarField === "hour") {
				value = cal.hour();
			} else if (calendarField === "minute") {
				value = cal.minute();
			} else if (calendarField === "second") {
				value = cal.second();
			} else {
				throw new Error("Calendar field '" + calendarField + "' must not be present.");
			}
		}
		return new VkBigDecimal(value, nichtPruefungsrelevant, kannGroesserWerden, kannKleinerWerden);
	}
}
