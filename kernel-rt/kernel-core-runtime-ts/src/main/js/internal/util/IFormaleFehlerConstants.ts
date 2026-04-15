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
import { IResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

/**
 * Die Konstanten der formalen Fehler dürfen NICHT als Regelname oder
 * Fehlercode verwendet werden. Damit die Konstanten nicht mehrfach verwaltet
 * werden, müssen werden sie aus diesem täglich nach
 * Validation.Commons\src\validation\common\services\NameValidator.java
 * kopiert, damit Sie auch im Editor bekannt sind.
 */
export class IFormaleFehlerConstants {
	// Standard Regelname für die Formatüberprüfungen
	static get FORMALE_PRUEFUNG(): string {
		return IResult.FORMAL_VALIDATION_RULE_NAME;
	}

	// Standard Regelname für die Pflichtfeld-überprüfungen
	static get PFLICHTFELD_PRUEFUNG(): string {
		return "pflichtFeldPruefung";
	}
	static get PFLICHTFELD_NICHTVORHANDEN(): string {
		return "pflichtFeldNichtVorhanden";
	}

	static get ZEICHEN_NICHT_IM_ZEICHENSATZ(): string {
		return "ZeichenNichtImZeichensatz";
	}
	static get WERT_NUR_LEERZEICHEN(): string {
		return "wertNurLeerzeichen";
	}

	static get FEHLER_FALSCHE_ANZAHL_INDICES(): string {
		return "falscheAnzahlIndices";
	}

	// Fehlermeldung Feld unbekannt
	static get FEHLER_FELD_UNBEKANNT(): string {
		return "feldUnbekannt";
	}

	// Fehlermeldung Feld hat eine nicht erlaubte Größe
	static get FEHLER_FELD_LEER(): string {
		return "feldLeer";
	}
	static get FEHLER_WERT_ZU_LANG(): string {
		return "wertZuLang";
	}

	// Fehlermeldung Feld darf nicht mit BLANK beginnen
	static get FEHLER_FUEHRENDES_BLANK(): string {
		return "fuehrendesBlank";
	}

	// Fehlermeldung Feld darf nicht mit BLANK enden
	static get FEHLER_NACHFOLGENDES_BLANK(): string {
		return "nachfolgendesBlank";
	}

	// Fehlermeldung maxLaufendeNummer überschritten
	// Überprüfung ob übergebene laufende Nummer größer als erlaubte LfdNummer
	static get FEHLER_LAUFENDE_NR_ZU_GROSS(): string {
		return "zuGrosseLfdNummer";
	}
	static get FEHLER_LAUFENDE_NR_ZU_KLEIN(): string {
		return "zuKleineLfdNummer";
	}

	// Fehlermeldung maxZeile überschritten
	// Überprüfung ob übergebene Zeile größer als erlaubte Zeile
	static get FEHLER_ZEILENNUMMER_ZU_GROSS(): string {
		return "zuGrosseZeile";
	}
	static get FEHLER_ZEILENNUMMER_ZU_KLEIN(): string {
		return "zuKleineZeile";
	}

	static get FEHLER_KONTEXTNUMMER_ZU_KLEIN(): string {
		return "zuKleineKontextnummer";
	}
	static get FEHLER_KONTEXTNUMMER_ZU_GROSS(): string {
		return "zuGrosseKontextnummer";
	}

	// Fehlermeldungen für Zahlformate:
	// --------------------------------
	// Überprüfung ob der Wert dem Pattern "[+|-]?[0-9]+(,[0-9]+)?" entspricht
	static get FEHLER_ZAHL_MIT_UNGUELTIGEN_ZEICHEN(): string {
		return "zahlHatUngueltigeZeichen";
	}

	// Überprüfung ob die Länge der Zahl die angegebenen Länge in der Formatdefinition überschreitet
	static get FEHLER_ZAHL_ZU_LANG(): string {
		return "zahlZuLang";
	}
	// Überprüfung ob die Länge der Zahl die angegebenen minmiale Länge in der Formatdefinition unterschreitet
	static get FEHLER_ZAHL_ZU_KURZ(): string {
		return "zahlZuKurz";
	}
	// Überprüfung, ob die Stelligkeit einer Zahl größer ist, als die maximal mögliche Stelligket
	static get STELLIGKEIT_ZAHL_ZU_LANG(): string {
		return "stelligkeitZuLang";
	}

	// Zahl enthält ein Vorzeichen, obwohl ohne Vorzeichen definiert
	static get FEHLER_ZAHL_ENTHAELT_VORZEICHEN(): string {
		return "zahlMitVorzeichen";
	}

	// Zahl enthält ein Plus-Vorzeichen, obwohl ohne Plus-Vorzeichen definiert
	static get FEHLER_ZAHL_ENTHAELT_PLUS(): string {
		return "zahlMitPlus";
	}

	// Zahl ist null, obwohl ohne Null definiert
	static get FEHLER_ZAHL_IST_NULL(): string {
		return "zahlIstNull";
	}

	// Zahl ist null hat Vorzeichen
	static get FEHLER_NULL_ENTHAELT_VORZEICHEN(): string {
		return "zahlNullEnthaeltVorzeichen";
	}

	// Zahl hat führende Nullen obwohl ohne führende Nullen definiert
	static get FEHLER_ZAHL_HAT_FUEHRENDE_NULLEN(): string {
		return "zahlHatFuehrendeNullen";
	}

	// Zahl hat Dezimaltrenner obwohl ohne Nachkommastellen definiert
	static get FEHLER_ZAHL_HAT_DEZIMALTRENNER(): string {
		return "zahlHatDezimalTrenner";
	}

	// Zahl hat keinen Dezimaltrenner obwohl mit Nachkommastellen definiert
	static get FEHLER_ZAHL_OHNE_DEZIMALTRENNER(): string {
		return "zahlOhneDezimalTrenner";
	}

	// Zahl hat zuviele Vorkommastellen
	static get FEHLER_ZAHL_ZUVIELE_VORKOMMASTELLEN(): string {
		return "zahlZuvieleVorkommastellen";
	}

	// Zahl hat falsche Anzahl von Nachkommastellen, es wurde eine Bereich von Bis definiert
	static get FEHLER_ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN(): string {
		return "zahlFalscheAnzahlNachkommastellen";
	}

	// Zahl ist kleiner als der Minwert
	static get FEHLER_ZAHL_ZU_KLEIN(): string {
		return "zahlZuKlein";
	}

	// Zahl ist groesser als der Maxwert
	static get FEHLER_ZAHL_ZU_GROSS(): string {
		return "zahlZuGross";
	}

	// 	Fehlermeldungen für Stringformate:
	// 	--------------------------------
	// static get entspricht nicht dem vorgegebenen Muster
	static get FEHLER_STRING_FALSCHES_MUSTER(): string {
		return "stringFalschesMuster";
	}

	// static get ist zu lang
	static get FEHLER_STRING_ZU_LANG(): string {
		return "stringZuLang";
	}
	// static get ist zu kurz
	static get FEHLER_STRING_ZU_KURZ(): string {
		return "stringZuKurz";
	}
	static get FEHLER_STRING_MIT_NEWLINE(): string {
		return "stringMitNewline";
	}

	// 	Fehlermeldungen für JaNein Format:
	// 	--------------------------------
	// JaNeinFeld enthält einen anderen Wert als 1 oder 2
	static get FEHLER_FELD_JA_NEIN(): string {
		return "feldJaNeinFalsch";
	}

	// 	Fehlermeldungen für Ja Format:
	// 	--------------------------------
	// JaNeinFeld enthält einen anderen Wert als 1
	static get FEHLER_FELD_JA(): string {
		return "feldJaFalsch";
	}

	// Datum entspricht nicht dem definierten Datumsformat
	static get FEHLER_DATUM_FORMAT(): string {
		return "datumFormatFalsch";
	}

	// Datum ist zu früh (vor 1900 bzw. vor dem Gregorianischen Kalender) oder Teilbekanntes Datum falsch
	static get FEHLER_DATUM(): string {
		return "datumFalsch";
	}

	// 	Fehlermeldungen für DatumBereich Format:
	// 	--------------------------------

	// Der Datumsbereichtrenner fehlt oder ist an einer falschen Stelle (nicht zwischen den
	// Datumsformaten)
	static get FEHLER_DATUM_BEREICH_TRENNER_FEHLT(): string {
		return "datumBereichTrennerFehlt";
	}

	// Keine gültigen Datumsformate
	static get FEHLER_DATUM_BEREICH_FORMAT(): string {
		return "datumBereichFormatFalsch";
	}

	// Kein gültiges Datumsformat (Datum1 nicht vor Datum 2 und es wurde nicht die
	// Zusatzprüfung ausgeschlossen)
	static get FEHLER_DATUM_BEREICH_NICHT_GUELTIG(): string {
		return "datumBereichNichtGueltig";
	}

	// Ein Datum des Intervalls ist zu früh (vor 1900 bzw. vor dem Gregorianischen Kalender)
	static get FEHLER_DATUM_BEREICH_DATUM(): string {
		return "datumBereichDatumFalsch";
	}

	//  Der berechnete Wert passt nicht zu dem Feldtyp
	static get BERECHNUNG_WERT_FEHLER(): string {
		return "berechnungsWertFehler";
	}

	// Feld vom Typ Vordefiniert ist ungültig
	/**
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Not needed anymore, when IdNrVerifier is created properly.
	 */
	static get TEST_ID_NUMMER(): string {
		return "testIDNummer";
	}

	static get UNGUELTIGE_BIC(): string {
		return "ungueltigeBIC";
	}

	static get UNGUELTIGE_IBAN(): string {
		return "ungueltigeIBAN";
	}

	static get UNGUELTIGE_EMAIL(): string {
		return "ungueltigeEmail";
	}

	// mandatory field
	static get FEHLER_PFLICHTFELD(): string {
		return "mandatoryField";
	}

	static get FEHLER_PFLICHTKONTEXT(): string {
		return "mandatoryGroup";
	}

	// uniqueness constraint of index fields
	static get FEHLER_INDEXFELD(): string {
		return "uniqueIndex";
	}

	// auto generated rule: uniqueness constraint of the value in multi-select groups
	static get FEHLER_MULTI_SELECT(): string {
		return "uniqueValue";
	}

	// auto generated rule: solitary index in group
	static get FEHLER_SOLITARY_INDEX(): string {
		return "solitaryIndex";
	}
}
