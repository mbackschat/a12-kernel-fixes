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
package com.mgmtp.a12.kernel.core.service.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserError;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorHandler;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.EntityIdImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.EntityIdFormatterUtil;
import com.mgmtp.a12.kernel.core.parser.internal.util.ReferencedIdsUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IChangeRuleSupport;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleParser;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;

/**
 * Implementation of {@link IChangeRuleSupport} providing methods which change a rule, e.g. adapting condition text or error text.
 */
public class ChangeRuleSupport implements IChangeRuleSupport {

	private final IEC ec;
	private final IProjectSpecificSettings pss;
	private final IModelSpecificSettings modelSpecificSettings;
	private final IServiceConfiguration serviceConfiguration;
	private final IRuleParser ruleParser;

	public ChangeRuleSupport(IServiceConfiguration serviceConfiguration, IRuleParser ruleParser) {
		this.ec = serviceConfiguration.getIEC();
		this.pss = serviceConfiguration.getProjectSpecificSettings();
		this.modelSpecificSettings = serviceConfiguration.getModelSpecificSettings();
		this.serviceConfiguration = serviceConfiguration;
		this.ruleParser = ruleParser;
	}

	@Override
	public String changeConditionText(IRule rule, String oldFullName, String newFullName,
			IProblemReporter pm) {
		Objects.requireNonNull(rule, "changeConditionText: Parameter 'rule' may not be null.");
		Objects.requireNonNull(oldFullName,
				"changeConditionText: Parameter 'oldFullName' may not be null.");
		Objects.requireNonNull(newFullName,
				"changeConditionText: Parameter 'newFullName' may not be null.");
		Objects.requireNonNull(pm, "changeConditionText: Parameter 'pm' may not be null.");

		ICondition condition = ruleParser.parseBedingungNoExpand(rule, pm);
		if (condition == null) {
			return null;
		}

		IEntity entity = ec.get(oldFullName);
		if (entity == null) {
			/*
			 * If we have a rule name or an unknown entity, we do nothing (for historical reasons).
			 * For a change in the rule name we would not change anything at the moment. The only
			 * case where a change in the condition text could be done if the full name of the rule
			 * has changed would be in an H model when a group contained in the rule group is
			 * referenced.
			 */
			return rule.getErrorConditionText();
		}

		boolean wasMoved = wasMoved(oldFullName, newFullName);
		boolean wasRenamed = EntityUtils.determineIfWasRenamed(oldFullName, newFullName);

		if (wasMoved) {
			if (!ExtmpUtils.isHModel(modelSpecificSettings)) {
				// after moving in an A12 model the condition text won't be updated
				reportError(rule, pm,
						ParserErrorTypesEnum.MVK_ENTITY_MOVE_IN_A12_MODEL_CONDITION);
				return null;
			} else if (!wasRenamed && entity.isField()) {
				// if a field is moved (and not renamed) in an H model, there is nothing to update in the condition text
				// reason: in H models fields are always referenced by their short names
				return rule.getErrorConditionText();
			}
		}
		StringBuilder result = new StringBuilder(rule.getErrorConditionText());
		ConditionMultiLanguageHandler mlh = new ConditionMultiLanguageHandler(
				modelSpecificSettings.getLanguageUsedInConditionText(), pss);

		if (wasRenamed || wasMoved) {
			String newRuleFullName = getNewRuleFullName(rule.getFullName(), oldFullName,
					newFullName);

			Predicate<IEntityId> toBeUpdated = id -> shouldBeUpdated(oldFullName, (EntityIdImpl) id);
			UnaryOperator<String> fullNameUpdater = fullNameToBeUpdated -> updateFullName(fullNameToBeUpdated, oldFullName,
					newFullName);
			updateReferences(condition, newRuleFullName, result, mlh, toBeUpdated, fullNameUpdater);
		}
		return result.toString();
	}

