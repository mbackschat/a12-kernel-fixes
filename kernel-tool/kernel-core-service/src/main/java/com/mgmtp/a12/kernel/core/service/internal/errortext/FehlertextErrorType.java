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
package com.mgmtp.a12.kernel.core.service.internal.errortext;

/**
 *
 */
public enum FehlertextErrorType {

	/*
	 * Dieser Fehler wird nur bei der Codegenerierung verwendet. Für das Speichern einer Regel oder
	 * den Import wird kein Fehlertext benötigt.
	 */
	RULE_HAS_NO_ERRORTEXT, // "Regel '%s' spezifiziert für die Sprache %s keinen Fehlertext."

	/*
	 * Präfix für die folgenden Fehlermeldungen
	 */
	RULE_ERRORTEXT_WRONG, // "Regel '%s' spezifiziert für die Sprache %s einen fehlerhaften Fehlertext. "

	WRONG_ENCODING, // "Fehlertext enthält ungültige Zeichen. Nur %s-Zeichen sind erlaubt."
	INVALID_LINEBREAK, // "Fehlertext enthält einen Zeilenumbruch."
	WRONG_NUMBER_OF_DOLLARS, // "Fehlertext enthält eine unzulässige Anzahl von $ Zeichen."
	INVALID_CHARACTERS, // "Fehlertext enthält ungültige Zeichen. Zeilenumbrüche und Steuerzeichen sind nicht erlaubt."
	INVALID_CONDITION, // "Fehlertext mit einem Parameter kann nur bei Regeln mit gültigen Regelbedingungen geprüft werden. Da die Regelbedingung ungültig ist, konnte der Fehlertext nicht geprüft werden."

	NO_BASE_YEAR, // "Fehlertext: Die Verwendung von Referenzjahr ist nur für Modelle mit Referenzjahr zulässig."
	INVALID_PARAMETER, // "Fehlertext-Parameter '%s' ist ungültig."
	INVALID_PATH_SPEC, // "Fehlertext: Fehlerparameter '%s' darf keinen Pfadnamen verwenden."

	INVALID_PATH_SPEC_IN_PARAMETER, // "Parameter '%s' darf keinen Pfadnamen verwenden."

	INVALID_ROOT_GROUP, // "Vordruck '%s' nicht gültig."
	INVALID_ENTITY, // "Kontext oder Feld '%s' nicht gültig."
	INVALID_FIELD, // "Das referenzierte Feld '%s' wird falsch referenziert oder es wird in der Regelbedingung nicht verwendet."
	INVALID_FIELD_IN_PARAMETER, // "Parameter '%s' referenziert einen ungültigen Feldnamen oder das Feld wird falsch referenziert."
	INVALID_FIELD_Z, // "Das referenzierte Feld '%s' hat nur maximal %d Zeilen."
	INVALID_FIELD_L, // "Der Vordruck des referenzierten Feldes '%s' hat nur eine max. lfd. Nr. von %d."
	PARAM_INVALID_IN_NEW_WORLD, // "Der Parameter '%s' darf in A12-DVs nicht verwendet werden."
	ITER_PARAM_WITH_STARREFERENCE, // "Parameter '%s' nicht erlaubt, da Kontext '%s' in der Bedingung nicht ohne Stern referenziert wird."
	INVALID_GROUP, // "Fehlertext: '%s' spezifiziert ungültigen Kontextnamen '%s'."
	PARAM_VALUE_WITH_STARREFERENCE, // "Parameter '%s' nicht erlaubt, da Feld '%s' in der Bedingung nicht ohne Stern referenziert wird."

	RULEGROUP_IMMEDIATELY_BELOW_ROOT, // "Der Parameter '#RegelKontext' ist in Regeln direkt unterm Vordruck nicht erlaubt."

	NO_U_ITERATION, // "Der Parameter '#Usb' ist nicht erlaubt, da die Regelbedingung nicht über den USB iteriert."
	NO_Z_ITERATION, // "Der Parameter '#Zeile' ist nicht erlaubt, da die Regelbedingung nicht über die Zeile iteriert."

	NO_VORDRUCKZEILE, // "Parameter '%s' referenziert ein Feld, das keine Vordruckzeile spezifiziert."

	INVALID_NO_VALUE_VALIDATION, // Feld '%s' definiert 'Keine Werteprüfung' und deswegen ist der Zugriff auf seinen Wert nicht erlaubt.

