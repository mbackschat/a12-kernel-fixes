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

package com.mgmtp.a12.kernel.md.document.apiV2.utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.documentchanges.Change;
import com.mgmtp.a12.kernel.md.document.apiV2.documentchanges.DocumentChanges;
import com.mgmtp.a12.kernel.md.document.apiV2.documentchanges.internal.DocumentChangesImpl;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@OnlyForUsage
public class DocumentV2Utils {
	private DocumentV2Utils() {
		throw new UnsupportedOperationException("Static utility class");
	}

	/**
	 * Recursively removes all
	 * <ul>
	 *     <li>empty fields: fields that have a value of {@code null} or and empty string ({@code ""})</li>
	 *     <li>empty groups: groups (or group repetitions) that contain no subgroups and no fields</li>
	 * </ul>
	 *
	 * @param document
	 * 		the input document
	 * @return a new document with all the empty groups and fields removed
	 */
	public static DocumentV2 removeEmptyFieldsAndGroups(@NonNull DocumentV2 document) {
		return (DocumentV2) removeEmptyFieldsAndGroups((GroupInstanceV2) document);
	}

	/**
	 * Recursively removes all
	 * <ul>
	 *     <li>empty fields: fields that have a value of {@code null} or and empty string ({@code ""})</li>
	 *     <li>empty groups: groups (or group repetitions) that contain no subgroups and no fields</li>
	 * </ul>
	 *
	 * @param group
	 * 		the input group
	 * @return a new group with all the empty groups and fields removed
	 */
	public static GroupInstanceV2 removeEmptyFieldsAndGroups(@NonNull GroupInstanceV2 group) {
		for (var fieldEntry : group.directFields()) {
			// null and "" are both handled the same
			if (fieldEntry.getValue().value() == null || (fieldEntry.getValue().value() instanceof String fv && fv.isEmpty())) {
				group = group.withDirectFieldRemoved(fieldEntry.getKey());
			}
		}

		for (var subGroupEntry : group.directSubgroups()) {
			var repetitions = removeEmptyFieldsAndGroups(subGroupEntry.getValue());
			group = group.withDirectSubgroup(subGroupEntry.getKey(), repetitions);
		}

		return group;
	}

	/**
	 * Recursively removes all
	 * <ul>
	 *     <li>empty fields: fields that have a value of {@code null} or and empty string ({@code ""})</li>
	 *     <li>empty groups: groups (or group repetitions) that contain no subgroups and no fields</li>
	 * </ul>
	 *
	 * @param repetitions
	 * 		the input repetitions
	 * @return the new repetitions with all the emptied groups
	 */
	public static RepetitionsV2 removeEmptyFieldsAndGroups(@NonNull RepetitionsV2 repetitions) {
		for (int iOneBased = repetitions.size(); iOneBased > 0; iOneBased--) {
			var subGroup = repetitions.get(iOneBased);
			subGroup = removeEmptyFieldsAndGroups(subGroup);

			if (subGroup.isEmpty()) {
				repetitions = repetitions.withRemoved(iOneBased);
			} else {
				repetitions = repetitions.withReplacedAt(iOneBased, subGroup);
			}
		}

		return repetitions;
	}

	/**
	 * Recursively removes all
	 * <ul>
	 *     <li>empty fields: fields that have a value of {@code null} or and empty string ({@code ""})</li>
	 * </ul>
	 *
	 * @param document
	 * 		the input document
	 * @return a new document with all the empty fields removed
	 */
	public static DocumentV2 removeEmptyFields(@NonNull DocumentV2 document) {
		return (DocumentV2) removeEmptyFields((GroupInstanceV2) document);
	}

	/**
	 * Recursively removes all
	 * <ul>
	 *     <li>empty fields: fields that have a value of {@code null} or and empty string ({@code ""})</li>
	 * </ul>
	 *
	 * @param group
	 * 		the input group
	 * @return a new group with all the fields removed
	 */
	public static GroupInstanceV2 removeEmptyFields(@NonNull GroupInstanceV2 group) {
		for (var fieldEntry : group.directFields()) {
			// null and "" are both handled the same
			if (fieldEntry.getValue().value() == null || (fieldEntry.getValue().value() instanceof String fv && fv.isEmpty())) {
				group = group.withDirectFieldRemoved(fieldEntry.getKey());
			}
		}

		for (var subGroupEntry : group.directSubgroups()) {
			var repetitions = removeEmptyFields(subGroupEntry.getValue());
			group = group.withDirectSubgroup(subGroupEntry.getKey(), repetitions);
		}

		return group;
	}

