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
package com.mgmtp.a12.kernel.mapping.service.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.ICodeGenResult;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.IMergedSingleScriptCodeGenResult;
import com.mgmtp.a12.kernel.mapping.facade.DynamicMappingServiceConfig;
import com.mgmtp.a12.kernel.mapping.facade.IMappingModelCacheIdProvider;
import com.mgmtp.a12.kernel.mapping.facade.IMappingModelResolver;
import com.mgmtp.a12.kernel.mapping.facade.internal.DefaultMappingModelCacheIdProvider;
import com.mgmtp.a12.kernel.mapping.facade.internal.ThrowingMappingModelResolverWrapper;
import com.mgmtp.a12.kernel.md.datatransfer.a12internal.mappingmodel.MappingModelService;
import com.mgmtp.a12.kernel.md.datatransfer.internal.AbstractDataTransferCodeGenerator;
import com.mgmtp.a12.kernel.md.datatransfer.internal.DataTransferCodeGenerator;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.api.services.internal.CodeGeneratorConfigV1_2Impl;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.RankedNotificationUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.RankedNotificationsException;
import com.mgmtp.a12.kernel.md.rt.api.IModelCodeCache;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingMetaModelService;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

/**
 * Generate model specific code and cache the result.
 */
public class DynamicMappingModelCodeGenerator {

	private static final ConcurrentMap<String, Object> LOCK_OBJECTS = new ConcurrentHashMap<>();
	private final DynamicMappingServiceConfig serviceConfig;
	private final IMappingModelResolver modelResolver;

	public DynamicMappingModelCodeGenerator(DynamicMappingServiceConfig serviceConfig) {
		this.serviceConfig = serviceConfig;
		this.modelResolver = ThrowingMappingModelResolverWrapper.wrap(serviceConfig.mappingModelResolver());
	}

	public DataTransferModelGroovyCode generateCode(final String mappingModelId) {
		MM_MappingModel_2 mappingModel = modelResolver.getMappingModelById(mappingModelId);
		IMappingModelCacheIdProvider modelIdProvider = serviceConfig.mappingModelCacheIdProvider()
				.orElse(new DefaultMappingModelCacheIdProvider());
		String modelCodeIdentifier = modelIdProvider.getMappingModelCacheId(mappingModel);
		IModelCodeCache cache = serviceConfig.modelCodeCache();
		DataTransferModelGroovyCode modelCode = (DataTransferModelGroovyCode) cache.getModelCode(modelCodeIdentifier);

		final String modelHash = calculateModelHash(mappingModel);

		if (modelCode == null || !Objects.equals(modelHash, modelCode.getModelHash())) {
			Object lock = getLockForModel(modelCodeIdentifier);
			// Note: the lock does not strictly guarantee that no concurrent modifications to the cache can occur,
			// because the cache could potentially be accessed from outside the current DynamicMappingModelCodeGenerator.
			// But this is not a problem because with our below code, such concurrent access could not break
			// correctness of the returned code, but only trigger unnecessary codeGeneration:
			// - in case we return code from the cache, we validate its hash _after_ fetching it from the cache
			// - in case we generate new code, we return our freshly generated code direcly without additional
			//   cache lookup, so that if the cache entry was to be overwritten in the cache after we generated it
			//   it would not affect our return value.
			// - assumtion: DataTransferModelGroovyCode instances are effectively immutable (the current lazy
			//   compilation is fine)
			synchronized (lock) {
				modelCode = (DataTransferModelGroovyCode) cache.getModelCode(modelCodeIdentifier);
				if (modelCode != null && Objects.equals(modelHash, modelCode.getModelHash())) {
					return modelCode;
				}
				final String generatedPlainCode = doCodeGeneration(mappingModel);
				modelCode = createCacheEntry(modelHash, generatedPlainCode);
				cache.addModelCode(modelCodeIdentifier, modelCode);
			}
		}
		return modelCode;
	}

