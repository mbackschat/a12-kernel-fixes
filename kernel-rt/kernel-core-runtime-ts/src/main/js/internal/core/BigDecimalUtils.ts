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
import type { RoundingMode } from "big.js";
import { Big } from "big.js";


import { FormatDefinitionZahl } from "../formatdef/FormatDefinitionZahl.js";
import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import { VkBigDecimal } from "../util/VkBigDecimal.js";
import type { VkString } from "../util/VkString.js";

export class BigDecimalUtils {
	public static makeDecimal(value: number | string): VkBigDecimal;
	public static makeDecimal(value: VkString, fd: IFormatDefinition): VkBigDecimal;
	public static makeDecimal(
		value: number | string | VkString,
		fd?: IFormatDefinition
	): VkBigDecimal {
		if (typeof value === "number") {
			return new VkBigDecimal(value);
		} else if (typeof value === "string") {
			const wert: string = value;
			if (!wert) {
				return new VkBigDecimal(0);
			}
			return new VkBigDecimal(wert.replace(/,/g, "."));
		} else {
			const wert: VkString = value;
			if (wert.isNichtPruefungsrelevant || !wert.hasValue) {
				const kannGroesserWerden: boolean = wert.isNichtAngegeben;
				let kannKleinerWerden: boolean = wert.isNichtAngegeben;
				const isEmpty = !value.hasValue;
				if (kannKleinerWerden && fd instanceof FormatDefinitionZahl) {
					kannKleinerWerden = fd.hatVorzeichen;
				}
				return new VkBigDecimal(
					0,
					wert.isNichtPruefungsrelevant,
					kannGroesserWerden,
					kannKleinerWerden,
					isEmpty
				);
			}

			return this.makeDecimalFromNonEmptyString(wert, fd);
		}
	}

	private static makeDecimalFromNonEmptyString(value: VkString, formatDefinition?: IFormatDefinition): VkBigDecimal {
		const stringValue = value.wert.replace(/,/g, ".")
		const bigDecimalValue = new VkBigDecimal(stringValue);
		let kannGroesserWerden = false;
		let kannKleinerWerden = false;
		if (value.isNichtAngegeben) {
			kannGroesserWerden = true;
			kannKleinerWerden = bigDecimalValue.compareTo(VkBigDecimal.ZAHL_0) > 0 || (formatDefinition instanceof FormatDefinitionZahl && formatDefinition.hatVorzeichen);
		}
		return new VkBigDecimal(stringValue, false, kannGroesserWerden, kannKleinerWerden);
	}

	public static abrunden(wert: VkBigDecimal, anzStellen: number): VkBigDecimal {
		const modus: RoundingMode = wert.greaterThanOrEqual(0)
			? VkBigDecimal.ROUND_DOWN
			: VkBigDecimal.ROUND_UP;
		return this.runden(wert, anzStellen, modus);
	}

	public static aufrunden(wert: VkBigDecimal, anzStellen: number): VkBigDecimal {
		const modus: RoundingMode = wert.greaterThanOrEqual(0)
			? VkBigDecimal.ROUND_UP
			: VkBigDecimal.ROUND_DOWN;
		return this.runden(wert, anzStellen, modus);
	}

	public static runden(d: VkBigDecimal, anzStellen: number, modus?: RoundingMode): VkBigDecimal {
		/*
		 * note: for internal calculations, we use an internal maximum number of decimal places, s.
		 * VkBigDecimal#INTERNAL_DECIMAL_PLACES
		 * before rounding down to a number of decimal digits, make a rounding using the default
		 * scale
		 * in this way we avoid rounding errors like "Abrunden(1/3 + 2/3) = 0"
		 */
		const valueWithDefaultScale: VkBigDecimal = d.setScale(
			VkBigDecimal.DEFAULT_SCALE,
			Big.roundHalfUp
		);
		const mode = modus === undefined ? VkBigDecimal.ROUND_HALF_UP : modus;
		return valueWithDefaultScale.setScale(anzStellen, mode);
	}

	// Kann nicht über ein single Feld abgehandelt werden,
	// weil im Falle von isEmpty 0 zurückgegeben wird und nicht unknown.
	static laenge(wert: VkString): VkBigDecimal {
		const kannGroesserWerden: boolean = wert.isNichtAngegeben;
		const kannKleinerWerden = false;
		return new VkBigDecimal(
			wert.length(),
			wert.isNichtPruefungsrelevant,
			kannGroesserWerden,
			kannKleinerWerden
		);
	}

	static absOp(operator: VkBigDecimal): VkBigDecimal {
		return operator.abs();
	}

	/**
	 * Erzeugt für einen {@link VkBigDecimal}-Wert einen Wert mit maximal maxPrecision Stellen.
	 * Dient zur Ausgabe eines fehlerhaften Werts.
	 *
	 * @param wert	der Wert.
	 * @param maxPrecision	die maximale Zahl von Stellen.
	 * @return eine Stringrepräsentation des Wertes.
	 */
	public static toStringWertLaengenBeschraenkt(wert: VkBigDecimal, maxPrecision: number): string {
		return wert.toStringWithMaxPrecision(maxPrecision);
	}
}
