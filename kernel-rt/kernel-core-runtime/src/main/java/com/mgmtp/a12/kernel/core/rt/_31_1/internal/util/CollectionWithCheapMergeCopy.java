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

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public interface CollectionWithCheapMergeCopy<T> {
	/**
	 * @return a  CollectionWithCheapMergeCopy that contains the union of elements from this and other, leaving both this and
	 * 		other unmodified.
	 */
	default CollectionWithCheapMergeCopy<T> merge(CollectionWithCheapMergeCopy<T> other) {
		// Optimization for the case where one or both are actually the singleton ImplEmpty.instance().
		// This optimization may in particular allow to avoid nearly all overhead when all initial origins are set to
		// CollectionWithCheapMergeCopy.empty() (in that case all subsequent merge() calls would only return the reference to
		// the empty instance, and not allocate anything)
		if(other == null || other instanceof ImplEmpty<T>) {
			assert other != null;
			return this;
		}
		if(this instanceof ImplEmpty<T>) {
			return other;
		}
		return new ImplJoined<>(this, other);
	}

	/**
	 * Add all <em>non-null</em> elements from this collection to the given set. This operation may be expensive because the deduplication work
	 * that could be skipped during merge gets done.
	 * <p>
	 * <em>Implementations must guarantee that no null element will be added to the targetSet</em>
	 */
	void exportToSetExpensive(Set<T> targetSet);

	static <T> CollectionWithCheapMergeCopy<T> of(T id) {
		return new ImplSingle<>(id);
	}

	static <T> CollectionWithCheapMergeCopy<T> of(Collection<T> elems) {
		if(elems.isEmpty()) {
			return CollectionWithCheapMergeCopy.empty();
		}
		return new ImplColl<>(elems);
	}

	static <T> CollectionWithCheapMergeCopy<T> empty() {
		return ImplEmpty.instance();
	}

	// =================== IMPLS =====================

	class ImplSingle<T> implements CollectionWithCheapMergeCopy<T> {
		private final T elem;

		private ImplSingle(T elem) {
			assert elem != null;
			this.elem = elem;
		}

		@Override public void exportToSetExpensive(Set<T> targetSet) {
			if (elem != null) { // Note: intellij incorrectly believes that elem is already guaranteed non-null, but that is only
				// based on the assertion from the constructor, which is not enforced unless assertions are enabled)
				targetSet.add(elem);
			}
		}
	}

	class ImplJoined<T> implements CollectionWithCheapMergeCopy<T> {
		private final CollectionWithCheapMergeCopy<T> a;
		private final CollectionWithCheapMergeCopy<T> b;

		private ImplJoined(CollectionWithCheapMergeCopy<T> a, CollectionWithCheapMergeCopy<T> b) {
			assert a != null;
			assert b != null;
			this.a = a;
			this.b = b;
		}

		@Override public void exportToSetExpensive(Set<T> targetSet) {
			if(a != null) { // let's be defensive here, given that this is the runtime. NOTE: ignore intellij's mistaken hint
				// that "a" is supposedly non-null, this hint is just based on the assert statement in the constructor, which is not
				// actually active in prod.
				a.exportToSetExpensive(targetSet);
			}
			if(b != null) {
				b.exportToSetExpensive(targetSet);
			}
		}
	}

	class ImplColl<T> implements CollectionWithCheapMergeCopy<T> {
		private final Collection<T> elems;

		private ImplColl(Collection<T> elems) {
			assert elems != null;
			assert elems.stream().allMatch(Objects::nonNull);
			this.elems = elems;
		}

		@Override public void exportToSetExpensive(Set<T> targetSet) {
			if(elems != null) {
				for(T e : elems) {
					if(e != null) {
						targetSet.add(e);
					}
				}
			}
		}
	}

	class ImplEmpty<T> implements CollectionWithCheapMergeCopy<T> {
		private static final ImplEmpty<?> INTERNAL_INSTANCE = new ImplEmpty<>();

		static <T> ImplEmpty<T> instance() {
			@SuppressWarnings("unchecked") // this is safe, because there is no element and therefore the
			// instances for all T are identical
			// see the implementation of java.util.List.of() for an example of the same pattern
			ImplEmpty<T> result = (ImplEmpty<T>) INTERNAL_INSTANCE;
			return result;
		}

		private ImplEmpty() {
		}

		@Override public void exportToSetExpensive(Set<T> targetSet) {
			// nothing to add
		}
	}
}
