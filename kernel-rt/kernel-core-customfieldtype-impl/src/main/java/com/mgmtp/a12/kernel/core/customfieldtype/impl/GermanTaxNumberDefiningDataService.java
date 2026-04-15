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

import java.io.IOException;
import java.io.Reader;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.impl.GermanTaxNumberDefiningDataInternalService;

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
 *
 * @deprecated Will be removed with A12K-3589. Use {@link IFadat#deserialize(Reader, boolean)} instead.
 */
@Deprecated(since = "30.6.0", forRemoval = true)
public class GermanTaxNumberDefiningDataService {

	private GermanTaxNumberDefiningDataService() {
		throw new IllegalArgumentException("This must not be instantiated.");
	}

	/**
	 * Deserializes and caches the information that is necessary for validation of german tax
	 * numbers and related custom field types.
	 *
	 * @param fadatInfo
	 *        {@link Reader} to the JSON-File containing the FADAT information
	 * @throws IOException
	 */
	public static synchronized void deserializeAndProvide(Reader fadatInfo) throws IOException {
		deserializeAndProvide(fadatInfo, false);
	}

	/**
	 * Deserializes the information and caches information that is necessary for validations.
	 *
	 * @param fadatInfo
	 *        {@link Reader} to the JSON-File containing the FADAT information
	 * @param testTaxOfficesAllowed
	 * 		If <code>true</code>, test tax-offices are added to the cache. In this case e.g.
	 * 		german tax numbers are interpreted as valid, which are actually only assigned to a
	 * 		test tax-offices. Setting this parameter to <code>true</code> is useful if you
	 * 		want to test in the production environment (e.g. MeinElster has this feature -
	 * 		other projects should not need it). If you only test in test-environments, this
	 * 		parameter should be <code>false</code> or use the standard
	 * 		method {@link #deserializeAndProvide(Reader)} instead.
	 * @throws IOException
	 */
	public static synchronized void deserializeAndProvide(Reader fadatInfo, boolean testTaxOfficesAllowed) throws IOException {
		GermanTaxNumberDefiningDataInternalService.getInstance().deserializeAndProvide(fadatInfo, testTaxOfficesAllowed);
	}
}
