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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;

import lombok.NonNull;

/**
 * The path information to an entity that's the cause for a validation error used by IMessage.
 * If the path does not contain {@code indices < 0} then it is convertible to a {@link DocumentPointer}.
 */
// keep impl package private - should only be instantiated via the interface's factory methods
class PartiallyKnownDocumentMultiPointerImpl implements PartiallyKnownDocumentMultiPointer {
	private final List<String> fullName;
	private final List<Integer> repetitions;

	public PartiallyKnownDocumentMultiPointerImpl(@NonNull String fullName, @NonNull List<Integer> repetitions) {
		this.fullName = DocumentPointerImpl.parseFullName(fullName);
		// with the repetitions, make sure that our internal repetitions list can't be
		// affected by external modifications in the entityInstance's list; to ensure immutability:
		this.repetitions = Collections.unmodifiableList(new ArrayList<>(repetitions));

		checkValidity();
	}

	static PartiallyKnownDocumentMultiPointerImpl of(@NonNull String fullName, int[] repetitions) {
		return new PartiallyKnownDocumentMultiPointerImpl(fullName,
				Arrays.stream(repetitions).boxed().toList());
	}

	private void checkValidity() {
		// we assert that, just in case this changes in the future, and we have to handle the additional values
		// if this assert triggers, then we have to adapt the javadoc
		assert (repetitions.stream().noneMatch(val -> val < 0 && val != IEntityInstance.UNKNOWN)) :
				"The repetition indexes are only expected to be >= 0 or -5";
	}

	@Override public String fullName() {
		return fullName.stream().collect(Collectors.joining("/", "/", ""));
	}

	@Override public List<Integer> repetitionIndexes() {
		// unmodifiableList() is redundant here, but this way the implementation
		// is more robust against accidental changes in the constructor.
		// And unmodifiableList() is super cheap if the list already is an UnmodifiableList.
		//noinspection RedundantUnmodifiable
		return Collections.unmodifiableList(repetitions);
	}

	@Override
	public String toString() {
		return "PartiallyKnownDocumentMultiPointerImpl[" + this.fullName() + ", " + this.repetitions + "]";
	}

	@Override public int hashCode() {
		return Objects.hash(fullName, repetitions);
	}

	@Override public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof PartiallyKnownDocumentMultiPointerImpl entityInstance) {
			return Objects.equals(fullName, entityInstance.fullName) && Objects.equals(repetitions, entityInstance.repetitions);
		} else {
			return false;
		}
	}
}
