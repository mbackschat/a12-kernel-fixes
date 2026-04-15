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

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.codegen.GroovyCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.RankedNotificationUtils;
import com.mgmtp.a12.kernel.md.rt.api.DocumentValidationException;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentModelIdProvider;
import com.mgmtp.a12.kernel.md.rt.api.IModelCode;
import com.mgmtp.a12.kernel.md.rt.api.IModelCodeCache;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;
import com.mgmtp.a12.model.notification.RankedNotification;

/**
 * Generate model specific code and cache the result.
 */
public class DocumentModelCodeGenerator {

	private final IModelCodeCache cache;
	private final IDocumentModelIdProvider modelIdProvider;
	private static final ConcurrentMap<String, Object> LOCK_OBJECTS = new ConcurrentHashMap<>();

	public DocumentModelCodeGenerator(IModelCodeCache cache, IDocumentModelIdProvider modelIdProvider) {
		this.cache = cache;
		this.modelIdProvider = modelIdProvider;
	}

	public IModelCode generateCode(final IDocumentModel documentModel, final String variant) {
		String modelCodeIdentifier = modelIdProvider.getDocumentModelId(documentModel, variant);
		IModelCode modelCode = cache.getModelCode(modelCodeIdentifier);

		final String modelHash = calculateModelHash(documentModel);

		if (modelCode == null || !Objects.equals(modelHash, modelCode.getModelHash())) {
			Object lock = getLockForModel(modelCodeIdentifier);
			synchronized (lock) {
				modelCode = cache.getModelCode(modelCodeIdentifier);
				if (modelCode != null && Objects.equals(modelHash, modelCode.getModelHash())) {
					assert modelCode instanceof ValidationModelGroovyCode : modelCode.getClass();
					return modelCode;
				}
				// if the code generation fails, a VkGeneratorException is thrown
				IDocumentModelService service = new ExternalDocumentModelService();
				Collection<RankedNotification> notifications = new ArrayList<>();
				final byte[] byteCode = service.generateValidationCode(documentModel, new GroovyCodeGeneratorConfig(variant), null,
						notifications::add);
				if (RankedNotificationUtils.hasErrors(notifications)) {
					throw new DocumentValidationException(
							"Following errors were found while generating code: " + RankedNotificationUtils.joinErrorMessages(
									notifications));
				}
				final String generatedPlainCode = new String(byteCode, StandardCharsets.UTF_8);
				modelCode = createCacheEntry(modelHash, generatedPlainCode);
				cache.addModelCode(modelCodeIdentifier, modelCode);
			}
		}
		assert modelCode instanceof ValidationModelGroovyCode : modelCode.getClass();
		return modelCode;
	}

	private Object getLockForModel(String modelCodeIdentifier) {
		return LOCK_OBJECTS.computeIfAbsent(modelCodeIdentifier, id -> new Object());
	}

	private IModelCode createCacheEntry(final String modelHash, final String generatedPlainCode) {
		return new ValidationModelGroovyCode(modelHash, generatedPlainCode);
	}

	private String calculateModelHash(final IDocumentModel documentModel) {
		String serializedModel = getSerializedModel(documentModel);
		MessageDigest messageDigest = createMessageDigest();
		messageDigest.update(serializedModel.getBytes());
		final byte[] digest = messageDigest.digest();
		final BigInteger bigInt = new BigInteger(1, digest);
		return bigInt.toString(16);
	}

	private String getSerializedModel(final IDocumentModel documentModel) {
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

	private MessageDigest createMessageDigest() {
		MessageDigest messageDigest;
		try {
			// nosemgrep: java.lang.security.audit.crypto.use-of-md5.use-of-md5
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to retreive MD5 message digest", e);
		}
		messageDigest.reset();
		return messageDigest;
	}

}
