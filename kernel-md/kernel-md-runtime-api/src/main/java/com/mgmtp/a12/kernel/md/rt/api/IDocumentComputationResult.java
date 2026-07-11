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
package com.mgmtp.a12.kernel.md.rt.api;

import java.util.Collection;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * The result of a document computation (see {@link IDocumentRtService#compute(DocumentV2, DocumentProcessingConfig)}).
 */
@OnlyForUsage
public interface IDocumentComputationResult {

	/**
	 * @return whether or not errors were encountered during computation processing.
	 */
	boolean noErrorOccurred();

	/**
	 * @return the {@link IComputedFieldInstance}s without formal errors (i.e. {@link IComputedFieldInstance#getErrorMessage()} is
	 * 		empty). Does not include cleared field instances (i.e. {@link IComputedFieldInstance#getValueV2()} is {@code ""}).
	 * 		Is never {@code null}.
	 */
	@NonNull Collection<IComputedFieldInstance> getComputedFieldInstancesWithoutErrors();

	/**
	 * @return the {@link IComputedFieldInstance}s without formal errors whose value differs from the {@link DocumentV2} used for the
	 * 		computation. Subset of {@link #getComputedFieldInstancesWithoutErrors()}. Is never {@code null}.
	 */
	@NonNull Collection<IComputedFieldInstance> getComputedFieldInstancesWithChanges();

	/**
	 * @return the {@link IComputedFieldInstance}s with formal errors (i.e. {@link IComputedFieldInstance#getErrorMessage()} is
	 * 		present). Is never {@code null}.
	 */
	@NonNull Collection<IComputedFieldInstance> getComputedFieldInstancesWithErrors();

	/**
	 * @return the {@link IComputedFieldInstance}s for instances of computed fields which were filled in the {@link DocumentV2}
	 * 		used for computation, but:
	 * 		<ul>
	 * 		    <li>were explicitly cleared (e.g. in a computation which set the value of the field instance to {@code ""}), or</li>
	 * 		    <li>were not computed (e.g. because the preconditions did not apply, due to a cascading error, or because there
	 * 		    were formal errors in operands).</li>
	 * 		</ul>
	 */
	@NonNull Collection<IComputedFieldInstance> getClearedFieldInstances();

	/**
	 * @return the formal errors caused by operands during formal validation. Is never {@code null}.
	 */
	@NonNull Collection<IMessage> getFormalErrorsInOperands();

	/**
	 * Applies the changes to the given {@link DocumentV2} and returns the result
	 * as an updated DocumentV2 (leaving the original unchanged).
	 * Clearing those field instances that are contained in {@link #getComputedFieldInstancesWithErrors} or in
	 * {@link #getClearedFieldInstances}.
	 *
	 * @param document
	 * 		the {@link DocumentV2} to apply the changes to.
	 */
	DocumentV2 applyTo(@NonNull DocumentV2 document);
}
