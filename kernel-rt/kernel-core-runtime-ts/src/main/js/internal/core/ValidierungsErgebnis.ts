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
import { ErrorType } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

/**
 * Modelliert die verschiedenen Ergebnistypen, die bei der Validierung einer
 * Bedingung auftreten können: true wegen Wertefehler, true wegen Auslassungsfehler
 * oder false (kein Fehler).
 */
export class ValidierungsErgebnis {
	/**
	 * Wertefehler
	 */
	static TRUE_WF = new ValidierungsErgebnis(true, ErrorType.VALUE_ERROR);
	/**
	 * Auslassungsfehler
	 */
	static TRUE_AF = new ValidierungsErgebnis(true, ErrorType.OMISSION_ERROR);
	/**
	 * Kein Fehler oder nicht prüfungsrelevant. Related: see DreiWertBool.java in the java runtime.
	 */
	static FALSE_OR_UNKNOWN = new ValidierungsErgebnis(false, undefined);

	private _booleanResult: boolean;
	private _fehlerTyp: ErrorType | undefined;

	constructor(b: boolean, typ?: ErrorType) {
		this._booleanResult = b;
		this._fehlerTyp = typ;
	}

	/**
	 * Liefert true, wenn es sich um einen Auslassungsfehler oder Wertfehler handelt,
	 * und false, falls kein Fehler vorliegt bzw das Ergebnis nicht prüfungsrelevant ist.
	 * <br>
	 * Umbenannt von castToBoolean(), um die Fehl-Vermutung zu vermeiden dass
	 * !(FeldAngegeben(f)).castToBoolean() === FeldNichtAngegeben(f).castToBoolean()
	 */
	public isKnownAndTrue(): boolean {
		return this._booleanResult;
	}

	/**
	 * Gibt den für die Schnittstelle verständlichen Fehlertyp zurück,
	 * falls es sich um einen Fehler handelt und undefined sonst.
	 */
	public get fehlerTyp(): ErrorType | undefined {
		return this._fehlerTyp;
	}

	/**
	 * Berechnet das ValidierungsErgebnis, das bei einer Und-Verknüpfung dieses
	 * Ergebnisses mit einem anderen entsteht. Grundlage ist folgende Wertetabelle:
	 * <table>
	 * 	<tr>
	 * 		<th>Und</th>
	 * 		<th>TRUE_WF</th>
	 * 		<th>TRUE_AF</th>
	 * 		<th>FALSE</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td><b>TRUE_WF</b></td>
	 * 		<td>TRUE_WF</td>
	 * 		<td>TRUE_AF</td>
	 * 		<td>FALSE</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td><b>TRUE_AF</b></td>
	 * 		<td>TRUE_AF</td>
	 * 		<td>TRUE_AF</td>
	 * 		<td>FALSE</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td><b>FALSE</b></td>
	 * 		<td>FALSE</td>
	 * 		<td>FALSE</td>
	 * 		<td>FALSE</td>
	 * 	</tr>
	 * </table>
	 */
	public combineUND(other: ValidierungsErgebnis): ValidierungsErgebnis {
		switch (other) {
			case ValidierungsErgebnis.FALSE_OR_UNKNOWN:
				return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			case ValidierungsErgebnis.TRUE_WF:
				return this; // da passiert nichts
			case ValidierungsErgebnis.TRUE_AF:
				if (this === ValidierungsErgebnis.FALSE_OR_UNKNOWN) {
					return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
				}
				return ValidierungsErgebnis.TRUE_AF;
			default:
				throw new Error("Invalid validation result type.");
		}
	}

	/**
	 * Berechnet das ValidierungsErgebnis, das bei einer Oder-Verknüpfung dieses
	 * Ergebnisses mit einem anderen entsteht. Grundlage ist folgende Wertetabelle:
	 * <table>
	 * 	<tr>
	 * 		<th>Oder</th>
	 * 		<th>TRUE_WF</th>
	 * 		<th>TRUE_AF</th>
	 * 		<th>FALSE</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td><b>TRUE_WF</b></td>
	 * 		<td>TRUE_WF</td>
	 * 		<td>TRUE_WF</td>
	 * 		<td>TRUE_WF</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td><b>TRUE_AF</b></td>
	 * 		<td>TRUE_WF</td>
	 * 		<td>TRUE_AF</td>
	 * 		<td>TRUE_AF</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td><b>FALSE</b></td>
	 * 		<td>TRUE_WF</td>
	 * 		<td>TRUE_AF</td>
	 * 		<td>FALSE</td>
	 * 	</tr>
	 * </table>
	 */
	public combineODER(other: ValidierungsErgebnis): ValidierungsErgebnis {
		switch (other) {
			case ValidierungsErgebnis.TRUE_WF: // Wertefehler in einem Oder-Zweig -> Wertefehler
				return ValidierungsErgebnis.TRUE_WF;
			case ValidierungsErgebnis.TRUE_AF:
				if (this === ValidierungsErgebnis.TRUE_WF) {
					return ValidierungsErgebnis.TRUE_WF;
				}
				return ValidierungsErgebnis.TRUE_AF;
			case ValidierungsErgebnis.FALSE_OR_UNKNOWN:
				return this; // da passiert nichts
			default:
				throw new Error("Invalid validation result type.");
		}
	}
}
