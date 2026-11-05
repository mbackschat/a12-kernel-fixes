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

package com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.MappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.ResolutionStrategies;

import lombok.NonNull;

/**
 * This context is created from the resolution strategies in a mapping block and provides utility functions to determine
 * applicability within a Structural Mapping Model.
 */
public class ResolutionStrategyApplicabilityContext {
	@NonNull private final Map<String, Map<String, List<ResolutionStrategies>>> targetGroupToSourceGroupToResolutionStrategies = new HashMap<>();
	@NonNull private final Map<String, List<ResolutionStrategies>> sourceGroupToResolutionStrategies = new HashMap<>();

	public static @NonNull ResolutionStrategyApplicabilityContext create(@NonNull MappingBlocks mappingBlock) {
		return create(mappingBlock.resolutionStrategies());
	}

	public static @NonNull ResolutionStrategyApplicabilityContext create(@NonNull List<ResolutionStrategies> resolutionStrategies) {
		var applicabilityContext = new ResolutionStrategyApplicabilityContext();
		resolutionStrategies.forEach(applicabilityContext::addResolutionStrategy);
		return applicabilityContext;
	}

	public Collection<ResolutionStrategies> resolutionStrategies() {
		return sourceGroupToResolutionStrategies.values().stream().flatMap(List::stream).toList();
	}

	public void addResolutionStrategy(@NonNull ResolutionStrategies rs) {
		// applicability requires source and target group to be set
		if (StringUtils.isEmpty(rs.sourceGroupFullName()) || StringUtils.isEmpty(rs.targetGroupFullName())) {
			return;
		}
		targetGroupToSourceGroupToResolutionStrategies
				.computeIfAbsent(rs.targetGroupFullName(), key -> new HashMap<>())
				.computeIfAbsent(rs.sourceGroupFullName(), key -> new ArrayList<>())
				.add(rs);
		sourceGroupToResolutionStrategies
				.computeIfAbsent(rs.sourceGroupFullName(), key -> new ArrayList<>())
				.add(rs);
	}

	/**
	 * @param targetGroupFullName
	 * 		a valid full name (need not exist).
	 * @return an immutable collection of all resolution strategies in this context for which
	 *        {@link ResolutionStrategies#targetGroupFullName()}} is equal to {@code targetGroupFullName}.
	 */
	public @NonNull Collection<ResolutionStrategies> resolutionStrategiesForTargetGroup(@NonNull String targetGroupFullName) {
		return targetGroupToSourceGroupToResolutionStrategies
				.getOrDefault(targetGroupFullName, Map.of())
				.values().stream()
				.flatMap(List::stream)
				.toList();
	}

	/**
	 * @param sourceGroupFullName
	 * 		a valid full name (need not exist).
	 * @return an immutable collection of all resolution strategies in this context for which
	 *        {@link ResolutionStrategies#sourceGroupFullName()}} is equal to {@code sourceGroupFullName}.
	 */
	public @NonNull Collection<ResolutionStrategies> resolutionStrategiesForSourceGroup(@NonNull String sourceGroupFullName) {
		return sourceGroupToResolutionStrategies.getOrDefault(sourceGroupFullName, List.of());
	}

	public @NonNull List<ResolutionStrategies> applicableResolutionStrategiesForTargetGroup(
			@NonNull String sourceFieldFullName,
			@NonNull String targetGroupFullName
	) {
		List<String> sourceFieldAncestors = ElementPathUtils.getAncestorPaths(sourceFieldFullName);
		var sourceGroupToResolutionStrategiesOfTargetGroup =
				targetGroupToSourceGroupToResolutionStrategies.get(targetGroupFullName);
		if (sourceGroupToResolutionStrategiesOfTargetGroup == null) {
			return List.of();
		}

		return sourceFieldAncestors.stream()
				.map(sourceGroupToResolutionStrategiesOfTargetGroup::get)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.toList();
	}

	/**
	 * @param sourceFieldFullName
	 * 		full name of a field mapping's source field
	 * @param targetFieldFullName
	 * 		full name of a field mapping's target field
	 * @return a list with as many elements as {@code targetFieldFullName} has ancestors, where each element is itself another list
	 * 		of the applicable resolution strategies for that ancestor. Note that having more than one applicable resolution strategy
	 * 		for a level is only possible in the error case.
	 */
	public List<List<ResolutionStrategies>> applicableResolutionStrategiesByLevel(@NonNull String sourceFieldFullName,
			@NonNull String targetFieldFullName) {
		return ElementPathUtils.getAncestorPaths(targetFieldFullName).stream()
				.map(targetFieldAncestor -> applicableResolutionStrategiesForTargetGroup(sourceFieldFullName, targetFieldAncestor))
				.toList();
	}

	public boolean hasMultipleApplicableResolutionStrategiesForAnyLevel(@NonNull String sourceFieldFullName,
			@NonNull String targetFieldFullName) {
		return applicableResolutionStrategiesByLevel(sourceFieldFullName, targetFieldFullName).stream()
				.anyMatch(resolutionStrategies -> resolutionStrategies.size() >= 2);
	}

	public boolean hasApplicableResolutionStrategies(@NonNull String sourceFieldFullName, @NonNull String targetFieldFullName) {
		return applicableResolutionStrategiesByLevel(sourceFieldFullName, targetFieldFullName).stream()
				.anyMatch(resolutionStrategies -> !resolutionStrategies.isEmpty());
	}

	/**
	 * Like {@link #applicableResolutionStrategiesByLevel(String, String)}, but flattens the resulting list to contain either the
	 * applicable resolution strategy for an ancestor or {@code null} if there is none or more than one.
	 */
	public List<ResolutionStrategies> applicableResolutionStrategyByLevel(@NonNull String sourceFieldFullName,
			@NonNull String targetFieldFullName) {
		return applicableResolutionStrategiesByLevel(sourceFieldFullName, targetFieldFullName).stream()
				.map(resolutionStrategies -> resolutionStrategies.size() == 1 ? resolutionStrategies.get(0) : null)
				.toList();
	}
}
