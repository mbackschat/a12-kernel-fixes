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
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.verifier.StrafAzVerifierImpl;

import lombok.NonNull;

/**
 * The {@link ICustomFieldValidator} implementation for the Straf-Aktenzeichen (penal case reference number).
 * <br>
 * <br>
 * This class validates the Straf-Aktenzeichen.
 * The validation of the Straf-Aktenzeichen consists of the following parts:
 * <ul>
 * <li>Formal check</li>
 * <li>Validity check</li>
 * </ul>
 * <p>
 * The formal check verifies whether the Straf-Aktenzeichen matches the following regular
 * expression:
 * <code>[0-9]{4}D[0-9]{4}[0-9]{7}[0-9]{1}</code>
 * </p>
 * <p>
 * The first four characters of the Straf-Aktenzeichen form the Bundesfinanzamtsnummer.
 * </p>
 * <p>
 * The validity check verifies the correctness of the check digit (last digit).
 * </p>
 */
public class StrafAzVerifier {

	private StrafAzVerifier() {
	}

	/**
	 * @return the instance of {@link ICustomFieldValidator}.
	 *
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use {@link #create(IFadat)} instead.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	public static ICustomFieldValidator getInstance() {
		return new StrafAzVerifierImpl(null);
	}

	/**
	 * @param fadat
	 * 		the federal tax office data. Can be created via {@link IFadat#deserialize(Reader, boolean)}.
	 * @return the instance of {@link ICustomFieldValidator}.
	 */
	public static @NonNull ICustomFieldValidator create(@NonNull IFadat fadat) {
		return new StrafAzVerifierImpl(fadat);
	}
}
