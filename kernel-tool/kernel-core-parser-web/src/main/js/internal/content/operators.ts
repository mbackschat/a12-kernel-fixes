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
			"name" : "UngleichMitToleranz10",
			"description": "Nur für Zahlen und Geldbeträge zulässig. Die Vergleichsoperation ist wahr, falls die beiden verglichenen Werte ungleich mit einer Toleranz von 10 sind. D.h. der Absolut-Betrag von v1 –v2 ist größer 10."
		},
		"en-US":{
			"name" : "DiffersWithToleranceRange10",
			"description": "This comparison operation is true if the two values compared differ from each other with a tolerance of 10, i.e. the absolute value of <Number1> - <Number2> is greater than 10 (returns false, if the difference is exactly or smaller than 10). This comparison is only allowed for numbers and amounts."
		}
    },
	{
		"de-DE": {
			"name" : "UngleichMitToleranz5",
			"description": "Nur für Zahlen und Geldbeträge zulässig. Die Vergleichsoperation ist wahr, falls die beiden verglichenen Werte ungleich mit einer Toleranz von 5 sind. D.h. der Absolut-Betrag von v1 –v2 ist größer 5."
		},
		"en-US":{
			"name" : "DiffersWithToleranceRange5",
			"description": "This comparison operation is true if the two values compared differ from each other with a tolerance of 5, i.e. the absolute value of <Number1> - <Number2> is greater than 5 (returns false, if the difference is exactly or smaller than 5). This comparison is only allowed for numbers and amounts."
		}
    },
	{
		"de-DE": {
			"name" : "UngleichMitToleranz2",
			"description": "Nur für Zahlen und Geldbeträge zulässig. Die Vergleichsoperation ist wahr, falls die beiden verglichenen Werte ungleich mit einer Toleranz von 2 sind. D.h. der Absolut-Betrag von v1 –v2 ist größer 2."
		},
		"en-US":{
			"name" : "DiffersWithToleranceRange2",
			"description": "This comparison operation is true if the two values compared differ from each other with a tolerance of 2, i.e. the absolute value of <Number1> - <Number2> is greater than 2 (returns false, if the difference is exactly or smaller than 2). This comparison is only allowed for numbers and amounts."
		}
    },
	{
		"de-DE": {
			"name" : "UngleichMitToleranz1",
			"description": "Nur für Zahlen und Geldbeträge zulässig. Die Vergleichsoperation ist wahr, falls die beiden verglichenen Werte ungleich mit einer Toleranz von 1 sind. D.h. der Absolut-Betrag von v1 –v2 ist größer 1."
		},
		"en-US":{
			"name" : "DiffersWithToleranceRange1",
			"description": "This comparison operation is true if the two values compared differ from each other with a tolerance of 1, i.e. the absolute value of <Number1> - <Number2> is greater than 1 (returns false, if the difference is exactly or smaller than 1). This comparison is only allowed for numbers and amounts."
		}
    },
	{
		"de-DE": {
			"name" : "NichtWieMuster",
			"description": "Nur für Strings zulässig. Darf nur verwendet werden, wenn auf der linken Seite ein Feld spezifiziert ist, das vom Typ String ist und auf der rechten Seite ein gültiger regulärer Ausdruck spezifiziert wurde. Der Vergleich ist wahr, falls die linke Seite nicht dem regulären Ausdruck, der auf der rechten Seite spezifiziert wurde, vollständig entspricht. Die Syntax des regulären Ausdruckes stützt sich auf die Perl-Syntax."
		},
		"en-US":{
			"name" : "PatternViolated",
			"description": "May only be used when on the left side a field of type string is specified and on the right side a valid regular expression. The comparison is true if the left side does not match the regular expression that has been specified on the other side. Allowed only for strings."
		}
    },
	{
		"de-DE": {
			"name" : "WieMuster",
			"description": "Nur für Strings zulässig. Darf nur verwendet werden, wenn auf der linken Seite ein Feld spezifiziert ist, das vom Typ String ist und auf der rechten Seite ein gültiger regulärer Ausdruck spezifiziert wurde. Der Vergleich ist wahr, falls die linke Seite dem regulären Ausdruck, der auf der rechten Seite spezifiziert wurde, vollständig entspricht. Die Syntax des regulären Ausdruckes stützt sich auf die Perl-Syntax."
		},
		"en-US":{
			"name" : "PatternMatched",
			"description": "May only be used when on the left side a field of type string is specified and on the right side a valid regular expression. The comparison is true if the left side matches the regular expression that has been specified on the other side. Allowed only for strings."
		}
    },
	{
		"de-DE": {
			"name" : "ErsterTag",
			"description": "Parameter der benutzt wird um teilweise unvollständige Daten zu vervollständigen."
		},
		"en-US":{
			"name" : "FirstDay",
			"description": "FirstDay is a parameter used for completing only partially specified date values."
		}
    },
	{
		"de-DE": {
			"name" : "LetzterTag",
			"description": "Parameter der benutzt wird um teilweise unvollständige Daten zu vervollständigen."
		},
		"en-US":{
			"name" : "LastDay",
			"description": "LastDay is a parameter used for completing only partially specified date values."
		}
    },
    {
		"de-DE": {
			"name" : "Fuer",
			"description": "Fuer-Operator zur Spezifikation eines semantischen Index."
		},
		"en-US":{
			"name" : "For",
			"description": "For infix operator used to specify semantic indexes."
		}
    },
   	{
		"de-DE": {
			"name" : "IN",
			"description": ""
		},
		"en-US":{
			"name" : "IN",
			"description": "IN language element used for checking whether a value is part of a list of values."
		}
    },
    {
		"de-DE": {
			"name" : "In",
			"description": ""
		},
		"en-US":{
			"name" : "In",
			"description": "IN language element used for checking whether a value is part of a list of values."
		}
    },
   	{
		"de-DE": {
			"name" : "in",
			"description": ""
		},
		"en-US":{
			"name" : "in",
			"description": "IN language element used for checking whether a value is part of a list of values."
		}
    },
	{
		"de-DE": {
			"name" : "@Von",
			"description": ""
		},
		"en-US":{
			"name" : "@From",
			"description": "@From language element used for specifying repeatable base directory for RepetitionNotUnique."
		}
    },
    {
		"de-DE": {
			"name" : "zu",
			"description": ""
		},
		"en-US":{
			"name" : "to",
			"description": ""
		}
    },
    {
		"de-DE": {
			"name" : "Zu",
			"description": ""
		},
		"en-US":{
			"name" : "To",
			"description": ""
		}
    },
    {
		"de-DE": {
			"name" : "ZU",
			"description": ""
		},
		"en-US":{
			"name" : "TO",
			"description": ""
		}
    },
    {
    	"de-DE": {
    		"name" : "UnterdrueckeWarnung",
    		"description": "Unterdrückt bestimmte Warnungen bei der Analyse der Regel. Wird '@SuppressWarning(<Error>)', dem Bedingungstext vorangestellt, so wird für unterstützte Fehler der Fehler unterdrückt."
    	},
    	"en-US": {
    		"name" : "SuppressWarning",
    		"description": "Suppress special warnings during parsing of the condition. Prepend the condition text with @SuppressWarning(<Error>)."
    	} 
    },
   	{
		"de-DE": {
			"name" : "Mit",
			"description": "Mit-Operator zur Angabe einer Filterbedingung zur Einschränkung von Sternoperatoren"
		},
		"en-US":{
			"name" : "Having",
			"description": "Having infix operator to specify a filter condition used to filter star operations"
		}
    }
]
