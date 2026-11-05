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

package com.mgmtp.a12.kernel.md.structuralmapping.internal.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelVisitorWithFullNames;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.GroupsToClearOnFirstFill;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.MappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.FieldMappings;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.ResolutionStrategies;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Sorts an SMM according to the provided ranks.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SMMSorter {
	@NonNull private final Map<String, Integer> sourceFullNameToRank;
	@NonNull private final Map<String, Integer> targetFullNameToRank;

	public static SMMSorter using(Map<String, Integer> sourceFullNameToRank, Map<String, Integer> targetFullNameToRank) {
		return new SMMSorter(sourceFullNameToRank, targetFullNameToRank);
	}

	public static SMMSorter usingPreorderOfElements(IDocumentModel sourceDM, IDocumentModel targetDM) {
		return using(PreorderRankCollector.collect(sourceDM), PreorderRankCollector.collect(targetDM));
	}

	public MM_StructuralMappingModel_1 sortedSMM(MM_StructuralMappingModel_1 smm) {
		return smm
				._with(MM_StructuralMappingModel_1._pointer().content().mappingBlocks(),
						smm._at(MM_StructuralMappingModel_1._pointer().content().mappingBlocks()).stream()
								.map(this::sortedMappingBlock)
								.toList())
				._with(MM_StructuralMappingModel_1._pointer().content().groupsToClearOnFirstFill(),
						smm._at(MM_StructuralMappingModel_1._pointer().content().groupsToClearOnFirstFill()).stream()
								.sorted(groupClearComparator())
								.toList());
	}

	public Comparator<GroupsToClearOnFirstFill> groupClearComparator() {
		return Comparator.comparing(GroupsToClearOnFirstFill::fullName);
	}

	public MappingBlocks sortedMappingBlock(MappingBlocks mappingBlock) {
		return mappingBlock
				._with(MappingBlocks._pointer().fieldMappings(),
						sortedFieldMappings(mappingBlock.fieldMappings()))
				._with(MappingBlocks._pointer().resolutionStrategies(),
						sortedResolutionStrategies(mappingBlock.resolutionStrategies()));
	}

	public List<FieldMappings> sortedFieldMappings(List<FieldMappings> fieldMappings) {
		return fieldMappings.stream().sorted(fieldMappingComparator()).toList();
	}

	public Comparator<FieldMappings> fieldMappingComparator() {
		return Comparator
				.comparing(FieldMappings::sourceFieldFullName, Comparator.comparing(this::getSourceRank))
				.thenComparing(FieldMappings::targetFieldFullName, Comparator.comparing(this::getTargetRank));
	}

	public List<ResolutionStrategies> sortedResolutionStrategies(List<ResolutionStrategies> resolutionStrategies) {
		return resolutionStrategies.stream().sorted(resolutionStrategyComparator()).toList();
	}

	public Comparator<ResolutionStrategies> resolutionStrategyComparator() {
		return Comparator
				.comparing(ResolutionStrategies::sourceGroupFullName, Comparator.comparing(this::getSourceRank))
				.thenComparing(ResolutionStrategies::targetGroupFullName, Comparator.comparing(this::getTargetRank));
	}

	public Integer getSourceRank(String fullName) {
		return sourceFullNameToRank.getOrDefault(fullName, -1);
	}

	public Integer getTargetRank(String fullName) {
		return targetFullNameToRank.getOrDefault(fullName, -1);
	}

	@Accessors(fluent = true) @Getter
	public static class PreorderRankCollector extends DocumentModelVisitorWithFullNames {
		private final Map<String, Integer> fullNameToRank = new HashMap<>();
		private int preorderRank = 0;

		public static Map<String, Integer> collect(IDocumentModel dm) {
			var collector = new PreorderRankCollector();
			new DocumentModelWalker().acceptDocumentModel(dm, collector);
			return collector.fullNameToRank();
		}

		@Override public void enterElement(IElement element) {
			super.enterElement(element);
			fullNameToRank.put(getFullName(), preorderRank++);
		}
	}
}
