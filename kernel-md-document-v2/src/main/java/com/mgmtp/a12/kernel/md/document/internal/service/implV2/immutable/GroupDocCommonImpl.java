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

import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils.validateLastRepIs1;
import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils.validateLastRepIsWildcard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.PathPart;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentMergeConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;

import lombok.NonNull;

public class GroupDocCommonImpl {

	public static <T extends GroupInstanceV2> T withDirectSubgroup(@NonNull T base, @NonNull String name, int index,
			GroupInstanceV2 group) {
		var rep = base.directSubgroup(name);
		if (rep == null) {
			rep = RepetitionsV2.empty();
		}
		return castResult(base, base.withDirectSubgroup(name, rep.withReplacedAt(index, group)));
	}

	public static <T extends GroupInstanceV2> T withGroup(@NonNull T base, @NonNull DocumentPointer pointer,
			/* NonNull, use rejectNullInsert for custom error message */ GroupInstanceV2 subgroupToAdd) {
		rejectNullInsert(subgroupToAdd);
		return updateNested(base, pointer, (group, name, repetition) -> group.withDirectSubgroup(name, repetition, subgroupToAdd));
	}

	public static <T extends GroupInstanceV2> T withGroupAllRepetitions(@NonNull T base, @NonNull DocumentPointer pointer,
			/* NonNull, use rejectNullInsert for custom error message */ RepetitionsV2 groupRepetitions) {
		rejectNullInsert(groupRepetitions);
		validateLastRepIsWildcard(pointer); // last rep will be ignored, but a non-wildcard value strongly indicates wrong usage
		return updateNested(base, pointer, (group, name, _repetition) -> group.withDirectSubgroup(name, groupRepetitions));
	}

	public static void rejectNullInsert(Object groupOrFieldToInsert) {
		Validate.isTrue(groupOrFieldToInsert != null,
				"nested insert of a null subgroup or field makes no sense but will implicitly add ancestor groups");
	}

	public static <T extends GroupInstanceV2> T withDirectSubgroupRepetitionInserted(@NonNull T base, @NonNull String name,
			int index,
			@NonNull GroupInstanceV2 group) {
		var rep = base.directSubgroup(name);
		if (rep == null) {
			rep = RepetitionsV2.empty();
		}
		return castResult(base, base.withDirectSubgroup(name, rep.withInserted(index, group)));
	}

	public static <T extends GroupInstanceV2> T withGroupRepetitionInserted(@NonNull T base, @NonNull DocumentPointer pointer,
			/* NonNull, use rejectNullInsert for custom error message */ GroupInstanceV2 subGroupToInsert) {
		rejectNullInsert(subGroupToInsert);
		return updateNested(base, pointer,
				(group, name, repetition) -> withDirectSubgroupRepetitionInserted(group, name, repetition, subGroupToInsert));
	}

	public static <T extends GroupInstanceV2> T withDirectSubgroupRepetitionAppended(@NonNull T base, @NonNull String name,
			@NonNull GroupInstanceV2 group) {
		var rep = base.directSubgroup(name);
		if (rep == null) {
			rep = RepetitionsV2.empty();
		}
		return castResult(base, base.withDirectSubgroup(name, rep.withAppended(group)));
	}

	public static <T extends GroupInstanceV2> T withGroupRepetitionAppended(@NonNull T base, @NonNull DocumentPointer pointer,
			/* NonNull, use rejectNullInsert for custom error message */ GroupInstanceV2 subGroupToAppend) {
		rejectNullInsert(subGroupToAppend);
		validateLastRepIsWildcard(pointer); // last rep will be ignored, but a non-wildcard value strongly indicates wrong usage
		return updateNested(base, pointer,
				(group, name, _repetition) -> withDirectSubgroupRepetitionAppended(group, name, subGroupToAppend));
	}

	public static <T extends GroupInstanceV2> T withGroupRemoved(@NonNull T base, @NonNull DocumentPointer pointer) {
		return removeNested(base, pointer, GroupDocCommonImpl::withDirectSubgroupRemoved);
	}

	public static <T extends GroupInstanceV2> T withDirectSubgroupRemoved(@NonNull T base, @NonNull String name, int index) {
		return withDirectSubgroup(base, name, index, null);
	}

