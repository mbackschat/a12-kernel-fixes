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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.EntityRegistry;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.model.FieldMapping;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.model.Fold;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.model.MappingBlock;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.model.ResolutionStrategy;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.model.Slice;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.model.StructuralMappingModelForCodegen;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingModelService;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency.ResolutionStrategyApplicabilityContext;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.GroupsToClearOnFirstFill;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.MappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.FieldMappings;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.ResolutionStrategies;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * <b>IMPORTANT:</b> This class expects the SMM to be valid (check via
 * {@link StructuralMappingModelService#checkConsistencyFull(MM_StructuralMappingModel_1, Consumer, Locale)}).
 */
@RequiredArgsConstructor
public class BuildStructuralMappingModelForCodegen {
	@NonNull private final Map<String, Entity> sourceEntitiesByFullName;
	@NonNull private final Map<String, Entity> targetEntitiesByFullName;

	public static StructuralMappingModelForCodegen run(@NonNull MM_StructuralMappingModel_1 smm,
			@NonNull Function<String, Optional<Element>> getSourceElementByFullNameFunc,
			@NonNull Function<String, Optional<Element>> getTargetElementByFullNameFunc) {
		var references = new ElementReferenceGatherer();
		references.gatherReferencesInContent(smm.content());
		var instance = new BuildStructuralMappingModelForCodegen(
				createEntities(references.references(SourceOrTarget.SOURCE), getSourceElementByFullNameFunc),
				createEntities(references.references(SourceOrTarget.TARGET), getTargetElementByFullNameFunc));
		return instance.buildStructuralMappingModel(smm);
	}

	private static Map<String, Entity> createEntities(Collection<ElementReferenceGatherer.References> references,
			Function<String, Optional<Element>> getElementByFullNameFunc) {
		var builder = EntityRegistry.builder();
		references.forEach(referencesForElement -> {
			String elementFullName = referencesForElement.elementFullName();
			Element element = getElementByFullNameFunc.apply(elementFullName).orElseThrow();
			if (element instanceof Group group) {
				builder.group(elementFullName, group.getRepeatability());
			} else {
				builder.field(elementFullName);
			}
		});
		return builder.build().toMap();
	}

	private StructuralMappingModelForCodegen buildStructuralMappingModel(@NonNull MM_StructuralMappingModel_1 smm) {
		var p = MM_StructuralMappingModel_1._pointer();
		Set<String> groupsToClear = smm._at(p.content().groupsToClearOnFirstFill()).stream()
				.map(GroupsToClearOnFirstFill::fullName).collect(Collectors.toSet());
		return new StructuralMappingModelForCodegen(
				smm._at(p.content().mappingBlocks()).stream().map(mb -> buildMappingBlock(mb, groupsToClear)).toList(),
				sourceEntitiesByFullName,
				targetEntitiesByFullName);
	}

	private MappingBlock buildMappingBlock(MappingBlocks mappingBlock, Set<String> groupsToClear) {
		Map<ResolutionStrategies, Integer> resolutionStrategyToRank =
				Java8Utils.zipWithIndex(mappingBlock.resolutionStrategies().stream())
						.collect(Collectors.toMap(Pair::getRight, Pair::getLeft));
		ResolutionStrategyApplicabilityContext applicabilityContext = ResolutionStrategyApplicabilityContext.create(mappingBlock);
		return new MappingBlock(
				mappingBlock.fieldMappings().stream()
						.map(fm -> buildFieldMapping(fm, applicabilityContext, groupsToClear, resolutionStrategyToRank)).toList());
	}

	private FieldMapping buildFieldMapping(FieldMappings fieldMapping,
			ResolutionStrategyApplicabilityContext applicabilityContext, Set<String> groupsToClear,
			Map<ResolutionStrategies, Integer> resolutionStrategyToRank) {
		List<ResolutionStrategies> resolutionStrategies = applicabilityContext.applicableResolutionStrategyByLevel(
				fieldMapping.sourceFieldFullName(), fieldMapping.targetFieldFullName());
		Entity groupToClear = ElementPathUtils.getAncestorPaths(fieldMapping.targetFieldFullName()).stream()
				.filter(groupsToClear::contains)
				.findFirst().map(this::targetEntity).orElse(null);
		return new FieldMapping(
				sourceEntity(fieldMapping.sourceFieldFullName()),
				targetEntity(fieldMapping.targetFieldFullName()),
				resolutionStrategies.stream()
						.map(resolutionStrategy -> buildResolutionStrategy(resolutionStrategy,
								resolutionStrategyToRank.getOrDefault(resolutionStrategy, -1)))
						.toList(),
				groupToClear);
	}

	private ResolutionStrategy buildResolutionStrategy(ResolutionStrategies resolutionStrategy, int rank) {
		if (resolutionStrategy == null) {
			return null;
		}
		return switch (resolutionStrategy.type()) {
			case Slice -> new Slice(rank,
					sourceEntity(resolutionStrategy.sourceGroupFullName()),
					targetEntity(resolutionStrategy.targetGroupFullName()),
					sourceEntity(resolutionStrategy.slice().sourceFieldFullName()),
					targetEntity(resolutionStrategy.slice().targetFieldFullName()));
			case Fold -> new Fold(rank,
					sourceEntity(resolutionStrategy.sourceGroupFullName()),
					targetEntity(resolutionStrategy.targetGroupFullName()));
		};
	}

	private Entity sourceEntity(String fullName) {
		return sourceEntitiesByFullName.get(fullName);
	}

	private Entity targetEntity(String fullName) {
		return targetEntitiesByFullName.get(fullName);
	}
}
