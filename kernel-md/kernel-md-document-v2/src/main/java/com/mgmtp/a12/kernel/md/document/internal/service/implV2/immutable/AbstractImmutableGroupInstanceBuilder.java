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

import java.util.Set;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.util.ValidatedImmutableMap;

import lombok.NonNull;

/**
 * All instantiation and updates of groups and documents should be done via the builder
 * in order to have the same checks done for batch updates and single updates (including the fields-vs-subgroups checks that are not
 * delegated to {@link ValidatedImmutableMap} but done by AbstractImmutableGroupInstanceBuilder directly).
 * <p/>
 * Because the builder is used for all updates, it should avoid any unnecessary expensive operations, e.g. copies should only be
 * done if necessary, and redundant validation of already-validated immutable data should be avoided.
 * <p/>
 * Note: builder instances are not thread safe
 */
// NOTE: in case of modifications to this class, consider TestDocV2Performance, it offers functionality to
// easily and locally record performance measurements for some relevant operations, before and after the
// code change, and to visually plot the results for easy comparison via Analyze_Improvised_Perftests.ipynb
abstract class AbstractImmutableGroupInstanceBuilder<G extends AbstractImmutableGroupInstance<G>> {
	protected final G originalIfExists;
	/**
	 * Note: in addition to the {@link SubGroupsInvariants}, all overarching invariants (that depend on both
	 * {@link #subGroupsLazyCopy} and {@link #fieldsLazyCopy}) must also be satisfied at all times (after initial construction of
	 * the builder), i.e. {@link #validateNoCollidingKeys()} would always return true.
	 */
	private final ValidatedImmutableMap.LazyCopyBuilder<String, RepetitionsV2, SubGroupsInvariants> subGroupsLazyCopy;
	/**
	 * invariants that must always hold: analogous to {@link #subGroupsLazyCopy}
	 */
	private final ValidatedImmutableMap.LazyCopyBuilder<String, FieldInstanceV2, FieldsInvariants> fieldsLazyCopy;

	protected AbstractImmutableGroupInstanceBuilder(@NonNull G original) {
		this.originalIfExists = original;
		this.subGroupsLazyCopy = original.groupsInternal().toLazyCopyBuilder();
		this.fieldsLazyCopy = original.fieldsInternal().toLazyCopyBuilder();
		// no need to validateNoCollidingKeys, this is already guaranteed because original was a valid group or document
	}

	protected AbstractImmutableGroupInstanceBuilder( // NOTE: seems to only be used in single-step creation code paths
			@NonNull ValidatedImmutableMap<String, RepetitionsV2, SubGroupsInvariants> subGroups,
			@NonNull ValidatedImmutableMap<String, FieldInstanceV2, FieldsInvariants> fields,
			boolean alreadyCheckedForCollidingFieldAndSubgroupNames
	) {
		this.originalIfExists = null;
		this.subGroupsLazyCopy = subGroups.toLazyCopyBuilder();
		this.fieldsLazyCopy = fields.toLazyCopyBuilder();
		// perform the overarching validation(s) that depend on _both_ field and subgroups, and can therefore not be handled
		// by the ValidatedImmutableMap invariants
		if(!alreadyCheckedForCollidingFieldAndSubgroupNames) {
			validateNoCollidingKeys();
		}
	}

	/**
	 * throw an exception if any subgroup name is also used for a field (or vice versa), otherwise return true.
	 */
	protected boolean validateNoCollidingKeys() {
		Set<String> takenSubgroupNames = this.subGroupsLazyCopy.keySet();
		Set<String> takenFieldNames = this.fieldsLazyCopy.keySet();
		takenFieldNames.stream()
				.filter(takenSubgroupNames::contains)
				.sorted()
				.findFirst()
				.ifPresent(firstCollidingName
						// in order to reuse rejectNameCollision here, we only reject one of the collisions.
						// (typeOfExistingChild and typeOfNewChild could equally well be swapped here)
						-> rejectNameCollision(firstCollidingName, "subgroup", "field"));
		return true;
	}

	G buildIfChanged() {
		if (isUnchangedOriginal()) {
			return originalIfExists;
		} else {
			return buildNewInstanceInternal(
					subGroupsLazyCopy.buildIfChanged(),
					fieldsLazyCopy.buildIfChanged());
		}
	}

	/**
	 * @return true if the map is guaranteed to be unchanged, false if it may have been changed.
	 */
	protected boolean isUnchangedOriginal() {
		return (subGroupsLazyCopy.isUnchangedOriginal() && fieldsLazyCopy.isUnchangedOriginal() && originalIfExists != null);
	}

	/**
	 * Only for use by {@link #buildIfChanged()}. Creates a new instance of {@link G} ({@link GroupInstanceV2} or
	 * {@link DocumentV2}).<br>
	 * Assumes that subGroups and fields are already validated (including all overarching validations like
	 * {@link #validateNoCollidingKeys()}).<br>
	 * Always creates a new instance - if an old instance can be reused, the <em>caller</em> is expected to do this, and avoid the
	 * call.
	 */
	abstract protected G buildNewInstanceInternal(
			@NonNull ValidatedImmutableMap<String, RepetitionsV2, SubGroupsInvariants> subGroups,
			@NonNull ValidatedImmutableMap<String, FieldInstanceV2, FieldsInvariants> fields);

	public AbstractImmutableGroupInstanceBuilder<G> putSubgroup(@NonNull String name, RepetitionsV2 subGroupRepetitions) {
		if (getFieldOrNull(name) != null) { // this check is not local to either the fields or the subGroup map(builder), so
			// we have to do it here, rather than encapsulating it in the ValidatedImmutableMap
			rejectNameCollision(name, "field", "subgroup");
		}
		// put already handles invariant checks, normalizations, and lazy-copy-builder behavior of immutable map
		subGroupsLazyCopy.put(name, subGroupRepetitions);
		return this;
	}

	public AbstractImmutableGroupInstanceBuilder<G> putField(@NonNull String name, FieldInstanceV2 field) {
		// note: getSubgroupOrEmpty(name).size() > 0 is equivalent to no entry existing for the subgroup (see SubGroupsInvariants
		// treatLikeAbsent and substituteAbsent)
		if (getSubgroupOrEmpty(name).size() > 0) { // this check is not local to either the fields or the subGroup map(builder), so
			// we have to do it here, rather than encapsulating it in the ValidatedImmutableMap
			rejectNameCollision(name, "subgroup", "field");
		}
		// put already handles invariant checks, normalizations, and lazy-copy-builder behavior of immutable map
		fieldsLazyCopy.put(name, field);
		return this;
	}

	public RepetitionsV2 getSubgroupOrEmpty(@NonNull String name) {
		return subGroupsLazyCopy.get(name);
	}

	public FieldInstanceV2 getFieldOrNull(@NonNull String name) {
		return fieldsLazyCopy.get(name);
	}

	private static void rejectNameCollision(@NonNull String nameThatIsTakenByOtherTypeOfChild, @NonNull String typeOfExistingChild,
			@NonNull String typeOfNewChild) {
		throw new IllegalArgumentException(String.format(
				"refusing to set a %3$s for name '%1$s' because the name is already used for a %2$s in the same " +
						"parent group. There must not be fields and subgroups with the same name. " +
						"If you really intend to set a %3$s for that name, remove the %2$s first.",
				nameThatIsTakenByOtherTypeOfChild, typeOfExistingChild, typeOfNewChild));
	}

	protected boolean hasFields() {
		return fieldsLazyCopy.size() > 0;
	}
}
