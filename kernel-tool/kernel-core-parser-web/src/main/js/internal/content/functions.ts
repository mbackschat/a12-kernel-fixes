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

export const content =
[
{
		"de-DE": {
			"name" : "Summe",
			"description": "Summe aller Werte der angegebenen Felder."
		},
		"en-US":{
			"name" : "Sum",
			"description": "Sum of the values of all given fields using zero as a default for each unspecified field value (must only be used with fields that are numeric or amounts)."
		}
    },
	{
		"de-DE": {
			"name" : "SummeVonProdukten",
			"description": "Für zwei wiederholbare Felder wird in jeder Wiederholung das Produkt berechnet und diese Produkte aus allen Wiederholungen aufsummiert."
		},
		"en-US":{
			"name" : "SumOfProducts",
			"description": "The product over two repeatable fields is calculated for each repetition of the product and those products will be added up for all repetitions (A1*B1 + A2*B2 + A3*B3...)."
		}
    },
    {
        "de-DE": {
            "name": "SummeVonTermen",
            "description": "Die Summe eines Terms über eine Menge von Wiederholungen, angegeben durch einen gesternten Pfad und den Term."
        },
        "en-US": {
            "name": "SumOfTerms",
            "description": "The sum of a term over a set of iterations specified by a path with asterisks and a term."
        }
    },
    {
		"de-DE": {
			"name" : "MaxWert",
			"description": "Maximaler Wert aller angegebenen Felder. Ist ein Feld nicht angegeben, wird es ignoriert."
		},
		"en-US":{
			"name" : "MaxValue",
			"description": "Maximum value of fields ignoring unspecified fields."
		}
    },
	{
		"de-DE": {
			"name" : "MinWert",
			"description": "Minimaler Wert aller angegebenen Felder. Ist ein Feld nicht angegeben, wird es ignoriert."
		},
		"en-US":{
			"name" : "MinValue",
			"description": "Minimum value of fields ignoring unspecified fields."
		}
    },
    {
		"de-DE": {
			"name" : "ErsterAngegebenerWert",
			"description": "Liefert für eine Liste von Entities den Wert des ersten angegebenen."
		},
		"en-US":{
			"name" : "FirstFilledValue",
			"description": "The first given value for a list of entities."
		}
    },
	{
		"de-DE": {
			"name" : "Runden",
			"description": "Auf den Wert wird das Kaufmännische Runden angewendet. Als zweiter Parameter kann die Anzahl an Nachkommastellen übergeben werden. Die resultierende Zahl hat die angegebene Anzahl Nachkommastellen."
		},
		"en-US":{
			"name" : "RoundAccounting",
			"description": "The value is rounded up to nearest integer in accordance with standard commercial practice (in case the first dropped decimal place is 5, 6, 7, 8, or 9, rounding up to the next decimal place will be done)."
		}
    },
	{
		"de-DE": {
			"name" : "RundenWert",
			"description": "Auf den Wert des Feldes wird das Kaufmännische Runden angewendet. Die resultierende Zahl hat die angegebene Nachkommastellen."
		},
		"en-US":{
			"name" : "RoundAccountingValue",
			"description": "The value is rounded up to nearest number in accordance with standard commercial practice. The resulting number will have the number of decimal places given (in case the first dropped decimal place is 5, 6, 7, 8, or 9, rounding up to the next decimal place will be done)."
		}
    },
	{
		"de-DE": {
			"name" : "Laenge",
			"description": "Liefert die Länge des Feldes (nur anwendbar auf Feldtyp „String“)."
		},
		"en-US":{
			"name" : "Length",
			"description": "Returns the number of characters of the field. The field must be of type String."
		}
    },
	{
		"de-DE": {
			"name" : "Aufrunden",
			"description": "Der Wert wird auf nächst größere ganze Zahl aufgerundet. Als zweiter Parameter kann die Anzahl an Nachkommastellen übergeben werden. Ist das der Fall wird der Wert auf die nächst kleinere Zahl mit der angegebenen Anzahl Nachkommastellen aufgerundet."
		},
		"en-US":{
			"name" : "RoundUp",
			"description": "The next integer value above or equal to the value will be returned. The number of decimal places might be specified as a second parameter."
		}
    },
	{
		"de-DE": {
			"name" : "AufrundenWert",
			"description": "Der Wert des Feldes wird auf nächst größere Zahl mit der angegebenen Anzahl Nachkommastellen aufgerundet."
		},
		"en-US":{
			"name" : "RoundUpValue",
			"description": "The field value is rounded up to the next greater integer (no decimal places)."
		}
    },
	{
		"de-DE": {
			"name" : "Abrunden",
			"description": "Der Wert wird auf nächst kleinere ganze Zahl abgerundet. Als zweiter Parameter kann die Anzahl an Nachkommastellen übergeben werden. Ist das der Fall wird der Wert auf die nächst kleinere Zahl mit der angegebenen Anzahl Nachkommastellen abgerundet."
		},
		"en-US":{
			"name" : "RoundDown",
			"description": "The next integer value below or equal to the value will be returned. The number of decimal places might be specified as a second parameter."
		}
    },
	{
		"de-DE": {
			"name" : "AbrundenWert",
			"description": "Der Wert des Feldes wird auf nächst kleinere ganze Zahl abgerundet. Als zweiter Parameter kann die Anzahl an Nachkommastellen übergeben werden. Ist das der Fall wird der Wert auf die nächst kleinere Zahl mit der angegebenen Anzahl Nachkommastellen abgerundet."
		},
		"en-US":{
			"name" : "RoundDownValue",
			"description": "The field value is rounded down to the next smaller integer (no decimal places)."
		}
    },
	{
		"de-DE": {
			"name" : "Abs",
			"description": "Absoluter Betrag vom Wert wird gebildet."
		},
		"en-US":{
			"name" : "Abs",
			"description": "The absolute value will be returned."
		}
    },
	{
		"de-DE": {
			"name" : "AbsWert",
			"description": "Absoluter Betrag vom Wert des Feldes wird gebildet."
		},
		"en-US":{
			"name" : "AbsValue",
			"description": "Absolute amount of the field value will be formed."
		}
    },
	{
		"de-DE": {
			"name" : "Max",
			"description": "Maximum der aufgelisteten Werte."
		},
		"en-US":{
			"name" : "Max",
			"description": "Maximum value of the given field values and other terms."
		}
    },
	{
		"de-DE": {
			"name" : "Min",
			"description": "Minimum der aufgelisteten Werte."
		},
		"en-US":{
			"name" : "Min",
			"description": "Minimum value of the given field values and other terms."
		}
    },
	{
		"de-DE": {
			"name" : "AnzahlWertInFeldliste",
			"description": "Gibt die Anzahl der Felder zurück, deren Wert mit der Konstante identisch ist."
		},
		"en-US":{
			"name" : "NumberOfValueInFields",
			"description": "Determines how many of the specified fields are filled with the specified <value>."
		}
    },
	{
		"de-DE": {
			"name" : "AnzahlAngegebenerKontexte",
			"description": "Gibt die Anzahl der Kontexte zurück, für die ein Wert angegeben ist. Ein Kontext gilt als angegeben, wenn für ein Feld in dem Kontext ein Wert angegeben ist. Hat ein Kontext Unterkontexte, so werden nicht die Anzahl der Unterkontexte gezählt sondern nur die Anzahl der Wiederholungen des Kontextes in der KontextListe."
		},
		"en-US":{
			"name" : "NumberOfFilledGroups",
			"description": "Returns the number of groups in <GrpList> for which at least one field has been set. In case there are subgroups, only the number of repetitions of the group within the group list will be counted."
		}
    },
    {
        "de-DE": {
			"name" : "AnzahlVerschiedenerWerte",
			"description": "Gibt die Anzahl verschiedener Feldwerte zurück. Ob Werte gleich oder verschieden sind, wird entsprechend dem Feldtyp entschieden. Es dürfen nur vergleichbare Feldtypen gemischt werden."
		},
		"en-US":{
			"name" : "NumberOfDifferentValues",
			"description": "Returns the number of distinct field values. Equality of values is decided in a suitable way that is chosen based on the field type. Only comparable field types may be combined."
		}
    },
	{
		"de-DE": {
			"name" : "AnzahlAngegebenerVordrucke",
			"description": "Liefert für den angegebenen Vordruck die Anzahl, wie oft der Vordruck angegeben wurde. Handelt es sich um einen USB-Vordruck, so ist AnzahlAngegebenerVordrucke die Anzahl der verschiedenen laufenden Nummern, für die der Vordruck mit dem aktuellen USB abgegeben wurde"
		}
    },
	{
		"de-DE": {
			"name" : "BereichAlsString",
			"description": "Es werden der Start und das Ende des Bereiches angegeben. Die Rückgabe ist einschließlich der angegebenen Position. Anschließende Vergleiche sind alphanumerisch. Für folgende Fälle wird bei den anschließenden Vergleichen ein Leerstring verwendet: Für das Feld ist kein Wert angegeben; Start- und Endeindex sind nicht passend zu der Länge des Feldwertes. Ist das Feld nicht prüfungsrelevant, so wird der Vergleich, in dem diese Operation stattfindet, nicht ausgewertet, d.h. der Vergleich evaluiert immer zu false und führt somit nicht zu einem Fehler."
		},
		"en-US":{
			"name" : "RangeAsString",
			"description": "Start and end position of the range are given and it will be tried to extract a string value from that range. The returned value includes the specified positions. Subsequent comparisons are alphanumeric. In case the field is not relevant for the check, the comparison within which this operation takes place will not be evaluated, therefore 'False' will always be returned and no error message will be shown."
		}
    },
	{
		"de-DE": {
			"name" : "BereichAlsZahl",
			"description": "Es werden der Start und das Ende des Bereiches angegeben. Die Rückgabe ist einschließlich der angegebenen Position. Anschließende Vergleiche sind numerisch. Für folgende Fälle wird bei den anschließenden Berechnungen oder Vergleichen ein 0 verwendet: Für das Feld ist kein Wert angegeben; Start- und Endeindex sind nicht passend zu der Länge des Feldwertes; Der von Start- und Endeindex definierte Teilstring ist keine numerische Ziffernfolge. Ist das Feld nicht prüfungsrelevant, so wird der Vergleich, in dem diese Operation stattfindet, nicht ausgewertet, d.h. der Vergleich evaluiert immer zu false und führt somit nicht zu einem Fehler."
		},
		"en-US":{
			"name" : "RangeAsNumber",
			"description": "Start and end position of the range are given and it will be tried to extract a numerical value from that range. The returned value includes the specified positions. Subsequent comparisons are numerically. In case the field is not relevant for the check, the comparison within which this operation takes place will not be evaluated, therefore 'False' will always be returned and no error message will be shown."
		}
    },
	{
		"de-DE": {
			"name" : "WertAlsDatum",
			"description": "Liefert den Wert des Feldes als Datum. Das Feld muss vom Typ Datum, Art der Teilbekanntheit TAG_OPTIONAL, MONAT_OPTIONAL oder JAHR_OPTIONAL sein. Als erster Parameter wird das Feld spezifiziert. Als zweiter Parameter ist nur entweder ErsterTag oder LetzterTag gültig."
		},
		"en-US":{
			"name" : "ValueAsDate",
			"description": "Returns the value of <dateField> as a date. Only ’FirstDay’ or ’LastDay’ are a valid as second parameter (determining whether the first or the last possible date should be returned for dates that are only partially specified). Subsequent comparisons will compare values of type date. The partially-specified property of the date has to be DAY_OPTIONAL, MONTH_OPTIONAL, or YEAR_OPTIONAL."
		}
    },
	{
		"de-DE": {
			"name" : "DifferenzInSekunden",
			"description": "Liefert die Differenz zwischen zwei Datumsangaben in Sekunden."
		},
		"en-US":{
			"name" : "DifferenceInSeconds",
			"description": "Returns the integer difference between two dates counted in seconds or 0 if at least one field is not filled."
		}
    },
	{
		"de-DE": {
			"name" : "DifferenzInMinuten",
			"description": "Liefert die Differenz zwischen zwei Datumsangaben in Minuten."
		},
		"en-US":{
			"name" : "DifferenceInMinutes",
			"description": "Returns the integer difference between two dates counted in minutes or 0 if at least one field is not filled."
		}
    },
	{
		"de-DE": {
			"name" : "DifferenzInStunden",
			"description": "Liefert die Differenz zwischen zwei Datumsangaben in Stunden."
		},
		"en-US":{
			"name" : "DifferenceInHours",
			"description": "Returns the integer difference between two dates counted in hours or 0 if at least one field is not filled."
		}
    },
	{
		"de-DE": {
			"name" : "DifferenzInTagen",
			"description": "Liefert die Differenz zwischen zwei Datumsangaben in Tagen."
		},
		"en-US":{
			"name" : "DifferenceInDays",
			"description": "Returns the integer difference between two dates counted in days or 0 if at least one field is not filled."
		}
    },
	{
		"de-DE": {
			"name" : "DifferenzInMonaten",
			"description": "Liefert die Differenz zwischen zwei Datumsangaben in Monaten."
		},
		"en-US":{
			"name" : "DifferenceInMonths",
			"description": "Returns the integer difference between two dates counted in months or 0 if at least one field is not filled."
		}
    },
	{
		"de-DE": {
			"name" : "DifferenzInJahren",
			"description": "Liefert die Differenz zwischen zwei Datumsangaben in Jahren."
		},
		"en-US":{
			"name" : "DifferenceInYears",
			"description": "Returns the integer difference between two dates counted in years or 0 if at least one field is not filled."
		}
    },
	{
		"de-DE": {
			"name" : "AddiereJahre",
			"description": "Addiert eine Anzahl von Jahren auf ein Datum."
		},
		"en-US":{
			"name" : "AddYears",
			"description": "Adds a number of years to a date."
		}
    },
    	{
		"de-DE": {
			"name" : "AddiereMonate",
			"description": "Addiert eine Anzahl von Monaten auf ein Datum."
		},
		"en-US":{
			"name" : "AddMonths",
			"description": "Adds a number of months to a date."
		}
    },
    	{
		"de-DE": {
			"name" : "AddiereTage",
			"description": "Addiert eine Anzahl von Tagen auf ein Datum."
		},
		"en-US":{
			"name" : "AddDays",
			"description": "Adds a number of days to a date."
		}
    },
    	{
		"de-DE": {
			"name" : "AddiereStunden",
			"description": "Addiert eine Anzahl von Stunden auf ein Datum."
		},
		"en-US":{
			"name" : "AddHours",
			"description": "Adds a number of hours to a date."
		}
    },
    	{
		"de-DE": {
			"name" : "AddiereMinuten",
			"description": "Addiert eine Anzahl von Minuten auf ein Datum."
		},
		"en-US":{
			"name" : "AddMinutes",
			"description": "Adds a number of minutes to a date."
		}
    },
    {
		"de-DE": {
			"name" : "AddiereSekunden",
			"description": "Addiert eine Anzahl von Sekunden auf ein Datum."
		},
		"en-US":{
			"name" : "AddSeconds",
			"description": "Adds a number of seconds to a date."
		}
    },
    {
		"de-DE": {
			"name" : "TagAusDatum",
			"description": "Liefert den Tag der Datumsangabe als Zahl zurück."
		},
		"en-US":{
			"name" : "DayFromDate",
			"description": "Returns the day part of the date as an integer."
		}
    },
	{
		"de-DE": {
			"name" : "MonatAusDatum",
			"description": "Liefert den Monat der Datumsangabe als Zahl zurück."
		},
		"en-US":{
			"name" : "MonthFromDate",
			"description": "Returns the month part of the date as an integer."
		}
    },
	{
		"de-DE": {
			"name" : "JahrAusDatum",
			"description": "Liefert das Jahr der Datumsangabe als Zahl zurück."
		},
		"en-US":{
			"name" : "YearFromDate",
			"description": "Returns the year part of the date as an integer."
		}
    },
	{
		"de-DE": {
			"name" : "QuartalAusDatum",
			"description": "Liefert das Quartal der Datumsangabe als Zahl zwischen 1 und 4 zurück."
		},
		"en-US":{
			"name" : "QuarterFromDate",
			"description": "Returns the quarter of the date as an integer (number between 1 and 4)."
		}
    },
    {
		"de-DE": {
			"name" : "StundenAusZeit",
			"description": "Liefert die Stunden der Datumsangabe als Zahl zwischen 00 und 23 zurück."
		},
		"en-US":{
			"name" : "HoursFromTime",
			"description": "Returns the hours of the date as an integer between 00 and 23."
		}
    },
    {
		"de-DE": {
			"name" : "MinutenAusZeit",
			"description": "Liefert die Minuten der Datumsangabe als Zahl zwischen 00 und 59 zurück."
		},
		"en-US":{
			"name" : "MinutesFromTime",
			"description": "Returns the minutes of the date as an integer between 00 and 59."
		}
    },
    {
		"de-DE": {
			"name" : "SekundenAusZeit",
			"description": "Liefert die Sekunden der Datumsangabe als Zahl zwischen 00 und 59 zurück."
		},
		"en-US":{
			"name" : "SecondsFromTime",
			"description": "Returns the seconds of the date as an integer between 00 and 59."
		}
    },
    {
		"de-DE": {
			"name" : "DatumAusDateTime",
			"description": "Liefert das Datum aus der DateTime Angabe zurück."
		},
		"en-US":{
			"name" : "DateFromDateTime",
			"description": "Returns the date fragment of the DateTime."
		}
    },
    {
		"de-DE": {
			"name" : "ZeitAusDateTime",
			"description": "Liefert die Zeit aus der DateTime Angabe zurück."
		},
		"en-US":{
			"name" : "TimeFromDateTime",
			"description": "Returns the time fragment of the DateTime."
		}
    },
	{
		"de-DE": {
			"name" : "MaximalAngegebenerZeilenindex",
			"description": "Liefert den maximal angegebenen Zeilenindex aus der Feldliste."
		}
    },
	{
		"de-DE": {
			"name" : "Datum",
			"description": "Konstruiert aus den angegebenen Parametern ein Datum."
		},
		"en-US":{
			"name" : "Date",
			"description": "Will assemble a date value from the given parameters."
		}
    },
    {
		"de-DE": {
			"name" : "DateTime",
			"description": "Konstruiert aus einer Datums und einer Zeitangabe ein DateTime."
		},
		"en-US":{
			"name" : "DateTime",
			"description": "Will assemble a date time from a date and a time specification."
		}
    },
    {
		"de-DE": {
			"name" : "Zeit",
			"description": "Konstruiert aus den angegebenen Parametern eine Zeitangabe."
		},
		"en-US":{
			"name" : "Time",
			"description": "Will assemble a time specification from the given parameters."
		}
    },
    {
		"de-DE": {
			"name" : "DatumBereich",
			"description": "Konstruiert aus zwei Datumsangaben einen DatumBereich."
		},
		"en-US":{
			"name" : "DateRange",
			"description": "Will assemble a date range from two dates."
		}
    },
    {
		"de-DE": {
			"name" : "AktuelleVordruckNummer",
			"description": "Liefert für das angegebene Feld die laufende Nummer des Vordruckes."
		}
    },
    {
		"de-DE": {
			"name" : "AktuelleWiederholung",
			"description": "Liefert für den angegebenen Kontext bzw. Vordruck die aktuelle Wiederholung."
		},
		"en-US": {
			"name" : "CurrentRepetition",
			"description": "Returns the current repetition of the specified group or root group, respectively."
		}
    },
	{
		"de-DE": {
			"name" : "FeldWertAlsZahl",
			"description": "Liefert den Wert des Feldes als Zahl. Das Feld kann vom Typ String mit dem Muster [0-9]+ sein. Zusätzlich muss die maximale Länge kleiner als 16 sein. FeldWertAlsZahl kann auch angewendet werden auf ein Feld vom Typ Enumeration, wenn alle möglichen Werte numerisch sind und eine Länge kleiner als 16 haben. Für andere Feldtypen ist die Operation nicht erlaubt."
		},
		"en-US": {
			"name" : "FieldValueAsNumber",
			"description": "Returns the value of the field as a number. The field can be of type string with the pattern [0-9]+. Additionally, the max length must be smaller than 16. FieldValueAsNumber also can be applied to a field of type enumeration, if all values of the enumeration are numeric and the length is smaller than 16. The operation is not permitted for other field types."
		}
    },
	{
		"de-DE": {
			"name" : "FeldWertAlsString",
			"description": "Liefert den Wert des Feldes als String. Das Feld kann vom Typ Zahl sein. Es wird immer die interne Repräsentation des Wertes verwendet."
		},
		"en-US": {
			"name" : "FieldValueAsString",
			"description": "Returns the value of the field as a String. The field can be of type number. The resulting presentation is the internal one. So you should only use it for fields without decimal places."
		}
    },
	{
		"de-DE": {
			"name" : "AnzahlAngegebenerFelder",
			"description": "Liefert die Anzahl angegebener Felder."
		},
		"en-US": {
			"name" : "NumberOfFilledFields",
			"description": "Returns the number of filled fields."
		}
    },
	{
		"de-DE": {
			"name" : "StartAusDatumBereich",
			"description": "Gibt für einen Datumsbereich den Start des Datumbereichs zurück."
		},
		"en-US":{
			"name" : "StartOfDateRange",
			"description": "Returns the start date of a date range.."
		}
    },
	{
		"de-DE": {
			"name" : "EndeAusDatumBereich",
			"description": "Gibt für einen Datumsbereich das Ende des Datumbereichs zurück."
		},
		"en-US":{
			"name" : "EndOfDateRange",
			"description": "Returns the end date of a date range."
		}
    },
	{
		"de-DE": {
			"name" : "Referenzjahr",
			"description": "Gibt das im Modell spezifizierte Referenzjahr an. Mit JahrAusDatum(Referenzjahr) kann in einem Modell mit Referenzjahr 2021 z.B. die Zahl 2021 ermittelt werden."
		},
		"en-US":{
			"name" : "BaseYear",
			"description": "Returns the base year set for the model. For example, using YearFromDate(BaseYear) in a model with base year 2021, the number 2021 would be returned."
		}
    },
	{
		"de-DE": {
			"name" : "Heute",
			"description": "Gibt das heutige Datum an."
		},
		"en-US":{
			"name" : "Today",
			"description": "Returns the current day as date."
		}
    },
    {
		"de-DE": {
			"name" : "Jetzt",
			"description": "Gibt das heutige Datum und die Uhrzeit an."
		},
		"en-US":{
			"name" : "Now",
			"description": "Returns the current date and time."
		}
    },
	{
		"de-DE": {
			"name" : "Wahr",
			"description": "Boolescher Wert Wahr für den Vergleich für ein Ja oder ein JaNein Feld."
		},
		"en-US":{
			"name" : "True",
			"description": "Boolean value 'true' resulting from comparisons for a 'yes' or a 'yes/no' field."
		}
    },
	{
		"de-DE": {
			"name" : "Falsch",
			"description": "Boolescher Wert Falsch für den Vergleich für ein JaNein Feld."
		},
		"en-US":{
			"name" : "False",
			"description": "Boolean value 'false' resulting from comparisons for a 'yes/no' field."
		}
    },
	{
		"de-DE": {
			"name" : "AnzahlAngegebeneUsb",
			"description": "Gibt die Anzahl der verschiedenen USB zurück, für die mindestens ein Feld angegeben wurde."
		}
    }
]

