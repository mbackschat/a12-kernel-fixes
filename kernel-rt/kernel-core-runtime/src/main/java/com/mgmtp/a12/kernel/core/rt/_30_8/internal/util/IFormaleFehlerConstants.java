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

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;

/**
 * <p>
 * Die Konstanten der formalen Fehler dürfen NICHT als Regelname oder
 * Fehlercode verwendet werden. Damit die Konstanten nicht mehrfach verwaltet
 * werden, müssen werden sie aus diesem I täglich nach
 * Validation.Commons\src\validation\common\services\NameValidator.java
 * kopiert, damit Sie auch im Editor bekannt sind.
 */
public interface IFormaleFehlerConstants {
	//Standard Regelname für die Formatüberprüfungen
	String FORMALE_PRUEFUNG = IResult.FORMAL_VALIDATION_RULE_NAME;

	//Standard Regelname für die Pflichtfeld-überprüfungen
	String PFLICHTFELD_PRUEFUNG = "pflichtFeldPruefung";
	String PFLICHTFELD_NICHTVORHANDEN = "pflichtFeldNichtVorhanden";

	String ZEICHEN_NICHT_IM_ZEICHENSATZ = "ZeichenNichtImZeichensatz";
	String WERT_NUR_LEERZEICHEN = "wertNurLeerzeichen";

	String FEHLER_FALSCHE_ANZAHL_INDICES = "falscheAnzahlIndices";

	//	Fehlermeldung Feld unbekannt
	String FEHLER_FELD_UNBEKANNT = "feldUnbekannt";

	//	Fehlermeldung Feld hat eine nicht erlaubte Größe
	String FEHLER_FELD_LEER = "feldLeer";
	String FEHLER_WERT_ZU_LANG = "wertZuLang";

	//	Fehlermeldung Feld darf nicht mit BLANK beginnen
	String FEHLER_FUEHRENDES_BLANK = "fuehrendesBlank";

	//	Fehlermeldung Feld darf nicht mit BLANK enden
	String FEHLER_NACHFOLGENDES_BLANK = "nachfolgendesBlank";

	//Fehlermeldung maxLaufendeNummer überschritten
	//Überprüfung ob übergebene laufende Nummer größer als erlaubte LfdNummer
	String FEHLER_LAUFENDE_NR_ZU_GROSS = "zuGrosseLfdNummer";
	String FEHLER_LAUFENDE_NR_ZU_KLEIN = "zuKleineLfdNummer";

	//	Fehlermeldung maxZeile überschritten
	//Überprüfung ob übergebene Zeile größer als erlaubte Zeile
	String FEHLER_ZEILENNUMMER_ZU_GROSS = "zuGrosseZeile";
	String FEHLER_ZEILENNUMMER_ZU_KLEIN = "zuKleineZeile";

	String FEHLER_KONTEXTNUMMER_ZU_KLEIN = "zuKleineKontextnummer";
	String FEHLER_KONTEXTNUMMER_ZU_GROSS = "zuGrosseKontextnummer";

	//Fehlermeldung maxUsb überschritten
	//Überprüfung ob übergebener USB größer als erlaubter USB
	String FEHLER_USB_ZU_GROSS = "zuGrosserUsb";
	String FEHLER_USB_ZU_KLEIN = "zuKleinerUsb";

	//Fehlermeldung kein USB angegeben, obwohl maxUsb nicht null
	String FEHLER_KEIN_USB_SPEZIFIZIERT = "keinUsbAngegeben";
	String FEHLER_USB_SPEZIFIZIERT = "usbAngegebenBeiMaxUsbNull";

	//Fehlermeldungen für Zahlformate:
	//--------------------------------
	//Überprüfung ob der Wert dem Pattern "[+|-]?[0-9]+(,[0-9]+)?" entspricht
	String FEHLER_ZAHL_MIT_UNGUELTIGEN_ZEICHEN = "zahlHatUngueltigeZeichen";

	//Überprüfung ob die Länge der Zahl die angegebenen Länge in der Formatdefinition überschreitet
	String FEHLER_ZAHL_ZU_LANG = "zahlZuLang";
	//Überprüfung ob die Länge der Zahl die angegebenen minmiale Länge in der Formatdefinition unterschreitet
	String FEHLER_ZAHL_ZU_KURZ = "zahlZuKurz";
	//Überprüfung, ob die Stelligkeit einer Zahl größer ist, als die maximal mögliche Stelligket
	String STELLIGKEIT_ZAHL_ZU_LANG = "stelligkeitZuLang";

	//Zahl enthält ein Vorzeichen, obwohl ohne Vorzeichen definiert
	String FEHLER_ZAHL_ENTHAELT_VORZEICHEN = "zahlMitVorzeichen";

	//Zahl enthält ein Plus-Vorzeichen, obwohl ohne Plus-Vorzeichen definiert
	String FEHLER_ZAHL_ENTHAELT_PLUS = "zahlMitPlus";

