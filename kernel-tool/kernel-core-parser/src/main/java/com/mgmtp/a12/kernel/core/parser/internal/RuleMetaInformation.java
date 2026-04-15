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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parser.internal.util.ReferencedIdsUtil;
import com.mgmtp.a12.kernel.core.parser.internal.util.RegelReferenzenErmittler;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.IndexArt;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleMetaInformation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.ECUtils;

/**
 * Implementation of {@link IRuleMetaInformation} providing methods to extract meta information of a rule.
 */
public class RuleMetaInformation implements IRuleMetaInformation {

	/**
	 * Entity Container providing all information about the fields and the groups.
	 */
	private final IEC ec;
	private final IServiceConfiguration serviceConfiguration;

	public RuleMetaInformation(IServiceConfiguration serviceConfiguration) {
		this.serviceConfiguration = serviceConfiguration;
		this.ec = serviceConfiguration.getIEC();
	}

	@Override
	public Set<IEntity> getDirectlyReferencedEntities(IRule rule, IProblemReporter pm) {
		ParserService ps = new ParserService(serviceConfiguration);
		return ps.getDirectlyReferencedEntities(rule, pm);
	}

	@Override
	public Set<IEntity> getDirectlyReferencedEntities(ICondition condition) {
		Predicate<IEntityId> filterRootGroups = id -> !EntityUtils.isRootGroup(id.getFullName());
		return ReferencedIdsUtil.getDirectlyReferencedEntityNamesWithoutRootGroups(condition, filterRootGroups, ec).stream()
				.map(ec::get)
				.collect(Collectors.toSet());
	}

	@Override public Set<String> getCandidateRelpathsForCustomRefErrorEntitiesA12DV(IRule rule, IProblemReporter pr) {
		return getCandidateRelpathsForCustomRefErrorEntitiesA12DV(rule, pr, null);
	}

	/**
	 * @param customRule2ConditionNoExpandForCustomRefErrorEntities
	 * 		optional, if null, the rule will be parsed to obtain the
	 * 		condition
	 */
	public <R extends IRule> Set<String> getCandidateRelpathsForCustomRefErrorEntitiesA12DV(
			R rule,
			IProblemReporter pr,
			Function<R, ICondition> customRule2ConditionNoExpandForCustomRefErrorEntities
	) {
		if (ExtmpUtils.isHModel(serviceConfiguration.getModelSpecificSettings())) {
			return Set.of();
		}

		ICondition condition;
		if (customRule2ConditionNoExpandForCustomRefErrorEntities != null) {
			condition = customRule2ConditionNoExpandForCustomRefErrorEntities.apply(rule);
		} else {
			condition = new ParserService(serviceConfiguration)
					.parseAndCheckCondition(rule, pr, ExpandMode.NO_EXPAND);
		}
		if (condition == null) {
			return Set.of(); // problems should already have been reported during parsing
		}

		int levelToStartDuplicateSearch = -1; // if we only need the fullnames/relpaths, we don't care about the indices
		return getCandidatesForCustomRefErrorEntitiesFromConditionA12DV(condition, ec, rule, levelToStartDuplicateSearch).keySet();
	}

	public static Map<String, Set<IEntityId>> getCandidatesForCustomRefErrorEntitiesFromConditionA12DV(
			ICondition conditionNoExpand,
			IEC ec,
			IRule rule4Fullname,
			int levelToStartDuplicateSearch
	) {
		if (conditionNoExpand == null) {
			assert false :
					"conditionNoExpand is not expected to be null here but we continue with an empty map as a defensive default";
			return Map.of();
		}
		final boolean isA12Model = true;
		ICondition _expandedCondition = null; // this should not be accessed, we need the unexpanded condition for customRefErrorEntities
		RegelReferenzenErmittler re = new RegelReferenzenErmittler(_expandedCondition, conditionNoExpand, ec,
				// is this the right ec?
				levelToStartDuplicateSearch);
		return re.getReferencedEntityIds4RefEntity(isA12Model).stream().collect(
				Collectors.groupingBy(
						eId -> EntityUtils.entityId2RelpathNoValidate(eId, rule4Fullname, true),
						HashMap::new,
						Collectors.toSet()));
	}

	@Override
	public IEntity getEntityInRule(IEC ec, String ruleGroup, String referencedEntity) {
		return ECUtils.getEntityInRule(ec, ruleGroup, referencedEntity);
	}

	@Override
	public boolean referencesIteration(ICondition condition, IndexArt indexType) {
		return AnalyseService.referenziertIteration(condition, indexType);
	}

	@Override
	public boolean[] referencesIteration(IRule rule, ICondition condition) {
		boolean[] iterationPerLevel = new boolean[rule.getErrorEntity().getMax().length];
		for (int i = 0; i < iterationPerLevel.length; i++) {
			iterationPerLevel[i] = AnalyseService.isIteration(condition, i, true);
		}
		return iterationPerLevel;
	}
}
