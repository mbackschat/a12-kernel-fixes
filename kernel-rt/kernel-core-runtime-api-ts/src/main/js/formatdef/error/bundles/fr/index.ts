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

const bundle = new ErrorLocalizationBundle("fr");
export default bundle;

const set: typeof bundle.set = bundle.set.bind(bundle);

set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.KEIN_FEHLER], "");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_ONK],
	"La valeur doit être un nombre entier."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKOVZ],
	"Seules les valeurs au format '0$decimalSeparator$$zeros$' avec exactement $numberOfDecimalPlaces$ décimales sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKPVZ],
	"Seules les valeurs au format '0$decimalSeparator$$zeros$' ou '-0$decimalSeparator$$zeros$' avec exactement $numberOfDecimalPlaces$ décimales sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_VNK],
	"Seules les valeurs avec $numberOfDecimalPlaces$ à $maxNumberOfDecimalPlaces$ décimales sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN],
	"Seuls les nombres sont autorisés."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STELLIGKEIT_ZU_LANG],
	"Seules les valeurs contenant jusqu'à 15 chiffres sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ENTHAELT_VORZEICHEN],
	"Seules les valeurs ne contenant pas de signe sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ENTHAELT_PLUS],
	"La valeur '0' ainsi que les valeurs positives ne doivent pas être précédées d'un signe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_HAT_DEZIMALTRENNER],
	"La valeur doit être un nombre entier."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_NULL_ENTHAELT_VORZEICHEN],
	"La valeur '0' ainsi que les valeurs positives ne doivent pas être précédées d'un signe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Seules les valeurs contenant jusqu'à $numberOfDigitsBeforeDecimalPoint$ chiffres avant la virgule sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Seules les valeurs contenant jusqu'à $numberOfDigitsBeforeDecimalPoint$ chiffres et un signe négatif facultatif sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZONK],
	"Seules les valeurs contenant jusqu'à $numberOfDigitsBeforeDecimalPoint$ chiffres sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZONK],
	"Seules les valeurs contenant jusqu'à $numberOfDigitsBeforeDecimalPoint$ chiffres et un signe négatif facultatif sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_IST_NULL_OVZ],
	"Seules les valeurs supérieures à 0 sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_IST_NULL],
	"Seules les valeurs non égales à 0 sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_HAT_FUEHRENDE_NULLEN],
	"Seules les valeurs ne commençant pas par des zéros sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_KURZ],
	"Seules les valeurs contenant au moins $minLength$ chiffres sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_LANG],
	"Seules les valeurs contenant jusqu'à $maxLength$ chiffres sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_KLEIN],
	"Seules les valeurs supérieures ou égales à $minValue$ sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_GROSS],
	"Seules les valeurs inférieures ou égales à $maxValue$ sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_OVZ],
	"Valeur non valide: les montants totaux doivent être saisis sans espace, sans séparateurs de milliers et sans virgule."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_MVZ],
	"Valeur non valide: les montants totaux doivent être saisis sans espace, sans séparateurs de milliers et sans virgule."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ENTHAELT_VORZEICHEN],
	"Signe non valide: les montants doivent être saisis sans signe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ENTHAELT_PLUS],
	"Signe non valide: le montant '0' ainsi que les montants positifs doivent être saisis sans signe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_NULL_ENTHAELT_VORZEICHEN],
	"Signe non valide: le montant '0' ainsi que les montants positifs doivent être saisis sans signe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Valeur non valide: la valeur ne doit pas dépasser $numberOfDigitsBeforeDecimalPoint$ chiffres."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Valeur non valide: la valeur ne doit pas dépasser $numberOfDigitsBeforeDecimalPoint$ chiffres."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_HAT_FUEHRENDE_NULLEN],
	"Valeur non valide: la valeur ne doit pas commencer par des zéros."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_KURZ],
	"Seules les valeurs contenant au moins $minLength$ chiffres sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_LANG],
	"Seules les valeurs contenant jusqu'à $maxLength$ chiffres sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_KLEIN],
	"Valeur trop petite: la valeur doit être supérieure ou égale à $minValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_GROSS],
	"Valeur trop grande: la valeur doit être inférieure ou égale à $maxValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_OVZ],
	"Valeur non valide: La valeur doit être au format '0$decimalSeparator$00' avec exactement deux décimales."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_MVZ],
	"Valeur non valide: La valeur doit être au format '0$decimalSeparator$00' ou '-0$decimalSeparator$00' avec exactement deux décimales."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ENTHAELT_VORZEICHEN],
	"Signe non valide: les montants doivent être saisis sans signe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ENTHAELT_PLUS],
	"Signe non valide: Le montant 0$decimalSeparator$00 ainsi que les montants positifs doivent être saisis sans signe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_NULL_ENTHAELT_VORZEICHEN],
	"Signe non valide: Le montant 0$decimalSeparator$00 ainsi que les montants positifs doivent être saisis sans signe."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Valeur trop longue: La valeur ne doit pas dépasser $numberOfDigitsBeforeDecimalPoint$ chiffres avant la virgule."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Valeur non valide: la valeur ne doit pas dépasser $numberOfDigitsBeforeDecimalPoint$ chiffres."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_HAT_FUEHRENDE_NULLEN],
	"Valeur non valide: la valeur ne doit pas commencer par des zéros."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_KURZ],
	"Seules les valeurs contenant au moins $minLength$ chiffres sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_LANG],
	"Seules les valeurs contenant jusqu'à $maxLength$ chiffres sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_KLEIN],
	"Valeur trop petite: la valeur doit être supérieure ou égale à $minValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_GROSS],
	"Valeur trop grande: la valeur doit être inférieure ou égale à $maxValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ZU_KURZ],
	"Seules les valeurs contenant au moins $minLength$ caractères sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ZU_LANG],
	"Seules les valeurs contenant jusqu'à $maxLength$ caractères sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ENTHAELT_NEWLINE],
	"Seules les valeurs ne contenant pas de sauts de ligne sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FELD_JA_NEIN],
	"Valeur non valide: La valeur doit être '$trueValue$' (pour Oui) ou '$falseValue$' (pour Non)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FELD_JA],
	"Valeur non valide: La valeur peut seulement être '$trueValue$' (pour Oui)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_FORMAT],
	"La date n'est pas valide. (Format nécessaire : '$dateFormat$')."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM],
	"Seules les dates à partir du 16/10/1583 sont autorisées (format nécessaire: $dateFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_ADDITIONAL_CHECK],
	"La date doit être après l'année 1900 (format nécessaire: $dateFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_TAG_FEHLER],
	"Only dates in the format $dateFormat$ are allowed. '$dateString$' is not a valid date because the day '$dateStringDay$' does not exist in the selected month."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_FEHLER],
	"Only dates in the format $dateFormat$ are allowed. '$dateStringMonth$' is not a valid month."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_FEBRUAR_29_FEHLER],
	"Only dates in the format $dateFormat$ are allowed. '$dateString$' is not a valid day, since $dateStringYear$ is not a leap year."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_FEHLT],
	"Un mois doit être indiqué et il ne doit pas être '00'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_FEHLT0],
	"Un mois doit être indiqué et il ne doit pas être '0'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_JAHR_FEHLT],
	"Une année doit être indiquée et elle ne doit pas être '0000'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN],
	"Etant donné que le mois a été indiqué avec '00', le jour doit également être '00'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN0],
	"Le mois ne doit pas être indiqué avec '0'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_JAHROPTIONAL_MONAT_ODER_TAG_ANGEGEBEN],
	"Etant donné que l'année a été indiquée avec '0000', le jour et le mois doivent également être '00'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_0],
	"Le mois ne doit pas être '0'. Le mois exact ou '00' doit être indiqué."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_TAG_0],
	"Le jour ne doit pas être '0'. Le jour exact ou '00' doit être indiqué."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_TAG_0_STATT_00],
	"Le jour a été indiqué avec '0', alors qu'il doit être '00'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_0_STATT_00],
	"Le mois a été indiqué avec '0', alors qu'il doit être '00'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_FORMAT],
	"Seules les plages de dates au format $timeIntervalFormat$ sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_NICHT_GUELTIG],
	"La date de fin doit être antérieure à la date de début (format nécessaire: $timeIntervalFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM],
	"Plage de dates hors limites: L'intervalle ne doit pas commencer avant le 16/10/1583 (format nécessaire: $timeIntervalFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM_ADDITIONAL_CHECK],
	"Plage de dates hors limites: L'intervalle ne doit pas commencer avant l'année 1900 (format nécessaire: $timeIntervalFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_START_TAG_FEHLER],
	"Only date ranges in the format $timeIntervalFormat$ are allowed. Start date: '$dateString$' is not a valid date because the day '$dateStringDay$' does not exist in the selected month."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_START_MONAT_FEHLER],
	"Only date ranges in the format $timeIntervalFormat$ are allowed. Start date: '$dateStringMonth$' is not a valid month."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_START_FEBRUAR_29_FEHLER],
	"Only date ranges in the format $timeIntervalFormat$ are allowed. Start date: '$dateString$' is not a valid day, since $dateStringYear$ is not a leap year."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_END_TAG_FEHLER],
	"Only date ranges in the format $timeIntervalFormat$ are allowed. End date: '$dateString$' is not a valid date because the day '$dateStringDay$' does not exist in the selected month."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_END_MONAT_FEHLER],
	"Only date ranges in the format $timeIntervalFormat$ are allowed. End date: '$dateStringMonth$' is not a valid month."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_END_FEBRUAR_29_FEHLER],
	"Only date ranges in the format $timeIntervalFormat$ are allowed. End date: '$dateString$' is not a valid day, since $dateStringYear$ is not a leap year."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_FM_TEXT],
	"Le champ appartient à un groupe source avec un numéro non valide."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_ZU_GROSS_FM_TEXT],
	"L'index est plus grand que l'index maximum autorisé."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_ZEILE_FM_TEXT],
	"Le numéro de ligne n'est pas valide."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_ZEILENNUMMER_ZU_GROSS_FM_TEXT],
	"Le numéro de ligne est plus grand que la valeur maximale autorisée."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_WERT_ZU_LANG_FM_TEXT],
	"La valeur excède le nombre maximal de charactères."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_FUEHRENDES_BLANK_FM_TEXT],
	"Seules les valeurs non précédées d'espaces sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_NACHFOLGENDES_BLANK_FM_TEXT],
	"Seules les valeurs non suivies d'espaces sont autorisées."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_FELD_UNBEKANNT],
	"Le champ n'a pas été trouvé dans le modèle."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZEICHEN_NICHT_IM_ZEICHENSATZ_FM_TEXT],
	"Le champ contient un ou plusieurs symboles incompatibles."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FALSCHE_ANZAHL_INDICES_FM_TEXT],
	"Champ '$fieldPathName$': Mauvais nombre d'index: $numberOfIndicesSpecified$ index ont été spécifiés, alors que $numberOfIndicesExpected$ index sont attendus."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.KONTEXTNUMMER_ZU_KLEIN_FM_TEXT],
	"L'index du groupe '$groupName$' a été spécifié par $groupIndexSpecified$, mais seuls les nombres supérieurs à zéro sont autorisés."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_KONTEXTNUMMER_ZU_GROSS_FM_TEXT],
	"L'index du groupe '$groupName$' a été spécifié par $groupIndexSpecified$, mais au plus $maxGroupIndex$ est autorisé."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.BERECHNUNG_WERT_FEHLER],
	"La valeur calculée n'est pas valide pour le champ."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER], "Ce champ est obligatoire.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.INDEX_FELD_FEHLER],
	"Les valeurs du champ doivent être uniques."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.COMMA_PLURAL], "virgules");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.POINT_PLURAL], "points");
set(DIVISION_BY_ZERO_INVALID, "La division par zéro n'est pas possible.");
set(
	ERROR_DURING_DATA_CHECK_EVAL,
	"Une erreur est survenue lors de la vérification des données: $error$"
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ENUMERATION_DEFAULT_ERROR], "La valeur n'est pas incluse dans la liste.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.SOLITARY_INDEX_FEHLER],
	"La valeur '$Feld.Wert$' était donner '$fieldName.Value$' pour le champ '$fieldName$', mais aucun champs n'a été spécifié."
);
