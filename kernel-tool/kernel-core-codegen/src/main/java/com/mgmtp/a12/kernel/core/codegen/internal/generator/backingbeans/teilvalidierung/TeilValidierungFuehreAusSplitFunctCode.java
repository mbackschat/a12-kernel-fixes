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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.teilvalidierung;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenRuleDefinition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

@StringTemplateGroup("teilvalidierung")
public class TeilValidierungFuehreAusSplitFunctCode extends STParameterClassBase {

	private final String minFeldNr;
	private final String maxFeldNr;
	private final String methodCount;
	private final SortedMap<Integer, List<CodeGenRuleDefinition>> feldNrXregelaufrufe;
	private final RuleType ruleType;
	private final String name;

	public TeilValidierungFuehreAusSplitFunctCode(int minFeldNr, int maxFeldNr,
			int methodCount,
			SortedMap<Integer, List<CodeGenRuleDefinition>> feldNrXregelaufrufe,
			RuleType regelArt, String paketName) {
		this.minFeldNr = String.valueOf(minFeldNr);
		this.maxFeldNr = String.valueOf(maxFeldNr);
		this.methodCount = String.valueOf(methodCount);
		this.feldNrXregelaufrufe = feldNrXregelaufrufe;
		this.ruleType = regelArt;
		this.name = paketName;
	}

	public String getMinFeldNummer() {
		return minFeldNr;
	}

	public String getMaxFeldNummer() {
		return maxFeldNr;
	}

	public String getMethodCounter() {
		return methodCount;
	}

	public String getCaseStmtCode() {
		StringBuilder sbCaseStmt = new StringBuilder();
		for (Entry<Integer, List<CodeGenRuleDefinition>> entry : feldNrXregelaufrufe.entrySet()) {
			Integer interneId = entry.getKey();
			List<CodeGenRuleDefinition> ruleDefinitions = entry.getValue();
			sbCaseStmt.append(new TeilValidierungFuehreAusCaseStmtCode(
					interneId, ruleDefinitions).toCode());
		}
		return sbCaseStmt.toString();
	}

	public String getCheckerTypNameCode() {

		if (ruleType == null) {
			return "TVCheckPreliminary_" + name;
		}

		switch (ruleType) {
			case ERROR:
				return "TVCheckFehler_" + name;
			case WARNING:
				return "TVCheckHinweis_" + name;
			case INFO:
				return "TVCheckInfo_" + name;
			default:
				throw new IllegalArgumentException("Unerwartete Regelart '" + ruleType + "' in "
						+ this.getClass().getName());
		}
	}
}
