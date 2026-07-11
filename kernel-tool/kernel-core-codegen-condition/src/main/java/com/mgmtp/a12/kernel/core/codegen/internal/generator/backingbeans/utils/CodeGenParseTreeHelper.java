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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationInfo;
import com.mgmtp.a12.kernel.core.calc.internal.util.DynamicEnumerationInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenFilterCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenSetOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdsCollector;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.CodeGenCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.CodeGenOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.SetOpOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.conversion.CodeGenCreator;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.CalculationCodeGenUtilities;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.DivisionTransformer;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.ExpandService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ISetOpOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IDynamicEnumerationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService.RoundingMode;

import lombok.Getter;

/**
 * Helper class to create {@link CodeGenCondition}s and {@link CodeGenOperation}s.
 * The entities are collected in an {@link EntityIdsCollector}.
 * Also, the filter conditions ({@link IEntityId#getFilterCondition()}) are handled. They are
 * extracted and transformed and the variable name is set in the corresponding {@link IEntityId}.
 * The filterConditions can be extracted via {@link #getFilterConditions()} after the transformation.
 */
public class CodeGenParseTreeHelper {

	private final IEC ec;
	private final boolean expandGroups;
	private final ExpandMode expandMode;
	private final String uniqueFieldName;
	private final EntityIdsCollector filterEntityIdsCollector = new EntityIdsCollector("filter_");
	@Getter private final EntityIdsCollector setOperationEntityIdsCollector = new EntityIdsCollector("setOp_");
	private final String baseYear;
	private final Function<IEntityId, String> uniqueFieldNamesMapper;
	private final FilterConditionVisitor filterConditionVisitor = new FilterConditionVisitor();
	private final SetOperationVisitor setOperationVisitor = new SetOperationVisitor();

	/**
	 * @param ec
	 * 		The IEC containing the field information.
	 * @param expandGroups
	 * 		specifies, whether groups should be expanded in {@link ICondition}s.
	 * @param uniqueFieldName
	 * 		the unique field name of the error field.
	 * @param baseYear
	 * 		the base year.
	 * @param uniqueFieldNamesMapper
	 * 		a mapper to convert full names of entities to unique names.
	 */
	public CodeGenParseTreeHelper(IEC ec, boolean expandGroups, String uniqueFieldName, String baseYear,
			Function<IEntityId, String> uniqueFieldNamesMapper) {
		this.ec = ec;
		this.expandGroups = expandGroups;
		this.expandMode = expandGroups ? ExpandMode.EXPAND_AND_ADD_GROUPS : ExpandMode.EXPAND_PARTIALLY;
		this.uniqueFieldName = uniqueFieldName;
		this.baseYear = baseYear;
		this.uniqueFieldNamesMapper = uniqueFieldNamesMapper;
	}

	public CodeGenParseTreeHelper(IEC ec, boolean expandGroups, String uniqueFieldName, String baseYear,
			Map<String, String> uniqueFieldNames) {
		this(ec, expandGroups, uniqueFieldName, baseYear, (IEntityId eid) -> uniqueFieldNames.get(eid.getFullName()));
	}

	private final class FilterConditionVisitor extends ASTVisitor {
		private int filterNumber = 0;
		private final List<CodeGenFilterCondition> filterConditions = new ArrayList<>();

		@Override
		public boolean visit(IEntityId entityId) {
			if (entityId.getFilterCondition() != null) {
				String varName = "filterCond" + (filterNumber++);
				CodeGenFilterCondition codeGenFilterCondition = new CodeGenFilterCondition(
						convertCondition(entityId.getFilterCondition(), filterEntityIdsCollector), varName
				);
				filterConditions.add(codeGenFilterCondition);
				if (entityId instanceof EntityId) {
					((EntityId) entityId).setFilterConditionName(varName);
				}
			}
			return false;
		}
	}

	private final class SetOperationVisitor extends ASTVisitor {
		private int setOperationNumber = 0;
		private final List<CodeGenSetOperation> setOperations = new ArrayList<>();

		public boolean visit(ISetOpOperation setOp) {
			String varName = "setOp" + (setOperationNumber++);
			if (!(setOp instanceof SetOpOperation setOpOperation)) {
				throw new IllegalStateException("The SetOperationVisitor must only be called on already converted parse trees.");
			}
			setOpOperation.getOperation().traverse(setOperationEntityIdsCollector);
			CodeGenSetOperation setOperation = new CodeGenSetOperation(
					setOpOperation.getOperation(), varName
			);
			setOperations.add(setOperation);
			setOpOperation.setOperationVarName(varName);
			return false;
		}
	}

