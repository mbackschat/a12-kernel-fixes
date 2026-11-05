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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.util;

import java.util.Objects;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifierWithSemanticIndex;

public class RtExternalIdentifierWithSemanticIndex extends RtExternalIdentifier implements IIdentifierWithSemanticIndex {

	private final IIdentifier indexField;
	private final String indexValue;
	private final Object indexValueAsBasicType;

	public RtExternalIdentifierWithSemanticIndex(String name, long[] indices, long usb, IIdentifier indexField,
			String indexValue, Object indexValueAsBasicType) {
		super(name, indices, usb);
		this.indexField = indexField;
		this.indexValue = emptyToNull(indexValue);
		this.indexValueAsBasicType = indexValueAsBasicType;
	}

	public static String emptyToNull(String indexValue) {
		return "".equals(indexValue) ? null : indexValue;
	}

	@Override public IIdentifier getIndexField() {
		return indexField;
	}

	@Override public Optional<String> getIndexValue() {
		return Optional.ofNullable(indexValue);
	}

	public Optional<Object> getIndexValueAsBasicType() {
		return Optional.ofNullable(indexValueAsBasicType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hashCode(indexField);
		result = prime * result + Objects.hashCode(indexValue);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IIdentifierWithSemanticIndex)) {
			return false;
		}
		final IIdentifierWithSemanticIndex other = (IIdentifierWithSemanticIndex) obj;
		if (!fieldsEqual(other)) {
			return false;
		}
		if (!Objects.equals(indexField, other.getIndexField())) {
			return false;
		}
		return Objects.equals(getIndexValue(), other.getIndexValue());
	}
}
