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

import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.ValidationCodeGenerator;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BaseCodeGenUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelForCodegen;
import com.mgmtp.a12.kernel.md.structuralmapping.codegen.internal.StructuralMappingCodeGenerationService.StructuralMappingEntryPoint;

@StringTemplateGroup("base")
public class DataTransferFactoryTmpl extends STParameterClassBase {
	private final List<String> packageName;
	private final String factoryName;
	private final Map<String, ValidationCodeGenerator.ValidationEntryPoint> entryPointsForSourceValidators;
	protected final MappingModelForCodegen mappingModel;
	private final AbstractDataTransferCodeGenerator.PrecompMapperEntryPoint precompMapperEntryPoint;
	private final ValidationCodeGenerator.ValidationEntryPoint precomputationEntryPoints;
	private final StructuralMappingEntryPoint structuralMappingEntryPoint;
	private final ValidationCodeGenerator.ValidationEntryPoint targetWithoutRulesEntryPointIfAvailable;

	public DataTransferFactoryTmpl(List<String> packageName, String factoryName,
			Map<String, ValidationCodeGenerator.ValidationEntryPoint> entryPointsForSourceValidators,
			MappingModelForCodegen mappingModel, AbstractDataTransferCodeGenerator.PrecompMapperEntryPoint precompMapperEntryPoint,
			ValidationCodeGenerator.ValidationEntryPoint precomputationEntryPoints,
			StructuralMappingEntryPoint structuralMappingEntryPoint,
			ValidationCodeGenerator.ValidationEntryPoint targetWithoutRulesEntryPointIfAvailable) {
		this.packageName = packageName;
		this.factoryName = factoryName;
		this.entryPointsForSourceValidators = entryPointsForSourceValidators;
		this.mappingModel = mappingModel;
		this.precompMapperEntryPoint = precompMapperEntryPoint;
		this.precomputationEntryPoints = precomputationEntryPoints;
		this.structuralMappingEntryPoint = structuralMappingEntryPoint;
		this.targetWithoutRulesEntryPointIfAvailable = targetWithoutRulesEntryPointIfAvailable;
	}

	public String getPackageName() {
		return String.join(".", packageName);
	}

	public String getFactoryName() {
		return factoryName;
	}

	public String groovyFactoryName() {
		return BaseCodeGenUtils.encodeToGroovyClassname(Java8Utils.appendList(packageName, factoryName));
	}

	public String getMetaValidierungMapEntries() {
		return AbstractDataTransferCodeGenerator.makeMetaValidierungMapEntries(entryPointsForSourceValidators);
	}

	public Object getValidatorMapEntries() {
		return AbstractDataTransferCodeGenerator.makeValidatorMapEntries(entryPointsForSourceValidators);
	}

	public String getNoSourceValidationSources() {
		return AbstractDataTransferCodeGenerator.makeNoSourceValidationSources(mappingModel);
	}

	public String getSerializedPrecompModelDataMapperResourcePath() {
		return String.join("/", precompMapperEntryPoint.serializedMapperResourceFqn());
	}

	public String getSerializedPrecompModelClassGroovy() {
		return BaseCodeGenUtils.encodeToGroovyClassname(precompMapperEntryPoint.serializedMapperResourceFqn());
	}

	public String getPrecompCalculatorFqn() {
		return String.join(".", precomputationEntryPoints.calculatorFqn());
	}

	public String getPrecompMetaFqn() {
		return String.join(".", precomputationEntryPoints.metaFqn());
	}

	public String getStructural_mapper_Fqn() {
		return String.join(".", structuralMappingEntryPoint.structualMapperVK());
	}

	public String getPrecompCalculatorFqn_Groovy() {
		return BaseCodeGenUtils.encodeToGroovyClassname(precomputationEntryPoints.calculatorFqn());
	}

	public String getPrecompMetaFqn_Groovy() {
		return BaseCodeGenUtils.encodeToGroovyClassname(precomputationEntryPoints.metaFqn());
	}

	public String getStructural_mapper_Fqn_Groovy() {
		return BaseCodeGenUtils.encodeToGroovyClassname(structuralMappingEntryPoint.structualMapperVK());
	}

	public String getTargetMetaClassname() {
		if (targetWithoutRulesEntryPointIfAvailable == null) {
			throw new RuntimeException("targetWithoutRulesMetaIfAvailable is not available for this generated code");
		}
		return BaseCodeGenUtils.encodeToGroovyClassname(targetWithoutRulesEntryPointIfAvailable.metaFqn());
	}
}
