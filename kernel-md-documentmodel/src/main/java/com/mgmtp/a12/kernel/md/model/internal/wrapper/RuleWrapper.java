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

import java.util.Map;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelObjectReference;
import com.mgmtp.a12.kernel.md.model.api.IEntity;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.Builder;
import lombok.NonNull;

public class RuleWrapper extends ElementWrapper implements IRule {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	private Rule rule;

	@Builder
	public RuleWrapper(@NonNull Rule rule) {
		super(rule);
		this.rule = rule;
	}

	@Override
	public @NonNull IDocumentModelObjectReference<IEntity> getErrorEntity() {
		com.mgmtp.a12.kernel.md.model.a12internal.IDocumentModelObjectReference<com.mgmtp.a12.kernel.md.model.a12internal.IEntity> errorEntity = rule.getErrorEntity();
		return new DocumentModelObjectReferenceWrapper<>(
				errorEntity.getRelativePath(),
				errorEntity.getDocumentModelObject().map(dmo -> {
					if (dmo instanceof Group group) {
						return new GroupWrapper(group);
					} else if (dmo instanceof Field field) {
						return new FieldWrapper(field);
					} else {
						return null;
					}
				}).orElse(null)
		);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
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
		if (rule == null) {
			if (other.rule != null) {
				return false;
			}
		} else if (!rule.equals(other.rule)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return rule.toString();
	}

}
