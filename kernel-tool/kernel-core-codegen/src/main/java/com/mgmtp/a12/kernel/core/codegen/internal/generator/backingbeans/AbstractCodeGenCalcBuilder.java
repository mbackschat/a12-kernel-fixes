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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.calc.CodeGenCalculatedFieldBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.dynamicEnum.CodeGenDynamicEnumerationBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.CodeGenParseTreeHelper;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;

import lombok.Getter;

/**
 * Abstract base class for the builders {@link CodeGenCalculatedFieldBuilder} and {@link CodeGenDynamicEnumerationBuilder}.
 */
public abstract class AbstractCodeGenCalcBuilder {
	@Getter protected final CodeGenData data;
	protected final EntityIdsCollector entityIdsCollector;
	protected final CodeGenParseTreeHelper codeGenParseTreeHelper;

	protected AbstractCodeGenCalcBuilder(CodeGenData data, boolean expandGroups,
			String uniqueFieldName, Function<IEntityId, String> uniqueFieldNamesMapper) {
		this.data = data;
		this.entityIdsCollector = new EntityIdsCollector("");
		this.codeGenParseTreeHelper = new CodeGenParseTreeHelper(
				data.getEc(), expandGroups, uniqueFieldName, data.getBaseYear(), uniqueFieldNamesMapper);
	}

	public EntityIdsCollector getRtControllerParamsCreationCodeCollector() {
		return entityIdsCollector;
	}

	public EntityIdsCollector getFilterEntityIdsCollector() {
		return codeGenParseTreeHelper.getFilterConditionEntityIdsCollector();
	}

	public List<CodeGenFilterCondition> getFilterConditions() {
		return codeGenParseTreeHelper.getFilterConditions();
	}

	public EntityIdsCollector getSetOpEntityIdsCollector() {
		return codeGenParseTreeHelper.getSetOperationEntityIdsCollector();
	}

	public Collection<CodeGenSetOperation> getSetOperations() {
		return codeGenParseTreeHelper.getSetOperations();
	}
}
