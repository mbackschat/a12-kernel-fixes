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
import {
	ErrorLocalizationBundle
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import { CustomFieldTypeErrorEnum } from "../../CustomFieldTypeErrorEnum.js";

const bundle = new ErrorLocalizationBundle("en");
export default bundle;

const set: typeof bundle.set = bundle.set.bind(bundle);

set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BIC_FORMALER_FEHLER],
	"Invalid BIC: The formal structure of the provided BIC is invalid."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BIC_LAENDERCODE_FEHLER],
	"Invalid BIC: The provided country code is invalid or currently not supported."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUFANR_FORMALER_FEHLER],
	"Invalid Bundesfinanzamtsnummer: The formal structure of the provided Bundesfinanzamtsnummer is invalid. The Bundesfinanzamtsnummer has to contain exactly 4 digits."
);

set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUFANR_UNGUELTIGE_FINANZAMTSNR],
	"Invalid Bundesfinanzamtsnummer: The provided Bundesfinanzamtsnummer is invalid or currently not supported."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.DATUM_UNGUELTIG],
	"Invalid date: The date must be in the format $dateFormat$."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUNDESSTNR_UNGUELTIG], "Invalid Bundessteuernummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EMAIL_UNGUELTIG], "Invalid Email Address.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER], "Invalid Aktenzeichen.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FINANZAMT_FEHLER], "Invalid Aktenzeichen.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_PRUEFZIFFER_FEHLER], "Invalid Aktenzeichen.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FORMATSCHLUESSEL_FEHLER],
	"Invalid Aktenzeichen."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_FORMAT_FEHLER], "Invalid Ermittlungsnummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_FINANZAMT_FEHLER], "Invalid Ermittlungsnummer.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_UNTERSCHEIDUNGSNUMMER_FEHLER],
	"Invalid Ermittlungsnummer."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_PRUEFZIFFER_FEHLER], "Invalid Ermittlungsnummer.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STRING_ZU_KURZ],
	"Value too short: The value must contain at least $minLength$ characters."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STRING_ZU_LANG],
	"Value too long: The value must not exceed $maxLength$ characters."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_FORMALER_FEHLER],
	"Invalid IBAN: The IBAN has to consist of a two-digit country code followed by a two-digit check number followed by the Basic Bank Account Number."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LAENDERCODE_FEHLER],
	"Invalid IBAN: The provided country code is invalid or currently not supported."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LANDESFORMAT_FEHLER],
	"Invalid IBAN: The provided IBAN doesn't comply with the defined formal structure for the provided country."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_PRUEFZIFFER_FEHLER],
	"Invalid IBAN: The check digit calculation for the provided IBAN results in a differing check number."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LEERZEICHEN_UNGUELTIG],
	"Invalid IBAN: If the provided IBAN contains spaces, the spaces must be after every 4th character."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_NUR_FUER_TEST], "Invalid identification number.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_NICHT_NUMERISCH], "Invalid identification number.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_ZIFFERNFOLGE_FEHLER], "Invalid identification number.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_LAENGE_UNGUELTIG], "Invalid identification number.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_PRUEFZIFFER_UNGUELTIG], "Invalid identification number.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_LEERZEICHEN_UNGUELTIG],
	"Invalid identification number."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.LAENDERKUERZEL_UNGUELTIG],
	"Invalid country code: The provided country code is invalid or currently not supported."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER], "Invalid tax number.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FINANZAMT_FEHLER], "Invalid tax number.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER_BEZIRK], "Invalid tax number.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_NEGATIVMUSTER], "Invalid tax number.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_PRUEFZIFFER_FEHLER], "Invalid tax number.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BICINTERNATIONAL_FORMALER_FEHLER],
	"Invalid BIC (international): The formal structure of the provided BIC (international) is invalid."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_FORMALER_FEHLER],
	"Invalid IBAN (international): The IBAN (international) has to consist of a two-digit country code followed by a two-digit check number followed by the Basic Bank Account Number."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_LANDESFORMAT_FEHLER],
	"Invalid IBAN (international): The provided IBAN (international) doesn't comply with the defined formal structure for the provided country."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_PRUEFZIFFER_FEHLER],
	"Invalid IBAN (international): The check digit calculation for the provided IBAN (international) results in a differing check number."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_LEERZEICHEN_UNGUELTIG],
	"Invalid IBAN (international): If the provided IBAN contains spaces, the spaces must be after every 4th character."
);

