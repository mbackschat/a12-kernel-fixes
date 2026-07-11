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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.parser.internal.ParserService;
import com.mgmtp.a12.kernel.core.parser.internal.RuleMetaInformation;
import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserProblemManager;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorHandler;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.ExpandService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextErrorLookup;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextErrorType;
import com.mgmtp.a12.kernel.core.service.internal.errortext.RuleErrortextChecker;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleParser;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporterProxy;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

/**
 * Implements the interface {@link IRuleParser} for parsing rules.
 */
public class RuleParser implements IRuleParser {

	private final IServiceConfiguration serviceConfiguration;

	public RuleParser(IServiceConfiguration serviceConfiguration) {
		this.serviceConfiguration = serviceConfiguration;
	}

	@Override
	public ICondition parseConditionExpand(@NonNull IRule rule, @NonNull String conditionText, @NonNull IProblemReporter pr) {
		return parseBedingungInternal(rule, conditionText, pr, ExpandMode.EXPAND);
	}

	@Override
	public ICondition parseConditionNoExpand(@NonNull IRule rule, @NonNull String conditionText, @NonNull IProblemReporter pr) {
		return parseBedingungInternal(rule, conditionText, pr, ExpandMode.NO_EXPAND);
	}

	@Override
	public ICondition expandCondition(ICondition condition) {
		return condition != null ?
				ExpandService.expandBedingung(condition, serviceConfiguration.getIEC(), ExpandMode.EXPAND) :
				null;
	}

	@Override
	public boolean hasValidConditionText(@NonNull IRule rule, @NonNull IProblemReporter pr) {
		// we take our own problem manager, to be able to reason about the problems later on.
		MVKParserProblemManager mvkPm = new MVKParserProblemManager();
		parseBedingungInternal(rule, mvkPm, ExpandMode.NO_EXPAND);
		mvkPm.transferProblems(pr);

		return !mvkPm.hasProblemOccured(Severity.ERROR);
	}

	@Override
	public boolean hasValidErrorText(
			@NonNull IRule rule,
			@NonNull Collection<Locale> locales,
			boolean extendProblemMessages,
			@NonNull IProblemReporter pr
	) {
		return hasValidErrorText(rule, rule.getErrorMessages(), locales, extendProblemMessages, pr);
	}

	@Override public boolean hasValidCustomErrorEntities(@NonNull IRule rule, @NonNull IProblemReporter pm) {
		return hasValidCustomErrorEntities(rule, pm, null);
	}

	/**
	 * @param customRule2ConditionNoExpandForCustomRefErrorEntities
	 * 		optional, if null, the rule will be parsed to obtain the
	 * 		condition
	 */
	public <R extends IRule> boolean hasValidCustomErrorEntities(
			@NonNull R rule,
			@NonNull IProblemReporter pm,
			Function<R, ICondition> customRule2ConditionNoExpandForCustomRefErrorEntities
	) {
		if (rule.getCustomRefErrorEntitiesIfFilled().isEmpty() && rule.getCustomRefErrorEntitiesIfNotFilled().isEmpty()) {
			// no need to parse
			return true;
		}

		ProblemReporterProxy prProxy = new ProblemReporterProxy(pm);
		Set<String> occuringEntityRelpaths = new RuleMetaInformation(serviceConfiguration)
				.getCandidateRelpathsForCustomRefErrorEntitiesA12DV(
						rule, prProxy, customRule2ConditionNoExpandForCustomRefErrorEntities);
		if (!prProxy.getProblems().isEmpty()) {
			return false; // if parsing failed, it doesn't make sense to check the customRefErrorEntities in detail
		}

		Locale errMsgLang = this.serviceConfiguration.getModelSpecificSettings().getLanguageUsedInConditionText();
		List<String> invalidRefIfFilled = rule.getCustomRefErrorEntitiesIfFilled().stream()
				.filter(relPath -> !occuringEntityRelpaths.contains(relPath)).toList();
		List<String> invalidRefIfNotFilled = rule.getCustomRefErrorEntitiesIfNotFilled().stream()
				.filter(relPath -> !occuringEntityRelpaths.contains(relPath)).toList();

		if (!invalidRefIfFilled.isEmpty()) {
			pm.reportProblem(ProblemInvalidCustomRefErrorEntityImpl.ofIfFilled(invalidRefIfFilled, errMsgLang, rule));
		}
		if (!invalidRefIfNotFilled.isEmpty()) {
			pm.reportProblem(ProblemInvalidCustomRefErrorEntityImpl.ofIfNotFilled(invalidRefIfNotFilled, errMsgLang, rule));
		}
		return invalidRefIfFilled.isEmpty() && invalidRefIfNotFilled.isEmpty();
	}

	public static class ProblemInvalidCustomRefErrorEntityImpl extends MVKCoreError implements IProblemInvalidCustomRefErrorEntity {
		final List<String> invalidRelPaths;

		private ProblemInvalidCustomRefErrorEntityImpl(List<String> invalidRelPaths, String msg, Object origin4Error) {
			super(msg, origin4Error);
			this.invalidRelPaths = invalidRelPaths;
		}

		public static ProblemInvalidCustomRefErrorEntityImpl ofIfFilled(List<String> invalidRefIfFilled, Locale errMsgLang,
				Object origin4Error) {
			String msg = (new ParserErrorHandler(errMsgLang)).getParserErrorMessage(
					ParserErrorTypesEnum.MVK_REF_ERROR_ENTITY_IF_FILLED_NOT_REFERENCED, String.join(", ", invalidRefIfFilled));
			return new ProblemInvalidCustomRefErrorEntityImpl(invalidRefIfFilled, msg, origin4Error);
		}