	private String doCodeGeneration(MM_MappingModel_2 mappingModel) {
		Function<String, MM_StructuralMappingModel_1> structuralMappingModelResolver = this.modelResolver::getStructuralMappingById;

		CodeGeneratorConfigV1_2Impl codeGenConfig = CodeGeneratorConfigV1_2Impl.of(
				null,
				null,
				() -> IValidationCodeGeneratorConfig.ProgrammingLanguage.GROOVY);

		Collection<RankedNotification> notifications = new ArrayList<>();

		ICodeGenResult<AbstractDataTransferCodeGenerator.DataTransferEntryPoint> code = DataTransferCodeGenerator.createCodeForMappingModel(
				mappingModel,
				this.modelResolver,
				structuralMappingModelResolver,
				codeGenConfig,
				notifications::add,
				Locale.US,
				true);
		assert code instanceof IMergedSingleScriptCodeGenResult<?> : code.getClass();

		if (RankedNotificationUtils.hasErrors(notifications)) {
			throw new RankedNotificationsException("Following errors were found while generating code", notifications);
		}

		return new String(code.toSingleFileBytesIfApplicableOrThrow(), StandardCharsets.UTF_8);
	}

	private Object getLockForModel(String modelCodeIdentifier) {
		return LOCK_OBJECTS.computeIfAbsent(modelCodeIdentifier, id -> new Object());
	}

	private DataTransferModelGroovyCode createCacheEntry(final String modelHash, final String generatedPlainCode) {
		return new DataTransferModelGroovyCode(modelHash, generatedPlainCode);
	}

	private String calculateModelHash(final MM_MappingModel_2 mappingModel) {
		String serializedModel = serializeModelAndTransitiveReferencedModels(mappingModel);
		return calculateHash(serializedModel);
	}

	private String calculateHash(final String modelString) {
		MessageDigest messageDigest = createMessageDigest();
		messageDigest.update(modelString.getBytes());
		final byte[] digest = messageDigest.digest();
		final BigInteger bigInt = new BigInteger(1, digest);
		return bigInt.toString(16);
	}

	private String serializeModelAndTransitiveReferencedModels(final MM_MappingModel_2 mappingModel) {
		String serializedModel;
		Collection<RankedNotification> notifications = new ArrayList<>();
		DocumentModelSerializerImpl serializer = new DocumentModelSerializerImpl();
		try (StringWriter writer = new StringWriter();) {
			MappingModelService.serialize(mappingModel, writer);
			var smm = modelResolver.getStructuralMappingById(mappingModel.content().structuralMappingModel().id());
			StructuralMappingMetaModelService.serialize(smm, writer);
			var targetDM = modelResolver.getDocumentModelById(mappingModel.content().target().dmId());
			serializer.serialize(targetDM, writer, notifications::add);
			if (mappingModel.content().preComputationFragment() != null) {
				var precompDM = modelResolver.getDocumentModelById(mappingModel.content().preComputationFragment().dmId());
				serializer.serialize(precompDM, writer, notifications::add);
			}
			var sourceDMs = mappingModel.content().source().stream().map(it -> modelResolver.getDocumentModelById(it.dmId()))
					.toList();
			sourceDMs.forEach(it -> {
				try {
					serializer.serialize(it, writer, notifications::add);
				} catch (IOException e) {
					// This should never happen
					throw new UncheckedIOException(e);
				}
			});
			serializedModel = writer.toString();
		} catch (IOException ioe) {
			throw new IllegalStateException("Error while serializing the mapping model", ioe);
		}
		if (notifications.stream().anyMatch(it -> it.getSeverity() == Severity.ERROR)) {
			throw new RankedNotificationsException(notifications);
		}
		return serializedModel;
	}

	private MessageDigest createMessageDigest() {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to retrieve SHA-256 message digest", e);
		}
		messageDigest.reset();
		return messageDigest;
	}

}
