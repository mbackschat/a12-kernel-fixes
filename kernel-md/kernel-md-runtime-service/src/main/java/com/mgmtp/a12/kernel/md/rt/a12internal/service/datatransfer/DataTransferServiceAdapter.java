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

package com.mgmtp.a12.kernel.md.rt.a12internal.service.datatransfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.IDataTransferService;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.IDataTransferService.DataTransferConfig;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.MappedData;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.SourceData;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.mapping.rt.a12internal.IMappingServiceA12Internal;
import com.mgmtp.a12.kernel.mapping.rt.api.MappedField;
import com.mgmtp.a12.kernel.mapping.rt.api.MappingResult;
import com.mgmtp.a12.kernel.mapping.rt.api.SourceDocument;
import com.mgmtp.a12.kernel.mapping.rt.api.SourceEligibilityResult;
import com.mgmtp.a12.kernel.mapping.rt.api.SourceField;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.a12internal.service.DataDocumentConversionUtils;
import com.mgmtp.a12.kernel.md.rt.a12internal.service.DocumentValidationResultImpl;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.ILabelProvider;
import com.mgmtp.a12.kernel.md.rt.internal.service.DataTransferConfigUtil;
import com.mgmtp.a12.kernel.md.rt.internal.service.IIdentifierToString;
import com.mgmtp.a12.kernel.md.rt.internal.service.IdentifierToStringForValidation;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@OnlyForUsage
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DataTransferServiceAdapter implements IMappingServiceA12Internal {
	private final @NonNull IDataTransferService transferService;
	@Getter private final @NonNull IMetaModel targetMetaModel;
	private final IDocumentModelResolver documentModelResolver;
	private final ILabelProvider labelProvider;

	public static DataTransferServiceAdapter wrap(@NonNull IDataTransferService transferService,
			@NonNull IMetaModel targetMetaModel, @NonNull IDocumentModelResolver documentModelResolver,
			ILabelProvider labelProvider) {
		return new DataTransferServiceAdapter(transferService, targetMetaModel, documentModelResolver, labelProvider);
	}

	public static DataTransferServiceAdapter wrap(@NonNull IDataTransferService transferService,
			@NonNull IMetaModel targetMetaModel) {
		return new DataTransferServiceAdapter(transferService, targetMetaModel, null, null);
	}

	/**
	 * like {@link #transferData(List, DocumentV2, DataTransferConfig)}, with
	 * {@code IDataTransferService.DataTransferConfig.of(locale)} for the config, i.e. with no {@code additionalInformation}
	 * and no {@code currentDateForTest} set (or any other additional config attributes that may be introduced some day).
	 */
	public MappingResult transferData(
			@NonNull List<SourceDocument> sourceDocuments,
			@NonNull DocumentV2 targetDocument,
			@NonNull Locale locale
	) {
		return transferData(sourceDocuments, targetDocument, DataTransferConfig.builder(locale).build());
	}

	/**
	 * Transfer data from the source documents into the target document according to the wrapped data transfer.
	 *
	 * @param sourceDocuments
	 * 		the documents to use as sources for the data transfer
	 * @param targetDocument
	 * 		the document which the data should be transferred into
	 * @param config
	 * 		the runtime configuration to use to execute the transfer code with
	 * @return the result of the data transfer
	 */
	@Override
	public @NonNull MappingResult transferData(
			@NonNull List<SourceDocument> sourceDocuments,
			@NonNull DocumentV2 targetDocument,
			@NonNull DocumentProcessingConfig config
	) {
		return transferData(sourceDocuments, targetDocument, DataTransferConfigUtil.fromDocumentProcessingConfig(config));
	}

	/**
	 * Transfer data from the source documents into the target document according to the wrapped data transfer.
	 *
	 * @param sourceDocuments
	 * 		the documents to use as sources for the data transfer
	 * @param targetDocument
	 * 		the document which the data should be transferred into
	 * @return the result of the data transfer
	 */
	public @NonNull MappingResult transferData(
			@NonNull List<SourceDocument> sourceDocuments,
			@NonNull DocumentV2 targetDocument,
			@NonNull DataTransferConfig config
	) {
		Locale locale = config.validationLanguage();

		DocumentValidationResultImpl precomputationConsistencyResult = new DocumentValidationResultImpl();
		DocumentValidationResultImpl targetResult = new DocumentValidationResultImpl();

		// convert source documents to SourceData
		List<SourceData> listOfSourceData = new ArrayList<>();
		Java8Utils.forEachIndexed(sourceDocuments, (index, sourceDocument) -> {
			String sourceName = sourceDocument.sourceName();
			String sourceId = sourceDocument.sourceInstanceId();
			IMetaModel metaModel = transferService._internal_getMetaObjectForSource(sourceName);
			DocumentValidationResultImpl eligibilityResult = new DocumentValidationResultImpl();
			IIdentifierToString identifierToStringFunc = getIdentifierToStringFunc(locale, sourceName);
			IData data = LowKernelApiUtils.createData(sourceDocument.document(), metaModel, locale, eligibilityResult,
					identifierToStringFunc, config.customFieldTypeFactory());
			listOfSourceData.add(SourceData.of(sourceName, sourceId, data, eligibilityResult));
		});

		// convert target document to IData
		IIdentifierToString identifierToStringFunc = getIdentifierToStringFunc(locale, targetDocument.getDocumentModelId());
		IData targetData = LowKernelApiUtils.createData(targetDocument, targetMetaModel, locale, targetResult,
				identifierToStringFunc, config.customFieldTypeFactory());

		// run data transfer
		MappedData mappedData = transferService.getTransferredData(targetData, listOfSourceData, targetResult,
				precomputationConsistencyResult, config);

		// create data transfer result
		Map<String, SourceEligibilityResult> eligibilityResults = listOfSourceData.stream().collect(Collectors.toMap(
				SourceData::sourceId,
				sd -> SourceEligibilityResult.of(
						sd.sourceId(),
						mappedData.selectedSourceIds().contains(sd.sourceId()),
						List.copyOf(((DocumentValidationResultImpl) sd.result()).getMessages())
				)));
		List<MappedField> mappedFields = mappedData.mappedFields().stream()
				.map(this::createMappedField)
				.toList();
		Set<DocumentPointer> removedEntities = mappedData.removedEntities().stream()
				.map(DataTransferServiceAdapter::pointerFromIdentifier)
				.collect(Collectors.toSet());
		boolean success = mappedData.success();
		return MappingResult.of(
				success,
				mappedFields,
				removedEntities,
				eligibilityResults,
				targetResult.getMessages(),
				precomputationConsistencyResult.getMessages()
		);
	}

	private MappedField createMappedField(com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.MappedField mappedField) {
		DocumentPointer targetField = pointerFromIdentifier(mappedField.targetField());
		Object value = LowKernelApiUtils.createValueV2(targetMetaModel, mappedField.targetField().getName(), mappedField.value());
		Set<SourceField> operationSourceFields = mappedField.source().operationSourceFields().stream()
				.map(sourceIdentifier ->
						SourceField.of(
								sourceIdentifier.sourceId(),
								pointerFromIdentifier(sourceIdentifier)))
				.collect(Collectors.toSet());
		return MappedField.of(targetField, value, operationSourceFields);
	}

	private IIdentifierToString getIdentifierToStringFunc(Locale locale, String documentModelId) {
		return documentModelResolver == null
				? IIdentifier::toString
				: new IdentifierToStringForValidation(locale, labelProvider, documentModelResolver, documentModelId);
	}

	private static DocumentPointer pointerFromIdentifier(IIdentifier id) {
		List<String> path = ElementPathUtils.splitAbsPath(id.getName());
		List<Integer> repetitions = Arrays.stream(id.getIndices())
				.mapToObj(index -> (int) index)
				.toList();
		return DocumentPointer.of(path, repetitions);
	}

	public IDataTransferService unwrap() {
		return transferService;
	}

	@Override public @NonNull ISortedSourceDocumentResult sortSourceDocument(@NonNull SourceDocument sourceDocument,
			@NonNull DocumentProcessingConfig config) {
		var metaModel = transferService._internal_getMetaObjectForSource(sourceDocument.sourceName());
		return sortData(sourceDocument, metaModel, config);
	}

	public static ISortedSourceDocumentResult sortData(SourceDocument sourceDocument, IMetaModel metaModel,
			DocumentProcessingConfig config) {
		DocumentValidationResultImpl errorResult = new DocumentValidationResultImpl();
		Locale locale = config.languageOfErrorMessages();
		IData data = DataDocumentConversionUtils.createData(sourceDocument.document(), metaModel, locale, errorResult, config.customFieldTypeFactory());
		if (!errorResult.noErrorOccurred()) {
			return IMappingServiceA12Internal.ISortedSourceDocumentResult.of(sourceDocument, errorResult.getMessages());
		}
		IData result = (IData) metaModel.getValue(IMetaKeys.MODEL_SORTED_DATA, data, errorResult, config.customFieldTypeFactory(),
				locale);
		if (result == data) {
			return IMappingServiceA12Internal.ISortedSourceDocumentResult.of(sourceDocument, errorResult.getMessages());
		}
		DocumentV2 newDocument = DataDocumentConversionUtils.createDocumentV2(result,
				sourceDocument.document().getDocumentModelId(), metaModel, errorResult, null);
		if (!errorResult.noErrorOccurred()) {
			return IMappingServiceA12Internal.ISortedSourceDocumentResult.of(sourceDocument, errorResult.getMessages());
		}
		return IMappingServiceA12Internal.ISortedSourceDocumentResult.of(
				SourceDocument.of(sourceDocument.sourceName(), sourceDocument.sourceInstanceId(), newDocument),
				List.of());
	}

}
