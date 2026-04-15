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

package com.mgmtp.a12.kernel.core.utils.internal.string.sourcemap;

import org.apache.commons.lang3.Validate;

/**
 * Represents an entry in a {@link StringWithFragmentsOriginMap} source map.
 * The key of the entry is the range within the combined
 * string and is represented by the two integers beginIdxInCombinedString and endIdxInCombinedString.
 * The value represents the origin of the string fragment and is of type {@link Object}.
 * <p>
 * (Note that the "map" in {@link StringWithFragmentsOriginMap} is not implemented as a real map (i.e. hash table or treemap), but
 * as a simple list over which the lookup function iterates in order to find an overlapping
 * {@link StringWithFragmentsOriginMapEntry} entry)
 */
public class StringWithFragmentsOriginMapEntry {
	/**
	 * (inclusive)
	 */
	private final int beginIdxInCombinedString;
	/**
	 * (exclusive)
	 */
	private final int endIdxInCombinedString;
	private final Object originRef;

	/**
	 * @param beginIdxInCombinedString
	 * 		(inclusive)
	 * @param endIdxInCombinedString
	 * 		(exclusive)
	 * @param originRef
	 * 		the "value" of the map entry
	 */
	public StringWithFragmentsOriginMapEntry(
			int beginIdxInCombinedString,
			int endIdxInCombinedString,
			Object originRef) {
		this.beginIdxInCombinedString = beginIdxInCombinedString;
		this.endIdxInCombinedString = endIdxInCombinedString;
		this.originRef = originRef;
	}

	/**
	 * @return true if the provided position (0-based index) in the combined string is within the range of this entry,
	 * 		otherwise return false.
	 */
	public boolean contains(int positionIdxInCombinedString) {
		return this.beginIdxInCombinedString <= positionIdxInCombinedString
				&& positionIdxInCombinedString < this.endIdxInCombinedString;
	}

	/**
	 * @return the index <b>within</b> the fragment which corresponds to the given {@code idxInCombinedString}.
	 */
	public int translateIdx(int idxInCombinedString) {
		// The earliest valid index is beginIdxInCombinedString (inclusive):
		Validate.isTrue(beginIdxInCombinedString <= idxInCombinedString);
		// The latest valid index that can be translated for this entry is an (exclusive [1]) end index at endIdxInCombinedString:
		//
		// [1] *exclusive* end indices occur e.g. because StringBuilder.replace(int start, int end, String str) uses exclusive
		//     end indices: https://docs.oracle.com/javase/7/docs/api/java/lang/StringBuilder.html#replace(int,%20int,%20String)
		Validate.isTrue(idxInCombinedString <= endIdxInCombinedString);
		return idxInCombinedString - beginIdxInCombinedString;
	}

	public int getBeginIdxInCombinedString() {
		return beginIdxInCombinedString;
	}

	public int getEndIdxInCombinedString() {
		return endIdxInCombinedString;
	}

	public Object getOriginRef() {
		return originRef;
	}
}
