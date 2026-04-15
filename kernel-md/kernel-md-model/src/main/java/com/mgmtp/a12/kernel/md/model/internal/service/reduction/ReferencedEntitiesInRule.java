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

package com.mgmtp.a12.kernel.md.model.internal.service.reduction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.mgmtp.a12.kernel.core.facade.internal.MVKService;
import com.mgmtp.a12.kernel.core.parser.internal.util.ReferencedIdsUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.service.internal.errortext.RuleErrorTextParameterService;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IMVK_Service;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ReferencedEntitiesInRule {

	/**
	 * @param mvkService
	 * 		the {@link IMVK_Service} necessary to analyze the rule. Must not be {@code null}.
	 * @param rule
	 * 		the {@link IRule} to be analyzed. Must not be {@code null}.
	 * @param pr
	 * 		the problem reporter, the error messages should be given to.
	 * @return The sets of referenced entities of the rule and of the root groups with additional fields.
	 */
	public ReferencedEntitiesInRuleResult compute(MVKService mvkService, IRule rule, IProblemReporter pr) {
		ICondition condition = mvkService.parseBedingungExpand(rule, pr);
		if (condition == null) {
			return new ReferencedEntitiesInRuleResult(Collections.emptySet(), Collections.emptySet());
		}
		Set<IEntity> referencedEntities = new HashSet<>();
		Set<IGroup> directlyReferencedRootGroups = new HashSet<>();

		addRefEntitiesInConditionText(mvkService, condition, referencedEntities, directlyReferencedRootGroups);
		addErrorEntity(rule.getErrorEntity(), referencedEntities);
		addRefEntitiesInErrorMessage(mvkService, rule, condition, pr, referencedEntities);
		return new ReferencedEntitiesInRuleResult(referencedEntities, directlyReferencedRootGroups);
	}

	private void addRefEntitiesInConditionText(MVKService mvkService, ICondition condition,
			Set<IEntity> referencedEntities, Set<IGroup> directlyReferencedRootGroups) {
		// all fields referenced (directly or via group) in the rule condition are collected
		referencedEntities.addAll(mvkService.getAllReferencedFields(condition));
		// directly referenced rootGroups are obviously not treated by the method above => adding them
		ReferencedIdsUtil.getReferenzierteVordruckNamen(condition).stream()
				.map(s -> mvkService.getIEC().get(String.format("/%s", s)))
				.forEach(rg -> {
					referencedEntities.add(rg);
					directlyReferencedRootGroups.add((IGroup) rg);
				});
	}

	/**
	 * Add the error field explicitly in case it is not yet referenced.
	 *
	 * @param errorEntity
	 * 		the {@link IEntity} to be analyzed. Can be {@code null}.
	 * @param entities
	 * 		the {@link Set<IEntity>} to be updated.
	 */
	private void addErrorEntity(IEntity errorEntity, Set<IEntity> entities) {
		if (errorEntity == null) {
			return;
		}
		entities.add(errorEntity);
	}

	/**
	 * Add referenced entities of error message explicitly in case they are not yet referenced.
	 */
	private void addRefEntitiesInErrorMessage(MVKService mvkService, IRule rule, ICondition condition, IProblemReporter pr,
			Set<IEntity> entities) {
		RuleErrorTextParameterService service = new RuleErrorTextParameterService(mvkService);
		entities.addAll(service.getReferencedEntities(rule, condition, pr));
	}

	public record ReferencedEntitiesInRuleResult(@Getter Set<IEntity> referencedEntities,
												 @Getter Set<IGroup> directlyReferencedRootGroups) {
	}
}
