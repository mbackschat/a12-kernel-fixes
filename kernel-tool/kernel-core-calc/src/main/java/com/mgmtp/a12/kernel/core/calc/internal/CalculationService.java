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

package com.mgmtp.a12.kernel.core.calc.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.calc.internal.util.BerechnungsFunktionFormatter;
import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationDependencies;
import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationErrorLookup;
import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationErrorType;
import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationUtils;
import com.mgmtp.a12.kernel.core.calc.internal.util.NormalisierteBerechnungsFormHelper;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.ado.IExtmpRule;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.ado.IExtmpRule.IterationInterval;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.ParserService;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule.RuleSeverityType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.ProblemException;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.ICalculationService;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.TopologicalSortHelper;
import com.mgmtp.a12.model.notification.Severity;

/**
 * An instance of <code>CalculationService</code> specifies an {@link ICalculationService} for
 * server-side calculations.
 *
 * @see ICalculationService
 */
public class CalculationService implements ICalculationService {

	private static final String ERROR_PROBLEM_REPORTER_NULL = "Problem reporter may not be null.";
	private static final String ERROR_CALCRULE_NULL = "calculationRule may not be null.";
	private static final String ERROR_RULE_NULL = "The rule may not be null.";
	protected IServiceConfiguration serviceConfig;
	protected ParserService parserService;
	protected CalculationErrorLookup errorLookup;

	private List<ICalculationRule> calculationRules;

	public CalculationService(IServiceConfiguration serviceConfiguration) {
		this.serviceConfig = serviceConfiguration;
		this.parserService = new ParserService(serviceConfig);
		this.errorLookup = new CalculationErrorLookup(serviceConfig.getModelSpecificSettings().getLanguageUsedInConditionText());
	}

	public CalculationService(IServiceConfiguration serviceConfiguration, List<ICalculationRule> calculationRules) {
		this(serviceConfiguration);
		this.calculationRules = calculationRules;
	}

	public Optional<List<ICalculationInfo>> checkAndExtractAllCalculationInfo(List<ICalculationRule> calcRules,
			IProblemReporter pr) {
		// Optimization is possible by parsing the rules only once.

		if (!checkAllCalculationRules(calcRules, pr)) {
			return Optional.empty();
		}

		return extractAllCalculationInfo(calcRules, pr);

	}

	private Optional<List<ICalculationInfo>> extractAllCalculationInfo(List<ICalculationRule> calcRules, IProblemReporter pr) {
		List<Optional<ICalculationInfo>> result = calcRules.stream()
				.map(cr -> extractCalculationInfo(cr, pr))
				.collect(Collectors.toList());
		if (result.stream().anyMatch(o -> !o.isPresent())) {
			return Optional.empty();
		}
		return Optional.of(result.stream().map(Optional::get).collect(Collectors.toList()));
	}

	private boolean checkAllCalculationRules(List<ICalculationRule> calcRules, IProblemReporter pr) {
		Map<String, List<ICalculationRule>> rulesByField = calcRules.stream()
				.collect(Collectors.groupingBy(cr -> cr.getCalculatedField().getFullName()));

		return rulesByField.values().stream()
				.map(crs -> crs.stream()
						.map(cr -> checkCalculationRule(cr, crs, pr))
						.reduce(true, (a, b) -> (a && b)))
				.reduce(true, (a, b) -> (a && b));
	}

	public static class RuleRef implements IRuleRef {
		private final String fullName;

		public RuleRef(String fullName) {
			this.fullName = fullName;
		}

		@Override public String getFullName() {
			return this.fullName;
		}