	public IDynamicEnumerationInfo expandAndTransformDynEnumInfo(IDynamicEnumerationInfo dynEnumInfo) {
		return new DynamicEnumerationInfo(dynEnumInfo.getDynamicEnumerationField(),
				expandAndTransform(dynEnumInfo.getSafeGuard()),
				expandAndTransformPartials(dynEnumInfo.getValueOperations(), null),
				dynEnumInfo.getName());
	}

	public List<ICalculationInfo> expandAndTransformCalcInfos(List<ICalculationInfo> calcInfos) {
		return calcInfos.stream()
				.map(this::expandAndTransformCalcInfo)
				.collect(Collectors.toList());
	}

	/**
	 * @param idCollector
	 * 		will collect entityIds and entityIdLists <em>and also, as a side-effect, in-place-assign unique
	 * 		varName to the converted EntityIds</em> (and lists?).
	 */
	public CodeGenCondition convertCondition(ICondition preCondition, EntityIdsCollector idCollector) {
		CodeGenCondition result = CodeGenCreator.convertCondition(preCondition, uniqueFieldNamesMapper,
				uniqueFieldName, baseYear, idCollector, ec, expandGroups);
		result.traverse(filterConditionVisitor);
		result.traverse(setOperationVisitor);
		return result;
	}

	/**
	 * @param idCollector
	 * 		will collect entityIds and entityIdLists <em>and also, as a side-effect, in-place-assign unique
	 * 		varName to the converted EntityIds</em> (and lists?).
	 */
	public CodeGenOperation convertOperation(IOperation operation, EntityIdsCollector idCollector) {
		CodeGenOperation result = CodeGenCreator.convertOperation(operation, uniqueFieldNamesMapper,
				uniqueFieldName, baseYear, idCollector, ec, expandGroups);
		result.traverse(filterConditionVisitor);
		result.traverse(setOperationVisitor);
		return result;
	}

	/**
	 * @param idCollector
	 * 		will collect entityIds and entityIdLists <em>and also, as a side-effect, in-place-assign unique
	 * 		varName to the converted EntityIds</em> (and lists?).
	 */
	public EntityId convertEntity(IEntityId entity, EntityIdsCollector idCollector) {
		EntityId result = CodeGenCreator.convertEntity(entity, uniqueFieldNamesMapper, idCollector);
		result.traverse(filterConditionVisitor);
		return result;
	}

	private ICalculationInfo expandAndTransformCalcInfo(ICalculationInfo calcInfo) {
		return new CalculationInfo(
				calcInfo.getCalculatedField(),
				expandAndTransform(calcInfo.getCommonPrecondition().orElse(null)),
				expandAndTransformPartials(
						calcInfo.getPartialCalculations(),
						calcInfo.getRoundingMode()),
				calcInfo.getRoundingMode(),
				calcInfo.getName()
		);
	}

	private List<Pair<ICondition, IOperation>> expandAndTransformPartials(List<Pair<ICondition, IOperation>> partials,
			RoundingMode roundingMode) {
		return partials.stream()
				.map(p -> expandAndTransformAlternative(p, roundingMode))
				.collect(Collectors.toList());
	}

	private Pair<ICondition, IOperation> expandAndTransformAlternative(Pair<ICondition, IOperation> p, RoundingMode roundingMode) {
		return Pair.of(
				expandAndTransform(p.getKey()),
				expandAndTransform(p.getValue(), roundingMode));
	}

	private IOperation expandAndTransform(IOperation operation, RoundingMode roundingMode) {
		IOperation expandedOperation;
		if (ExpandService.containsExpandableEntity(operation)) {
			expandedOperation = ExpandService.expandOperation(operation, ec, expandMode);
		} else {
			expandedOperation = operation;
		}
		return DivisionTransformer.transform(CalculationCodeGenUtilities.prefixRoundingOperation(expandedOperation, roundingMode));
	}

	private ICondition expandAndTransform(ICondition cond) {
		if (cond == null) {
			return null;
		}
		ICondition expandedCondition;
		if (ExpandService.containsExpandableEntity(cond)) {
			expandedCondition = ExpandService.expandBedingung(cond, ec, expandMode);
		} else {
			expandedCondition = cond;
		}
		return DivisionTransformer.transform(expandedCondition);
	}

	public List<CodeGenFilterCondition> getFilterConditions() {
		return filterConditionVisitor.filterConditions;
	}

	public List<CodeGenSetOperation> getSetOperations() {
		return setOperationVisitor.setOperations;
	}

	public EntityIdsCollector getFilterConditionEntityIdsCollector() {
		return filterEntityIdsCollector;
	}

}
