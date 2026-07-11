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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.facade.a12internal.ServiceFactory;
import com.mgmtp.a12.kernel.core.parser.internal.ParserService;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parser.internal.util.ReferencedIdsUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.IndexArt;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMandatoryInformation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelChanges;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ServiceConfiguration;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IMVK_Service;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleCollectionMetaInformation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleMetaInformation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleParser;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;

import lombok.NonNull;

/**
 * implements the MVK Services
 */
public class MVKService implements IMVK_Service {

	/**
	 * Entity Container providing all information about the fields and the groups.
	 */
	private final IEC ec;
	private final IModelSpecificSettings modelSpecificSettings;
	private final IServiceConfiguration serviceConfiguration;
	/**
	 * project specific settings.
	 */
	private final IProjectSpecificSettings pss;
	private final IRuleParser ruleParser;
	private final IRuleCollectionMetaInformation ruleCollectionMetaInformation;
	private final IRuleMetaInformation ruleMetaInformation;
	private final ServiceFactory serviceFactory = new ServiceFactory();

	public MVKService(IServiceConfiguration serviceConfiguration) {
		this.serviceConfiguration = serviceConfiguration;
		this.ec = serviceConfiguration.getIEC();
		this.pss = serviceConfiguration.getProjectSpecificSettings();
		this.modelSpecificSettings = serviceConfiguration.getModelSpecificSettings();
		this.ruleParser = serviceFactory.getRuleParser(serviceConfiguration);
		this.ruleCollectionMetaInformation = serviceFactory.getRuleCollectionMetaInformation(serviceConfiguration);
		this.ruleMetaInformation = serviceFactory.getRuleMetaInformation(serviceConfiguration);
	}

	public MVKService(IEC entityContainer, IModelSpecificSettings modelSpecificSettings, IProjectSpecificSettings pss) {
		this(entityContainer, modelSpecificSettings, pss, ParserStrictness.STRICT);
	}

	public MVKService(IEC entityContainer, IModelSpecificSettings modelSpecificSettings, IProjectSpecificSettings pss,
			ParserStrictness strictness) {
		this.serviceConfiguration = new ServiceConfiguration(entityContainer, modelSpecificSettings, pss, strictness);
		this.ec = entityContainer;
		this.pss = pss;
		this.modelSpecificSettings = modelSpecificSettings;
		this.ruleParser = serviceFactory.getRuleParser(serviceConfiguration);
		this.ruleCollectionMetaInformation = serviceFactory.getRuleCollectionMetaInformation(serviceConfiguration);
		this.ruleMetaInformation = serviceFactory.getRuleMetaInformation(serviceConfiguration);
	}

	/** {@link IServiceConfiguration} implementations */

	@Override
	public IEC getIEC() {
		return this.ec;
	}

	@Override
	public IModelSpecificSettings getModelSpecificSettings() {
		return modelSpecificSettings;
	}

	@Override
	public IProjectSpecificSettings getProjectSpecificSettings() {
		return pss;
	}

	/** {@link IRuleParser} implementations */

	@Override
	public ICondition parseConditionExpand(@NonNull IRule rule, @NonNull String conditionText, @NonNull IProblemReporter pm) {
		return ruleParser.parseConditionExpand(rule, conditionText, pm);
	}

	@Override
	public ICondition parseConditionNoExpand(@NonNull IRule rule, @NonNull String conditionText, @NonNull IProblemReporter pm) {
		return ruleParser.parseConditionNoExpand(rule, conditionText, pm);
	}

	@Override
	public ICondition expandCondition(ICondition condition) {
		return ruleParser.expandCondition(condition);
	}

	@Override
	public boolean hasValidConditionText(@NonNull IRule rule, @NonNull IProblemReporter pm) {
		return ruleParser.hasValidConditionText(rule, pm);
	}

	@Override
	public boolean hasValidErrorText(@NonNull IRule rule, @NonNull Collection<Locale> locales, boolean extendProblemMessages,
			@NonNull IProblemReporter pm) {
		return ruleParser.hasValidErrorText(rule, locales, extendProblemMessages, pm);
	}

	@Override public boolean hasValidCustomErrorEntities(@NonNull IRule rule, @NonNull IProblemReporter pm) {
		return ruleParser.hasValidCustomErrorEntities(rule, pm);
	}

	@Override
	public boolean hasValidErrorText(@NonNull IRule rule, ICondition condition, @NonNull Collection<Locale> locales,
			boolean extendProblemMessages,
			@NonNull IProblemReporter pm) {
		return ruleParser.hasValidErrorText(rule, condition, locales, extendProblemMessages, pm);
	}

