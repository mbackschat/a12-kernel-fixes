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
package com.mgmtp.a12.kernel.core.tool.a12internal.api.services;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.utils.OnlyForUsage;

@OnlyForUsage
public interface IRuleParser extends IRuleConsistencyCheck {

	/**
	 * Is used for rule conditions. Parses the condition text using
	 * ExpandMode "Expand". Returns null if the condition text is not valid. Errors are reported to the problem reporter.
	 *
	 * @param rule
	 * 		rule to be parsed, may not be null
	 * @param conditionText
	 * 		condition text of a rule, may not be null
	 * @param pr
	 * 		problem reporter, may not be null
	 * @return null if the condition is not valid, else a parse tree
	 */
	ICondition parseConditionExpand(IRule rule, String conditionText, IProblemReporter pr);

	/**
	 * Is used for rule conditions. Parses the condition text using
	 * ExpandMode "NoExpand". Returns null if the condition text is not valid. Errors are reported to the problem reporter.
	 *
	 * @param rule
	 * 		rule to be parsed, may not be null
	 * @param conditionText
	 * 		condition text of a rule, may not be null
	 * @param pr
	 * 		problem reporter, may not be null
	 * @return null if the condition is not valid, else a parse tree
	 */
	ICondition parseConditionNoExpand(IRule rule, String conditionText, IProblemReporter pr);

	/**
	 * Creates a copy of <code>condition</code>, that is expanded. The result is the same as if the
	 * condition was parsed with ExpandMode "EXPAND".
	 *
	 * @param condition
	 * 		the condition to be expanded. This should have been parsed with ExpandMode "NO_EXPAND".
	 * @return the expanded condition or null if the <code>condition</code> is null.
	 */
	ICondition expandCondition(ICondition condition);

	/**
	 * Precondition: The <code>condition</code> has to be expanded, i.e. the condition has to be parsed with
	 * {@link #parseConditionExpand(IRule, String, IProblemReporter)}. Otherwise the referenced fields are not determined correctly.
	 * <p>
	 * Note that, if the condition is null, no referenced fields are recognized, so an error text containing a field reference will
	 * be regarded as invalid.
	 *
	 * @param rule
	 * 		all error texts of the rule are checked, may not be null
	 * @param condition
	 * 		parsed condition of the rule, should not be non-null if fields are referenced in an error text (otherwise a
	 * 		problem is reported to the problem manager)
	 * @param locales
	 * 		specifies the error texts which are checked, the collection and its content may not be null
	 * @param extendProblemMessages
	 * 		specifies whether messages in reported problems should be prepended with a sentence
	 * 		about the broader context of the problem (i.e. the error message of the rule being wrong)
	 * @param pm
	 * 		problem reporter, may not be null
	 * @return true, if the error messages are correct and false otherwise.
	 */
	boolean hasValidErrorText(IRule rule, ICondition condition, Collection<Locale> locales, boolean extendProblemMessages,
			IProblemReporter pm);

	/**
	 * In contrast to {@link #hasValidErrorText(IRule, Collection, boolean, IProblemReporter)}} the given
	 * <code>errorTexts</code> are checked and NOT the error messages contained in the
	 * <code>rule</code> object. Can be used to check application specific error messages.<br>
	 *
	 * @param rule
	 * 		used for determining the referenced fields, may not be null
	 * @param errorTexts
	 * 		error texts to be checked, may not be null
	 * @param locales
	 * 		specifies the error texts which are checked, the collection and its content may not be null
	 * @param extendProblemMessages
	 * 		specifies whether messages in reported problems should be prepended with a sentence
	 * 		about the broader context of the problem (i.e. the error message of the rule being wrong)
	 * @param pm
	 * 		problem reporter, may not be null
	 * @return true, if the error messages are correct and false otherwise.
	 */
	boolean hasValidErrorText(IRule rule, Map<Locale, String> errorTexts, Collection<Locale> locales, boolean extendProblemMessages,
			IProblemReporter pm);

	/**
	 * In contrast to {@link #hasValidErrorText(IRule, ICondition, Collection, boolean, IProblemReporter)}} the given
	 * <code>errorTexts</code> are checked and NOT the error messages contained in the <code>rule</code> object. Can be used to
	 * check application specific error messages.
	 * <br>
	 * <p>
	 * Precondition: The <code>condition</code> has to be expanded, i.e. the condition has to be parsed with
	 * {@link #parseConditionExpand(IRule, String, IProblemReporter)}. Otherwise the referenced fields are not determined
	 * correctly.
	 */
	boolean hasValidErrorText(IRule rule, Map<Locale, String> errorTexts, Collection<Locale> locales, boolean extendProblemMessages,
			IProblemReporter pm, ICondition condition);

	/**
	 * The method is intended to be used mgm-internally to implement the test data generator (Maybe in future also other
	 * modules). Parses the condition text using ExpandMode "Expand". Returns null if the condition text is not valid.
	 * Errors are reported to the problem reporter.
	 *
	 * @param rule
	 * 		rule to be parsed, may not be null
	 * @param pm
	 * 		problem reporter, may not be null
	 * @return null if the condition is not valid, else a parse tree
	 */
	ICondition parseBedingungExpand(IRule rule, IProblemReporter pm);

	/**
	 * The method is intended to be used mgm-internally to implement the test data generator (Maybe in future also other
	 * modules). Parses the condition text using ExpandMode "ExpandPartially". Returns null if the condition text is not valid.
	 * Errors are reported to the problem reporter.
	 *
	 * @param rule
	 * 		rule to be parsed, may not be null
	 * @param pm
	 * 		problem reporter, may not be null
	 * @return null if the condition is not valid, else a parse tree
	 */
	ICondition parseBedingungExpandPartially(IRule rule, IProblemReporter pm);

	/**
	 * The method is intended to be used mgm-internally to implement the test data generator (Maybe in future also other
	 * modules). Parses the condition text using ExpandMode "NoExpand". Returns null if the condition text is not valid.
	 * Errors are reported to the problem reporter.
	 *
	 * @param rule
	 * 		rule to be parsed, may not be null
	 * @param pm
	 * 		problem reporter, may not be null
	 * @return null if the condition is not valid, else a parse tree
	 */
	ICondition parseBedingungNoExpand(IRule rule, IProblemReporter pm);

	/**
	 * Like {@link #parseBedingungNoExpand(IRule, IProblemReporter)}, but for condition texts that don't necessarily correspond
	 * to a full rule condition. Rule-global consistency checks are omitted.
	 */
	ICondition parseBedingungSnippetNoExpand(String operationText, IRule rule, IProblemReporter pm);

	/**
	 * Like {@link #parseBedingungSnippetNoExpand(String, IRule, IProblemReporter)} but for snippets that parse to an IOperation
	 * instead of an ICondition.
	 */
	IOperation parseOperationSnippetNoExpand(String operationSnippet, IRule rule, IProblemReporter pm);
}
