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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParamUtil;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule.RuleSeverityType;

/**
 * Creates the methods and calls necessary for the separation of rules into groups for the
 * individual root groups.
 * <p>
 * The output is performed by instances of the template backing bean class {@link RulesInRootGroup},
 * that is used intensively here.
 */
public class CollectedRulesPerRootGroupCreator {

	private static final String GLOBAL_ROOTGROUP = "global";
	private static final String ERROR_POSTFIX = "F";
	private static final String HINT_POSTFIX = "H";
	private static final String INFO_POSTFIX = "I";
	private static final String PRELIMINARY_POSTFIX = "P";

	// For user specified rules
	private final List<RulesInRootGroup> allRules4RootGroup;
	private final List<RulesInRootGroup> errorRules4RootGroup;
	private final List<RulesInRootGroup> warningRules4RootGroup;
	private final List<RulesInRootGroup> infoRules4RootGroup;

	// For preliminary rules called between formal check and rule evaluation
	private final List<RulesInRootGroup> preliminaryRules4RootGroup;

	public CollectedRulesPerRootGroupCreator(CodeGenData data, List<RuleInfo> rules, List<CodeGenRuleDefinition> codeGenRuleDefs) {
		Map<String, CodeGenRuleDefinition> codeGenRules4Name = codeGenRuleDefs.stream()
				.collect(Collectors.toMap(CodeGenRuleDefinition::getFullRuleName, Function.identity()));

		Map<Boolean, List<RuleInfo>> preliminarySeparatedRules = rules.stream()
				.collect(Collectors.partitioningBy(RuleInfo::isPreliminary));
		List<RuleInfo> preliminaryRules = preliminarySeparatedRules.get(true);
		List<RuleInfo> nonPreliminaryRules = preliminarySeparatedRules.get(false);

		// Root group mapping for all rules
		LinkedHashMap<String, List<RuleInfo>> rulesPerRootGroup = nonPreliminaryRules.stream()
				.collect(Java8Utils.groupingByLinkedHashMap(this::getRootGroup4Rule));
		// Put global group to first position
		Map<String, List<RuleInfo>> rulesPerRootGroupGlobalFirst = Java8Utils.putKeyToStart(rulesPerRootGroup, GLOBAL_ROOTGROUP);
		allRules4RootGroup = rulesPerRootGroupGlobalFirst.entrySet().stream()
				.map(e -> createRulesInRootGroup(null, "", data, e.getKey(), e.getValue(), codeGenRules4Name))
				.toList();

		Map<RuleSeverityType, LinkedHashMap<String, List<RuleInfo>>> rulesPerRootGroupAndSeverity = nonPreliminaryRules.stream()
				.collect(Collectors.groupingBy(r -> r.getRule().getSeverityType(),
						Java8Utils.groupingByLinkedHashMap(this::getRootGroup4Rule)));
		// Put all global entries to first position
		rulesPerRootGroupAndSeverity = Java8Utils.mapMapValues(rulesPerRootGroupAndSeverity,
				m -> Java8Utils.putKeyToStart(m, GLOBAL_ROOTGROUP));
		// Root group mapping for error rules
		Map<String, List<RuleInfo>> errorRulesPerRootGroup = rulesPerRootGroupAndSeverity.getOrDefault(RuleSeverityType.ERROR,
				new LinkedHashMap<>());
		errorRules4RootGroup = errorRulesPerRootGroup.entrySet().stream()
				.map(e -> createRulesInRootGroup(RuleSeverityType.ERROR, ERROR_POSTFIX, data, e.getKey(), e.getValue(),
						codeGenRules4Name))
				.toList();
		// Root group mapping for warning rules
		Map<String, List<RuleInfo>> warningRulesPerRootGroup = rulesPerRootGroupAndSeverity.getOrDefault(RuleSeverityType.WARNING,
				new LinkedHashMap<>());
		warningRules4RootGroup = warningRulesPerRootGroup.entrySet().stream()
				.map(e -> createRulesInRootGroup(RuleSeverityType.WARNING, HINT_POSTFIX, data, e.getKey(), e.getValue(),
						codeGenRules4Name))
				.toList();
		// Root group mapping for info rules
		Map<String, List<RuleInfo>> infoRulesPerRootGroup = rulesPerRootGroupAndSeverity.getOrDefault(RuleSeverityType.INFO,
				new LinkedHashMap<>());
		infoRules4RootGroup = infoRulesPerRootGroup.entrySet().stream()
				.map(e -> createRulesInRootGroup(RuleSeverityType.INFO, INFO_POSTFIX, data, e.getKey(), e.getValue(),
						codeGenRules4Name))
				.toList();

		// Root group mapping for preliminary rules
		LinkedHashMap<String, List<RuleInfo>> preliminaryRulesPerRootGroup = preliminaryRules.stream()
				.collect(Java8Utils.groupingByLinkedHashMap(this::getRootGroup4Rule));
		Map<String, List<RuleInfo>> preliminaryRulesPerRootGroupGlobalFirst = Java8Utils.putKeyToStart(preliminaryRulesPerRootGroup,
				GLOBAL_ROOTGROUP);
		preliminaryRules4RootGroup = preliminaryRulesPerRootGroupGlobalFirst.entrySet().stream()
				.map(e -> createRulesInRootGroup(null, PRELIMINARY_POSTFIX, data, e.getKey(), e.getValue(), codeGenRules4Name))
				.toList();
	}

