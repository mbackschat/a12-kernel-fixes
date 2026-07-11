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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.meta.util;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;

public class MetaFeldUtil {

	private MetaFeldUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * precondition: both arrays have the same length
	 *
	 * @param entry1List
	 * 		array of strings which should be at the first position of the pair
	 * @param entry2List
	 * 		array of strings which should be at the second position of the pair
	 * @return a list of string-string pairs
	 */
	public static <T1, T2> List<Pair<T1, T2>> createPairList(T1[] entry1List, T2[] entry2List) {
		List<Pair<T1, T2>> result = new ArrayList<>();
		for (int i = 0; i < entry1List.length; i++) {
			result.add(new Pair<>(entry1List[i], entry2List[i]));
		}
		return result;
	}
}