	@Override
	public Map<Locale, String> changeErrorText(IRule rule, String oldFullName, String newFullName, IProblemReporter pm) {
		Objects.requireNonNull(rule, "changeErrorText: Parameter 'rule' may not be null.");
		Objects.requireNonNull(oldFullName,
				"changeErrorText: Parameter 'oldFullName' may not be null.");
		Objects.requireNonNull(newFullName,
				"changeErrorText: Parameter 'newFullName' may not be null.");
		Objects.requireNonNull(pm, "changeErrorText: Parameter 'pm' may not be null.");

		ICondition condition = null;
		if (rule.getErrorMessages().values().stream().anyMatch(s -> s != null && s.contains("$"))) {
			// we require the condition (to determine ref. fields and iteration) only, if error text parameters are used
			condition = ruleParser.parseBedingungExpand(rule, pm);
			if (condition == null) {
				// we have an erroneous condition text but potentially need the referenced fields
				return null;
			}
		}

		// all non-empty error texts are changed
		List<Locale> locales = rule.getErrorMessages().keySet().stream()
				.filter(locale -> !StringUtils.isEmpty(rule.getErrorMessage(locale)))
				.collect(Collectors.toList());
		if (!ruleParser.hasValidErrorText(rule, rule.getErrorMessages(), locales, true, pm, condition)) {
			return null;
		}

		if (!ExtmpUtils.isHModel(modelSpecificSettings) && wasMoved(oldFullName,
				newFullName)) { // move not allowed in A12 models
			reportError(rule, pm, ParserErrorTypesEnum.MVK_ENTITY_MOVE_IN_A12_MODEL_ERRORTEXT);
			return null;
		}

		Map<Locale, String> errorTexts = new HashMap<>(rule.getErrorMessages());
		UnaryOperator<String> fullNameUpdater = fullNameToBeUpdated -> updateFullName(fullNameToBeUpdated, oldFullName,
				newFullName);
		String oldRuleFullName = rule.getFullName();
		String newRuleFullName = EntityUtils.isDescendantOrEqual(oldRuleFullName, oldFullName) ?
				oldRuleFullName.replaceFirst(oldFullName, newFullName) :
				oldRuleFullName;
		// replace references in error texts
		for (Locale loc : locales) {
			String oldErrorText = errorTexts.get(loc);
			String newErrorText = FehlertextUtil.replaceReferenceInErrorText(oldErrorText, newRuleFullName, fullNameUpdater,
					this.serviceConfiguration, rule, condition, false);
			errorTexts.put(loc, newErrorText);
		}
		return errorTexts;
	}

	@Override
	public String changeConditionTextForDecoration(IRule rule,
			Map<String, String> oldToNewFullNames, String newRuleFullName, IProblemReporter pm) {
		Objects.requireNonNull(rule, "changeConditionText: Parameter 'rule' may not be null.");
		Objects.requireNonNull(oldToNewFullNames,
				"changeConditionText: Parameter 'oldToNewFullNames' may not be null.");
		Objects.requireNonNull(newRuleFullName,
				"changeConditionText: Parameter 'newRuleFullName' may not be null.");
		Objects.requireNonNull(pm, "changeConditionText: Parameter 'pm' may not be null.");

		if (oldToNewFullNames.isEmpty()) {
			throw new IllegalArgumentException(
					"Parameter 'oldToNewFullNames' may not be an empty map.");
		}
		if (StringUtils.isEmpty(newRuleFullName)) {
			throw new IllegalArgumentException(
					"The new full name of the rule to be adjusted may not be null or empty when several entities are renamed at once.");
		}
		if (ExtmpUtils.isHModel(modelSpecificSettings)) {
			throw new IllegalArgumentException(
					"In H models, renaming several entities at once is not allowed.");
		}

		ICondition condition = ruleParser.parseBedingungNoExpand(rule, pm);
		if (condition == null) {
			return null;
		}
		StringBuilder result = new StringBuilder(rule.getErrorConditionText());
		ConditionMultiLanguageHandler mlh = new ConditionMultiLanguageHandler(
				modelSpecificSettings.getLanguageUsedInConditionText(), pss);
		Map<String, String> oldToNewFullNamesFiltered = getRelevantNameChanges(oldToNewFullNames);
		if (!oldToNewFullNamesFiltered.isEmpty()) {
			Predicate<IEntityId> toBeUpdated = id -> oldToNewFullNamesFiltered.containsKey(id.getFullName());
			UnaryOperator<String> fullNameUpdater = fullNameToBeUpdated -> updateFullName(fullNameToBeUpdated, oldToNewFullNames);
			updateReferences(condition, newRuleFullName, result, mlh, toBeUpdated, fullNameUpdater);
		}
		return result.toString();
	}

