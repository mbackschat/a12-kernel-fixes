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

package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal;

import java.util.Objects;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeConversionResult;

import lombok.NonNull;

public class CustomFieldTypeConversionResultImpl implements ICustomFieldTypeConversionResult {

	private String value;
	private String errorMessage;

	public CustomFieldTypeConversionResultImpl(@NonNull String value, String errorMessage) {
		this.value = value;
		this.errorMessage = errorMessage;
	}

	@NonNull @Override
	public String getConvertedValue() {
		return value;
	}

	@NonNull @Override
	public Optional<String> getErrorMessage() {
		return Optional.ofNullable(errorMessage);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (!(obj instanceof CustomFieldTypeConversionResultImpl)) {
			return false;
		}
		CustomFieldTypeConversionResultImpl other = (CustomFieldTypeConversionResultImpl) obj;
		return Objects.equals(errorMessage, other.errorMessage) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "[Value: " + value + ", " +
				(errorMessage != null ? "Error: " + errorMessage : "No error") + "]";
	}
}