	@Override
	public boolean hasValidErrorText(@NonNull IRule rule, @NonNull Map<Locale, String> errorTexts,
			@NonNull Collection<Locale> locales, boolean extendProblemMessages, @NonNull IProblemReporter pm) {
		return ruleParser.hasValidErrorText(rule, errorTexts, locales, extendProblemMessages, pm);
	}

	@Override
	public boolean hasValidErrorText(@NonNull IRule rule, @NonNull Map<Locale, String> errorTexts,
			@NonNull Collection<Locale> locales, boolean extendProblemMessages, @NonNull IProblemReporter pm,
			ICondition condition) {
		return ruleParser.hasValidErrorText(rule, errorTexts, locales, extendProblemMessages, pm, condition);
	}

	@Override
	public ICondition parseBedingungExpand(@NonNull IRule rule, @NonNull IProblemReporter pm) {
		return ruleParser.parseBedingungExpand(rule, pm);
	}

	@Override
	public ICondition parseBedingungExpandPartially(@NonNull IRule rule, @NonNull IProblemReporter pm) {
		return ruleParser.parseBedingungExpandPartially(rule, pm);
	}

	@Override
	public ICondition parseBedingungNoExpand(@NonNull IRule rule, @NonNull IProblemReporter pm) {
		return ruleParser.parseBedingungNoExpand(rule, pm);
	}

	@Override public ICondition parseBedingungSnippetNoExpand(@NonNull String bedingungSnippet, @NonNull IRule rule,
			@NonNull IProblemReporter pm) {
		return ruleParser.parseBedingungSnippetNoExpand(bedingungSnippet, rule, pm);
	}

	@Override public IOperation parseOperationSnippetNoExpand(@NonNull String operationSnippet, @NonNull IRule rule,
			@NonNull IProblemReporter pm) { // TODO does this need to be API?
		return ruleParser.parseOperationSnippetNoExpand(operationSnippet, rule, pm);
	}

	/** {@link com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleCollectionMetaInformation} implementations */

	@Override
	public @NonNull List<String> getApplicationConditionNames(
			@NonNull Collection<? extends IRule> rules, @NonNull IProblemReporter pm) {
		return ruleCollectionMetaInformation.getApplicationConditionNames(rules, pm);
	}

	@Override
	public @NonNull List<String> getApplicationConditionNames(@NonNull Collection<ICondition> conditions) {
		return ruleCollectionMetaInformation.getApplicationConditionNames(conditions);
	}

	@Override
	public IMandatoryInformation getMandatoryInformationFromRules(Collection<? extends IRule> rules, IProblemReporter pm) {
		return ruleCollectionMetaInformation.getMandatoryInformationFromRules(rules, pm);
	}

	@Override
	public IMandatoryInformation getMandatoryInformationFromRules(Map<IRule, ICondition> rules) {
		return ruleCollectionMetaInformation.getMandatoryInformationFromRules(rules);
	}

	@Override
	public IModelChanges getChangesAsSpecifiedByRules(Collection<? extends IRule> rules, IProblemReporter pm) {
		return ruleCollectionMetaInformation.getChangesAsSpecifiedByRules(rules, pm);
	}

	@Override
	public IModelChanges getChangesAsSpecifiedByRules(Map<IRule, ICondition> ruleToCondition, IProblemReporter pm) {
		return ruleCollectionMetaInformation.getChangesAsSpecifiedByRules(ruleToCondition, pm);
	}

	@Override
	public Map<String, Integer> getNoValidationLength(List<IRule> rules, IProblemReporter pr) {
		return ruleCollectionMetaInformation.getNoValidationLength(rules, pr);
	}

	@Override
	public Map<String, List<String>> getDynamicEnumerationStringValues(List<IRule> rules, IProblemReporter pr) {
		return ruleCollectionMetaInformation.getDynamicEnumerationStringValues(rules, pr);
	}

	/** {@link com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleMetaInformation} implementations */

	@Override
	public Set<IEntity> getDirectlyReferencedEntities(IRule rule, IProblemReporter pm) {
		return ruleMetaInformation.getDirectlyReferencedEntities(rule, pm);
	}

	@Override
	public Set<IEntity> getDirectlyReferencedEntities(ICondition condition) {
		return ruleMetaInformation.getDirectlyReferencedEntities(condition);
	}

	@Override public Set<String> getCandidateRelpathsForCustomRefErrorEntitiesA12DV(IRule rule, IProblemReporter pr) {
		return ruleMetaInformation.getCandidateRelpathsForCustomRefErrorEntitiesA12DV(rule, pr);
	}

	@Override
	public IEntity getEntityInRule(IEC ec, String ruleGroup, String referencedEntity) {
		return ruleMetaInformation.getEntityInRule(ec, ruleGroup, referencedEntity);
	}