	@Override
	public Map<Locale, String> changeErrorTextForDecoration(IRule rule, Map<String, String> oldToNewFullNames,
			String newRuleFullName, IProblemReporter pm) {
		Objects.requireNonNull(rule, "changeErrorText: Parameter 'rule' may not be null.");
		Objects.requireNonNull(oldToNewFullNames,
				"changeErrorText: Parameter 'oldToNewFullNames' may not be null.");
		Objects.requireNonNull(newRuleFullName,
				"changeErrorText: Parameter 'newRuleFullName' may not be null.");
		Objects.requireNonNull(pm, "changeErrorText: Parameter 'pm' may not be null.");

		if (oldToNewFullNames.isEmpty()) {
			throw new IllegalArgumentException(
					"Parameter 'oldToNewFullNames' may not be an empty map.");
		}
		if (StringUtils.isEmpty(newRuleFullName)) {
			throw new IllegalArgumentException(
					"The new full name of the rule to be adjusted may not be null or empty when several entities are renamed at once.");
		}
		if (ExtmpUtils.isHModel(modelSpecificSettings)) {
			throw new IllegalArgumentException(
					"In H models, renaming several entities at once is not allowed.");
		}

		ICondition condition = null;
		if (rule.getErrorMessages().values().stream().anyMatch(s -> s != null && s.contains("$"))) {
			// we require the condition (to determine ref. fields and iteration) only, if error text parameters are used
			condition = ruleParser.parseBedingungExpand(rule, pm);
			if (condition == null) {
				// we have an erroneous condition text but potentially need the referenced fields
				return null;
			}
		}

		// all non-empty error texts are changed
		List<Locale> locales = rule.getErrorMessages().keySet().stream()
				.filter(locale -> !StringUtils.isEmpty(rule.getErrorMessage(locale)))
				.collect(Collectors.toList());
		if (!ruleParser.hasValidErrorText(rule, rule.getErrorMessages(), locales, true, pm, condition)) {
			return null;
		}

		Map<Locale, String> errorTexts = new HashMap<>(rule.getErrorMessages());
		Map<String, String> oldToNewFullNamesFiltered = getRelevantNameChanges(oldToNewFullNames);
		UnaryOperator<String> fullNameUpdater = fullNameToBeUpdated -> updateFullName(fullNameToBeUpdated,
				oldToNewFullNamesFiltered);
		// replace references in error texts
		for (Locale loc : locales) {
			String oldErrorText = errorTexts.get(loc);
			String newErrorText = FehlertextUtil.replaceReferenceInErrorText(oldErrorText, newRuleFullName, fullNameUpdater,
					this.serviceConfiguration, rule, condition, true);
			errorTexts.put(loc, newErrorText);
		}
		return errorTexts;
	}

	private boolean wasMoved(String oldFullName, String newFullName) {
		return !EntityUtils.getParentPath(oldFullName).equals(EntityUtils.getParentPath(newFullName));
	}

	private void reportError(IRule rule, IProblemReporter pm, ParserErrorTypesEnum errKey,
			Object... args) {
		String errMsg = new ParserErrorHandler(modelSpecificSettings.getLanguageUsedInConditionText()).getParserErrorMessage(errKey,
				args);
		MVKParserError err = new MVKParserError(rule, errMsg);
		pm.reportProblem(err);
	}

	/**
	 * @return the new full name of the rule, if a group in the rule group was renamed or moved. Otherwise, the original rule full
	 * 		name is returned.
	 */
	private String getNewRuleFullName(String oldRuleFullName, String oldFullName,
			String newFullName) {
		// We change the rule full name only, if the first (and in that case then only) entity
		// is an ancestor group of the rule group.
		return EntityUtils.isDescendantOrEqual(oldRuleFullName, oldFullName) ?
				oldRuleFullName.replaceFirst(oldFullName, newFullName) :
				oldRuleFullName;
	}

