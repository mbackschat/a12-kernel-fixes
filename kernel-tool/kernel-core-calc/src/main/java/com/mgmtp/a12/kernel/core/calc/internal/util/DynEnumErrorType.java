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

package com.mgmtp.a12.kernel.core.calc.internal.util;

public enum DynEnumErrorType {
	MVK_DYNENUM_INVALID_TYPE, // Das Zielfeld hat einen ungültigen Feldtyp. Es sind nur Felder vom Typ String ohne Einschränkung, vom Typ Zahl ohne Nachkommastellen oder Enumerationen erlaubt.
	MVK_ERROR_WRONG_DYNENUM_FIELD, // Das Zielfeld '%s' stimmt nicht mit dem Fehlerfeld der Regel '%s' überein
	MVK_DYNENUM_OLD_WORLD, // Dynamische Enumerationen sind in H-DVs nicht zulässig.
	MVK_DYNENUM_PARALLEL_ITERATION, // Parallele Iteration ist in Regeln für dynamische Enumerationen nicht erlaubt.
	MVK_DYNENUM_REPETITION_NOT_UNIQUE, // WiederholungNichtEindeutig darf in Regeln für dynamische Enumerationen nicht verwendet werden.
	MVK_DYNENUM_INVALID_GATE, // Der Aufbau der Regel für dynamische Enumerationen entspricht nicht der kanonischen Form. Nach einer optionalen Absicherung muss "FeldAngegeben(<Zielfeld>) Und " angegeben werden gefolgt von den Berechnungsoperationen getrennt durch "Und".
	MVK_INVALID_ITERATION, // Bei der Berechnung von dynamischen Enumerationen darf für das Zielfeld nur einfache Iteration verwendet werden. Mengenbildung oder semantischer Index sind nicht erlaubt. Die Iteration muss innerhalb der Regel konsistent sein.
	MVK_DYNENUM_INVALID_CALCOP, // Der Aufbau der Regel für dynamische Enumerationen entspricht nicht der kanonischen Form. Die einzelnen Berechnungsoperationen für die Werte bestehen aus einem Vergleich des Wertes des Fehlerfeldes mit der Berechnungsfunktion. Optional kann eine negative Vorbedingung mit "Oder" verknüpft vorangestellt werden.
	MVK_DYNENUM_INVALID_CALCOP_OP, // Der Aufbau der Regel für dynamische Enumerationen entspricht nicht der kanonischen Form. Das Zielfeld muss über "!=" mit der Berechnungsfunktion verglichen werden. Alternativ kann auch mittels WertNichtKonsistent die Konsistenz des Wertes geprüft werden.
	MVK_ERROR_DYNENUM_FIELD_WITH_CATEGORY, // Kategoriezugriffe auf dem Zielfeld sind derzeit für die Berechnung von dynamischen Enumerationen nicht erlaubt.
	MVK_DYNENUM_INVALID_FIELD_REFERENCE, // In Vorbedingungen und Berechnungsoperationen dürfen nur abgesicherte Operanden referenziert werden. '%s' ist kein abgesicherter Operand.
	MVK_DYNENUM_INDEX_FIELD, // Dynamische Enumerationen dürfen nicht für Indexfelder definiert werden.
}