		@Override public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof RuleRef))
				return false;
			RuleRef ruleRef = (RuleRef) o;
			return Objects.equals(fullName, ruleRef.fullName);
		}

		@Override public int hashCode() {
			return Objects.hash(fullName);
		}

		@Override public String toString() {
			return "RuleRef{" +
					"fullName='" + fullName + '\'' +
					'}';
		}
	}

	public static class DependentFieldRef implements IDependentFieldRef {
		private final String referencedFieldName;

		public DependentFieldRef(String referencedFieldName) {
			this.referencedFieldName = referencedFieldName;
		}

		@Override public String getReferencedFieldName() {
			return this.referencedFieldName;
		}

		@Override public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof DependentFieldRef))
				return false;
			DependentFieldRef that = (DependentFieldRef) o;
			return Objects.equals(referencedFieldName, that.referencedFieldName);
		}

		@Override public int hashCode() {
			return Objects.hash(referencedFieldName);
		}

		@Override public String toString() {
			return "DependentFieldRef{" +
					"referencedFieldName='" + referencedFieldName + '\'' +
					'}';
		}
	}

	/**
	 * Checks whether
	 * <ul>
	 * <li>the iteration is <em>not</em> restricted.</li>
	 * <li>the rule is given in the prescribed canonical form.</li>
	 * </ul>
	 * Precondition:
	 * The <code>condition</code> must not be expanded, i.e. the condition text has to be parsed
	 * with {@link ExpandMode#NO_EXPAND}.
	 */
	private boolean testRuleStructure(IRule r, ICondition condition, IProblemReporter pr) {
		Objects.requireNonNull(r, ERROR_RULE_NULL);
		Objects.requireNonNull(pr, ERROR_PROBLEM_REPORTER_NULL);

		if (condition == null) {
			// The error message has to be set by the parser!
			return false;
		}
		if (isIterationRestricted(r)) {
			errorLookup.reportError(pr, r, CalculationErrorType.MVK_ERROR_RESTRICTED_CALCULATION_RULE);
			return false;
		}

		IEntity errorEntity = r.getErrorEntity();
		if (!errorEntity.isField()) {
			errorLookup.reportError(pr, r, CalculationErrorType.MVK_GROUP_AS_CALCULATED_FIELD, errorEntity.getFullName());
			return false;
		}
		IField errorField = (IField) errorEntity;

		if (AnalyseService.usesSemanticIndexOrCategoriesForErrorField(condition, errorField.getFullName())) {
			errorLookup.reportError(pr, r, CalculationErrorType.MVK_ERROR_SEMANTIC_INDEX_OR_CATEGORY_FOR_ERRORFIELD);
			return false;
		}

		// Falls eine Fehlermeldung zurückkommt -> kein Fehlerfeld
		return getBerechnungsHelper(pr, r)
				.checkNormalisierteForm(condition, errorField);
	}

	@Override
	public Optional<ICalculationInfo> extractCalculationInfo(ICalculationRule cr, IProblemReporter pr) {
		Objects.requireNonNull(cr, ERROR_CALCRULE_NULL);
		Objects.requireNonNull(pr, ERROR_PROBLEM_REPORTER_NULL);

		ICondition condition = parserService.parseAndCheckCondition(cr.getRule(), cr.getRule().getErrorConditionText(), pr,
				ExpandMode.NO_EXPAND);
		return getBerechnungsHelper(pr, cr.getRule())
				.extractCalculationInfo(condition, cr.getRoundingMode(), cr.getCalculatedField());
	}

	private NormalisierteBerechnungsFormHelper getBerechnungsHelper(IProblemReporter pm, IRule rule) {
		CalculationErrorLookup errorLookup = new CalculationErrorLookup(
				serviceConfig.getModelSpecificSettings().getLanguageUsedInConditionText());
		return new NormalisierteBerechnungsFormHelper(errorLookup, pm, rule, serviceConfig.getIEC(),
				ExtmpUtils.isHModel(serviceConfig.getModelSpecificSettings()), serviceConfig.getModelSpecificSettings(),
				serviceConfig.getProjectSpecificSettings(), serviceConfig.strictness());
	}

	@Override
	public Optional<String> getCalculationTextForRule(ICalculationRule cr, IProblemReporter pr) {
		if (cr == null || cr.getRule() == null || cr.getRoundingMode() == null) {
			return Optional.empty();
		}
		if (pr == null) {
			pr = p -> {
			};
		}

		IRule rule = cr.getRule();
		ICondition nonExpandedCondition = parserService.parseAndCheckCondition(rule, rule.getErrorConditionText(), pr,
				ExpandMode.NO_EXPAND);
		return getCalculationTextForRule(cr, nonExpandedCondition, pr);
	}

	/**
	 * Precondition:
	 * The <code>nonExpandedCondition</code> may not be expanded, i.e. the condition text has to be
	 * parsed with {@link ExpandMode#NO_EXPAND}.
	 */
	@Override
	public Optional<String> getCalculationTextForRule(ICalculationRule cr, ICondition nonExpandedCondition, IProblemReporter pr) {
		if (cr == null || cr.getRule() == null || cr.getRoundingMode() == null) {
			return Optional.empty();
		}
		if (pr == null) {
			pr = p -> {
			};
		}

		if (nonExpandedCondition == null || !checkCalculationRule(cr, nonExpandedCondition, pr)) {
			return Optional.empty();
		}
		Optional<ICalculationInfo> info = getBerechnungsHelper(pr, cr.getRule())
				.extractCalculationInfo(nonExpandedCondition, cr.getRoundingMode(), cr.getCalculatedField());
		return info.map(sbi -> new BerechnungsFunktionFormatter(serviceConfig).formatiereBerechnungsFunktion(sbi, 80));
	}

	@Override
	public boolean checkCalculationRule(ICalculationRule calculationRule, List<ICalculationRule> fieldCalculations,
			IProblemReporter pm) {
		Objects.requireNonNull(calculationRule, ERROR_CALCRULE_NULL);
		if (pm == null) {
			pm = new ProblemReporter();
		}

		IRule rule = calculationRule.getRule();
		ICondition bedingung = parserService.parseAndCheckCondition(rule, rule.getErrorConditionText(), pm, ExpandMode.NO_EXPAND);
		return checkCalculationRule(calculationRule, bedingung, pm);
	}

	@Override
	public boolean checkCalculationRule(ICalculationRule calculationRule, ICondition condition, IProblemReporter pm) {
		Objects.requireNonNull(calculationRule, ERROR_CALCRULE_NULL);
		Objects.requireNonNull(pm, "problem reporter.");

		if (condition == null) {
			return false;
		}
		IField field = calculationRule.getCalculatedField();
		IRule rule = calculationRule.getRule();
		if (rule.getSeverityType() != RuleSeverityType.ERROR) {
			errorLookup.reportError(pm, rule, CalculationErrorType.MVK_CALC_INVALID_ACTION_TYPE);
			return false;
		}
		//Berechnetes Feld muss Fehlerfeld der Regel sein.
		if (!field.getFullName().equals(rule.getErrorEntity().getFullName())) {
			errorLookup.reportError(pm, rule, CalculationErrorType.MVK_ERROR_WRONG_CALCULATED_FIELD, field.getFullName());
			return false;
		}
		if (EntityUtils.isIndexField(field, serviceConfig.getIEC())) {
			errorLookup.reportError(pm, rule, CalculationErrorType.MVK_ERROR_INDEX_AS_CALCULATED_FIELD, field.getFullName());
			return false;
		}

		if (field.getFullName().contains(".") || field.getFullName().contains(":")) {
			errorLookup.reportError(pm, rule, CalculationErrorType.MVK_ERROR_INVALID_FIELDNAME, field.getFullName());
			return false;
		}

		if (!testRuleStructure(rule, condition, pm)) {
			return false;
		}

		Optional<ICalculationInfo> info = getBerechnungsHelper(pm, rule)
				.extractCalculationInfo(condition, calculationRule.getRoundingMode(), calculationRule.getCalculatedField());
		return info.isPresent();
	}

	public static boolean isIterationRestricted(IRule regel) {
		Optional<IterationInterval> lfdNrInterval = regel instanceof IExtmpRule extmpRule
				? extmpRule.getRootgroupIterationRestriction()
				: Optional.empty();
		if (lfdNrInterval.isPresent() && (lfdNrInterval.get().getStart() > 1 || lfdNrInterval.get()
				.getEnd() < EntityUtils.getMaxLfdNr(regel.getErrorEntity()))) {
			return true;
		}

		Optional<IterationInterval> zeileIntervall = regel instanceof IExtmpRule extmpRule
				? extmpRule.getRowIterationRestriction()
				: Optional.empty();
		if (zeileIntervall.isPresent()) {
			return (zeileIntervall.get().getStart() > 1) || (zeileIntervall.get().getEnd() < EntityUtils.getMaxZeile(
					(IField) regel.getErrorEntity()));
		}
		return false;
	}

	@Override
	public Optional<List<Pair<String, IRefType>>> checkForCycle(Map<String, Set<String>> extraDependencies)
			throws ProblemException {
		return getCycle(getCalculationDependencies(extraDependencies));
	}

	private CalculationDependencies getCalculationDependencies(Map<String, Set<String>> extraDependencies) throws ProblemException {
		ProblemReporter pr = new ProblemReporter();
		Optional<List<ICalculationInfo>> calcInfosOpt = this.checkAndExtractAllCalculationInfo(calculationRules, pr);
		if (calcInfosOpt.isEmpty() || pr.hasProblemOccurred(Severity.ERROR)) {
			throw new ProblemException("The given calculations are not parsable!", pr.getProblems());
		}

		List<ICalculationInfo> calculationInfos = calcInfosOpt.get().stream().map(ci ->
				CalculationUtils.addImplicitGroupFilledToCommonPreconditionIfApplicable(ci, serviceConfig.getIEC())).toList();
		return new CalculationDependencies(
				calculationInfos,
				serviceConfig.getIEC(),
				ExtmpUtils.isHModel(serviceConfig.getModelSpecificSettings()),
				extraDependencies
		);
	}

	public Optional<List<Pair<String, IRefType>>> getCycle(CalculationDependencies calculationDependencies) {
		TopologicalSortHelper.TopologicalSortResult<String> result =
				TopologicalSortHelper.topologischeSortierung(calculationDependencies.getDependencyGraphCopy());

		if (result.isSuccessful()) {
			return Optional.empty();
		} else {
			return Optional.of(augmentCycleInfo(
					result.getCycle(),
					(n1, n2) -> pickOneDeterministically(calculationDependencies.getEdgeLabel(n1, n2))
			));
		}
	}

	/**
	 * (relies on a deterministic toString implementation of the elems)
	 */
	private static IRefType pickOneDeterministically(Collection<IRefType> elems) {
		return elems.stream().min(Comparator.comparing(IRefType::toString)).orElse(null);
	}

	private static List<Pair<String, ICalculationService.IRefType>> augmentCycleInfo(
			List<String> cycle,
			BiFunction<String, String, IRefType> edge2Label
	) {
		List<Pair<String, ICalculationService.IRefType>> result = new ArrayList<>(cycle.size());
		for (int i = 0; i < cycle.size() - 1; i++) {
			ICalculationService.IRefType rule = edge2Label.apply(cycle.get(i), cycle.get(i + 1));
			result.add(Pair.of(cycle.get(i), rule));
		}
		String lastField = cycle.get(cycle.size() - 1);
		ICalculationService.IRefType wrappingRule = edge2Label.apply(lastField, cycle.get(0));
		result.add(Pair.of(lastField, wrappingRule));
		return result;
	}
}
