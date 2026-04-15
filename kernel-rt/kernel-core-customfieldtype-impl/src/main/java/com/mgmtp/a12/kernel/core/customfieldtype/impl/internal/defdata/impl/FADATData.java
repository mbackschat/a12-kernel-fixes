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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat;

public class FADATData implements IFadat {
	private final Set<Integer> bufaNummern;
	private final Set<Integer> ewazTestBufaNummern;

	public FADATData(Set<Integer> bufaNummern, Set<Integer> ewazTestBufaNummern) {
		this.bufaNummern = bufaNummern != null ? bufaNummern : Collections.emptySet();
		this.ewazTestBufaNummern = ewazTestBufaNummern != null ? ewazTestBufaNummern : Collections.emptySet();
	}

	public static IFadat createFADATData(Map<String, Object> configData) {
		if (configData.containsKey(FADATConstants.BUNDESFINANZAMTNUMMERN) || configData.containsKey(
				FADATConstants.EWAZ_TEST_BUNDESFINANZAMTNUMMERN)) {
			// The MeinELSTER way: FADAT is passed with every call (needed, because it changes with the Session - Test-FADAT could
			//                     be either allowed or not).
			Set<Integer> bufaNummern = getValue(configData, FADATConstants.BUNDESFINANZAMTNUMMERN);
			Set<Integer> ewazTestBufaNummern = getValue(configData, FADATConstants.EWAZ_TEST_BUNDESFINANZAMTNUMMERN);
			return new FADATData(bufaNummern, ewazTestBufaNummern);
		} else {
			// The A12 way: FADAT is cached there, when being deserialized from json
			IFadat fadatData = GermanTaxNumberDefiningDataInternalService.getInstance().getFadatData();
			if (fadatData != null) {
				return fadatData;
			}
			throw new IllegalArgumentException("The necessary FADAT information is not available (integration error).");
		}

	}

	private static Set<Integer> getValue(Map<String, Object> configData, String key) {
		if (configData.containsKey(key)) {
			Object o = configData.get(key);
			if (!(o instanceof Set<?>)) {
				throw new IllegalArgumentException(
						"The FADAT information was not passed in the expected structure 'Set<String>' (integration error).");
			}
			@SuppressWarnings("unchecked")
			Set<Integer> oCasted = (Set<Integer>) o;
			return oCasted;
		} else {
			return Collections.emptySet();
		}
	}

	@Override public Set<Integer> getFederalTaxOfficeNumbers() {
		return bufaNummern;
	}

	@Override public Set<Integer> getEWAzTestFederalTaxOfficeNumber() {
		return ewazTestBufaNummern;
	}
}
