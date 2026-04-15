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

import java.util.Locale;
import java.util.Set;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Access to document services (static or dynamic), i.e. validation and computation.
 * <p>
 * For <b>static services</b> the generated code for the model must be accessible for the service
 * at runtime.
 * <p>
 * While executing <b>dynamic services</b>, code for the model is generated according to
 * following conditions:
 * <ul>
 * <li>If the code for the model is not present in cache (see
 * {@link IDocumentDynamicServiceConfig#getCache()}) the code will be generated.</li>
 * <li>If code for the model was found in cache and the model was changed (based on its hash code),
 * code will be generated and the cached code is replaced by the newly generated code.</li>
 * <li>If code for the model was found in cache and the model was not changed, no code will be
 * generated.</li>
 * </ul>
 * To populate the cache with precompiled document models you can use
 * {@link #precompileDocumentModel(IDocumentModel)}. This is implemented in a threadsafe way as long
 * as the given model cache (see {@link IDocumentDynamicServiceConfig#getCache()}) is threadsafe,
 * too.
 * <p>
 * <b>Important remark while using the validation/computation services:</b> Kernel distinguishes
 * between the internal value (the value which is used during the validation/computation) and the
 * corresponding display value (the value which is displayed at a user interface). An
 * {@link DocumentV2}-instance contains always internal values. During the validation/computation, if
 * an error message (either from the formal validation or a rule fires) is thrown and a field value
 * is referenced in the message, the display value for the field will be used. For this purpose, the
 * internal value coming from the document is converted into a display value using certain
 * information called presentation information (see PresentationInformation.java) which is static and set internally, note that
 * this class is not part of the api, but it can be used as reference (see also PresentationInformationConstants.java). This
 * conversion is needed in order to have the same error messages in the client and in the server.
 */
@OnlyForUsage
public interface IDocumentRtService {

	/**
	 * Validates the whole given {@link DocumentV2}.
	 *
	 * @param document
	 * 		the {@link DocumentV2} which should be validated. May not be {@code null}.
	 * @param config
	 * 		the {@link DocumentProcessingConfig} including the locale in which error messages are returned. May not be {@code null}.
	 * @return a {@link IDocumentValidationResult} containing the validation messages.
	 */
	IDocumentValidationResult validateFull(@NonNull DocumentV2 document, @NonNull DocumentProcessingConfig config);

	/**
	 * See {@link #validateFull(DocumentV2, DocumentProcessingConfig)}.
	 *
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use
	 *        {@link #validateFull(DocumentV2, DocumentProcessingConfig)} instead.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	IDocumentValidationResult validateFull(IDocument document, Locale locale);

	/**
	 * See {@link #validateFull(DocumentV2, DocumentProcessingConfig)}.
	 *
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use
	 *        {@link #validateFull(DocumentV2, DocumentProcessingConfig)} instead.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	IDocumentValidationResult validateFull(@NonNull DocumentV2 document, @NonNull Locale locale);

	/**
	 * Validates a part of the given {@link DocumentV2}.
	 * <p>
	 * <b>Notes:</b>
	 * <ul>
	 *     <li>Rule conditions are evaluated using three value logic, which may cause validation rules to not fire if not all fields
	 *     and repetitions referenced by the rule are marked as relevant.</li>
	 *     <li>All possible field instances of global fields are internally added to the relevant entity instances and therefore,
	 *     they are considered during the partial validation.</li>
	 * </ul>
	 *
	 * @param document
	 * 		the {@link DocumentV2} which should be validated. May not be {@code null}.
	 * @param relevantEntities
	 * 		The set of entity instances which are relevant for validation.
	 * 		<p>
	 * 		Allows the specification of repetitions as the wildcard {@link IEntityInstance#ALL_SEMANTIC} (repetition index = 0),
	 * 		which refers to all repetitions of the respective repeatable group.
	 * 		<p>
	 * 		Validation rules are only executed if their error field instance is contained in this set. Of those
	 * 		rules that are evaluated, validation errors will be suppressed if the rule references any repetition of a
	 * 		field or group that is not marked as relevant (except for some cases where kernel is sure that no possible
	 * 		value of that field/group repetition could have prevented the validation error).
	 * 		In particular, this means that if the rule has a wildcard reference to all allowed repetitions of a field
	 * 		(e.g. {@code /RG`*`/f1}), validation errors for that rule will usually be suppressed unless /RG[0]/f1 is marked as
	 * 		relevant.
	 * 		Example: if only two repetitions of RG are filled, but RG's max repeatability is 10, then it is insufficient to
	 * 		mark only /RG[1]/f1 and /RG[2]/f2 as revant, because /RG`*`/f1 also references to the not-filled repetitions of f1.
	 * 		<p>
	 * 		For groups declared as relevant, all descendants (fields and groups) are also assumed relevant (as if these descendants
	 * 		had also been declared, with the repetitions of path elements below the group set to
	 *        {@link IEntityInstance#ALL_SEMANTIC}).
	 * 		<p>
	 * 		Repetitions of entity instances beyond the max repeatability will be ignored.
	 * 		<p>
	 * 		<b>Note:</b>
	 * 		It is encouraged to set the repetitions of all path elements (including non-repeatable ones) to
	 *        {@link IEntityInstance#ALL_SEMANTIC}:
	 * 		If only a subset of repetitions is specified as relevant, validation errors will be suppressed in cases that can
	 * 		be unexpected to the user (see description above).
	 * 		Compared to listing the all instances of a repetition layer separately, this additionally offers performance benefits and
	 * 		does not prevent some language constructs from being evaluated (like `NumberOfFilledFields`, `NumberOfFilledGroups`, and
	 * 		in some constellations semantic index and parallel iteration).
	 * 		<p>
	 * 		If you <em>do</em> specify a subset of repetitions, make sure to correctly understand how partial validation will behave
	 * 		in this scenario (when in doubt, ask the kernel team).
	 * @param config
	 * 		the {@link DocumentProcessingConfig} including the locale in which error messages are returned. May not be {@code null}.
	 * @return a {@link IDocumentValidationResult} containing the validation messages.
	 */
	IDocumentValidationResult validatePart(@NonNull DocumentV2 document,
			@NonNull Set<? extends DocumentMultiPointer> relevantEntities,
			@NonNull DocumentProcessingConfig config);

	/**
	 * See {@link #validatePart(DocumentV2, Set, DocumentProcessingConfig)}.
	 *
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use
	 *        {@link #validatePart(DocumentV2, Set, DocumentProcessingConfig)} instead.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	IDocumentValidationResult validatePart(IDocument document, Set<IEntityInstance> relevantEntityInstances, Locale locale);

	/**
	 * See {@link #validatePart(DocumentV2, Set, DocumentProcessingConfig)}.
	 *
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use
	 *        {@link #validatePart(DocumentV2, Set, DocumentProcessingConfig)} instead.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	IDocumentValidationResult validatePart(@NonNull DocumentV2 document,
			@NonNull Set<? extends DocumentMultiPointer> relevantEntities,
			@NonNull Locale locale);

	/**
	 * Computes all fields which can be computed based on the given {@link IDocument}.
	 * <p>
	 * Before performing any computations, all non-computed fields in the given {@link IDocument} that are operands of any
	 * computation will be formally validated: <ol>
	 * <li>If a field contains an invalid value, a corresponding error will be reported.</li>
	 * <li>If a required field is empty, no error will be reported.</li>
	 * <li>If an index field is empty or not unique, an error will be reported.</li>
	 * The errors reported in this step will be returned in {@link IDocumentComputationResult#getFormalErrorsInOperands()}.
	 * <p>
	 * Regardless of the formal errors found, computations will be performed. Any computations relying directly or indirectly on
	 * invalid fields will be skipped.
	 * <p>
	 * Computations are performed in an order that ensures that all operands of a computation rule are final (i.e. computed
	 * before). If a computation results in a value that is not valid (e.g. a number value outside the valid range), the erroneous
	 * {@link IComputedFieldInstance} will be included in {@link IDocumentComputationResult#getComputedFieldInstancesWithErrors()}
	 * with a corresponding {@link IComputedFieldInstance#getErrorMessage()}. A field instance will only be computed if its
	 * computation does not depend on such an invalid computed field instance directly or indirectly.
	 * <p>
	 * {@link IDocumentComputationResult#getComputedFieldInstancesWithoutErrors()} will contain all field instances that could
	 * be computed without errors.
	 * <p>
	 * {@link IDocumentComputationResult#getClearedFieldInstances()} will contain field instances that were part of the
	 * {@link DocumentV2} used for computation, but should be understood as unfilled after the computation.
	 * <p>
	 * <b>Note:</b> It is recommended to validate the document, e.g. via
	 * {@link IDocumentRtService#validateFull(DocumentV2, DocumentProcessingConfig)},
	 * before executing the computation.
	 *
	 * @param document
	 * 		the {@link DocumentV2} to take operands from. May not be {@code null}.
	 * @param config
	 * 		the {@link DocumentProcessingConfig} including the locale in which error messages are returned. May not be {@code null}.
	 * @return a {@link IDocumentComputationResult} containing all computed field instances (valid or invalid) and errors in operands.
	 */
	IDocumentComputationResult compute(@NonNull DocumentV2 document, @NonNull DocumentProcessingConfig config);

	/**
	 * See {@link #compute(DocumentV2, DocumentProcessingConfig)}.
	 *
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use {@link #compute(DocumentV2, DocumentProcessingConfig)} instead.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	IDocumentComputationResult compute(IDocument document, Locale locale);

	/**
	 * See {@link #compute(DocumentV2, DocumentProcessingConfig)}.
	 *
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use {@link #compute(DocumentV2, DocumentProcessingConfig)} instead.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	IDocumentComputationResult compute(@NonNull DocumentV2 document, @NonNull Locale locale);

	/**
	 * The generated code for the given document model will be created and precompiled.
	 *
	 * @param documentModel
	 * 		the model to be pre-compiled.
	 */
	void precompileDocumentModel(@NonNull IDocumentModel documentModel);

	/**
	 * If you set an own implementation in {@link IDocumentServiceConfig#getModelIdProvider()} while creating {@code this} service,
	 * this method uses that implementation, otherwise the default implementation is used.
	 *
	 * @return the string with which the generated code (generated out of the given document model) is cached, see
	 *        {@link IStaticModelCodeCache#addModelCode(String, IStaticModelCode)} and
	 *        {@link IModelCodeCache#addModelCode(String, IModelCode)}.
	 */
	String createModelCodeIdentifier(@NonNull IDocumentModel documentModel, String variant);
}
