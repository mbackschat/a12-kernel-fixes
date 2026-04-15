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

import java.util.Map;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.util.ValidatedImmutableMap;

import lombok.NonNull;

// NOTE: in case of modifications to this class, consider TestDocV2Performance, it offers functionality to
// easily and locally record performance measurements for some relevant operations, before and after the
// code change, and to visually plot the results for easy comparison via Analyze_Improvised_Perftests.ipynb
public final class ImmutableGroupV2Impl
		extends AbstractImmutableGroupInstance<ImmutableGroupV2Impl>
		implements GroupInstanceV2 {
	private final ValidatedImmutableMap<String, RepetitionsV2, SubGroupsInvariants> groups;
	private final ValidatedImmutableMap<String, FieldInstanceV2, FieldsInvariants> fields;

	/**
	 * Must only be called via {@link Builder}, in order to ensure a single code path for parameter validation (in particular any validation that is not delegated to ValidatedImmutableMap because it concerns not a single map)
	 * <br/>
	 * callers <em>must</em> guarantee that any necessary consistency checks on groups and fields have already been done
	 */
	private ImmutableGroupV2Impl(
			ValidatedImmutableMap<String, RepetitionsV2, SubGroupsInvariants> groups,
			ValidatedImmutableMap<String, FieldInstanceV2, FieldsInvariants> fields
	) {
		assert groups.values().stream().flatMap(RepetitionsV2::stream).allMatch(g -> g instanceof AbstractImmutableGroupInstance)
				: "all group instances are expected to be instanceof AbstractImmutableGroupInstance (" +
				" AbstractImmutableGroupInstance#withBatchUpdatesOptimizedInternal relies on this fact).";
		this.groups = groups;
		this.fields = fields;
	}

	public static ImmutableGroupV2Impl emptyInternal() {
		return new Builder(
				ValidatedImmutableMap.empty(SubGroupsInvariants.SINGLETON),
				ValidatedImmutableMap.empty(FieldsInvariants.SINGLETON),
				true).buildIfChanged();
	}

	/**
	 * validates & makes defensive copies of the given subgroups and fields, and then constructs a group that contains them.
	 */
	public static ImmutableGroupV2Impl ofUnvalidatedMutableSubgroupsAndFields(
			@NonNull Map<String, RepetitionsV2> subGroups,
			@NonNull Map<String, FieldInstanceV2> fields) {
		return new Builder(
				ValidatedImmutableMap.ofPotentiallyUncheckedMap(subGroups, SubGroupsInvariants.SINGLETON),
				ValidatedImmutableMap.ofPotentiallyUncheckedMap(fields, FieldsInvariants.SINGLETON),
				false
		).buildIfChanged();
	}

	@Override protected ValidatedImmutableMap<String, RepetitionsV2, SubGroupsInvariants> groupsInternal() {
		return groups;
	}

	@Override protected ValidatedImmutableMap<String, FieldInstanceV2, FieldsInvariants> fieldsInternal() {
		return fields;
	}

	@Override protected ImmutableGroupV2Impl self() {
		return this;
	}

	@Override Builder toLazyCopyBuilder() {
		return new Builder(self());
	}

	static class Builder extends AbstractImmutableGroupInstanceBuilder<ImmutableGroupV2Impl> {
		protected Builder(@NonNull ImmutableGroupV2Impl original) {
			super(original);
		}

		Builder(
				ValidatedImmutableMap<String, RepetitionsV2, SubGroupsInvariants> subGroups,
				ValidatedImmutableMap<String, FieldInstanceV2, FieldsInvariants> fields,
				boolean alreadyCheckedForCollidingFieldAndSubgroupNames) {
			super(subGroups, fields, alreadyCheckedForCollidingFieldAndSubgroupNames);
		}

		@Override protected ImmutableGroupV2Impl buildNewInstanceInternal(
				@NonNull ValidatedImmutableMap<String, RepetitionsV2, SubGroupsInvariants> groupsReadOnly,
				@NonNull ValidatedImmutableMap<String, FieldInstanceV2, FieldsInvariants> fieldsReadOnly) {
			assert validateNoCollidingKeys();
			return new ImmutableGroupV2Impl(groupsReadOnly, fieldsReadOnly);
		}
	}
}
