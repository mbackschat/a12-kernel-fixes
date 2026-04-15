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
			"name" : "MindestensEinFeldAngegeben",
			"description": "wahr, wenn für mindestens ein Feld der <FeldListe> ein Wert angegeben ist."
		},
		"en-US": {
			"name" : "AtLeastOneFieldFilled",
			"description": "True, if at least for one field of <FieldList> a value is filled."
		}
    },
	{
		"de-DE": {
			"name" : "MindestensEinKontextAngegeben",
			"description": "wahr, wenn mindestens für ein Element ein Wert angegeben ist. "
		},
		"en-US":{
			"name" : "AtLeastOneGroupFilled",
			"description": "True, if for one or more groups at least one field is filled."
		}
    },
	{
		"de-DE": {
			"name" : "NichtAlleFelderOderKeinFeldAngegeben",
			"description": "wahr, wenn nicht für alle Felder ein Wert angegeben ist. Dies schließt den Fall ein, dass für kein Feld ein Wert angegeben ist."
		},
		"en-US":{
			"name" : "NotAllFieldsFilled",
			"description": "True, if not for each field in <FieldList> a value is given. This includes the case in which no value was given for any of the fields in the field list at all."
		}
    },
	{
		"de-DE": {
			"name" : "NichtAlleOderKeinKontextAngegeben",
			"description": "wahr, wenn nicht für alle Elemente ein Wert angegeben ist. Dies schließt den Fall ein, dass für kein Feld ein Wert angegeben ist."
		},
		"en-US":{
			"name" : "NotAllGroupsFilled",
			"description": "True, if for at least one group in <GrpLst> the predicate GroupFilled does not hold true. This includes the case in which no value was given for any of the fields in the specified groups or their subgroups at all."
		}
    },
	 {
		"de-DE": {
			"name" : "KeinFeldAngegeben",
			"description": "wahr, wenn für kein Feld ein Wert angegeben ist. "
		},
		"en-US":{
			"name" : "NoFieldFilled",
			"description": "True, if for none of the fields in <FieldList> a value is given."
		}
    },
	{
		"de-DE": {
			"name" : "KeinKontextAngegeben",
			"description": "wahr, wenn für kein Element ein Wert angegeben ist."
		},
		"en-US":{
			"name" : "NoGroupFilled",
			"description": "True, if not for a single group in <GrpLst> any field is filled."
		}
    },
	{
		"de-DE": {
			"name" : "AlleFelderAngegeben",
			"description": "wahr, wenn für alle Felder ein Wert angegeben ist."
		},
		"en-US":{
			"name" : "AllFieldsFilled",
			"description": "True, if for all the fields in <FieldsList> values are given."
		}
    },
	{
		"de-DE": {
			"name" : "AlleKontexteAngegeben",
			"description": "wahr, wenn für alle Elemente ein Wert angegeben ist."
		},
		"en-US":{
			"name" : "AllGroupsFilled",
			"description": "True, if for every group in <GrpLst> at least one field is filled."
		}
    },
	{
		"de-DE": {
			"name" : "NichtGenauEinFeldAngegeben",
			"description": "wahr, wenn für mehr als ein Feld ein Wert angegeben ist, oder wenn für kein Feld ein Wert angegeben ist."
		},
		"en-US":{
			"name" : "NotExactlyOneFieldFilled",
			"description": "True, if for more than one field or for no field at all in <FileList> a value is given."
		}
    },
	{
		"de-DE": {
			"name" : "FeldAngegeben",
			"description": "wahr, wenn das Feld angegeben ist, d.h. für das Feld <Feldname> ein Wert angegeben ist."
		},
		"en-US":{
			"name" : "FieldFilled",
			"description": "True, if the field <FieldName> is filled, i.e. a specific value has been given for it."
		}
    },
	{
		"de-DE": {
			"name" : "FeldNichtAngegeben",
			"description": "wahr, wenn das Feld nicht angegeben ist, d.h. für das Feld <Feldname> kein Wert angegeben ist."
		},
		"en-US":{
			"name" : "FieldNotFilled",
			"description": "True, if the field <FieldName> is not filled, i.e. no value at all has been given for it."
		}
    },
	{
		"de-DE": {
			"name" : "FelderNichtGemeinsamAngegeben",
			"description": "wahr, wenn nicht für alle Felder ein Wert angegeben ist und nicht für kein Feld ein Wert angegeben ist."
		},
		"en-US":{
			"name" : "FieldsNotCollectivelyFilled",
			"description": "True, if for at least one but not for all fields in <FieldList> a value is specified."
		}
    },
	{
		"de-DE": {
			"name" : "KontextAngegeben",
			"description": "wahr, wenn das Element angegeben ist."
		},
		"en-US":{
			"name" : "GroupFilled",
			"description": "True, if a value is filled for at least one field in <Group>."
		}
    },
	{
		"de-DE": {
			"name" : "KontextNichtAngegeben",
			"description": "wahr, wenn das Element nicht angegeben ist."
		},
		"en-US":{
			"name" : "GroupNotFilled",
			"description": "True, if not a single field in <Group> is filled."
		}
    },
	{
		"de-DE": {
			"name" : "KontexteNichtGemeinsamAngegeben",
			"description": "wahr, wenn nicht alle Elemente angegeben sind und nicht für kein Element angegeben ist."
		},
		"en-US":{
			"name" : "GroupsNotCollectivelyFilled",
			"description": "True, if for at least one, but not for every group in <GrpLst> at least one value is filled."
		}
    },
	{
		"de-DE": {
			"name" : "VordruckAngegeben",
			"description": "wahr, wenn der spezifizierte Vordruck angegeben ist."
		}
    },
	{
		"de-DE": {
			"name" : "VordruckNichtAngegeben",
			"description": "wahr, wenn der spezifizierte Vordruck nicht angegeben ist."
		}
    },
	{
		"de-DE": {
			"name" : "MindestensEinVordruckAngegeben",
			"description": "wahr, mindestens ein Vordruck der Vordruckliste angegeben ist."
		}
    },
	{
		"de-DE": {
			"name" : "KeinVordruckAngegeben",
			"description": "wahr, wenn kein Vordruck der Vordruckliste angegeben ist."
		}
    },
	{
		"de-DE": {
			"name" : "VordruckNichtFortlaufend",
			"description": "wahr, wenn die angegebenen Vordruck nicht fortlaufend nummeriert sind."
		}
    },
	{
		"de-DE": {
			"name" : "UsbNichtFortlaufend",
			"description": "wahr, wenn die Usb-Wiederholungen des angegebenen Vordrucks nicht fortlaufend nummeriert sind."
		}
    },
	{
		"de-DE": {
			"name" : "ZeilenNichtAufsteigend",
			"description": "wahr, wenn für die vorherige Zeile nicht ein Wert angegeben ist."
		}
    },
	{
		"de-DE": {
			"name" : "ZeileNichtEindeutig",
			"description": "wahr, wenn in mindestens zwei Zeilen die gleichen Werte eingegeben wurden."
		}
    },
	{
		"de-DE": {
			"name" : "WiederholungNichtEindeutig",
			"description": "wahr, wenn für mindestens zwei Wiederholungen die gleichen Werte eingegeben wurden."
		},
		"en-US":{
			"name" : "RepetitionNotUnique",
			"description": "True, if for at least two repetitions the same values have been given."
		}
    },
	{
		"de-DE": {
			"name" : "ApplikationsBedingung",
			"description": ""
		},
		"en-US":{
			"name" : "CustomCondition",
			"description": ""
		}
    },
	{
		"de-DE": {
			"name" : "FeldWertInWerteListe",
			"description": "wahr, wenn der Wert gleich einem Wert in der Werteliste ist. Es können Zahlen und Zeichenketten verglichen werden."
		},
		"en-US":{
			"name" : "FieldValueIncludedInValueList",
			"description": "True, if the specified field is filled and its value is included in the list of values specified by <List> (works for numbers and strings only)."
		}
    },
	{
		"de-DE": {
			"name" : "FeldWertNichtInWerteListe",
			"description": "wahr, wenn der Wert nicht gleich einem Wert in der Werteliste ist. Es können Zahlen und Zeichenketten verglichen werden."
		},
		"en-US":{
			"name" : "FieldValueNotIncludedInValueList",
			"description": "True, if the specified field is filled and its value is not included in the list of values specified by <List>."
		}
    },
	{
		"de-DE": {
			"name" : "MehrAlsEinFeldAngegeben",
			"description": "wahr, wenn für mindestens zwei Felder ein Wert angegeben ist."
		},
		"en-US":{
			"name" : "MoreThanOneFieldFilled",
			"description": "True, if at least two or more fields in <FieldList> have been filled with a value."
		}
    },
	{
		"de-DE": {
			"name" : "FeldWerteNichtEindeutig",
			"description": "wahr, wenn zwei Felder in der Feldlisten den gleichen Wert haben."
		},
		"en-US":{
			"name" : "FieldValuesNotUnique",
			"description": "True, if there are fields in <FieldList> which do have the same value. Always returns False if none of the fields is filled."
		}
    },
	{
		"de-DE": {
			"name" : "DatumsBereicheUeberlappend",
			"description": "wahr, wenn zwei Felder in der Feldliste sich als Datumsbereiche überlappen."
		},
		"en-US":{
			"name" : "DateRangesOverlap",
			"description": "True, if two fields in the field list overlap as date ranges."
		}
    },
	{
		"de-DE": {
			"name" : "ZeilenLueckenlos",
			"description": "wahr, wenn die Zeilen der Feldliste lückenlos aufsteigend sind."
		}
    },
	{
		"de-DE": {
			"name" : "NichtAlleFeldWerteInWerteListe",
			"description": "wahr, wenn nicht alle Feldwerte in der Werteliste enthalten sind."
		},
		"en-US":{
			"name" : "NotAllFieldValuesIncludedInValueList",
			"description": "True, if at least one value of a field in <FieldList> is not included in the value list (this list can be a list of string constants)."
		}
    },
	{
		"de-DE": {
			"name" : "KeinFeldWertInWerteListe",
			"description": "wahr, wenn kein Feldwert aus der ersten Liste in den Werten in der zweiten Liste vorkommt."
		},
		"en-US":{
			"name" : "NoFieldValueIncludedInValueList",
			"description": "True, if no value of any field in <FieldList> is included in the value list (this list can be a list of string constants)."
		}
    },
	{
		"de-DE": {
			"name" : "MindestensEinFeldWertInWerteListe",
			"description": "wahr, wenn mindestens ein Feldwert aus der ersten Liste in den Werten in der zweiten Liste vorkommt."
		},
		"en-US":{
			"name" : "AtLeastOneFieldValueIncludedInValueList",
			"description": "True, if at least one value of any field in <FieldList> is included in the value list (this list can be a list of string constants)."
		}
    },
	{
		"de-DE": {
			"name" : "Gueltig",
			"description": "wahr, wenn das konstruierte Datum ein gültiges Datum bzw. das Feld von dem spezifizierten vordefinierten Typ ist"
		},
		"en-US":{
			"name" : "Valid",
			"description": "True, if the constructed date is a valid date / the field is of the specified predefined type."
		}
    },
	{
		"de-DE": {
			"name" : "Ungueltig",
			"description": "wahr, wenn das konstruierte Datum kein gültiges Datum bzw. das Feld nicht von dem spezifizierten vordefinierten Typ ist"
		},
		"en-US":{
			"name" : "Invalid",
			"description": "True, if the constructed date is not a valid date / the field is not of the specified predefined type."
		}
    },
    {
		"de-DE": {
			"name" : "MindestensEinDatumsBereichUeberlappend",
			"description": "wahr, wenn sich der Datumsbereich des ersten Feldes mit dem Datumsbereich mindestens eines Feldes aus FeldListe überschneidet."
		},
		"en-US":{
			"name" : "AtLeastOneDateRangeOverlaps",
			"description": "True, if the date range of the first field overlaps with the date range of at least one field from the FieldList."
		}
    },
    {
		"de-DE": {
			"name" : "WertNichtKonsistent",
			"description": "wahr, wenn der Wert der angegebenen Enumeration mit Tabellenbezug nicht zu den angegebenen Werten der abhängigen Spalten der Enumeration passt."
		},
		"en-US":{
			"name" : "ValueNotConsistent",
			"description": "True, if the value of the given enumeration with table reference does not match the given values for the dependent columns of the enumeration."
		}
    }
]