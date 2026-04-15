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

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;

import lombok.NonNull;

public class CustomFieldTypeCheckErrorImpl implements ICustomFieldTypeCheckError {

	private final String errorMessage;
	private final String errorKey;

	public CustomFieldTypeCheckErrorImpl(String errorMsg, String errorKey) {
		this.errorMessage = errorMsg;
		this.errorKey = errorKey;
	}

	@NonNull @Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@NonNull @Override
	public String getErrorKey() {
		return errorKey;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CustomFieldTypeCheckErrorImpl)) {
			return false;
		}
		CustomFieldTypeCheckErrorImpl other = (CustomFieldTypeCheckErrorImpl) obj;
		return Objects.equals(errorMessage, other.errorMessage) && Objects.equals(errorKey, other.errorKey);
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((errorKey == null) ? 0 : errorKey.hashCode());
		return result;
	}

	public String toString() {
		if (errorMessage == null) {
			return "No error";
		}
		return "Error: " + errorMessage;
	}
}
