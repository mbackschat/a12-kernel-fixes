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

import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils.assertHasNoTrailingWildcardRep;
import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils.assertLastRepIsWildcard;
import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils.cascadeNulls;
import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils.checkOneBasedIndex;
import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils.validateLastRepIs1;
import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils.validateLastRepIsWildcard;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentMergeConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.GroupDocCommonImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.ImmutableGroupV2Impl;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Additional specialized utility methods can be found in {@link com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils}.
 */
@OnlyForUsage
public interface GroupInstanceV2 {
	/**
	 * @return an RepetitionsV2 with all present repetitions of the subgroup that corresponds to name.
	 * 		If the subgroup is not repeatable, the maximum size of the returned RepetitionsV2 will be 1.
	 * 		If the subgroup is not present (equivalent to no repetition being present), then an empty RepetitionsV2 will be returned.
	 * 		Never returns null.
	 */
	RepetitionsV2 directSubgroup(@NonNull String name);

	/**
	 * @return the field, or null if no field with the name is present in the group
	 */
	FieldInstanceV2 directField(@NonNull String name);

	/**
	 * @return a read-only collection of read-only Map.Entry objects that represent the named subgroups of this group.
	 * 		Entries that contain an empty RepetitionsV2 will be ommitted.
	 */
	Set<Map.Entry<String, RepetitionsV2>> directSubgroups();

	/**
	 * @return a read-only collection of read-only Map.Entry objects that represent the named fields in this group.
	 */
	Set<Map.Entry<String, FieldInstanceV2>> directFields();

	/**
	 * @param groupRepetitions
	 * 		Note: if null or an empty RepetitionsV2, this will remove the group from the document
	 * @implSpec implementations must return an instance of their own class, not just any GroupInstanceV2
	 */
	GroupInstanceV2 withDirectSubgroup(@NonNull String name, RepetitionsV2 groupRepetitions);

	/**
	 * @implSpec implementations must return an instance of their own class, not just any GroupInstanceV2
	 */
	GroupInstanceV2 withDirectField(@NonNull String name, FieldInstanceV2 field);

	/**
	 * Performs multiple insertions in one batch, with potentially improved performance.
	 * The given updateActions will be reordered so that if there is a group update for /RG/G and also a subgroup/field update for
	 * /RG/G/G2/x, then the group update will be applied first, and the subgroup/field updates afterards. i.e. the field update will
	 * not be overwritten by the ancestor group update.
	 * <p/>
	 * The update actions should be created with the static factory methods of {@link UpdateAction} (e.g.
	 * {@link UpdateAction#putField(String, FieldInstanceV2)}).
	 * <p/>
	 * Note: This method current only supports additions, not removals (see {@link UpdateAction.UpdateActionType}
	 *
	 * @implSpec implementations must return an instance of their own class, not just any GroupInstanceV2
	 */
	// Note: withBatchUpdates can be functionally implemented based on withDirectSubgroup and withDirectField,
	// but needs access to the internals for an efficient (ca O(n*log(n))) implementation
	GroupInstanceV2 withBatchUpdates(@NonNull Collection<UpdateAction> updates);

	/**
	 * Instances of GroupInstanceV2 are compared for equality based on their current value, independent of the concrete
	 * sub-interface or subclass.
	 */
	@Override boolean equals(Object other);

	/**
	 * see {@link #equals}
	 */
	@Override int hashCode();

	// UTILS =====
	static GroupInstanceV2 empty() {
		return ImmutableGroupV2Impl.emptyInternal();
	}

	static GroupInstanceV2 of(
			@NonNull Map<String, RepetitionsV2> subGroups,
			@NonNull Map<String, FieldInstanceV2> fields) {
		return ImmutableGroupV2Impl.ofUnvalidatedMutableSubgroupsAndFields(subGroups, fields);
	}

	default boolean isEmpty() {
		return this.directSubgroups().isEmpty() && this.directFields().isEmpty();
	}

