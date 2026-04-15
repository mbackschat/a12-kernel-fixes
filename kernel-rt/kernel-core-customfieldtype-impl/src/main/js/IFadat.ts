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

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { ICustomFieldValidator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";


/**
 * Fadat is an abbreviation for “Finanzamtsdaten” (tax office data). This information is needed for several
 * {@link ICustomFieldValidator} implementations.
 * Use {@link deserializeFadat} to receive an instance.
 */
export interface IFadat {
	readonly federalTaxOfficeNumbers: Set<number>;
	readonly ewazTestFederalTaxOfficeNumbers: Set<number>;
}

/**
 * Deserializes to {@link IFadat}.
 *
 * @param fadatInfo
 * 		object deserialized from the JSON file exported from ElsterRules that contains the FADAT information
 * @param testTaxOfficesAllowed
 * 		use {@code true}, if numbers of test tax offices and EWAz test tax office numbers should be added to the
 * 		sets that are returned by {@link IFadat.getFederalTaxOfficeNumbers()} and {@link IFadat.getEWAzTestFederalTaxOfficeNumber()}.
 * 		If `false`, they are not added.
 * @return the deserialized {@link IFadat}.
 */
export function deserializeFadat(fadatInfo: unknown, testTaxOfficesAllowed = false): IFadat {
	const federalTaxOfficeNumbers = new Set<number>();
	const ewazTestFederalTaxOfficeNumbers = new Set<number>();
	const actualFadatInfo = <{ bufaNrs: [{ bufaNr: number, isTestTaxOffice: boolean }] }>fadatInfo;
	for (const bufaObject of actualFadatInfo.bufaNrs) {
		if (!bufaObject.isTestTaxOffice || testTaxOfficesAllowed) {
			if (bufaObject.bufaNr == undefined) {
				throw new Error(`The provided tax office object must define a property "bufaNr" but was: ${JSON.stringify(bufaObject)}`);
			}
			const bufaNr = Number(bufaObject.bufaNr);
			federalTaxOfficeNumbers.add(bufaNr);
			if (bufaObject.isTestTaxOffice) {
				ewazTestFederalTaxOfficeNumbers.add(bufaNr);
			}
		}
	}
	return { federalTaxOfficeNumbers, ewazTestFederalTaxOfficeNumbers };
}