	/**
	 * Recursively removes all
	 * <ul>
	 *     <li>empty fields: fields that have a value of {@code null} or and empty string ({@code ""})</li>
	 * </ul>
	 *
	 * @param repetitions
	 * 		the input repetitions
	 * @return the new repetitions with all the emptied groups
	 */
	public static RepetitionsV2 removeEmptyFields(@NonNull RepetitionsV2 repetitions) {
		for (int iOneBased = repetitions.size(); iOneBased > 0; iOneBased--) {
			var subGroup = repetitions.get(iOneBased);
			subGroup = removeEmptyFields(subGroup);
			repetitions = repetitions.withReplacedAt(iOneBased, subGroup);
		}

		return repetitions;
	}

	/**
	 * similar to
	 * {@link com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService#get(String, int[])},
	 * but returns only <em>group</em> instances.
	 * <p>
	 * Note: if no wildcard repetitions are involved, simply use {@link GroupInstanceV2#group} on the documentOrGroup.
	 *
	 * @param pointer
	 * 		path and repetition indices of the requested group instance(s). (If wildcard repetitions are present, then the result may
	 * 		contain multiple group instances)
	 * @return zero or more pairs of {@link DocumentPointer} and {@link GroupInstanceV2}
	 */
	public static Collection<Map.Entry<DocumentPointer, GroupInstanceV2>> getGroupInstances(
			@NonNull GroupInstanceV2 documentOrGroup,
			@NonNull DocumentMultiPointer pointer) {
		Map<DocumentPointer, GroupInstanceV2> groupInstances = new HashMap<>();

		IDocumentV2Visitor visitor = new IDocumentV2Visitor() {
			@Override public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
				IDocumentV2Visitor.super.visitGroup(pointerRelativeToBase, group);

				if (pointer.containsDescendentOf(pointerRelativeToBase)) {
					if (pointer.contains(pointerRelativeToBase)) {
						groupInstances.put(pointerRelativeToBase, group);

						// group found, subgroups are no longer relevant
						return DescendType.SKIP_CHILDREN;
					} else {
						String nextName = pointer.getPartAtLevel(pointerRelativeToBase.size()).name();
						return DescendType.visitSingleSubgroup(nextName);
					}
				} else {
					// we don't have to check further
					return DescendType.SKIP_CHILDREN;
				}
			}

			@Override public RepetitionDescendType visitRepetitions(DocumentPointer pointerRelativeToBase, RepetitionsV2 rep) {
				IDocumentV2Visitor.super.visitRepetitions(pointerRelativeToBase, rep);

				int groupIndex = pointer.getPartAtLevel(pointerRelativeToBase.size() - 1).repetitionIndex();

				if (groupIndex == DocumentMultiPointer.WILDCARD) {
					return RepetitionDescendType.VISIT_CHILDREN;
				} else {
					return RepetitionDescendType.visitSingleOneBasedRepetition(groupIndex);
				}
			}
		};

		documentOrGroup.traverse(visitor);

