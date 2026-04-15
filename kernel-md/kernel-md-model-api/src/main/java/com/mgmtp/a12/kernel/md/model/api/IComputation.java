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
package com.mgmtp.a12.kernel.md.model.api;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Read operations for a computation.
 * <p>
 * Represents a computed value for a {@link IField} of a {@link IDocumentModel}. The computation
 * normally depends on values of other {@link IField}s.
 */
@OnlyForUsage
public interface IComputation extends IElement, Serializable {

	/**
	 * Definition of an alternative for computing a {@link IField} value.
	 */
	interface IComputationAlternative extends Serializable {

		/**
		 * @return the precondition which needs to be fulfilled in order to run the computation in
		 * 		this
		 * 		way (see also {@link #getOperation()}).
		 */
		Optional<String> getPrecondition();

		/**
		 * @return the operation with which the value is computed if the precondition (see also
		 *        {@link #getPrecondition()}) is fulfilled.
		 *        Normally non-empty, but may be empty (but never null) for incomplete computation alternatives for
		 *        which modeling has not been completed yet.
		 */
		String getOperation();

		/**
		 * @return the tolerance range operation used in this computation alternative.
		 */
		Optional<String> getToleranceRangeOp();
	}

	/**
	 * @return the referenced {@link IField} to be computed. Is never {@code null}.
	 */
	@NonNull IDocumentModelObjectReference<IField> getComputedField();

	/**
	 * @return a list of the parser error codes which should be suppressed while parsing the
	 * 		complete computation statement. Never {@code null}.
	 * 		<p>
	 * 		At the moment, the list can contain following error codes:
	 * 		<ul>
	 * 		<li>MVK_INVALID_COMPARE_DEC_PLACES</li>
	 * 		</ul>
	 */
	@NonNull List<String> getErrorCodesToSuppress();

	/**
	 * @return a list of {@link IComputationAlternative}s describing how to calculate the value. Is
	 * 		never {@code null} or empty.
	 */
	@NonNull List<IComputationAlternative> getComputationAlternatives();

	/**
	 * @return the common precondition
	 */
	Optional<String> getCommonPrecondition();

	/**
	 * Only for ExtMP.
	 *
	 * @return the error message. Is never {@code null}. If no error messages are defined, the
	 *        {@link ILocalizedTextMap} is empty.
	 */
	@NonNull ILocalizedTextMap getErrorMessage();

	/**
	 * @return the filter that determines whether or not the {@link IComputation} should be applied
	 * 		for a certain variant.
	 */
	Optional<IVariantFilter> getVariantFilter();

	/**
	 * @return @return the rounding mode which should be used after the computation, defined in the alternatives,
	 * 		is done. It is normally used to match the fractional digits of the computed field. At the moment, only "RoundUp" and
	 * 		"RoundDown" are supported.
	 */
	Optional<String> getRoundingMode();

}
