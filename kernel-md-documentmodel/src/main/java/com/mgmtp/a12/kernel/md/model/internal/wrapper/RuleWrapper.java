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

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelObjectReference;
import com.mgmtp.a12.kernel.md.model.api.IEntity;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class RuleWrapper extends ElementWrapper implements IRule {

	@NoNeedToAdaptSerialVersionUID @Serial
	private static final long serialVersionUID = 1L;

	private final @NonNull Rule rule;

	public RuleWrapper(@NonNull Rule rule) {
		super(rule);
		this.rule = rule;
	}

	@Override
	public @NonNull IDocumentModelObjectReference<IEntity> getErrorEntity() {
		return DocumentModelObjectReferenceWrapper.wrapEntity(rule.getErrorEntity());
	}

	@Override
	public @NonNull String getErrorCode() {
		return rule.getErrorCode();
	}

	@Override
	public @NonNull String getErrorCondition() {
		return rule.getErrorCondition();
	}

	@Override
	public @NonNull Severity getSeverity() {
		return rule.getSeverity();
	}

	@Override
	public @NonNull ILocalizedTextMap getErrorMessage() {
		return rule.getErrorMessage();
	}

	@Override
	public @NonNull Map<String, String> getGuiPatterns() {
		return rule.getGuiPatterns();
	}

	@RequiredArgsConstructor
	@EqualsAndHashCode
	public static class FilterForPossiblyCausingEntitiesWrapper implements IFilterForPossiblyCausingEntities {

		@NoNeedToAdaptSerialVersionUID @Serial
		private static final long serialVersionUID = 1L;

		private final @NonNull Rule.FilterForPossiblyCausingEntities filterForPossiblyCausingEntities;

		@Override
		public @NonNull List<IDocumentModelObjectReference<IEntity>> getEntitiesEnabledAsPossibleCauseIfFilled() {
			return filterForPossiblyCausingEntities.getEntitiesEnabledAsPossibleCauseIfFilled().stream()
					.map(DocumentModelObjectReferenceWrapper::wrapEntity)
					.collect(Collectors.toList());
		}

		@Override
		public @NonNull List<IDocumentModelObjectReference<IEntity>> getEntitiesEnabledAsPossibleCauseIfNotFilled() {
			return filterForPossiblyCausingEntities.getEntitiesEnabledAsPossibleCauseIfNotFilled().stream()
					.map(DocumentModelObjectReferenceWrapper::wrapEntity)
					.collect(Collectors.toList());
		}
	}

	@Override
	public @NonNull Optional<IFilterForPossiblyCausingEntities> getFilterForPossiblyCausingEntities() {
		return rule.getFilterForPossiblyCausingEntities().map(FilterForPossiblyCausingEntitiesWrapper::new);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + rule.hashCode();
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
		RuleWrapper other = (RuleWrapper) obj;
		if (!rule.equals(other.rule)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return rule.toString();
	}

}
