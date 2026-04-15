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

package com.mgmtp.a12.kernel.md.rt.a12internal.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils;
import com.mgmtp.a12.kernel.md.document.internal.service.DocumentFactoryImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.FieldInstanceImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV1V2ConverterImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.utils.DocumentUtils;
import com.mgmtp.a12.kernel.md.rt.api.IComputedFieldInstance;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentComputationResult;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.kernel.md.rt.internal.service.ComputedFieldInstanceImpl;
import com.mgmtp.a12.kernel.md.rt.internal.service.DocumentPointerConverter;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Implementation of the result of a document computation.
 * <p>
 * <em>Note:</em> is considered kernel internal but can also be used by Q12-TDS
 */
@OnlyForUsage
public class DocumentComputationResultImpl implements IDocumentComputationResult {

	/**
	 * @deprecated the document V1 API, including {@link IDocumentFactory}, is deprecated. Use the document V2 API instead.
	 */
	@Deprecated(since = "30.0.0")
	public static final IDocumentFactory DOC_FACTORY = new DocumentFactoryImpl();

	/**
	 * @deprecated only used for {@link #getDocument()} remove when the method is removed
	 */
	@Deprecated(since = "30.2.0")
	private IDocument documentV1;
	private final Collection<IComputedFieldInstance> computedFieldInstancesWithoutErrors = new ArrayList<>();
	private final Collection<IComputedFieldInstance> computedFieldInstancesWithChanges = new ArrayList<>();
	private final Collection<IComputedFieldInstance> computedFieldInstancesWithErrors = new ArrayList<>();
	private final Collection<IComputedFieldInstance> clearedFieldInstances = new ArrayList<>();
	private final Collection<IMessage> formalErrorsInOperands = new ArrayList<>();

	/**
	 * Implementation of the result of a document computation.
	 *
	 * @param documentV1
	 * 		the document
	 * @param computedData
	 * 		the computed data
	 * @param resultMessages
	 * 		the list of error messages
	 * @param metaModel
	 * 		the metamodel
	 * @deprecated use {@link #DocumentComputationResultImpl(DocumentV2, IData, List, IMetaModel)} instead.
	 */
	@Deprecated(since = "30.0.0")
	public DocumentComputationResultImpl(
			@NonNull IDocument documentV1,
			@NonNull IData computedData,
			@NonNull List<IMessage> resultMessages,
			@NonNull IMetaModel metaModel
	) {
		this.documentV1 = documentV1;
		this.init(DocumentV1V2ConverterImpl.getInstance().ofDocumentV1(documentV1), computedData, resultMessages, metaModel);
	}

	/**
	 * Implementation of the result of a document computation.
	 *
	 * @param document
	 * 		the document
	 * @param computedData
	 * 		the computed data
	 * @param resultMessages
	 * 		the list of error messages
	 * @param metaModel
	 * 		the metamodel
	 */
	public DocumentComputationResultImpl(
			@NonNull DocumentV2 document,
			@NonNull IData computedData,
			@NonNull List<IMessage> resultMessages,
			@NonNull IMetaModel metaModel
	) {
		this.documentV1 = DocumentV1V2ConverterImpl.getInstance().toDocumentV1(document);
		this.init(document, computedData, resultMessages, metaModel);
		assert resultMessages.stream().allMatch(m -> m.getSeverity() == IMessage.Severity.ERROR);
	}

