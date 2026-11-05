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
package com.mgmtp.a12.kernel.core.parser.internal;

import java.util.Locale;
import java.util.Objects;

import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserProblemManager;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.formatter.FormatterVisitor;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IConditionFormatter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleParser;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.model.notification.Severity;

/**
 * Implementation of {@link IConditionFormatter}.
 */
public class ConditionFormatter implements IConditionFormatter {

	private final IServiceConfiguration serviceConfiguration;
	private final IProjectSpecificSettings pss;
	private final IRuleParser ruleParser;

	public ConditionFormatter(IServiceConfiguration serviceConfiguration, IRuleParser ruleParser) {
		this.serviceConfiguration = serviceConfiguration;
		this.pss = serviceConfiguration.getProjectSpecificSettings();
		this.ruleParser = ruleParser;
	}

	@Override
	public String formatConditionText(IRule rule, IProblemReporter pm) {
		Objects.requireNonNull(rule, "formatConditionText: Parameter 'rule' may not be null.");
		Objects.requireNonNull(pm, "formatConditionText: Parameter 'pm' may not be null.");
		IParseTreeEntity condition = parseConditionInternal(rule, pm);
		return formatInternal(condition, rule);
	}

	@Override
	public String formatConditionText(ICondition condition, IRule rule) {
		Objects.requireNonNull(condition,
				"formatConditionText: Parameter 'condition' may not be null.");
		Objects.requireNonNull(rule, "formatConditionText: Parameter 'rule' may not be null.");
		return formatInternal(condition, rule);
	}

	@Override
	public String formatConditionSnippet(String conditionSnippet, IRule rule, IProblemReporter pm) {
		IParseTreeEntity condition = parseConditionSnippetInternal(conditionSnippet, rule, pm);
		return formatInternal(condition, rule);
	}

	@Override
	public String formatOperationSnippet(String operationSnippet, IRule rule, IProblemReporter pm) {
		IParseTreeEntity operation = parseOperationSnippetInternal(operationSnippet, rule, pm);
		return formatInternal(operation, rule);
	}

	@Override
	public String formatConditionPart(IParseTreeEntity conditionPart, String ruleFullName) {
		Objects.requireNonNull(conditionPart,
				"formatConditionPart: Parameter 'conditionPart' may not be null.");
		Objects.requireNonNull(ruleFullName,
				"formatConditionPart: Parameter 'ruleFullname' may not be null.");
		return formatInternal(conditionPart, ruleFullName, getSameLangMlh()).trim();
		// formatConditionPart calls .trim to preserve the pre-refactoring behavior. The other methods (including *Snippet methods)
		// don't do that because this gives the caller the choice to either trim or not trim, and trailing line-breaks seem like a
		// good idea in case the text ends with a comment.
	}

	@Override
	public String formatAndTranslateConditionText(IRule rule, Locale targetLanguage, IProblemReporter pm) {
		Objects.requireNonNull(rule, "formatAndTranslateConditionText: Parameter 'rule' may not be null.");
		Objects.requireNonNull(pm, "formatAndTranslateConditionText: Parameter 'pm' may not be null.");
		assert targetLanguage != null : "targetLanguage should probably not be null";
		IParseTreeEntity condition = parseConditionInternal(rule, pm);
		ConditionMultiLanguageHandler formatterMlh = new ConditionMultiLanguageHandler(targetLanguage);
		return formatInternal(condition, rule, formatterMlh);
	}

	@Override
	public String stripComments(IRule rule) {
		return ConditionCommentStripper.stripComments(rule.getErrorConditionText(),
				serviceConfiguration.getModelSpecificSettings().getLanguageUsedInConditionText(), pss);
	}

	@Override
	public String formatAndTranslateConditionText(IRule rule, ICondition condition, Locale targetLanguage) {
		Objects.requireNonNull(condition,
				"formatAndTranslateConditionText: Parameter 'condition' may not be null.");
		Objects.requireNonNull(rule, "formatAndTranslateConditionText: Parameter 'rule' may not be null.");
		assert targetLanguage != null : "targetLanguage should probably not be null";
		ConditionMultiLanguageHandler mlh = new ConditionMultiLanguageHandler(targetLanguage);
		return formatInternal(condition, rule, mlh);
	}

	private IParseTreeEntity parseConditionInternal(IRule rule, IProblemReporter pm) {
		MVKParserProblemManager mvkPm = new MVKParserProblemManager();
		ICondition b = ruleParser.parseBedingungNoExpand(rule, mvkPm);
		mvkPm.transferProblems(pm);

		if (mvkPm.hasProblemOccured(Severity.ERROR) || b == null) {
			return null;
		}
		return b;
	}

	private IParseTreeEntity parseConditionSnippetInternal(String conditionSnippet, IRule rule, IProblemReporter pm) {
		MVKParserProblemManager mvkPm = new MVKParserProblemManager();
		ICondition b = ruleParser.parseBedingungSnippetNoExpand(conditionSnippet, rule, mvkPm);
		mvkPm.transferProblems(pm);

		if (mvkPm.hasProblemOccured(Severity.ERROR) || b == null) {
			return null;
		}
		return b;
	}

	private IParseTreeEntity parseOperationSnippetInternal(String operationSnippet, IRule rule, IProblemReporter pm) {
		MVKParserProblemManager mvkPm = new MVKParserProblemManager();
		IOperation b = ruleParser.parseOperationSnippetNoExpand(operationSnippet, rule, mvkPm);
		mvkPm.transferProblems(pm);

		if (mvkPm.hasProblemOccured(Severity.ERROR) || b == null) {
			return null;
		}
		return b;
	}

	private String formatInternal(IParseTreeEntity conditionOrOperation, IRule rule) {
		return formatInternal(conditionOrOperation, rule.getFullName(), getSameLangMlh());
	}

	private String formatInternal(IParseTreeEntity conditionOrOperation, IRule rule, ConditionMultiLanguageHandler targetLangMlh) {
		return formatInternal(conditionOrOperation, rule.getFullName(), targetLangMlh);
	}

	private String formatInternal(IParseTreeEntity conditionOrOperation, String ruleFullName,
			ConditionMultiLanguageHandler targetLangMlh) {
		if (conditionOrOperation == null) {
			return null;
		}
		return FormatterVisitor.getFormattedOutput(conditionOrOperation, serviceConfiguration, ruleFullName, targetLangMlh);
	}

	private ConditionMultiLanguageHandler getSameLangMlh() {
		return new ConditionMultiLanguageHandler(
				serviceConfiguration.getModelSpecificSettings().getLanguageUsedInConditionText(), pss);
	}
}
