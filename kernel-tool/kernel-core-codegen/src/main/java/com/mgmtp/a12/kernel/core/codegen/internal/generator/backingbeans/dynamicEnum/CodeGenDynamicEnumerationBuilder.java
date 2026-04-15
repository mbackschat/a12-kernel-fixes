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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.dynamicEnum;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.calc.internal.dynEnum.ConsistentValueOpImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenFilterCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdsCollector;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.CodeGenCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.EntityListAndValueConditionMixedList;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.CodeGenOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.CodeGenParseTreeHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.conversion.CodeGenCreator;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IDynamicEnumerationInfo;

/**
 * Builder for the template parameter class {@link CodeGenDynamicEnumeration}.
 */
public class CodeGenDynamicEnumerationBuilder {
	private final CodeGenData data;
	private final IDynamicEnumerationInfo dynEnumInfo;
	private final EntityId dynEnumField;
	private final List<CodeGenDynamicEnumerationValue> enumValues;
	private final EntityIdsCollector entityIdsCollector;
	private final CodeGenCondition safeGuard;
	private final CodeGenParseTreeHelper codeGenParseTreeHelper;

	public CodeGenDynamicEnumerationBuilder(IDynamicEnumerationInfo dynEnumInfo, CodeGenData data,
			Function<IEntityId, String> uniqueFieldNamesMapper, boolean expandGroups) {
		String uniqueFieldName = uniqueFieldNamesMapper.apply(dynEnumInfo.getDynamicEnumerationField());
		String baseYear = data.getBaseYear();
		codeGenParseTreeHelper = new CodeGenParseTreeHelper(data.getEc(), expandGroups, uniqueFieldName, baseYear,
				uniqueFieldNamesMapper);
		this.dynEnumInfo = codeGenParseTreeHelper.expandAndTransformDynEnumInfo(dynEnumInfo);

		this.entityIdsCollector = new EntityIdsCollector("");
		this.data = data;
		this.dynEnumField = CodeGenCreator.convertEntity(dynEnumInfo.getDynamicEnumerationField(), uniqueFieldNamesMapper,
				entityIdsCollector);
		this.safeGuard = dynEnumInfo.getSafeGuard() == null ? null
				: codeGenParseTreeHelper.convertCondition(dynEnumInfo.getSafeGuard(), entityIdsCollector);
		this.enumValues = computeEnumValues();
	}

	private List<CodeGenDynamicEnumerationValue> computeEnumValues() {
		return dynEnumInfo.getValueOperations().stream()
				.map(alt -> createEnumValues(alt.getLeft(), alt.getRight()))
				.collect(Collectors.toList());
	}

	private CodeGenDynamicEnumerationValue createEnumValues(ICondition preCondition, IOperation valueOperation) {
		CodeGenCondition codeGenPreCondition;
		if (preCondition == null) {
			codeGenPreCondition = null;
		} else {
			codeGenPreCondition = codeGenParseTreeHelper.convertCondition(preCondition, entityIdsCollector);
		}
		CodeGenOperation codeGenValueOperation;
		if (valueOperation instanceof ConsistentValueOpImpl) {
			codeGenValueOperation = createConsistentValueOp((ConsistentValueOpImpl) valueOperation, entityIdsCollector);

		} else {
			codeGenValueOperation = codeGenParseTreeHelper.convertOperation(valueOperation, entityIdsCollector);
		}

		return new CodeGenDynamicEnumerationValue(codeGenPreCondition, codeGenValueOperation);
	}

	private CodeGenOperation createConsistentValueOp(ConsistentValueOpImpl valueOperation, EntityIdsCollector entityIdsCollector) {
		EntityListAndValueConditionMixedList convertedBaseCond = (EntityListAndValueConditionMixedList) codeGenParseTreeHelper.convertCondition(
				valueOperation.getReferenceCondition(), entityIdsCollector);
		return new ConsistentValueOp(convertedBaseCond);
	}

	public CodeGenDynamicEnumeration build() {
		return new CodeGenDynamicEnumeration(this);
	}

	EntityId getDynEnumField() {
		return dynEnumField;
	}

	CodeGenData getData() {
		return data;
	}

	List<CodeGenDynamicEnumerationValue> getEnumValueOperations() {
		return enumValues;
	}

	EntityIdsCollector getRtControllerParamsCreationCodeCollector() {
		return entityIdsCollector;
	}

	public CodeGenCondition getSafeGuard() {
		return safeGuard;
	}

	public EntityIdsCollector getFilterEntityIdsCollector() {
		return codeGenParseTreeHelper.getFilterConditionEntityIdsCollector();
	}

	public List<CodeGenFilterCondition> getFilterConditions() {
		return codeGenParseTreeHelper.getFilterConditions();
	}
}