		return groupInstances.entrySet();
	}

	/**
	 * see {@link #getGroupInstances(GroupInstanceV2, DocumentMultiPointer)}
	 */
	public static Collection<Map.Entry<DocumentPointer, GroupInstanceV2>> getGroupInstances(@NonNull GroupInstanceV2 group,
			@NonNull String path) {
		return getGroupInstances(group, DocumentMultiPointer.of(path));
	}

	/**
	 * similar to
	 * {@link com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService#get(String, int[])},
	 * but returns only field instances that are in the given path.
	 * <p>
	 * Note: if no wildcard repetitions are involved, simply use {@link GroupInstanceV2#field} (or
	 * {@link GroupInstanceV2#fieldValue}) on the documentOrGroup.
	 *
	 * @param pointer
	 * 		path and repetition indices of the requested field instance(s). (If wildcard repetitions are present, then the result may
	 * 		contain multiple field instances)
	 * @return zero or more pairs of {@link DocumentPointer} and {@link FieldInstanceV2}
	 */
	public static Collection<Map.Entry<DocumentPointer, FieldInstanceV2>> getFieldInstances(
			@NonNull GroupInstanceV2 documentOrGroup,
			@NonNull DocumentMultiPointer pointer) {

		final Map<DocumentPointer, FieldInstanceV2> fieldInstances = new HashMap<>();

		IDocumentV2Visitor visitor = new IDocumentV2Visitor() {
			@Override public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
				IDocumentV2Visitor.super.visitGroup(pointerRelativeToBase, group);

				if (pointer.containsDescendentOf(pointerRelativeToBase)) {
					if (pointerRelativeToBase.size() >= pointer.size()) {
						// no need to descend further, we are already at a sibling group of the desired field (when can
						// this even happen? maybe if there actually isn't a field at the desired path, but a group?)
						assert false : "this should not happen; if we got here, we descended into a group that is at " +
								"the path of the desired field, which cannot contain the desired field, so we descended " +
								"too far";
						return DescendType.SKIP_CHILDREN;
					}
					String nextName = pointer.getPartAtLevel(pointerRelativeToBase.size()).name();
					if (pointerRelativeToBase.size() == pointer.size() - 1) {
						assert pointer.parent().contains(pointerRelativeToBase);
						FieldInstanceV2 fieldIfPresent = group.directField(nextName);
						if (fieldIfPresent != null) {
							fieldInstances.put(pointerRelativeToBase.withAppended(nextName, 1), fieldIfPresent);
						}
						return DescendType.SKIP_CHILDREN;
					} else {
						return DescendType.visitSingleSubgroup(nextName);
					}
				} else {
					// we don't have to check further
					return DescendType.SKIP_CHILDREN;
				}
			}

			@Override public RepetitionDescendType visitRepetitions(DocumentPointer pointerRelativeToBase, RepetitionsV2 rep) {
				IDocumentV2Visitor.super.visitRepetitions(pointerRelativeToBase, rep);

				int groupIndex = pointer.getPartAtLevel(pointerRelativeToBase.size() - 1).repetitionIndex();

				if (groupIndex == DocumentMultiPointer.WILDCARD) {
					return RepetitionDescendType.VISIT_CHILDREN;
				} else {
					return RepetitionDescendType.visitSingleOneBasedRepetition(groupIndex);
				}
			}
		};

		documentOrGroup.traverse(visitor);

		return fieldInstances.entrySet();
	}

	/**
	 * see {@link #getFieldInstances(GroupInstanceV2, DocumentMultiPointer)}
	 */
	public static Collection<Map.Entry<DocumentPointer, FieldInstanceV2>> getFieldInstances(@NonNull GroupInstanceV2 group,
			@NonNull String path) {
		return getFieldInstances(group, DocumentMultiPointer.of(path));
	}

	/**
	 * Computes the changes between the given oldDocumentOrGroup and newDocumentOrGroup.
	 * <p>
	 * Compares the {@link FieldInstanceV2}s and {@link GroupInstanceV2} in the {@link DocumentV2} of {@code oldDocumentOrGroup}
	 * with the ones in {@code newDocumentOrGroup}.
	 * <p>
	 * A change can either be
	 * <ul>
	 *     <li>field/group added ({@link Change#isAdd()}): exists in {@code newDocumentOrGroup} but not in {@code oldDocumentOrGroup}</li>
	 *     <li>field/group deleted ({@link Change#isDelete()}): exists in {@code oldDocumentOrGroup} but not in {@code newDocumentOrGroup}</li>
	 *     <li>field updated ({@link Change#isUpdate()}): the field exists in both documents, but it's value has changed</li>
	 * </ul>
	 *
	 * <b>Note:</b> A {@link FieldInstanceV2} exists in a {@link DocumentV2}-instance only if its
	 * value is not {@code null} nor empty. This can be adapted with the provided {@link CompareConfig}.
	 *
	 * @param config
	 * 		Provide a config for the comparison, the minimal configuration can be created by using
	 *        {@code CompareConfig.builder().build()}.
	 * @return the needed information about the changed entity instances. If there were no changes,
	 *        {@link DocumentChanges#noChanges()} will return {@code true}.
	 */
	public static DocumentChanges compare(@NonNull GroupInstanceV2 oldDocumentOrGroup,
			@NonNull GroupInstanceV2 newDocumentOrGroup, @NonNull CompareConfig config) {
		GroupInstanceV2 oldDocumentOrGroupAdapted = oldDocumentOrGroup;
		GroupInstanceV2 newDocumentOrGroupAdapted = newDocumentOrGroup;

		if (config.isEmptyAndNullAsAbsent()) {
			oldDocumentOrGroupAdapted = removeEmptyFields(oldDocumentOrGroupAdapted);
			newDocumentOrGroupAdapted = removeEmptyFields(newDocumentOrGroupAdapted);
		}

		final GroupInstanceV2 finalOldDocumentOrGroup = oldDocumentOrGroupAdapted;
		final GroupInstanceV2 finalNewDocumentOrGroup = newDocumentOrGroupAdapted;

		DocumentChangesImpl changes = DocumentChangesImpl.empty();

		// look from old, to find deleted and updated elements
		IDocumentV2Visitor deletedAndUpdatedVisitor = new IDocumentV2Visitor() {
			@Override public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 oldGroup) {
				GroupInstanceV2 newGroup = finalNewDocumentOrGroup.group(pointerRelativeToBase);
				if (newGroup == null) {
					changes.groupChange(Change.groupDeleted(pointerRelativeToBase, oldGroup));
					return DescendType.SKIP_CHILDREN;
				}
				return IDocumentV2Visitor.super.visitGroup(pointerRelativeToBase, oldGroup);
			}

			@Override public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 oldField) {
				IDocumentV2Visitor.super.visitField(pointerRelativeToBase, oldField);

				FieldInstanceV2 newField = finalNewDocumentOrGroup.field(pointerRelativeToBase);

				if (newField == null) { // cases of empty or null _value_ have already been normalized by removeEmptyFieldsAndGroups
					changes.fieldChange(Change.fieldDeleted(pointerRelativeToBase, oldField));
				} else if (!fieldValuesEqual(oldField, newField, config.isIgnoreScaleOfNumbers())) {
					// A possible alternative impl for isIgnoreScaleOfNumbers would be to normalize field values by stripping
					// trailing zeros, similar to the impl for isEmptyAndNullAsAbsent. But currently the chosen solution
					// seems simpler, in particular because in contrast to isEmptyAndNullAsAbsent, no special case handling
					// for _absent_ fields is needed.
					// Note: handling the config here rather than as a preprocessing normalization in general has the downside
					// that expandedFieldChanges won't automatically behave correctly. But for isIgnoreScaleOfNumbers this is not
					// an issue, because group changes (which are the ones that get expanded) can only be deletions or additions,
					// so the case of a _changed_ field value (that could be equal in value but differ in scale) cannot occur.

					changes.fieldChange(Change.fieldUpdated(pointerRelativeToBase, oldField, newField));
				}
			}
		};

		// look the other direction to find new elements
		IDocumentV2Visitor addedVisitor = new IDocumentV2Visitor() {
			@Override public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 newGroup) {
				GroupInstanceV2 oldGroup = finalOldDocumentOrGroup.group(pointerRelativeToBase);
				if (oldGroup == null) {
					changes.groupChange(Change.groupAdded(pointerRelativeToBase, newGroup));
					return DescendType.SKIP_CHILDREN;
				}
				return IDocumentV2Visitor.super.visitGroup(pointerRelativeToBase, newGroup);
			}

			@Override public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 newField) {
				IDocumentV2Visitor.super.visitField(pointerRelativeToBase, newField);

				FieldInstanceV2 oldField = finalOldDocumentOrGroup.field(pointerRelativeToBase);

				if (oldField == null) {
					changes.fieldChange(Change.fieldAdded(pointerRelativeToBase, newField));
				}
				// if the oldField is present, then we already compared the value in deletedAndUpdatedVisitor#visitField
			}
		};

		oldDocumentOrGroupAdapted.traverse(deletedAndUpdatedVisitor);
		newDocumentOrGroupAdapted.traverse(addedVisitor);

		return changes;
	}

	/**
	 * Returns true if the values of both fields are to be considered equal.
	 * Assumes that both fields are present (not null).
	 */
	private static boolean fieldValuesEqual(
			@NonNull FieldInstanceV2 oldField,
			@NonNull FieldInstanceV2 newField,
			boolean ignoreScaleOfNumbers
	) {
		if (ignoreScaleOfNumbers
				&& oldField.value() instanceof BigDecimal oldDecimal
				&& newField.value() instanceof BigDecimal newDecimal
		) {
			return oldDecimal.compareTo(newDecimal) == 0; // compareTo ignores the scale, in contrast to equals
		} else {
			return Objects.equals(oldField.value(), newField.value());
		}
	}

	@Builder
	@Getter
	public static class CompareConfig {
		/**
		 * If {@code true} (default), field instances with an empty value ({@code ""}) or a {@code null} field are internally
		 * removed from the
		 * documents before being compared. See {@link #removeEmptyFieldsAndGroups(DocumentV2)}.
		 * <p>
		 * {@code true} (default) corresponds to the behavior for IDocument (V1) compare.
		 */
		@Builder.Default
		private boolean emptyAndNullAsAbsent = true;
		/**
		 * Consider two numbers equal if their value is equal, even when their scale (number of decimal places) is unequal.
		 * e.g. {@code 4.2} and {@code 4.20} are to be treated as equal.
		 * <p>
		 * This differs from the more strict definition of {@link java.math.BigDecimal#equals} (numbers in A12 documents are
		 * {@code BigDecimal} in java).
		 * See {@url <a href="https://stackoverflow.com/a/66180493">https://stackoverflow.com/a/66180493</a>} for why it is not
		 * generally safe to ignore the scale.
		 * One particular reason why one may wish to ignore the scale is that A12 frontend code represents numbers as floating
		 * point numbers (JavaScript type {@code number}) and therefore doesn't preserve their scale during
		 * backend-frontend-backend roundtrips.
		 */
		@Builder.Default
		private boolean ignoreScaleOfNumbers = false;
	}
}
