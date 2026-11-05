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

package com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.NonNull;

/**
 * similar to {@link ImmutableMap}, but for List.
 * @implNote see the Implementation Note in {@link ImmutableMap}
 */
// NOTE: in case of modifications to this class, consider TestDocV2Performance, it offers functionality to
// easily and locally record performance measurements for some relevant operations, before and after the
// code change, and to visually plot the results for easy comparison via Analyze_Improvised_Perftests.ipynb
public final class ImmutableList<T> extends AbstractList<T> {
	private final @NonNull List<T> delegate;

	/**
	 * must only be called internally (or from the builder), with a list that is guaranteed to not get modified (internally or
	 * externally)
	 */
	private ImmutableList(@NonNull List<T> neverAgainModifiedElements) {
		// use unmodifiable view as an extra layer of protection and to detect missing defensive copies / mutability bugs
		this.delegate = Collections.unmodifiableList(neverAgainModifiedElements);
	}

	/**
	 * If the given list already is an {@link ImmutableList, it is simply returned} otherwise its contents are copied into a new
	 * ImmutableList, which is then returned.
	 */
	public static <T> ImmutableList<T> ofPotentiallyMutableList(@NonNull List<T> other) {
		if (other instanceof ImmutableList<T> otherImmutableList) {
			return otherImmutableList;
		}
		List<T> delegateGuaranteedToNeverBeModified = Collections.unmodifiableList(new ArrayList<>(other));
		return new ImmutableList<>(delegateGuaranteedToNeverBeModified);
	}

	public static <T> ImmutableList<T> empty() {
		return new ImmutableList<>(Collections.emptyList());
	}

	@Override public T get(int index) {
		return delegate.get(index);
	}

	@Override public int size() {
		return delegate.size();
	}

	public LazyCopyBuilder<T> toLazyCopyBuilder() {
		return new LazyCopyBuilder<>(this);
	}

	// it appears that copying the delegate directly can be much faster than new ArrayList<>(this), so we use this
	// method.
	// (in the withFieldValue_growth_1000_reps test case, this caused approximately 2.5x speed difference)
	@NonNull private ArrayList<T> makeMutableCopyOfDelegateForBuilder() {
		return new ArrayList<>(delegate);
	}

	/**
	 * An immutable list builder that minimizes defensive copies, analogous to {@link ImmutableMap.LazyCopyBuilder}.
	 * The builder enables batched updates and only copies the underlying list if/when a change actually occurs.
	 * <p>
	 * Builder instances are <em>not</em> thread safe.
	 */
	public static class LazyCopyBuilder<T> {
		private final @NonNull ImmutableList<T> original;
		/**
		 * No mutable reference to this list must ever be leaked, because it will be passed to the new ImmutableList during
		 * buildIfChanged(), without further defensive copy.
		 */
		private List<T> modifiedInternal = null;
		private boolean isFrozen = false;

		private LazyCopyBuilder(@NonNull ImmutableList<T> original) {
			this.original = original;
		}

		private void preModifyChecksAndInit() {
			if (isFrozen) {
				throw new IllegalStateException("this builder already frozen and cannot be modified anymore");
			}
			if (modifiedInternal == null) {
				modifiedInternal = original.makeMutableCopyOfDelegateForBuilder();
			}
		}

		public ImmutableList<T> buildIfChanged() {
			isFrozen = true;
			if (modifiedInternal != null) {
				modifiedInternal = Collections.unmodifiableList(modifiedInternal); // wrap the builder's reference too, as a safeguard against accidental later mutations via the builder
				return new ImmutableList<>(modifiedInternal);
			} else {
				return original;
			}
		}

		public LazyCopyBuilder<T> add(T element) {
			preModifyChecksAndInit();
			modifiedInternal.add(element);
			return this;
		}

		public LazyCopyBuilder<T> add(int index, T element) {
			preModifyChecksAndInit();
			modifiedInternal.add(index, element);
			return this;
		}

		public LazyCopyBuilder<T> remove(int index) {
			preModifyChecksAndInit();
			modifiedInternal.remove(index);
			return this;
		}

		public T get(int index) {
			return getEffectiveListInternal().get(index);
		}

		public int size() {
			return getEffectiveListInternal().size();
		}

		public boolean isUnchangedOriginal() {
			return modifiedInternal == null;
		}

		/**
		 * The returned list must remain internal to this builder, see modifiedInternal for details.
		 */
		private List<T> getEffectiveListInternal() {
			// wrapping in unmodifiableList is not strictly necessary, but it seems more robust against accidental
			// modifications or leaks of mutable references. And it should be reasonably cheap.
			return modifiedInternal != null ? Collections.unmodifiableList(modifiedInternal) : original;
		}
	}
}
