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

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.util.ValidatedImmutableMap;

import lombok.NonNull;

// NOTE: in case of modifications to this class, consider TestDocV2Performance, it offers functionality to
// easily and locally record performance measurements for some relevant operations, before and after the
// code change, and to visually plot the results for easy comparison via Analyze_Improvised_Perftests.ipynb
public final class ImmutableDocumentV2Impl
		extends AbstractImmutableGroupInstance<ImmutableDocumentV2Impl>
		implements DocumentV2 {
	private final String documentModelId;
	/**
	 * can be null
	 */
	private final String id;
	private final AbstractImmutableGroupInstance<?> wrappedRootOfRootGroups;

	/**
	 * must only be called via builder, to ensure that there is one single code path that ensures consistency checks
	 */
	private ImmutableDocumentV2Impl(String documentModelId, String id, AbstractImmutableGroupInstance<?> rootOfRoots) {
		this.documentModelId = documentModelId;
		this.id = id;
		this.wrappedRootOfRootGroups = rootOfRoots;
	}

	public static ImmutableDocumentV2Impl emptyInternal(String documentModelId) {
		return emptyInternal(documentModelId, null);
	}

	public static ImmutableDocumentV2Impl emptyInternal(String documentModelId, String id) {
		return Builder.empty(documentModelId, id).buildIfChanged();
	}

	/**
	 * @param documentId
	 * 		may be null
	 */
	public static DocumentV2 ofRootOfRootGroups(String documentModelId, String documentId,
			@NonNull GroupInstanceV2 rootOfRootGroups) {
		Validate.isInstanceOf(AbstractImmutableGroupInstance.class, rootOfRootGroups, "no custom implementations supported");
		return Builder.ofRootOfRootGroups(documentModelId, documentId, (AbstractImmutableGroupInstance<?>) rootOfRootGroups)
				.buildIfChanged();
	}

	@Override Builder toLazyCopyBuilder() {
		return new Builder(self());
	}

	static class Builder extends AbstractImmutableGroupInstanceBuilder<ImmutableDocumentV2Impl> {
		private String documentModelId;
		private String id;

		protected Builder(@NonNull ImmutableDocumentV2Impl original) {
			super(original);
			this.documentModelId = original.getDocumentModelId();
			this.id = original.getId().orElse(null);
			// no additional validation necessary, original is already a valid document instance
		}

		protected Builder(
				String documentModelId,
				String id,
				@NonNull ValidatedImmutableMap<String, RepetitionsV2, SubGroupsInvariants> subGroups,
				// note: a document cannot even have fields, but in order to keep the validation of this invariant
				// in the builder, we accept the fields parameter here, and reject non-empty values later.
				@NonNull ValidatedImmutableMap<String, FieldInstanceV2, FieldsInvariants> fields,
				boolean alreadyCheckedForCollidingFieldAndSubgroupNames) {
			super(subGroups, fields, alreadyCheckedForCollidingFieldAndSubgroupNames);
			this.documentModelId = documentModelId;
			this.id = validateDocumentId(id);
			if (this.hasFields()) {
				throw makeRootFieldsNotAllowedException();
			}
		}

		static Builder ofRootOfRootGroups(String documentModelId, String id,
				@NonNull AbstractImmutableGroupInstance<?> rootOfRootGroups) {
			return new Builder(documentModelId, // use the constructor for untrusted parameters, because there are additional
					// constraints in documents vs in fields, e.g. no direct fields are allowed.
					id,
					rootOfRootGroups.groupsInternal(),
					rootOfRootGroups.fieldsInternal(),
					true // rootOfRootGroups was already valid, so there can't be name collisions
			);
		}

		public static Builder empty(String documentModelId, String id) {
			return new Builder(documentModelId, id,
					ValidatedImmutableMap.empty(SubGroupsInvariants.SINGLETON),
					ValidatedImmutableMap.empty(FieldsInvariants.SINGLETON),
					true);
		}

		@Override protected boolean isUnchangedOriginal() {
			return super.isUnchangedOriginal() &&
					Objects.equals(this.documentModelId,
							Optional.ofNullable(this.originalIfExists).map(ImmutableDocumentV2Impl::getDocumentModelId).orElse(null)) &&
					Objects.equals(this.id,
							Optional.ofNullable(this.originalIfExists).flatMap(ImmutableDocumentV2Impl::getId).orElse(null));
		}

		@Override protected ImmutableDocumentV2Impl buildNewInstanceInternal(
				@NonNull ValidatedImmutableMap<String, RepetitionsV2, SubGroupsInvariants> subGroups,
				@NonNull ValidatedImmutableMap<String, FieldInstanceV2, FieldsInvariants> fields) {
			return new ImmutableDocumentV2Impl(documentModelId, id,
					new ImmutableGroupV2Impl.Builder(subGroups, fields, true).buildIfChanged());
		}

		@Override public Builder putField(@NonNull String name, FieldInstanceV2 field) {
			throw makeRootFieldsNotAllowedException();
		}

		public Builder setId(String id) {
			this.id = validateDocumentId(id);
			return this;
		}

		public Builder setDocumentModelId(String documentModelId) {
			this.documentModelId = documentModelId;
			return this;
		}

		private static UnsupportedOperationException makeRootFieldsNotAllowedException() {
			return new UnsupportedOperationException("documents cannot directly contain fields, only groups in documents can");
		}

		private static String validateDocumentId(String id) {
			if (Objects.equals(id, "")) {
				// for compatibility with IDocument#setId:
				throw new IllegalArgumentException("id may be null or a non-empty string, but not the empty string");
			}
			return id;
		}
	}

	@Override protected ValidatedImmutableMap<String, RepetitionsV2, SubGroupsInvariants> groupsInternal() {
		return wrappedRootOfRootGroups.groupsInternal();
	}

	@Override protected ValidatedImmutableMap<String, FieldInstanceV2, FieldsInvariants> fieldsInternal() {
		return wrappedRootOfRootGroups.fieldsInternal();
	}

	@Override protected ImmutableDocumentV2Impl self() {
		return this;
	}

	@Override public Optional<String> getId() {
		return Optional.ofNullable(this.id);
	}

	@Override public DocumentV2 withId(String id) {
		return toLazyCopyBuilder().setId(id).buildIfChanged();
	}

	@Override public String getDocumentModelId() {
		return this.documentModelId;
	}

	@Override public DocumentV2 withDocumentModelId(String documentModelId) {
		return toLazyCopyBuilder().setDocumentModelId(documentModelId).buildIfChanged();
	}

	@Override public GroupInstanceV2 toRootRootGroup() {
		return wrappedRootOfRootGroups;
	}

	@Override public int hashCode() {
		if (cachedHashCode == 0) { // this is probably thread even safe?
			cachedHashCode = Objects.hash(super.hashCode(), this.documentModelId);
		}
		return cachedHashCode;
	}

	@Override public boolean equals(Object obj) {
		return super.equals(obj)
				&& obj instanceof ImmutableDocumentV2Impl other // instanceof check will always succeed after super's class equality
				// check, but this tells the compiler that the cast ist safe.
				&& this.getDocumentModelId().equals(other.getDocumentModelId());
	}

	@Override public String toString() {
		return DocumentV2InternalUtils.docToString(this);
	}
}
