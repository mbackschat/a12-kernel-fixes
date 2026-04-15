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
package com.mgmtp.a12.kernel.core.facade.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.calc.internal.dynEnum.DynamicEnumerationService;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.facade.a12internal.ServiceFactory;
import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserProblemManager;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parser.internal.util.CheckerFuerKontextEinschraenkung;
import com.mgmtp.a12.kernel.core.parser.internal.util.CheckerFuerLfdNrEinschraenkung;
import com.mgmtp.a12.kernel.core.parser.internal.util.CheckerFuerUsbEinschraenkung;
import com.mgmtp.a12.kernel.core.parser.internal.util.CheckerFuerZeilenEinschraenkung;
import com.mgmtp.a12.kernel.core.parser.internal.util.ModelChanges;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IApplicationCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FindAllAst;
import com.mgmtp.a12.kernel.core.service.internal.extservices.MandatoryInformationService;
import com.mgmtp.a12.kernel.core.service.internal.serviceparams.MandatoryInformation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IDynamicEnumerationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMandatoryInformation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelChanges;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleCollectionMetaInformation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleParser;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporterProxy;
import com.mgmtp.a12.model.notification.Severity;

/**
 * Implementation of {@link IRuleCollectionMetaInformation} providing methods to extract meta information of rule collections.
 */
public class RuleCollectionMetaInformation implements IRuleCollectionMetaInformation {

	/**
	 * Entity Container providing all information about the fields and the groups.
	 */
	private final IEC ec;
	private final IServiceConfiguration serviceConfiguration;
	private final IRuleParser ruleParser;

	public RuleCollectionMetaInformation(IServiceConfiguration serviceConfiguration) {
		this.serviceConfiguration = serviceConfiguration;
		this.ec = serviceConfiguration.getIEC();
		this.ruleParser = new ServiceFactory().getRuleParser(serviceConfiguration);
	}

	@Override
	public List<String> getApplicationConditionNames(Collection<? extends IRule> rules, IProblemReporter pm) {
		Objects.requireNonNull(rules,
				"getApplicationConditionNames: Parameter 'rules' may not be null.");
		Objects.requireNonNull(pm, "getApplicationConditionNames: Parameter 'pm' may not be null.");

		Collection<ICondition> conditions = rules.stream()
				.map(rule -> ruleParser.parseBedingungNoExpand(rule, pm))
				.collect(Collectors.toList());
		return getApplicationConditionNames(conditions);
	}

	@Override
	public List<String> getApplicationConditionNames(Collection<ICondition> conditions) {
		Objects.requireNonNull(conditions,
				"getApplicationConditionNames: Parameter 'conditions' may not be null.");

		Set<String> result = new TreeSet<>();
		for (ICondition condition : conditions) {
			if (condition != null) {
				List<String> applicationNames = FindAllAst.findAll(condition,
								IApplicationCondition.class::isInstance, true).stream()
						.map(a -> ((IApplicationCondition) a).getName())
						.collect(Collectors.toList());
				result.addAll(applicationNames);
			}
		}
		return new ArrayList<>(result);
	}

	@Override
	public IMandatoryInformation getMandatoryInformationFromRules(Collection<? extends IRule> rules,
			IProblemReporter pm) {
		Objects.requireNonNull(rules,
				"getMandatoryInformationFromRules: Parameter 'rules' may not be null.");
		Objects.requireNonNull(pm,
				"getMandatoryInformationFromRules: Parameter 'pm' may not be null.");

		MVKParserProblemManager mvkPm = new MVKParserProblemManager();
		Map<IRule, ICondition> ruleToCondition = rules.stream()
				.filter(rule -> (IRule.RuleSeverityType.ERROR == rule.getSeverityType()))
				.collect(Java8Utils.toNullableMap(rule -> rule, rule -> ruleParser.parseBedingungExpand(rule, mvkPm)));
		mvkPm.transferProblems(pm);

		if (mvkPm.hasProblemOccured(Severity.ERROR)) {
			return new MandatoryInformation();
		}

		return getMandatoryInformationFromRules(ruleToCondition);
	}

	@Override
	public IMandatoryInformation getMandatoryInformationFromRules(Map<IRule, ICondition> rules) {
		Objects.requireNonNull(rules, "getMandatoryInformationFromRules: Parameter 'rules' may not be null.");

		MandatoryInformationService service = MandatoryInformationService.createInstance(rules, serviceConfiguration);
		return service.getMandatoryInformation();
	}

