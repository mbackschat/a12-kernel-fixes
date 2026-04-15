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

package com.mgmtp.a12.kernel.core.calc.internal.util;

import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.copyOfRangeAsList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.EntityIdImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.EntityIdListImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.CompositeConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.EntityListConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.RootGroupConditionImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;

/**
 * Provide various utilities for handling calculations.
 */
public class CalculationUtils {
	private CalculationUtils() {
		throw new UnsupportedOperationException("Utility class should not be instantiated");
	}

	/**
	 * Extract the group that contains the calculated field for which a subgroup or itself is
	 * referenced in all Or-Paths of the calculation rule in a GroupFilled construct.
	 * We call this conditional calculation group.
	 *
	 * @param calculatedField
	 * 		the calculated field, that needs to be contained in the resulting group.
	 * @param calculationsForField
	 * 		all calculation information for the field.
	 * @return the conditional calculation group or the empty string if the group is unconditional.
	 */
	public static String extractConditionalCalculationGroup(String calculatedField, List<ICalculationInfo> calculationsForField) {
		List<ICondition> preconditions = calculationsForField.stream()
				.map(CalculationUtils::expandCommonPrecondition)
				.flatMap(ci -> ci.getPartialCalculations().stream())
				.map(Pair::getLeft)
				.filter(Objects::nonNull)
				.toList();
		// The partial calculations are all connected by or
		ICondition condition = new CompositeConditionImpl(LogOp.Or, preconditions);
		return ConditionalCalculationGroupVisitor.getConditionalCalculationGroup(condition, calculatedField);
	}

	public static ICalculationInfo expandCommonPrecondition(ICalculationInfo originalCalc) {
		Optional<ICondition> commonPreconditionOpt = originalCalc.getCommonPrecondition();
		if (commonPreconditionOpt.isEmpty()) {
			return originalCalc;
		}
		ICondition commonPrecondition = commonPreconditionOpt.get();
		List<Pair<ICondition, IOperation>> partialCalculations = originalCalc.getPartialCalculations().stream()
				.map(p -> Pair.of(andOp(commonPrecondition, p.getLeft()), p.getRight()))
				.toList();
		return new CalculationInfo(originalCalc.getCalculatedField(), null, partialCalculations, originalCalc.getRoundingMode(),
				originalCalc.getName());
	}

	/**
	 * If the given {@link ICalculationInfo}
	 * <ul>
	 *     <li>is from an A12-Model, and</li>
	 *     <li>calculates a field with a repeatable group in its path, and</li>
	 *     <li>does not already have the calculated field's innermost repeatable group or one of its descendants as its conditional
	 *     calculation group,</li>
	 * </ul>
	 * create a copy of the {@link ICalculationInfo} where the common precondition is conjoined (via {@code AND}) with a
	 * {@code GroupFilled} on the calculated field's innermost repeatable group. Otherwise return the original.
	 */
	public static ICalculationInfo addImplicitGroupFilledToCommonPreconditionIfApplicable(ICalculationInfo calculationInfo,
			IEC ec) {
		IEntityId errorEntityId = calculationInfo.getCalculatedField();
		if (errorEntityId.isHmodel()) { // automatic/implicit repetition-filled check is not supported for H-Models. Reason: this
			// feature is not required and the concept of "innermost repeatable group" does not cleanly apply to H-Models, where
			// individual fields can be repeatable
			return calculationInfo;
		}

		Optional<IEntityId> irgIdOpt = getInnermostRepeatableGroupA12DVOnly(errorEntityId, ec);
		if (irgIdOpt.isEmpty()) {
			// if the calculated field is not below any repeatable group, then there is no group-filled condition to be added
			return calculationInfo;
		}
		IEntityId irgId = irgIdOpt.get();

		String conditionalCalculationGroupFullName = extractConditionalCalculationGroup(
				errorEntityId.getFullName(), List.of(calculationInfo));
		if (!conditionalCalculationGroupFullName.isEmpty()
				&& EntityUtils.isDescendantOrEqual(conditionalCalculationGroupFullName, irgId.getFullName())) {
			// if the calculation already has a GroupFilled on the irg or one of its descendants, the irg is already implicitly
			// required to be filled, so no group-filled condition is needed
			return calculationInfo;
		}

		return new CalculationInfo(
				calculationInfo.getCalculatedField(),
				andOp(groupFilledCondition(irgId), calculationInfo.getCommonPrecondition().orElse(null)),
				calculationInfo.getPartialCalculations(),
				calculationInfo.getRoundingMode(),
				calculationInfo.getName());
	}

	private static ICondition groupFilledCondition(IEntityId entityId) {
		// the distinction between root/non-root group similar to KontextListenBedingungCreator.create().
		// (This appears to be a performance optimization or so, which happens in the parser and is therefore not automatically
		// applied to the condition we create here)
		if (EntityUtils.isRootGroup(entityId.getFullName())) {
			return new RootGroupConditionImpl(
					RootGroupConditionName.MindestensEinVordruckAngegeben,
					entityId.getFullName().substring(1),
					entityId.getIndices()[0],
					entityId.getUsb(),
					entityId);
		} else {
			return new EntityListConditionImpl(
					EntityListConditionName.AllGroupsFilled,
					new EntityIdListImpl(List.of(entityId)), null);
		}
	}

	private static ICondition andOp(ICondition op1, ICondition op2) {
		if (op1 == null) {
			return op2;
		} else if (op2 == null) {
			return op1;
		}
		return new CompositeConditionImpl(LogOp.And, List.of(op1, op2));
	}

	/**
	 * <b>Precondition</b>: A12 model. (otherwise a field may be returned if the field is repeatable on row level)
	 */
	private static Optional<IEntityId> getInnermostRepeatableGroupA12DVOnly(IEntityId errorEntityId, IEC ec) {
		IEntity errorEntity = ec.get(errorEntityId.getFullName());
		String irgFullName = EntityUtils.getLowestRepeatableGroupPath(errorEntity);
		if (irgFullName == null) {
			return Optional.empty();
		}

		int irgLevel = EntityUtils.getLevel(irgFullName);
		List<Long> irgIdIndices = copyOfRangeAsList(errorEntityId.getIndices(), 0, irgLevel + 1);
		IEntity irg = ec.get(irgFullName);

		IEntityId irgId = new EntityIdImpl(
				irg,
				irgIdIndices,
				errorEntityId.getUsb(),
				errorEntityId.getSemanticIndex(),
				errorEntityId.isHmodel(),
				ec,
				errorEntityId.isOuter(),
				PathNotationConfig.IRRELEVANT
		);
		return Optional.of(irgId);
	}

	public static List<IParseTreeEntity> getAllPartsOfCalculationInfos(List<ICalculationInfo> cis) {
		List<IParseTreeEntity> result = cis.stream()
				.flatMap(ci -> ci.getPartialCalculations().stream()
						.flatMap(p -> Stream.of(p.getLeft(), p.getRight()))
						.filter(Objects::nonNull))
				.collect(Collectors.toCollection(() -> new ArrayList<>()));
		result.addAll(cis.stream()
				.flatMap(ci -> ci.getCommonPrecondition().stream())
				.toList());
		return result;
	}
}
