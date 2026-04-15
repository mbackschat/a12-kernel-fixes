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
package com.mgmtp.a12.kernel.core.utils.internal.enumTable;

/**
 *
 */
public enum CSVTableErrorType {
	CHARACTERS_NOT_ALLOWED, // Zeile %d, Spalte %d: Das %d.Zeichen des Wertes '%s' ist nicht erlaubt
	COLUMN_NAME_WRONG, // Der Spaltenname '%s' ist fehlerhaft.
	EMPTY_LINE, // Die Tabelle enhält in Zeile %d eine Leerzeile. Das ist nicht erlaubt.
	EMPTY_VALUE, // Zeile %d, Spalte %d: Es wurde kein Wert angegeben.
	ENUM_COLUMN_MISSING, // Für den Spaltennamen '%s' existiert die Basisspalte '%s' nicht.
	ENUM_VALUES_NOT_UNIQUE, // Zeile %d und Zeile %d haben ungültigerweise die gleichen Werte für die Enumerationsspalten.
	NUM_COLUMNS_DONT_MATCH, // Die Anzahl %d der Spalten der Zeile %d stimmt nicht mit der Anzahl %d der Headerspalten überein.
	UTF8_ENCODING_WRONG, // Die Tabellendaten müssen in UTF-8 kodiert sein.
	VALUE_TOO_LONG, // Zeile %d, Spalte %d: Es dürfen nicht mehr als 500 Zeichen pro Wert angegeben werden.
	XML_UI_VALUE_NOT_UNIQUE, // Für die Spalte %s gibt es für den Wert %s die unterschiedlichen Werte %s und %s für die Spalte %s
	EMPTY_TABLE, // Die Tabelle muss Daten enthalten.
}
