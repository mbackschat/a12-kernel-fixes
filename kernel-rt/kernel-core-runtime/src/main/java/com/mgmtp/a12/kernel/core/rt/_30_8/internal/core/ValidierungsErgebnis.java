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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.core;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;

/**
 * Modelliert die verschiedenen Ergebnistypen, die bei der Validierung einer
 * Bedingung auftreten können: true wegen Wertefehler, true wegen Auslassungsfehler
 * oder false (kein Fehler).
 */
public enum ValidierungsErgebnis {
	/**
	 * Wertefehler
	 */
	TRUE_WF(true, IResult.ErrorType.VALUE_ERROR),
	/**
	 * Auslassungsfehler
	 */
	TRUE_AF(true, IResult.ErrorType.OMISSION_ERROR),
	/**
	 * Kein Fehler oder nicht prüfungsrelevant. related {@link com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DreiWertBool}
	 */
	FALSE_OR_UNKNOWN(false, null);

	private boolean booleanResult;
	private IResult.ErrorType fehlerTyp;

	private ValidierungsErgebnis(boolean b, IResult.ErrorType typ) {
		this.booleanResult = b;
		this.fehlerTyp = typ;
	}

	/**
	 * Liefert true, wenn es sich um einen Auslassungsfehler oder Wertfehler handelt,
	 * und false, falls kein Fehler vorliegt bzw das Ergebnis nicht prüfungsrelevant ist.
	 * <br>
	 * Umbenannt von castToBoolean(), um die Fehl-Vermutung zu vermeiden dass
	 * !(FeldAngegeben(f)).castToBoolean() === FeldNichtAngegeben(f).castToBoolean()
	 */
	public boolean isKnownAndTrue() {
		return booleanResult;
	}

	/**
	 * Gibt den für die Schnittstelle verständlichen Fehlertyp zurück,
	 * falls es sich um einen Fehler handelt und null sonst.
	 */
	public IResult.ErrorType getFehlerTyp() {
		return fehlerTyp;
	}

	/**
	 * Berechnet das ValidierungsErgebnis, das bei einer Und-Verknüpfung dieses
	 * Ergebnisses mit einem anderen entsteht. Grundlage ist folgende Wertetabelle:
	 * <table border="1">
	 * <tr>
	 * <th>Und</th>
	 * <th>TRUE_WF</th>
	 * <th>TRUE_AF</th>
	 * <th>FALSE</th>
	 * </tr>
	 * <tr>
	 * <td><b>TRUE_WF</b></td>
	 * <td>TRUE_WF</td>
	 * <td>TRUE_AF</td>
	 * <td>FALSE</td>
	 * </tr>
	 * <tr>
	 * <td><b>TRUE_AF</b></td>
	 * <td>TRUE_AF</td>
	 * <td>TRUE_AF</td>
	 * <td>FALSE</td>
	 * </tr>
	 * <tr>
	 * <td><b>FALSE</b></td>
	 * <td>FALSE</td>
	 * <td>FALSE</td>
	 * <td>FALSE</td>
	 * </tr>
	 * </table>
	 */
	public ValidierungsErgebnis combineUND(ValidierungsErgebnis other) {
		//tempcobertura::0.75::tempcobertura
		switch (other) {
			case FALSE_OR_UNKNOWN:
				return FALSE_OR_UNKNOWN;
			case TRUE_WF:
				return this; // da passiert nichts
			case TRUE_AF:
				if (this == FALSE_OR_UNKNOWN) {
					return FALSE_OR_UNKNOWN;
				}
				return ValidierungsErgebnis.TRUE_AF;
			default:
				//tempcobertura::0.00::tempcobertura
				throw new IllegalArgumentException("Invalid validation result type.");
		}
	}

	/**
	 * Berechnet das ValidierungsErgebnis, das bei einer Oder-Verknüpfung dieses
	 * Ergebnisses mit einem anderen entsteht. Grundlage ist folgende Wertetabelle:
	 * <table border="1">
	 * <tr>
	 * <th>Oder</th>
	 * <th>TRUE_WF</th>
	 * <th>TRUE_AF</th>
	 * <th>FALSE</th>
	 * </tr>
	 * <tr>
	 * <td><b>TRUE_WF</b></td>
	 * <td>TRUE_WF</td>
	 * <td>TRUE_WF</td>
	 * <td>TRUE_WF</td>
	 * </tr>
	 * <tr>
	 * <td><b>TRUE_AF</b></td>
	 * <td>TRUE_WF</td>
	 * <td>TRUE_AF</td>
	 * <td>TRUE_AF</td>
	 * </tr>
	 * <tr>
	 * <td><b>FALSE</b></td>
	 * <td>TRUE_WF</td>
	 * <td>TRUE_AF</td>
	 * <td>FALSE</td>
	 * </tr>
	 * </table>
	 */
	public ValidierungsErgebnis combineODER(ValidierungsErgebnis other) {
		//tempcobertura::0.75::tempcobertura
		switch (other) {
			case TRUE_WF://Wertefehler in einem Oder-Zweig -> Wertefehler
				return TRUE_WF;
			case TRUE_AF:
				if (this == TRUE_WF) {
					return TRUE_WF;
				}
				return ValidierungsErgebnis.TRUE_AF;
			case FALSE_OR_UNKNOWN:
				return this; //da passiert nichts
			default:
				//tempcobertura::0.00::tempcobertura
				throw new IllegalArgumentException("Invalid validation result type.");
		}
	}
}