	@Override
	public IModelChanges getChangesAsSpecifiedByRules(Collection<? extends IRule> rules,
			IProblemReporter pm) {
		Objects.requireNonNull(rules,
				"getChangesAsSpecifiedByRules: Parameter 'rules' may not be null.");
		Objects.requireNonNull(pm, "getChangesAsSpecifiedByRules: Parameter 'pm' may not be null.");

		ProblemReporterProxy proxyPr = new ProblemReporterProxy(pm);
		Map<IRule, ICondition> ruleToCondition = rules.stream()
				.filter(rule -> (IRule.RuleSeverityType.ERROR == rule.getSeverityType()))
				.collect(Java8Utils.toNullableMap(rule -> rule,
						rule -> ruleParser.parseBedingungNoExpand(rule, proxyPr)));
		ruleToCondition.keySet()
				.removeIf(rule -> ruleToCondition.get(rule) == null); // if condition invalid

		if (proxyPr.hasProblemOccurred()) {
			return new ModelChanges();
		}
		return getChangesAsSpecifiedByRules(ruleToCondition, pm);
	}

	@Override
	public IModelChanges getChangesAsSpecifiedByRules(Map<IRule, ICondition> ruleToCondition, IProblemReporter pm) {
		ModelChanges modelChanges = new ModelChanges();

		ProblemReporterProxy proxyPr = new ProblemReporterProxy(pm);
		Locale language = serviceConfiguration.getModelSpecificSettings().getLanguageUsedInConditionText();
		CheckerFuerLfdNrEinschraenkung.checkAndSaveAnwendungsSpezifischeLfdNr(modelChanges,
				ruleToCondition, ec, proxyPr, language);
		// in an A12 model the row is not repeatable and there is no USB, so both cannot be changed by a rule
		if (ExtmpUtils.isHModel(serviceConfiguration.getModelSpecificSettings())) {
			CheckerFuerZeilenEinschraenkung.checkAndSaveAnwendungsSpezifischeZeile(modelChanges,
					ruleToCondition, ec, proxyPr, language);
			CheckerFuerUsbEinschraenkung.checkAndSaveAnwendungsSpezifischenUsb(modelChanges,
					ruleToCondition, ec, proxyPr, language);
		} else { // in an H model groups are not repeatable, so they cannot be restricted by a rule
			CheckerFuerKontextEinschraenkung.checkAndSaveAnwendungsSpezifischeKontextwiederholbarkeit(
					modelChanges, ruleToCondition, ec, proxyPr, language);
		}

		if (proxyPr.hasProblemOccurred()) {
			return new ModelChanges();
		}
		return modelChanges;
	}

	@Override
	public Map<String, Integer> getNoValidationLength(List<IRule> rules, IProblemReporter pr) {
		return rules.stream()
				.map(r -> ruleParser.parseBedingungNoExpand(r, pr))
				.filter(Objects::nonNull)
				.map(c -> AnalyseService.getNameAndLengthOfAllowedValueValidation(c, ec))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toMap(p -> p.fieldFullName(), p -> p.length()));
	}

	@Override
	public Map<String, List<String>> getDynamicEnumerationStringValues(List<IRule> rules,
			IProblemReporter pr) {
		DynamicEnumerationService dynEnumService = new DynamicEnumerationService(
				this.serviceConfiguration);
		return rules.stream()
				.map(r -> dynEnumService.checkAndExtractDynEnumInfo(r, pr))
				.map(de -> de.flatMap(this::checkAndConvertConstantDynEnum))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
	}

	private Optional<Pair<String, List<String>>> checkAndConvertConstantDynEnum(IDynamicEnumerationInfo dynEnumInfo) {
		if (dynEnumInfo.getSafeGuard() != null) {
			// No operators and therefore no safe-guards allowed
			return Optional.empty();
		}
		List<Pair<ICondition, IOperation>> operations = dynEnumInfo.getValueOperations();
		if (operations.stream().anyMatch(p -> p.getLeft() != null)) {
			// No pre-conditions for
			return Optional.empty();
		}
		String fieldName = dynEnumInfo.getDynamicEnumerationField().getFullName();
		Optional<List<String>> values = Java8Utils.reduceToOptionalList(
				operations.stream().map(p -> extractConstantValue(p.getRight())));
		return values.map(v -> Pair.of(fieldName, v));
	}

	private Optional<String> extractConstantValue(IOperation operation) {
		if (!(operation instanceof IConstantOperation)) {
			return Optional.empty();
		}
		return Optional.ofNullable(((IConstantOperation) operation).getValue());
	}
}
