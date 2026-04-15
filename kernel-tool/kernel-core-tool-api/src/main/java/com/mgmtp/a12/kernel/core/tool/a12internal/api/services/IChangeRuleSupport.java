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
import java.util.Map;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Services which change the rule, e.g. adapting condition text or error text.
 * @deprecated use {@code KernelExtToolFacade#changeConditionTextForMoves} and {@code KernelExtToolFacade#changeErrorTextsForMove} instead
 *
 */
@OnlyForUsage
@Deprecated(since = "30.6.0")
public interface IChangeRuleSupport {

	/**
	 * The method returns the modified condition text of the rule. All references of the old entity are replaced in the condition
	 * text by references to the new entity. The new entity is referenced by
	 * <ul>
	 * <li>its <i>relative</i> path if the new entity is contained in the group of the rule (except
	 * field names in the ExtMP old world)
	 * <li>its <i>absolute</i> path if the new entity is not contained in the group of the rule
	 * (except field names in the ExtMP old world)
	 * <li>in the ExtMP old world: field names are always referenced without path
	 * </ul>
	 * <p>
	 * It is <b>not</b> checked, if the new condition text is valid. The modification is restricted
	 * to a pure textual replacement.
	 * <p>
	 * If the old entity is not referenced in the condition text, the original condition text is
	 * returned. The method can be used to rename an entity or to move (only ExtMP old world) an
	 * entity to another group. Note that this entity could also be the error field of the rule.
	 * There is no special treatment if the changed entity is the error field.
	 * <p>
	 * In the new world, where groups can be repeatable, the method cannot be used to move an
	 * entity, because it is not possible to decide if repeatable groups should be referenced with
	 * iteration or wildcard. In this case the entity is not replaced in the condition text and the
	 * problem is reported to the problem reporter.
	 * <p>
	 * <b>Precondition</b>
	 * <p>
	 * Rule condition text must be valid. The method returns null, if the given rule condition text
	 * is not valid. Problems are reported to the problem reporter.
	 * Additionally, the full name of the rule and the content of the {@link IEC} used by this
	 * service must match. I.e. the old full name should be used with the {@link IEC} before
	 * movement of the group.
	 *
	 * @param rule
	 * 		specifies the rule: at minimum condition text, fullname and error field must be provided; may not be null
	 * @param oldFullName
	 * 		full name of the entity to be replaced; may not be null
	 * @param newFullName
	 * 		full name of the new entity which will replace the old one; may not be null
	 * @param pr
	 * 		problem reporter; may not be null
	 * @return the changed condition text or null, if the original condition text is not valid or a new world entity is moved
	 */
	public String changeConditionText(IRule rule, String oldFullName, String newFullName, IProblemReporter pr);

	/**
	 * The method returns the modified error texts of the rule. All references of the old entity are replaced in the error texts by
	 * references to the new entity. The new entity is referenced by
	 * <ul>
	 * <li>its <i>relative</i> path, if the new entity is contained in the group of the rule (except
	 * field names in the ExtMP old world)
	 * <li>its <i>absolute</i> path, if the new entity is not contained in the group of the rule
	 * (except field names in the ExtMP old world)
	 * <li>in the ExtMP old world: field names are always referenced without path
	 * </ul>
	 * <br>
	 * It is <b>not</b> checked, if the <i>new</i> error texts are valid. The modification is
	 * restricted to a pure textual replacement.
	 * <br>
	 * If the old entity is not referenced in an error text, the original error text is returned.
	 * The method can be used to rename an entity or to move (only ExtMP old world) an entity to
	 * another group. Note that this entity could also be the error field of the rule. There is no
	 * special treatment, if the changed entity is the error field.
	 * <br>
	 * In the new world, where groups can be repeatable, the method cannot be used to move an
	 * entity. Because it is not possible to update the condition text in this case (see
	 * {@link #changeConditionText(IRule, String, String, IProblemReporter)}), the error text is not
	 * updated as well and a problem is reported to the problem reporter.
	 * <br>
	 * <b>Precondition</b>
	 * <br>
	 * Rule condition text and all error texts must be valid (empty error texts are accepted here).
	 * The method returns null, if the given rule condition text or an error text is not valid.
	 * Problems are reported to the problem reporter (the rule condition text is only checked if an
	 * error text contains an error text parameter).
	 *
	 * @param rule
	 * 		specifies the rule: at minimum condition text, fullname, error field and error text (for the specified
	 *        {@link Locale}) must be provided; may not be null
	 * @param oldFullName
	 * 		full name of the entity to be replaced; may not be null
	 * @param newFullName
	 * 		full name of new entity which will replace the old one; may not be null
	 * @param pr
	 * 		problem reporter; may not be null
	 * @return the changed error texts or null, if an original error text or the condition text is not valid or a new world entity
	 * 		ty is moved. The returned map contains an entry for each error text of the rule, it may be empty if no error texts are
	 * 		defined for the rule.
	 */
	public Map<Locale, String> changeErrorText(IRule rule, String oldFullName, String newFullName, IProblemReporter pr);

	/**
	 * Updates the condition text for decorations. For decorations, multiple entities are renamed "at the same time" (i.e., within
	 * one execution of this method) in the decoration model, but not moved.
	 * <p>
	 * The old full name of the rule and all entities contained in {@code oldToNewFullNames} and the content of the {@link IEC} used
	 * by this service must match.
	 *
	 * @param rule
	 * 		may not be null
	 * @param oldToNewFullNames
	 * 		maps the old full name of each entity whose path and/or name was changed to its new full name; the
	 * 		map and its content may not be null
	 * @param newRuleFullName
	 * 		may not be null
	 * @param pm
	 * 		may not be null
	 * @return the new condition text
	 */
	String changeConditionTextForDecoration(IRule rule,
			Map<String, String> oldToNewFullNames, String newRuleFullName, IProblemReporter pm);

	/**
	 * Updates the error text for decorations.
	 * <p>
	 * The old full name of the rule and all entities contained in {@code oldToNewFullNames} and the content of the {@link IEC} used
	 * by this service must match.
	 *
	 * @param rule
	 * 		may not be null
	 * @param oldToNewFullNames
	 * 		maps the old full name of each entity whose path and/or name was changed to its new full name; the
	 * 		map and its content may not be null
	 * @param newRuleFullName
	 * 		may not be {@code null}
	 * @param pm
	 * 		may not be null
	 * @return the new error texts or null if errors occurred
	 */
	Map<Locale, String> changeErrorTextForDecoration(IRule rule, Map<String, String> oldToNewFullNames,
			String newRuleFullName, IProblemReporter pm);
}
