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

package com.mgmtp.a12.kernel.md.datatransfer.internal;

import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.appendList;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenContext;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.ValidationCodeGenerator;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.ICodeGenResult;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.IMergedSingleScriptCodeGenResult;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.ISeparateFilesCodeGenResult;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelForCodegen;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.a12internal.ICodeGeneratorConfigV1_2;
import com.mgmtp.a12.kernel.md.model.internal.service.codegen.MdCoreConversionUtil;
import com.mgmtp.a12.kernel.md.structuralmapping.codegen.internal.StructuralMappingCodeGenerationService.StructuralMappingEntryPoint;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;

public class DataTransferCodeGenerator extends
		AbstractDataTransferCodeGenerator<
				IDocumentModelResolver,
				ICodeGeneratorConfigV1_2,
				RankedNotification
				> {
	/**
	 * see
	 * {@link #generateDataTransferCode(MM_MappingModel_2, IDocumentModelResolver, Function, ICodeGeneratorConfigV1_2, Consumer,
	 * Locale, boolean)}
	 */
	public static ICodeGenResult<DataTransferEntryPoint> createCodeForMappingModel(
			MM_MappingModel_2 mappingModel,
			// note: the mappingmodel has already been validated during construction of the MappingModelForCodegen
			IDocumentModelResolver resolver,
			Function<String, MM_StructuralMappingModel_1> structuralMappingModelResolver,
			ICodeGeneratorConfigV1_2 codegenConfig,
			Consumer<RankedNotification> notificationReceiver,
			Locale localeForSomeButNotAllProblems,
			boolean addtargetWithoutRulesMetaCode
	) {
		return new DataTransferCodeGenerator().generateDataTransferCode(
				mappingModel, resolver, structuralMappingModelResolver, codegenConfig,
				notificationReceiver, localeForSomeButNotAllProblems, addtargetWithoutRulesMetaCode);
	}

	@Override protected Consumer<RankedNotification> wrapNotificationReceiver(Consumer<RankedNotification> notificationReceiver) {
		return notificationReceiver;
	}

	@Override protected ICodeGenResult<DataTransferEntryPoint> generateFactory(
			MappingModelForCodegen mappingModel,
			IDocumentModelResolver resolver,
			List<String> packageName,
			PrecompMapperEntryPoint precompMapperEntryPoint,
			ValidationCodeGenerator.ValidationEntryPoint precomputationEntryPoints,
			Map<String, ValidationCodeGenerator.ValidationEntryPoint> entryPointsForSourceValidators,
			StructuralMappingEntryPoint structuralMappingEntryPoint,
			ValidationCodeGenerator.ValidationEntryPoint targetWithoutRulesEntryPointIfAvailable,
			ICodeGeneratorConfigV1_2 codegenConfig) {
		var programmingLanguage = MdCoreConversionUtil.convertProglangMdToCore(codegenConfig.getProgrammingLanguage());
		CodeGenContext.initialize(
				ICodeGeneratorParameters.Product.DATATRANSFER,
				programmingLanguage);
		var factoryName = "DataTransferFactory";
		List<String> factoryFqn = appendList(packageName, factoryName);
		DataTransferFactoryTmpl tpl = new DataTransferFactoryTmpl(
				packageName, factoryName, entryPointsForSourceValidators,
				mappingModel, precompMapperEntryPoint, precomputationEntryPoints,
				structuralMappingEntryPoint,
				targetWithoutRulesEntryPointIfAvailable);
		return switch (programmingLanguage) {
			case JAVA -> ISeparateFilesCodeGenResult.ofSingleFileUtf8(factoryFqn, DataTransferEntryPoint::new /*ignore optional target meta for now*/, ".java", tpl.toCode());
			case GROOVY -> {
				DataTransferEntryPoint entryPoint = new DataTransferEntryPoint(factoryFqn);
				yield IMergedSingleScriptCodeGenResult.of(
					entryPoint,
					"", // no header (e.g. copyright notice) at the moment
					Arrays.stream(tpl.toCode("import").split("\n")).collect(Collectors.toSet()),
					tpl.toCode("globals"),
					List.of(tpl.toCode()),
					makeGroovyFilenamePath(entryPoint)
				);
			}
			default -> throw new NotImplementedException("datatransfer codegen is only implemented for java and groovy, got " + programmingLanguage);
		};
	}
}
