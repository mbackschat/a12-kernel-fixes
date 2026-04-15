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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.calc;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModel;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.ListParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.MapParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.SetParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

/**
 * Parameter class for the creation of calculations with preconditions. The generated class will be
 * of the form "C_&lt;modelName&gt;".
 */
@StringTemplateGroup("calc")
public class CodeGenCalc extends CodeGenModel {

	private final MapParameter<StringParameter, SetParameter<StringParameter>> dependencyGraph;
	private final SetParameter<StringParameter> expandedOperandFieldsOfCalculations;
	private final MapParameter<StringParameter, StringParameter> calcFuncNameMap;
	private final ListParameter<StringParameter> calculatedFieldFullNamesInOrder;
	public final List<CodeGenCalculatedField> calculatedFields;
	private List<CodeGenCalcObject> codeGenCalcObjects = new ArrayList<>();

	CodeGenCalc(CodeGenCalcBuilder builder) {
		super(builder);
		calculatedFields = builder.getCalculatedFields();
		calcFuncNameMap = builder.getCalcFuncNameMap(calculatedFields);
		calculatedFieldFullNamesInOrder = builder.getCalculatedFieldFullNamesInOrder(calculatedFields);
		dependencyGraph = builder.getDependencyGraph(calculatedFields);
		expandedOperandFieldsOfCalculations = builder.getExpandedOperandFieldsOfCalculations();
		codeGenCalcObjects = builder.getCalcObjects(calculatedFields);
	}

	public List<String> getCalcObjectCreationsCode() {
		return codeGenCalcObjects.stream()
				.map(co -> co.toCode("line"))
				.toList();
	}

	public List<String> getCalcObjectClasses() {
		return codeGenCalcObjects.stream()
				.map(co -> co.toCode("class"))
				.toList();
	}

	public String getDependencyGraph() {
		return dependencyGraph.toCode();
	}

	public String getExpandedOperandFieldsOfCalculations() {
		return expandedOperandFieldsOfCalculations.toCode();
	}

	public String getCalcFuncNameMap() {
		return calcFuncNameMap.toCode();
	}

	public String getCalculatedFieldFullNamesInOrder() {
		return calculatedFieldFullNamesInOrder.toCode();
	}

	public List<String> getBerechneteFelderCode() {
		return calculatedFields.stream()
				.map(CodeGenCalculatedField::toCode)
				.toList();
	}

	public List<String> getBerechneteFelderAufrufCode() {
		return calculatedFields.stream()
				.map(bf -> bf.toCode("call"))
				.toList();
	}
}
