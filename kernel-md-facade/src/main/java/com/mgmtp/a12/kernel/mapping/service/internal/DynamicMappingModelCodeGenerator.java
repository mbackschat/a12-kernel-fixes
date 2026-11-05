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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.ICodeGenResult;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.IMergedSingleScriptCodeGenResult;
import com.mgmtp.a12.kernel.mapping.facade.DynamicMappingServiceConfig;
import com.mgmtp.a12.kernel.mapping.facade.IMappingModelResolver;
import com.mgmtp.a12.kernel.mapping.facade.internal.ThrowingMappingModelResolverWrapper;
import com.mgmtp.a12.kernel.md.datatransfer.internal.AbstractDataTransferCodeGenerator;
import com.mgmtp.a12.kernel.md.datatransfer.internal.DataTransferCodeGenerator;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.api.services.internal.CodeGeneratorConfigV1_2Impl;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.RankedNotificationUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.RankedNotificationsException;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;

/**
 * Generate model specific code and cache the result.
 */
public class DynamicMappingModelCodeGenerator {

	private final IMappingModelResolver modelResolver;

	public DynamicMappingModelCodeGenerator(DynamicMappingServiceConfig serviceConfig) {
		this.modelResolver = ThrowingMappingModelResolverWrapper.wrap(serviceConfig.mappingModelResolver());
	}

	public DataTransferModelGroovyCode generateCode(final String mappingModelId) {
		MM_MappingModel_2 mappingModel = modelResolver.getMappingModelById(mappingModelId);
		String generatedPlainCode = doCodeGeneration(mappingModel);
		return new DataTransferModelGroovyCode(generatedPlainCode);
	}

	private String doCodeGeneration(MM_MappingModel_2 mappingModel) {
		Function<String, MM_StructuralMappingModel_1> structuralMappingModelResolver = this.modelResolver::getStructuralMappingById;

		CodeGeneratorConfigV1_2Impl codeGenConfig = CodeGeneratorConfigV1_2Impl.of(
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

}