	public static <T extends GroupInstanceV2> T withGroupAllRepetitionsRemoved(@NonNull T base, @NonNull DocumentPointer pointer) {
		validateLastRepIsWildcard(pointer); // last rep will be ignored, but a non-wildcard value strongly indicates wrong usage
		return removeNested(base, pointer, (group, name, _repetition) -> withDirectSubgroupAllRepetitionsRemoved(group, name));
	}

	public static <T extends GroupInstanceV2> T withDirectSubgroupAllRepetitionsRemoved(@NonNull T base, @NonNull String name) {
		return castResult(base, base.withDirectSubgroup(name, null));
	}

	public static <T extends GroupInstanceV2> T withGroupEmptiedButNotRemoved(@NonNull T base, @NonNull DocumentPointer pointer) {
		return removeNested(base, pointer, GroupDocCommonImpl::withDirectSubgroupEmptiedButNotRemoved);
	}

	public static <T extends GroupInstanceV2> T withDirectSubgroupEmptiedButNotRemoved(@NonNull T base, @NonNull String name,
			int index) {
		return base.directSubgroup(name, index) != null ?
				withDirectSubgroup(base, name, index, GroupInstanceV2.empty()) :
				base;
	}

	public static <T extends GroupInstanceV2> T withField(@NonNull T base, @NonNull DocumentPointer pointer,
			/* NonNull, use rejectNullInsert for custom error message */ FieldInstanceV2 field) {
		rejectNullInsert(field);
		validateLastRepIs1(pointer); // last rep will be ignored, but a non-1 value strongly indicates wrong usage
		return updateNested(base, pointer, (group, name, _repetition) -> group.withDirectField(name, field));
	}

	public static <T extends GroupInstanceV2> T withDirectFieldRemoved(T base, String name) {
		return castResult(base, base.withDirectField(name, null));
	}

	public static <T extends GroupInstanceV2> T withFieldRemoved(T base, DocumentPointer pointer) {
		validateLastRepIs1(pointer); // last rep will be ignored, but a non-1 value strongly indicates wrong usage
		return removeNested(base, pointer, (group, name, _repetition) -> withDirectFieldRemoved(group, name));
	}

	public static <T extends GroupInstanceV2> T withDirectFieldValue(T base, String name, Object value) {
		var field = base.directField(name);
		return castResult(base,
				base.withDirectField(name, field != null ? field.withValue(value) : FieldInstanceV2.ofValue(value)));
	}

	public static <T extends GroupInstanceV2> T withFieldValue(T base, DocumentPointer pointer, Object value) {
		validateLastRepIs1(pointer); // last rep will be ignored, but a non-1 value strongly indicates wrong usage
		return updateNested(base, pointer, (group, name, _repetition) -> withDirectFieldValue(group, name, value));
	}

	/**
	 * Equivalent to {@code (T) updated},<br/>
	 * but intended to be a bit harder to get wrong, and avoids the noisy unchecked-cast warning at the call site.
	 * <p/>
	 * Background:
	 * The type system doesn't currently know that the {@code with*} methods of groups/documents will return instantces
	 * of the same subtype as the original instance (this seems to be somewhat overcomplicated to achieve with java's type system)
	 * Therefore we only specify this informally via documentation (all implementations are in kernel, so this should be
	 * safe enough).
	 *
	 * @param orig
	 * 		the original group or document instance on wich the {@code with*} method was called. This only serves
	 * 		to determine the target type of the cast.
	 * @param updated
	 * 		the group or document instance to be casted
	 */
	private static <T extends GroupInstanceV2> T castResult(T orig, GroupInstanceV2 updated) {
		@SuppressWarnings("unchecked") // see description in javadoc for why this is safe here, when the method is used as intended
		T casted = (T) updated;
		return casted;
	}

	public static <R> R readNested(GroupInstanceV2 group, DocumentPointer pointer, GroupFn<R> readAtLeaf) {
		PathPart first = pointer.getPartAtLevel(0);
		if (pointer.size() == 1) {
			return readAtLeaf.apply(group, first.name(), first.repetitionIndex());
		} else {
			GroupInstanceV2 subGroup = group.directSubgroup(first.name(), first.repetitionIndex());
			if (subGroup == null) {
				return null; // nothing to read
			}
			return readNested(subGroup, pointer.rest(), readAtLeaf);
		}
	}

