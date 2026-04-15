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
package com.mgmtp.a12.kernel.core.base.utils.internal;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.iterators.UnmodifiableIterator;

/**
 * {@link Set} implementation with following properties:
 * <ul>
 * <li>Unmodifiable: an {@link UnsupportedOperationException} will be thrown while calling methods
 * which are meant to modify the set.</li>
 * <li>The order of the collection with which it is initialized is kept.</li>
 * </ul>
 * Use case: This implementation could be used as returned type when it comes to a read-only
 * collection, in which only distinct objects are present and for which deterministic results are
 * important.
 */
public class LinkedUnmodifiableSet<E> extends AbstractSet<E> implements Set<E> {

	private final List<E> list;

	public LinkedUnmodifiableSet(Collection<E> collection) {
		this.list = collection.stream().distinct().collect(Collectors.toList());
	}

	@Override
	public Iterator<E> iterator() {
		return UnmodifiableIterator.unmodifiableIterator(list.iterator());
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public int hashCode() {
		// no special hashCode logic
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// no special logic
		return super.equals(obj);
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		throw new UnsupportedOperationException();
	}

}
