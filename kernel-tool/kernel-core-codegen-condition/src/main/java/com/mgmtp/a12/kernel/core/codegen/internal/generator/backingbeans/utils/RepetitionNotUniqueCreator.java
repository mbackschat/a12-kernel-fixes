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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.EntityListCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.IRepetitionNotUniqueHandler;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;

/**
 * Helper class for generating code for the language construct RepetitionNotUnique.
 */
public class RepetitionNotUniqueCreator {

	private RepetitionNotUniqueCreator() {
	}

	/**
	 * Generates the initialization code for rules ZeileNichtEindeutig / WiederholungNichtEindeutig.
	 * Might be used for computations as well as validation rules.
	 *
	 * @param codeGenObject
	 *        {@link CodeGenRuleDefinition} or {@link CodeGenCalculatedField} for which we are
	 * 		generating code.
	 * @return The generated code fragment
	 */
	public static String getInitCode(IRepetitionNotUniqueHandler codeGenObject) {
		int ebene = codeGenObject.getEbeneAbDerDuplikatGesucht();
		if (ebene < 0) {
			return "";
		}
		return new InitWiederholungNichtEindeutigParameter().toCode();
	}

	/**
	 * Generates the clear-code for rules ZeileNichtEindeutig / WiederholungNichtEindeutig.
	 *
	 * @param codeGenObject
	 *        {@link CodeGenRuleDefinition} or {@link CodeGenCalculatedField} object for which
	 * 		we are generating code.
	 * @return The generated code fragment.
	 */
	public static String getClearCode(IRepetitionNotUniqueHandler codeGenObject) {
		int ebene = codeGenObject.getEbeneAbDerDuplikatGesucht();
		if (ebene < 0) {
			return "";
		}
		ClearWiederholungNichtEindeutigParameter clearParams = new ClearWiederholungNichtEindeutigParameter(ebene);
		return clearParams.toCode();
	}

	/**
	 * For the rules ZeileNichtEindeutig (unique model species) und WiederholungNichtEindeutig
	 * (non-unique model species) parameter numbers are needed in the generated code. This number
	 * needs to be stored in the corresponding {@link EntityListCondition} object. This is done
	 * here.
	 *
	 * @param def
	 * 		The {@link CodeGenRuleDefinition} or {@link CodeGenCalculatedField} object for
	 * 		which we compute the parameter numbers.
	 */
	public static void setParamNumberForRuleWiederholungNichtEindeutig(IRepetitionNotUniqueHandler def) {

		class Visitor extends com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor {

			@Override
			public boolean visit(IEntityListCondition ifb) {
				EntityListCondition cflb = (EntityListCondition) ifb;
				if (cflb.isWiederholungNichtEindeutig()) {
					int levelWithoutUsb = getEbeneAbDerDuplikatGesucht(def.isHModel(), def.getFullRuleName(), cflb);
					// The layer only depends on the rule name (and on the first entity being in root group).
					// So if no first entity is in root group all layers are the same.
					List<Integer> searchedDuplicateLayers = getSearchedDuplicateLayers(cflb, levelWithoutUsb,
							def.isProgLangWithUsb());
					def.setEbeneAbDerDuplikatGesucht(def.isProgLangWithUsb() ? levelWithoutUsb + 1 : levelWithoutUsb);
					cflb.setSearchedDuplicateLayers(searchedDuplicateLayers);
				}
				return false;
			}
		}
		def.getRulePartsForCodeGeneration().forEach(b -> b.traverse(new Visitor()));
	}

	/**
	 * Returns the position of the index array from which we start searching for duplicates for the
	 * rules ZeileNichtEindeutig and WiederholungNichtEindeutig. For H models this always
	 * corresponds to the row, for A12 models, this corresponds to the position of the rule context
	 * - or in the special case of root groups this is 0.
	 */
	public static int getEbeneAbDerDuplikatGesucht(boolean isHModel, String ruleFullName, ICondition c) {
		if (!(c instanceof IEntityListCondition elc)) {
			return -1;
		}
		if (elc.getConditionName() != EntityListConditionName.RepetitionNotUnique &&
				elc.getConditionName() != EntityListConditionName.ZeileNichtEindeutig) {
			return -1;
		}
		if (isHModel) {
			return 1;
		}

		// If we have a from statement, use it
		Optional<IEntityId> additionalGroupReference = elc.getAdditionalGroupReference();
		if (additionalGroupReference.isPresent()) {
			return EntityUtils.countHierarchyLevels(additionalGroupReference.get().getFullName()) - 1;
		}

		String fullNameOfFirstEntity = elc.getEntityList().getEntityIdList().get(0).getFullName();
		boolean isInRootGroup = EntityUtils.countHierarchyLevels(fullNameOfFirstEntity) == 2;
		return isInRootGroup
				? 0
				: EntityUtils.countHierarchyLevels(ruleFullName) - 1; // default: 1 level below the rule group
	}

	private static List<Integer> getSearchedDuplicateLayers(EntityListCondition cflb, int firstLayer, boolean adjustForUsb) {
		SortedSet<Integer> result = new TreeSet<>();
		for (IEntityId entity : cflb.getEntityList().getEntityIdList()) {
			long[] indices = entity.getIndices();
			for (int i = firstLayer; i < indices.length; i++) {
				if (indices[i] == IParseTreeEntity.ITERATION) {
					result.add(adjustForUsb ? i + 1 : i);
				}
			}
		}
		return new ArrayList<>(result);
	}
}
