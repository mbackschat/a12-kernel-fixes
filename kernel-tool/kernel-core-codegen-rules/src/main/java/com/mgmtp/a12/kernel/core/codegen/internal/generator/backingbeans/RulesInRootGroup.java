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

import static com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT;
import static com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper.deriveSplitFunctionCode;
import static com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper.deriveSplitFunctionCodeCalls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenContext;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.OptimizationCodeFragment;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParamUtil;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.StringUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule.RuleSeverityType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Template class to output the rules contained in the individual root groups of a
 * <code>R_&lt;name&gt;</code> class.
 */
@StringTemplateGroup("rules/")
public class RulesInRootGroup extends STParameterClassBase {

	private Map<RuleSeverityType, List<CodeGenRuleDefinition>> ruleCalls;
	private String rootGroupName;
	private RuleSeverityType severity;
	private long maxRootGroupIndex;
	private boolean isGlobal;
	private String mTVA;
	private String postFix;

	public RulesInRootGroup(RulesInRootGroupBuilder b) {
		ruleCalls = b.getRuleCalls();
		rootGroupName = b.getRootGroupName();
		maxRootGroupIndex = b.getMaxRootGroupIndex();
		severity = b.getSeverity();
		postFix = b.getPostFix();
		isGlobal = b.isGlobal();
		mTVA = b.getMTVA();
	}

	public List<String> getRuleCallsCode() {
		return STParamUtil.toListOfCode(getCurrentRuleCalls(), "call");
	}

	public String getPostfix() {
		return postFix;
	}

	public String getRootGroupName() {
		return rootGroupName;
	}

	public String getMaxLfdNummerCodeFragment() {
		return new OptimizationCodeFragment("getMaxGesetzterKontext(0)", maxRootGroupIndex).toCode();
	}

	public String getRootGroupName4Method() {
		return StringUtil.normalizeSpecialChars(rootGroupName);
	}

	public boolean getIsGlobal() {
		return isGlobal;
	}

	public String getMTVA() {
		return mTVA;
	}

	public String getRootGroupSplitFunctionCalls() {
		ProgrammingLanguage programmingLanguage = CodeGenContext.getInstance().getProgrammingLanguage();
		String methodCallTemplate = this.toCode("methodCallTemplate");
		List<CodeGenRuleDefinition> currentRuleCalls = getCurrentRuleCalls();
		return deriveSplitFunctionCodeCalls(currentRuleCalls.size(),
				methodCallTemplate,
				ELEMENTS_PER_SPLIT_METHOD_DEFAULT,
				programmingLanguage);
	}

	public String getRootGroupSplitFunctionCode() {
		ProgrammingLanguage programmingLanguage = CodeGenContext.getInstance().getProgrammingLanguage();
		List<CodeGenRuleDefinition> currentRuleCalls = getCurrentRuleCalls();
		String methodSigTemplate = this.toCode("methodSigTemplate");
		String methodPrefix = this.toCode("methodPrefix");
		return deriveSplitFunctionCode(currentRuleCalls,
				methodSigTemplate,
				ELEMENTS_PER_SPLIT_METHOD_DEFAULT,
				programmingLanguage,
				methodPrefix,
				"",
				"call");
	}

	public List<CodeGenRuleDefinition> getCurrentRuleCalls() {
		if (severity == null) {

			List<CodeGenRuleDefinition> result = new ArrayList<>();
			// for a fix order in the generated code we use values(). S. EXTP-2712
			Stream.of(RuleSeverityType.values())
					.filter(type -> (ruleCalls.containsKey(type)))
					.forEach(type -> result.addAll(ruleCalls.get(type)));
			return result;
		} else {
			List<CodeGenRuleDefinition> rules = ruleCalls.get(severity);
			if (rules == null) {
				return Collections.emptyList();
			}
			return rules;
		}
	}
}
