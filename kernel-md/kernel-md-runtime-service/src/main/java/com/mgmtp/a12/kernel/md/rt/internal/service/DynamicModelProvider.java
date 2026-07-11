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

import static com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelResolverUtils.getDocumentModelOrThrow;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mgmtp.a12.kernel.core.rt.a12internal.IGeneratedCodeAccessFactory;
import com.mgmtp.a12.kernel.core.utils.internal.HashingHelper;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.codegen.GroovyCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DMProblemUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelServiceUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.RankedNotificationUtils;
import com.mgmtp.a12.kernel.md.rt.api.DocumentValidationException;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentDynamicServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IModelCode;
import com.mgmtp.a12.kernel.md.rt.api.IModelCodeHashProvider;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DynamicModelProvider implements IModelProvider {

	private final @NonNull IDocumentDynamicServiceConfig config;
	private final @NonNull IDocumentModelResolver documentModelResolver;
	private static final ConcurrentMap<String, Object> LOCK_OBJECTS = new ConcurrentHashMap<>();

	@Override public String createModelCodeIdentifier(IDocumentModel documentModel) {
		return config.getModelIdProvider()
				.orElse(DocumentModelServiceUtils::createGeneratedCodeId)
				.getDocumentModelId(documentModel);
	}

	@Override
	public IGeneratedCodeAccessFactory getGeneratedCodeAccessFactory(String documentModelId) {
		IDocumentModel documentModel = getDocumentModelOrThrow(documentModelResolver, documentModelId);
		String modelCodeIdentifier = createModelCodeIdentifier(documentModel);
		IModelCode modelCode = config.getCache().getModelCode(modelCodeIdentifier);
		IModelCodeHashProvider hashProvider = config.getModelCodeHashProvider()
				.orElse((dmId, resolver) -> calculateModelHash(documentModel));

		String modelHash = hashProvider.getHash(documentModelId, documentModelResolver);
		if (modelCode == null || !Objects.equals(modelHash, modelCode.getModelHash())) {
			// Note: The lock here does not prevent concurrent modifications of the cache - those can occur at any point in time
			// since it is controlled by the caller. Assuming that ValidationModelGroovyCode is immutable and that the hash
			// comparison is a sufficient check for equality, we can guarantee that the model code returned will match the document
			// model, because:
			// - in case we return code from the cache, we validate its hash _after_ fetching it from the cache
			// - in case we generate new code, we return our freshly generated code directly without additional cache lookup. If
			//   the cache entry got modified during code generation, it would not affect our return value
			// We can _not_ guarantee that the cache entry will be correct afterward. At worst, this will result in unnecessary code
			// generation.
			//
			// In case the hashing function is _not_ sufficient, we can easily reach inconsistent cache states (regardless of
			// whether we use a lock here), which could result in outdated models being used for the validation/computation.
			Object lock = getLockForModel(modelCodeIdentifier);
			synchronized (lock) {
				modelCode = config.getCache().getModelCode(modelCodeIdentifier);
				if (modelCode == null || !Objects.equals(modelHash, modelCode.getModelHash())) {
					String generatedPlainCode = doCodeGeneration(documentModel);
					modelCode = new ValidationModelGroovyCode(modelHash, generatedPlainCode);
					config.getCache().addModelCode(modelCodeIdentifier, modelCode);
				}
			}
		}

		return (ValidationModelGroovyCode) modelCode;
	}

	private String doCodeGeneration(IDocumentModel documentModel) {
		Collection<RankedNotification> notifications = new ArrayList<>();
		byte[] byteCode = new ExternalDocumentModelService(config.considerNotInD()).generateValidationCode(
				documentModel, new GroovyCodeGeneratorConfig(), null, notifications::add);
		// if the code generation fails, a VkGeneratorException is thrown
		if (RankedNotificationUtils.hasErrors(notifications)) {
			throw new DocumentValidationException(
					"Following errors were found while generating code: " + DMProblemUtils.join(notifications, Severity.ERROR));
		}
		return new String(byteCode, StandardCharsets.UTF_8);
	}

	private static @NonNull Object getLockForModel(@NonNull String modelCodeIdentifier) {
		return LOCK_OBJECTS.computeIfAbsent(modelCodeIdentifier, id -> new Object());
	}

	private static String calculateModelHash(final IDocumentModel documentModel) {
		return HashingHelper.calculateHash(getSerializedModel(documentModel));
	}

	private static String getSerializedModel(final IDocumentModel documentModel) {
		IDocumentModelSerializer serializer = new DocumentModelSerializerImpl();
		String serializedModel = null;
		try (StringWriter writer = new StringWriter();) {
			List<RankedNotification> problems = new ArrayList<>();
			serializer.serialize(documentModel, writer, problems::add);
			if (!problems.isEmpty()) {
				throw new IllegalStateException("The document model could not be serialized: " + problems);
			}
			serializedModel = writer.toString();
		} catch (IOException ioe) {
			throw new IllegalStateException("Error while serializing the document model", ioe);
		}
		return serializedModel;
	}
}
