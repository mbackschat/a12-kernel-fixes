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

package com.mgmtp.a12.kernel.md.model.api.docs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mgmtp.a12.kernel.md.rt.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentRtServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;

public class IDocumentModelService {
	@Test
	void validationGeneration() {
		IDocumentModel docModel = getDocModel();

		// tag::validationGeneration[]
		DocumentModelServiceFactory factory = new DocumentModelServiceFactory();
		com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService docModelService = factory.createDocumentModelService();

		// using Java as the target language
		IValidationCodeGeneratorConfig validationCodeGeneratorConfig =
				() -> IValidationCodeGeneratorConfig.ProgrammingLanguage.JAVA;
		ArrayList<RankedNotification> notifications = new ArrayList<>();
		// the code as a zip file byte array
		byte[] code = docModelService.generateValidationCode(docModel, validationCodeGeneratorConfig,
				null, notifications::add);

		// store `code` to a zip file and extract the java source files
		// end::validationGeneration[]

		Assertions.assertEquals(List.of(), notifications);
		Assertions.assertTrue(code.length > 20);
	}

	private static IDocumentModel getDocModel() {
		Reader reader = new InputStreamReader(
				Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("document-models/small-example-for-deserialization/MyDm.json")),
				StandardCharsets.UTF_8);

		DocumentModelServiceFactory dmFactory = new DocumentModelServiceFactory();
		IDocumentModelSerializer dmSerializer = dmFactory.createDocumentModelSerializer();

		IDocumentModelResolver myDMResolver = id -> {
			try {
				return dmSerializer.deserialize(reader);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
		String documentModelId = "MyDm";
		IDocumentModel docModel = myDMResolver.getDocumentModelById(documentModelId);
		return docModel;
	}

	@Test
	void dynamicServiceConfig() {
		// tag::dynamicServiceConfig[]
		// Your own implementation of IModelCodeCache
		// An appropriate implementation prevents the document model code from being regenerated,
		// compiled, and instantiated.
		IModelCodeCache cache = new IModelCodeCache() {
			private final ConcurrentMap<String, IModelCode> genCodeCache = new ConcurrentHashMap<>();

			@Override
			public IModelCode getModelCode(@NonNull String modelCodeId) {
				return genCodeCache.get(modelCodeId);
			}

			@Override
			public void addModelCode(@NonNull String modelCodeId, @NonNull IModelCode modelCode) {
				genCodeCache.put(modelCodeId, modelCode);
			}
		};

		// Your own implementation of IDocumentDynamicServiceConfig, consider using 'cache' from above
		IDocumentDynamicServiceConfig config = new IDocumentDynamicServiceConfig() {
			@Override
			public Optional<String> getVariant() {
				return Optional.empty(); // not supported in A12
			}

			@Override
			public IModelCodeCache getCache() {
				return cache;
			}

			@Override
			public Optional<ILabelProvider> getLabelProvider() {
				return Optional.empty(); // for details see JavaDoc
			}
		};
		// end::dynamicServiceConfig[]
	}

	@Test
	void staticServiceConfig() {
		// tag::staticServiceConfig[]
		// Your own implementation of IStaticModelCodeCache
		// An appropriate implementation prevents the code for the document model
		// from being instantiated again.
		final IStaticModelCodeCache cache = new IStaticModelCodeCache() {
			private final Map<String, IStaticModelCode> map = Collections.synchronizedMap(new HashMap<>());

			@Override public IStaticModelCode getModelCode(@NonNull String modelCodeIdentifier) {
				return map.get(modelCodeIdentifier);
			}

			@Override public void addModelCode(@NonNull String modelCodeIdentifier, @NonNull IStaticModelCode modelCode) {
				map.put(modelCodeIdentifier, modelCode);
			}
		};

		// Your own implementation of IDocumentDynamicServiceConfig, consider using 'cache' from above
		IDocumentStaticServiceConfig config = new IDocumentStaticServiceConfig() {
			@Override
			public Optional<String> getVariant() {
				return Optional.empty(); // not supported in A12
			}

			@Override
			public Optional<IStaticModelCodeCache> getCache() {
				return Optional.of(cache);
			}

			@Override
			public Optional<ILabelProvider> getLabelProvider() {
				return Optional.empty(); // for details see JavaDoc
			}

			@Override
			public Optional<String> getModelPackage(String documentModelId) {
				return Optional.empty(); // for details see JavaDoc
			}
		};
		// end::staticServiceConfig[]
	}

	@Test
	void validateDocument() {
		IDocumentModel TODO = getDocModel();

		IModelCodeCache modelCodeCache = new IModelCodeCache() {
			private final ConcurrentMap<String, IModelCode> generatedCodeCache = new ConcurrentHashMap<>(); // caches the generated code by model name

			@Override
			public IModelCode getModelCode(@NonNull String modelName) {
				return generatedCodeCache.get(modelName);
			}

			@Override
			public void addModelCode(@NonNull String modelName, @NonNull IModelCode modelCode) {
				generatedCodeCache.put(modelName, modelCode);
			}
		};

		// @formatter:off
		IDocumentDynamicServiceConfig config = new IDocumentDynamicServiceConfig() {
			@Override public IModelCodeCache getCache() {return modelCodeCache;}
			@Override public Optional<String> getVariant() {return Optional.empty();}
			@Override public Optional<ILabelProvider> getLabelProvider() {return Optional.empty();}
		};
		// @formatter:on
		DocumentV2 document = DocumentV2.empty("MyDm");

		// tag::validateDocument[]
		// Your own implementation of IDocumentModelResolver
		IDocumentModelResolver dmResolver = new IDocumentModelResolver() {
			@Override
			public IDocumentModel getDocumentModelById(@NonNull String id) {
				return TODO; // for details see JavaDoc
			}
		};

		// A shorter form is also possible:
		{
			IDocumentModelResolver dmResolver2 = id -> {
				return TODO; // for details see JavaDoc
			};
		}

		DocumentRtServiceFactory rtFactory = new DocumentRtServiceFactory(dmResolver);
		IDocumentRtService docRtService = rtFactory.createDocumentRtService(config);
		IDocumentValidationResult docValResult = docRtService.validateFull(document, DocumentProcessingConfig.builder(Locale.ENGLISH).build());
		// end::validateDocument[]
	}
}
