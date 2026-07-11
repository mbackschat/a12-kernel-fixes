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

package com.mgmtp.a12.kernel.md.model.a12internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.model.header.Annotation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * Represents a computed value for a {@link Field} of a {@link DocumentModel}. The computation
 * normally depends on values of other {@link Field}s.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Computation extends Element {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	public static final String TR1 = "DiffersWithToleranceRange1";
	public static final String TR2 = "DiffersWithToleranceRange2";
	public static final String TR5 = "DiffersWithToleranceRange5";
	public static final String TR10 = "DiffersWithToleranceRange10";

	/**
	 * Definition of a way to compute a value for a {@link Field}.
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	@ToString
	@Builder
	public static class ComputationAlternative implements Serializable {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		/**
		 * @param precondition
		 * 		the new precondition for this alternative (for more information see
		 *        {@link #getPrecondition()}). May be {@code null} to unset.
		 */
		private String precondition;
		/**
		 * see {@link #getOperation()}
		 */
		private String operation; // not marked as NonNull because serialization allows invalid but null operations too

		/**
		 * @return the operation with which the value is computed if the precondition (see also
		 *        {@link #getPrecondition()}) is fulfilled.
		 *        Normally non-empty, but may be empty (but never null) for incomplete computation alternatives for
		 *        which modeling has not been completed yet.
		 */
		public @NonNull String getOperation() {
			return operation != null ? operation : "";
		}
		/**
		 * @param toleranceRangeOp
		 * 		the new operation with tolerance range (e.g. DiffersWithToleranceRange1)
		 * 		for this alternative. May be {@code null} to unset.
		 */
		private String toleranceRangeOp;

		/**
		 * @return the precondition for this alternative: If this precondition is fulfilled the
		 * 		value
		 * 		will be computed as described in {@link #getOperation()}.
		 */
		public Optional<String> getPrecondition() {
			return Optional.ofNullable(precondition);
		}

		/**
		 * @return the operation with tolerance range (e.g. DiffersWithToleranceRange1)
		 * 		for this alternative.
		 */
		public Optional<String> getToleranceRangeOp() {
			return Optional.ofNullable(toleranceRangeOp);
		}

	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@EqualsAndHashCode
	@ToString
	public static class ComputationPreconditionsAndOperations implements Serializable {
		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		private String commonPrecondition;
		private List<ComputationAlternative> alternatives;

		public Optional<String> getCommonPrecondition() {
			return Optional.ofNullable(commonPrecondition);
		}

		public void setCommonPrecondition(String commonPrecondition) {
			this.commonPrecondition = commonPrecondition;
		}

		public List<ComputationAlternative> getAlternatives() {
			return alternatives;
		}

		public void setAlternatives(List<ComputationAlternative> alternatives) {
			this.alternatives = alternatives;
		}
	}

	public void setComputationPreconditionsAndOperations(ComputationPreconditionsAndOperations fragments) {
		this.computationAlternatives = fragments.alternatives;
		fragments.getCommonPrecondition().ifPresent(cp -> this.commonPrecondition = cp);
	}

	public ComputationPreconditionsAndOperations getComputationPreconditionsAndOperations() {
		return new ComputationPreconditionsAndOperations(commonPrecondition, this.computationAlternatives);
	}

	/**
	 * @return the referenced computed {@link Field}. Is never {@code null}.
	 * @param computedField
	 * 		the new referenced computed {@link Field}. May not be {@code null}.
	 */
	private @NonNull IDocumentModelObjectReference<Field> computedField;
	/**
	 * @return a list of the parser error codes which should be suppressed while parsing the
	 * 		complete computation statement. Never {@code null}. For more information see
	 *        {@link IComputation#getErrorCodesToSuppress()}.
	 * @param errorCodesToSuppress
	 * 		a list with the new error codes which should be suppressed while parsing the
	 * 		complete computation statement. Use a empty list to unset. For more information
	 * 		see {@link IComputation#getErrorCodesToSuppress()}.
	 */
	private @NonNull List<String> errorCodesToSuppress = new ArrayList<>();
	/**
	 * @return a list with the alternatives for this computation (see also
	 *        {@link ComputationAlternative}). Is never {@code null} or empty.
	 * @param computationAlternatives
	 * 		a list with the new alternatives for this computation (see also
	 *        {@link ComputationAlternative}). May not be {@code null} or empty.
	 */
	private @NonNull List<ComputationAlternative> computationAlternatives = new ArrayList<>();

	private String commonPrecondition;

	public Optional<String> getCommonPrecondition() {
		return Optional.ofNullable(commonPrecondition);
	}

	/**
	 * @return the defined message which will be reported in case the corresponding rule for this
	 * 		computation fires. Is never {@code null}. If no error messages are defined, the
	 *        {@link ILocalizedTextMap} is empty.
	 * @param errorMessage
	 * 		the new message which should be reported is the rule fires. May not be
	 *        {@code null}.
	 * 		Use an empty {@link ILocalizedTextMap} to unset.
	 */
	private @NonNull ILocalizedTextMap errorMessage = new LocalizedTextMapBuilder().build();
	/**
	 * @param roundingMode
	 * 		the new rounding mode for this computation. At the moment, only "RoundUp" and "RoundDown" are supported. May be
	 *        {@code null} to unset.
	 */
	private String roundingMode;

	@Builder(builderClassName = "ComputationBuilder")
	private Computation(@NonNull String id, @NonNull String name, @NonNull ILocalizedTextMap internalDescription,
			@NonNull ILocalizedTextMap externalDescription, @NonNull List<Annotation> annotations,
			@NonNull IDocumentModelObjectReference<Field> computedField, @NonNull List<String> errorCodesToSuppress,
			@NonNull List<ComputationAlternative> computationAlternatives, String commonPrecondition,
			@NonNull ILocalizedTextMap errorMessage,
			String roundingMode) {
		super(null, id, name, internalDescription, externalDescription, annotations);
		this.computedField = computedField;
		this.errorCodesToSuppress = errorCodesToSuppress;
		this.computationAlternatives = computationAlternatives;
		this.commonPrecondition = commonPrecondition;
		this.errorMessage = errorMessage;
		this.roundingMode = roundingMode;

	}

	/**
	 * @return the rounding mode for this computation.
	 */
	public Optional<String> getRoundingMode() {
		return Optional.ofNullable(roundingMode);
	}

	public static class ComputationBuilder {
		private ILocalizedTextMap internalDescription = new LocalizedTextMapBuilder().build(); // in order to make ComputationBuilder.internalDescription() optional
		private ILocalizedTextMap externalDescription = new LocalizedTextMapBuilder().build(); // in order to make ComputationBuilder.externalDescription() optional
		private List<Annotation>  annotations = new ArrayList<>(); // in order to make ComputationBuilder.annotations() optional
		private List<String> errorCodesToSuppress = new ArrayList<>(); // in order to make ComputationBuilder.errorCodesToSuppress() optional
		private List<ComputationAlternative> computationAlternatives = new ArrayList<>(); // in order to make ComputationBuilder.computationAlternatives() optional when calling the builder for the first time
		private ILocalizedTextMap errorMessage = new LocalizedTextMapBuilder().build(); // in order to make ComputationBuilder.errorMessage() optional when calling the builder for the first time
	}

}
