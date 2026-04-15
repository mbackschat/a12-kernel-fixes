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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.util.ValidatedImmutableMap;

import lombok.NonNull;

// NOTE: in case of modifications to this class, consider TestDocV2Performance, it offers functionality to
// easily and locally record performance measurements for some relevant operations, before and after the
// code change, and to visually plot the results for easy comparison via Analyze_Improvised_Perftests.ipynb
public abstract class AbstractImmutableGroupInstance<S extends AbstractImmutableGroupInstance<S>> implements GroupInstanceV2 {
	protected int cachedHashCode = 0; // this is still thread-safe even though this variable is mutable, because even if it gets
	// computed multiple times, the result will be the same each time. We deliberately accept that in a multithreaded setting,
	// multiple (or even every) threads might independently compute the hash (wich is wasteful): This could be partially or fully
	// prevented with "volatile" or with synchronization, but the performance impact of those mechanisms is likely worse, and less
	// predictable / more risky.

	/**
	 * should not be exposed at the API, to keep the API minimal
	 */
	protected abstract ValidatedImmutableMap<String, RepetitionsV2, SubGroupsInvariants> groupsInternal();

	/**
	 * should not be exposed at the API, to keep the API minimal
	 */
	protected abstract ValidatedImmutableMap<String, FieldInstanceV2, FieldsInvariants> fieldsInternal();

	protected abstract S self();

	/**
	 * Can/Should be used by all modifying (with.*) methods of this group instance, even for small changes. Therefor no data
	 * of the group instance should be copied when the builder is created. It is the builders responsibility to copy data
	 * before modification (only) when necessary.
	 */
	abstract AbstractImmutableGroupInstanceBuilder<S> toLazyCopyBuilder();

	@Override public RepetitionsV2 directSubgroup(@NonNull String name) {
		return groupsInternal().get(name); // the ValidatedImmutableMap takes care
		// of normalizing the null return value for absent keys to RepetitionsV2.empty()
	}

	@Override public FieldInstanceV2 directField(@NonNull String name) {
		return fieldsInternal().get(name);
	}

	@Override public Set<Map.Entry<String, RepetitionsV2>> directSubgroups() {
		// repIsNonEmpty should already be ensured during construction of the group.
		assert groupsInternal().values().stream().allMatch(AbstractImmutableGroupInstance::repIsNonEmpty);
		return groupsInternal().entrySet();
	}

	@Override public Set<Map.Entry<String, FieldInstanceV2>> directFields() {
		return fieldsInternal().entrySet();
	}

	@Override public S withDirectSubgroup(@NonNull String name, RepetitionsV2 groupRepetitions) {
		return toLazyCopyBuilder().putSubgroup(name, groupRepetitions).buildIfChanged();
	}

	@Override public S withDirectField(@NonNull String name, FieldInstanceV2 field) {
		return toLazyCopyBuilder().putField(name, field).buildIfChanged();
	}

	@Override public S withBatchUpdates(@NonNull Collection<UpdateAction> updates) {
		List<UpdateAction> sortedUpdates = updates.stream()
				// NOTE: full sorting is only ok because
				// 1) we define the corresponding behavior for overlapping updates
				// 2) there are currently no removing UpdateActions, in particular no removing actions that overlap with additions
				// NOTE: the sorting must be stable (I think we should guarantee this to get a sensible behavior in case
				// of multiple updateActions for the exact same element - users shouldn't create such redundant batchUpdates,
				// but it if they accidentally doo, it would be really wierd if the sorting was not stable)
				.sorted(Comparator.comparing(UpdateAction::pointer))
				.collect(Collectors.toList());
		return GroupBatchUpdater.batchUpdate(self(), sortedUpdates);
	}

	/**
	 * internal un-optimized implementation for reference/assertions. should return the same result as
	 * {@link #withBatchUpdates(Collection)}
	 * once the handling of non-commutative update ops is resolved. This implementation probably has quadratic runtime if
	 * a large number of fields or subgroups is inserted into a single subgroup, or if a large number of repetitions is inserted
	 * into the one RepetitionsV2 instance
	 */
	public S withBatchUpdatesSlowSimple(@NonNull Collection<UpdateAction> updates) { // for testing
		List<UpdateAction> sortedUpdates = updates.stream().sorted(Comparator.comparing(up -> up.pointer().size())).toList();
		S current = self();
		for (UpdateAction update : sortedUpdates) {
			current = switch (update.type()) {
				case PUT_GROUP_REPETITION -> GroupDocCommonImpl.withGroup(current, update.pointer(), update.argGroupInstance());
				case PUT_FIELD -> GroupDocCommonImpl.withField(current, update.pointer(), update.argFieldInstance());
			};
		}
		return current;
	}

	/**
	 * empty and null RepetitionsV2 are both treated as if the group is not present in the document
	 */
	public static boolean repIsNonEmpty(RepetitionsV2 rep) {
		return rep != null && rep.size() > 0;
	}

	@Override public int hashCode() {
		if (cachedHashCode == 0) { // this is thread safe AFAIK (multiple threads could compute the hash at the same time,
			// but would get the same result; and assignment to cachedHashCode itself is atomic)
			cachedHashCode = Objects.hash(this.groupsInternal(), this.fieldsInternal());
		}
		return cachedHashCode;
	}

	@Override public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj.getClass() != this.getClass()) {
			return false; // we don't want documents to be treated as equal to groups (that would be non-commutative, as documents
			// additionally have the documentModelId flag),
			// and there is currently only a single implementation class for each of group and document
		}
		final AbstractImmutableGroupInstance<?> other = (AbstractImmutableGroupInstance<?>) obj;
		if (!this.groupsInternal().equals(other.groupsInternal())) {
			return false;
		}
		return this.fieldsInternal().equals(other.fieldsInternal());
	}

	@Override public String toString() {
		return DocumentV2InternalUtils.groupToString(this);
	}
}