	/**
	 * @param index
	 * 		1-based index (see {@link RepetitionsV2#get(int)}
	 * @throws IndexOutOfBoundsException
	 * 		if {@code index < 1}
	 */
	default GroupInstanceV2 directSubgroup(@NonNull String name, int index) {
		checkOneBasedIndex(index);
		RepetitionsV2 repetitions = this.directSubgroup(name);
		return repetitions != null ? repetitions.get(index) : null;
	}

	default GroupInstanceV2 group(@NonNull DocumentPointer pointer) {
		if (pointer.size() == 0) { // edge-case that is not suitable for the general readNested implementation
			return this;
		}
		return GroupDocCommonImpl.readNested(this, pointer, GroupInstanceV2::directSubgroup);
	}

	default GroupInstanceV2 group(@NonNull String pointer) {
		return group(DocumentPointer.of(pointer));
	}

	/**
	 * @return a (non-null, possibly empty) RepetitionsV2 that contains all repetitions of the group pointed to by pointer.
	 * 		The last repetition index of pointer is expected to be 0, for wildcard.
	 * 		If the group is not repeatable, the RepetitionsV2 will simply contain the single present repetition
	 * 		(or nothing if the group is not present).
	 */
	default RepetitionsV2 groupAllRepetitions(@NonNull DocumentPointer pointer) {
		validateLastRepIsWildcard(pointer); // last rep will be ignored, but a non-wildcard value strongly indicates wrong usage
		RepetitionsV2 rep = GroupDocCommonImpl.readNested(this, pointer, (group, name, _repetition) -> group.directSubgroup(name));
		return rep != null ? rep : RepetitionsV2.empty();
	}

	/**
	 * see {@link #groupAllRepetitions(DocumentPointer)}
	 */
	default RepetitionsV2 groupAllRepetitions(@NonNull String pointer) {
		return groupAllRepetitions(DocumentPointer.of(pointer));
	}

	/**
	 * Note: the last repetition of the pointer is expected to be 1, because fields are not repeatable
	 */
	default FieldInstanceV2 field(@NonNull DocumentPointer pointer) {
		validateLastRepIs1(pointer); // last rep will be ignored, but a non-1 value strongly indicates wrong usage
		return GroupDocCommonImpl.readNested(this, pointer, (group, name, _repetition) -> group.directField(name));
	}

	default FieldInstanceV2 field(@NonNull String pointer) {
		return field(DocumentPointer.of(pointer));
	}

	default Object directFieldValue(@NonNull String name) {
		return cascadeNulls(this.directField(name), FieldInstanceV2::value);
	}

	default Object fieldValue(@NonNull DocumentPointer pointer) {
		return cascadeNulls(this.field(pointer), FieldInstanceV2::value);
	}

	default Object fieldValue(@NonNull String pointer) {
		return fieldValue(DocumentPointer.of(pointer));
	}

	/**
	 * sets or replaces the subgroup and repetition at (name, index) to group
	 *
	 * @param group
	 * 		the group instance to put as a direct subgroup, can be {@code null} to remove the subgroup
	 */
	default GroupInstanceV2 withDirectSubgroup(@NonNull String name, int index, GroupInstanceV2 group) {
		return GroupDocCommonImpl.withDirectSubgroup(
				this, name, index, group
		);
	}

	/**
	 * sets or replaces the subgroup and repetition at the path specified by pointer to group
	 */
	default GroupInstanceV2 withGroup(@NonNull DocumentPointer pointer, @NonNull GroupInstanceV2 group) {
		return GroupDocCommonImpl.withGroup(this, pointer, group);
	}

	/**
	 * see {@link #withGroup(DocumentPointer, GroupInstanceV2)}
	 */
	default GroupInstanceV2 withGroup(@NonNull String pointer, @NonNull GroupInstanceV2 group) {
		return withGroup(DocumentPointer.of(pointer), group);
	}

