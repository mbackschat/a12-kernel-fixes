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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule.RuleSeverityType;

public class RulesInRootGroupBuilder {

	private final String rootGroupName;
	private final List<RuleInfo> rootGroupRuleInfos;
	private final long maxRootGroupIndex;
	private final RuleSeverityType severity;
	private final Map<String, CodeGenRuleDefinition> codeGenRules4Name;
	private final boolean isGlobal;
	private final String mTVA;
	private final String postFix;

	public RulesInRootGroupBuilder(CodeGenData data, String rootGroupName, List<RuleInfo> ruleInfoInRootGroup,
			RuleSeverityType severity, String postFix,
			Map<String, CodeGenRuleDefinition> codeGenRules4Name, boolean isGlobal) {
		this.rootGroupName = rootGroupName;
		this.rootGroupRuleInfos = ruleInfoInRootGroup;
		this.isGlobal = isGlobal;
		if (isGlobal) {
			this.maxRootGroupIndex = 0;
		} else {
			this.maxRootGroupIndex = data.getEc().get("/" + rootGroupName).getMax()[0];
		}
		this.severity = severity;
		this.postFix = postFix;
		this.codeGenRules4Name = codeGenRules4Name;
		this.mTVA = data.getParams().getName();
	}

	public RulesInRootGroup build() {
		return new RulesInRootGroup(this);
	}

	public Map<RuleSeverityType, List<CodeGenRuleDefinition>> getRuleCalls() {
		return rootGroupRuleInfos.stream()
				.collect(Collectors.groupingBy(r -> r.getRule().getSeverityType(),
						Collectors.mapping(r -> codeGenRules4Name.get(r.getRule().getFullName()), Collectors.toList())));
	}

	/**
	 * @return the rootGroupName
	 */
	public String getRootGroupName() {
		return rootGroupName;
	}

	public long getMaxRootGroupIndex() {
		return maxRootGroupIndex;
	}

	public RuleSeverityType getSeverity() {
		return severity;
	}

	public boolean isGlobal() {
		return isGlobal;
	}

	public String getMTVA() {
		return mTVA;
	}

	public String getPostFix() {
		return postFix;
	}
}
