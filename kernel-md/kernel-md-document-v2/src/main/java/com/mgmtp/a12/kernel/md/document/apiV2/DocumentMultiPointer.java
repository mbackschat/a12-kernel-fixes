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

import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Similar to {@link DocumentPointer} but allows the use of {@link #WILDCARD} as repetition indexes in the pointer.
 * <p>
 * Methods that take a DocumentMultiPointer allow to handle multiple entities with the same path and different repetitions.
 * <p>
 * A DocumentMultiPointer can be thought of as a representation of a set of one or more DocumentPointers that share the same
 * fullName
 * but have different repetitions. For each {@link PathPart} of the DocumentMultiPointer that has repetition {@link #WILDCARD},
 * this
 * (hypothetical, potentially infinite) set contains a document pointer for <em>every</em> (relevant) non-wildcard repetition
 * index.
 * <br>
 * i.e. {@code DocumentMultiPointer.of("a[0]/b[2]")} corresponds to the set<br>
 * {@code DocumentPointer.of("a[1]/b[2]"), DocumentPointer.of("a[2]/b[2]"), DocumentPointer.of("a[3]/b[2]"), ...}
 * <p>
 * Note: DocumentMultiPointers always reference sets of <em>group instances</em> or <em>field instances</em>, but never reference
 * a RepetitionsV2 node of the document tree (as a DocumentPointer with trailing {@code 0} index would). Accordingly, when
 * a DocumentMultiPointer is interpreted as a set of DocumentPointers, this set never contains a DocumentPointer with trailing
 * {@code 0} index.
 * <p>
 * Internal Note: implementations must be immutable
 */
@OnlyForUsage
public interface DocumentMultiPointer extends Comparable<DocumentMultiPointer> {

	/**
	 * A wildcard repetition index.
	 */
	int WILDCARD = 0;

	/**
	 * Returns true if the set of DocumentPointers that corresponds to this DocumentMultiPointer (see {@link DocumentMultiPointer}
	 * for
	 * details), contains a DocumentPointer that is an ancestor of the given document pointer {@code other} (ancestor as defined by
	 * {@link DocumentPointer#isAncestorOf}).
	 * <p>
	 * <h1>Examples</h1>
	 * {@snippet class = TestJavaDocDocumentMultiPointer region = containsAncestorOf}
	 */
	boolean containsAncestorOf(@NonNull DocumentPointer other);

	/**
	 * Returns true if the set of DocumentPointers that corresponds this DocumentMultiPointer (see {@link DocumentMultiPointer} for
	 * details), contains a DocumentPointer that is a descendent of the given document pointer {@code other} (ancestor as defined by
	 * {@link DocumentPointer#isDescendantOf}).
	 * <p>
	 * <h1>Examples</h1>
	 * {@snippet class = TestJavaDocDocumentMultiPointer region = containsDescendentOf}
	 */
	boolean containsDescendentOf(@NonNull DocumentPointer other);

	/**
	 * @return true, if {@link #containsAncestorOf(DocumentPointer)} {@code &&} {@link #containsDescendentOf(DocumentPointer)}.
	 */
	boolean contains(@NonNull DocumentPointer other);

	/**
	 * Create the {@link DocumentMultiPointer} from a list of {@link PathPart}s.
	 *
	 * <h1>Examples</h1>
	 * {@snippet class = TestJavaDocDocumentMultiPointer region = createOfPathParts}
	 *
	 * @throws IllegalArgumentException
	 * 		if a PathPart is null or has an empty name.
	 */
	static @NonNull DocumentMultiPointer of(@NonNull List<PathPart> pathParts) throws IllegalArgumentException {
		return DocumentMultiPointerImpl.of(pathParts);
	}

	/**
	 * Create the {@link DocumentMultiPointer} from a path with optionally set repetitions.
	 *
	 * <h1>Examples</h1>
	 * {@snippet class = TestJavaDocDocumentMultiPointer region = createOfPathWithRepetitions}
	 *
	 * @param pathWithRepetitions
	 * 		if a path part does not contain a repetition index, then a repetition index of '{@code 1}' is used.
	 */
	static @NonNull DocumentMultiPointer of(@NonNull String pathWithRepetitions) {
		return DocumentMultiPointerImpl.of(pathWithRepetitions);
	}

	/**
	 * Create the {@link DocumentMultiPointer} from a list of path names and a list of their respective repetitions.
	 *
	 * <h1>Examples</h1>
	 * {@snippet class = TestJavaDocDocumentMultiPointer region = createOfPathListAndRepetitions}
	 *
	 * @param pathNames
	 * 		the list of names in the path
	 * @param repetitions
	 * 		the repetitions of each path element, each repetition index has to be added, even if it is '{@code 1}'.
	 * @throws IllegalArgumentException
	 * 		if the lists don't have the same length.
	 */
	static @NonNull DocumentMultiPointer of(@NonNull List<String> pathNames, @NonNull List<Integer> repetitions)
			throws IllegalArgumentException {
		return of(PathPartImpl.toPathPartList(pathNames, repetitions));
	}

	/**
	 * Create the {@link DocumentMultiPointer} from a full name and a list of their respective repetitions.
	 * Similar to {@link #of(List, List)} but with the full name as a single string.
	 *
	 * <h1>Examples</h1>
	 * {@snippet class = TestJavaDocDocumentMultiPointer region = createOfPathAndRepetitions}
	 *
	 * @param fullName
	 * 		the full name of the path without repetitions
	 * @param repetitions
	 * 		the repetitions of each path element, each repetition index has to be added, even if it is '{@code 1}'.
	 * @throws IllegalArgumentException
	 * 		if the repetitions lists does not have the same length as the amount of names in fullName.
	 */
	static @NonNull DocumentMultiPointer of(@NonNull String fullName, @NonNull List<Integer> repetitions)
			throws IllegalArgumentException {
		return of(DocumentPointerImpl.parsePathParts(fullName, repetitions));
	}

	/**
	 * Create from a {@link DocumentPointer}, this is always valid to create.
	 */
	static @NonNull DocumentMultiPointer of(@NonNull DocumentPointer pointer) {
		return DocumentMultiPointerImpl.of(pointer);
	}

	/**
	 * Similar to {@link #of(String)} but interprets all indexes as a {@link #WILDCARD} index.
	 *
	 * <h1>Examples</h1>
	 * {@snippet class = TestJavaDocDocumentMultiPointer region = createOfPathWithAllWildcards}
	 */
	static @NonNull DocumentMultiPointer ofPathWithAllWildcards(String pathWithoutRepetitions) {
		return DocumentMultiPointerImpl.ofPathWithAllWildcards(pathWithoutRepetitions);
	}

	default int size() {
		return getPathParts().size();
	}

	default DocumentMultiPointer parent() {
		return of(getPathParts().subList(0, size() - 1));
	}

	default PathPart getPartAtLevel(int level) {
		return getPathParts().get(level);
	}

	List<PathPart> getPathParts();

	default String fullName() {
		return getPathParts().stream().map(pp -> "/" + pp.name()).collect(Collectors.joining());
	}

	default List<Integer> repetitionIndexes() {
		return getPathParts().stream().map(PathPart::repetitionIndex).toList();
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
