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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenRuleConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.MetaRegelFehlertexte;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.MetaRegelFehlertexteBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.UniqueNameService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.services.ParseBaumBedingungsServices;
import com.mgmtp.a12.kernel.core.service.internal.extservices.MandatoryInformationService;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporterProxy;

/**
 * Builder for the parameter class for the template of the R_ classes containing the validation
 * rules code.
 */
public class CodeGenRulesBuilder extends CodeGenEREntitiesBuilder<CodeGenData> {

	private List<CodeGenRuleDefinition> ruleDefinitions;
	private List<RuleInfo> ruleInfos;
	private List<MetaRegelFehlertexte> ruleFehlertexte = new ArrayList<>();
	private final boolean expandGroups;

	public CodeGenRulesBuilder(CodeGenData data, ProblemReporterProxy problemReporter, PackageInformation packageInfo,
			boolean expandGroups) {
		super(data, packageInfo, problemReporter);

		this.expandGroups = expandGroups;

		this.ruleInfos = new ArrayList<>(data.getRuleInfos(problemReporter));
		if (problemReporter.hasErrorOccurred()) {
			return;
		}

		List<RuleInfo> autogenRuleInfos = data.getAutogenRuleInfos(problemReporter);
		if (problemReporter.hasErrorOccurred()) {
			return;
		}
		ruleInfos.addAll(autogenRuleInfos);

		Set<String> ruleFullNames = ruleInfos.stream()
				.map(RuleInfo::getFullName)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Map<String, String> uniqueRulesName = UniqueNameService.makeUniqueRuleNames(ruleFullNames);
		this.ruleDefinitions = this.ruleInfos.stream()
				.map(r -> addMetaFehlertextAndCreateCodeGenRuleDefinitionBuilder(r, uniqueRulesName).build())
				.collect(Collectors.toList());
	}

	private CodeGenRuleDefinitionBuilder addMetaFehlertextAndCreateCodeGenRuleDefinitionBuilder(RuleInfo ruleInfo,
			Map<String, String> uniqueRulesName) {
		MetaRegelFehlertexte metaRegelFehlertexte = null;
		metaRegelFehlertexte = new MetaRegelFehlertexteBuilder(ruleInfo, data).build();
		this.ruleFehlertexte.add(metaRegelFehlertexte);
		return new CodeGenRuleDefinitionBuilder(ruleInfo, data, uniqueRulesName, metaRegelFehlertexte, expandGroups);
	}

	public SortedSet<String> getApplBedNames() {
		SortedSet<String> applNames = new TreeSet<>();
		for (RuleInfo ri : ruleInfos) {
			applNames.addAll(ParseBaumBedingungsServices.getApplikationsBedingungsnamen(ri.getCondition()));
		}
		return applNames;
	}

	public MandatoryInformationService getMandatoryInformation() {
		SortedMap<IRule, ICondition> sortedRuleToCondition = new TreeMap<>(
				(r1, r2) -> r1.getFullName().compareTo(r2.getFullName()));

		for (RuleInfo rule : ruleInfos) {
			sortedRuleToCondition.put(rule.getRule(), rule.getCondition());
		}

		return MandatoryInformationService.createInstance(sortedRuleToCondition, data.getEc());
	}

	@Override
	public CodeGenRules build() {
		return new CodeGenRules(this);
	}

	public List<CodeGenRuleDefinition> getRuleDefinitions() {
		return ruleDefinitions;
	}

	public List<RuleInfo> getRules() {
		return ruleInfos;
	}

	public List<MetaRegelFehlertexte> getRegelFehlertexte() {
		return ruleFehlertexte;
	}

	public CollectedRulesPerRootGroupCreator getCollectedRulesPerRootGroupCreator() {
		return new CollectedRulesPerRootGroupCreator(data, ruleInfos, ruleDefinitions);
	}

	public List<CodeGenRuleCheckObject> getCheckObjects() {
		List<CodeGenRuleCheckObject> checkObjects = new ArrayList<>();
		CodeGenRuleCheckObject curCheckObject = null;
		int checkObjectNum = 0;
		String modelName = data.getParams().getName();
		for (int i = 0; i < ruleDefinitions.size(); i++) {
			if (i % CodeGenRuleConstants.NUM_RULES_PER_CLASS_GROOVY == 0) {
				checkObjectNum++;
				curCheckObject = new CodeGenRuleCheckObject(checkObjectNum, modelName,
						packageInformation.getPackageAsGroovyClassnamePrefix());
				checkObjects.add(curCheckObject);
			}
			curCheckObject.addRule(ruleDefinitions.get(i));
		}
		return checkObjects;
	}

	public int getNumRuleFilesCpp() {
		return SplitFunctionsHelper.computeNumSplits(ruleDefinitions.size(), CodeGenRuleConstants.NUM_RULES_PER_FILE_CPP);
	}
}
