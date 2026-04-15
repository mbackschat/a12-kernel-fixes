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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.calc.internal.CalculationService;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.ICalculationService;

/**
 * A helper class to represent the relevant dependencies of calculations within a model.
 */
public class CalculationDependencies {
	private final Map<String, Map<ICalculationService.IRefType, Set<String>>> calculatedFieldToRuleRefToFieldDependencies
			= new HashMap<>();
	private final Set<String> expandedOperandFieldsOfCalculations = new HashSet<>();
	private Map<String, Set<String>> dependencyGraph = null;
	private Map<Pair<String, String>, Set<ICalculationService.IRefType>> edgeLabels = null;
	private final Map<String, List<ICalculationInfo>> calculatedFieldToCalculationInfos;
	private final IEC ec;
	private final boolean isHModel;
	private final Map<String, Set<String>> supplementalDependencyGraph;

	public CalculationDependencies(List<ICalculationInfo> calculationInfos, IEC ec, boolean isHModel,
			Map<String, Set<String>> supplementalDependencyGraph) {
		this.calculatedFieldToCalculationInfos = calculationInfos.stream()
				.collect(Collectors.groupingBy(ci -> ci.getCalculatedField().getFullName()));
		this.ec = ec;
		this.isHModel = isHModel;
		this.supplementalDependencyGraph = supplementalDependencyGraph;
		gatherFieldReferencesInCalculations();
	}

	public List<ICalculationInfo> getCalculationInfo(String fieldFullName) {
		return calculatedFieldToCalculationInfos.get(fieldFullName);
	}

	/**
	 * @return a set of all operand fields (full names) of all calculations (with groups expanded to all contained fields) for
	 * 		A12-Models, or an empty set for H-Models
	 * 		<p>
	 * 		While "dependencies" represent constraints on the order of calculations, "operands" describe the super set of entities
	 * 		that are relevant to the execution of calculations in any way and must be formally validated. Every field dependency is
	 * 		always an operand field, but not all operand fields are necessarily field dependencies. Since this info is currently not
	 * 		needed on a per-field basis, we simply collect the unique set of all operand fields here.
	 */
	public Set<String> getExpandedOperandFieldsOfCalculations() {
		// Note: it's safe to return  an empty set for H-DVs, because
		// CalculationCommand#getDataWithoutEntitiesNotNeededForCalculations will ignore the operand fields set anyway, for H-DVs
		return isHModel ? Collections.emptySet() : expandedOperandFieldsOfCalculations;
	}

	/**
	 * @return a map describing fields relevant to dependency sorting of calculations.
	 * 		<p>
	 * 		A key in the map is the full name of a calculated field and the corresponding value is a map where the keys are the full
	 * 		names of calculation rules that calculate said calculated field and the values are a set of full names of fields
	 * 		referenced directly or indirectly (via their group) in that rule (without the calculated field itself) as relevant for
	 * 		dependency sorting (i.e. only fields that have to be calculated before the respective calculated field).
	 * 		<p>
	 * 		The set of referenced fields will not include fields contained in groups referenced via a single-argument 'GroupFilled'
	 * 		where the referenced group is an ancestor or equal to the field's conditional calculation group. Non-calculated fields
	 * 		are treated like externally computed fields, with their innermost repeatable group as conditional calculation group.
	 * 		<p>
	 * 		calculated field -> (calculation rule -> referenced fields)
	 */
	private Map<String, Map<ICalculationService.IRefType, Set<String>>> getCalculatedFieldToRuleRefToFieldDependencies() {
		return calculatedFieldToRuleRefToFieldDependencies;
	}

