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

public enum CalculationErrorType {
	MVK_ERROR_WRONG_CALCULATED_FIELD, // "Das berechnete Feld '%s' stimmt nicht mit dem Fehlerfeld der Regel überein."
	MVK_ERROR_RESTRICTED_CALCULATION_RULE, // "Die Iteration darf bei Berechnungsregeln nicht eingeschränkt werden."
	MVK_ERROR_SEMANTIC_INDEX_OR_CATEGORY_FOR_ERRORFIELD, // "Für das berechnete Feld darf kein semantischer Index und kein Kategoriezugriff spezifiziert werden."

	MVK_ERROR_REFERENCE_TO_CALCULATED_FIELD, // "Das berechnete Feld darf in Vorbedingungen und den Berechnungsfunktionen nicht verwendet werden."
	MVK_ERROR_ITERATION_UNCHECKED, // "Ist das berechnete Feld wiederholbar, muss für jede Teilberechnung sichergestellt werden, dass für die jeweilige Wiederholung auch andere Werte existieren."
	MVK_ERROR_INVALID_COMPARISON, // "Teilberechnungen müssen mit einem Ungleich- oder Toleranzvergleich des berechneten Feldes mit der Berechnungsfunktion enden."
	// Note: The constant 'MVK_ERROR_INVALID_ENUM_CALCULATION' is also referenced as a string in the parser
	MVK_ERROR_INVALID_ENUM_CALCULATION, // "Enumerationswerte dürfen nur aus Konstanten, Enumerationen oder Kategorien von Enumerationen zugewiesen werden, die zu den Enumerationswerten passen."
	MVK_ERROR_COMPARISON_WITH_OR, // "Der Berechnungsvergleich muss mit UND von der Vorbedingung getrennt werden."
	MVK_ERROR_EMPTY_PRECONDITION, // "Leere Vorbedingung hier nicht erlaubt."
	MVK_ERROR_INVALID_LANGUAGE_FEATURE, // "In Berechnungsregeln ist das Sprachkonstrukt '%s' nicht erlaubt."
	MVK_ERROR_INVALID_FIELD_GATE, // "Die Regelbedingung muss von der Form FeldAngegeben(<BerechnetesFeld>) UND ... sein."
	MVK_ERROR_WRONG_ROUNDING_MODE, // "Bei exakten Berechnungsvergleichen darf nur die Rundungsart 'Exakt' verwendet werden."
	MVK_ERROR_INVALID_FIELDTYPE, // "Bei Toleranzvergleichen mit Rundungsart 'Exakt' darf das berechnete Felde nur vom Typ 'Zahl' sein."

	MVK_ERROR_CALC_CYCLE, // Folgende Berechnete Felder bilden über die in Klammern angegeben Regeln verbunden einen Zyklus: %s.

	MVK_ERROR_INVALID_FIELDNAME, //Der Name des berechneten Feldes darf weder Punkte noch Doppelpunkte enthalten.

	MVK_CALCULATION_FIELD_AND_NO_CALCULATION_RULE, // Aus der Regel '%s' kann für Feld '%s' keine Berechnung abgeleitet werden. Feld '%s' wurde aber als berechnetes Feld markiert.

	MVK_ERROR_INDEX_AS_CALCULATED_FIELD, // "Das berechnete Feld '%s' darf kein Indexfeld sein."

	MVK_GROUP_AS_CALCULATED_FIELD, // "Das berechnete Feld '%s' darf kein Kontext sein."

	MVK_CALC_INVALID_ACTION_TYPE, // Eine Hinweis- oder Info-Regel darf nicht als Berechnungsregel geschaltet werden.

}
