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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationDependencies;
import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationErrorLookup;
import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationErrorType;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModelBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.ListParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.MapParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.SetParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.CalculationCodeGenUtilities;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.ICalculationService;
import com.mgmtp.a12.kernel.core.utils.internal.TopologicalSortHelper;

public class CodeGenCalcBuilder extends CodeGenModelBuilder {

	private final boolean expandGroups;
	private final CalculationDependencies calculationDependencies = new CalculationDependencies(
			data.getParams().getCalculationInformation(),
			data.getEc(),
			ExtmpUtils.isHModel(data.getModelSpecificSettings()),
			Collections.emptyMap()
	);

	public CodeGenCalcBuilder(CodeGenData data, IProblemReporter problemReporter, PackageInformation packageInformation,
			boolean expandGroups) {
		super(data, problemReporter, packageInformation);
		this.expandGroups = expandGroups;
	}

	public List<CodeGenCalculatedField> getCalculatedFields() {
		TopologicalSortHelper.TopologicalSortResult<String> result =
				TopologicalSortHelper.topologischeSortierung(calculationDependencies.getDependencyGraphCopy());

		if (result.isSuccessful()) {
			Function<IEntityId, String> uniqueFieldNamesMapper = e -> data.getUniqueFieldNames().get(e.getFullName());
			return result.getSort().stream()
					.map(fieldName -> new CodeGenCalculatedFieldBuilder(
							calculationDependencies.getCalculationInfo(fieldName),
							data,
							uniqueFieldNamesMapper,
							expandGroups).build())
					.toList();
		} else {
			CalculationErrorLookup errorLookup = new CalculationErrorLookup(
					data.getModelSpecificSettings().getLanguageUsedInConditionText());
			List<String> cycle = result.getCycle();
			String cycleString = CalculationCodeGenUtilities.createCycleString(cycle, (targetField, startField) -> {
				for (ICalculationService.IRefType ref : calculationDependencies.getEdgeLabel(targetField, startField)) {
					if (ref instanceof ICalculationService.IRuleRef ruleRef) {
						return ruleRef.getFullName();
					}
				}
				return "?";
			});
			IField field = (IField) data.getEc().get(cycle.get(0));
			errorLookup.reportError(problemReporter, field, CalculationErrorType.MVK_ERROR_CALC_CYCLE, cycleString);

			return Collections.emptyList();
		}
	}

	public MapParameter<StringParameter, StringParameter> getCalcFuncNameMap(List<CodeGenCalculatedField> calculatedFields) {
		return calculatedFields.stream().collect(MapParameter.collector(
				calculatedField -> new StringParameter(calculatedField.getCalculatedFieldName()),
				calculatedField -> new StringParameter(calculatedField.getCalcFuncName())
		));
	}

	public ListParameter<StringParameter> getCalculatedFieldFullNamesInOrder(List<CodeGenCalculatedField> calculatedFields) {
		return new ListParameter<>(calculatedFields.stream().map(calculatedField ->
				new StringParameter(calculatedField.getCalculatedFieldName())).toList(), true);
	}

	public MapParameter<StringParameter, SetParameter<StringParameter>>
	getDependencyGraph(List<CodeGenCalculatedField> calculatedFields) {
		Map<String, Set<String>> dependencyGraph = calculationDependencies.getDependencyGraph();
		return calculatedFields.stream().collect(MapParameter.collector(
				calculatedField -> new StringParameter(calculatedField.getCalculatedFieldName()),
				calculatedField -> {
					Set<String> dependencies = dependencyGraph.get(calculatedField.getCalculatedFieldName());
					return new SetParameter<>(dependencies.stream()
							.map(StringParameter::new)
							.toList());
				}
		));
	}

	public SetParameter<StringParameter> getExpandedOperandFieldsOfCalculations() {
		return new SetParameter<>(
				calculationDependencies.getExpandedOperandFieldsOfCalculations().stream().sorted().map(StringParameter::new)
						.toList(),
				true
		);
	}

	public List<CodeGenCalcObject> getCalcObjects(List<CodeGenCalculatedField> calculatedFields) {
		List<CodeGenCalcObject> calcObjects = new ArrayList<>();
		CodeGenCalcObject curCalcObject = null;
		int calcObjectNum = 0;
		String modelName = data.getParams().getName();
		for (int i = 0; i < calculatedFields.size(); i++) {
			if (i % CodeGenConstants.NUM_CALCULATEDFIELDS_PER_CLASS_GROOVY == 0) {
				calcObjectNum++;
				curCalcObject = new CodeGenCalcObject(calcObjectNum, modelName,
						packageInformation.getPackageAsGroovyClassnamePrefix());
				calcObjects.add(curCalcObject);
			}
			curCalcObject.addCalculatedField(calculatedFields.get(i));
		}
		return calcObjects;
	}

	@Override
	public CodeGenCalc build() {
		return new CodeGenCalc(this);
	}

}
