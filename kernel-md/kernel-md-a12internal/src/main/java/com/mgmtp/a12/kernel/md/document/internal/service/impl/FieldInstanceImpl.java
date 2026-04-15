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

package com.mgmtp.a12.kernel.md.document.internal.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.document.api.IEntityInstanceDerivable;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;

/**
 * See javadoc of {@link DocumentImpl}.
 */
public class FieldInstanceImpl implements IFieldInstance, IEntityInstanceDerivable {

	private final EntityInstanceImpl internalEi;
	private Object value;

	public FieldInstanceImpl(String path, int[] repetitions) {
		this.internalEi = new EntityInstanceImpl(path, repetitions);
	}

	public EntityInstanceImpl getInternalEi() {
		return internalEi;
	}

	@Override
	public String getPath() {
		return internalEi.getPath();
	}

	@Override
	public int[] getRepetitions() {
		return internalEi.getRepetitions();
	}

	@Override
	public Optional<Object> getValue() {
		return Optional.ofNullable(value);
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((internalEi == null) ? 0 : internalEi.hashCode());
		result = prime * result + (fieldValueHashCode(this));
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
		FieldInstanceImpl other = (FieldInstanceImpl) obj;
		if (!Objects.equals(internalEi, other.internalEi)) {
			return false;
		}
		return fieldInstanceValuesEqual(this, other);
	}

	private static boolean fieldInstanceValuesEqual(IFieldInstance a, IFieldInstance b) {
		Object valueA = a.getValue().orElse(null);
		Object valueB = b.getValue().orElse(null);
		return fieldValuesEqual(valueA, valueB);
	}

	public static boolean fieldValuesEqual(Object valueA, Object valueB) {
		if (valueA instanceof Date[] dateRangeA && valueB instanceof Date[] dateRangeB) {
			return Arrays.equals(dateRangeA, dateRangeB);
		}
		return Objects.equals(valueA, valueB);
	}

	private static int fieldValueHashCode(IFieldInstance f) {
		Object value = f.getValue().orElse(null);
		if (value instanceof Date[] dateRange) {
			return Arrays.hashCode(dateRange);
		} else {
			return Objects.hashCode(value);
		}
	}

	@Override
	public String toString() {
		return String.format("%s=%s", internalEi.toString(), value);
	}

	@Override public FieldInstanceImpl withPath(String newPath) {
		FieldInstanceImpl newInstance = new FieldInstanceImpl(newPath, getRepetitions());
		newInstance.setValue(this.value); // warning: don't use getValue here, it wraps value in an optional
		return newInstance;
	}

	@Override public FieldInstanceImpl withRepetitions(int[] newRepetitions) {
		FieldInstanceImpl newInstance = new FieldInstanceImpl(getPath(), newRepetitions);
		newInstance.setValue(this.value); // warning: don't use getValue here, it wraps value in an optional
		return newInstance;
	}
}
