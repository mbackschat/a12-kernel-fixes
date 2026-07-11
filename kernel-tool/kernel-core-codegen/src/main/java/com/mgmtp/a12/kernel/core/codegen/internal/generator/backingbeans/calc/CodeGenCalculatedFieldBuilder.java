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

import java.util.List;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.AbstractCodeGenCalcBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdsCollector;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.CodeGenCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.CodeGenOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.CalculatedFieldIterationLimitCreator;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.IterationLimitCreatorBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.conversion.CodeGenCreator;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.INumberType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;

/**
 * Builder for the template parameter class {@link CodeGenCalculatedField}.
 */
public class CodeGenCalculatedFieldBuilder extends AbstractCodeGenCalcBuilder {
	private final List<ICalculationInfo> calcInfos;
	private final boolean isHModel;
	private final long maxDigitsGlobal;
	private final EntityId calculatedField;
	private final List<CodeGenCalculationAlternative> calcAlternatives;

	public CodeGenCalculatedFieldBuilder(List<ICalculationInfo> calcInfos, CodeGenData data,
			Function<IEntityId, String> uniqueFieldNamesMapper, boolean expandGroups) {
		super(data, expandGroups,
				uniqueFieldNamesMapper.apply(calcInfos.get(0).getCalculatedField()),
				uniqueFieldNamesMapper);
		this.isHModel = ExtmpUtils.isHModel(data.getModelSpecificSettings());
		this.maxDigitsGlobal = IProjectSpecificSettings.MAXDIGITS_IN_NUMBERS;

		List<ICalculationInfo> calcInfosWithExpandedCommonPrecondition = calcInfos.stream()
				.map(CalculationUtils::expandCommonPrecondition)
				.toList();
		this.calcInfos = codeGenParseTreeHelper.expandAndTransformCalcInfos(calcInfosWithExpandedCommonPrecondition);

		this.calculatedField = CodeGenCreator.convertEntity(calcInfos.get(0).getCalculatedField(), uniqueFieldNamesMapper,
				entityIdsCollector);

		this.calcAlternatives = computeCalculationAlternatives();
		assert this.calcInfos.stream().allMatch(ci -> ci.getCommonPrecondition().isEmpty()) :
				"common preconditions have already moved/distributed into the calculation alternatives a few lines above";
	}

	private List<CodeGenCalculationAlternative> computeCalculationAlternatives() {
		return calcInfos.stream()
				.flatMap(ci -> ci.getPartialCalculations().stream()
						.map(alt -> createCalculationAlternative(ci, alt.getLeft(), alt.getRight())))
				.toList();
	}

	private CodeGenCalculationAlternative createCalculationAlternative(ICalculationInfo ci, ICondition preCondition,
			IOperation calcOperation) {
		// common preconditions were expanded before.
		assert ci.getCommonPrecondition().isEmpty();
		CodeGenCondition codeGenPreCondition;
		if (preCondition == null) {
			codeGenPreCondition = null;
		} else {
			codeGenPreCondition = codeGenParseTreeHelper.convertCondition(preCondition, this.entityIdsCollector);
		}

		CodeGenOperation codeGenCalculationOperation = codeGenParseTreeHelper.convertOperation(calcOperation, entityIdsCollector);

		// Get the maximum
		IField field = (IField) data.getEc().get(ci.getCalculatedField().getFullName());
		int maxFractionalDigits = 0;
		int minFractionalDigits = 0;
		if (field.getType() instanceof INumberType type) {
			maxFractionalDigits = type.getMaxFractionalDigits();
			minFractionalDigits = type.getMinFractionalDigits();
		}

		return new CodeGenCalculationAlternative(ci, codeGenPreCondition, codeGenCalculationOperation, calculatedField,
				maxDigitsGlobal, minFractionalDigits, maxFractionalDigits, getParallelIterationLevel());
	}

	public CodeGenCalculatedField build() {
		return new CodeGenCalculatedField(this);
	}

	List<ICalculationInfo> getCalcInfos() {
		return calcInfos;
	}

	boolean isHModel() {
		return isHModel;
	}

	boolean isProgLangWithUsb() {
		return data.getParams().getProgrammingLanguage().isWithUsb();
	}

	EntityId getCalculatedField() {
		return calculatedField;
	}

	IterationLimitCreatorBase getIterationLimitCreator() {
		return new CalculatedFieldIterationLimitCreator(calculatedField, data, getParallelIterationLevel());
	}

	List<CodeGenCalculationAlternative> getCalculationAlternatives() {
		return calcAlternatives;
	}

	boolean isParallelIteration() {
		return calcInfos.stream()
				.flatMap(calcInfo -> calcInfo.getPartialCalculations().stream())
				.anyMatch(p -> AnalyseService.isParallelIteration(p.getLeft(), true) || AnalyseService.isParallelIteration(
						p.getRight(), true)) ||
				// Hinweis: die commonPrecondition ist hier vermutlich leer, aber so ist der code robuster
				calcInfos.stream()
						.flatMap(calcInfo -> calcInfo.getCommonPrecondition().stream())
						.anyMatch(p -> AnalyseService.isParallelIteration(p, true));
	}

	int getParallelIterationLevel() {
		if (!isParallelIteration()) {
			return -1;
		}
		return EntityUtils.getIndexFieldLevel(calculatedField.getFullName(), data.getEc());
	}
}