	/**
	 * @return <code>true</code> if the given id should be updated, otherwise <code>false</code>.
	 */
	private boolean shouldBeUpdated(String oldFullName, EntityIdImpl id) {
		if (!EntityUtils.isDescendantOrEqual(id.getFullName(),
				oldFullName)) {
			return false;
		}
		if (id.getStartIdxInCondition() == -1) {
			// entities which are not in the condition text are not relevant
			return false;
		}
		// if a group was modified in an H model, the corresponding referenced fields don't have to be updated
		return !(ExtmpUtils.isHModel(modelSpecificSettings) && ec.get(oldFullName).isGroup() && id.isField());
	}

	private static String updateFullName(String fullNameToBeUpdated, String oldFullName, String newFullName) {
		if (EntityUtils.isDescendantOrEqual(fullNameToBeUpdated, oldFullName)) {
			return fullNameToBeUpdated.replaceFirst(oldFullName, newFullName);
		}
		return fullNameToBeUpdated;
	}

	private static String updateFullName(String fullNameToBeUpdated, Map<String, String> oldToNewFullNames) {
		if (oldToNewFullNames.containsKey(fullNameToBeUpdated)) {
			return oldToNewFullNames.get(fullNameToBeUpdated);
		}
		return fullNameToBeUpdated;
	}

	/**
	 * @param condition
	 * 		condition to be updated
	 * @param newRuleFullName
	 * 		new full name of the rule whose condition is to be updated
	 * @param result
	 * 		condition text which is modified by the method
	 * @param mlh
	 * 		the {@link ConditionMultiLanguageHandler}
	 * @param toBeUpdated
	 * 		for determining the entities references to be updated
	 * @param fullNameUpdater
	 * 		the {@link UnaryOperator} to update the full name
	 */
	private void updateReferences(ICondition condition, String newRuleFullName, StringBuilder result,
			ConditionMultiLanguageHandler mlh, Predicate<IEntityId> toBeUpdated, UnaryOperator<String> fullNameUpdater) {
		// replace references in condition text
		ReferencedIdsUtil.getDirectlyReferencedIEntityIdsFilteredWithoutRootGroups(Collections.singletonList(condition),
						ReferencedIdsUtil.ReferencedIdsMode.USE_FILTERCONDITION_UNMODIFIED, this.ec, toBeUpdated, false)
				.stream()
				.map(EntityIdImpl.class::cast)
				.sorted(// we sort the entities from the end backwards
						// in this way the stored indices at the entities are always valid
						(o1, o2) -> Integer.compare(o2.getStartIdxInCondition(),
								o1.getStartIdxInCondition()))
				.forEach(id -> {
					String oldFullName = id.getFullName();
					String newFullName = fullNameUpdater.apply(oldFullName);
					id.setFullName(newFullName);
					String newNameInCondition = getNewNameInCondition(id, oldFullName, newFullName, newRuleFullName, mlh);
					result.replace(id.getStartIdxInCondition(), id.getStopIdxInCondition(),
							newNameInCondition);
				});
	}

	private Map<String, String> getRelevantNameChanges(Map<String, String> oldToNewFullNames) {
		HashMap<String, String> relevantOldToNewFullNames = new HashMap<>();
		for (Map.Entry<String, String> entry : oldToNewFullNames.entrySet()) {
			String oldFullName = entry.getKey();
			String newFullName = entry.getValue();
			IEntity entity = ec.get(oldFullName);
			if (entity == null) {
				throw new IllegalArgumentException(
						String.format("Entity '%s' is not contained in the provided EntityContainer.", oldFullName));
			}
			if (newFullName == null) {
				throw new IllegalArgumentException(
						String.format("For entity '%s' an invalid new full name was specified (null).", oldFullName));
			}
			if (!oldFullName.equals(newFullName)) {
				relevantOldToNewFullNames.put(oldFullName, newFullName);
			}
		}
		return relevantOldToNewFullNames;
	}

	private String getNewNameInCondition(EntityIdImpl id, String oldPath, String newPath, String newRulePath,
			ConditionMultiLanguageHandler mlh) {
		String newRuleGroupPath = EntityUtils.getParentPath(newRulePath);
		return EntityIdFormatterUtil.formatEntityIdForConditionText(id,
				EntityUtils.getParentPath(newRulePath),
				unUsedShortName -> EntityUtils.shouldShortNameBeUsed(newRuleGroupPath, oldPath, newPath, this.serviceConfiguration),
				mlh);
	}
}
