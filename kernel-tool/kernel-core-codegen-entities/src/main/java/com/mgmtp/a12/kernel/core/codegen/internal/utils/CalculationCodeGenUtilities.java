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
package com.mgmtp.a12.kernel.core.codegen.internal.utils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationUtils;
import com.mgmtp.a12.kernel.core.calc.internal.util.compOp.CalcOperationWithCompOp;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.OpListOperationWithParameterImpl;
import com.mgmtp.a12.kernel.core.parser.internal.util.ReferencedIdsUtil;
import com.mgmtp.a12.kernel.core.parser.internal.util.ReferencedIdsUtil.ReferencedIdsMode;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.FieldIdOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OpListOperationName;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService.RoundingMode;

/**
 * Zentralisiert alle utility Methoden für die Erzeugung von Berechnungscode.
 */
public class CalculationCodeGenUtilities {

	private CalculationCodeGenUtilities() {
	}

	/**
	 * Determines, whether a calculation is a carryover. For a carryover, the special form of the
	 * calculation is [Field1] != [Field2], i.e. one field will be transferred to another one.
	 * <p>
	 * There are some additional constraints:
	 * <ul>
	 * <li>The calculation must not have additional partial calculations.</li>
	 * <li>And the rounding mode has to be {@link RoundingMode#EXACT}.</li>
	 * <li>The carryover field must not contain a semantic index.</li>
	 * </ul>
	 *
	 * @param calc
	 * 		the calculation information for a field.
	 * @return <code>true</code>, iff the given calculation is a carryover.
	 */
	public static boolean isUebertrag(List<ICalculationInfo> calc) {
		if (calc == null) {
			return false;
		}
		if (calc.size() != 1 || calc.getFirst().getPartialCalculations().size() != 1) {
			return false;
		}
		if (calc.getFirst().getRoundingMode() != RoundingMode.EXACT) {
			return false;
		}
		Pair<ICondition, IOperation> partialCalc = calc.getFirst().getPartialCalculations().getFirst();
		if (partialCalc.getRight() instanceof IFieldIdOperation fop && fop.getOperatorName() == FieldIdOperationName.FieldValue) {
			return hasNoSemanticIndex(fop);
		}

		if (partialCalc.getRight() instanceof CalcOperationWithCompOp cop &&
				cop.getOperation() instanceof IFieldIdOperation fop &&
				fop.getOperatorName() == FieldIdOperationName.FieldValue) {
			return hasNoSemanticIndex(fop);
		}

		return false;
	}

	public static String createCycleString(List<String> cycle, BinaryOperator<String> edge2Label) {
		StringBuilder sb = new StringBuilder();
		String prevField = null;
		for (String field : cycle) {
			if (prevField != null) {
				sb
						.append(" - ('")
						.append(edge2Label.apply(prevField, field))
						.append("') - ");
			}
			sb
					.append("'")
					.append(field)
					.append("'");
			prevField = field;
		}
		sb
				.append(" - ('")
				.append(edge2Label.apply(prevField, cycle.getFirst()))
				.append("') - ");
		sb
				.append("'")
				.append(cycle.getFirst())
				.append("'");
		return sb.toString();
	}

	/**
	 * Übersetzt die Rundungsart in eine explizite FeldOperation.
	 *
	 * @param innereOperation
	 * 		die zu rundende Operation.
	 * @param rundungsArt
	 * 		die Rundungsart.
	 * @return die gegebenenfalls um die Rundung ergänzte FeldOperation.
	 */
	public static IOperation prefixRoundingOperation(IOperation innereOperation, RoundingMode rundungsArt) {
		if (rundungsArt == null) {
			return innereOperation;
		}
		return switch (rundungsArt) {
			case ROUND_DOWN -> new OpListOperationWithParameterImpl(OpListOperationName.RoundDown,
					Collections.singletonList(innereOperation), 0);
			case ROUND_UP -> new OpListOperationWithParameterImpl(OpListOperationName.RoundUp,
					Collections.singletonList(innereOperation), 0);
			default -> innereOperation;
		};
	}

	private static boolean hasNoSemanticIndex(IFieldIdOperation fop) {
		IEntityId field = fop.getField();
		return field.getSemanticIndex() == null || field.getSemanticIndex().isEmpty();
	}
}
