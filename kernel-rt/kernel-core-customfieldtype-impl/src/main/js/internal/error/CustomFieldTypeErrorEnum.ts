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

/**
 * Konstanten, die als Schlüssel zu den sprachabhängigen Fehlertexten dienen. Die Fehlertexte sind
 * in ResourceBundles (z.B. {@link VordefiniertErrorMessages_de_DE}) hinterlegt.
 */
export enum CustomFieldTypeErrorEnum {
	KEIN_FEHLER,

	BIC_FORMALER_FEHLER,
	BIC_LAENDERCODE_FEHLER,

	BUFANR_FORMALER_FEHLER,
	BUFANR_UNGUELTIGE_FINANZAMTSNR,
	BUNDESSTNR_UNGUELTIG,

	DATUM_UNGUELTIG,

	EMAIL_UNGUELTIG,

	STRING_ZU_KURZ,
	STRING_ZU_LANG,

	IBAN_FORMALER_FEHLER,
	IBAN_LAENDERCODE_FEHLER,
	IBAN_LANDESFORMAT_FEHLER,
	IBAN_PRUEFZIFFER_FEHLER,
	IBAN_LEERZEICHEN_UNGUELTIG,

	/**
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Not needed anymore, when IdNrVerifier is created properly.
	 */
	IDNR_NUR_FUER_TEST,
	IDNR_NICHT_NUMERISCH,
	IDNR_ZIFFERNFOLGE_FEHLER,
	IDNR_LAENGE_UNGUELTIG,
	IDNR_PRUEFZIFFER_UNGUELTIG,
	IDNR_LEERZEICHEN_UNGUELTIG,

	LAENDERKUERZEL_UNGUELTIG,

	STNR_FORMALER_FEHLER,
	STNR_FINANZAMT_FEHLER,
	STNR_FORMALER_FEHLER_BEZIRK,
	STNR_FORMALER_NEGATIVMUSTER,
	STNR_PRUEFZIFFER_FEHLER,

	EWAZ_FORMALER_FEHLER,
	EWAZ_FINANZAMT_FEHLER,
	EWAZ_PRUEFZIFFER_FEHLER,
	EWAZ_FORMATSCHLUESSEL_FEHLER,

	BICINTERNATIONAL_FORMALER_FEHLER,

	IBANINTERNATIONAL_FORMALER_FEHLER,
	IBANINTERNATIONAL_LANDESFORMAT_FEHLER,
	IBANINTERNATIONAL_PRUEFZIFFER_FEHLER,
	IBANINTERNATIONAL_LEERZEICHEN_UNGUELTIG
}
