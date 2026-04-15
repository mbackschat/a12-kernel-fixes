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

package com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.util.ImmutableList;

import lombok.NonNull;

public class ImmutableRepetitionsImpl implements RepetitionsV2 {
	private final ImmutableList<GroupInstanceV2> repetitions;

	/**
	 * must only be called from {@link ImmutableRepetitionsImpl.Builder}, because the builder takes care of parameter validation.
	 */
	private ImmutableRepetitionsImpl(ImmutableList<GroupInstanceV2> repetitions) {
		this.repetitions = repetitions;
	}

	public static ImmutableRepetitionsImpl of(@NonNull List<GroupInstanceV2> repetitions) {
		return new Builder(ImmutableList.ofPotentiallyMutableList(repetitions), false).buildIfChanged();
	}

	public static RepetitionsV2 empty() {
		return new Builder(ImmutableList.empty(), false).buildIfChanged();
	}

	@Override public int size() {
		return repetitions.size();
	}

	@Override public GroupInstanceV2 get(int oneBasedIndex) {
		DocumentV2InternalUtils.checkOneBasedIndex(oneBasedIndex);
		int zeroBasedIndex = oneBasedIndex - 1;
		if (repetitions.size() <= zeroBasedIndex) {
			return null;
		}
		return repetitions.get(zeroBasedIndex);
	}

	@Override public List<GroupInstanceV2> toList() {
		return Collections.unmodifiableList(repetitions); // already unmodifiable, but extra wrapper for robustness
	}

	@Override public ImmutableRepetitionsImpl withInserted(int oneBasedIndex, @NonNull GroupInstanceV2 group) {
		return toLazyCopyBuilder().insertInPlace(oneBasedIndex, group).buildIfChanged();
	}

	@Override public ImmutableRepetitionsImpl withRemoved(int oneBasedIndex) {
		return toLazyCopyBuilder().removeInPlace(oneBasedIndex).buildIfChanged();
	}

	@Override public int hashCode() {
		return DocumentV2InternalUtils.hashRepetitions(this);
	}

	@SuppressWarnings("EqualsDoesntCheckParameterClass") // is checked in equalsRepetitions
	@Override public boolean equals(Object obj) {
		return DocumentV2InternalUtils.equalsRepetitions(this, obj);
	}

	@Override public String toString() {
		return DocumentV2InternalUtils.repetitionsToString(this);
	}

	@Override public @NonNull Iterator<GroupInstanceV2> iterator() {
		return toList().iterator();
	}

	ImmutableRepetitionsImpl.Builder toLazyCopyBuilder() {
		return new ImmutableRepetitionsImpl.Builder(this);
	}

	public static class Builder {
		protected final ImmutableRepetitionsImpl originalIfExists;
		ImmutableList.LazyCopyBuilder<GroupInstanceV2> wrappedBuilder;

		private Builder(ImmutableRepetitionsImpl original) {
			this.originalIfExists = original;
			this.wrappedBuilder = original.repetitions.toLazyCopyBuilder();
		}

		private Builder(ImmutableList<GroupInstanceV2> repetitions, boolean alreadyValidated) {
			this.originalIfExists = null;
			this.wrappedBuilder = repetitions.toLazyCopyBuilder();
			if(!alreadyValidated) {
				validateRepetitions(repetitions);
			}
		}

		private static void validateRepetitions(@NonNull ImmutableList<GroupInstanceV2> unvalidated) {
			unvalidated.forEach(g -> requireNonNullRepetition(g, () -> "but got : " + unvalidated));
		}

		private static void requireNonNullRepetition(GroupInstanceV2 repetition, Supplier<String> extraMessageSupplier) {
			Objects.requireNonNull(repetition, () -> "no repetition may be null. " + extraMessageSupplier.get());
		}

		public boolean isUnchangedOriginal() {
			return originalIfExists != null &&  wrappedBuilder.isUnchangedOriginal();
		}

		Builder removeInPlace(int oneBasedIndex) {
			DocumentV2InternalUtils.checkOneBasedIndex(oneBasedIndex);
			if (oneBasedIndex > wrappedBuilder.size()) {
				return this;
			}
			wrappedBuilder.remove(oneBasedIndex - 1);
			return this;
		}

		Builder insertInPlace(int oneBasedIndex, @NonNull GroupInstanceV2 subGroup) {
			DocumentV2InternalUtils.checkOneBasedIndex(oneBasedIndex);
			requireNonNullRepetition(subGroup,
					() -> "inserting null into repetitions (as a means of deletion or otherwise) can lead to inconsistent states");
			int zeroBasedIndex = oneBasedIndex - 1;
			while (wrappedBuilder.size() < zeroBasedIndex) {
				wrappedBuilder.add(GroupInstanceV2.empty());
			}
			wrappedBuilder.add(zeroBasedIndex, subGroup);
			return this;
		}

		Builder replaceInPlace(int oneBasedIndex, @NonNull GroupInstanceV2 subGroup) {
			return this.removeInPlace(oneBasedIndex).insertInPlace(oneBasedIndex, subGroup);
		}

		GroupInstanceV2 getOr(int oneBasedIndex, @NonNull Supplier<GroupInstanceV2> fallback) {
			if (oneBasedIndex > wrappedBuilder.size()) {
				return fallback.get();
			}
			return wrappedBuilder.get(oneBasedIndex - 1);
		}

		public ImmutableRepetitionsImpl buildIfChanged() {
			if (isUnchangedOriginal()) {
				return originalIfExists;
			} else {
				return new ImmutableRepetitionsImpl(wrappedBuilder.buildIfChanged());
			}
		}
	}
}
