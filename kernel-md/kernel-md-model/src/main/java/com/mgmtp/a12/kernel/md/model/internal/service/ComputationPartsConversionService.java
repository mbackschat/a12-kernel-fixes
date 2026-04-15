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

package com.mgmtp.a12.kernel.md.model.internal.service;

import java.util.List;

import com.mgmtp.a12.kernel.core.calc.internal.util.compOp.IWithComparisonOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IConditionFormatter;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation.ComputationAlternative;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation.ComputationPreconditionsAndOperations;

import lombok.NonNull;

/**
 * Service to convert {@link ICalculationInfo} to {@link ComputationPreconditionsAndOperations}.
 */
public class ComputationPartsConversionService {

	public static @NonNull ComputationPreconditionsAndOperations convertToComputationParts(
			@NonNull ICalculationInfo ci,
			@NonNull String ruleFullName,
			@NonNull IConditionFormatter conditionFormatter
	) {
		List<ComputationAlternative> alternatives = ci.getPartialCalculations()
				.stream()
				.map(a -> convertToComputationAlternative(
						a.getLeft(), a.getRight(), ruleFullName, conditionFormatter))
				.toList();
		String commonPreconditionOrNull = ci.getCommonPrecondition()
				.map(cp -> conditionFormatter.formatConditionPart(cp, ruleFullName)).orElse(null);
		return new ComputationPreconditionsAndOperations(commonPreconditionOrNull, alternatives);
	}

	public static @NonNull ComputationAlternative convertToComputationAlternative(
			ICondition preconditionOrNull,
			@NonNull IOperation operation,
			@NonNull String ruleFullName,
			@NonNull IConditionFormatter conditionFormatter
	) {
		String preCondition = null;
		if (preconditionOrNull != null) {
			preCondition = conditionFormatter.formatConditionPart(preconditionOrNull, ruleFullName);
		}

		String withoutComparisonOp = conditionFormatter.formatConditionPart(unwrapToleranceOp(operation), ruleFullName);

		return ComputationAlternative.builder()
				.precondition(preCondition)
				.operation(withoutComparisonOp)
				.toleranceRangeOp(getToleranceRangeOp(operation))
				.build();
	}

	private static IOperation unwrapToleranceOp(IOperation operation) {
		if (!(operation instanceof IWithComparisonOperation withCompOp)) {
			return operation;
		}
		return withCompOp.getOriginalOperation();
	}

	private static String getToleranceRangeOp(IOperation operation) {
		if (!(operation instanceof IWithComparisonOperation wCompOp)
				|| wCompOp.getComparisonOperation() == null) {
			return null;
		}
		return switch (wCompOp.getComparisonOperation()) {
			case NE10 -> Computation.TR10;
			case NE5 -> Computation.TR5;
			case NE2 -> Computation.TR2;
			case NE1 -> Computation.TR1;
			default -> null;
		};
	}
}
