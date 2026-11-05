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
package com.mgmtp.a12.kernel.md.rt.internal.service;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * Default implementation of {@link IIdentifier}.
 */
public class Identifier implements IIdentifier {

	private final String fullName;
	private final long[] indices;

	public Identifier(final String fullName, final Long[] indices) {
		Validate.notBlank(fullName);
		Objects.requireNonNull(indices);

		this.fullName = fullName;
		this.indices = ArrayUtils.toPrimitive(indices);
	}

	public Identifier(final String fullName, final long[] indices) {
		Validate.notBlank(fullName);
		Objects.requireNonNull(indices);

		this.fullName = fullName;
		this.indices = indices;
	}

	@Override
	public String getName() {
		return fullName;
	}

	@Override
	public long[] getIndices() {
		return indices;
	}

	@Override
	public long getUsb() {
		return USB_NOT_SPECIFIED;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
		result = prime * result + Arrays.hashCode(indices);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Identifier)) {
			return false;
		}
		Identifier other = (Identifier) obj;
		if (fullName == null) {
			if (other.fullName != null) {
				return false;
			}
		} else if (!fullName.equals(other.fullName)) {
			return false;
		}
		if (!Arrays.equals(indices, other.indices)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final String[] pathElements = fullName.split("/");
		int offset = 0;
		for (int i = 0; i < pathElements.length; i++) {
			final String pathElement = pathElements[i];
			if (pathElement.isEmpty()) {
				offset++;
				continue;
			}
			sb.append("/").append(pathElement).append("[").append(indices[i - offset]).append("]");
		}
		return sb.toString();
	}
}
