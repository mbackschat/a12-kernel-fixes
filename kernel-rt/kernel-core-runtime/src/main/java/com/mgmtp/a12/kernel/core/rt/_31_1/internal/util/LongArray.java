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
package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.util.Arrays;

/**
 * Klasse implementiert eine Wrapper-Klasse für Long-Arrays.
 * Diese wird benötigt, da die equals-Methode von Long-Arrays nur auf Identity prüfen und nicht
 * auf semantische Gleichheit.
 */
public class LongArray implements Comparable<LongArray> {

	private final long[] origArray;

	public LongArray(long[] orig) {
		this.origArray = orig;
	}

	public long[] getOriginClone() {
		return origArray.clone();
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof LongArray)) {
			return false;
		}

		LongArray other = (LongArray) obj;
		return Arrays.equals(origArray, other.origArray);
	}

	public int hashCode() {
		return Arrays.hashCode(origArray);
	}

	public String toString() {
		return Arrays.toString(origArray);
	}

	@Override public int compareTo(LongArray o) {
		int commonLength = Math.min(o.origArray.length, origArray.length);
		for (int i=0; i < commonLength; i++) {
			int comp = Long.compare(origArray[i], o.origArray[i]);
			if (comp != 0) {
				return comp;
			}
		}
		return Integer.compare(origArray.length, o.origArray.length);
	}
}
