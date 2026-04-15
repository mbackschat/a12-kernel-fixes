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
import java.util.Set;

import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.impl.FADATJsonDeserializer;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Fadat is an abbreviation for “Finanzamtsdaten” (tax office data). This information is needed for several
 * {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator} implementations.
 * Use {@link IFadat#deserialize} to receive an instance.
 */
@OnlyForUsage
public interface IFadat {
	Set<Integer> getFederalTaxOfficeNumbers();

	Set<Integer> getEWAzTestFederalTaxOfficeNumber();

	/**
	 * Deserializes to {@link IFadat}.
	 *
	 * @param fadatInfo
	 * 		A reader to the file that has been exported from ExtMP.
	 * @param testTaxOfficesAllowed
	 * 		use {@code true}, if numbers of test tax offices and EWAz test tax office numbers should be added to the
	 * 		sets that are returned by {@link IFadat#getFederalTaxOfficeNumbers()} and {@link IFadat#getEWAzTestFederalTaxOfficeNumber()}.
	 * 		If <code>false</code>, they are not added.
	 * @return the deserialized {@link IFadat}.
	 *
	 * @throws IOException
	 */
	static IFadat deserialize(Reader fadatInfo, boolean testTaxOfficesAllowed) throws IOException {
		return FADATJsonDeserializer.deserialize(fadatInfo, testTaxOfficesAllowed);
	}
}
