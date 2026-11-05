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

export class CustomFieldTypeErrorKeys {
	static get UNGUELTIGE_EMAIL(): string {
		return "ungueltigeEmail";
	}

	static get UNGUELTIGE_ID_NUMMER(): string {
		return "ungueltigeIDNummer";
	}

	static get UNGUELTIGE_STNR(): string {
		return "ungueltigeSteuernummer";
	}

	static get UNGUELTIGES_EWAZ(): string {
		return "ungueltigesEinheitswertAktenzeichen";
	}

	static get UNGUELTIGE_ESSTERMNR(): string {
		return "ungueltigeESStErmittlungsnummer";
	}

	static get UNGUELTIGE_BUFANR(): string {
		return "ungueltigeBundesfinanzamtsnummer";
	}

	static get UNGUELTIGES_LAENDERKUERZEL(): string {
		return "ungueltigesLaenderkuerzel";
	}

	static get UNGUELTIGE_IBAN(): string {
		return "ungueltigeIBAN";
	}

	static get UNGUELTIGE_BIC(): string {
		return "ungueltigeBIC";
	}

	static get UNGUELTIGES_DATUM_MMJJ(): string {
		return "ungueltigesDatumMMJJ";
	}

	static get UNGUELTIGES_DATUM_TTMMJJ(): string {
		return "ungueltigesDatumTTMMJJ";
	}

	static get UNGUELTIGE_BUSTNR(): string {
		return "ungueltigeBundessteuernummer";
	}

	static get UNGUELTIGE_DEUTSCHE_USTID(): string {
		return "ungueltigeDeutscheUStIDNr";
	}

	static get UNGUELTIGER_BICINTERNATIONAL(): string {
		return "ungueltigerBICinternational";
	}

	static get UNGUELTIGE_IBANINTERNATIONAL(): string {
		return "ungueltigeIBANinternational";
	}

	// für Typ E-Mail benötigt
	static get EMAIL_ZU_LANG(): string {
		return "stringZuLang";
	}
	// für Typ E-Mail benötigt
	static get EMAIL_ZU_KURZ(): string {
		return "stringZuKurz";
	}
}