	/**
	 * Note: the last repetition of pointer must be 0
	 */
	default GroupInstanceV2 withGroupAllRepetitions(@NonNull DocumentPointer pointer, @NonNull RepetitionsV2 groupRepetitions) {
		return GroupDocCommonImpl.withGroupAllRepetitions(this, pointer, groupRepetitions);
	}

	default GroupInstanceV2 withGroupAllRepetitions(@NonNull String pointer, @NonNull RepetitionsV2 groupRepetitions) {
		return withGroupAllRepetitions(DocumentPointer.of(pointer), groupRepetitions);
	}

	/**
	 * <em>insert</em> {@code group} into {@code base} at the subgroup and repetition specified by pointer.
	 * If equal or higher repetitions were present  for the subgroup, their repetition indices willbe
	 * shifted upwards to make root for the inserted group repetition.
	 */
	default GroupInstanceV2 withDirectSubgroupRepetitionInserted(@NonNull String name, int index, @NonNull GroupInstanceV2 group) {
		return GroupDocCommonImpl.withDirectSubgroupRepetitionInserted(this, name, index, group);
	}

	default GroupInstanceV2 withGroupRepetitionInserted(@NonNull DocumentPointer pointer, @NonNull GroupInstanceV2 group) {
		return GroupDocCommonImpl.withGroupRepetitionInserted(this, pointer, group);
	}

	default GroupInstanceV2 withGroupRepetitionInserted(@NonNull String pointer, @NonNull GroupInstanceV2 group) {
		return withGroupRepetitionInserted(DocumentPointer.of(pointer), group);
	}

	/**
	 * Appends group as the last repetition of the RepetitionsV2 pointed-to by pointer.
	 *
	 * @param pointer
	 * 		the last repetition index of this pointer will be ignored (as the next free repetition will be used)
	 */
	default GroupInstanceV2 withGroupRepetitionAppended(@NonNull DocumentPointer pointer, @NonNull GroupInstanceV2 group) {
		return GroupDocCommonImpl.withGroupRepetitionAppended(this, pointer, group);
	}

	/**
	 * see {@link #withGroupRepetitionAppended(DocumentPointer, GroupInstanceV2)}
	 */
	default GroupInstanceV2 withGroupRepetitionAppended(@NonNull String pointer, @NonNull GroupInstanceV2 group) {
		return withGroupRepetitionAppended(DocumentPointer.of(pointer), group);
	}

	/**
	 * removes the subgroup and repetion at pointer. If higher repetitions exist, their indices shift downwards; otherwise (if the
	 * repetition was 1 and was the only  present repetition for the subgroup)
	 * the repetition becomes empty, which is equivalent to fully removing the repetable subgroup.
	 */
	default GroupInstanceV2 withGroupRemoved(@NonNull DocumentPointer pointer) {
		return GroupDocCommonImpl.withGroupRemoved(this, pointer);
	}

	/**
	 * see {@link #withGroupRemoved(DocumentPointer)}
	 */
	default GroupInstanceV2 withGroupRemoved(@NonNull String pointer) {
		return withGroupRemoved(DocumentPointer.of(pointer));
	}

	/**
	 * similar to {@link #withGroupRemoved(DocumentPointer)}
	 */
	default GroupInstanceV2 withDirectSubgroupRemoved(@NonNull String name, int index) {
		return GroupDocCommonImpl.withDirectSubgroupRemoved(this, name, index);
	}

	/**
	 * Note: the last repetition of pointer must be 0 (wildcard)
	 */
	default GroupInstanceV2 withGroupAllRepetitionsRemoved(@NonNull DocumentPointer pointer) {
		return GroupDocCommonImpl.withGroupAllRepetitionsRemoved(this, pointer);
	}

	/**
	 * see {@link #withGroupAllRepetitionsRemoved(DocumentPointer)}
	 */
	default GroupInstanceV2 withGroupAllRepetitionsRemoved(@NonNull String pointer) {
		return withGroupAllRepetitionsRemoved(DocumentPointer.of(pointer));
	}

