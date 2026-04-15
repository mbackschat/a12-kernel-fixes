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

package com.mgmtp.a12.kernel.md.structuralmapping.internal.util;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.model.StructuralMappingModelForCodegen;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAwareDocServiceImpl;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.ExternalDocumentModelSerializerService;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.MMDocumentModelLoader;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingMetaModelService;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingModelService;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.GroupsToClearOnFirstFill;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.MappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.FieldMappings;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.ResolutionStrategies;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks._resolutionstrategies.type;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * This service provides functions related to structural mapping models.
 */
@Accessors(fluent = true)
public class StructuralMappingModelUtil {
	private static final String MM_CODEGEN = "extmpinternal/codegen-mm_structuralmappingmodel-1.json";

	private StructuralMappingModelUtil() {
		throw new UnsupportedOperationException("Should not be instantiated");
	}

	public static Stream<FieldMappings> streamAllFieldMappings(MM_StructuralMappingModel_1 smm) {
		return smm._at(MM_StructuralMappingModel_1._pointer().content().mappingBlocks()).stream()
				.flatMap(mb -> mb._at(MappingBlocks._pointer().fieldMappings()).stream());
	}

	public static Stream<ResolutionStrategies> streamAllResolutionStrategies(MM_StructuralMappingModel_1 smm) {
		return smm._at(MM_StructuralMappingModel_1._pointer().content().mappingBlocks()).stream()
				.flatMap(mb -> mb._at(MappingBlocks._pointer().resolutionStrategies()).stream());
	}

	public static MM_StructuralMappingModel_1 createSMM(String name, @NonNull List<GroupsToClearOnFirstFill> groupsToClear,
			@NonNull List<MappingBlocks> mappingBlocks) {
		return StructuralMappingMetaModelService.createEmptyModel(name)
				._with(MM_StructuralMappingModel_1._pointer().content().groupsToClearOnFirstFill(), groupsToClear)
				._with(MM_StructuralMappingModel_1._pointer().content().mappingBlocks(), mappingBlocks);
	}

	public static GroupsToClearOnFirstFill createGroupToClear(String fullName) {
		return GroupsToClearOnFirstFill._empty()
				._with(GroupsToClearOnFirstFill._pointer().fullName(), fullName);
	}

	public static MappingBlocks createMappingBlock(@NonNull List<ResolutionStrategies> resolutionStrategies,
			@NonNull List<FieldMappings> fieldMappings) {
		return MappingBlocks._empty()
				._with(MappingBlocks._pointer().resolutionStrategies(), resolutionStrategies)
				._with(MappingBlocks._pointer().fieldMappings(), fieldMappings);
	}

	public static ResolutionStrategies createSlice(String sourceGroupFullName, String targetGroupFullName,
			String sourceFieldFullName, String targetFieldFullName) {
		return ResolutionStrategies._empty()
				._with(ResolutionStrategies._pointer().type(), type.Slice)
				._with(ResolutionStrategies._pointer().sourceGroupFullName(), sourceGroupFullName)
				._with(ResolutionStrategies._pointer().targetGroupFullName(), targetGroupFullName)
				._with(ResolutionStrategies._pointer().slice().sourceFieldFullName(), sourceFieldFullName)
				._with(ResolutionStrategies._pointer().slice().targetFieldFullName(), targetFieldFullName);
	}

	public static ResolutionStrategies createFold(String sourceGroupFullName, String targetGroupFullName) {
		return ResolutionStrategies._empty()
				._with(ResolutionStrategies._pointer().type(), type.Fold)
				._with(ResolutionStrategies._pointer().sourceGroupFullName(), sourceGroupFullName)
				._with(ResolutionStrategies._pointer().targetGroupFullName(), targetGroupFullName);
	}

	public static FieldMappings createFieldMapping(String sourceFieldFullName, String targetFieldFullName) {
		return FieldMappings._empty()
				._with(FieldMappings._pointer().sourceFieldFullName(), sourceFieldFullName)
				._with(FieldMappings._pointer().targetFieldFullName(), targetFieldFullName);
	}

	/**
	 * @param notificationReceiver
	 * 		will receive corresponding notifications if the SMM is not valid or can not be constructed at all
	 * @return the valid StructuralMappingModelForCodegen, or null if no valid structural mapping model could be constructed from
	 * 		the given
	 * 		parameters
	 */
	public static StructuralMappingModelForCodegen createAndPrepareValidStructuralMappingModel(
			@NonNull MM_StructuralMappingModel_1 smm,
			@NonNull DocumentModel sourceDm,
			@NonNull DocumentModel targetDm,
			@NonNull Consumer<? super RankedNotification> notificationReceiver,
			@NonNull Locale localeForNotifications
	) {
		DocumentModelSearchService sourceDMSS = new DocumentModelSearchService(sourceDm);
		DocumentModelSearchService targetDMSS = new DocumentModelSearchService(targetDm);

		IDocumentModelResolver modelResolver = MMDocumentModelLoader.createResolver(MM_CODEGEN);
		DocumentV2 codegenSmmDm = DmAwareDocServiceImpl.ofResolver(MM_CODEGEN, modelResolver)
				.removeUnknowns(smm._unwrap());
		MM_StructuralMappingModel_1 codegenSMM = MM_StructuralMappingModel_1._viewOf(codegenSmmDm);

		boolean isValid = StructuralMappingModelService.create(sourceDm, targetDm)
				.checkConsistencyFull(codegenSMM, notificationReceiver, localeForNotifications);
		if (!isValid) {
			return null;
		}
		return BuildStructuralMappingModelForCodegen.run(codegenSMM, sourceDMSS::getByPath, targetDMSS::getByPath);
	}

	/**
	 * @param notificationReceiver
	 * 		will receive corresponding notifications if the SMM is not valid or can not be constructed at all
	 * @return the valid StructuralMappingModelForCodegen, or null if no valid structural mapping model could be constructed from
	 * 		the given
	 * 		parameters
	 */
	public static StructuralMappingModelForCodegen createAndPrepareValidStructuralMappingModel(
			@NonNull MM_StructuralMappingModel_1 smm, @NonNull IDocumentModel sourceDm, @NonNull IDocumentModel targetDm,
			@NonNull Consumer<? super RankedNotification> notificationReceiver, @NonNull Locale localeForNotifications) {
		return createAndPrepareValidStructuralMappingModel(smm,
				ExternalDocumentModelSerializerService.getInternDocumentModel(sourceDm),
				ExternalDocumentModelSerializerService.getInternDocumentModel(targetDm), notificationReceiver,
				localeForNotifications);
	}
}