	private void gatherFieldReferencesInCalculations() {
		Map<String, Set<String>> groupToUnconditionallyCalculatedFields = getFieldsByGroupWithoutConditionallyCalculatedFields();

		calculatedFieldToCalculationInfos.forEach((calculatedFieldFullName, calculationInfos) -> {
			var ruleRefToFieldDependencies = calculatedFieldToRuleRefToFieldDependencies.computeIfAbsent(calculatedFieldFullName,
					k -> new HashMap<>());

			for (ICalculationInfo calculationInfo : calculationInfos) {
				ICalculationService.IRefType ruleRef = new CalculationService.RuleRef(calculationInfo.getName());
				Set<String> fieldDependencies = ruleRefToFieldDependencies.computeIfAbsent(ruleRef, k -> new HashSet<>());

				// 0. find entities referenced in calculation
				CalculationOperandsCollector collectedOperands = CalculationOperandsCollector.collectOperands(calculationInfo);

				// 1. directly referenced fields are always a dependency
				fieldDependencies.addAll(collectedOperands.getFieldReferences());
				expandedOperandFieldsOfCalculations.addAll(collectedOperands.getFieldReferences());

				// 2. all fields contained in groups referenced "normally" (as opposed to 3.) are always a dependency
				for (String referencedGroup : collectedOperands.getNormalGroupReferences()) {
					getAllFieldFullnamesInGroup(referencedGroup).forEach(containedField -> {
						fieldDependencies.add(containedField);
						expandedOperandFieldsOfCalculations.add(containedField);
					});
				}

				// 3. references to an ancestor group of the calculated field via a single-argument "GroupFilled" will only
				// create a dependency on fields which are "unconditional" to that group (see definition of conditional calculated
				// group in CalculationUtils#extractConditionalCalculationGroup)
				// reason: only for these fields can the order of execution cause the result of the language construct to change
				for (String referencedGroup : collectedOperands.getConditionalGroupReferences()) {
					Set<String> unconditionalCalculatedFields = new HashSet<>(
							groupToUnconditionallyCalculatedFields.getOrDefault(referencedGroup, Collections.emptySet()));
					unconditionalCalculatedFields.remove(calculatedFieldFullName);
					fieldDependencies.addAll(unconditionalCalculatedFields);

					// all contained fields are operands
					expandedOperandFieldsOfCalculations.addAll(getAllFieldFullnamesInGroup(referencedGroup));
				}

				// 4. for all index groups referenced in the calculation via semantic index or parallel iteration, the index fields
				// must be considered dependencies
				for (String indexGroupFullName : collectedOperands.getIndexGroupReferences()) {
					IGroup indexGroup = (IGroup) ec.get(indexGroupFullName);
					assert indexGroup.getIndexFieldNames().size() <= 1 :
							"expected zero or one index fields (that are directly in the repeatable group). " +
									"Our abspath construction might be incorrect if this assumption does not hold), but got "
									+ indexGroup.getIndexFieldNames();
					for (String indexFieldName : indexGroup.getIndexFieldNames()) {
						String indexFieldFullName = EntityUtils.createPath(indexGroupFullName, indexFieldName);
						fieldDependencies.add(indexFieldFullName);
						expandedOperandFieldsOfCalculations.add(indexFieldFullName);
					}
				}
			}
		});
	}

	private Set<String> getAllFieldFullnamesInGroup(String groupFullName) {
		return ec.getAllContainedFields(groupFullName).stream().map(IEntity::getFullName).collect(Collectors.toSet());
	}

	/**
	 * @return a map where the key is the full name of a group, and the value is a set of fields (full names) which are
	 * 		contained in that group but do not have the group or one of its descendants as a conditional calculation group (i.e.
	 * 		could cause the group to become filled).
	 * 		<p>
	 * 		For calculated fields, the conditional calculation group is extracted from the common precondition of the associated
	 * 		calculation rules.
	 * 		<p>
	 * 		All other fields (i.e. not calculated by DM rules), are treated as if they were externally calculated (i.e. as if they
	 * 		had their innermost repeatable group as a conditional calculation group).
	 */
	private Map<String, Set<String>> getFieldsByGroupWithoutConditionallyCalculatedFields() {
		Map<String, Set<String>> groupToUnconditionalCalculatedFields = new HashMap<>();
		calculatedFieldToCalculationInfos.forEach((calculatedFieldFullName, calculationInfos) -> {
			String conditionalCalculationGroupFullName = CalculationUtils.extractConditionalCalculationGroup(
					calculatedFieldFullName, calculationInfos);
			int conditionalCalculationGroupLevel = conditionalCalculationGroupFullName.isEmpty()
					? -1 // if there is no conditional calculation group, the calculated field is unconditional for all levels
					: EntityUtils.getLevel(conditionalCalculationGroupFullName);
			int calculatedFieldLevel = EntityUtils.getLevel(calculatedFieldFullName);
			for (int level = conditionalCalculationGroupLevel + 1; level < calculatedFieldLevel; level++) {
				String groupFullName = EntityUtils.getPathUpToLevel(level, calculatedFieldFullName);
				groupToUnconditionalCalculatedFields
						.computeIfAbsent(groupFullName, k -> new HashSet<>())
						.add(calculatedFieldFullName);
			}
		});

		// externally calculated fields can cause non-repeatable (but never repeatable) groups to be filled, because they
		// functionally have their innermost repeatable group as a conditional calculation group
		if (!isHModel) {
			for (IField field : EntityUtils.getAllFields(ec)) {
				String fieldFullName = field.getFullName();
				// calculated fields were already handled, everything else we treat as an externally calculated field
				if (calculatedFieldToCalculationInfos.containsKey(fieldFullName)) {
					continue;
				}

				long[] maxIndices = field.getMax();
				int level = maxIndices.length - 2;
				while (level >= 0 && maxIndices[level] == 1) {
					String groupFullName = EntityUtils.getPathUpToLevel(level, fieldFullName);
					groupToUnconditionalCalculatedFields
							.computeIfAbsent(groupFullName, k -> new HashSet<>())
							.add(fieldFullName);
					level--;
				}
			}
		}

		return groupToUnconditionalCalculatedFields;
	}

