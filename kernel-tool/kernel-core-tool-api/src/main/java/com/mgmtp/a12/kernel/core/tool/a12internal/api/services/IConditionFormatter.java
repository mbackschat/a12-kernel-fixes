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

import java.util.Locale;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.utils.OnlyForUsage;

@OnlyForUsage
public interface IConditionFormatter {

	/**
	 * Returns the formatted condition text, e.g. for a nice print in a documentation. No semantic
	 * change, only white_space, line breaks.
	 * <br>
	 * <b>Precondition</b>
	 * <br>
	 * Rule condition text must be valid. The method returns null, if the given rule condition text
	 * is not valid. Problems are reported to the problem reporter.
	 * <br>
	 * The following is normalized:
	 * <ul>
	 * <li>Unnecessary brackets (e.g. double brackets or brackets around a simple operation or
	 * condition) are removed. For example <code>(FieldNotFilled(name))</code> returns
	 * <code>FieldNotFilled(name)</code>
	 * </ul>
	 *
	 * @param rule
	 * 		specifies the rule: at minimum condition text, full name and error field must be
	 * 		provided; may not be null
	 * @param pr
	 * 		problem reporter; may not be null
	 * @return the formated condition text or null if the condition text is not valid.
	 */
	String formatConditionText(IRule rule, IProblemReporter pr);

	/**
	 * Format a condition that is not necessarily the whole validation rule condition,
	 * for example, the precondition of a computation. Otherwise similar to {@link #formatConditionText(IRule, IProblemReporter)}
	 */
	String formatConditionSnippet(String conditionSnippet, IRule rule, IProblemReporter pm);

	/**
	 * Like {@link #formatConditionSnippet(String, IRule, IProblemReporter)} but for snippets that correspond to an operation
	 * instead of a condition. Example: operations of computations.
	 */
	String formatOperationSnippet(String operationSnippet, IRule rule, IProblemReporter pm);

	/**
	 * Precondition: The <code>condition</code> and the <code>rule</code> may not be null. If the
	 * condition text is to be formatted only (without expanding), the condition text has to be
	 * parsed with {@link IRuleParser#parseConditionNoExpand(IRule, String, IProblemReporter)}.
	 *
	 * @param condition
	 * 		may not be null
	 * @param rule
	 * 		may not be null
	 * @return the formatted condition text or null if the condition text is not valid.
	 */
	String formatConditionText(ICondition condition, IRule rule);

	/**
	 * Returns the formatted representation of the given part of the condition text, e.g. for a nice
	 * print in a documentation. No semantic change, only white_space, line breaks.
	 * <br>
	 * <b>Precondition</b>
	 * <br>
	 * The following is normalized:
	 * <ul>
	 * <li>Unnecessary brackets (e.g. double brackets or brackets around a simple operation or
	 * condition) are removed. For example <code>(FieldNotFilled(name))</code> returns
	 * <code>FieldNotFilled(name)</code>
	 * </ul>
	 *
	 * @param conditionPart
	 * 		the condition part to be formatted.
	 * @param ruleFullName
	 * 		the full name (including the path) of the rule, the condition part was parsed
	 * 		from. It is necessary for formatting field names and <code>RuleGroup</code>
	 * 		correctly.
	 * @return a string representing the given part of an error condition.
	 *
	 * @throws MVKParserError
	 * 		in case of blacklisted language constructs.
	 */
	default String formatConditionPart(IParseTreeEntity conditionPart, String ruleFullName) {
		throw new UnsupportedOperationException(
				"Please contact the Kernel team and inform them, that you have your own implementation of the MVKService! "
						+ "This usually should not be necessary besides mocked out services for tests.");
	}

	/**
	 * Returns the condition text formatted and in the specified language, e.g. for a nice print in a
	 * documentation. No semantic change, only white_space, line breaks.
	 * <br>
	 * <b>Precondition</b>
	 * <br>
	 * Rule condition text must be valid. The method returns null, if the given rule condition text
	 * is not valid. Problems are reported to the problem reporter.
	 * <br>
	 * The following is normalized:
	 * <ul>
	 * <li>Unnecessary brackets (e.g. double brackets or brackets around a simple operation or
	 * condition) are removed. For example <code>(FieldNotFilled(name))</code> returns
	 * <code>FieldNotFilled(name)</code>
	 * </ul>
	 *
	 * @param rule
	 * 		specifies the rule: at minimum condition text, fullname and error field must be
	 * 		provided; may not be null
	 * @param targetLanguage
	 * 		Specifies the language of the returned formatted condition text. If null, the
	 * 		condition text is only formatted, but not translated.
	 * @param pr
	 * 		problem reporter; may not be null
	 * @return the formated condition text or null if the condition text is not valid.
	 */
	String formatAndTranslateConditionText(IRule rule, Locale targetLanguage, IProblemReporter pr);

	/**
	 * Returns a condition text that is stripped of comments.
	 * As a side effect, leading and trailing whitespace is eliminated from the condition text even if there are no comments in
	 * the condition text.
	 * <p/>
	 * <em>Note</em> If the condition text of the rule is invalid in a way that prevents successful processing by the lexer, an
	 * exception may be thrown.
	 * In that case, rule.getErrorConditionText() can be a suitable fallback if you want to use the
	 * unchanged condition text.
	 *
	 * @param rule
	 * 		the rule, the condition text of which should be stripped of comments.
	 * @return the stripped condition text.
	 */
	default String stripComments(IRule rule) {
		throw new UnsupportedOperationException(
				"Please contact the Kernel team and inform them, that you have your own implementation of the MVKService! "
						+ "This usually should not be necessary besides mocked out services for tests.");
	}

	/**
	 * Precondition: The <code>condition</code> must not be expanded, i.e. the condition text has to be parsed with
	 * {@link com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ExpandMode#NO_EXPAND}.
	 * <p>
	 * For more information see {@link #formatAndTranslateConditionText(IRule, Locale, IProblemReporter)}.
	 *
	 * @param condition
	 * 		the parsed condition text. Must not be {@code null}.
	 */
	String formatAndTranslateConditionText(IRule rule, ICondition condition, Locale targetLanguage);
}
