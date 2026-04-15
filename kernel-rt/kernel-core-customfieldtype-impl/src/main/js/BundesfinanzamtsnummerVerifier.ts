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
import type { ICustomFieldValidator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import type { IFadat, deserializeFadat } from "./IFadat.js";
import { BundesfinanzamtsnummerVerifierImpl } from "./internal/verifier/BundesfinanzamtsnummerVerifierImpl.js";

/**
 * Checks the Bundesfinanzamtsnummer.
 * The validation of the Bundesfinanzamtsnummer consists of the following parts:
 * <ul>
 * <li>Is the Bundesfinanzamtsnummer formally valid?</li>
 * <li>Is there an existing Bundesfinanzamt with this number?</li>
 * </ul>
 * This formal check verifies that the number is a four-digit number.
 * If the Bundesfinanzamtsnummer does not match the regular expression
 * <code>[0-9]{4}</code> it is a formal error.
 */
export class BundesfinanzamtsnummerVerifier {

	private constructor() { }

	/**
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use {@link create} instead.
	 */
	public static getInstance(): ICustomFieldValidator {
		return new BundesfinanzamtsnummerVerifierImpl(undefined);
	}

	/**
	 * @param fadat
	 * 		the federal tax office data. Can be created via {@link deserializeFadat}.
	 * @return the instance of {@link ICustomFieldValidator}.
	 */
	public static create(fadat: IFadat): ICustomFieldValidator {
		return new BundesfinanzamtsnummerVerifierImpl(fadat);
	}
}