	/**
	 * @return the full dependency graph including
	 * 		- all calculated fields with the fields that have to be calculated before them (calculated and non-calculated)
	 * 		- all entries in the {@code supplementalDependencyGraph}.
	 * 		<p>
	 * 		The nodes in the graph are the full names of fields. For a key node in the map, each node listed in the value
	 * 		represents a dependency, i.e. the key depends on the nodes in value being calculated first.
	 */
	public Map<String, Set<String>> getDependencyGraph() {
		if (dependencyGraph == null) {
			dependencyGraph = new HashMap<>();
			// Flatten the "Map fieldname -> map rule -> operands" into "Map fieldname -> operands"
			getCalculatedFieldToRuleRefToFieldDependencies().forEach((calculatedField, ruleRefToReferencedFields) -> {
				Set<String> dependencies = dependencyGraph.computeIfAbsent(calculatedField, k -> new HashSet<>());
				ruleRefToReferencedFields.forEach((ruleRef, operands) -> dependencies.addAll(operands));
			});
			supplementalDependencyGraph.forEach((dependent, dependencies) ->
					dependencyGraph.computeIfAbsent(dependent, k -> new HashSet<>()).addAll(dependencies));
		}
		return dependencyGraph;
	}

	public Map<String, Set<String>> getDependencyGraphCopy() {
		return Java8Utils.mapMapValues(getDependencyGraph(), set -> new HashSet<>(set));
	}

	private Map<Pair<String, String>, Set<ICalculationService.IRefType>> getEdgeLabels() {
		if (edgeLabels == null) {
			edgeLabels = new HashMap<>();
			getCalculatedFieldToRuleRefToFieldDependencies().forEach((calculatedField, ruleToOperandsMap) ->
					ruleToOperandsMap.forEach((rule, operandsSet) ->
							operandsSet.forEach(operand -> {
								var edgeKey = Pair.of(calculatedField, operand);
								edgeLabels.computeIfAbsent(edgeKey, k -> new HashSet<>()).add(rule);
							})));

			supplementalDependencyGraph.forEach((targetNode, startNodes) ->
					startNodes.forEach(startNode ->
							// we only report one cause per edge in a cycle anyway, so here we can
							// arbitrarily decide to pick RuleRefs over DependentFieldRefs
							edgeLabels.computeIfAbsent(
									Pair.of(targetNode, startNode),
									// Optionally we could add info about the startNode too
									k -> Set.of(new CalculationService.DependentFieldRef(targetNode)))));
		}
		return edgeLabels;
	}

	/**
	 * @return the {@link ICalculationService.IRefType}s that caused {@code targetNode} to have a dependency on {@code startNode},
	 * 		or null if unknown.
	 * 		<p>
	 * 		E.g. if calculated field {@code /G/F1} is referenced in the calculation rule {@code /G/R1} of the calculated field
	 *        {@code /G/F2}, the call {@code getEdgeLabel("/G/F2", "/G/F1")} will return a {@link Set} containing an
	 *        {@link ICalculationService.IRuleRef} for {@code /G/R1}.
	 */
	public Set<ICalculationService.IRefType> getEdgeLabel(String targetNode, String startNode) {
		return getEdgeLabels().get(Pair.of(targetNode, startNode));
	}
}
