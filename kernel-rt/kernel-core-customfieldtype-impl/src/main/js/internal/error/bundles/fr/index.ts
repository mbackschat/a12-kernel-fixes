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

const bundle = new ErrorLocalizationBundle("fr");
export default bundle;

const set: typeof bundle.set = bundle.set.bind(bundle);

set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BIC_FORMALER_FEHLER],
	"BIC non valide: la structure formelle du BIC fourni n'est pas valide."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BIC_LAENDERCODE_FEHLER],
	"BIC non valide: le code de pays fourni n'est pas valide ou n'est actuellement pas pris en charge."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUFANR_FORMALER_FEHLER],
	"Bundesfinanzamtsnummer invalide: la structure formelle du Bundesfinanzamtsnummer fourni n'est pas valide. Le Bundesfinanzamtsnummer doit contenir exactement 4 chiffres."
);

set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUFANR_UNGUELTIGE_FINANZAMTSNR],
	"Bundesfinanzamtsnummer non valide: le Bundesfinanzamtsnummer fourni n'est pas valide ou n'est actuellement pas pris en charge."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.DATUM_UNGUELTIG],
	"Date invalide: la date doit être au format $dateFormat$."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BUNDESSTNR_UNGUELTIG], "Bundessteuernummer invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EMAIL_UNGUELTIG], "Adresse e-mail invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER], "Aktenzeichen invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FINANZAMT_FEHLER], "Aktenzeichen invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_PRUEFZIFFER_FEHLER], "Aktenzeichen invalide.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.EWAZ_FORMATSCHLUESSEL_FEHLER],
	"Aktenzeichen invalide."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_FORMAT_FEHLER], "Ermittlungsnummer invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_FINANZAMT_FEHLER], "Ermittlungsnummer invalide.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_UNTERSCHEIDUNGSNUMMER_FEHLER],
	"Ermittlungsnummer invalide."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.ESSTERMNR_PRUEFZIFFER_FEHLER], "Ermittlungsnummer invalide.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STRING_ZU_KURZ],
	"Valeur trop courte: la valeur doit contenir au moins $minLength$ caractères."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STRING_ZU_LANG],
	"Valeur trop longue: la valeur ne doit pas dépasser $maxLength$ caractères."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_FORMALER_FEHLER],
	"IBAN invalide: l'IBAN doit être composé d'un code pays à deux chiffres suivi d'un numéro de chèque à deux chiffres suivi du numéro de compte bancaire de base."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LAENDERCODE_FEHLER],
	"IBAN invalide: le code de pays fourni n'est pas valide ou n'est actuellement pas pris en charge."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LANDESFORMAT_FEHLER],
	"IBAN invalide: l'IBAN fourni n'est pas conforme à la structure formelle définie pour le pays fourni."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_PRUEFZIFFER_FEHLER],
	"IBAN non valide: le calcul du chiffre de contrôle pour l'IBAN fourni entraîne un numéro de contrôle différent."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBAN_LEERZEICHEN_UNGUELTIG],
	"IBAN non valide."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_NUR_FUER_TEST], "Numéro d'identification invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_NICHT_NUMERISCH], "Numéro d'identification invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_ZIFFERNFOLGE_FEHLER], "Numéro d'identification invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_LAENGE_UNGUELTIG], "Numéro d'identification invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_PRUEFZIFFER_UNGUELTIG], "Numéro d'identification invalide.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IDNR_LEERZEICHEN_UNGUELTIG],
	"Numéro d'identification invalide."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.LAENDERKUERZEL_UNGUELTIG],
	"Code de pays non valide : le code de pays fourni n'est pas valide ou n'est actuellement pas pris en charge."
);
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER], "Numéro fiscal invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FINANZAMT_FEHLER], "Numéro fiscal invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER_BEZIRK], "Numéro fiscal invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_FORMALER_NEGATIVMUSTER], "Numéro fiscal invalide.");
set(CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.STNR_PRUEFZIFFER_FEHLER], "Numéro fiscal invalide.");
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.BICINTERNATIONAL_FORMALER_FEHLER],
	"BIC (international) non valide: la structure formelle du BIC (international) fourni n'est pas valide."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_FORMALER_FEHLER],
	"IBAN (international) invalide: l'IBAN (international) doit être composé d'un code de pays à deux chiffres suivi d'un numéro de chèque à deux chiffres suivi du numéro de compte bancaire de base."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_LANDESFORMAT_FEHLER],
	"IBAN (international) non valide: l'IBAN (international) fourni n'est pas conforme à la structure formelle définie pour le pays fourni."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_PRUEFZIFFER_FEHLER],
	"IBAN (international) non valide: le calcul du chiffre de contrôle pour l'IBAN (international) fourni entraîne un numéro de contrôle différent."
);
set(
	CustomFieldTypeErrorEnum[CustomFieldTypeErrorEnum.IBANINTERNATIONAL_LEERZEICHEN_UNGUELTIG],
	"IBAN (international) invalide."
);
