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
package com.mgmtp.a12.kernel.md.rt.api.docs;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.kernel.internal.test.md.utils.DocumentModelResolverFromClasspath;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.facade.DocumentRtServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.ICustomCondition;
import com.mgmtp.a12.kernel.md.rt.api.ICustomConditionFactory;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentDynamicServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.ILabelProvider;
import com.mgmtp.a12.kernel.md.rt.api.IModelCode;
import com.mgmtp.a12.kernel.md.rt.api.IModelCodeCache;

import lombok.NonNull;

class TestCustomConditionSnippetsForDocs {

	// tag::IsNotReverseCustomCondition[]
	class IsNotReverseCustomCondition implements ICustomCondition {
		private final Set<String> fieldPaths;

		/**
		 * Uses two paths of fields that have to be compared
		 */
		public IsNotReverseCustomCondition(String fieldPath1, String fieldPath2) {
			this.fieldPaths = Set.of(fieldPath1, fieldPath2);
		}

		@Override public boolean check(@NonNull DocumentV2 document,
				Set<? extends DocumentMultiPointer> relevantEntities,
				@NonNull Set<DocumentPointer> formallyIncorrectEntities,
				@NonNull PartiallyKnownDocumentMultiPointer errorEntityInstance) {
			// a) necessary fields are in relevantEntityInstances OR all fields are relevant
			if (relevantEntities != null && relevantEntities.stream().anyMatch(fi -> !this.fieldPaths.contains(fi.fullName()))) {
				return false;
			}

			// b) necessary fields not in formallyIncorrectEntityInstances
			if (formallyIncorrectEntities.stream().anyMatch(fi -> this.fieldPaths.contains(fi.fullName()))) {
				return false;
			}

			// c) condition
			List<String> values = fieldValuesFromDocument(document);
			String field1 = values.get(0);
			String field2 = values.get(1);

			return !field1.equals(new StringBuilder(field2).reverse().toString());
		}

		private List<String> fieldValuesFromDocument(DocumentV2 document) {
			return this.fieldPaths.stream()
					.map(fieldPath ->
							// fieldValue() may return null or "" when a field is not filled in the document.
							Optional.ofNullable(document.fieldValue(fieldPath)).map(Object::toString).orElse(""))
					.toList();
		}

		// tag::exclude[]
		public Set<String> getFieldPaths() {
			return fieldPaths;
		}
		// end::exclude[]
	}
	// end::IsNotReverseCustomCondition[]

	// tag::CustomConditionFactory[]
	// option 1: implement the interface ICustomConditionFactory
	class MyCustomConditionFactory implements ICustomConditionFactory {
		@Override
		public Optional<ICustomCondition> createCustomConditionV2(@NonNull String customConditionName) {
			return Optional.ofNullable(switch (customConditionName) {
				case "NotReverse" -> new IsNotReverseCustomCondition("/Group1/Field1", "/Group1/Field2");
				case "NotReverse2" -> new IsNotReverseCustomCondition("/Group1/Field1", "/Group1/Field3");
				default -> null;
			});
		}
	}
	// end::CustomConditionFactory[]

	@Test
	void testCustomConditionFactoryFromMap() {
		// tag::CustomConditionFactoryFromMap[]
		// option 2: use the simplified factory method
		ICustomConditionFactory.fromMap(Map.of(
				"NotReverse", new IsNotReverseCustomCondition("/Group1/Field1", "/Group1/Field2"),
				"NotReverse2", new IsNotReverseCustomCondition("/Group1/Field1", "/Group1/Field3")
		));
		// end::CustomConditionFactoryFromMap[]
	}

	@Test
	void testFullValidation() {
		IDocumentModelResolver dmResolver = DocumentModelResolverFromClasspath.of(List.of(
				dmId -> "document-models/example-for-custom-conditions/" + dmId + ".json"
		));

		// @formatter:off
		IModelCodeCache modelCodeCache = new IModelCodeCache() {
			private final ConcurrentMap<String, IModelCode> generatedCodeCache = new ConcurrentHashMap<>(); // caches the generated code by model name
			@Override public IModelCode getModelCode(@NonNull String modelName) { return generatedCodeCache.get(modelName); }
			@Override public void addModelCode(@NonNull String modelName, @NonNull IModelCode modelCode) {
				generatedCodeCache.put(modelName, modelCode);
			}
		};
		IDocumentDynamicServiceConfig serviceConfig = new IDocumentDynamicServiceConfig() {
			@Override public IModelCodeCache getCache() {return modelCodeCache;}
			@Override public Optional<ILabelProvider> getLabelProvider() {return Optional.empty();}
		};
		// @formatter:on
		DocumentV2 document = DocumentV2.empty("CustomIsReverseTestModel")
				.withFieldValue("Group1[1]/Field1", "abcd")
				.withFieldValue("Group1[1]/Field2", "aaaa") // invalid
				.withFieldValue("Group1[1]/Field3", "dcba"); // valid

		DocumentRtServiceFactory rtFactory = new DocumentRtServiceFactory(dmResolver);
		IDocumentRtService documentRtService = rtFactory.createDocumentRtService(serviceConfig);
		// tag::provideCustomCode[]
		// configure the custom condition factory in the DocumentProcessingConfig
		DocumentProcessingConfig config = DocumentProcessingConfig.builder(Locale.ENGLISH)
				.customConditionFactory(new MyCustomConditionFactory())
				.build();
		// use this DocumentProcessingConfig for API calls (validation/computation/mapping/...)
		IDocumentValidationResult validationResult = documentRtService.validateFull(document, config);
		// end::provideCustomCode[]
		Assertions.assertEquals(1, validationResult.getMessages().size());
		Assertions.assertEquals("SampleErrorCode", validationResult.getMessages().getFirst().getErrorCode());
		Assertions.assertEquals("Field1 is not reversed Field2", validationResult.getMessages().getFirst().getErrorText());
	}
}
