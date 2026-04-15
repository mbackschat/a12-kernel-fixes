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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.util;

/**
 * Eine Instanz der Klasse ValidierungArt zeigt an, ob es sich beim Validierungsprozess
 * um eine Teilvalidierung nach 3-Wert-Logik handelt. Diese Information wird in der Klasse
 * MainValidatorController
 * verwaltet.
 * <p>
 * Im Gegensatz zu einer Vollvalidierung, muss bei einer Teilvalidierung nach 3-Wert-Logik an den
 * entsprechenden
 * Stellen berücksichtigt werden, ob bei den Wiederholungsebenen (also USB, die jeweiligen
 * Kontextebenen und Zeilen), jede Wiederholung prüfungsrelevant ist oder nicht. Diese Information
 * wird für jede Wiederholungsebene separat ermittelt.
 * </p>
 * <p>
 * <b>Beispiel1: (exemplarisch für Zeilen)</b><br>
 * Nur das mehrzeilige Feld A ist prüfungsrelevant. A hat maximale Zeilenzahl 5. Dazu wird das Feld
 * A mit dem Zeilenindex 1 übergeben. D.h. bei der Validierung ist nur die erste Zeile
 * prüfungsrelevant, die anderen Zeilen sind nicht prüfungsrelevant. In diesem Fall ist die
 * Teilvalidierungsart für Zeile nicht gleich, d.h. an der letzten Stelle der Liste ist FALSE
 * spezifiziert.<br>
 * Wird das Feld A mit Zeilenindex 0 übergeben so werden die Zeilen gleichbehandelt, d.h. die
 * Teilvalidierungsart für Zeile ist gleich, d.h. an der letzten Stelle der Liste ist TRUE
 * spezifiziert. Wird für das Feld A der A.1, A.2, A.3, A.4 A.5 als prüfungsrelevant spezifiziert,
 * so wird dies vom Algorithmus nicht erkannt.
 * </p>
 * <p>
 * <b>Beispiel2: (exemplarisch für Zeilen)</b><br>
 * Nur die mehrzeiligen Felder A und B sind prüfungsrelevant. A und B haben maximale Zeilenzahl 5.
 * Dazu wird das Feld A mit dem Zeilenindex 0 übergeben und B mit dem Zeilenindex 1.<br>
 * D.h. bei der Validierung ist für B nur die erste Zeile prüfungsrelevant. Für A sind zwar alle
 * Zeilen prüfungsrelevant, die Teilvalidierungsart wird aber für alle prüfungsrelevanten Felder
 * bestimmt. D.h. in diesem Fall ist die Teilvalidierungsart für Zeile nicht gleich, d.h. an der
 * letzten Stelle der Liste ist FALSE spezifiziert.
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
 * In der Klasse ValidierungArt werden, falls es sich um eine Teilvalidierung nach 3-Wert-Logik
 * handelt, für die
 * jeweiligen Ebenen Flags für eine Gleichbehandlung gespeichert, die bei einer Bewertung im
 * MainValidationController erzeugt, und an verschiedenen Stellen wiederum im
 * MainValidationController ausgewertet werden.
 * </p>
 * <p>
 * Es werden keine einzelnen Flags je Kontext einer Ebene vergeben, sondern es werden alle Kontexte
 * einer Verschachtelungstiefe zusammengefasst, und hierfür wird ein Flag zur Gleichbehandlung
 * vergeben.
 * </p>
 */
public class ValidierungArt {

	/**
	 * Per default handelt es sich um eine Vollvalidierung, dann ist das folgende Array null.
	 * Das Array wird nur gesetzt (in der Methode setValidationPartWith3ValueLogic), wenn es sich um
	 * eine
	 * Teilvalidierung nach 3-Wert-Logik handelt.
	 * <p>
	 * Enthält alle Flags für die Gleichbehandlung für alle Ebenen, mit:<br>
	 * - i = 0 -> USB,<br>
	 * - i = 1 -> lfdNr Vordruck,<br>
	 * - i = [2 bis n] -> Verschachtelungstiefe Kontexte<br>
	 * - i = n+1 -> Zeile
	 */
	private boolean[] gleichbehandlungPerEbene = null;

	/**
	 * Das letzte Element aus dem Array gleichbehandlungPerEbene sagt aus,
	 * ob auf der Ebene der Zeilen eine Gleichbehandlung spezifiziert ist.
	 */
	public boolean zeileGleich() {
		return (isVollvalidierung() || gleichbehandlungPerEbene[gleichbehandlungPerEbene.length - 1]);
	}

	/**
	 * Liefert den Wert des Flags für eine Gleichbehandlung der Ebene i. i == 0 ist der USB, i == 1
	 * die LfdNr etc.
	 *
	 * @param ebene
	 * 		, entspricht i (s.o.)
	 */
	public boolean istEbeneGleich(int ebene) {
		if (isVollvalidierung() || (ebene >= gleichbehandlungPerEbene.length - 1)) {
			return true;
		}
		return gleichbehandlungPerEbene[ebene];
	}

	public void setValidationPartWith3ValueLogic(boolean[] gleichbehandlungPerEbene) {
		this.gleichbehandlungPerEbene = gleichbehandlungPerEbene;
	}

	public boolean isVollvalidierung() {
		return (gleichbehandlungPerEbene == null);
	}

	public boolean isValidationPartWith3ValueLogic() {
		return (gleichbehandlungPerEbene != null);
	}
}
