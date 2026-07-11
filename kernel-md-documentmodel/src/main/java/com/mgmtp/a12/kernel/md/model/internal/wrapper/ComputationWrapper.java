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

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation.ComputationAlternative;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelObjectReference;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.NonNull;

public class ComputationWrapper extends ElementWrapper implements IComputation {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	public static class ComputationAlternativeWrapper implements IComputationAlternative {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		private final ComputationAlternative computationAlternative;

		public ComputationAlternativeWrapper(ComputationAlternative computationAlternative) {
			this.computationAlternative = computationAlternative;
		}

		@Override
		public Optional<String> getPrecondition() {
			return computationAlternative.getPrecondition();
		}

		@Override
		public String getOperation() {
			return computationAlternative.getOperation();
		}

		@Override
		public Optional<String> getToleranceRangeOp() {
			return computationAlternative.getToleranceRangeOp();
		}
	}

	private final Computation computation;

	public ComputationWrapper(@NonNull Computation computation) {
		super(computation);
		this.computation = computation;
	}

	@Override
	public @NonNull IDocumentModelObjectReference<IField> getComputedField() {
		com.mgmtp.a12.kernel.md.model.a12internal.IDocumentModelObjectReference<Field> computedField = computation.getComputedField();
		return new DocumentModelObjectReferenceWrapper<>(
				computedField.getRelativePath(),
				computedField.getDocumentModelObject().map(FieldWrapper::new).orElse(null)
		);
	}

	@Override
	public @NonNull List<String> getErrorCodesToSuppress() {
		return computation.getErrorCodesToSuppress();
	}

	@Override
	public @NonNull List<IComputationAlternative> getComputationAlternatives() {
		return computation.getComputationAlternatives().stream()
				.map(ComputationAlternativeWrapper::new)
				.collect(Collectors.toList());
	}

	public Optional<String> getCommonPrecondition() {
		return computation.getCommonPrecondition();
	}

	@Override
	public @NonNull ILocalizedTextMap getErrorMessage() {
		return computation.getErrorMessage();
	}

	@Override
	public Optional<String> getRoundingMode() {
		return computation.getRoundingMode();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((computation == null) ? 0 : computation.hashCode());
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
		ComputationWrapper other = (ComputationWrapper) obj;
		if (computation == null) {
			if (other.computation != null) {
				return false;
			}
		} else if (!computation.equals(other.computation)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return computation.toString();
	}

}
