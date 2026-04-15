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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenFilterCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdsCollector;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.CodeGenCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

/**
 * Models the calculation code for dynamic enumeration values.
 */
@StringTemplateGroup("dynamicEnum")
public class CodeGenDynamicEnumeration extends STParameterClassBase {
	private final EntityId dynEnumField;

	private final List<CodeGenDynamicEnumerationValue> enumValueOperations;

	private final CodeGenCondition safeGuard;

	private final String modelName;

	private final EntityIdsCollector rtCtrlParamCodeCollector;

	private final EntityIdsCollector filterEntityIdsCollector;

	private final Collection<CodeGenFilterCondition> filterConditions;

	CodeGenDynamicEnumeration(CodeGenDynamicEnumerationBuilder builder) {
		this.dynEnumField = builder.getDynEnumField();
		this.enumValueOperations = builder.getEnumValueOperations();
		this.safeGuard = builder.getSafeGuard();
		this.modelName = builder.getData().getParams().getName();
		this.rtCtrlParamCodeCollector = builder.getRtControllerParamsCreationCodeCollector();
		this.filterEntityIdsCollector = builder.getFilterEntityIdsCollector();
		this.filterConditions = builder.getFilterConditions();
	}

	public String getName() {
		return this.modelName;
	}

	public EntityId getEnumerationField() {
		return dynEnumField;
	}

	public String getDynEnumFieldName() {
		return dynEnumField.getFullName();
	}

	public String getUniqueName() {
		return dynEnumField.getUniqueName();
	}

	public String getEnumValuesCode() {
		StringBuilder sb = new StringBuilder();
		enumValueOperations.forEach(ba -> sb.append(ba.toCode()));
		return sb.toString();
	}

	public String getSafeGuardCode() {
		return safeGuard.getBooleanCode();
	}

	public boolean hasSafeGuard() {
		return safeGuard != null;
	}

	public List<String> getRtControllerParamsCreationCode() {
		return rtCtrlParamCodeCollector.getAll().stream()
				.map(co -> co.toCode("varAssignment"))
				.collect(Collectors.toList());
	}

	public List<String> getFilterParamsCreationCode() {
		return filterEntityIdsCollector.getAll().stream()
				.map(co -> co.toCode("varAssignment"))
				.collect(Collectors.toList());
	}

	public List<String> getFilterCode() {
		return filterConditions.stream()
				.map(CodeGenFilterCondition::toCode)
				.collect(Collectors.toList());
	}
}
