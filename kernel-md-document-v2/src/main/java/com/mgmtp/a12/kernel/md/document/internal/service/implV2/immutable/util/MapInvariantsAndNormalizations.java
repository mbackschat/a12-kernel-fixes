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

package com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.util;

/**
 * Defines invariants and normalization rules for keys and values in {@link ValidatedImmutableMap} (including
 * handling of absent values), during map construction and updates.
 * <p>
 * Implementing classes should typically have singleton instances, so that {@link ValidatedImmutableMap} can recognize
 * equal MapInvariantsAndNormalizations with simple object identity, in order to efficiently avoid redundant re-validations
 * of maps that have already been validated with the same/equal MapInvariantsAndNormalizations<br/>
 *
 * @param <I>
 * 		note: this type parameter is specified so that the compiler can statically check for accidental conversion
 * 		between maps with different invariants, which would be likely unintentional (and trigger somewhat-expensive
 * 		re-validation).
 * 		But the static check can only precisely match the actual dynamic behavior if all instances of the implementing
 * 		class bound to {@code I} are {@code equal} (best achieved by making them singletons).
 */
public interface MapInvariantsAndNormalizations<K, V, I extends MapInvariantsAndNormalizations<K, V, I>> {
	/**
	 * Checks if the given key is allowed to exist / be inserted into the map. must throw an exception if not.
	 * <p>
	 * Behavior must be repeatable, i.e. equal keys, the same behavior must happen every time, so that the
	 * {@link ValidatedImmutableMap} does not need to re-validate later. (for equal instances of MapInvariantsAndNormalizations)
	 */
	default void checkKey(K key) {
	}

	/**
	 * Checks if the given value is allowed to exist / be inserted into the map. must throw an exception if not.
	 * <p>
	 * Behavior must be repeatable, i.e. for equal values, the same behavior must happen every time, so that the
	 * {@link ValidatedImmutableMap} does not need to re-validate later. (for equal instances of MapInvariantsAndNormalizations)
	 */
	default void checkValue(V value) {
	}

	/**
	 * @return true if an entry with the given value is to be considered equivalent to an absent entry in the map (e.g. for
	 * 		GroupInstanceV2 subgroups, both null and RepetitionsV2.empty() are equivalent to absent subgroups).
	 * 		<p>
	 * 		Behavior must be repeatable, i.e. for equal values, the same behavior must happen every time, so that the
	 *      {@link ValidatedImmutableMap} does not need to re-validate later. (for equal instances of
	 *      MapInvariantsAndNormalizations)
	 */
	default boolean treatLikeAbsent(V value) {
		return value == null;
	}

	/**
	 * @return the value to return for entries that are not present in the map. Must be repeatable, i.e. the same value must
	 * be returned every time (for equal instances of MapInvariantsAndNormalizations)
	 */
	default V substituteAbsent() {
		return null;
	}
}
