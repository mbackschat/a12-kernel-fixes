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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenRuleConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParamUtil;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

/**
 * Parameter class for the template of the R_ classes containing the code for the validation rules.
 */
@StringTemplateGroup("rules/")
public class CodeGenRules extends CodeGenEREntities<CodeGenData> {

	private int currentRuleFile = 0;
	private final int numRuleFiles;

	private final List<CodeGenRuleDefinition> codeGenRuleDefinitions;
	private final List<CodeGenRuleCheckObject> codeGenRuleCheckObjects;
	private final CollectedRulesPerRootGroupCreator rulesForRootGroupCreator;

	public CodeGenRules(CodeGenRulesBuilder builder) {
		super(builder);
		this.codeGenRuleDefinitions = builder.getRuleDefinitions();
		this.codeGenRuleCheckObjects = builder.getCheckObjects();
		this.rulesForRootGroupCreator = builder.getCollectedRulesPerRootGroupCreator();
		this.numRuleFiles = builder.getNumRuleFilesCpp();
	}

	public List<String> getRuleCheckObjectCreationsCode() {
		return STParamUtil.toListOfCode(codeGenRuleCheckObjects, "line");
	}

	public List<String> getRuleCheckObjectClasses() {
		return STParamUtil.toListOfCode(codeGenRuleCheckObjects, "class");
	}

	public List<String> getRuleDefinitionsCode() {
		return STParamUtil.toListOfCode(codeGenRuleDefinitions);
	}

	public List<String> getRuleDefinitionsCodeCpp() {
		List<String> ret = new LinkedList<>();
		int startValue = currentRuleFile * CodeGenRuleConstants.NUM_RULES_PER_FILE_CPP;
		for (int i = startValue; i < startValue + CodeGenRuleConstants.NUM_RULES_PER_FILE_CPP && i < codeGenRuleDefinitions.size(); i++) {
			CodeGenRuleDefinition c = codeGenRuleDefinitions.get(i);
			ret.add(c.toCode());
		}
		currentRuleFile++;
		return ret;
	}

	public List<String> getRulesPerRootGroupMethodsCode() {
		return this.rulesForRootGroupCreator.createAllMethods("");
	}

	public List<String> getRulesPerRootGroupMethodsCodeH() {
		return this.rulesForRootGroupCreator.createAllMethods("h");
	}

	public List<String> getRulesPerRootGroupCalls() {
		return rulesForRootGroupCreator.createAllMethodCalls();
	}

	public List<String> getErrorRulesPerRootGroupCalls() {
		return rulesForRootGroupCreator.createErrorMethodCalls();
	}

	public List<String> getWarningRulesPerRootGroupCalls() {
		return rulesForRootGroupCreator.createWarningMethodCalls();
	}

	public List<String> getInfoRulesPerRootGroupCalls() {
		return rulesForRootGroupCreator.createInfoMethodCalls();
	}

	public List<String> getPreliminaryRulesPerRootGroupCalls() {
		return rulesForRootGroupCreator.createPreliminaryMethodCalls();
	}

	public List<String> getUniqueFieldNamesWithPreliminaryRules() {
		return rulesForRootGroupCreator.getPreliminaryRulesPerErrorField().stream()
				.map(PreliminaryRulesForErrorField::getUniqueErrorFieldName)
				.toList();
	}

	public List<String> getPreliminaryRulesPerErrorFieldMethodsCode() {
		return STParamUtil.toListOfCode(rulesForRootGroupCreator.getPreliminaryRulesPerErrorField(), "method");
	}

	public List<String> getPreliminaryRulesPerErrorFieldCallsCode() {
		return STParamUtil.toListOfCode(rulesForRootGroupCreator.getPreliminaryRulesPerErrorField(), "call");
	}

	// methods for model access

	/**
	 * Helper method for partial validation. Groups the rules by error field name.
	 *
	 * @param filter
	 * 		the filter to be used for filtering the rules.
	 */
	Map<String, List<CodeGenRuleDefinition>> getFilteredRulesByErrorFieldName(Predicate<CodeGenRuleDefinition> filter) {
		return codeGenRuleDefinitions.stream()
				.filter(filter)
				.collect(Collectors.groupingBy(CodeGenRuleDefinition::getUniqueErrorFieldName));
	}

	public int getNumRuleFilesCpp() {
		return numRuleFiles;
	}

	public String getRuleDefinitionsHFileCode() {
		return STParamUtil.toConcatenatedCode(codeGenRuleDefinitions, "h");
	}

	public String getAllRulesVBACode() {
		return getRulesCode(false);
	}

	public String getAllPreliminaryRulesVBACode() {
		return getRulesCode(true);
	}

	public String getRulesCode(boolean preliminary) {
		return STParamUtil.toConcatenatedCode(codeGenRuleDefinitions.stream().filter(r -> r.isPreliminary() == preliminary),
				"call");
	}
}
