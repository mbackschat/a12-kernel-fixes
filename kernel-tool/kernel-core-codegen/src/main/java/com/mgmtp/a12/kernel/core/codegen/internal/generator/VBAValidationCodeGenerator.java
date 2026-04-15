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

package com.mgmtp.a12.kernel.core.codegen.internal.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.vba.utils.VBAEC;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.vba.utils.VBAFieldFilter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.vba.utils.VBAGroupFilter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.vba.utils.VBARuleFilter;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.SourceCodeCollectorV2;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.vba.VBAFieldGroupData;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporterProxy;

public class VBAValidationCodeGenerator extends ValidationCodeGenerator {
	private List<ValidationCodeGenerator> moduleGenerators = new ArrayList<>();

	private List<RuleInfo> ruleInfos;
	private VBARuleFilter ruleFilter;
	private VBAGroupFilter groupFilter;
	private VBAFieldFilter fieldFilter;

	public VBAValidationCodeGenerator(CodeGenData dataModel, IProblemReporter pr, SourceCodeCollectorV2 collector) {
		super(dataModel, pr, collector);

		if (!(data.getParams() instanceof IExtmpCodeGenParams)) {
			throw new IllegalArgumentException(
					"Code generation parameters for VBA code generation must be of type IExtmpCodeGenParams, but was " + data.getParams()
							.getClass().getName());
		}

		ruleInfos = dataModel.getRuleInfos(pr);

		this.ruleFilter = new VBARuleFilter(data, ruleInfos, pr);
		this.groupFilter = new VBAGroupFilter(data.getEc());
		this.fieldFilter = new VBAFieldFilter(data.getEc(), pr);

		Map<String, List<VBAFieldGroupData>> moduleGroups = ((IExtmpCodeGenParams) data.getParams()).getVBAModuleGroups();

		moduleGenerators = moduleGroups.entrySet().stream()
				.map(e -> createSingleModuleCodeGenerator(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}

	private ValidationCodeGenerator createSingleModuleCodeGenerator(String moduleName, List<VBAFieldGroupData> fieldGroupData) {
		Set<String> fields = fieldGroupData.stream()
				.flatMap(l -> l.getFields().stream())
				.filter(fieldFilter)
				.collect(Collectors.toSet());
		Set<String> relevantGroups = groupFilter.determineRelevantGroups(fieldGroupData);
		List<RuleInfo> partialRules = ruleFilter.filterRules(relevantGroups, fields);

		IEC partialEC = new VBAEC(data.getEc(), relevantGroups, fields, partialRules);

		CodeGenData tmpData = new CodeGenData(partialEC, data.getModelSpecificSettings(), data.getParams(), partialRules,
				data.getPss());

		ValidationCodeGenerator moduleCodeGen = new ValidationCodeGenerator(tmpData, new ProblemReporterProxy(this.problemReporter),
				this.collector);
		moduleCodeGen.setRelativePath(moduleName + "/");
		return moduleCodeGen;
	}

	@Override
	protected boolean preCheck() {
		return moduleGenerators.stream().allMatch(ModelBasedCodeGenerator::preCheck);
	}

	@Override
	protected boolean createTemplateParameterClasses() {
		return moduleGenerators.stream().allMatch(ModelBasedCodeGenerator::createTemplateParameterClasses);
	}

	@Override
	protected void createEntries() {
		moduleGenerators.forEach(ModelBasedCodeGenerator::createEntries);
	}

}
