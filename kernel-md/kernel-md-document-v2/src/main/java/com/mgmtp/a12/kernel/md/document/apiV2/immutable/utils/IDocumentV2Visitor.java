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

package com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils;

import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils.assertLastRepIs1;
import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils.assertLastRepIsWildcard;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;

import lombok.NonNull;

public interface IDocumentV2Visitor {
	/**
	 * Describes how to traverse further in the child elements.
	 */
	class DescendType {
		private enum TypeInternal {SKIP, VISIT, VISIT_SINGLE_SUBGROUP}

		private final TypeInternal type;
		private final String singleSubgroupNameToVisitIfTypeIsSingle;

		private DescendType(final TypeInternal type, final String singleSubgroupNameToVisitIfTypeIsSingle) {
			this.type = type;
			this.singleSubgroupNameToVisitIfTypeIsSingle = singleSubgroupNameToVisitIfTypeIsSingle;
		}

		public String _internal_singleSubgroupNameToVisitIfTypeIsSingle() {
			return singleSubgroupNameToVisitIfTypeIsSingle;
		}

		/**
		 * Visit all the contained children
		 */
		public static DescendType VISIT_CHILDREN = new DescendType(TypeInternal.VISIT, null);
		/**
		 * Don't visit any children.
		 */
		public static DescendType SKIP_CHILDREN = new DescendType(TypeInternal.SKIP, null);

		/**
		 * Visit only the child subgroup provided by the parameter.
		 * <p>
		 * If the provided name is not an actually present subgroup then this behaves like {@link #SKIP_CHILDREN}.
		 *
		 * @param singleSubgroupName
		 * 		the subgroup to visit
		 * @return the constructed descend type
		 */
		public static DescendType visitSingleSubgroup(@NonNull String singleSubgroupName) {
			return new DescendType(TypeInternal.VISIT_SINGLE_SUBGROUP, singleSubgroupName);
		}

		public boolean isVisitSingleSubgroup() {
			return type == TypeInternal.VISIT_SINGLE_SUBGROUP;
		}

		@Override public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof DescendType that))
				return false;
			return type == that.type && Objects.equals(
					singleSubgroupNameToVisitIfTypeIsSingle, that.singleSubgroupNameToVisitIfTypeIsSingle);
		}

		@Override public int hashCode() {
			return Objects.hash(type, singleSubgroupNameToVisitIfTypeIsSingle);
		}
	}

	/**
	 * Describes what repetitions to traverse.
	 */
	class RepetitionDescendType {
		private enum TypeInternal {SKIP, VISIT, VISIT_SINGLE}

		private final TypeInternal type;
		private final int repetitionOneBasedIndex;

		private RepetitionDescendType(final TypeInternal type, final int repetitionOneBasedIndex) {
			this.type = type;
			this.repetitionOneBasedIndex = repetitionOneBasedIndex;
		}

		public int _internal_repetitionOneBasedIndex() {
			return repetitionOneBasedIndex;
		}

		/**
		 * Visit all the repetitions
		 */
		public static RepetitionDescendType VISIT_CHILDREN = new RepetitionDescendType(TypeInternal.VISIT, 0);
		/**
		 * Visit none of the repetitions
		 */
		public static RepetitionDescendType SKIP_CHILDREN = new RepetitionDescendType(TypeInternal.SKIP, 0);

		/**
		 * Visit only the single repetitions of the provided index
		 * <p>
		 * If the repetition is not present (bigger) than the actual repetition of the group, then this behaves like
		 * {@link #SKIP_CHILDREN}.
		 *
		 * @param singleOneBasedRepetitionIndex
		 * 		the index of the single repetition to visit
		 * @return the constructed descend type
		 */
		public static RepetitionDescendType visitSingleOneBasedRepetition(int singleOneBasedRepetitionIndex) {
			Validate.isTrue(singleOneBasedRepetitionIndex >= 1);
			return new RepetitionDescendType(TypeInternal.VISIT_SINGLE, singleOneBasedRepetitionIndex);
		}

		public boolean isVisitSingle() {
			return this.type == TypeInternal.VISIT_SINGLE;
		}

		@Override public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof RepetitionDescendType that))
				return false;
			return repetitionOneBasedIndex == that.repetitionOneBasedIndex && type == that.type;
		}

		@Override public int hashCode() {
			return Objects.hash(type, repetitionOneBasedIndex);
		}
	}

	default IDocumentV2Visitor.DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
		return IDocumentV2Visitor.DescendType.VISIT_CHILDREN;
	}

	default void endVisitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
	}

	/**
	 * @param pointerRelativeToBase
	 * 		the last repetition of this pointer must be 0 (for wildcard, because the repetitions instance
	 * 		represents all repetitions)
	 */
	default IDocumentV2Visitor.RepetitionDescendType visitRepetitions(DocumentPointer pointerRelativeToBase, RepetitionsV2 rep) {
		assertLastRepIsWildcard(pointerRelativeToBase);
		return IDocumentV2Visitor.RepetitionDescendType.VISIT_CHILDREN;
	}

	/**
	 * see {@link #visitRepetitions(DocumentPointer, RepetitionsV2)}
	 */
	default void endVisitRepetitions(DocumentPointer pointerRelativeToBase, RepetitionsV2 rep) {
		assertLastRepIsWildcard(pointerRelativeToBase);
	}

	/**
	 * @param pointerRelativeToBase
	 * 		the last repetition index of this pointer must always be 1, because fields are not repeatable
	 */
	default void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 field) {
		assertLastRepIs1(pointerRelativeToBase);
	}
}