	private RulesInRootGroup createRulesInRootGroup(RuleSeverityType severity, String postFix, CodeGenData data, String rootGroup,
			List<RuleInfo> ruleInfoList, Map<String, CodeGenRuleDefinition> codeGenRules4Name) {
		// If preliminary rules iterate external on root group level, uniqueness constraints on root group level do not work correctly:
		// If a repetition is marked as erroneous, the value is discarded subsequently and no further errors will be shown.
		// So we regard all preliminary rules as being global (but still execute them root group wise).
		boolean isPreliminary = PRELIMINARY_POSTFIX.equals(postFix);
		boolean isGlobal = isPreliminary || GLOBAL_ROOTGROUP.equals(rootGroup);
		return new RulesInRootGroupBuilder(data, rootGroup, ruleInfoList, severity, postFix, codeGenRules4Name, isGlobal).build();
	}

	private String getRootGroup4Rule(RuleInfo ruleInfo) {
		if (ruleInfo.isAlwaysEvaluated()) {
			return GLOBAL_ROOTGROUP;
		}
		if (ruleInfo.isUseMultipleRootGroups()) {
			return GLOBAL_ROOTGROUP;
		}

		String fullName = ruleInfo.getRule().getErrorEntity().getFullName();
		return EntityUtils.getRootGroupName(fullName);
	}

	private Stream<RulesInRootGroup> getAllRulesInRootGroup() {
		return Stream.of(errorRules4RootGroup, warningRules4RootGroup, infoRules4RootGroup, allRules4RootGroup,
						preliminaryRules4RootGroup)
				.flatMap(List::stream);
	}

	public List<String> createAllMethods(String postfix) {
		String param = "method" + (postfix.isEmpty() ? postfix : "_" + postfix);
		return STParamUtil.toListOfCode(getAllRulesInRootGroup(), param);
	}

	public List<String> createAllMethodCalls() {
		return STParamUtil.toListOfCode(allRules4RootGroup, "call");
	}

	public List<String> createErrorMethodCalls() {
		return STParamUtil.toListOfCode(errorRules4RootGroup, "call");
	}

	public List<String> createWarningMethodCalls() {
		return STParamUtil.toListOfCode(warningRules4RootGroup, "call");
	}

	public List<String> createInfoMethodCalls() {
		return STParamUtil.toListOfCode(infoRules4RootGroup, "call");
	}

	public List<String> createPreliminaryMethodCalls() {
		return STParamUtil.toListOfCode(preliminaryRules4RootGroup, "call");
	}

	public List<PreliminaryRulesForErrorField> getPreliminaryRulesPerErrorField() {
		return preliminaryRules4RootGroup.stream()
				.flatMap(r -> r.getCurrentRuleCalls().stream())
				.filter(CodeGenRuleDefinition::isErrorEntityAField)
				.collect(Collectors.groupingBy(CodeGenRuleDefinition::getUniqueErrorFieldName))
				.entrySet().stream()
				.sorted(Map.Entry.comparingByKey()) // stable order
				.map(Map.Entry::getValue)
				.map(PreliminaryRulesForErrorField::new)
				.toList();
	}
}
