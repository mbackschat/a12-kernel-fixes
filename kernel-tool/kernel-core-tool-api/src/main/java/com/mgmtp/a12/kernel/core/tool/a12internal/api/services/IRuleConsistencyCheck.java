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
import java.util.List;
import java.util.Locale;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Services to determine if the properties of a rule are consistent, e.g. condition text or error text.
 */
@OnlyForUsage
public interface IRuleConsistencyCheck {

	/**
	 * Checks, if the condition text of the rule is valid. To check if a condition text is valid,
	 * the
	 * rule must provide at least the condition text, the fullname and the error field:
	 * <ul>
	 * <li>the <i>condition text</i> is parsed and checks are performed, if it is correct
	 * <li>the <i>error field</i> is required to check, if the iteration specified in the condition
	 * text is correct.
	 * <li>the <i>full name of the rule</i> is required, because fields in the condition text may be
	 * references relative to the path of the rule.
	 * </ul>
	 * The language of the condition text is specified by the project specific settings. Problems
	 * are reported in the language specified in the project specific settings. Problems are
	 * reported to the specified problem reporter.
	 *
	 * <br>
	 * <b>Note</b>
	 * <br>
	 * To check, if the error text of the rule is correct, use
	 * {@link IRuleConsistencyCheck#hasValidErrorText(IRule, Collection, boolean, IProblemReporter)}
	 *
	 * @param rule
	 * 		specifies the rule: at minimum condition text, fullname and error field must be
	 * 		provided; may not be null
	 * @param pr
	 * 		problem reporter, may not be null
	 * @return true, if the condition text of the rule is valid; false otherwise.
	 */
	boolean hasValidConditionText(@NonNull IRule rule, @NonNull IProblemReporter pr);

	/**
	 * Checks, if the error messages specified by the given {@link Locale}s are correct. An error
	 * message is
	 * correct, if
	 * <ul>
	 * <li>it is not null and not empty
	 * <li>all parameters which reference fields are referenced in the condition as well
	 * <li>iteration parameters are consistent to the iteration modeled by the condition text
	 * <li>additional checks may take place according to the project specific settings.
	 * </ul>
	 * <br>
	 * <b>Precondition</b>
	 * <br>
	 * Rule condition text must be valid if the error text contains parameters, otherwise this
	 * method will return {@code false}.
	 * Detailed problems with the rule condition text are not reported to the problem reporter
	 * (besides the rule condition text not being valid).
	 * Please check {@link IRule} for further information regarding the error
	 * text.
	 *
	 * @param rule
	 * 		specifies the rule: at minimum condition text, fullname, error field and error
	 * 		texts (for the specified {@link Locale}s) must be provided; may not be null
	 * @param locales
	 * 		specifies the error texts, which are checked. The collection and its content may
	 * 		not be null
	 * @param extendProblemMessages
	 * 		specifies whether messages in reported problems should be prepended with a sentence
	 * 		about the broader context of the problem (i.e. the error message of the rule being wrong)
	 * @param pr
	 * 		problem reporter, may not be null
	 * @return true, if the error messages are correct and false otherwise.
	 */
	boolean hasValidErrorText(@NonNull IRule rule, @NonNull Collection<Locale> locales, boolean extendProblemMessages,
			@NonNull IProblemReporter pr);

	/**
	 * Checks if the given IRule's {@link IRule#getCustomRefErrorEntitiesIfFilled()} and
	 * {@link IRule#getCustomRefErrorEntitiesIfNotFilled()} are valid, i.e. either empty or contain only relPaths of entities
	 * that are referenced in the rule's {@link IRule#getErrorConditionText()}
	 *
	 * @param rule
	 * @param pm
	 * 		will receive one {@link IProblemInvalidCustomRefErrorEntity} for every invalid reference.
	 * 		If the rule is otherwise invalid in a way that prevents parsing, {@link IProblem}s regarding the failed
	 * 		parsing <em>may</em> be reported instead.
	 * @return true if valid, false otherwise
	 */
	boolean hasValidCustomErrorEntities(@NonNull IRule rule, @NonNull IProblemReporter pm);

	interface IProblemInvalidCustomRefErrorEntity extends IProblem {
		/**
		 * @return the relPaths (relative to the corresponding rule's group) of the entity that occur in
		 *        {@link IRule#getCustomRefErrorEntitiesIfFilled()} or {@link IRule#getCustomRefErrorEntitiesIfNotFilled()} but is not
		 * 		referenced in the rule's condition
		 */
		List<String> invalidlyReferencedEntityRelpaths();
	}
}
