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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.internal.test.md.utils.DocumentModelResolverFromClasspath;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.facade.DocumentRtServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentDynamicServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.ILabelProvider;
import com.mgmtp.a12.kernel.md.rt.api.IModelCode;
import com.mgmtp.a12.kernel.md.rt.api.IModelCodeCache;

import lombok.NonNull;

class TestCustomFieldTypeSnippetsForDocs {

	// tag::CustomFieldValidator[]
	class CheckCreditCardNumber implements ICustomFieldValidator {
		@Override
		public @NonNull Optional<ICustomFieldTypeCheckError> validate(
				@NonNull String value,
				@NonNull ICustomFieldTypeValidationParam valParam,
				boolean isDisplayValue
		) {
			if (!isValidCreditCardNumber(value)) {
				return createError(valParam.getErrorMsgLocale());
			}
			return Optional.empty();
		}
		// tag::exclude[]

		private static boolean isValidCreditCardNumber(@NonNull String value) {
			return value.replaceAll("\\s+", "").matches("^\\d{8,19}$");
		}

		private static Optional<ICustomFieldTypeCheckError> createError(@NonNull Locale locale) {
			return Optional.of(new ICustomFieldTypeCheckError() {
				@Override
				public @NonNull String getErrorMessage() {
					return switch (locale.getLanguage()) {
						case "de" -> "Eine Kreditkartennummer muss aus 8 bis 19 Ziffern bestehen.";
						case "en" -> "A credit card number must consist of 8 to 19 digits.";
						default -> "Unknown locale."; // in a non-test setting falling back to English would be better
					};
				}

				@Override
				public @NonNull String getErrorKey() {
					return "invalidCreditCardNumber";
				}
			});
		}
		// end::exclude[]
	}
	// end::CustomFieldValidator[]

	// tag::MyCustomFieldTypeFactory[]
	// option 1: implement the interface ICustomFieldTypeFactory
	class MyCustomFieldTypeFactory implements ICustomFieldTypeFactory {
		@Override
		public Optional<ICustomFieldValidator> createCustomFieldValidator(String customFieldTypeName) {
			if (customFieldTypeName.equals("MyCreditCardNumber")) {
				// E.g. for 'MyCreditCardNumber' an instance of 'CheckCreditCardNumber' must be created and returned
				return Optional.of(new CheckCreditCardNumber());
			}
			return Optional.empty();
		}
	}
	//end::MyCustomFieldTypeFactory[]

	@Test
	void testCustomFieldTypeFactoryFromMap() {
		// tag::CustomFieldTypeFactoryFromMap[]
		// option 2: use the simplified factory method
		ICustomFieldTypeFactory.fromMap(Map.of(
				"MyCreditCardNumber", new CheckCreditCardNumber()
		));
		// end::CustomFieldTypeFactoryFromMap[]
	}

	@Test
	void testFullValidation() {
		IDocumentModelResolver dmResolver = DocumentModelResolverFromClasspath.of(List.of(
				dmId -> "document-models/example-for-custom-field-types/" + dmId + ".json"
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
		DocumentV2 document = DocumentV2.empty("CustomFieldValidatorModel")
				.withFieldValue("PaymentInfo[1]/CreditCardNumber", "1234567812345678") // valid
				.withFieldValue("PaymentInfo[2]/CreditCardNumber", "1234 5678 1234") // valid
				.withFieldValue("PaymentInfo[3]/CreditCardNumber", "123456"); // invalid

		DocumentRtServiceFactory rtFactory = new DocumentRtServiceFactory(dmResolver);
		IDocumentRtService documentRtService = rtFactory.createDocumentRtService(serviceConfig);
		// tag::registerCustomFieldTypes[]
		// configure the custom field type factory in the DocumentProcessingConfig
		DocumentProcessingConfig processingConfig = DocumentProcessingConfig.builder(Locale.ENGLISH)
				.customFieldTypeFactory(new MyCustomFieldTypeFactory())
				.build();
		// use this DocumentProcessingConfig for API calls (validation/computation/mapping/...)
		IDocumentValidationResult validationResult = documentRtService.validateFull(document, processingConfig);
		// end::registerCustomFieldTypes[]
		Assertions.assertEquals(1, validationResult.getMessages().size());
		Assertions.assertEquals("invalidCreditCardNumber", validationResult.getMessages().getFirst().getErrorCode());
		Assertions.assertEquals("A credit card number must consist of 8 to 19 digits.",
				validationResult.getMessages().getFirst().getErrorText());
	}
}
