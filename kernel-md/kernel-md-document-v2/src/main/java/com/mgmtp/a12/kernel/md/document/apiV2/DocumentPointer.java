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

package com.mgmtp.a12.kernel.md.document.apiV2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Represents a path (subgroup names and repetitions) into a document. A path may point to a subgroup or field.
 * <p>
 * All repetition indexes must be {@code > {@value DocumentMultiPointer#WILDCARD}}, <em>except</em> the last repetition index can be
 * {@value DocumentMultiPointer#WILDCARD}
 * (for referencing {@link RepetitionsV2}).
 * <p>
 * Note: implementations must be immutable
 */
@OnlyForUsage
public interface DocumentPointer extends Comparable<DocumentPointer> {

	PathPart getPartAtLevel(int level);

	List<PathPart> getPathParts();

	int size();

	static DocumentPointer of(@NonNull List<PathPart> pathParts) {
		return DocumentPointerImpl.of(pathParts);
	}

	static DocumentPointer of(@NonNull String pathWithRepetitions) {
		return DocumentPointerImpl.of(pathWithRepetitions);
	}

	static DocumentPointer of(@NonNull String fullName, @NonNull List<Integer> repetitions) {
		return DocumentPointerImpl.of(fullName, repetitions);
	}

	static DocumentPointer of(@NonNull List<String> pathNames, @NonNull List<Integer> repetitions) {
		return of(PathPartImpl.toPathPartList(pathNames, repetitions));
	}

	default DocumentPointer rest() {
		return DocumentPointerImpl.of(getPathParts().subList(1, size()));
	}

	default DocumentPointer parent() {
		return of(getPathParts().subList(0, size() - 1));
	}

	default DocumentPointer withAppended(@NonNull String name, @NonNull Integer repetition) {
		List<PathPart> newPathParts = new ArrayList<>(getPathParts());
		newPathParts.add(new PathPartImpl(name, repetition));
		return DocumentPointerImpl.of(newPathParts);
	}

	default DocumentPointer withConcatenated(@NonNull DocumentPointer other) {
		if (other.size() == 0) {
			return this;
		}

		ArrayList<PathPart> pathParts = new ArrayList<>(this.getPathParts());
		pathParts.addAll(other.getPathParts());
		return of(pathParts);
	}

	default DocumentPointer withLastRepetitionReplaced(int oneBasedRepetition) {
		if (this.size() <= 0) {
			throw new IllegalStateException("cannot be called on a zero-size pointer");
		}
		List<PathPart> newPathParts = new ArrayList<>(getPathParts());
		PathPart lastPart = newPathParts.get(newPathParts.size() - 1);
		newPathParts.set(newPathParts.size() - 1, new PathPartImpl(lastPart.name(), oneBasedRepetition));
		return DocumentPointerImpl.of(newPathParts);
	}

	default String fullName() {
		return getPathParts().stream().map(pp -> "/" + pp.name()).collect(Collectors.joining());
	}

	default List<Integer> repetitionIndexes() {
		return getPathParts().stream().map(PathPart::repetitionIndex).toList();
	}

	/**
	 * @return true if this pointer is an ancestor of (or equal to) the given pointer
	 */
	default boolean isAncestorOf(@NonNull DocumentPointer pointer) {
		if (pointer.size() < size()) {
			return false;
		}

		// root is ancestor of everything
		if (this.size() == 0) {
			return true;
		}

		List<PathPart> otherPathParts = pointer.getPathParts().subList(0, size());

		// handle the last part separate, as we allow `0` as index
		PathPart lastPart = this.getPathParts().getLast();
		PathPart lastPartOther = otherPathParts.getLast();
		if (!lastPart.name().equals(lastPartOther.name())) {
			return false;
		}
		if (!lastPart.equals(lastPartOther) && lastPart.repetitionIndex() != 0) {
			return false;
		}

		// if there are more than one parts, we have to check everything else with just equality
		if (this.size() > 1) {
			// because DocumentPointers cannot contain wildcard (0) repetitions in other positions than the
			// last, we know that the non-last path parts will have all concrete (non-0) repetitions,
			// so it is sufficient to check for equality here.
			return this.parent().getPathParts()
					// parent cannot be used, as otherPathParts might be larger than this
					.equals(otherPathParts.subList(0, size() - 1));
		} else {
			return true;
		}
	}

	/**
	 * @return true if this pointer is a descendent (or equal to) the given pointer
	 */
	default boolean isDescendantOf(@NonNull DocumentPointer pointer) {
		return pointer.isAncestorOf(this);
	}

	/**
	 * implementations must implement equals (and hashCode) and compare path + repetitions, ignoring the specific implementation
	 * class
	 */
	@Override boolean equals(Object o);

	/**
	 * see {@link #equals(Object)}
	 */
	@Override int hashCode();
}