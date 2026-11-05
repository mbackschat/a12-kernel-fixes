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
package com.mgmtp.a12.kernel.md.model.internal.wrapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Group.IncludeConfig;
import com.mgmtp.a12.kernel.md.model.a12internal.Group.SortField;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelObjectReference;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.NonNull;

public class GroupWrapper extends ElementWrapper implements IGroup {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	public static class SortFieldWrapper implements ISortField {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		private SortField sortField;

		SortFieldWrapper(@NonNull SortField sortField) {
			this.sortField = sortField;
		}

		@Override
		public @NonNull IDocumentModelObjectReference<IField> getField() {
			return DocumentModelObjectReferenceWrapper.wrapField(sortField.getField());
		}

		@Override
		public @NonNull Order getOrder() {
			return sortField.getOrder();
		}

	}

	public static class IncludeDetailsWrapper implements IIncludeConfig {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		private IncludeConfig includeDetails;

		public IncludeDetailsWrapper(@NonNull Group.IncludeConfig includeDetails) {
			this.includeDetails = includeDetails;
		}

		@Override
		public @NonNull String reference() {
			return includeDetails.getReference();
		}

		@Override public @NonNull IncludeLevel includeLevel() {
			return includeDetails.getIncludeLevel();
		}

		@Override
		public boolean excludeRules() {
			return includeDetails.isExcludeRules();
		}

		@Override
		public boolean excludeComputations() {
			return includeDetails.isExcludeComputations();
		}

	}

	private Group group;

	public GroupWrapper(@NonNull Group group) {
		super(group);
		this.group = group;
	}

	@Override
	public @NonNull ILocalizedTextMap getLabel() {
		return group.getLabel();
	}

	@Override
	public int getRepeatability() {
		return group.getRepeatability();
	}

	@Override
	public Optional<IDocumentModelObjectReference<IField>> getIndexField() {
		return group.getIndexField().map(DocumentModelObjectReferenceWrapper::wrapField);
	}

	@Override
	public Optional<String> getUsageType() {
		return group.getUsageType();
	}

	@Override
	public @NonNull List<ISortField> getSortFields() {
		return group.getSortFields().stream()
				.map(SortFieldWrapper::new)
				.collect(Collectors.toList());
	}

	@Override
	public @NonNull List<IElement> getElements() {
		return ElementUtils.convert(group.getElements());
	}

	@Override
	public Optional<IIncludeConfig> getIncludeConfig() {
		return group.getIncludeConfig().map(IncludeDetailsWrapper::new);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GroupWrapper other = (GroupWrapper) obj;
		if (group == null) {
			if (other.group != null) {
				return false;
			}
		} else if (!group.equals(other.group)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return group.toString();
	}

}
