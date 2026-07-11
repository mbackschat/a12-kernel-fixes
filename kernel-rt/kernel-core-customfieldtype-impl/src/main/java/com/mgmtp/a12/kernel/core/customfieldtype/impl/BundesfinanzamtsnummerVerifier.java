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
package com.mgmtp.a12.kernel.core.customfieldtype.impl;

import java.io.Reader;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.verifier.BundesfinanzamtsnummerVerifierImpl;

import lombok.NonNull;

/**
 * Validates the Bundesfinanzamtsnummer (federal tax office number).
 * The validation consists of the following parts:
 * <ul>
 * <li>Is the Bundesfinanzamtsnummer formally valid?</li>
 * <li>Does a federal tax office with this number exist?</li>
 * </ul>
 * The formal check verifies whether it is a four-digit number. If the
 * Bundesfinanzamtsnummer does not match the regular expression
 * <code>[0-9]{4}</code>
 * it is a formal error.
 * <br>
 *
 * @see com.mgmtp.a12.kernel.core.customfieldtype.impl
 */
public class BundesfinanzamtsnummerVerifier {

	private BundesfinanzamtsnummerVerifier() {
	}

	/**
	 * @param fadat
	 * 		the federal tax office data. Can be created via {@link IFadat#deserialize(Reader, boolean)}.
	 * @return the instance of {@link ICustomFieldValidator}.
	 */
	public static @NonNull ICustomFieldValidator create(@NonNull IFadat fadat) {
		return new BundesfinanzamtsnummerVerifierImpl(fadat);
	}
}