	private void init(@NonNull DocumentV2 document,
			@NonNull IData computedData,
			@NonNull List<IMessage> resultMessages,
			@NonNull IMetaModel metaModel) {
		Map<PartiallyKnownDocumentMultiPointer, IMessage> entityInstanceToErrorMessage = resultMessages.stream()
				.collect(Collectors.toMap(IMessage::getErrorFieldPointer, Function.identity()));
		Map<DocumentPointer, Object> originalComputedFieldInstances = getFilledComputedFieldInstances(document, metaModel);

		// if there are formal errors in the document, then we don't have any computed field instances
		for (IIdentifier id : computedData) {
			String computedStringValue = computedData.getValue(id);
			if (StringUtils.isEmpty(computedStringValue)) {
				// We don't process deleted fields, they are handled below by the originalComputedFieldInstances
				continue;
			}
			PartiallyKnownDocumentMultiPointer partiallyKnownPointer =
					DocumentPointerConverter.convertToPartiallyKnownDocumentMultiPointer(id);
			IMessage errorMessage = entityInstanceToErrorMessage.remove(partiallyKnownPointer);
			DocumentPointer pointer = partiallyKnownPointer.toDocumentPointer().orElseThrow(() -> new IllegalArgumentException(
					"This should not happen as the provided IData should only contain valid computed fields"));

			Object computedV2Value = LowKernelApiUtils.createValueV2(metaModel, pointer.fullName(), computedStringValue);
			if (errorMessage == null && ((computedV2Value == null) != StringUtils.isEmpty(computedStringValue))) {
				throw new IllegalArgumentException(
						"The computed string value '" + computedStringValue + "' is supposedly valid according to the " +
								"provided error messages but could not be converted to a basic value. This may occur when " +
								"the computed string value is not in the internal format, e.g. if the calculation was called " +
								"with presentation information present.");
			}
			IComputedFieldInstance computedFieldInstance = new ComputedFieldInstanceImpl(
					this.documentV1,
					pointer,
					computedV2Value,
					computedStringValue,
					Optional.ofNullable(errorMessage)
			);

			Object originalValue = originalComputedFieldInstances.remove(pointer);
			if (errorMessage == null) {
				computedFieldInstancesWithoutErrors.add(computedFieldInstance);
				if (!FieldInstanceImpl.fieldValuesEqual(originalValue, computedV2Value)) {
					computedFieldInstancesWithChanges.add(computedFieldInstance);
				}
			} else {
				computedFieldInstancesWithErrors.add(computedFieldInstance);
			}
		}

		// all remaining field instances were not computed --> must have been cleared
		originalComputedFieldInstances.forEach((pointer, originalValue) ->
				clearedFieldInstances.add(ComputedFieldInstanceImpl.empty(this.documentV1, pointer))
		);

		// all remaining errors are not from computed field instances --> must be from the operands
		formalErrorsInOperands.addAll(entityInstanceToErrorMessage.values());
	}

	@Override
	public boolean noErrorOccurred() {
		return computedFieldInstancesWithErrors.isEmpty() && formalErrorsInOperands.isEmpty();
	}

	@Override
	public @NonNull Collection<IComputedFieldInstance> getComputedFieldInstancesWithoutErrors() {
		return computedFieldInstancesWithoutErrors;
	}

	@Override
	public @NonNull Collection<IComputedFieldInstance> getComputedFieldInstancesWithChanges() {
		return computedFieldInstancesWithChanges;
	}

	@Override
	public @NonNull Collection<IComputedFieldInstance> getComputedFieldInstancesWithErrors() {
		return computedFieldInstancesWithErrors;
	}

	@Override
	public @NonNull Collection<IComputedFieldInstance> getClearedFieldInstances() {
		return clearedFieldInstances;
	}

	@Override
	public @NonNull Collection<IMessage> getFormalErrorsInOperands() {
		return formalErrorsInOperands;
	}

	/**
	 * @deprecated see {@link IDocumentComputationResult#getDocument()}
	 */
	@Deprecated(since = "30.0.0")
	@SuppressWarnings({ "removal" }) @Override
	public IDocument getDocument() {
		return documentV1;
	}

	/**
	 * @deprecated see {@link IDocumentComputationResult#apply(Consumer)}
	 */
	@Deprecated(since = "30.0.0")
	@SuppressWarnings("deprecation") @Override
	public void apply(Consumer<IComputedFieldInstance> errorHandler) {
		computedFieldInstancesWithChanges.forEach(cfi -> cfi.applyTo(documentV1));
		computedFieldInstancesWithErrors.forEach(
				cfi -> DocumentUtils.clearFieldInstanceValue(documentV1, cfi.getPath(), cfi.getRepetitions()));
	}

