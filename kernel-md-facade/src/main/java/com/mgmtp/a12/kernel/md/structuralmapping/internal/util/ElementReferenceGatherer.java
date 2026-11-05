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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.util.SMMNotificationSource;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.pointers._mm_structuralmappingmodel_1._content.PMappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.pointers._mm_structuralmappingmodel_1._content._mappingblocks.PFieldMappings;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.pointers._mm_structuralmappingmodel_1._content._mappingblocks.PResolutionStrategies;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1.Content;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.MappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.FieldMappings;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.ResolutionStrategies;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks._resolutionstrategies.Type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Looks at all elements referenced by the structural mapping model and tracks for each element and all its ancestors: <ul>
 * <li>who they were referenced by</li>
 * <li>whether they were referenced as a field or a group (or both). All ancestors of directly referenced elements are considered
 * referenced as a group.</li>
 * <li>whether they were referenced as a source or target element</li>
 * </ul>
 * Since all ancestors are also tracked, this always produces a single fully connected tree of source and target elements, which
 * includes all elements directly or indirectly referenced by the structural mapping model.
 */
public class ElementReferenceGatherer {
	private final Map<String, References> sourceElementToReferences = new HashMap<>();
	private final Map<String, References> targetElementToReferences = new HashMap<>();

	@Getter @RequiredArgsConstructor
	@Accessors(fluent = true)
	public static class References {
		private final String elementFullName;
		private final Set<SMMNotificationSource<?>> referencedAsGroupBy = new HashSet<>();
		private final Set<SMMNotificationSource<?>> referencedAsGroupDirectlyBy = new HashSet<>();
		private final Set<SMMNotificationSource<?>> referencedAsFieldBy = new HashSet<>();
		private final Set<SMMNotificationSource<?>> referencedAsFieldDirectlyBy = new HashSet<>();

		public void addReference(FieldOrGroup fieldOrGroup, SMMNotificationSource<?> referencedBy) {
			switch (fieldOrGroup) {
				case FIELD -> referencedAsFieldBy.add(referencedBy);
				case GROUP -> referencedAsGroupBy.add(referencedBy);
			}
		}

		public void addDirectReference(FieldOrGroup fieldOrGroup, SMMNotificationSource<?> referencedBy) {
			addReference(fieldOrGroup, referencedBy);
			switch (fieldOrGroup) {
				case FIELD -> referencedAsFieldDirectlyBy.add(referencedBy);
				case GROUP -> referencedAsGroupDirectlyBy.add(referencedBy);
			}
		}

		public boolean isInconsistent() {
			return hasReferenceAsField() && hasReferenceAsGroup();
		}

		public boolean hasReferenceAsField() {
			return !referencedAsFieldBy.isEmpty();
		}

		public boolean hasReferenceAsGroup() {
			return !referencedAsGroupBy.isEmpty();
		}
	}

	public void gatherReferencesInContent(Content content) {
		var pointer = MM_StructuralMappingModel_1._pointer().content();
		Java8Utils.forEachIndexed(content.groupsToClearOnFirstFill(), (index, groupToClear) ->
				gatherElement(groupToClear.fullName(), FieldOrGroup.GROUP, SourceOrTarget.TARGET,
						SMMNotificationSource.of(pointer.groupsToClearOnFirstFill(index + 1), groupToClear)));
		Java8Utils.forEachIndexed(content.mappingBlocks(),
				(index, mappingBlock) -> gatherReferencesInMappingBlock(mappingBlock, pointer.mappingBlocks(index + 1)));
	}

	private void gatherReferencesInMappingBlock(MappingBlocks mappingBlock, PMappingBlocks<MM_StructuralMappingModel_1> pointer) {
		Java8Utils.forEachIndexed(mappingBlock.fieldMappings(), (index, fieldMapping) ->
				gatherReferencesInFieldMapping(fieldMapping, pointer.fieldMappings(index + 1)));
		Java8Utils.forEachIndexed(mappingBlock.resolutionStrategies(), (index, resolutionStrategy) ->
				gatherReferencesInResolutionStrategy(resolutionStrategy, pointer.resolutionStrategies(index + 1)));
	}

	private void gatherReferencesInFieldMapping(FieldMappings fieldMapping, PFieldMappings<MM_StructuralMappingModel_1> pointer) {
		var referencer = SMMNotificationSource.of(pointer, fieldMapping);
		gatherElement(fieldMapping.sourceFieldFullName(), FieldOrGroup.FIELD, SourceOrTarget.SOURCE, referencer);
		gatherElement(fieldMapping.targetFieldFullName(), FieldOrGroup.FIELD, SourceOrTarget.TARGET, referencer);
	}

	private void gatherReferencesInResolutionStrategy(ResolutionStrategies resolutionStrategy,
			PResolutionStrategies<MM_StructuralMappingModel_1> pointer) {
		var referencer = SMMNotificationSource.of(pointer, resolutionStrategy);
		gatherElement(resolutionStrategy.sourceGroupFullName(), FieldOrGroup.GROUP, SourceOrTarget.SOURCE, referencer);
		gatherElement(resolutionStrategy.targetGroupFullName(), FieldOrGroup.GROUP, SourceOrTarget.TARGET, referencer);
		if (resolutionStrategy.type() == Type.Slice && resolutionStrategy.slice() != null) {
			gatherElement(resolutionStrategy.slice().sourceFieldFullName(), FieldOrGroup.FIELD, SourceOrTarget.SOURCE, referencer);
			gatherElement(resolutionStrategy.slice().targetFieldFullName(), FieldOrGroup.FIELD, SourceOrTarget.TARGET, referencer);
		}
	}

	private void gatherElement(String elementFullName, FieldOrGroup fieldOrGroup, SourceOrTarget sourceOrTarget,
			SMMNotificationSource<?> referencedBy) {
		if (StringUtils.isEmpty(elementFullName)) {
			return;
		}
		Map<String, References> elementToReferences = elementToReferences(sourceOrTarget);
		elementToReferences.computeIfAbsent(elementFullName, References::new).addDirectReference(fieldOrGroup, referencedBy);
		for (String ancestorFullName : ElementPathUtils.getAncestorPaths(elementFullName)) {
			elementToReferences.computeIfAbsent(ancestorFullName, References::new).addReference(FieldOrGroup.GROUP, referencedBy);
		}
	}

	public Collection<References> references(SourceOrTarget sourceOrTarget) {
		return elementToReferences(sourceOrTarget).values();
	}

	private Map<String, References> elementToReferences(SourceOrTarget sourceOrTarget) {
		return switch (sourceOrTarget) {
			case SOURCE -> sourceElementToReferences;
			case TARGET -> targetElementToReferences;
		};
	}
}