	@Override
	public boolean referencesIteration(ICondition condition, IndexArt indexType) {
		return ruleMetaInformation.referencesIteration(condition, indexType);
	}

	@Override
	public boolean[] referencesIteration(IRule rule, ICondition condition) {
		boolean[] iterationPerLevel = new boolean[rule.getErrorEntity().getMax().length];
		for (int i = 0; i < iterationPerLevel.length; i++) {
			iterationPerLevel[i] = AnalyseService.isIteration(condition, i, true);
		}
		return iterationPerLevel;
	}

	/** {@link com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IConditionFormatter} implementations */

	@Override
	public String formatConditionText(IRule rule, IProblemReporter pm) {
		return serviceFactory.getConditionFormatter(serviceConfiguration).formatConditionText(rule, pm);
	}

	@Override
	public String formatConditionText(ICondition condition, IRule rule) {
		return serviceFactory.getConditionFormatter(serviceConfiguration).formatConditionText(condition, rule);
	}

	@Override
	public String formatConditionSnippet(String conditionSnippet, IRule rule, IProblemReporter pm) {
		return serviceFactory.getConditionFormatter(serviceConfiguration).formatConditionSnippet(conditionSnippet, rule, pm);
	}

	@Override
	public String formatOperationSnippet(String operationSnippet, IRule rule, IProblemReporter pm) {
		return serviceFactory.getConditionFormatter(serviceConfiguration).formatOperationSnippet(operationSnippet, rule, pm);
	}

	@Override
	public String formatConditionPart(IParseTreeEntity conditionPart, String ruleFullName) {
		return serviceFactory.getConditionFormatter(serviceConfiguration)
				.formatConditionPart(conditionPart, ruleFullName);
	}

	@Override
	public String formatAndTranslateConditionText(IRule rule, Locale targetLanguage, IProblemReporter pm) {
		return serviceFactory.getConditionFormatter(serviceConfiguration)
				.formatAndTranslateConditionText(rule, targetLanguage, pm);
	}

	@Override
	public String stripComments(IRule rule) {
		return serviceFactory.getConditionFormatter(serviceConfiguration).stripComments(rule);
	}

	@Override
	public String formatAndTranslateConditionText(IRule rule, ICondition condition, Locale targetLanguage) {
		return serviceFactory.getConditionFormatter(serviceConfiguration)
				.formatAndTranslateConditionText(rule, condition, targetLanguage);
	}

	/** {@link com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IFieldErrorTextParser} implementations */

	@Override
	public boolean hasValidErrorText(@NonNull IField field, @NonNull Collection<Locale> locales, @NonNull IProblemReporter pr) {
		return serviceFactory.getFieldErrorTextParser(serviceConfiguration).hasValidErrorText(field, locales, pr);
	}

	@Override public boolean hasValidErrorTextInRequirednessConfig(
			@NonNull IField field, @NonNull Collection<Locale> locales, @NonNull IProblemReporter pr) {
		return serviceFactory.getFieldErrorTextParser(serviceConfiguration)
				.hasValidErrorTextInRequirednessConfig(field, locales, pr);
	}

	/** Other public methods */

	public ParserService createParserService() {
		return new ParserService(this.getServiceConfiguration());
	}

	/**
	 * @param rule
	 * 		rule to get the (directly or indirectly) referenced fields, may not be
	 * 		<code>null</code>
	 * @param pm
	 * 		problem reporter, may not be <code>null</code>
	 * @return all fields referenced (directly or via a group) in the rule condition, or
	 * 		<code>null</code> if the condition text of the rule is invalid<br>
	 * 		<b>Note</b>: referenced root groups are <b>NOT</b> expanded; for example, for
	 * 		<b>GroupNotFilled(/rootgroup)</b> no referenced fields are returned
	 */
	public Set<IEntity> getAllReferencedFields(IRule rule, IProblemReporter pm) {
		ICondition condition = parseBedingungExpand(rule, pm);
		if (condition == null) {
			return null;
		}
		return getAllReferencedFields(condition);
	}

	/**
	 * Like {@link #getAllReferencedFields(IRule, IProblemReporter)}, but we need the parsed rule.
	 *
	 * @param condition
	 * 		parsed rule. May not be {@code null}.
	 */
	public Set<IEntity> getAllReferencedFields(ICondition condition) {
		return ReferencedIdsUtil.getDirectlyReferencedFieldNames(condition, ec).stream()
				.map(ec::get)
				.collect(Collectors.toSet());
	}

	public IEC getEc() {
		return ec;
	}

	public IServiceConfiguration getServiceConfiguration() {
		return serviceConfiguration;
	}

	@Override public ParserStrictness strictness() {
		return serviceConfiguration.strictness();
	}
}
