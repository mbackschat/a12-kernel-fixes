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

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt.a12internal.custom.ICustomConditionFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.*;

import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV1V2ConverterImpl;
import com.mgmtp.a12.kernel.md.rt.a12internal.service.DocumentComputationResultImpl;
import com.mgmtp.a12.kernel.md.rt.a12internal.service.DocumentValidationResultImpl;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentComputationResult;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.model.repository.FaultException;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV2;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("removal")
public class LowKernelApiUtils {
	private LowKernelApiUtils() {
		throw new IllegalStateException("Utility class");
	}

	private static ValidationMode configureValidationMode(DocumentProcessingConfig config) {
		final ValidationMode result = new ValidationMode();
		result.setLanguage(config.languageOfErrorMessages());
		result.setCurrentDateForTest(config.currentDateForTest());
		config.additionalInformation().forEach(result::setAdditionalInformation);
		result.setCustomFieldTypeFactory(config.customFieldTypeFactory());
		result.setCustomConditionFactory(toA12Internal(config.customConditionFactory()));
		result.setValidationType(ValidationMode.ValidationType.fullExamination);
		result.setRuleType(ValidationMode.RuleType.ALL);
		// never pass diverging number and date format here because the document values always use the canonical format
		result.setPresentationInformation(null);
		return result;
	}

	private static ICustomConditionFactory toA12Internal(com.mgmtp.a12.kernel.md.rt.api.ICustomConditionFactory externalFactory) {
		return externalFactory == null ? null : new CustomConditionFactoryWrapper(externalFactory);
	}

	/**
	 * @deprecated use {@link #createData(DocumentV2, IMetaModel, Locale, IResult, IIdentifierToString)} instead
	 */
	@Deprecated(since = "30.0.0")
	public static IData createData(IDocument document, IMetaModel metaModel, Locale locale, IResult result,
			IIdentifierToString identifierToStringFunc) {
		return new ValidationData(document, metaModel, locale, result, identifierToStringFunc);
	}

	public static IData createData(DocumentV2 document, IMetaModel metaModel, Locale locale, IResult result,
			IIdentifierToString identifierToStringFunc) {
		return new ValidationData(document, metaModel, locale, result, identifierToStringFunc);
	}

	/**
	 * Convert the given internal string value to a basic type based on the field type format of the field at the given path of the
	 * given metamodel. Conversion errors are ignored.
	 *
	 * @deprecated use {@link #createValueV2(IMetaModel, String, String)} instead.
	 */
	@Deprecated(since = "30.2.0")
	public static Object createValue(IMetaModel metaModel, String path, String stringValue) {
		if (StringUtils.isNotEmpty(stringValue)) {
			IMetaField metaField = (IMetaField) metaModel.getValue(IMetaKeys.MODEL_META_FIELD, path);
			Objects.requireNonNull(metaField);
			// problems with stringValue are presumed to have already been reported somewhere else (e.g. computation)
			DocumentValidationResultImpl validationResult = new DocumentValidationResultImpl();
			return metaField.convertWithOnlyBasicTypeCheck(stringValue, null, validationResult);
		} else {
			return null;
		}
	}

	/**
	 * like {@link #createValue(IMetaModel, String, String)}, but for
	 * {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2}
	 * / {@link BasicTypeV2} instead of {@link IDocument} /
	 * {@link BasicTypeV1}.
	 */
	public static Object createValueV2(IMetaModel metaModel, String path, String stringValue) {
		return BasicTypeV1V2Conversion.basicTypeV1ToV2(createValue(metaModel, path, stringValue));
	}

	@Getter @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public enum ValidationMethod {
		FULL_VALIDATION("full validation"),
		PARTIAL_VALIDATION("partial validation with 3-value-logic");

		private final String displayName;
	}

	/**
	 * @deprecated use {@link #callCalculator(ICalculator, IMetaModel, DocumentV2, DocumentProcessingConfig, IIdentifierToString)} instead
	 */
	@Deprecated(since = "30.0.0")
	public static IDocumentComputationResult callCalculator(@NonNull ICalculator calculator, @NonNull IMetaModel metaModel,
			@NonNull IDocument document, @NonNull Locale locale, @NonNull IIdentifierToString identifierToStringFunc) {
		return callCalculator(calculator, metaModel, DocumentV1V2ConverterImpl.getInstance().ofDocumentV1(document),
				DocumentProcessingConfig.builder(locale).build(),
				identifierToStringFunc);
	}

