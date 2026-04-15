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

const bundle = new ErrorLocalizationBundle("nl");
export default bundle;

const set: typeof bundle.set = bundle.set.bind(bundle);

set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.KEIN_FEHLER], "");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_ONK], "Gelieve een getal invullen.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKOVZ],
	"Er mogen alleen waardes worden ingevuld, die aan het formaat '0$decimalSeparator$$zeros$' met precies $numberOfDecimalPlaces$ decimalen voldoen."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKPVZ],
	"Er mogen alleen waardes worden ingevuld, die of aan het formaat '0$decimalSeparator$$zeros$' of '-0$decimalSeparator$$zeros$' met precies $numberOfDecimalPlaces$ decimalen voldoen."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_VNK],
	"Er mogen alleen waardes van ten minste $numberOfDecimalPlaces$ tot maximaal $maxNumberOfDecimalPlaces$ decimalen worden ingevuld."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN], "Gelieve een getal invullen.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STELLIGKEIT_ZU_LANG],
	"Er mogen alleen waardes van maximaal 15 cijfers worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ENTHAELT_VORZEICHEN],
	"Er mogen alleen waardes zonder voorteken worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ENTHAELT_PLUS],
	"Alle positieve getallen incluis '0' dienen zonder voorteken te worden ingevuld."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_HAT_DEZIMALTRENNER], "Gelieve een gehele getal invullen (integer).");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_NULL_ENTHAELT_VORZEICHEN],
	"Alle positieve getallen incluis '0' dienen zonder voorteken te worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Er mogen alleen waardes van $numberOfDigitsBeforeDecimalPoint$ cijfers voor het decimalteken worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Er mogen alleen waardes van maximaal $numberOfDigitsBeforeDecimalPoint$ cijfers voor het decimalteken en een optioneel minteken worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZONK],
	"Er mogen alleen waardes van $numberOfDigitsBeforeDecimalPoint$ cijfers worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZONK],
	"Er mogen alleen waardes van maximaal $numberOfDigitsBeforeDecimalPoint$ cijfers voor het decimalteken en een optioneel minteken worden ingevuld."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_IST_NULL_OVZ], "Gelieve een waarde groter dan '0' invullen.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_IST_NULL], "Gelieve een waarde anders dan '0' invullen.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_HAT_FUEHRENDE_NULLEN],
	"Er mogen alleen waardes zonder voorafgaande nullen worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_KURZ],
	"Er mogen alleen waardes van ten minste $minLength$ cijfers worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_LANG],
	"Er mogen alleen waardes van maximaal $maxLength$ cijfers worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_KLEIN],
	"Gelieve een waarde groter dan of gelijk aan $minValue$ invullen."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_GROSS],
	"Gelieve een waarde kleiner dan of gelijk aan $maxValue$ invullen."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_OVZ],
	"Ongeldige waarde: Geldbedragen dienen zonder spaties, scheidings- e/o decimaaltekens te worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_MVZ],
	"Ongeldige waarde: Geldbedragen dienen zonder spaties, scheidings- e/o decimaaltekens te worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ENTHAELT_VORZEICHEN],
	"Ongeldige teken: Geldbedragen dienen zonder voorteken te worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ENTHAELT_PLUS],
	"Ongeldige teken: Alle positieve getallen incluis '0' dienen zonder voorteken te worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_NULL_ENTHAELT_VORZEICHEN],
	"Ongeldige teken: Alle positieve getallen incluis '0' dienen zonder voorteken te worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Ongeldige waarde: De waarde mag niet meer dan $numberOfDigitsBeforeDecimalPoint$ cijfers bevatten."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Ongeldige waarde: De waarde mag niet meer dan $numberOfDigitsBeforeDecimalPoint$ cijfers en een negatief voorteken bevatten."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_HAT_FUEHRENDE_NULLEN],
	"Ongeldige waarde: Er mogen alleen waardes zonder voorafgaande nullen worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_KURZ],
	"Er mogen alleen waardes van ten minste $minLength$ cijfers worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_LANG],
	"Er mogen alleen waardes van maximaal $maxLength$ cijfers worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_KLEIN],
	"Waarde te klein: De waarde moet groter of gelijk zijn aan $minValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_GROSS],
	"Waarde te groot: De waarde moet kleiner of gelijk zijn aan $maxValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_OVZ],
	"Ongeldige waarde: Er mogen alleen waardes worden ingevuld, die aan het formaat '0$decimalSeparator$00' met precies twee decimalen voldoen."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_MVZ],
	"Ongeldige waarde: Er mogen alleen waardes worden ingevuld, die aan het formaat '0$decimalSeparator$00' of '-0$decimalSeparator$00' met precies twee decimalen voldoen."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ENTHAELT_VORZEICHEN],
	"Ongeldige teken: Geldbedragen dienen zonder voorteken te worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ENTHAELT_PLUS],
	"Ongeldige teken: De waarde 0$decimalSeparator$00 en alle positieve getallen dienen zonder voorteken te worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_NULL_ENTHAELT_VORZEICHEN],
	"Ongeldige teken: De waarde 0$decimalSeparator$00 en alle positieve getallen dienen zonder voorteken te worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Waarde te lang: De waarde mag niet meer dan $numberOfDigitsBeforeDecimalPoint$ cijfers vóór het decimaalteken bevatten."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Waarde te lang: De waarde mag niet meer dan $numberOfDigitsBeforeDecimalPoint$ cijfers en een minteken vóór het decimaalteken bevatten."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_HAT_FUEHRENDE_NULLEN],
	"Ongeldige waarde: Er mogen alleen waardes zonder voorafgaande nullen worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_KURZ],
	"Er mogen alleen waardes van ten minste $minLength$ cijfers worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_LANG],
	"Er mogen alleen waardes van maximaal $maxLength$ cijfers worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_KLEIN],
	"Waarde te klein: De waarde moet groter of gelijk zijn aan $minValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_GROSS],
	"Waarde te groot: De waarde moet kleiner of gelijk zijn aan $maxValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ZU_KURZ],
	"Alleen waardes met minimaal $minLength$ tekens zijn toegestaan."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ZU_LANG],
	"Alleen waardes met maximaal $maxLength$ tekens zijn toegestaan."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ENTHAELT_NEWLINE],
	"De waarde dient in één regel te staan."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FELD_JA_NEIN],
	"Ongeldige waarde: De waarde moet of '$trueValue$' (d.w.z. Ja) of '$falseValue$' (d.w.z. Nee) zijn."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FELD_JA],
	"Ongeldige waarde: De waarde mag alleen '$trueValue$' (d.w.z. Ja) zijn."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_FORMAT], "De datum is ongeldig. (Vereist formaat: '$dateFormat$').");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM],
	"Alleen datums vanaf 16 oktober 1583 zijn toegestaan (geëist formaat: $dateFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_ADDITIONAL_CHECK],
	"De datum moet na het jaar 1900 liggen (geëist formaat: $dateFormat$)."
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
	"De maand ontbreekt en de waarde mag niet '00' zijn."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_FEHLT0],
	"De maand ontbreekt en de waarde mag niet '0' zijn."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_JAHR_FEHLT],
	"Het jaar ontbreekt en de waarde mag niet '0000' zijn."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN],
	"Omdat de maand is ingevuld met '00', dient ook de dag '00' te zijn."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN0], "De maand mag niet '0' zijn.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_JAHROPTIONAL_MONAT_ODER_TAG_ANGEGEBEN],
	"Omdat het jaar is ingevuld met '0000', dienen én de dag én de maand '00' te zijn."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_0],
	"De maand mag niet '0' zijn. Er moet een exacte maand of '00' worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_TAG_0],
	"De dag mag niet '0' zijn. Er moet een exacte dag of '00' worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_TAG_0_STATT_00],
	"De dag was ingevuld met '0' hoewel het formaat '00' dient te zijn."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_0_STATT_00],
	"De maand was ingevuld met '0' hoewel het formaat '00' dient te zijn."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_FORMAT],
	"Alleen datumbereiken van het formaat $timeIntervalFormat$ zijn toegestaan."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_NICHT_GUELTIG],
	"De einddatum mag niet voor de startdatum liggen (geëist formaat: $timeIntervalFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM],
	"Ongeldig datumbereik: De begin- en einddatum mogen niet voor 16 oktober 1583 liggen (geëist formaat: $timeIntervalFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM_ADDITIONAL_CHECK],
	"Ongeldig datumbereik: Het interval mag niet voor het jaar 1900 beginnen (geëist formaat: $timeIntervalFormat$)."
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
	"Het veld behoort tot een bron met ongeldig nummer."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_ZU_GROSS_FM_TEXT],
	"De index is groter dan de maximaal toegestane index."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_ZEILE_FM_TEXT], "Het rijnummer is ongeldig.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_ZEILENNUMMER_ZU_GROSS_FM_TEXT],
	"Het rijnummer is groter dan de maximaal toegestane waarde."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_WERT_ZU_LANG_FM_TEXT],
	"De waarde bevat meer dan de maximaal toegestane aantal tekens."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_FUEHRENDES_BLANK_FM_TEXT],
	"Er mogen alleen waardes zonder voorafgaande spaties worden ingevuld."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_NACHFOLGENDES_BLANK_FM_TEXT],
	"Alleen waardes zonder spaties zijn toegestaan."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_FELD_UNBEKANNT],
	"Het veld is niet gevonden in het model."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZEICHEN_NICHT_IM_ZEICHENSATZ_FM_TEXT],
	"Het veld bevat één of meerdere ongefundeerde tekens."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FALSCHE_ANZAHL_INDICES_FM_TEXT],
	"Veld' $fieldPathName$': Foutief aantal indices: $numberOfIndicesSpecified$ indices werden gespecificeerd, terwijl $numberOfIndicesExpected$ indices worden verwacht."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.KONTEXTNUMMER_ZU_KLEIN_FM_TEXT],
	"De index voor groep' $groupName$' werd gespecificeerd met $groupIndexSpecified$, maar alleen getallen groter dan 0 zijn toegestaan."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_KONTEXTNUMMER_ZU_GROSS_FM_TEXT],
	"De index voor de groep' $groupName$' werd gespecificeerd met $groupIndexSpecified$, maar hooguit $maxGroupIndex$ is toegestaan."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.BERECHNUNG_WERT_FEHLER],
	"De berekende waarde is niet geldig voor het veld."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER], "Dit is een verplicht veld.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.INDEX_FELD_FEHLER], "De waardes van het veld moeten uniek zijn.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.COMMA_PLURAL], "komma's");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.POINT_PLURAL], "punten");
set(DIVISION_BY_ZERO_INVALID, "Delen door null niet mogelijk.");
set(ERROR_DURING_DATA_CHECK_EVAL, "Bij de evaluatie van de gegevens is een fout opgetreden: $error$");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ENUMERATION_DEFAULT_ERROR], "De waarde staat niet in de lijst.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.SOLITARY_INDEX_FEHLER],
	"Er is de waarde '$fieldName.Value$' opgegeven voor veld '$fieldName$', maar u heeft geen aanvullende waarden opgegeven."
);
