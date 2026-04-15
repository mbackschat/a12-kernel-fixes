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
package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberDefiningData;

public class GermanTaxNumberDefiningDataInternalService {

	private static final GermanTaxNumberDefiningDataInternalService ME = new GermanTaxNumberDefiningDataInternalService();

	public static GermanTaxNumberDefiningDataInternalService getInstance() {
		return ME;
	}

	private final GermanTaxNumberDefiningData unchangeableData = GermanTaxNumberDefiningData.getInstance();
	private IFadat fadatData;

	private GermanTaxNumberDefiningDataInternalService() {
	}

	public IGermanTaxNumberDefiningData getGermanTaxNumberDefiningData() {
		return unchangeableData;
	}

	public IFadat getFadatData() {
		return fadatData;
	}

	/**
	 * Resets the IGermanTaxNumberDefiningData to null.
	 * This method should only be used for tests!
	 */
	public synchronized void resetFADATData() {
		this.fadatData = null;
	}

	public synchronized void deserializeAndProvide(Reader fadatInfo, boolean testTaxOfficesAllowed) throws IOException {
		this.fadatData = IFadat.deserialize(fadatInfo, testTaxOfficesAllowed);
	}

	public synchronized void provide(Set<Integer> bufaNummern, Set<Integer> ewazTestBufaNummern) {
		this.fadatData = new FADATData(bufaNummern, ewazTestBufaNummern);
	}
}
