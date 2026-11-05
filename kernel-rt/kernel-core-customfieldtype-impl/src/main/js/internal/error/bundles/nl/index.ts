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

const bundle = new ErrorLocalizationBundle("nl");
export default bundle;

const set: typeof bundle.set = bundle.set.bind(bundle);

set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BIC_FORMALER_FEHLER],
	"Ongeldige BIC: De formele structuur van de opgegeven BIC is ongeldig."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BIC_LAENDERCODE_FEHLER],
	"Ongeldige BIC: De opgegeven landcode is ongeldig of wordt momenteel niet ondersteund."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUFANR_FORMALER_FEHLER],
	"Ongeldig Bundesfinanzamtsnummer: De formele structuur van het verstrekte Bundesfinanzamtsnummer is ongeldig. Het Bundesfinanzamtsnummer moet exact 4 cijfers bevatten."
);

set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUFANR_UNGUELTIGE_FINANZAMTSNR],
	"Ongeldig Bundesfinanzamtsnummer: Het opgegeven Bundesfinanzamtsnummer is ongeldig of wordt momenteel niet ondersteund."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.DATUM_UNGUELTIG],
	"Ongeldige datum: De datum moet de indeling $dateFormat$ hebben."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUNDESSTNR_UNGUELTIG], "Ongeldig Bundessteuernummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EMAIL_UNGUELTIG], "Ongeldig e-mailadres.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER], "Ongeldige Aktenzeichen.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FINANZAMT_FEHLER], "Ongeldige Aktenzeichen.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_PRUEFZIFFER_FEHLER], "Ongeldige Aktenzeichen.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FORMATSCHLUESSEL_FEHLER],
	"Ongeldige Aktenzeichen."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_FORMAT_FEHLER], "Ongeldige Ermittlungsnummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_FINANZAMT_FEHLER], "Ongeldige Ermittlungsnummer.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_UNTERSCHEIDUNGSNUMMER_FEHLER],
	"Ongeldige Ermittlungsnummer."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_PRUEFZIFFER_FEHLER], "Ongeldige Ermittlungsnummer.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STRING_ZU_KURZ],
	"Waarde te kort: De waarde moet minimaal $minLength$ tekens bevatten."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STRING_ZU_LANG],
	"Waarde te lang: De waarde mag niet langer zijn dan $maxLength$ tekens."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_FORMALER_FEHLER],
	"Ongeldige IBAN: De IBAN moet bestaan uit een tweecijferige landcode gevolgd door een tweecijferig chequenummer gevolgd door het Basisbankrekeningnummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LAENDERCODE_FEHLER],
	"Ongeldige IBAN: De IBAN moet bestaan uit een tweecijferige landcode gevolgd door een tweecijferig chequenummer gevolgd door het Basisbankrekeningnummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LANDESFORMAT_FEHLER],
	"Ongeldige IBAN: De opgegeven IBAN komt niet overeen met de gedefinieerde formele structuur voor het opgegeven land."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_PRUEFZIFFER_FEHLER],
	"Ongeldige IBAN: De berekening van het controlecijfer voor de opgegeven IBAN resulteert in een ander controlenummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LEERZEICHEN_UNGUELTIG],
	"Ongeldige IBAN."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_NUR_FUER_TEST], "Ongeldig identificatienummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_NICHT_NUMERISCH], "Ongeldig identificatienummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_ZIFFERNFOLGE_FEHLER], "Ongeldig identificatienummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_LAENGE_UNGUELTIG], "Ongeldig identificatienummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_PRUEFZIFFER_UNGUELTIG], "Ongeldig identificatienummer.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_LEERZEICHEN_UNGUELTIG],
	"Ongeldig identificatienummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.LAENDERKUERZEL_UNGUELTIG],
	"Ongeldige landcode: De opgegeven landcode is ongeldig of wordt momenteel niet ondersteund."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER], "Ongeldig belastingnummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FINANZAMT_FEHLER], "Ongeldig belastingnummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER_BEZIRK], "Ongeldig belastingnummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_NEGATIVMUSTER], "Ongeldig belastingnummer.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_PRUEFZIFFER_FEHLER], "Ongeldig belastingnummer.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BICINTERNATIONAL_FORMALER_FEHLER],
	"Ongeldige BIC (internationaal): De formele structuur van de opgegeven BIC (internationaal) is ongeldig."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_FORMALER_FEHLER],
	"Ongeldig IBAN (internationaal): Het IBAN (internationaal) moet bestaan uit een tweecijferige landcode gevolgd door een tweecijferig chequenummer gevolgd door het Basisbankrekeningnummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_LANDESFORMAT_FEHLER],
	"Ongeldige IBAN (internationaal): De opgegeven IBAN (internationaal) voldoet niet aan de gedefinieerde formele structuur voor het opgegeven land."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_PRUEFZIFFER_FEHLER],
	"Ongeldige IBAN (internationaal): De berekening van het controlecijfer voor de opgegeven IBAN (internationaal) resulteert in een ander controlenummer."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_LEERZEICHEN_UNGUELTIG],
	"Ongeldige IBAN (internationaal)."
);
