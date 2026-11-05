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

import { ESStErmNrVerifierImpl } from "./internal/verifier/ESStErmNrVerifierImpl.js";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import type { IFadat, deserializeFadat } from "./IFadat.js";

/**
 * The {@link ICustomFieldValidator} implementation for the ESSt-Ermittlungsnummer.
 * <br>
 * <br>
 * The number is stored in the internal (XML) format {@code FFFF8BBBJJJJUUUUUKKKTTTP} (24 digits) and
 * shown to the user in the display format {@code FFFF/BBB/JJJJ/UUUUU/KKK/000P}. The Merkzeichen ({@code 8}
 * at position 5) is not displayed and the group {@code TTT} is always rendered as {@code 000}.
 * <p>
 * The validation consists of the following parts:
 * <ul>
 * <li>Formal check: the value matches the internal 24-digit structure (including the fixed Merkzeichen
 * {@code 8}).</li>
 * <li>Validity check of the Bundesfinanzamtsnummer ({@code FFFF}).</li>
 * <li>The Unterscheidungsnummer ({@code UUUUU}) must not consist solely of zeros.</li>
 * <li>Check-digit ({@code P}) verification.</li>
 * </ul>
 */
export class ESStErmNrVerifier {

	private constructor() {
		// at the moment, we don't need to do something
	}

	/**
	 * @param fadat
	 * 		the federal tax office data. Can be created via {@link deserializeFadat}.
	 * @return the instance of {@link ICustomFieldValidator}.
	 */
	public static create(fadat: IFadat): ICustomFieldValidator {
		return new ESStErmNrVerifierImpl(fadat);
	}
}
