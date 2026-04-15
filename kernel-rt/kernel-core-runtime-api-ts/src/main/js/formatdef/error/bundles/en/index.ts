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

const bundle = new ErrorLocalizationBundle("en");
export default bundle;

const set: typeof bundle.set = bundle.set.bind(bundle);

set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.KEIN_FEHLER], "");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_ONK], "The value must be integer.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKOVZ],
	"Only values of the format '0$decimalSeparator$$zeros$' with exactly $numberOfDecimalPlaces$ " +
	"decimal places are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKPVZ],
	"Only values of the format '0$decimalSeparator$$zeros$' or '-0$decimalSeparator$$zeros$' " +
	"with exactly $numberOfDecimalPlaces$ decimal places are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_VNK],
	"Only values with $numberOfDecimalPlaces$ to $maxNumberOfDecimalPlaces$ decimal places are allowed."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN], "Only numbers are allowed.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STELLIGKEIT_ZU_LANG], "Only values with up to 15 digits are allowed.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ENTHAELT_VORZEICHEN], "Only values without a sign are allowed.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ENTHAELT_PLUS],
	"The value '0' as well as positive values must be provided without a sign."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_HAT_DEZIMALTRENNER], "The value must be integer.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_NULL_ENTHAELT_VORZEICHEN],
	"The value '0' as well as positive values must be provided without a sign."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Only values with up to $numberOfDigitsBeforeDecimalPoint$ digits before the decimal point are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Only values with up to $numberOfDigitsBeforeDecimalPoint$ digits and an optional negative sign before the decimal point are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZONK],
	"Only values with up to $numberOfDigitsBeforeDecimalPoint$ digits are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZONK],
	"Only values with up to $numberOfDigitsBeforeDecimalPoint$ digits and an optional negative sign are allowed."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_IST_NULL_OVZ], "Only values greater than 0 are allowed.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_IST_NULL], "Only values not equal to 0 are allowed.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_HAT_FUEHRENDE_NULLEN],
	"Only values without leading zeros are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_KURZ],
	"Only values with at least $minLength$ digits are allowed."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_LANG], "Only values with up to $maxLength$ digits are allowed.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_KLEIN],
	"Only values greater or equal to $minValue$ are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZAHL_ZU_GROSS],
	"Only values smaller than or equal to $maxValue$ are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_OVZ],
	"Invalid value: Full money amounts must be provided as a digit sequence without decimal point."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_MVZ],
	"Invalid value: Full money amounts must be provided as a digit sequence without decimal point."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ENTHAELT_VORZEICHEN],
	"Invalid sign: Money amounts must be provided without a sign."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ENTHAELT_PLUS],
	"Invalid sign: The amount '0' as well as positive amounts must be provided without a sign."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_NULL_ENTHAELT_VORZEICHEN],
	"Invalid sign: The amount '0' as well as positive amounts must be provided without a sign."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Invalid value: The value must not exceed $numberOfDigitsBeforeDecimalPoint$ digits."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Invalid value: The value must not exceed $numberOfDigitsBeforeDecimalPoint$ digits and a negative sign."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_HAT_FUEHRENDE_NULLEN],
	"Invalid value: The value must not contain leading zeros."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_KURZ],
	"Only values with at least $minLength$ digits are allowed."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_LANG], "Only values with up to $maxLength$ digits are allowed.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_KLEIN],
	"Value too small: The value must be greater or equal to $minValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBOC_ZU_GROSS],
	"Value too big: The value must be less or equal to $maxValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_OVZ],
	"Invalid value: The value must be in the format '0$decimalSeparator$00' with exactly two decimal places."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_MVZ],
	"Invalid value: The value must be in the format '0$decimalSeparator$00' or '-0$decimalSeparator$00' with exactly two decimal places."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ENTHAELT_VORZEICHEN],
	"Invalid sign: Money amounts must be provided without a sign."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ENTHAELT_PLUS],
	"Invalid sign: The amount 0$decimalSeparator$00 as well as positive amounts must be provided without a sign."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_NULL_ENTHAELT_VORZEICHEN],
	"Invalid sign: The amount 0$decimalSeparator$00 as well as positive amounts must be provided without a sign."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_OVZ],
	"Value too long: The value must not exceed $numberOfDigitsBeforeDecimalPoint$ digits before the decimal point."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_MVZ],
	"Value too long: The value must not exceed $numberOfDigitsBeforeDecimalPoint$ digits and a negative sign before the decimal point."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_HAT_FUEHRENDE_NULLEN],
	"Invalid value: The value must not contain leading zeros."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_KURZ],
	"Only values with at least $minLength$ digits are allowed."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_LANG], "Only values with up to $maxLength$ digits are allowed.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_KLEIN],
	"Value too small: The value must be greater or equal to $minValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.GBMC_ZU_GROSS],
	"Value too big: The value must be less or equal to $maxValue$."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ZU_KURZ],
	"Only values with at least $minLength$ characters are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ZU_LANG],
	"Only values with up to $maxLength$ characters are allowed."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.STRING_ENTHAELT_NEWLINE], "Only values without line breaks are allowed.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FELD_JA_NEIN],
	"Invalid value: The value must either be '$trueValue$' (for Yes) or '$falseValue$' (for No)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FELD_JA],
	"Invalid value: The value may only be '$trueValue$' (for Yes)."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_FORMAT], "Only dates in the format $dateFormat$ are allowed.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM],
	"Only dates in the format $dateFormat$ are allowed. The date must not be before 10/16/1583."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_ADDITIONAL_CHECK],
	"Only dates in the format $dateFormat$ are allowed. The date must be after the year 1900."
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
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_FEHLT], "A month must be specified and it must not be '00'.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_FEHLT0], "A month must be specified and it must not be '0'.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_JAHR_FEHLT], "A year must be specified and it must not be '0000'.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN],
	"Since the month was specified with '00', the day must also be '00'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN0],
	"The month must not be specified with '0'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_JAHROPTIONAL_MONAT_ODER_TAG_ANGEGEBEN],
	"Since the year was specified with '0000', both the day and month must be '00'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_0],
	"The month must not be '0'. An exact month or '00' must be specified."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_TAG_0],
	"The day must not be '0'. An exact day or '00' must be specified."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_TAG_0_STATT_00],
	"The day was specified with '0', although it must be '00'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_MONAT_0_STATT_00],
	"The month was specified with '0', although it must be '00'."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_FORMAT],
	"Only date ranges in the format $timeIntervalFormat$ are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_NICHT_GUELTIG],
	"The end date must not be before the start date (necessary format: $timeIntervalFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM],
	"Date range out of range: The interval must not start before 10/16/1583 (necessary format: $timeIntervalFormat$)."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM_ADDITIONAL_CHECK],
	"Date range out of range: The interval must not start before the year 1900 (necessary format: $timeIntervalFormat$)."
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
	"The field belongs to a rootgroup with invalid number."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_ZU_GROSS_FM_TEXT],
	"The index is bigger than the maximum allowed index."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_ZEILE_FM_TEXT], "The row number is invalid.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_ZEILENNUMMER_ZU_GROSS_FM_TEXT],
	"The row number is bigger than the maximum allowed value."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_WERT_ZU_LANG_FM_TEXT],
	"The value exceeds the maximum number of characters."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_FUEHRENDES_BLANK_FM_TEXT],
	"Only values without leading spaces are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_NACHFOLGENDES_BLANK_FM_TEXT],
	"Only values without trailing spaces are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_FELD_UNBEKANNT],
	"The field was not found in the model."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ZEICHEN_NICHT_IM_ZEICHENSATZ_FM_TEXT],
	"The field contains one or several unsupported signs."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FALSCHE_ANZAHL_INDICES_FM_TEXT],
	"Field '$fieldPathName$': Wrong number of indices: $numberOfIndicesSpecified$ indices were specified, while $numberOfIndicesExpected$ indices are expected."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.KONTEXTNUMMER_ZU_KLEIN_FM_TEXT],
	"The index for group '$groupName$' was specified with $groupIndexSpecified$, but only numbers greater than 0 are allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.FEHLER_KONTEXTNUMMER_ZU_GROSS_FM_TEXT],
	"The index for group '$groupName$' was specified with $groupIndexSpecified$, but at most $maxGroupIndex$ is allowed."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.BERECHNUNG_WERT_FEHLER],
	"The calculated value is not valid for the field."
);
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.DECIMAL_SEPERATOR_INVALID], "Invalid Decimal Seperator.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.COMMA_PLURAL], "commas");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.POINT_PLURAL], "points");
set(DIVISION_BY_ZERO_INVALID, "Division by zero is not possible");
set(ERROR_DURING_DATA_CHECK_EVAL, "An error occurred while evaluation the data check: $error$");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER], "This field is required.");
set(RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.INDEX_FELD_FEHLER], "The values of the field must be unique.");
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.ENUMERATION_DEFAULT_ERROR],
	"The value is not allowed for the enumeration."
);
set(
	RuntimeFormalErrorEnum[RuntimeFormalErrorEnum.SOLITARY_INDEX_FEHLER],
	"You specified '$fieldName.Value$' for '$fieldName$', but you did not specify additional values."
);
