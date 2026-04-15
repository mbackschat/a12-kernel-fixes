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
import java.util.Locale;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * The result of a document computation (see {@link IDocumentRtService#compute(IDocument, Locale)}).
 */
@OnlyForUsage
public interface IDocumentComputationResult {

	/**
	 * @return whether or not errors were encountered during computation processing.
	 */
	boolean noErrorOccurred();

	/**
	 * @return the {@link IComputedFieldInstance}s without formal errors (i.e. {@link IComputedFieldInstance#getErrorMessage()} is
	 * 		empty). Does not include cleared field instances (i.e. {@link IComputedFieldInstance#getValue()} is {@code ""} or empty).
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
	 * @return the {@link IDocument} based on which this {@link IDocumentComputationResult} was computed. Not a copy (i.e. includes
	 * 		alls modifications made to the document since the computation. Is never {@code null}.
	 *
	 * @deprecated the {@link DocumentV2} has to be provided to the {@link #applyTo(DocumentV2)} method
	 */
	@Deprecated(since = "30.0.0")
	IDocument getDocument();

	/**
	 * Applies the computed field instances, which were computed without an error, to the document
	 * from which the computation was performed (see also {@link #getDocument()}):
	 * <ul>
	 * <li>If the document already contains field instances which correspond to the computed ones,
	 * the corresponding field instances in the document will be updated with the corresponding
	 * computed value.</li>
	 * <li>Otherwise, {@link IFieldInstance}s, which correspond to the computed ones, are internally
	 * created and added to the document (in no specific order).</li>
	 * </ul>
	 * <p>
	 * <b>Important note:</b> This method does change the original document mentioned above.
	 *
	 * @param errorHandler
	 * 		operation which should be performed if an error occurred while computing the
	 * 		corresponding {@link IComputedFieldInstance}. May not be {@code null}.
	 * @deprecated since Kernel version 29.0.0. Use {@link #applyTo(IDocument)} instead.
	 */
	@Deprecated(since = "29.0.0") // can only be removed when A12-17017 is done
	void apply(Consumer<IComputedFieldInstance> errorHandler);

	/**
	 * Modifies the provided {@link IDocument} by applying the changes in {@link #getComputedFieldInstancesWithChanges} and clearing
	 * those field instances that are contained in {@link #getComputedFieldInstancesWithErrors} or in
	 * {@link #getClearedFieldInstances}.
	 *
	 * @param document
	 * 		the {@link IDocument} to apply the changes to. May not be {@code null}.
	 * @deprecated use {@link #applyTo(DocumentV2)} instead
	 */
	@Deprecated(since = "30.0.0")
	void applyTo(IDocument document);

	/**
	 * similar to {@link #applyTo(IDocument)}, but applies the changes to the given {@link DocumentV2} and returns the result
	 * as an updated DocumentV2 (leaving the original unchanged).
	 */
	DocumentV2 applyTo(@NonNull DocumentV2 document);

	/**
	 * Extension of {@link #applyTo(IDocument)} (see description there).
	 * <p>
	 * Use this method only if you used your own implementation of {@link IDocument}/{@link IFieldInstance} while performing the
	 * computation (see also {@link DocumentDeserializationConfig#getDocumentFactory()}). Otherwise, you should use
	 * {@link #applyTo(IDocument)}.
	 *
	 * @param document
	 * 		the {@link IDocument} to apply the changes to. May not be {@code null}.
	 * @param documentFactory
	 * 		the {@link IDocumentFactory} from which {@link IFieldInstance}s should be created if necessary (see
	 *        {@link IComputedFieldInstance#applyTo(IDocument)}). May not be {@code null}.
	 * @deprecated use {@link #applyTo(DocumentV2)} instead
	 */
	@Deprecated(since = "30.0.0")
	void applyTo(IDocument document, IDocumentFactory documentFactory);
}