	public static IDocumentComputationResult callCalculator(@NonNull ICalculator calculator, @NonNull IMetaModel metaModel,
			@NonNull DocumentV2 document, @NonNull DocumentProcessingConfig config,
			@NonNull IIdentifierToString identifierToStringFunc) {
		try {
			DocumentValidationResultImpl formalErrors = new DocumentValidationResultImpl();
			IData data = createData(document, metaModel, config.languageOfErrorMessages(), formalErrors, identifierToStringFunc);
			ValidationMode mode = configureValidationMode(config);
			IData calculatedData = calculator.calculateAll(data, mode, formalErrors);
			assert formalErrors.getMessages().stream().allMatch(m -> m.getSeverity() == IMessage.Severity.ERROR);
			return new DocumentComputationResultImpl(document, calculatedData, formalErrors.getMessages(), metaModel);
		} catch (final Exception e) {
			throw new FaultException("Unable to invoke computation", e);
		}
	}

	/**
	 * @deprecated use
	 *        {@link #callValidator(ValidationMethod, IValidator, IMetaModel, DocumentV2, Set, DocumentProcessingConfig,
	 *        IIdentifierToString)} instead
	 */
	@Deprecated(since = "30.0.0")
	public static IDocumentValidationResult callValidator(
			@NonNull ValidationMethod validationMethod,
			@NonNull IValidator validator,
			@NonNull IMetaModel metaModel,
			@NonNull IDocument document,
			Set<IEntityInstance> relevantEntityInstances,
			@NonNull Locale locale,
			@NonNull IIdentifierToString identifierToStringFunc
	) {
		Set<DocumentMultiPointer> relevantEntityInstancesPointers = relevantEntityInstances == null ? null
				: DocumentPointerConverter.convertToDocumentMultiPointerSet(
				EntityInstanceConverter.convertToIIdentifierSet(relevantEntityInstances));
		return callValidator(validationMethod, validator, metaModel, DocumentV1V2ConverterImpl.getInstance().ofDocumentV1(document),
				relevantEntityInstancesPointers, DocumentProcessingConfig.builder(locale).build(), identifierToStringFunc);
	}

	public static IDocumentValidationResult callValidator(
			@NonNull ValidationMethod validationMethod,
			@NonNull IValidator validator,
			@NonNull IMetaModel metaModel,
			@NonNull DocumentV2 document,
			Set<? extends DocumentMultiPointer> relevantEntityInstances,
			@NonNull DocumentProcessingConfig config,
			@NonNull IIdentifierToString identifierToStringFunc
	) {
		DocumentValidationResultImpl result = new DocumentValidationResultImpl();
		IData data = createData(document, metaModel, config.languageOfErrorMessages(), result, identifierToStringFunc);
		ValidationMode mode = configureValidationMode(config);

		try {
			switch (validationMethod) {
				case FULL_VALIDATION -> validator.validateAll(data, result, mode);
				case PARTIAL_VALIDATION -> {
					// add ids of global fields
					Set<IIdentifier> relevantIds = DocumentPointerConverter.convertToIIdentifierSet(relevantEntityInstances);
					addGlobalFieldsToRelevantEntities(metaModel, relevantIds);
					validator.validatePartlyWith3ValueLogic(data, relevantIds, result, mode);
				}
			}
			return result;
		} catch (final Exception e) {
			throw new FaultException("Unable to invoke validation", e);
		}
	}

	private static void addGlobalFieldsToRelevantEntities(IMetaModel metaModel, Set<IIdentifier> relevantIds) {
		@SuppressWarnings("unchecked")
		Set<IIdentifier> globalIds = (Set<IIdentifier>) metaModel.getValue("GlobalFields");
		relevantIds.addAll(globalIds);
	}

	@SuppressWarnings("unchecked")
	public static void checkLocaleIsSupported(@NonNull IMetaModel metaModel, @NonNull Locale locale) {
		List<Locale> supportedLocales = (List<Locale>) metaModel.getValue(IMetaKeys.MODEL_LANGUAGES);
		if (!supportedLocales.contains(locale)) {
			throw new IllegalArgumentException(
					"The locale '%s' is not supported (supported locales: %s).".formatted(
							locale,
							supportedLocales.stream().map("'%s'"::formatted).collect(Collectors.joining(", "))));
		}
	}
}
