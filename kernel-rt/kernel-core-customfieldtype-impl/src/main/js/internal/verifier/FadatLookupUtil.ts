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


import type {IFadat} from "../../IFadat.js";

export class FadatLookupUtil {

	public static staticFadat: IFadat | undefined;

	private static _actualFadat(fadat: IFadat | undefined): IFadat {
		if (!fadat) {
			if (!FadatLookupUtil.staticFadat) {
				throw new Error("The necessary FADAT information is not available (integration error).");
			}
			return FadatLookupUtil.staticFadat;
		}
		return fadat;
	}

	static existsFederalTaxOfficeNumber(fadat: IFadat | undefined, federalTaxOfficeNumber: number): boolean {
		const actual = FadatLookupUtil._actualFadat(fadat);
		if (actual.federalTaxOfficeNumbers.size === 0) {
			throw new Error("The necessary FADAT information is not available (integration error).");
		}
		return actual.federalTaxOfficeNumbers.has(federalTaxOfficeNumber);
	}

	static existsEWAzTestFederalTaxOfficeNumber(fadat: IFadat | undefined, federalTaxOfficeNumber: number): boolean {
		return FadatLookupUtil._actualFadat(fadat).ewazTestFederalTaxOfficeNumbers.has(federalTaxOfficeNumber);
	}
}
