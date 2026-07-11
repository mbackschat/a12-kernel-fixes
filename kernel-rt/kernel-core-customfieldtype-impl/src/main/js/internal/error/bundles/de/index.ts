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

const bundle = new ErrorLocalizationBundle("de");
export default bundle;

const set: typeof bundle.set = bundle.set.bind(bundle);

declare function unescape(params: string): string;

set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BIC_FORMALER_FEHLER],
	"Ung" +
	unescape("%FC") +
	"ltiger BIC: Der formale Aufbau des angegebenen BIC ist ung" +
	unescape("%FC") +
	"ltig."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BIC_LAENDERCODE_FEHLER],
	"Ung" +
	unescape("%FC") +
	"ltiger BIC: Der angegebene L" +
	unescape("%E4") +
	"ndercode ist ung" +
	unescape("%FC") +
	"ltig oder wird aktuell im ELSTER-Verfahren nicht unterst" +
	unescape("%FC") +
	"tzt."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUFANR_FORMALER_FEHLER],
	"Ung" +
	unescape("%FC") +
	"ltige Bundesfinanzamtsnummer: Der formale Aufbau der angegebenen Bundesfinanzamtsnummer ist ung" +
	unescape("%FC") +
	"ltig. Die Bundesfinanzamtsnummer ist als vierstellige Zahl anzugeben."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUFANR_UNGUELTIGE_FINANZAMTSNR],
	"Ung" +
	unescape("%FC") +
	"ltige Bundesfinanzamtsnummer: Die Bundesfinanzamtsnummer ist ung" +
	unescape("%FC") +
	"ltig oder wird aktuell im ELSTER-Verfahren nicht unterst" +
	unescape("%FC") +
	"tzt."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.DATUM_UNGUELTIG],
	"Das Datum im Feld ist nicht g" +
	unescape("%FC") +
	"ltig. (Erforderliches Format: $dateFormat$)."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUNDESSTNR_UNGUELTIG],
	"Sie haben eine ung" + unescape("%FC") + "ltige Bundessteuernummer angegeben."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EMAIL_UNGUELTIG], "Ung" + unescape("%FC") + "ltige E-Mail-Adresse.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER],
	"Sie haben ein ung" + unescape("%FC") + "ltiges Aktenzeichen angegeben."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FINANZAMT_FEHLER],
	"Sie haben ein ung" + unescape("%FC") + "ltiges Aktenzeichen angegeben."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_PRUEFZIFFER_FEHLER],
	"Sie haben ein ung" + unescape("%FC") + "ltiges Aktenzeichen angegeben."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FORMATSCHLUESSEL_FEHLER],
	"Sie haben ein ung" + unescape("%FC") + "ltiges Aktenzeichen angegeben."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STRING_ZU_KURZ],
	"Das Feld muss mindestens $minLength$ Zeichen enthalten."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STRING_ZU_LANG],
	"Das Feld darf maximal $maxLength$ Zeichen enthalten."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_FORMALER_FEHLER],
	"Ung" +
	unescape("%FC") +
	"ltige IBAN: IBAN muss aus zweistelligem L" +
	unescape("%E4") +
	"ndercode gefolgt von zweistelliger Pr" +
	unescape("%FC") +
	"fziffer gefolgt von der Basic Bank Account Number bestehen."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LAENDERCODE_FEHLER],
	"Ung" +
	unescape("%FC") +
	"ltige IBAN: Der angegebene L" +
	unescape("%E4") +
	"ndercode ist ung" +
	unescape("%FC") +
	"ltig oder wird aktuell im ELSTER-Verfahren nicht unterst" +
	unescape("%FC") +
	"tzt."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LANDESFORMAT_FEHLER],
	"Ung" +
	unescape("%FC") +
	"ltige IBAN: Die angegebene IBAN entspricht nicht dem f" +
	unescape("%FC") +
	"r das angegebene Land definierten formalen Aufbau der IBAN."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_PRUEFZIFFER_FEHLER],
	"Ung" +
	unescape("%FC") +
	"ltige IBAN: Die Pr" +
	unescape("%FC") +
	"fziffernberechnung zur angegebenen IBAN f" +
	unescape("%FC") +
	"hrt zu einer abweichenden Pr" +
	unescape("%FC") +
	"fziffer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LEERZEICHEN_UNGUELTIG],
	"Ung" +
	unescape("%FC") +
	"ltige IBAN: Falls Sie die IBAN mit Leerzeichen angeben, m" +
	unescape("%FC") +
	"ssen die Leerzeichen nach jeder 4. Stelle sein."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_NUR_FUER_TEST],
	"Ung" + unescape("%FC") + "ltige Identifikationsnummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_NICHT_NUMERISCH],
	"Ung" + unescape("%FC") + "ltige Identifikationsnummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_ZIFFERNFOLGE_FEHLER],
	"Ung" + unescape("%FC") + "ltige Identifikationsnummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_LAENGE_UNGUELTIG],
	"Ung" + unescape("%FC") + "ltige Identifikationsnummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_PRUEFZIFFER_UNGUELTIG],
	"Ung" + unescape("%FC") + "ltige Identifikationsnummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_LEERZEICHEN_UNGUELTIG],
	"Ung" +
	unescape("%FC") +
	"ltige Identifikationsnummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.LAENDERKUERZEL_UNGUELTIG],
	"Ung" +
	unescape("%FC") +
	"ltiges L" +
	unescape("%E4") +
	"nderk" +
	unescape("%FC") +
	"rzel: Das L" +
	unescape("%E4") +
	"nderk" +
	unescape("%FC") +
	"rzel ist ung" +
	unescape("%FC") +
	"ltig oder wird aktuell im ELSTER-Verfahren nicht unterst" +
	unescape("%FC") +
	"tzt."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER],
	"Sie haben eine ung" + unescape("%FC") + "ltige Steuernummer angegeben."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FINANZAMT_FEHLER],
	"Sie haben eine ung" + unescape("%FC") + "ltige Steuernummer angegeben."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER_BEZIRK],
	"Sie haben eine ung" + unescape("%FC") + "ltige Steuernummer angegeben."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_NEGATIVMUSTER],
	"Sie haben eine ung" + unescape("%FC") + "ltige Steuernummer angegeben."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_PRUEFZIFFER_FEHLER],
	"Sie haben eine ung" + unescape("%FC") + "ltige Steuernummer angegeben."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BICINTERNATIONAL_FORMALER_FEHLER],
	"Ung" +
	unescape("%FC") +
	"ltiger BIC (International): Der formale Aufbau des angegebenen BIC (International) ist ung" +
	unescape("%FC") +
	"ltig."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_FORMALER_FEHLER],
	"Ung" +
	unescape("%FC") +
	"ltige IBAN (International): IBAN (International) muss aus zweistelligem L" +
	unescape("%E4") +
	"ndercode gefolgt von zweistelliger Pr" +
	unescape("%FC") +
	"fziffer gefolgt von der Basic Bank Account Number bestehen."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_LANDESFORMAT_FEHLER],
	"Ung" +
	unescape("%FC") +
	"ltige IBAN (International): Die angegebene IBAN (International) entspricht nicht dem f" +
	unescape("%FC") +
	"r das angegebene Land definierten formalen Aufbau der IBAN."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_PRUEFZIFFER_FEHLER],
	"Ung" +
	unescape("%FC") +
	"ltige IBAN (International): Die Pr" +
	unescape("%FC") +
	"fziffernberechnung zur angegebenen IBAN (International) f" +
	unescape("%FC") +
	"hrt zu einer abweichenden Pr" +
	unescape("%FC") +
	"fziffer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_LEERZEICHEN_UNGUELTIG],
	"Ung" +
	unescape("%FC") +
	"ltige IBAN (International): Falls Sie die IBAN mit Leerzeichen angeben, m" +
	unescape("%FC") +
	"ssen die Leerzeichen nach jeder 4. Stelle sein."
);