	//Zahl ist null, obwohl ohne Null definiert
	String FEHLER_ZAHL_IST_NULL = "zahlIstNull";

	//Zahl ist null hat Vorzeichen
	String FEHLER_NULL_ENTHAELT_VORZEICHEN = "zahlNullEnthaeltVorzeichen";

	//Zahl hat führende Nullen obwohl ohne führende Nullen definiert
	String FEHLER_ZAHL_HAT_FUEHRENDE_NULLEN = "zahlHatFuehrendeNullen";

	//Zahl hat Dezimaltrenner obwohl ohne Nachkommastellen definiert
	String FEHLER_ZAHL_HAT_DEZIMALTRENNER = "zahlHatDezimalTrenner";

	//Zahl hat keinen Dezimaltrenner obwohl mit Nachkommastellen definiert
	String FEHLER_ZAHL_OHNE_DEZIMALTRENNER = "zahlOhneDezimalTrenner";

	//Zahl hat zuviele Vorkommastellen
	String FEHLER_ZAHL_ZUVIELE_VORKOMMASTELLEN = "zahlZuvieleVorkommastellen";

	//Zahl hat falsche Anzahl von Nachkommastellen, es wurde eine Bereich von Bis definiert
	String FEHLER_ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN = "zahlFalscheAnzahlNachkommastellen";

	//Zahl ist kleiner als der Minwert
	String FEHLER_ZAHL_ZU_KLEIN = "zahlZuKlein";

	//Zahl ist groesser als der Maxwert
	String FEHLER_ZAHL_ZU_GROSS = "zahlZuGross";

	//	Fehlermeldungen für Stringformate:
	//	--------------------------------
	//String entspricht nicht dem vorgegebenen Muster
	String FEHLER_STRING_FALSCHES_MUSTER = "stringFalschesMuster";

	//String ist zu lang
	String FEHLER_STRING_ZU_LANG = "stringZuLang";
	//String ist zu kurz
	String FEHLER_STRING_ZU_KURZ = "stringZuKurz";
	String FEHLER_STRING_MIT_NEWLINE = "stringMitNewline";

	//	Fehlermeldungen für JaNein Format:
	//	--------------------------------
	//JaNeinFeld enthält einen anderen Wert als 1 oder 2
	String FEHLER_FELD_JA_NEIN = "feldJaNeinFalsch";

	//	Fehlermeldungen für Ja Format:
	//	--------------------------------
	//JaNeinFeld enthält einen anderen Wert als 1
	String FEHLER_FELD_JA = "feldJaFalsch";

	//Datum entspricht nicht dem definierten Datumsformat
	String FEHLER_DATUM_FORMAT = "datumFormatFalsch";

	//Datum ist zu früh (vor 1900 bzw. vor dem Gregorianischen Kalender) oder Teilbekanntes Datum falsch
	String FEHLER_DATUM = "datumFalsch";

	//	Fehlermeldungen für DatumBereich Format:
	//	--------------------------------

	//Der Datumsbereichtrenner fehlt oder ist an einer falschen Stelle (nicht zwischen den
	//Datumsformaten)
	String FEHLER_DATUM_BEREICH_TRENNER_FEHLT = "datumBereichTrennerFehlt";

	//Keine gültigen Datumsformate
	String FEHLER_DATUM_BEREICH_FORMAT = "datumBereichFormatFalsch";

	//Kein gültiges Datumsformat (Datum1 nicht vor Datum 2 und es wurde nicht die
	//Zusatzprüfung ausgeschlossen)
	String FEHLER_DATUM_BEREICH_NICHT_GUELTIG = "datumBereichNichtGueltig";

	//Ein Datum des Intervalls ist zu früh (vor 1900 bzw. vor dem Gregorianischen Kalender)
	String FEHLER_DATUM_BEREICH_DATUM = "datumBereichDatumFalsch";

	// Der berechnete Wert passt nicht zu dem Feldtyp
	String BERECHNUNG_WERT_FEHLER = "berechnungsWertFehler";

	//Feld vom Typ Vordefiniert ist ungültig
	/**
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Not needed anymore, when IdNrVerifier is created properly.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	String TEST_ID_NUMMER = "testIDNummer";

	// auto generated rule: mandatory field
	String FEHLER_PFLICHTFELD = "mandatoryField";

	// auto generated rule: mandatory group
	String FEHLER_PFLICHTKONTEXT = "mandatoryGroup";

	// auto generated rule: uniqueness constraint of index fields
	String FEHLER_INDEXFELD = "uniqueIndex";

	// auto generated rule: uniqueness constraint of the value in multi-select groups
	String FEHLER_MULTI_SELECT = "uniqueValue";

	// auto generated rule: solitary index in group
	String FEHLER_SOLITARY_INDEX = "solitaryIndex";
}