	NO_CATEGORY, // Der Name der Kategorie für Feld '%s' darf nicht leer sein.
	FIELD_NO_ENUM, // Feld '%s' ist nicht vom Typ Enumeration und daher darf kein Zugriff auf Kategorienwerte spezifiziert werden.
	INVALID_CATEGORY, // Kategorie '%s' für Feld '%s' ist unbekannt.

	FIELD_ERRORTEXT_WRONG, // Feld '%s' spezifiziert für die Sprache %s einen fehlerhaften Fehlertext:
	ILLEGAL_SEMANTIC_INDEX, // Ungültige Spezifikation eines semantischen Index '%s'.
	MVK_INDEX_FOR_ERROR_TEXT_INVALID, // Ein semantischer Index darf in einem Fehlertext nur verwendet werden, wenn er auch in der Regelbedingung verwendet wird. Fehlerhaft: %s.
	INVALID_USE_OF_SEMANTIC_INDEX, //Spezifikation des semantischen Index '%s' ist nicht zulässig, da der Fehler auch auftreten kann, wenn kein Feld für die spezifizierte Wiederholung angegeben ist.
	INVALID_USE_OF_ITERATION_PARAM, //Der Parameter '%s' ist nicht zulässig, da für die Ebene parallele Iteration spezifiziert wurde und der Fehler auch auftreten kann, wenn kein Feld für die spezifizierte Wiederholung angegeben ist.
	INVALID_HASH_LFDNR_FOR_PARALLEL_ITERATION, //Der Parameter '%s' ist nicht zulässig, da in der Bedingung eine parallele Iteration spezifiziert wurde. Bitte verwenden Sie ggf. den Parameter '%s'.

	INVALID_INDEX_FIELD, // Das referenzierte Feld in Parameter '%s' ist kein Indexfeld, wird falsch referenziert oder es wird in der Regelbedingung nicht verwendet.
	INVALID_INDEX_FIELD_PARAMETER, // Der Indexfeld-Parameter '%s' greift weder auf den Wert noch auf den Kategoriewert zu.
	INVALID_INDEX_FIELD_NONREP_ENUM, // Das auf einem nicht-wiederholbaren Kontext liegende referenzierte Indexfeld '%s' muss eine Enumeration sein, genau einen legalen Wert haben und dieser muss als S-Wert gesetzt sein.
	INVALID_INDEX_FIELD_REP, // Die referenzierten Indexfelder '%s' und '%s' müssen entweder beide wiederholbar sein und in der Regelbedingung muss darüber (parallel) iteriert werden oder keines der beiden darf wiederholbar sein.
	INVALID_INDEX_FIELD_REP_ITER, // Das wiederholbare Indexfeld '%s' darf nur referenziert werden, wenn darüber in der Regelbedingung nur (parallel) iteriert wird.
	INVALID_INDEX_FIELD_ENUM_XML_VALUES, // Die referenzierten Indexfelder '%s' und '%s' haben unterschiedliche XML-Werte oder deren Reihenfolge ist unterschiedlich.
	INVALID_INDEX_FIELD_ENUM_XML_TO_CAT, // Die referenzierten Indexfelder '%s' und '%s' haben unterschiedliche Zuordnungen zwischen XML- und Kategorie-Werten.
	INVALID_INDEX_FIELD_INCONSISTENT_TYPES, // Die referenzierten Indexfelder '%s' und '%s' müssen entweder beide vom Typ Enumeration sein oder keines.
	INVALID_INDEX_FIELD_ENUM_XML_TO_DISPLAY, // Die referenzierten Indexfelder '%s' und '%s' haben unterschiedliche Zuordnungen zwischen XML- und Oberflächen-Werten.
	INVALID_INDEX_FIELD_AMBIGUOUS, // Der Indexfeld-Parameter '%s' darf für '%s' nicht verwendet werden, da es auf dem Pfad des Fehlerfeldes mehrere Indexfelder mit diesem Kurznamen gibt.
	INVALID_INDEX_FIELD_COMPOSED_INDEX, // Der Indexfeld-Parameter '%s' darf für '%s' nicht verwendet werden, da der Index aus mehreren Feldern besteht.

	ERROR_TEXT_PARAMETER_TOO_MANY_PARENT_GROUPS,//Pfadangabe '%s': Die relative Pfadangabe enthält zu viele Aufwärts-Schritte.
	ERROR_TEXT_FIELDNAME_NOT_UNIQUE,//Der Feldname '%s' wird im Modell mehrfach verwendet. Daher reicht der Feldname nicht für die Referenz des Feldes. (Felder mit gleichem Namen: %s)

	ERROR_TEXT_PARAMETER, // Parameter
}
