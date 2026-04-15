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

package com.mgmtp.a12.kernel.md.combination.a12internal;

import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.internal.impl.RankedNotificationImpl;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.internal.service.Identifier;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAwareDocServiceImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.MMDocumentModelLoader;
import com.mgmtp.a12.kernel.mmtypings.mm_selectionmodel_2.views.MM_SelectionModel_2;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.utils.Experimental;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.kernel.mmvalidators.extmpinternal.codegen_mm_selectionmodel__0032.Factory_codegen_mm_selectionmodel__0032;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Experimental
@OnlyForUsage
public class SelectionModelService {
	public static final String MODEL_TYPE = "selection";
	public static final String MODEL_VERSION = "2.0.0";
	private static final String META_MODEL_DM_NAME = "extmpinternal/none-mm_selectionmodel-2.json";
	private static final String META_MODEL_CODEGEN_DM_NAME = "extmpinternal/codegen-mm_selectionmodel-2.json";

	@Getter(value = AccessLevel.PRIVATE, lazy = true)
	private static final IDocumentModel metaModel = MMDocumentModelLoader.deserializeMMDocumentModel(META_MODEL_DM_NAME);
	@Getter(value = AccessLevel.PRIVATE, lazy = true)
	private static final IDocumentModelResolver metaModelResolver = MMDocumentModelLoader.createResolver(metaModel());
	@Getter(value = AccessLevel.PRIVATE, lazy = true)
	private static final IDocumentV2Serializer documentSerializer = new DocumentSerializerImpl(metaModelResolver());

	/**
	 * Fills all computable fields of the provided {@link MM_SelectionModel_2 SelectionModel}.
	 */
	public static @NonNull MM_SelectionModel_2 fillComputableFields(@NonNull MM_SelectionModel_2 selectionModel) {
		return selectionModel
				._with(MM_SelectionModel_2._pointer().header().modelType(), MODEL_TYPE)
				._with(MM_SelectionModel_2._pointer().header().modelVersion(), MODEL_VERSION);
	}

	/**
	 * Check to see if the structure of the selection model is valid.
	 * <p>
	 * This executes the validation rules of the {@link MM_SelectionModel_2 SelectionModel}.
	 * <p>
	 * Does not perform checks that are specific to a particular DM to which the selection is to be applied.
	 */
	public static boolean checkConsistencyStandalone(@NonNull MM_SelectionModel_2 selectionModelV2,
			@NonNull Locale locale, @NonNull Consumer<? super RankedNotification> notificationReceiver) {
		IDocumentModelResolver modelResolver = MMDocumentModelLoader.createResolver(META_MODEL_CODEGEN_DM_NAME);
		DocumentV2 codegenDm = DmAwareDocServiceImpl.ofResolver(META_MODEL_CODEGEN_DM_NAME, modelResolver)
				.removeUnknowns(selectionModelV2._unwrap());

		IDocumentValidationResult documentValidationResult = LowKernelApiUtils.callValidator(
				LowKernelApiUtils.ValidationMethod.FULL_VALIDATION,
				Factory_codegen_mm_selectionmodel__0032.getInstance().getValidator(),
				Factory_codegen_mm_selectionmodel__0032.getInstance().getMetaObject(),
				codegenDm,
				null,
				DocumentProcessingConfig.builder(locale).build(),
				id -> new Identifier(id.getName(), id.getIndices()).toString());

		documentValidationResult.getMessages()
				.forEach(m -> notificationReceiver.accept(
						RankedNotificationImpl.of(m.getErrorText(), m.getSeverity().toBaseSeverity(), m)));

		return documentValidationResult.noErrorOccurred();
	}

	/**
	 * Deserialize a {@link MM_SelectionModel_2 SelectionModel} using the provided reader.
	 */
	public static @NonNull MM_SelectionModel_2 deserialize(@NonNull Reader reader,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		DocumentDeserializationConfig config = DocumentDeserializationConfig.builder().build();
		DocumentV2 documentV2 = documentSerializer().deserializeV2(reader, metaModel().getHeader().getId(), config,
				notificationReceiver::accept);
		return MM_SelectionModel_2._viewOf(documentV2);
	}

	/**
	 * Deserialize a {@link MM_SelectionModel_2 SelectionModel} from a {@link JsonNode}.
	 */
	public static @NonNull MM_SelectionModel_2 deserialize(@NonNull JsonNode jsonNode,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		DocumentDeserializationConfig config = DocumentDeserializationConfig.builder().build();
		DocumentV2 documentV2 = documentSerializer().deserializeV2(jsonNode, metaModel().getHeader().getId(), config,
				notificationReceiver::accept);
		return MM_SelectionModel_2._viewOf(documentV2);
	}

	/**
	 * Serialize the {@link MM_SelectionModel_2 SelectionModel} to Json.
	 */
	public static void serialize(@NonNull MM_SelectionModel_2 selectionModel, @NonNull Writer writer) {
		DocumentSerializationConfig config = DocumentSerializationConfig.builder().build();
		DocumentV2 document = selectionModel._unwrap();

		documentSerializer().serializeV2(document, writer, config);
	}
}
