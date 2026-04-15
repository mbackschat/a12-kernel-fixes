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
import { DIVISION_BY_ZERO_INVALID, ERROR_DURING_DATA_CHECK_EVAL, RuntimeFormalErrorEnum } from "../../keys.js";

import { ErrorLocalizationBundle } from "../ErrorLocalizationBundle.js";

const bundle = new ErrorLocalizationBundle("de");
export default bundle;

const set: typeof bundle.set = bundle.set.bind(bundle);

set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.KEIN_FEHLER], "");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_ONK],
	"Es sind nur ganzzahlige Werte erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKOVZ],
	"Es sind nur Werte vom Format '0$decimalSeparator$$zeros$' mit genau $numberOfDecimalPlaces$ Nachkommastellen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKPVZ],
	"Es sind nur Werte vom Format '0$decimalSeparator$$zeros$' oder '-0$decimalSeparator$$zeros$' mit genau $numberOfDecimalPlaces$ Nachkommastellen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_VNK],
	"Es sind nur Zahlen mit $numberOfDecimalPlaces$ bis $maxNumberOfDecimalPlaces$ Nachkommastellen erlaubt."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN], "Es sind nur Zahlen erlaubt.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STELLIGKEIT_ZU_LANG], "Es sind maximal 15 Stellen erlaubt.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ENTHAELT_VORZEICHEN], "Es sind nur Zahlen ohne Vorzeichen erlaubt.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ENTHAELT_PLUS],
	"Für den Wert 0 sowie positive Werte sind keine Vorzeichen erlaubt."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_HAT_DEZIMALTRENNER], "Es sind nur ganzzahlige Werte erlaubt.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_NULL_ENTHAELT_VORZEICHEN],
	"Für den Wert 0 sowie positive Werte sind keine Vorzeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Es sind nur Werte mit maximal $numberOfDigitsBeforeDecimalPoint$ Vorkommastellen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Es sind nur Werte mit maximal $numberOfDigitsBeforeDecimalPoint$ Vorkommastellen und einem optionalen negativen Vorzeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZONK],
	"Es sind nur Werte mit maximal $numberOfDigitsBeforeDecimalPoint$ Stellen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZONK],
	"Es sind nur Werte mit maximal $numberOfDigitsBeforeDecimalPoint$ Stellen und einem optionalen negativen Vorzeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_IST_NULL_OVZ],
	"Es sind nur Werte größer 0 erlaubt."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_IST_NULL], "Es sind nur Werte ungleich 0 erlaubt.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_HAT_FUEHRENDE_NULLEN],
	"Es sind nur Werte ohne führende Nullen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_KURZ],
	"Es sind nur Werte mit mindestens $minLength$ Zeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_LANG],
	"Es sind nur Werte mit maximal $maxLength$ Zeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_KLEIN],
	"Es sind nur Werte erlaubt, die größer oder gleich $minValue$ sind."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_GROSS],
	"Es sind nur Werte erlaubt, die kleiner oder gleich $maxValue$ sind."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_OVZ],
	"Volle Geldbeträge müssen als Ziffernfolge ohne Dezimaltrenner eingetragen werden. Negative Geldbeträge sind nicht zulässig."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_MVZ],
	"Volle Geldbeträge müssen als Ziffernfolge ohne Dezimaltrenner eingetragen werden. Negativen Geldbeträgen ist ein negatives Vorzeichen voranzustellen."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ENTHAELT_VORZEICHEN],
	"Unzulässiges Vorzeichen. Geldbeträge müssen ohne Vorzeichen eingetragen werden."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ENTHAELT_PLUS],
	"Unzulässiges Vorzeichen. Der Betrag 0 sowie positive Beträge müssen ohne Vorzeichen eingetragen werden."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_NULL_ENTHAELT_VORZEICHEN],
	"Unzulässiges Vorzeichen. Der Betrag 0 sowie positive Beträge müssen ohne Vorzeichen eingegeben werden."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Es sind nur Werte mit maximal $numberOfDigitsBeforeDecimalPoint$ Stellen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Es sind nur Werte mit maximal $numberOfDigitsBeforeDecimalPoint$ Stellen und einem optionalen negativen Vorzeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_HAT_FUEHRENDE_NULLEN],
	"Werte mit führenden Nullen sind nicht erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_KURZ],
	"Es sind nur Werte mit mindestens $minLength$ Zeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_LANG],
	"Es sind nur Werte mit maximal $maxLength$ Zeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_KLEIN],
	"Es sind nur Werte größer oder gleich $minValue$ erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_GROSS],
	"Es sind nur Werte kleiner oder gleich $maxValue$ erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_OVZ],
	"Geldbeträge müssen vom Format '0$decimalSeparator$00' mit genau 2 Nachkommastellen sein."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_MVZ],
	"Geldbeträge müssen vom Format '0$decimalSeparator$00' oder '-0$decimalSeparator$00' mit genau 2 Nachkommastellen sein."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ENTHAELT_VORZEICHEN],
	"Unzulässiges Vorzeichen. Geldbeträge müssen ohne Vorzeichen eingetragen werden."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ENTHAELT_PLUS],
	"Unzulässiges Vorzeichen. Der Betrag 0$decimalSeparator$00 sowie positive Beträge müssen ohne Vorzeichen eingetragen werden."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_NULL_ENTHAELT_VORZEICHEN],
	"Unzulässiges Vorzeichen. Der Betrag 0$decimalSeparator$00 sowie positive Beträge müssen ohne Vorzeichen eingegeben werden."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Es sind nur Werte mit maximal $numberOfDigitsBeforeDecimalPoint$ Vorkommastellen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Es sind nur Werte mit maximal $numberOfDigitsBeforeDecimalPoint$ Vorkommastellen und einem optionalen negativen Vorzeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_HAT_FUEHRENDE_NULLEN],
	"Es sind nur Werte ohne führende Nullen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_KURZ],
	"Es sind nur Werte mit mindestens $minLength$ Zeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_LANG],
	"Es sind nur Werte mit maximal $maxLength$ Zeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_KLEIN],
	"Es sind nur Werte größer oder gleich $minValue$ erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_GROSS],
	"Es sind nur Werte kleiner oder gleich $maxValue$ erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ZU_KURZ],
	"Es sind nur Werte mit mindestens $minLength$ Zeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ZU_LANG],
	"Es sind nur Werte mit maximal $maxLength$ Zeichen erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ENTHAELT_NEWLINE],
	"Es sind nur Werte ohne Zeilenumbrüche erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FELD_JA_NEIN],
	"Unzulässiges Zeichen: Es darf nur '$trueValue$' (für Ja) oder '$falseValue$' (für Nein) eingetragen werden."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FELD_JA],
	"Unzulässiges Zeichen: Es darf nur '$trueValue$' (für Ja) eingetragen werden."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_FORMAT], "Bitte geben Sie ein gültiges Datum $dateFormat$ ein.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM],
	"Bitte geben Sie ein gültiges Datum $dateFormat$ ein. Es sind nur Daten ab dem 16.10.1583 erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_ADDITIONAL_CHECK],
	"Bitte geben Sie ein gültiges Datum $dateFormat$ ein. Es sind nur Daten nach dem Jahr 1900 erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_TAG_FEHLER],
	"Bitte geben Sie ein gültiges Datum $dateFormat$ ein. '$dateString$' ist kein gültiges Datum, weil es den Tag '$dateStringDay$' im gewählten Monat nicht gibt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_FEHLER],
	"Bitte geben Sie ein gültiges Datum $dateFormat$ ein. '$dateStringMonth$' ist keine gültige Monatsangabe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_FEBRUAR_29_FEHLER],
	"Bitte geben Sie ein gültiges Datum $dateFormat$ ein. '$dateString$' ist keine gültige Tagesangabe, weil $dateStringYear$ kein Schaltjahr ist."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_FEHLT],
	"Es wurde '00' für den Monat spezifiziert, obwohl eine Monatsangabe erfolgen muss."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_FEHLT0],
	"Es wurde '0' für den Monat spezifiziert, obwohl eine Monatsangabe erfolgen muss."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_JAHR_FEHLT],
	"Es wurde '0000' für das Jahr spezifiziert, obwohl eine Jahresangabe erfolgen muss."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN],
	"Es wurde '00' für den Monat spezifiziert, daher muss '00' für den Tag spezifiziert werden."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN0],
	"Es wurde '0' für den Monat spezifiziert. Dies ist nicht zulässig."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_JAHROPTIONAL_MONAT_ODER_TAG_ANGEGEBEN],
	"Es wurde '0000' für das Jahr spezifiziert, daher muss '00' für den Tag und '00' für den Monat spezifiziert werden."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_0],
	"Es wurde '0' für den Monat spezifiziert, obwohl eine genaue Monatsangabe oder die Angabe '00' erfolgen muss."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_TAG_0],
	"Es wurde '0' für den Tag spezifiziert, obwohl eine genaue Tagesangabe oder die Angabe '00' erfolgen muss."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_TAG_0_STATT_00],
	"Es wurde '0' für den Tag spezifiziert, obwohl die Angabe '00' erfolgen muss."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_0_STATT_00],
	"Es wurde '0' für den Monat spezifiziert, obwohl die Angabe '00' erfolgen muss."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_FORMAT],
	"Bitte geben Sie einen gültigen Datumsbereich $timeIntervalFormat$ ein."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_NICHT_GUELTIG],
	"Bei Datumsbereichen muss das Start- vor dem End-Datum liegen. (Erforderliches Format: $timeIntervalFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM],
	"Start- bzw. End-Datum dürfen nicht vor dem 16.10.1583 liegen. (Erforderliches Format: $timeIntervalFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM_ADDITIONAL_CHECK],
	"Start- bzw. End-Datum dürfen nicht vor dem Jahr 1900 liegen. (Erforderliches Format: $timeIntervalFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_START_TAG_FEHLER],
	"Bitte geben Sie einen gültigen Datumsbereich $timeIntervalFormat$ ein. Startdatum: '$dateString$' ist kein gültiges Datum, weil es den Tag '$dateStringDay$' im gewählten Monat nicht gibt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_START_MONAT_FEHLER],
	"Bitte geben Sie einen gültigen Datumsbereich $timeIntervalFormat$ ein. Startdatum: '$dateStringMonth$' ist keine gültige Monatsangabe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_START_FEBRUAR_29_FEHLER],
	"Bitte geben Sie einen gültigen Datumsbereich $timeIntervalFormat$ ein. Startdatum: '$dateString$' ist keine gültige Tagesangabe, weil $dateStringYear$ kein Schaltjahr ist."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_END_TAG_FEHLER],
	"Bitte geben Sie einen gültigen Datumsbereich $timeIntervalFormat$ ein. Enddatum: '$dateString$' ist kein gültiges Datum, weil es den Tag '$dateStringDay$' im gewählten Monat nicht gibt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_END_MONAT_FEHLER],
	"Bitte geben Sie einen gültigen Datumsbereich $timeIntervalFormat$ ein. Enddatum: '$dateStringMonth$' ist keine gültige Monatsangabe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_END_FEBRUAR_29_FEHLER],
	"Bitte geben Sie einen gültigen Datumsbereich $timeIntervalFormat$ ein. Enddatum: '$dateString$' ist keine gültige Tagesangabe, weil $dateStringYear$ kein Schaltjahr ist."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_FM_TEXT],
	"Der Index der Wurzelgruppe ist ungültig."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_ZU_GROSS_FM_TEXT],
	"Der Index der Wurzelgruppe ist größer als der maximal erlaubte Index."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_ZEILE_FM_TEXT],
	"Die Zeilennummer ist ungültig."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_ZEILENNUMMER_ZU_GROSS_FM_TEXT],
	"Die Zeilenangabe ist größer als die maximal erlaubte Zeilenangabe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_WERT_ZU_LANG_FM_TEXT],
	"Der angegebene Wert überschreitet die maximal erlaubte Anzahl an Zeichen."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_FUEHRENDES_BLANK_FM_TEXT],
	"Es sind nur Werte erlaubt, die nicht mit einem Leerzeichen beginnen."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_NACHFOLGENDES_BLANK_FM_TEXT],
	"Es sind nur Werte erlaubt, die nicht mit einem Leerzeichen enden."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_FELD_UNBEKANNT],
	"Das Feld wurde im Modell nicht gefunden."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZEICHEN_NICHT_IM_ZEICHENSATZ_FM_TEXT],
	"Der angegebene Wert enthält ein oder mehrere ungültige Zeichen."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FALSCHE_ANZAHL_INDICES_FM_TEXT],
	"Für das Feld '$fieldPathName$' wurden $numberOfIndicesSpecified$ Indices spezifiziert, aber $numberOfIndicesExpected$ Indices erwartet."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.KONTEXTNUMMER_ZU_KLEIN_FM_TEXT],
	"Die Instanznummer für Gruppe '$groupName$' wurde mit $groupIndexSpecified$ spezifiziert, es sind aber nur Zahlen größer 0 erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_KONTEXTNUMMER_ZU_GROSS_FM_TEXT],
	"Die Instanznummer für Gruppe '$groupName$' wurde mit $groupIndexSpecified$ spezifiziert, es sind aber maximal $maxGroupIndex$ erlaubt."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.BERECHNUNG_WERT_FEHLER],
	"Der berechnete Wert ist für das Feld nicht gültig."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DECIMAL_SEPERATOR_INVALID],
	"Ungültiges Dezimaltrennzeichen."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.COMMA_PLURAL], "Kommata");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.POINT_PLURAL], "Punkte");
set(DIVISION_BY_ZERO_INVALID, "Division durch null ist nicht möglich.");
set(
	ERROR_DURING_DATA_CHECK_EVAL,
	"Es trat ein Fehler beim Auswerten der Datenprüfung auf: $error$"
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER], "Dieses Feld ist ein Pflichtfeld.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.INDEX_FELD_FEHLER],
	"Die Werte des Feldes müssen eindeutig sein."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ENUMERATION_DEFAULT_ERROR],
	"Der Wert ist nicht in der Aufzählung enthalten."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.SOLITARY_INDEX_FEHLER],
	"Sie haben angegeben, dass Sie Angaben für '$fieldName.Value$' machen möchten, haben aber außer der Angabe im Feld '$fieldName$' keine weiteren Angaben getätigt."
);
