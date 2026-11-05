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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * The path information to an entity that's the cause for a validation error used by IMessage
 * If the path does not contain {@code indices < 0} then it is convertible to a {@link DocumentPointer}.
 * <p>
 * Allows for repetition indexes to be {@value #UNKNOWN} (unknown).
 * <p>
 * Note: implementations must be immutable
 */
@OnlyForUsage
public interface PartiallyKnownDocumentMultiPointer {
	int WILDCARD = DocumentMultiPointer.WILDCARD;
	int UNKNOWN = -5;

	static PartiallyKnownDocumentMultiPointer of(@NonNull DocumentPointer documentPointer) {
		return new PartiallyKnownDocumentMultiPointerImpl(documentPointer.fullName(), documentPointer.repetitionIndexes(), null);
	}

	static PartiallyKnownDocumentMultiPointer of(@NonNull DocumentPointer documentPointer,
			Object semanticIndexValueForUnknownIndexGroup) {
		return new PartiallyKnownDocumentMultiPointerImpl(documentPointer.fullName(), documentPointer.repetitionIndexes(),
				semanticIndexValueForUnknownIndexGroup);
	}

	static PartiallyKnownDocumentMultiPointer of(@NonNull String fullName, @NonNull List<Integer> repetitions) {
		return new PartiallyKnownDocumentMultiPointerImpl(fullName, repetitions, null);
	}

	static PartiallyKnownDocumentMultiPointer of(@NonNull String fullName, @NonNull List<Integer> repetitions,
			Object semanticIndexValueForUnknownIndexGroup) {
		return new PartiallyKnownDocumentMultiPointerImpl(fullName, repetitions, semanticIndexValueForUnknownIndexGroup);
	}

	/**
	 * @deprecated since 31.2.0. Use {@link #of(String, List)} instead.
	 */
	@Deprecated(since = "31.2.0")
	static PartiallyKnownDocumentMultiPointer of(@NonNull String fullName, int[] repetitions) {
		return new PartiallyKnownDocumentMultiPointerImpl(fullName, Arrays.stream(repetitions).boxed().toList(), null);
	}

	/**
	 * @return the path containing all path names, e.g. {@code /A/B/C}.
	 */
	String fullName();

	/**
	 * The repetition indices can contain a
	 * <ul>
	 *     <li><b>number > 0</b>: concrete index of the repetition</li>
	 *     <li><b>number == {@value #WILDCARD}</b>: a wildcard that means all repetitions (see {@link #WILDCARD}).</li>
	 *     <li><b>number == {@value #UNKNOWN}</b>: no repetition with the searched semantic index (see {@link #UNKNOWN}).</li>
	 * </ul>
	 *
	 * @return a list of indices for each of the path elements.
	 */
	List<Integer> repetitionIndexes();

	/**
	 * @return {@link Optional#empty()} if there is no repetition with value {@value #UNKNOWN} ({@link #UNKNOWN}). Otherwise,
	 * 		return a pointer to the instances of the index group for which the semantic index could not be resolved (e.g. for
	 * 		pointer {@literal /RootGroup[3]/IndexGroup[-5]/MyField[1]} the pointer {@literal /RootGroup[3]/IndexGroup[0]} would be
	 * 		returned).
	 *
	 * @see #semanticIndexValueForUnknownIndexGroup()
	 */
	Optional<DocumentMultiPointer> pointerToUnknownIndexGroup();

	/**
	 * @return {@link Optional#empty()} if there is no repetition with value {@value #UNKNOWN} ({@link #UNKNOWN}) or if the field
	 * 		value used for the semantic index was not filled. Otherwise, return the value for which the unknown index group could
	 * 		not be resolved (see also {@link #pointerToUnknownIndexGroup()}).
	 */
	Optional<Object> semanticIndexValueForUnknownIndexGroup();

	/**
	 * @return the {@link DocumentPointer} if the {@link PartiallyKnownDocumentMultiPointer} is convertible or empty otherwise.
	 */
	default Optional<DocumentPointer> toDocumentPointer() {
		// we do not need to check the length of both lists, as we only construct valid instances
		List<Integer> repetitions = repetitionIndexes();

		if (repetitions.subList(0, repetitions.size() - 1).stream().allMatch(repetition -> repetition > WILDCARD) &&
				repetitions.getLast() >= WILDCARD) {
			try {
				return Optional.of(DocumentPointer.of(fullName(), repetitions));
			} catch (IllegalArgumentException e) {
				assert false : "not expected to fail after our checks succeed";
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}

	/**
	 * @return the {@link DocumentMultiPointer} if the {@link PartiallyKnownDocumentMultiPointer} is convertible or empty otherwise.
	 */
	default Optional<DocumentMultiPointer> toDocumentMultiPointer() {
		// we do not need to check the length of both lists, as we only construct valid instances
		List<Integer> repetitions = repetitionIndexes();

		if (repetitions.stream().allMatch(repetition -> repetition >= WILDCARD)) {
			try {
				return Optional.of(DocumentMultiPointer.of(fullName(), repetitionIndexes()));
			} catch (IllegalArgumentException e) {
				assert false : "not expected to fail after our checks succeed";
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}
}