	/**
	 * @deprecated see {@link IDocumentComputationResult#applyTo(IDocument)}
	 */
	@Deprecated(since = "30.0.0")
	@SuppressWarnings({ "removal" }) @Override
	public void applyTo(IDocument document) {
		applyTo(document, DOC_FACTORY);
	}

	@Override
	public DocumentV2 applyTo(@NonNull DocumentV2 document) {
		DocumentV2 changedDocument = document;
		for (ComputedFieldInstanceImpl fi : Stream.of(clearedFieldInstances.stream(), computedFieldInstancesWithErrors.stream(),
						computedFieldInstancesWithChanges.stream())
				.flatMap(Function.identity())
				.map(fi -> {
					assert fi instanceof ComputedFieldInstanceImpl : "every computed field instance should be our implementation class";
					return (ComputedFieldInstanceImpl) fi;
				}).toList()) {
			changedDocument = fi.applyTo(changedDocument);
		}

		return changedDocument;
	}

	/**
	 * @deprecated see {@link IDocumentComputationResult#applyTo(IDocument, IDocumentFactory)}
	 */
	@Deprecated(since = "30.0.0")
	@SuppressWarnings({ "removal" }) @Override
	public void applyTo(IDocument document, IDocumentFactory documentFactory) {
		clearedFieldInstances.forEach(cfi -> cfi.applyTo(document, documentFactory));
		computedFieldInstancesWithChanges.forEach(cfi -> cfi.applyTo(document, documentFactory));
		computedFieldInstancesWithErrors.forEach(
				cfi -> DocumentUtils.clearFieldInstanceValue(document, cfi.getPath(), cfi.getRepetitions()));
	}

	@SuppressWarnings("unchecked") private static Set<String> getComputedFieldPaths(IMetaModel metaModel) {
		return ((Collection<IMetaField>) metaModel.getValue(IMetaKeys.MODEL_CALC_FIELDS)).stream()
				.map(metaField -> (String) metaField.getValue(IMetaKeys.FIELD_FULLNAME))
				.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * Return a map <ul>
	 * <li>from: pointers of the filled instances of computed fields in the given document</li>
	 * <li>to: the value of that field instance in the given document</li>
	 * </ul>
	 */
	private static Map<DocumentPointer, Object> getFilledComputedFieldInstances(DocumentV2 document, IMetaModel metaModel) {
		Set<String> computedFieldPaths = getComputedFieldPaths(metaModel);

		return computedFieldPaths.stream().map(DocumentMultiPointer::ofPathWithAllWildcards)
				.flatMap(pointer -> DocumentV2Utils.getFieldInstances(document, pointer).stream()).filter(fi ->
						DocumentUtils.valueExists(fi.getValue().value())
				).collect(Collectors.toMap(Map.Entry::getKey, fi -> fi.getValue().value()));
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof IDocumentComputationResult that))
			return false;
		return Objects.equals(documentV1, that.getDocument()) &&
				Objects.equals(computedFieldInstancesWithoutErrors, that.getComputedFieldInstancesWithoutErrors()) &&
				Objects.equals(computedFieldInstancesWithChanges, that.getComputedFieldInstancesWithChanges()) &&
				Objects.equals(computedFieldInstancesWithErrors, that.getComputedFieldInstancesWithErrors()) &&
				Objects.equals(clearedFieldInstances, that.getClearedFieldInstances()) &&
				Objects.equals(formalErrorsInOperands, that.getFormalErrorsInOperands());
	}

	@Override public int hashCode() {
		return Objects.hash(documentV1, computedFieldInstancesWithoutErrors,
				computedFieldInstancesWithChanges,
				computedFieldInstancesWithErrors, clearedFieldInstances, formalErrorsInOperands);
	}
}
