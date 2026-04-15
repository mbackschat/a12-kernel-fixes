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

package com.mgmtp.a12.kernel.md.document.apiV2.immutable;

import java.util.List;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.ImmutableRepetitionsImpl;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

@OnlyForUsage
public interface RepetitionsV2 extends Iterable<GroupInstanceV2> {
	/**
	 * @param oneBasedIndex
	 * 		the <em>1-based</em> index
	 * @return the group, or null for out-of-bounds index
	 *
	 * @throws IllegalArgumentException
	 * 		if {@literal index <= 0}
	 */
	GroupInstanceV2 get(int oneBasedIndex);

	/**
	 * the highest 1-based index of any non-null group in this RepetitionsV2 instance
	 */
	int size();

	RepetitionsV2 withInserted(int oneBasedIndex, @NonNull GroupInstanceV2 group);

	RepetitionsV2 withRemoved(int oneBasedIndex);

	// util methods (can functionally be implemented based on the core methods):
	List<GroupInstanceV2> toList();

	default Stream<GroupInstanceV2> stream() {
		return toList().stream();
	}

	/**
	 * Instances of RepetitionsV2 are compared for equality based on their current list value (independent of the concrete
	 * implementation class)
	 */
	@Override boolean equals(Object other);

	/**
	 * see {@link #equals}
	 */
	@Override int hashCode();

	// UTILS ====
	static RepetitionsV2 empty() {
		return ImmutableRepetitionsImpl.empty();
	}

	static RepetitionsV2 of(@NonNull List<GroupInstanceV2> repetitions) {
		return ImmutableRepetitionsImpl.of(repetitions);
	}

	/**
	 * if group is null, this method will return {@code withRemoved(index)} (and not perform an insert)
	 */
	default RepetitionsV2 withReplacedAt(int index, GroupInstanceV2 group) {
		// NOTE: this is inefficient
		var withRemoved = this.withRemoved(index);
		return group != null ? withRemoved.withInserted(index, group) : withRemoved;
	}

	/**
	 * @param group
	 * 		must not be null
	 */
	default RepetitionsV2 withAppended(@NonNull GroupInstanceV2 group) {
		return withInserted(size() + 1, group);
	}
}