	default GroupInstanceV2 withDirectSubgroupAllRepetitionsRemoved(@NonNull String name) {
		return GroupDocCommonImpl.withDirectSubgroupAllRepetitionsRemoved(this, name);
	}

	/**
	 * replaces the subgroup repetition at pointer with an empty group if an entry existed for the repetition.
	 * this allows to empty a particular repetition, without affecting the repetition indices of higher repetitions of that
	 * subgroup.
	 */
	default GroupInstanceV2 withGroupEmptiedButNotRemoved(@NonNull DocumentPointer pointer) {
		return GroupDocCommonImpl.withGroupEmptiedButNotRemoved(this, pointer);
	}

	/**
	 * see {@link #withGroupEmptiedButNotRemoved(DocumentPointer)}
	 */
	default GroupInstanceV2 withGroupEmptiedButNotRemoved(@NonNull String pointer) {
		return withGroupEmptiedButNotRemoved(DocumentPointer.of(pointer));
	}

	default GroupInstanceV2 withDirectSubgroupEmptiedButNotRemoved(@NonNull String name, int index) {
		return GroupDocCommonImpl.withDirectSubgroupEmptiedButNotRemoved(this, name, index);
	}

	default GroupInstanceV2 withField(@NonNull DocumentPointer pointer, @NonNull FieldInstanceV2 field) {
		return GroupDocCommonImpl.withField(this, pointer, field);
	}

	default GroupInstanceV2 withField(@NonNull String pointer, @NonNull FieldInstanceV2 field) {
		return withField(DocumentPointer.of(pointer), field);
	}

	default GroupInstanceV2 withDirectFieldRemoved(@NonNull String name) {
		return GroupDocCommonImpl.withDirectFieldRemoved(this, name);
	}

	default GroupInstanceV2 withFieldRemoved(@NonNull DocumentPointer pointer) {
		return GroupDocCommonImpl.withFieldRemoved(this, pointer);
	}

	default GroupInstanceV2 withFieldRemoved(@NonNull String pointer) {
		return withFieldRemoved(DocumentPointer.of(pointer));
	}

	/**
	 * only supported for groups, unsupported operation for documents (documents
	 * can only contain groups, not fields directly)
	 */
	default GroupInstanceV2 withDirectFieldValue(@NonNull String name, Object value) {
		return GroupDocCommonImpl.withDirectFieldValue(this, name, value);
	}

	/**
	 * Note: if the field is not yet present in the group/document, it will be created, including any ancestor groups that may
	 * be missing; even if value is null (in that case, a field with value null will be inserted)
	 */
	default GroupInstanceV2 withFieldValue(@NonNull DocumentPointer pointer, Object value) {
		return GroupDocCommonImpl.withFieldValue(this, pointer, value);
	}

	/**
	 * see {@link #withFieldValue(DocumentPointer, Object)}
	 */
	default GroupInstanceV2 withFieldValue(@NonNull String pointer, Object value) {
		return withFieldValue(DocumentPointer.of(pointer), value);
	}

	/**
	 * see {@link #withMerged(GroupInstanceV2, IDocumentMergeConfig)}
	 */
	default GroupInstanceV2 withMerged(@NonNull GroupInstanceV2 other) {
		return withMerged(other, ptr -> false);
	}

	/**
	 * Creates a merged group that contains the (recursive) subgroups and fields of this group and the other group.
	 * If fields with identical path and repetitions exist in both groups, and the field from the other group has a non-null
	 * value, then the field from the other group overrides the current field.
	 * Subgroups of identical name and repetition will be merged recursively, unless configured otherwise via the mergeConfig
	 * parameter.
	 * <p/>
	 * The above default behavior can be modified via {@link IDocumentMergeConfig}, e.g. in order to concatenate repetitions or
	 * perform custom logic such as custom-sorted or de-duplicated concatenation.
	 */
	default GroupInstanceV2 withMerged(@NonNull GroupInstanceV2 other, @NonNull IDocumentMergeConfig mergeConfig) {
		return GroupDocCommonImpl.merge(this, other, mergeConfig);
	}

