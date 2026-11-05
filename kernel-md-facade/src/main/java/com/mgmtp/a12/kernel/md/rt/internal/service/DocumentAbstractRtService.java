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

import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentComputationResult;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.ILabelProvider;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils.ValidationMethod;

import lombok.NonNull;

public abstract class DocumentAbstractRtService implements IDocumentRtService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentAbstractRtService.class);

	protected final IModelProvider modelProvider;
	protected final ILabelProvider labelProvider;
	protected final IDocumentModelResolver documentModelResolver;

	protected DocumentAbstractRtService(
			final IDocumentServiceConfig config,
			final IDocumentModelResolver documentModelResolver,
			final IModelProvider modelProvider
	) {
		this.modelProvider = modelProvider;
		this.labelProvider = config.getLabelProvider().orElseGet(LabelProviderImpl::new);
		this.documentModelResolver = documentModelResolver;
	}

	public IMetaModel getMetaModel(@NonNull DocumentV2 document) {
		return modelProvider.getGeneratedCodeAccessFactory(document.getDocumentModelId()).getMetaObject();
	}

	@Override public IDocumentComputationResult compute(@NonNull DocumentV2 document, @NonNull DocumentProcessingConfig config) {
		final StopWatch watch = new StopWatch();
		watch.start();
		LOGGER.debug("Executing method calculateAll");

		var accessFactory = modelProvider.getGeneratedCodeAccessFactory(document.getDocumentModelId());
		IMetaModel metaModel = accessFactory.getMetaObject();
		ICalculator calculator = accessFactory.getCalculator();
		try {
			return LowKernelApiUtils.callCalculator(calculator, metaModel, document, config,
					identifierToStringFunc(document.getDocumentModelId(), config.languageOfErrorMessages()));
		} finally {
			watch.stop();
			LOGGER.debug("=== '{}' computation executed in '{}'", document.getDocumentModelId(), watch);
		}
	}

	@Override public IDocumentValidationResult validateFull(@NonNull DocumentV2 document,
			@NonNull DocumentProcessingConfig config) {
		return callValidator(ValidationMethod.FULL_VALIDATION, document, null, config);
	}

	@Override public IDocumentValidationResult validatePart(@NonNull DocumentV2 document,
			@NonNull Set<? extends DocumentMultiPointer> relevantEntities, @NonNull DocumentProcessingConfig config) {
		return callValidator(ValidationMethod.PARTIAL_VALIDATION, document, relevantEntities, config);
	}

	private IDocumentValidationResult callValidator(
			ValidationMethod validationMethod,
			DocumentV2 document,
			Set<? extends DocumentMultiPointer> relevantEntityInstances,
			DocumentProcessingConfig config
	) {
		final StopWatch watch = new StopWatch();
		watch.start();
		var accessFactory = modelProvider.getGeneratedCodeAccessFactory(document.getDocumentModelId());
		IMetaModel metaModel = accessFactory.getMetaObject();
		IValidator validator = accessFactory.getValidator();
		watch.stop();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Validation call prepared in '%s'", watch));
			LOGGER.debug(String.format("Executing method '%s'", validationMethod.getDisplayName()));
		}
		watch.reset();
		watch.start();
		try {
			return LowKernelApiUtils.callValidator(validationMethod, validator, metaModel, document, relevantEntityInstances,
					config, identifierToStringFunc(document.getDocumentModelId(), config.languageOfErrorMessages()));
		} finally {
			watch.stop();
			LOGGER.debug("=== '{}' Validation executed in '{}'", document.getDocumentModelId(), watch);
		}
	}

	@Override public String createModelCodeIdentifier(@NonNull IDocumentModel documentModel) {
		return modelProvider.createModelCodeIdentifier(documentModel);
	}

	private IIdentifierToString identifierToStringFunc(String documentModelId, Locale locale) {
		return new IdentifierToStringForValidation(locale, labelProvider, documentModelResolver, documentModelId);
	}
}
