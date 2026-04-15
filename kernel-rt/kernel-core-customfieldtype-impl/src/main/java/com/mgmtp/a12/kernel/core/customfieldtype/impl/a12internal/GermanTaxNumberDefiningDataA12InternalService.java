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
package com.mgmtp.a12.kernel.core.customfieldtype.impl.a12internal;

import java.io.Reader;
import java.util.Set;

import com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.impl.GermanTaxNumberDefiningDataInternalService;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.impl.StnrVerifierDeserializer;

/**
 * This class is the equivalent of {@link com.mgmtp.a12.kernel.core.customfieldtype.impl.GermanTaxNumberDefiningDataService} for A12
 * internal use.
 */
public class GermanTaxNumberDefiningDataA12InternalService {

	private GermanTaxNumberDefiningDataA12InternalService() {
		throw new IllegalArgumentException("This must not be instantiated.");
	}

	/**
	 * Initializes the internal service for validation of german tax numbers and related custom field types with the given data.
	 *
	 * @param bufaNummern
	 * 		the BUFA numbers to be set. Must not be {@code null}.
	 * @param ewazTestBufaNummern
	 * 		the BUFA numbers for ewaz test. Must not be {@code null}.
	 * @deprecated Will be removed with A12K-3589. Use {@link IFadat#deserialize(Reader, boolean)} instead.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	public static synchronized void initWith(Set<Integer> bufaNummern, Set<Integer> ewazTestBufaNummern) {
		GermanTaxNumberDefiningDataInternalService.getInstance().provide(bufaNummern, ewazTestBufaNummern);
	}

	/**
	 * <b>Note:</b> This function is used by projAlpha.
	 *
	 * @return {@link Reader} for the file "StNrVerifier.json". <br><b>Note:</b> The invoker is responsible to close this {@link Reader}.
	 */
	public static Reader getReaderToGermanTaxNumberDefiningData() {
		return StnrVerifierDeserializer.getReaderToGermanTaxNumberDefiningData();
	}
}
