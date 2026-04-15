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

package com.mgmtp.a12.kernel.core.utils.internal;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 */

public class Pair<E1, E2> implements Serializable {

	private static final long serialVersionUID = 2573550707854721551L;
	private E1 entry1;
	private E2 entry2;

	public static <T1, T2> Pair<T1, T2> of(T1 entry1, T2 entry2) {
		return new Pair<>(entry1, entry2);
	}

	@SuppressWarnings("unused")
	private Pair() {
		/*
		 * der GWT-Compiler braucht diesen Konstruktor und deswegen wurde die Sichbarkeit nicht auf
		 * 'public' gesetzt denn dieser Konstruktor soll nicht zum Entwickeln verwendet werden.
		 */
	}

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

	public E2 getEntry2() {
		return entry2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entry1 == null) ? 0 : entry1.hashCode());
		result = prime * result + ((entry2 == null) ? 0 : entry2.hashCode());
		return result;

	}

	/**
	 * evaluiert, ob zwei Paare gleich sind gleich sind.
	 *
	 * @return wahr, falls beide Felschlüssel gleich sind, false sonst.
	 */
	@SuppressWarnings("unchecked")
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (other.getClass() != getClass()) {
			return false;
		}

		Pair<E1, E2> o = (Pair<E1, E2>) other;

		return Objects.equals(entry1, o.getEntry1())
				&& Objects.equals(entry2, o.getEntry2());
	}

	@Override public String toString() {
		return "Pair(" + entry1 + ", " + entry2 + ')';
	}
}