	/**
	 * returns an updated group with the removal applied, if the element-to-be removed was present in the group. Otherwise, the
	 * unchanged group is returned.
	 */
	public static <G extends GroupInstanceV2> G removeNested(G group, DocumentPointer pointer,
			GroupUpdateFn removeAtLeaf
	) {
		PathPart first = pointer.getPartAtLevel(0);
		if (pointer.size() == 1) {
			// we can cast here, because GroupUpdateFn#apply is required to return a G when given a G
			return castResult(group, removeAtLeaf.apply(group, first.name(), first.repetitionIndex()));
		} else {
			GroupInstanceV2 subGroup = group.directSubgroup(first.name(), first.repetitionIndex());
			if (subGroup == null) {
				return group; // nothing to remove if leaf or parent of leaf is missing
			}
			// we can cast here, because G#withDirectSubgroup is required to return a G
			return castResult(group, group.withDirectSubgroup(first.name(), first.repetitionIndex(),
					removeNested(subGroup, pointer.rest(), removeAtLeaf)));
		}
	}

	/**
	 * returns an updated version of the group. if parent elements of the to-be-updated element are missing in the original group,
	 * they will be inserted.
	 */
	public static <G extends GroupInstanceV2> G updateNested(G group, DocumentPointer pointer, GroupUpdateFn updateLeaf
	) {
		if (pointer.size() < 1) {
			// we can't simply return the leaf update when passed a 0-sized pointer, because the type might not conform to G (e.g.
			// if G is DocumentV2, but the update is only the more general GroupInstanceV2
			throw new IllegalArgumentException("0-sized pointer not allowed here");
		}
		PathPart first = pointer.getPartAtLevel(0);
		if (pointer.size() == 1) {
			// we can cast here, because GroupUpdateFn#apply is required to return a G when given a G
			return castResult(group, updateLeaf.apply(group, first.name(), first.repetitionIndex()));
		} else {
			GroupInstanceV2 subGroup = group.directSubgroup(first.name(), first.repetitionIndex());
			if (subGroup == null) {
				subGroup = GroupInstanceV2.empty(); // create missing parent. NOTE: we could optimize this and only create
				// the parents after the update, because the pre-update parents will be discarded after it gets updated
			}
			// we can cast here, because G#withDirectSubgroup is required to return a G
			return castResult(group, group.withDirectSubgroup(first.name(), first.repetitionIndex(),
					updateNested(subGroup, pointer.rest(), updateLeaf)));
		}
	}

	@FunctionalInterface
	public interface GroupFn<R> {
		R apply(GroupInstanceV2 group, String name, int repetition);
	}

	@FunctionalInterface
	public interface GroupUpdateFn extends GroupFn<GroupInstanceV2> {
		/**
		 * @return the update group instance. the returned group (or document) must be an instance of the same implementing
		 * 		class as the given group
		 */
		GroupInstanceV2 apply(GroupInstanceV2 group, String name, int repetition);
	}

	/**
	 * see {@link GroupInstanceV2#withMerged(GroupInstanceV2, IDocumentMergeConfig)}
	 */
	public static <G extends GroupInstanceV2> G merge(
			G g1, GroupInstanceV2 g2,
			IDocumentMergeConfig mergeConfig
	) {
		List<UpdateAction> updates = new ArrayList<>();
		g2.traverse(new IDocumentV2Visitor() {
			@Override public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
				if (group.isEmpty() // only leaf groups need to be explicitly added
						// don't overwrite a (potentially non-empty) group that already exists in g1
						&& g1.group(pointerRelativeToBase) == null
				) {
					updates.add(UpdateAction.putGroup(pointerRelativeToBase, group));
				}
				return IDocumentV2Visitor.super.visitGroup(pointerRelativeToBase, group);
			}

			@Override public RepetitionDescendType visitRepetitions(DocumentPointer pointerRelativeToBase, RepetitionsV2 rep) {
				if (mergeConfig.shouldUseCustomMergeForRepetitions(pointerRelativeToBase)) {
					updates.addAll(mergeConfig.customMergeForRepetitions(
							pointerRelativeToBase,
							g1.groupAllRepetitions(pointerRelativeToBase),
							rep
					));
					return RepetitionDescendType.SKIP_CHILDREN;
				}
				return IDocumentV2Visitor.super.visitRepetitions(pointerRelativeToBase, rep);
			}

			@Override public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 field) {
				if (field.value() != null) { // It probably makes more sense to not let null values overwrite existing non-null values
					updates.add(UpdateAction.putFieldValue(pointerRelativeToBase, field.value()));
				}
				IDocumentV2Visitor.super.visitField(pointerRelativeToBase, field);
			}
		});
		return castResult(g1, g1.withBatchUpdates(updates));
	}
}
