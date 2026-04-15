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
package com.mgmtp.a12.kernel.core.rt.a12internal;

import java.io.Serializable;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Container class to represent a 2 element pair.
 *
 * @param <E1>
 * 		the type of the 1st element
 * @param <E2>
 * 		the type of the 2nd element
 */
@OnlyForUsage
public class Pair<E1, E2> implements Serializable {
	private static final long serialVersionUID = 7018882672829576361L;

	private E1 entry1;
	private E2 entry2;

	public Pair(E1 entry1, E2 entry2) {
		this.entry1 = entry1;
		this.entry2 = entry2;
	}

	public Pair(Pair<? extends E1, ? extends E2> other) {
		if (other == null) {
			entry1 = null;
			entry2 = null;
		} else {
			entry1 = other.entry1;
			entry2 = other.entry2;
		}
	}

	public E1 getEntry1() {
		return entry1;
	}

	public void setEntry1(E1 entry1) {
		this.entry1 = entry1;
	}

	public E2 getEntry2() {
		return entry2;
	}

	public void setEntry2(E2 entry2) {
		this.entry2 = entry2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entry1 == null) ? 0 : entry1.hashCode());
		result = prime * result + ((entry2 == null) ? 0 : entry2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (entry1 == null) {
			if (other.entry1 != null) {
				return false;
			}
		} else if (!entry1.equals(other.entry1)) {
			return false;
		}
		if (entry2 == null) {
			if (other.entry2 != null) {
				return false;
			}
		} else if (!entry2.equals(other.entry2)) {
			return false;
		}
		return true;
	}

	public String toString() {
		String part1 = (entry1 == null || entry1.toString() == null) ? "null" : entry1.toString();
		String part2 = (entry2 == null || entry2.toString() == null) ? "null" : entry2.toString();

		return "[" + part1 + "," + part2 + "]";
	}

}