		public static ProblemInvalidCustomRefErrorEntityImpl ofIfNotFilled(List<String> invalidRefIfNotFilled, Locale errMsgLang,
				Object origin4Error) {
			String msg = (new ParserErrorHandler(errMsgLang)).getParserErrorMessage(
					ParserErrorTypesEnum.MVK_REF_ERROR_ENTITY_IF_NOT_FILLED_NOT_REFERENCED,
					String.join(", ", invalidRefIfNotFilled));
			return new ProblemInvalidCustomRefErrorEntityImpl(invalidRefIfNotFilled, msg, origin4Error);
		}

		@Override public List<String> invalidlyReferencedEntityRelpaths() {
			return invalidRelPaths;
		}
	}

	@Override
	public boolean hasValidErrorText(
			@NonNull IRule rule,
			ICondition condition,
			@NonNull Collection<Locale> locales,
			boolean extendProblemMessages,
			@NonNull IProblemReporter pr
	) {
		Map<Locale, String> errorTexts = rule.getErrorMessages();
		return hasValidErrorText(rule, errorTexts, locales, extendProblemMessages, pr, condition);
	}

	@Override
	public boolean hasValidErrorText(
			@NonNull IRule rule,
			@NonNull Map<Locale, String> errorTexts,
			@NonNull Collection<Locale> locales,
			boolean extendProblemMessages,
			@NonNull IProblemReporter pr
	) {
		ICondition condition = null;
		boolean conditionIsInvalid = false;

		// also falsely flags escaped dollars ("$$") as parameters
		if (errorTexts.values().stream().anyMatch(s -> s != null && s.contains("$"))) {
			// we require the condition (to determine ref. fields and iteration) only, if error text parameters are used
			condition = parseBedingungInternal(rule, problem -> {
				// do not report problems with the condition
			}, ExpandMode.EXPAND);
			if (condition == null) {
				conditionIsInvalid = true;
				FehlertextErrorLookup errorHandler = new FehlertextErrorLookup(
						serviceConfiguration.getModelSpecificSettings().getLanguageUsedInConditionText());
				pr.reportProblem(new MVKCoreError(errorHandler.getErrorMessage(FehlertextErrorType.INVALID_CONDITION),
						Severity.ERROR,
						rule));
			}
			// even if the condition is invalid, continue with all other checks that don't need it
		}
		return hasValidErrorText(rule, errorTexts, locales, extendProblemMessages, pr, condition) && !conditionIsInvalid;
	}

	@Override
	public boolean hasValidErrorText(
			@NonNull IRule rule,
			@NonNull Map<Locale, String> errorTexts,
			@NonNull Collection<Locale> locales,
			boolean extendProblemMessages,
			@NonNull IProblemReporter pm,
			ICondition condition
	) {
		RuleErrortextChecker ftChecker = new RuleErrortextChecker(rule, condition, serviceConfiguration.getIEC(),
				serviceConfiguration.getModelSpecificSettings(), extendProblemMessages);

		MVKParserProblemManager mvkPm = new MVKParserProblemManager();
		for (Locale loc : locales) {
			ftChecker.checkErrorMessageNotEmptyAndCorrect(errorTexts.get(loc), loc, mvkPm);
		}
		mvkPm.transferProblems(pm);

		return !mvkPm.hasProblemOccured(Severity.ERROR);
	}

	@Override
	public ICondition parseBedingungExpand(@NonNull IRule rule, @NonNull IProblemReporter pm) {
		return parseBedingungInternal(rule, pm, ExpandMode.EXPAND);
	}

	@Override
	public ICondition parseBedingungExpandPartially(@NonNull IRule rule, @NonNull IProblemReporter pm) {
		return parseBedingungInternal(rule, pm, ExpandMode.EXPAND_PARTIALLY);
	}

	@Override
	public ICondition parseBedingungNoExpand(@NonNull IRule rule, @NonNull IProblemReporter pm) {
		return parseBedingungInternal(rule, pm, ExpandMode.NO_EXPAND);
	}

	@Override
	public ICondition parseBedingungSnippetNoExpand(@NonNull String operationText, @NonNull IRule rule,
			@NonNull IProblemReporter pm) {
		ParserService ps = new ParserService(serviceConfiguration);
		return ps.parseConditionSnippet(rule, operationText, pm, ExpandMode.NO_EXPAND);
	}

	@Override
	public IOperation parseOperationSnippetNoExpand(@NonNull String operationText, @NonNull IRule rule,
			@NonNull IProblemReporter pm) {
		ParserService ps = new ParserService(serviceConfiguration);
		return ps.parseOperationSnippet(rule, operationText, pm, ExpandMode.NO_EXPAND);
	}

	private ICondition parseBedingungInternal(@NonNull IRule rule, @NonNull IProblemReporter pm, @NonNull ExpandMode expand) {
		return parseBedingungInternal(rule, rule.getErrorConditionText(), pm, expand);
	}

	private ICondition parseBedingungInternal(IRule rule, String conditionText, IProblemReporter pm, ExpandMode expand) {
		ParserService ps = new ParserService(serviceConfiguration);
		return ps.parseAndCheckCondition(rule, conditionText, pm, expand);
	}
}