	/**
	 * traverses all groups, fields and recursive subgroups in the document or group.
	 *
	 * @param visitor
	 * 		an (e.g. anonymous) implementation of {@link IDocumentV2Visitor}
	 */
	default void traverse(@NonNull IDocumentV2Visitor visitor) {
		traverseInternal(DocumentPointer.of(List.of()), this, visitor);
	}

	private static void traverseInternal(DocumentPointer pointer, GroupInstanceV2 group, IDocumentV2Visitor visitor) {
		assertHasNoTrailingWildcardRep(pointer);
		IDocumentV2Visitor.DescendType dt = visitor.visitGroup(pointer, group);
		//noinspection StatementWithEmptyBody
		if (dt.equals(IDocumentV2Visitor.DescendType.SKIP_CHILDREN)) {
			// nothing to do
		} else if (dt.equals(IDocumentV2Visitor.DescendType.VISIT_CHILDREN)) {
			for (var entry : group.directSubgroups()) {
				String subGroupName = entry.getKey();
				RepetitionsV2 subGroupReps = entry.getValue();
				DocumentPointer subGroupRepsPointer = pointer.withAppended(subGroupName, DocumentMultiPointer.WILDCARD);
				traverseRepetitionsInternal(subGroupRepsPointer, subGroupReps, visitor);
			}
			for (var entry : group.directFields()) {
				String fieldName = entry.getKey();
				FieldInstanceV2 field = entry.getValue();
				visitor.visitField(pointer.withAppended(fieldName, 1), field);
			}
		} else if (dt.isVisitSingleSubgroup()) {
			String subGroupName = dt._internal_singleSubgroupNameToVisitIfTypeIsSingle();
			RepetitionsV2 subGroupReps = group.directSubgroup(subGroupName);
			DocumentPointer subGroupRepsPointer = pointer.withAppended(subGroupName, DocumentMultiPointer.WILDCARD);
			traverseRepetitionsInternal(subGroupRepsPointer, subGroupReps, visitor);
		} else {
			throw new RuntimeException("internal: unexpected descend type: " + dt);
		}
		visitor.endVisitGroup(pointer, group);
	}

	private static void traverseRepetitionsInternal(
			DocumentPointer pointer,
			RepetitionsV2 rep,
			IDocumentV2Visitor visitor
	) {
		assertLastRepIsWildcard(pointer);
		IDocumentV2Visitor.RepetitionDescendType dt = visitor.visitRepetitions(pointer, rep);
		//noinspection StatementWithEmptyBody
		if (dt.equals(IDocumentV2Visitor.RepetitionDescendType.SKIP_CHILDREN)) {
			// skip contents of repetitions
		} else if (dt.equals(IDocumentV2Visitor.RepetitionDescendType.VISIT_CHILDREN)) {
			for (int iOneBased = 1; iOneBased <= rep.size(); iOneBased++) {
				GroupInstanceV2 subGroup = rep.get(iOneBased);
				traverseInternal(pointer.withLastRepetitionReplaced(iOneBased), subGroup, visitor);
			}
		} else if (dt.isVisitSingle()) {
			int repetitionToVisit = dt._internal_repetitionOneBasedIndex();
			if (repetitionToVisit <= rep.size()) {
				GroupInstanceV2 subGroup = rep.get(repetitionToVisit);
				traverseInternal(pointer.withLastRepetitionReplaced(repetitionToVisit), subGroup, visitor);
			}
		} else {
			throw new RuntimeException("internal: unexpected visit type: " + dt);
		}
		visitor.endVisitRepetitions(pointer, rep);
	}
}
