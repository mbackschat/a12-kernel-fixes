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
package com.mgmtp.a12.kernel.core.parser.internal.error;

/**
 *
 */
public enum ParserErrorTypesEnumForHModel {

	MVK_INVALID_OPERATION_IN_H_MODEL, //"'%s' darf in H-DVs nicht verwendet werden."
	MVK_INVALID_USB, //"'%s' darf keinen USB spezifizieren."
	MVK_H_MODEL_AND_PATH, // "In H-DVs darf der Pfad des Feldes nicht mit angegeben werden."
	MVK_H_MODEL_AND_LSPEC_FOR_GROUP, // "'%s': Spezifikation der lfd. Nr. bei Kontexten nicht erlaubt."
	MVK_H_MODEL_INVALID_INDEX_SPEC, // "'%s': Spezifikation der '%s' ungültig."
	MVK_H_MODEL_INDEX_TOO_BIG, //  "'%s': %d größer als der maximal erlaubte Wert für %s (momentan %d)."
	MVK_H_MODEL_INVALID_ZITERATION, // "'%s': Angabe von '#Zeile' ist nur für Kontexte erlaubt. "
	MVK_ILLEGAL_RESTRICTION_OF_WILDCARD, //"'%s': Ungültige Einschränkung des Stern-Operators für %s."
	MVK_INVALID_WILDCARD_H_MODEL, //"'%s' ist für %s nicht wiederholbar,  daher darf für %s kein Stern spezifiziert werden."

	MVK_INVALID_USB_WILDCARD, //"Bei '%s' darf für den USB nicht '*' spezifiziert werden."
	MVK_INVALID_DATE_TYPE, //	Bei '%s' : Teilbekanntes Datum nicht erlaubt.  Fehlerhaft: '%s'.
	MVK_NO_Z_ITERATION, //	Bei '%s' darf nicht über die Zeile iteriert werden.
	MVK_ROOT_GROUP_REFERENCED_AS_GROUP, //"Für die Referenzierung des Vordrucks verwenden Sie bitte die Vordruck-Sprachkonstrukte."
	MVK_L_NO_MULT, //"'%s' darf nur verwendet werden für Vordrucke mit max. lfd. Nr. größer 1.  Für den USB und für lfd. Nr. dürfen keine Angaben gemacht werden."
	MVK_U_NO_MULT, //"'%s' darf nur verwendet werden für USB-Vordrucke mit max. USB größer 1. Für den USB und für lfd. Nr. dürfen keine Angaben gemacht werden."
	MVK_INVALID_L, // Bei '%s' darf keine laufende Nummer spezifiziert werden.
	MVK_NO_USB, //	'AnzahlAngegebeneUsb' darf nur verwendet werden,  wenn das Modell einen Vordruck mit USB verwendet.
	MVK_MISSING_Z_ITERATION, //"Bei '%s' muss für die Zeile Iteration spezifiziert werden. Fehlerhaft: '%s'.
	MVK_DIFFERENT_Z_MULT, //"Bei '%s' müssen alle Parameter den gleiche maximale Zeilenwiederholbarkeit haben. Fehlerhaft: '%s'.
	MVK_NO_Z_MULT, //"Bei '%s' müssen alle Parameter Zeilenwiederholbarkeit > 1 haben. Fehlerhaft: '%s'." },
	MVK_NO_Z_WILDCARD_WITHOUT_RESTRICTION, //"Bei '%s' müssen alle Parameter die Zeile mit Stern referenzieren (ohne Einschränkung). Fehlerhaft: '%s'."
	MVK_INVALID_LU_WILDCARD, //"Bei '%s' darf nur die Zeile mit Stern referenziert werden. Fehlerhaft: '%s'."
	MVK_UITERATION_INVALID, //"Bedingung darf nicht mit USB-Iteration spezifiziert werden. Für Details s. Leitfaden für Regelersteller."
	MVK_INVALID_Z_WILDCARD_FOR_GROUP, //"'%s': Bei einem Kontext darf die Zeile nicht mit Stern spezifiziert werden."

	MVK_INVALID_L_MULT_H_MODEL, //"Stern-Operation für die lfd. Nr. Vordruck muss in H-DVs mit einem Slash abgetrennt werden."

	MVK_INVALID_L_ITER, //"Für '%s' wurde Iteration für die laufende Nummer spezifiziert, obwohl das Fehlerfeld auf einem nicht wiederholbaren Vordruck liegt."
	MVK_INVALID_L_ITER2, //"Es wurde Iteration für die laufende Nummer spezifiziert, aber es wird weder das Fehlerfeld noch der Vordruck des Fehlerfeld mit LfdNr-Iteration referenziert."

	MVK_INVALID_U_ITER, //"	Für '%s' wurde Iteration für den USB spezifiziert, obwohl das Fehlerfeld nicht auf einem USB-Vordruck liegt."
	MVK_INVALID_Z_ITER, //"	Für '%s' wurde Iteration für die Zeile spezifiziert, obwohl das Fehlerfeld nicht mehrzeilig ist."

	MVK_DIFFERENT_Z_IN_ITER, //Die maximale Zeile von '%s' passt nicht zu der maximalen Zeile des Fehlerfeldes '%s'.
	MVK_DIFFERENT_L_IN_ITER, //Die maximale lfd. Nr. Vordruck von '%s' passt nicht zu der maximalen lfd. Nr. des Fehlerfeldes '%s'.

	MVK_DIFFERENT_Z, //"	Für '%s' wurde Iteration für die Zeile spezifiziert, obwohl dieser Kontext Felder mit unterschiedlichen Zeilenspezifikationen umfasst.
	MVK_INVALID_Z_FOR_GROUP, //"'%s': Die  Zeile wurde konkret spezifiziert, obwohl dieser Kontext Felder mit unterschiedlichen Zeilenspezifikationen umfasst.
	MVK_ERROR_FIELD_AND_ZITERATION_INVALID, //"	Für  Zeile wird in der Bedingung Iteration spezifiziert. Das Fehlerfeld '%s'  darf daher nicht nur über ein Vordruck-Sprachkonstrukt referenziert werden.

	MVK_ITERATION_RESTRICTION_AND_NO_ITERATION, // "In den Basisdaten wurde die Iteration für die %s auf %s-%s eingeschränkt, obwohl die Bedingung keine Iteration für die %s spezifiziert."
	MVK_ITERATION_RESTRICTION_INVALID_INTERVAL, // "\"Für %s\": Ungültiger Bereich."

	MVK_ITERATION_AND_ERRFIELD_INVALID, //Für '%s' wird in der Bedingung Iteration spezifiziert. Daher muss auch für '%s', in dem das Fehlerfeld liegt, eine entsprechende Iteration definiert werden, oder für die Regel muss ein anderes Fehlerfeld ausgewählt werden.""

	// getChangesAsSpecifiedByRules
	MVK_MAX_INDEX_RESTRICTION_TO_ONE, // "% wird unzulässig auf 1 eingeschränkt."

	MVK_NEG_CONDITION_IN_ITERATION_ZEILE, // Die Bedingung spezifiziert Iteration für die Zeile, aber die Regel kann für nicht angegebene Zeilen feuern.

	MVK_NEG_CONDITION_IN_ITERATION_LFDNR, // Die Bedingung spezifiziert Iteration für die laufende Nummer Vordruck, aber die Regel kann für nicht angegebene Vordruckwiederholungen feuern.
}
