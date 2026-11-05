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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import lombok.NonNull;

// keep impl package private - should only be instantiated via the interface's factory methods
class DocumentMultiPointerImpl implements DocumentMultiPointer {
	private final List<PathPart> pathParts;

	private DocumentMultiPointerImpl(@NonNull List<PathPart> pathParts) {
		// we want to make sure that the list is a copy AND unmodifiable (so that neither we nor the caller can modify its contents)
		//noinspection Java9CollectionFactory
		pathParts = Collections.unmodifiableList(new ArrayList<>(pathParts));
		assert pathParts.stream().noneMatch(
				pp -> pp.repetitionIndex() < 0) : "PathPart is responsible for repetition indexes >= 0, but contains index < 0";
		this.pathParts = pathParts;
	}

	public static @NonNull DocumentMultiPointer of(@NonNull List<PathPart> pathParts) {
		return new DocumentMultiPointerImpl(pathParts);
	}

	public static @NonNull DocumentMultiPointer of(@NonNull String pathWithRepetitions) {
		return of(DocumentPointerImpl.parsePathParts(pathWithRepetitions));
	}

	public static @NonNull DocumentMultiPointer of(@NonNull DocumentPointer pointer) {
		return of(pointer.getPathParts());
	}

	static @NonNull DocumentMultiPointer ofPathWithAllWildcards(@NonNull String pathWithoutRepetitions) {
		List<String> fullNameParts = DocumentPointerImpl.parseFullName(pathWithoutRepetitions);
		return of(fullNameParts.stream()
				.map(namePart -> PathPart.of(namePart, DocumentMultiPointer.WILDCARD)).toList());
	}

	@Override public boolean containsAncestorOf(@NonNull DocumentPointer other) {
		List<PathPart> documentPointerPathParts = other.getPathParts();

		if (documentPointerPathParts.size() < pathParts.size()) {
			return false;
		}

		return IntStream
				.range(0, pathParts.size())
				.allMatch(i -> {
					if (!pathParts.get(i).name().equals(documentPointerPathParts.get(i).name())) {
						return false;
					}

					if (documentPointerPathParts.get(i).repetitionIndex() > 0) {
						return documentPointerPathParts.get(i).repetitionIndex() == pathParts.get(i).repetitionIndex() ||
								pathParts.get(i).repetitionIndex() == DocumentMultiPointer.WILDCARD;
					} else {
						assert i == pathParts.size() - 1 : "we expect to be at the last index";
						assert pathParts.size() == documentPointerPathParts.size() : "DocumentPointer can only contain a 0 in the last part";
						// This is the case where the DocumentMultiPointer points to one or all repetitions
						// of a group (one or multiple GroupInstanceV2) [1],
						// but other is a DocumentPointer with last index 0, so it references
						// the RepetitionsV2, and the individual group instances (to which the multipointer points) in the RepetitionsV2
						// are _children_ of the RepetitionsV2
						// -> none of those group instances is an _ancestor_ of the RepetitionsV2
						//    -> the multipointer does _not_ contain an ancestor of the document pointer
						//       -> return false
						//
						// [1] (it could also be a field, but that would not make sense given that other
						//     has a wildcard as the last repetition index)
						return false;
					}
				});
	}

	@Override public boolean containsDescendentOf(@NonNull DocumentPointer other) {
		List<PathPart> documentPointerPathParts = other.getPathParts();

		if (documentPointerPathParts.size() > pathParts.size()) {
			return false;
		}

		return IntStream
				.range(0, documentPointerPathParts.size())
				.allMatch(i -> {
					if (!pathParts.get(i).name().equals(documentPointerPathParts.get(i).name())) {
						return false;
					}

					if (documentPointerPathParts.get(i).repetitionIndex() > 0) {
						return documentPointerPathParts.get(i).repetitionIndex() == pathParts.get(i).repetitionIndex() ||
								pathParts.get(i).repetitionIndex() == DocumentMultiPointer.WILDCARD;
					} else {
						assert i == documentPointerPathParts.size() - 1 : "DocumentPointer can only contain a 0 in the _last_ part";
						// case where the DocumentMultiPointer is the group/field, but other is the RepetitionsV2.
						// The individual group instances are a "child" of the RepetitionsV2
						// -> the multipointer contains a descendent of the document pointer
						return true;
					}
				});
	}

	@Override public boolean contains(@NonNull DocumentPointer other) {
		return containsAncestorOf(other) && containsDescendentOf(other);
	}

	@Override public List<PathPart> getPathParts() {
		return Collections.unmodifiableList(pathParts); // unmodifiableList should be redundant here, but seems more robust for
		// future changes
	}

	@Override public boolean equals(Object o) {
		if (!(o instanceof DocumentMultiPointer that))
			return false;
		return Objects.equals(pathParts, that.getPathParts());
	}

	@Override public int hashCode() {
		return Objects.hashCode(getPathParts());
	}

	@Override public String toString() {
		var str = PathPartImpl.pathPartListToString(getPathParts());
		return "".equals(str) ? "(empty DocumentMultiPointer)" : str;
	}

	@Override public int compareTo(@NonNull DocumentMultiPointer o) {
		int sharedSize = Math.min(size(), o.size());
		for (int i = 0; i < sharedSize; i++) {
			int levelCompare = getPartAtLevel(i).compareTo(o.getPartAtLevel(i));
			if (levelCompare != 0) {
				return levelCompare;
			}
		}
		return Integer.compare(size(), o.size());
	}
}
