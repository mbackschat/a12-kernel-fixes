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
import java.util.Set;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.CodeGenCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.MetaRegelFehlertexte;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.CodeGenHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.CodeGenParseTreeHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.EntityListAndValueListCreator;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.InitEntityListAndValueListConditionParam;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.IterationLimitCreatorBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleIterationLimitCreator;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.CodeGenUtilities;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.DivisionTransformer;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;

/**
 * Factory for a StringTemplate parameter class {@link CodeGenRuleDefinition} object representing
 * the code of a rule.
 */
public class CodeGenRuleDefinitionBuilder {

	private final RuleInfo ruleInfo;
	private final Map<String, String> uniqueFieldNamesMap;
	private final Map<String, String> uniqueRuleNames;
	private final CodeGenData data;
	private final MetaRegelFehlertexte metaRegelFehlertexte;
	private final boolean expandGroups;
	private final CodeGenParseTreeHelper codeGenParseTreeHelper;
	private final EntityIdsCollector entityIdsCollector;
	private final CodeGenCondition codeGenCondition;
	private final List<InitEntityListAndValueListConditionParam> entityListAndValueListInitCodes;

	/**
	 * @param ruleInfo
	 * 		the rule information, that should be represented by the created parameter class.
	 * @param data
	 * 		the code generation data
	 * @param uniqueRuleNames
	 * 		the mapping from full rule names to the unique rule names, used for naming the
	 * 		rule method in the code.
	 * @param metaRegelFehlertexte
	 * @param expandGroups
	 */
	public CodeGenRuleDefinitionBuilder(RuleInfo ruleInfo, CodeGenData data, Map<String, String> uniqueRuleNames,
			MetaRegelFehlertexte metaRegelFehlertexte, boolean expandGroups) {
		this.ruleInfo = ruleInfo;
		this.data = data;
		this.uniqueRuleNames = uniqueRuleNames;
		this.uniqueFieldNamesMap = data.getUniqueFieldNames();
		this.metaRegelFehlertexte = metaRegelFehlertexte;
		this.expandGroups = expandGroups;

		String uniqueErrorFieldName = uniqueFieldNamesMap.get(ruleInfo.getErrorEntity().getFullName());
		String baseYear = data.getBaseYear();
		this.codeGenParseTreeHelper = new CodeGenParseTreeHelper(data.getEc(), expandGroups, uniqueErrorFieldName, baseYear,
				e -> uniqueFieldNamesMap.get(e.getFullName()));

		entityIdsCollector = new EntityIdsCollector("");

		this.codeGenCondition = createCodeGenCondition();
		this.entityListAndValueListInitCodes = EntityListAndValueListCreator.postprocessParseTree(codeGenCondition);
	}

	/**
	 * @return the created object.
	 */
	public CodeGenRuleDefinition build() {
		return new CodeGenRuleDefinition(this);
	}

	/*
	 * If the created class was in the same package the rest of the methods should be package
	 * private.
	 *****************************************************************************************/

	public RuleInfo getRuleInfo() {
		return ruleInfo;
	}

	public MetaRegelFehlertexte getRegelFehlertexte() {
		return metaRegelFehlertexte;
	}

	public IterationLimitCreatorBase getIterationLimitCreator() {
		return new RuleIterationLimitCreator(ruleInfo, data);
	}

	/**
	 * This methods generates a new {@link CodeGenCondition}-object from the parsed rule. The object
	 * is not cached, so this method should be called only once per rule.
	 * <p>
	 * Here, the expansion mode {@link ExpandMode#NO_EXPAND} is used that may contain the groups.
	 *
	 * @return the newly created {@link CodeGenCondition}-object.
	 */
	public CodeGenCondition getCodeGenCondition() {
		return codeGenCondition;
	}

	private CodeGenCondition createCodeGenCondition() {
		ICondition baseCondition = getBaseCondition();
		return codeGenParseTreeHelper.convertCondition(baseCondition, entityIdsCollector);
	}

	private ICondition getBaseCondition() {
		ICondition baseCondition;
		if (!expandGroups) {
			baseCondition = ruleInfo.getPartiallyExpandedCondition();
		} else {
			baseCondition = ruleInfo.getGroupExpandedCondition();
		}
		return DivisionTransformer.transform(baseCondition);
	}

	public boolean isWithOmissionErrors() {
		if (!CodeGenUtilities.isFullStandardCodeProfile(data)) {
			return false; //  omission errors are not relevant for C++ or source in a mapping relationship.
		}

		return CodeGenHelper.canCreateOmissionErrors(ruleInfo.getCondition());
	}

	public String getUniqueErrorFieldName() {
		if (ruleInfo.getRule().getErrorEntity().isGroup()) {
			return ruleInfo.getRule().getErrorEntity().getFullName();
		}
		return uniqueFieldNamesMap.get(ruleInfo.getRule().getErrorEntity().getFullName());
	}

	public String getUniqueRuleName() {
		return uniqueRuleNames.get(ruleInfo.getRule().getFullName());
	}

	public boolean isHModel() {
		return ExtmpUtils.isHModel(data.getModelSpecificSettings());
	}

	public boolean isProgLangWithUsb() {
		return data.getParams().getProgrammingLanguage().isWithUsb();
	}

	public String getMTVA() {
		return data.getParams().getName();
	}

	public int getSemanticIndexLevel() {
		return ruleInfo.getSemanticIndexLevel();
	}

	public List<CodeGenFilterCondition> getFilterConditions() {
		return codeGenParseTreeHelper.getFilterConditions();
	}

	public EntityIdsCollector getEntityIdsCollector() {
		return entityIdsCollector;
	}

	public EntityIdsCollector getFilterEnditityIdsCollector() {
		return codeGenParseTreeHelper.getFilterConditionEntityIdsCollector();
	}

	public List<InitEntityListAndValueListConditionParam> getEntityListAndValueListInitCodes() {
		return entityListAndValueListInitCodes;
	}

	public Set<String> getUsbRootGroups() {
		return AnalyseService.getUsbIterationRootGroups(codeGenCondition);
	}
}
