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

/**
 * Eine Instanz der Klasse ValidierungArt zeigt an, ob es sich beim Validierungsprozess
 * um eine Teilvalidierung nach 3-Wert-Logik handelt. Diese Information wird in der Klasse MainValidatorController
 * verwaltet.
 * <p>
 * Im Gegensatz zu einer Vollvalidierung, muss bei einer Teilvalidierung nach 3-Wert-Logik an den entsprechenden
 * Stellen berücksichtigt werden, ob bei den Wiederholungsebenen (also die jeweiligen
 * Kontextebenen und Zeilen), jede Wiederholung prüfungsrelevant ist oder nicht. Diese Information
 * wird für jede Wiederholungsebene separat ermittelt.
 * </p>
 * <p>
 * Können einzelne Wiederholungsebenen gleichbehandelt werden, so können zum Beispiel Optimierungen
 * bzgl. einer Iteration über Indizes umgesetzt werden, welche dann nur im Bereich der tatsächlich
 * gesetzten Indizes stattfinden, anstatt dass bis zu allen per Definiton festgelegten Max-Indizes
 * iteriert werden muss. Die Gleichbehandlung bedeutet, dass durch Nichtbetrachtung der nicht
 * übergebenen Indizes kein anderes Validierungsergebnis erzeugt wird, als wenn jeweils über alle
 * Indizes iteriert werden würde.
 * </p>
 * <p>
 * In der Klasse ValidierungArt werden, falls es sich um eine Teilvalidierung nach 3-Wert-Logik handelt, für die
 * jeweiligen Ebenen Flags für eine Gleichbehandlung gespeichert, die bei einer Bewertung im
 * MainValidationController erzeugt, und an verschiedenen Stellen wiederum im
 * MainValidationController ausgewertet werden.
 * </p>
 * <p>
 * Es werden keine einzelnen Flags je Kontext einer Ebene vergeben, sondern es werden alle Kontexte
 * einer Verschachtelungstiefe zusammengefasst, und hierfür wird ein Flag zur Gleichbehandlung
 * vergeben.
 * </p>
 * <p>
 * Die letzte Ebene (Zeile) ist immer auf TRUE gesetzt.
 * </p>
 *
 */
export class ValidierungArt {
	/**
	 * Per default handelt es sich um eine Vollvalidierung, dann ist das folgende Array undefined.
	 * Das Array wird nur gesetzt (in der Methode setValidationPartWith3ValueLogic), wenn es sich um eine
	 * Teilvalidierung nach 3-Wert-Logik handelt.
	 *
	 * Enthält alle Flags für die Gleichbehandlung für alle Ebenen, mit:<br>
	 * - i = 0 -> lfdNr Vordruck,<br>
	 * - i = [1 bis n] -> Verschachtelungstiefe Kontexte<br>
	 * - i = n+1 -> Zeile
	 */
	private _gleichbehandlungPerEbene: boolean[] | undefined;

	/**
	 * Liefert den Wert des Flags für eine Gleichbehandlung der Ebene i. i == 0 ist die LfdNr etc.
	 *
	 * @param ebene entspricht i (s.o.)
	 */
	public istEbeneGleich(ebene: number): boolean {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		if (this.isVollvalidierung() || ebene >= this._gleichbehandlungPerEbene!.length - 1) {
			return true;
		}
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return this._gleichbehandlungPerEbene![ebene];
	}

	public setValidationPartWith3ValueLogic(gleichbehandlungPerEbene: boolean[]): void {
		this._gleichbehandlungPerEbene = gleichbehandlungPerEbene;
	}

	public isVollvalidierung(): boolean {
		return this._gleichbehandlungPerEbene == undefined;
	}

	public isValidationPartWith3ValueLogic(): boolean {
		return this._gleichbehandlungPerEbene != undefined;
	}
}
