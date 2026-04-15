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

import java.util.Set;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.IndexArt;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.utils.OnlyForUsage;

@OnlyForUsage
public interface IRuleMetaInformation {

	/**
	 * Returns all fields or groups referenced directly by the condition text. Returns an empty set, if the condition text
	 * references neither fields nor groups. Note: the error field is not automatically included in the result set. Also the
	 * indirectly used index fields are not automatically included for the use of semantic index or parallel iteration.
	 * <br>
	 * <b>Precondition</b>
	 * <br>
	 * Rule condition text must be valid. The method returns null, if the given rule condition text is not valid. Problems are
	 * reported to the problem reporter.
	 *
	 * @param rule
	 * 		specifies the rule: at minimum condition text, fullname and error field must be provided; may not be null
	 * @param pr
	 * 		problem reporter; may not be null
	 * @return a set containing all fields and groups (including root groups), referenced directly by the condition text. Returns
	 * 		null if the condition text of the rule is not valid.
	 */
	Set<IEntity> getDirectlyReferencedEntities(IRule rule, IProblemReporter pr);

	/**
	 * Returns the directly referenced entities (fields and groups, but no root groups) of the condition. Note that these correspond
	 * to the
	 * <ul>
	 * <li>the directly referenced entities of the original condition text, if the condition was
	 * parsed via {@link IRuleParser#parseConditionNoExpand(IRule, String, IProblemReporter)}</li>
	 * <li>all referenced entities of the original condition text, if the condition was parsed via
	 * {@link IRuleParser#parseConditionExpand(IRule, String, IProblemReporter)}</li>
	 * </ul>
	 * Note that in contrast to {@link #getDirectlyReferencedEntities(IRule, IProblemReporter)} only
	 * fields and groups, but no root groups are returned.
	 *
	 * @return directly referenced entities of the condition; the set is empty if the condition is null
	 */
	Set<IEntity> getDirectlyReferencedEntities(ICondition condition);

	/**
	 * Returns the relative paths (relative to the rule's parent group) of the referenced entities of the rule's condition,
	 * including the indirectly referenced index-fields and semantic-index field references for entities that are referenced with
	 * semantic index or parallel iteration.
	 * <p>
	 * References to Groups will not be expanded (vgl ExpandMode.NO_EXPAND)
	 * <p>
	 * Paths will be in the implicit-turning-point notation, i.e. ../../G/f1 instead of ../..RG/G/f1.
	 * Paths will be relative even if they refer to a different root group (which would not be allowed within the rule condition).
	 * The path to the RuleGroup is represented as "./" (not "").
	 * <p>
	 * <em>Note:</em> in contrast to {@link #getDirectlyReferencedEntities(IRule, IProblemReporter)}, and
	 * {@link #getDirectlyReferencedEntities(ICondition)}, referenced root groups will
	 * be included, just like any other referenced groups.
	 */
	Set<String> getCandidateRelpathsForCustomRefErrorEntitiesA12DV(IRule rule, IProblemReporter pr);

	/**
	 * <b>Note:</b> this method works only for A12 models. It considers following cases:
	 * <ul>
	 * <li>absolute (field or group) reference</li>
	 * <li>relative (field or group) reference</li>
	 * <li>reference by the <b>field</b> short name if it is unique</li>
	 * </ul>
	 * <p>
	 *
	 * @param ec
	 * 		entity container of the model
	 * @param ruleGroup
	 * 		group full name in which the rule exists
	 * @param referencedEntity
	 * 		reference of the entity like it was defined in the rule
	 * @return the {@link IField}- or {@link IGroup}-instance of the entity referenced in a rule (error entity, entity in error message,
	 * 		entity in error condition text). If the entity was not found, {@code null} is returned.
	 */
	IEntity getEntityInRule(IEC ec, String ruleGroup, String referencedEntity);

	/**
	 * @param condition
	 * 		parsed condition, may not be null
	 * @return true if the <code>condition</code> references iteration for the specified
	 * 		<code>indexType</code>, false otherwise
	 */
	boolean referencesIteration(ICondition condition, IndexArt indexType);

	/**
	 * Checks for each repetition level of the error field, if the condition references iteration for that level. (level 0 = root
	 * group and so on - USB is not treated here).
	 *
	 * @param rule
	 * 		used to determine the number of repetition levels of the error field, may not be null
	 * @return for each level: true if the <code>condition</code> references iteration for the level, false otherwise. The length of
	 * 		the return values equals the length of the max. values of the error field. If the condition is null, false is returned for
	 * 		each level
	 */
	boolean[] referencesIteration(IRule rule, ICondition condition);
}
