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

package com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services;

import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentComputationResult;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.internal.service.Identifier;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAwareDocServiceImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.MMDocumentModelLoader;
import com.mgmtp.a12.kernel.mmtypings.ct_model_header_1.views._ct_model_header_1.Header;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.kernel.mmvalidators.extmpinternal.codegen_mm_structuralmappingmodel__0031.Factory_codegen_mm_structuralmappingmodel__0031;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@OnlyForUsage
public class StructuralMappingMetaModelService {
	private static final String META_MODEL_DM_NAME = "extmpinternal/none-mm_structuralmappingmodel-1.json";
	private static final String META_MODEL_CODEGEN_DM_NAME = "extmpinternal/codegen-mm_structuralmappingmodel-1.json";

	@Getter(lazy = true)
	private static final IDocumentModel documentModel = MMDocumentModelLoader.deserializeMMDocumentModel(META_MODEL_DM_NAME);
	@Getter
	private static final IDocumentModelResolver modelResolver = MMDocumentModelLoader.createResolver(documentModel());
	@Getter
	private static final IDocumentV2Serializer documentSerializer = new DocumentSerializerImpl(modelResolver());

	@Getter(lazy = true)
	private static final Header computedHeader = computeAndApply(MM_StructuralMappingModel_1._empty(), Locale.US).header();

	private static Header createComputedHeaderWithId(String id) {
		return computedHeader()._with(Header._pointer().id(), id);
	}

	private StructuralMappingMetaModelService() {
		throw new UnsupportedOperationException("Should not be instantiated");
	}

	/**
	 * See {@link IDocumentV2Serializer#serializeV2(DocumentV2, Writer, DocumentSerializationConfig)}.
	 */
	public static void serialize(@NonNull MM_StructuralMappingModel_1 document, @NonNull Writer writer) {
		documentSerializer.serializeV2(document._unwrap(), writer, DocumentSerializationConfig.builder().build());
	}

	/**
	 * See {@link IDocumentV2Serializer#deserializeV2(Reader, String, DocumentDeserializationConfig, Consumer)}.
	 */
	public static @NonNull MM_StructuralMappingModel_1 deserialize(@NonNull Reader reader,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		DocumentV2 document = documentSerializer.deserializeV2(
				reader,
				documentModel().getHeader().getId(),
				DocumentDeserializationConfig.builder().build(),
				notificationReceiver::accept);
		return MM_StructuralMappingModel_1._viewOf(document);
	}

	public static IDocumentValidationResult validate(@NonNull MM_StructuralMappingModel_1 document, @NonNull Locale locale) {
		IDocumentModelResolver modelResolver = MMDocumentModelLoader.createResolver(META_MODEL_CODEGEN_DM_NAME);
		DocumentV2 codegenDm = DmAwareDocServiceImpl.ofResolver(META_MODEL_CODEGEN_DM_NAME, modelResolver)
				.removeUnknowns(document._unwrap());

		return LowKernelApiUtils.callValidator(
				LowKernelApiUtils.ValidationMethod.FULL_VALIDATION,
				Factory_codegen_mm_structuralmappingmodel__0031.getInstance().getValidator(),
				Factory_codegen_mm_structuralmappingmodel__0031.getInstance().getMetaObject(),
				codegenDm,
				null,
				DocumentProcessingConfig.builder(locale).build(),
				id -> new Identifier(id.getName(), id.getIndices()).toString());
	}

	public static IDocumentComputationResult compute(@NonNull MM_StructuralMappingModel_1 document, @NonNull Locale locale) {
		return LowKernelApiUtils.callCalculator(
				Factory_codegen_mm_structuralmappingmodel__0031.getInstance().getCalculator(),
				Factory_codegen_mm_structuralmappingmodel__0031.getInstance().getMetaObject(),
				document._unwrap(),
				DocumentProcessingConfig.builder(locale).build(),
				id -> new Identifier(id.getName(), id.getIndices()).toString());
	}

	public static MM_StructuralMappingModel_1 computeAndApply(@NonNull MM_StructuralMappingModel_1 document,
			@NonNull Locale locale) {
		return MM_StructuralMappingModel_1._viewOf(compute(document, locale).applyTo(document._unwrap()));
	}

	public static MM_StructuralMappingModel_1 createEmptyModel(@NonNull String id) {
		return MM_StructuralMappingModel_1._empty()
				._with(MM_StructuralMappingModel_1._pointer().header(), createComputedHeaderWithId(id));
	}

	public static String modelVersion() {
		return computedHeader().modelVersion();
	}

	public static String modelType() {
		return computedHeader().modelType();
	}
}
