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

import { deserializeFadat } from "./IFadat.js";
import { AbstractFadatRelatedCustomFieldType } from "./internal/verifier/AbstractFadatRelatedCustomFieldType.js";

/**
 * This class can be used to provide the necessary defining data for later usage of the German tax
 * number and similar {@link ICustomFieldValidator}s. The following classes need this data:<br>
 * <ul>
 * <li>{@link SteuernummerVerifier}</li>
 * <li>{@link BundessteuernummerVerifier}</li>
 * <li>{@link BundesfinanzamtsnummerVerifier}</li>
 * <li>{@link LaenderkuerzelVerifier}</li>
 * <li>{@link EWAzVerifier}</li>
 * </ul>
 * @deprecated Will be removed with A12K-3589. Use {@link deserializeFadat} instead.
 */
export class GermanTaxNumberDefiningDataService {
	/**
	 * Deserializes and caches the information that is necessary for validation of german tax
	 * numbers and related custom field types.
	 *
	 * @param fadatInfo 	JSON containing the FADAT information
	 * @param testTaxOfficesAllowed 	If <code>true</code>, test tax-offices are added to the cache.
	 * In this case e.g. german tax numbers are interpreted as valid, which are actually only assigned to
	 * a test tax-offices. Setting this parameter to <code>true</code> is useful if you want to test
	 * in the production environment (e.g. MeinElster has this feature).
	 * With this mechanism you can avoid real tax declarations. If you only test in
	 * test-environments, this parameter is <code>false</code> by default.
	 */
	public static deserializeAndProvide(fadatInfo: any, testTaxOfficesAllowed = false): void {
		AbstractFadatRelatedCustomFieldType.staticFadat = deserializeFadat(fadatInfo, testTaxOfficesAllowed);
	}
}
