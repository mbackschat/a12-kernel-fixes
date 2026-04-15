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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenRuleConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenEREntitiesBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.CodeGenUtilities;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IDynamicEnumerationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/**
 * Builder for the parameter class for the templates of Meta_Regel_ classes.
 */
public class CodeGenMetaRulesBuilder extends CodeGenEREntitiesBuilder<CodeGenData> {

	private final Map<String, Integer> fieldIds;
	private final ArrayList<IMetaInfoCodeGenParams> metaCodeGenParams;
	private List<MetaRegelReferenzen> ruleReferences;
	private final Map<String, ICalculationInfo> serverCalculationInfo4RuleName;
	private final Map<String, IDynamicEnumerationInfo> dynamicEnumerationInfo4RuleName;
	private List<MetaRegelFehlertexte> ruleFehlertexte;

	public CodeGenMetaRulesBuilder(CodeGenData data, IProblemReporter problemReporter, PackageInformation packageInfo,
			List<RuleInfo> ruleInfos,
			List<MetaRegelFehlertexte> ruleFehlertexte, Map<String, Integer> fieldIds) {
		super(data, packageInfo, problemReporter);
		this.fieldIds = fieldIds;

		List<? extends ICalculationInfo> serverCalculationInfo = data.getParams().getCalculationInformation();
		this.serverCalculationInfo4RuleName = serverCalculationInfo != null
				? serverCalculationInfo.stream().collect(Collectors.toMap(ICalculationInfo::getName, Function.identity()))
				: Collections.emptyMap();

		List<? extends IDynamicEnumerationInfo> dynamicEnumerationInfo = data.getParams().getDynamicEnumerationInformation();
		this.dynamicEnumerationInfo4RuleName = dynamicEnumerationInfo.stream()
				.collect(Collectors.toMap(IDynamicEnumerationInfo::getName, Function.identity()));

		this.ruleReferences = ruleInfos.stream().map(this::createMetaRegelReferenzen).collect(Collectors.toList());
		this.ruleFehlertexte = ruleFehlertexte;

		this.metaCodeGenParams = new ArrayList<>(data.getParams().getMetaInfoCodeGenParams());
	}

	private MetaRegelReferenzen createMetaRegelReferenzen(RuleInfo ruleInfo) {
		boolean isServerCalculation = serverCalculationInfo4RuleName.containsKey(ruleInfo.getRule().getFullName());
		boolean isDynamicEnumeration = dynamicEnumerationInfo4RuleName.containsKey(ruleInfo.getRule().getFullName());
		return new MetaRegelReferenzenBuilder(ruleInfo, data, fieldIds, isServerCalculation,
				isDynamicEnumeration, problemReporter).build();
	}

	public boolean isFullMetaCode() {
		return CodeGenUtilities.isFullMetaCode(data);
	}

	public List<MetaRegelReferenzen> getRuleReferences() {
		return ruleReferences;
	}

	@Override
	public CodeGenMetaRules build() {
		return new CodeGenMetaRules(this);
	}

	public List<MetaRegelMethod> getMetaRegelMethods() {
		return ruleReferences.stream()
				.map(this::createMetaRegelMethodObject)
				.collect(Collectors.toList());
	}

	protected MetaRegelMethod createMetaRegelMethodObject(MetaRegelReferenzen ruleReference) {
		return new MetaRegelMethod(ruleReference, metaCodeGenParams);
	}

	public List<MetaRegelFehlertexte> getRegelFehlertexte() {
		return ruleFehlertexte;
	}

	public Integer getNumMetaRulesPerFile() {
		ProgrammingLanguage language = data.getParams().getProgrammingLanguage();
		return CodeGenRuleConstants.NUM_METARULES_PER_FILE.get(language);
	}

	public int getNumMetaRuleFiles() {
		Integer rulesPerFile = getNumMetaRulesPerFile();
		if (rulesPerFile == null) {
			return 1;
		}
		return SplitFunctionsHelper.computeNumSplits(ruleReferences.size(), rulesPerFile);
	}
}
