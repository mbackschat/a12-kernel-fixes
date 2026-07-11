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
package com.mgmtp.a12.kernel.md.rt.internal.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;

import lombok.NonNull;

/**
 * Provides conversion between {@link DocumentPointer}s and {@link IIdentifier}s.
 */
public class DocumentPointerConverter {

	private DocumentPointerConverter() {
		throw new IllegalStateException("Utility class");
	}

	private static long[] computeIndexes(@NonNull final List<Integer> indexes) {
		return indexes.stream().mapToLong(i -> i).toArray();
	}

	private static List<Integer> toIntList(final int[] repetitions) {
		return Arrays.stream(repetitions).boxed().toList();
	}

	public static IIdentifier convertToIIdentifier(@NonNull final DocumentPointer pointer) {
		return new Identifier(pointer.fullName(), computeIndexes(pointer.repetitionIndexes()));
	}

	public static IIdentifier convertToIIdentifier(@NonNull final DocumentMultiPointer pointer) {
		return new Identifier(pointer.fullName(), computeIndexes(pointer.repetitionIndexes()));
	}

	public static Set<DocumentPointer> convertToDocumentPointerSet(@NonNull final Set<IIdentifier> identifiers) {
		return identifiers
				.stream()
				.map(DocumentPointerConverter::convertToDocumentPointer)
				.collect(Collectors.toSet());
	}

	public static Set<DocumentMultiPointer> convertToDocumentMultiPointerSet(@NonNull final Set<IIdentifier> identifiers) {
		return identifiers
				.stream()
				.map(DocumentPointerConverter::convertToDocumentMultiPointer)
				.collect(Collectors.toSet());
	}

	public static Set<PartiallyKnownDocumentMultiPointer> convertToPartiallyKnownDocumentMultiPointerSet(@NonNull final Set<IIdentifier> identifiers) {
		return identifiers
				.stream()
				.map(DocumentPointerConverter::convertToPartiallyKnownDocumentMultiPointer)
				.collect(Collectors.toSet());
	}

	public static DocumentMultiPointer convertToDocumentMultiPointer(@NonNull IIdentifier identifier) {
		int[] repetitions = toIntArray(identifier.getIndices());
		return DocumentMultiPointer.of(identifier.getName(), toIntList(repetitions));
	}

	public static DocumentPointer convertToDocumentPointer(@NonNull IIdentifier identifier) {
		int[] repetitions = toIntArray(identifier.getIndices());
		return DocumentPointer.of(identifier.getName(), toIntList(repetitions));
	}

	public static PartiallyKnownDocumentMultiPointer convertToPartiallyKnownDocumentMultiPointer(@NonNull IIdentifier identifier) {
		int[] repetitions = toIntArray(identifier.getIndices());
		return PartiallyKnownDocumentMultiPointer.of(identifier.getName(), repetitions);
	}

	public static Set<IIdentifier> convertToIIdentifierSet(@NonNull final Set<? extends DocumentMultiPointer> entityInstances) {
		return entityInstances.stream()
				.map(DocumentPointerConverter::convertToIIdentifier)
				.collect(Collectors.toSet());
	}

	private static int[] toIntArray(final long[] indices) {
		Objects.requireNonNull(indices);
		return Arrays.stream(indices).mapToInt(i -> (int) i).toArray();
	}
}
