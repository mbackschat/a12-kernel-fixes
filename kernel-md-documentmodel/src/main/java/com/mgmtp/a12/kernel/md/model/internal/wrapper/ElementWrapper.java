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

import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.model.header.Annotation;

import lombok.NonNull;

public class ElementWrapper implements IElement {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	private Element element;

	public ElementWrapper(@NonNull Element element) {
		this.element = element;
	}

	@Override
	public @NonNull String getId() {
		return element.getId();
	}

	@Override
	public @NonNull String getName() {
		return element.getName();
	}

	@Override
	public @NonNull ILocalizedTextMap getInternalDescription() {
		return element.getInternalDescription();
	}

	@Override
	public @NonNull ILocalizedTextMap getExternalDescription() {
		return element.getExternalDescription();
	}

	@Override
	public @NonNull List<Annotation> getAnnotations() {
		return element.getAnnotations();
	}

	@Override
	public void setAnnotations(@NonNull List<Annotation> annotations) {
		element.setAnnotations(annotations);
	}

	@Override
	public IGroup getParent() {
		if (element.getParent() != null) {
			return new GroupWrapper(element.getParent());
		}
		return null;
	}

	public @NonNull Element getElement() {
		return element;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ElementWrapper other = (ElementWrapper) obj;
		if (element == null) {
			if (other.element != null) {
				return false;
			}
		} else if (!element.equals(other.element)) {
			return false;
		}
		return true;
	}

}
