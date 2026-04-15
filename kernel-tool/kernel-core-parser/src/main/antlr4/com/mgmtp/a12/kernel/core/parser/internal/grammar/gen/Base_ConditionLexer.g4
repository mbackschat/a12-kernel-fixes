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

lexer grammar Base_ConditionLexer;
options {
	superClass=AbstractConditionLexer;
}

// If adding new allowed characters, you have to add them to CommonUtils.LEGAL_CHARACTERS, too.

Comment: ';;'  ~('\r' | '\n')* (('\r' | '\n') (' '|'\r'|'\t'|'\u000C'|'\n')* ';;' ~('\r' | '\n')*)*;

ZiffernFolge 
	:	DIGIT+;

StringLiteral : '"' ( '""' | ~["] )* '"' ;

// escaping mechanism to allow for field names, which coincide with a keyword
// The lexer does not create keywords, but the quotes have to be removed externally.
NameOhnePunktMitQuotes
	:	'\'' STR (STR | DIGIT)* '\'' { this.setTextUnquotedInLexer(); };

///////////////////////////////////////////////////////////////////////////
ID: [a-zA-Z]+('1'|'2'|'5'|'10')? {this.getMultiLanguageHandler().setTypeInLexer(this);};

/////////////////////////////////////////////////////////////////////////////////////////////
ABRUNDEN: 'Abrunden';
ABRUNDEN_WERT: 'AbrundenWert';
ABS: 'Abs';
ABS_WERT: 'AbsWert';
ADDIERE_JAHRE: 'AddiereJahre';
ADDIERE_MONATE: 'AddiereMonate';
ADDIERE_TAGE: 'AddiereTage';
ADDIERE_STUNDEN: 'AddiereStunden';
ADDIERE_MINUTEN: 'AddiereMinuten';
ADDIERE_SEKUNDEN: 'AddiereSekunden';
AKTUELLE_VORDRUCK_NUMMER: 'AktuelleVordruckNummer';
AKTUELLE_WIEDERHOLUNG: 'AktuelleWiederholung';
ALLE_FELDER_ANGEGEBEN: 'AlleFelderAngegeben';
ALLE_KONTEXTE_ANGEGEBEN: 'AlleKontexteAngegeben';
ANZAHL_ANGEGEBENER_FELDER: 'AnzahlAngegebenerFelder';
ANZAHL_ANGEGEBENER_KONTEXTE: 'AnzahlAngegebenerKontexte';
ANZAHL_ANGEGEBENER_VORDRUCKE: 'AnzahlAngegebenerVordrucke';
ANZAHL_ANGEGEBENE_USB: 'AnzahlAngegebeneUsb';
ANZAHL_VERSCHIEDENER_WERTE: 'AnzahlVerschiedenerWerte';
ANZAHL_WERT_IN_FELDLISTE: 'AnzahlWertInFeldliste';
APPLIKATIONS_BEDINGUNG: 'ApplikationsBedingung';
AUFRUNDEN: 'Aufrunden';
AUFRUNDEN_WERT: 'AufrundenWert';
BEREICH_ALS_STRING: 'BereichAlsString';
BEREICH_ALS_ZAHL: 'BereichAlsZahl';
DATETIME: 'DateTime';
DATUM: 'Datum';
DATUM_BEREICH: 'DatumBereich';
DATUMS_BEREICHE_UEBERLAPPEND: 'DatumsBereicheUeberlappend';
DATUM_AUS_DATETIME: 'DatumAusDateTime';
DIFFERENZ_IN_JAHREN: 'DifferenzInJahren';
DIFFERENZ_IN_MINUTEN: 'DifferenzInMinuten';
DIFFERENZ_IN_MONATEN: 'DifferenzInMonaten';
DIFFERENZ_IN_SEKUNDEN: 'DifferenzInSekunden';
DIFFERENZ_IN_STUNDEN: 'DifferenzInStunden';
DIFFERENZ_IN_TAGEN: 'DifferenzInTagen';
ENDE_AUS_DATUM_BEREICH: 'EndeAusDatumBereich';
ERSTER_ANGEGEBENER_WERT: 'ErsterAngegebenerWert';
ERSTER_TAG: 'ErsterTag'; 
FALSCH: 'Falsch';
FELD_ANGEGEBEN: 'FeldAngegeben';
FELDER_NICHT_GEMEINSAM_ANGEGEBEN: 'FelderNichtGemeinsamAngegeben';
FELD_NICHT_ANGEGEBEN: 'FeldNichtAngegeben';
FELD_WERT_ALS_ZAHL: 'FeldWertAlsZahl';
FELD_WERT_ALS_STRING: 'FeldWertAlsString';
FELD_WERTE_NICHT_EINDEUTIG: 'FeldWerteNichtEindeutig';
FELD_WERT_IN_WERTE_LISTE: 'FeldWertInWerteListe';
FELD_WERT_NICHT_IN_WERTE_LISTE: 'FeldWertNichtInWerteListe';
FOR: 'Fuer';
GUELTIG: 'Gueltig';
HEUTE: 'Heute';
IN1: 'In'; 
IN2: 'in'; 
IN3: 'IN'; 
JAHR_AUS_DATUM: 'JahrAusDatum';
JETZT: 'Jetzt';
KEIN_FELD_ANGEGEBEN: 'KeinFeldAngegeben';
KEIN_FELD_WERT_IN_WERTE_LISTE: 'KeinFeldWertInWerteListe';
KEIN_KONTEXT_ANGEGEBEN: 'KeinKontextAngegeben';
KEIN_VORDRUCK_ANGEGEBEN: 'KeinVordruckAngegeben';
KONTEXT_ANGEGEBEN: 'KontextAngegeben';
KONTEXTE_NICHT_GEMEINSAM_ANGEGEBEN: 'KontexteNichtGemeinsamAngegeben';
KONTEXT_NICHT_ANGEGEBEN: 'KontextNichtAngegeben';
LAENGE: 'Laenge';
LETZTER_TAG: 'LetzterTag';  
MAX: 'Max';
MAXIMAL_ANGEGEBENER_ZEILENINDEX: 'MaximalAngegebenerZeilenindex';
MAX_WERT: 'MaxWert';
MEHR_ALS_EIN_FELD_ANGEGEBEN: 'MehrAlsEinFeldAngegeben';
MIN: 'Min';
MINDESTENS_EIN_DATUMSBEREICH_UEBERLAPPEND: 'MindestensEinDatumsBereichUeberlappend';
MINDESTENS_EIN_FELD_ANGEGEBEN: 'MindestensEinFeldAngegeben';
MINDESTENS_EIN_FELD_WERT_IN_WERTE_LISTE: 'MindestensEinFeldWertInWerteListe';
MINDESTENS_EIN_KONTEXT_ANGEGEBEN: 'MindestensEinKontextAngegeben';
MINDESTENS_EIN_VORDRUCK_ANGEGEBEN: 'MindestensEinVordruckAngegeben';
MINUTEN_AUS_ZEIT: 'MinutenAusZeit';
MIN_WERT: 'MinWert';
MIT: 'Mit';
MONAT_AUS_DATUM: 'MonatAusDatum';
NICHT_ALLE_FELDER_ODER_KEIN_FELD_ANGEGEBEN: 'NichtAlleFelderOderKeinFeldAngegeben';
NICHT_ALLE_FELD_WERTE_IN_WERTE_LISTE: 'NichtAlleFeldWerteInWerteListe';
NICHT_ALLE_ODER_KEIN_KONTEXT_ANGEGEBEN: 'NichtAlleOderKeinKontextAngegeben';
NICHT_GENAU_EIN_FELD_ANGEGEBEN: 'NichtGenauEinFeldAngegeben'; 
NICHT_WIE_MUSTER: 'NichtWieMuster';
ODER1: 'Oder';
ODER2: 'oder';
ODER3: 'ODER';
QUARTAL_AUS_DATUM: 'QuartalAusDatum';
RUNDEN: 'Runden';
RUNDEN_WERT: 'RundenWert';
REGEL_KONTEXT: 'RegelKontext';
SEKUNDEN_AUS_ZEIT: 'SekundenAusZeit';
START_AUS_DATUM_BEREICH: 'StartAusDatumBereich';
STUNDEN_AUS_ZEIT: 'StundenAusZeit';
SUMME: 'Summe';
SUMME_VON_PRODUKTEN: 'SummeVonProdukten';
TAG_AUS_DATUM: 'TagAusDatum';
TIME: 'Zeit';
UND1: 'Und';
UND2: 'und';
UND3: 'UND';
UNGUELTIG: 'Ungueltig';
UNGLEICH_MIT_TOLERANZ1: 'UngleichMitToleranz1';
UNGLEICH_MIT_TOLERANZ2: 'UngleichMitToleranz2';
UNGLEICH_MIT_TOLERANZ5: 'UngleichMitToleranz5';
UNGLEICH_MIT_TOLERANZ10: 'UngleichMitToleranz10';
UNTERDRUECKE_WARNUNG: 'UnterdrueckeWarnung';
USB_NICHT_FORTLAUFEND: 'UsbNichtFortlaufend';
REFERENZJAHR: 'Referenzjahr';
VORDRUCK_ANGEGEBEN: 'VordruckAngegeben';
VORDRUCK_NICHT_ANGEGEBEN: 'VordruckNichtAngegeben';
VORDRUCK_NICHT_FORTLAUFEND: 'VordruckNichtFortlaufend';
WAHR: 'Wahr';
WERT_ALS_DATUM: 'WertAlsDatum';
WERT_NICHT_KONSISTENT: 'WertNichtKonsistent';
WIEDERHOLUNG_NICHT_EINDEUTIG: 'WiederholungNichtEindeutig';
WIE_MUSTER: 'WieMuster';
ZEILE_NICHT_EINDEUTIG: 'ZeileNichtEindeutig';
ZEILEN_LUECKENLOS: 'ZeilenLueckenlos';
ZEILEN_NICHT_AUFSTEIGEND: 'ZeilenNichtAufsteigend';
ZEIT_AUS_DATETIME: 'ZeitAusDateTime';
ZU1: 'zu';
ZU2: 'Zu';
ZU3: 'ZU';
PLUS: '+';
MINUS: '-';
MULT: '*';
DIV: '/';
POW: '^';
GT: '>';
NE: '!=';
LT: '<';
EQ: '==';
GE: '>=';
LE: '<=';
ASS: '=';
KLAMMER_AUF: '(';
KLAMMER_ZU: ')';
ECKIGE_KLAMMER_AUF: '[';
ECKIGE_KLAMMER_ZU: ']';
GESCHWEIFTE_KLAMMER_AUF: '{';
GESCHWEIFTE_KLAMMER_ZU: '}';
KOMMA: ',';
PUNKT: '.';
UP: '..';
FROM: '@From' {this.getMultiLanguageHandler().setTypeInLexer(this);};
VON: '@Von' {this.getMultiLanguageHandler().setTypeInLexer(this);};
AT: '@';
ZEILE_ITER: '#Zeile';
CAT: '->' ;
DOLLAR: '$';

//// Lexer rules, similar in BaseErrorTextParameterLexer.g4. In case of changes, check if the other lexer should be changed, too. ////
NameOhnePunktSTRAmAnfang	
	:	STR (STR | DIGIT )*;
	
NameOhnePunktZifferAmAnfang 	
	:	ZiffernFolge STR (STR | DIGIT )* ;

fragment STR: 'a'..'z'|'A'..'Z'|'_'|'Ä'|'Ü'|'Ö'|'ä'|'ü'|'ö'|'ß'| ':';
fragment DIGIT:('0' .. '9');	 
WS : (' '|'\r'|'\t'|'\u000C'|'\n') -> channel(HIDDEN);
