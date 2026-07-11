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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Something that has a parent (chain).
 *
 * @param <T>
 * 		the type of the implementing type. All receivers must also be an instance of this type.
 * @param <P>
 * 		a subtype of {@code T} that is returned by {@link #parent()}.
 */
public interface Parentable<T extends Parentable<T, P>, P extends T> {
	/**
	 * @return the parent of the receiver, or {@code null} if it doesn't have one (i.e. is a root).
	 */
	P parent();

	/**
	 * @return the receiver.
	 */
	T self();

	/**
	 * @return the distance of the receiver to its root (i.e. {@literal 0} for a root, {@literal 1} for its child, etc.).
	 */
	int level();

	/**
	 * @return the level index of the receiver (i.e. 0-indexed instead of 1-indexed). Always one lower than {@link #level()}.
	 */
	default int levelIndex() {
		return level() - 1;
	}

	/**
	 * @return {@code true}, if the receiver is a root (i.e. returns {@code null} for {@link #parent}). Otherwise {@code false}.
	 * 		Roots are always expected to be at level {@literal 0}.
	 */
	default boolean isRoot() {
		return parent() == null;
	}

	/**
	 * @return {@code true}, if the receiver is a descendant of {@code other}. Otherwise, {@code false}.
	 */
	default boolean isDescendantOf(T other) {
		return other.isAncestorOf(self());
	}

	/**
	 * @return {@code true}, if the receiver is a descendant of or equal to {@code other}. Otherwise, {@code false}.
	 */
	default boolean isDescendantOfOrEqualTo(T other) {
		return other.isAncestorOfOrEqualTo(self());
	}

	/**
	 * @return {@code true}, if the receiver is an ancestor of {@code other}. Otherwise, {@code false}.
	 */
	default boolean isAncestorOf(T other) {
		return level() < other.level() && equals(other.ancestorAtLevel(level()));
	}

	/**
	 * @return {@code true}, if the receiver is an ancestor of or equal to {@code other}. Otherwise, {@code false}.
	 */
	default boolean isAncestorOfOrEqualTo(T other) {
		return equals(other.ancestorAtLevel(level()));
	}

	/**
	 * @return the ancestor of the receiver with the specified level. Will return {@code null} unless
	 *        {@code 0 <= levelOfAncestor <= this.getLevel()}.
	 */
	default T ancestorAtLevel(int levelOfAncestor) {
		return ancestorAtDistance(level() - levelOfAncestor);
	}

	/**
	 * @return the ancestor of the receiver whose {@link #level} is {@code distanceOfAncestor} smaller than the receiver's
	 *        {@link #level}. Equivalent to chaining {@link #parent()} {@code distanceOfAncestor} times. Will return
	 *        {@code null} unless {@code 0 <= distanceOfAncestor <= this.getLevel()}.
	 */
	default T ancestorAtDistance(int distanceOfAncestor) {
		if (distanceOfAncestor < 0 || distanceOfAncestor > level()) {
			return null;
		}
		T ancestor = self();
		for (int distance = distanceOfAncestor; distance > 0; distance--) {
			ancestor = ancestor.parent();
		}
		return ancestor;
	}

	/**
	 * @return the ancestors of the receiver without the receiver itself, ordered from closest to root to furthest from root. Does
	 * 		not include the root.
	 */
	default List<P> ancestors() {
		return ancestorsUntil(0);
	}

	/**
	 * @return the ancestors of the receiver (but not the receiver itself) up to but not including the ancestor with level
	 *        {@code exclusiveLowestLevel}, ordered from closest to the root to furthest from the root. Will return an empty list
	 * 		unless {@code exclusiveLowestLevel < this.parent().level()}. May include the root if {@code exclusiveLowestLevel} is
	 * 		negative.
	 */
	default List<P> ancestorsUntil(int exclusiveLowestLevel) {
		int distance = level() - 1 - Math.max(exclusiveLowestLevel, -1);
		if (distance <= 0) {
			return List.of();
		}

		@SuppressWarnings("unchecked") // the array does not leave this method. List.of() creates a new copy for itself
		P[] ancestors = (P[]) new Parentable[distance];
		P ancestor = parent();
		for (int i = distance - 1; i >= 0; i--) {
			ancestors[i] = ancestor;
			ancestor = ancestor.parent();
		}
		return List.of(ancestors);
	}

	/**
	 * @return the ancestors of the receiver and the receiver itself, ordered from closest to root to furthest from root. Does not
	 * 		include the root.
	 */
	default List<T> withAncestors() {
		return withAncestorsUntil(0);
	}

	/**
	 * @return the receiver itself and its ancestors up to but not including the ancestor with level {@code exclusiveLowestLevel},
	 * 		ordered from closest to the root to furthest from the root. Will return an empty list unless
	 *        {@code exclusiveLowestLevel < this.level()}. May include the root if {@code exclusiveLowestLevel} is negative.
	 */
	default List<T> withAncestorsUntil(int exclusiveLowestLevel) {
		int distance = level() - Math.max(exclusiveLowestLevel, -1);
		if (distance <= 0) {
			return List.of();
		}

		@SuppressWarnings("unchecked") // the array does not leave this method. List.of() creates a new copy for itself
		T[] withAncestors = (T[]) new Parentable[distance];
		T ancestor = self();
		for (int i = distance - 1; i >= 0; i--) {
			withAncestors[i] = ancestor;
			ancestor = ancestor.parent();
		}
		return List.of(withAncestors);
	}

	/**
	 * @return the common ancestor of the receiver and {@code other}. If one is an ancestor of the other, returns that one.
	 */
	default T commonAncestor(T other) {
		T left = this.level() <= other.level() ? self() : this.ancestorAtLevel(other.level());
		T right = other.level() <= this.level() ? other : other.ancestorAtLevel(this.level());

		while (!left.equals(right)) {
			left = left.parent();
			right = right.parent();
		}

		return left;
	}

	static <T extends Parentable<T, P>, P extends T> Set<T> withAllAncestors(Collection<? extends T> parentables) {
		Set<T> parentablesWithAllAncestors = new HashSet<>();
		for (T parentable : parentables) {
			T ancestor = parentable;
			while (!ancestor.isRoot() && parentablesWithAllAncestors.add(ancestor)) {
				ancestor = ancestor.parent();
			}
		}
		return parentablesWithAllAncestors;
	}
}
