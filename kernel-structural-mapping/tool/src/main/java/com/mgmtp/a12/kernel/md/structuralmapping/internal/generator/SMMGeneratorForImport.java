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

package com.mgmtp.a12.kernel.md.structuralmapping.internal.generator;

import static com.mgmtp.a12.kernel.md.structuralmapping.internal.util.StructuralMappingModelUtil.createFieldMapping;
import static com.mgmtp.a12.kernel.md.structuralmapping.internal.util.StructuralMappingModelUtil.createFold;
import static com.mgmtp.a12.kernel.md.structuralmapping.internal.util.StructuralMappingModelUtil.createGroupToClear;
import static com.mgmtp.a12.kernel.md.structuralmapping.internal.util.StructuralMappingModelUtil.createMappingBlock;
import static com.mgmtp.a12.kernel.md.structuralmapping.internal.util.StructuralMappingModelUtil.createSMM;
import static com.mgmtp.a12.kernel.md.structuralmapping.internal.util.StructuralMappingModelUtil.createSlice;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.EntityImpl;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.EntityRegistry;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelVisitorWithFullNames;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency.NotificationCode;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency.StructuralMappingModelNotifier;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.util.SMMSorter;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.util.SourceOrTarget;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.GroupsToClearOnFirstFill;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.MappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.FieldMappings;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.ResolutionStrategies;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public class SMMGeneratorForImport {
	public static final String NAME_PREFIX_FOR_SLICE_SOURCE_FIELDS = "I_";
	public static final String ANNOTATION_FOR_MAPPING_WITHOUT_CLEAR = "MAPPING_WITHOUT_CLEAR";

	private final Set<Entity> groupsWhoseDescendantsDontCauseClears;
	private final SMMSorter sorter;

	private final Collection<Entity> potentialTargetFields;
	private final Map<String, List<Entity>> potentialSourceFieldsByShortName;
	private final Map<Entity, List<Entity>> fieldsInTargetByIrg;
	private final Map<Entity, List<Entity>> potentialSliceSourceFieldsByParent;

	private final Set<Entity> groupsToClearOnFirstFill = new HashSet<>();
	private final Map<Entity, GroupClearSources> targetGroupToClearingSources = new HashMap<>();

	private record GroupClearSources(
			Set<Entity> sourceFieldsThatCauseClear,
			Set<Entity> sourceFieldsThatCauseNoClear
	) {
		public GroupClearSources() {
			this(new HashSet<>(), new HashSet<>());
		}

		public boolean hasConflict() {
			return !sourceFieldsThatCauseClear.isEmpty() && !sourceFieldsThatCauseNoClear.isEmpty();
		}
	}

	private SMMGeneratorForImport(Map<String, Entity> sourceEntitiesByFullName,
			Map<String, Entity> targetEntitiesByFullName, Set<Entity> groupsWhoseDescendantsDontCauseClears,
			SMMSorter sorter) {
		this.groupsWhoseDescendantsDontCauseClears = groupsWhoseDescendantsDontCauseClears;
		this.sorter = sorter;

		List<Entity> sourceFields = sourceEntitiesByFullName.values().stream().filter(Entity::isField).toList();
		List<Entity> targetFields = targetEntitiesByFullName.values().stream().filter(Entity::isField).toList();
		potentialTargetFields = targetFields.stream()
				.filter(this::isPotentialSourceOrTargetField)
				.toList();
		potentialSourceFieldsByShortName = sourceFields.stream()
				.filter(this::isPotentialSourceOrTargetField)
				.collect(Collectors.groupingBy(Entity::shortName));
		fieldsInTargetByIrg = targetFields.stream()
				.collect(Collectors.groupingBy(Entity::irg));
		potentialSliceSourceFieldsByParent = sourceFields.stream()
				.filter(this::isPotentialSliceSourceField)
				.collect(Collectors.groupingBy(Entity::parent));
	}

	private static final Pattern potentialSourceOrTargetFieldNamePattern = Pattern.compile("(E[0-9]{7})|(sbz[0-9]{6})");

	private boolean isPotentialSourceOrTargetField(Entity entity) {
		return potentialSourceOrTargetFieldNamePattern.matcher(entity.shortName()).matches();
	}

	private boolean isPotentialSliceSourceField(Entity entity) {
		return entity.shortName().startsWith(NAME_PREFIX_FOR_SLICE_SOURCE_FIELDS);
	}

	private boolean shouldCauseClear(Entity sourceField) {
		return sourceField.ancestors().stream().noneMatch(groupsWhoseDescendantsDontCauseClears::contains);
	}

	@Accessors(fluent = true) @Getter
	private static class DocumentModelInfoCollector extends DocumentModelVisitorWithFullNames {
		private final EntityRegistry.Builder entityRegistryBuilder = EntityRegistry.builder();
		private final Collection<String> groupsWhoseDescendantsDontCauseClears = new HashSet<>();
		private final Map<String, Integer> fullNameToPreorderRank = new HashMap<>();
		private int preorderRank = 0;

		public static DocumentModelInfoCollector collect(IDocumentModel dm) {
			DocumentModelInfoCollector collector = new DocumentModelInfoCollector();
			new DocumentModelWalker().acceptDocumentModel(dm, collector);
			return collector;
		}

		@Override
		public void enterField(IField field) {
			entityRegistryBuilder.field(getFullName());
			fullNameToPreorderRank.put(getFullName(), preorderRank++);
		}

		@Override
		public void enterGroup(IGroup group) {
			entityRegistryBuilder.group(getFullName(), group.getRepeatability());
			if (group.getAnnotations().stream().map(Annotation::getName).anyMatch(ANNOTATION_FOR_MAPPING_WITHOUT_CLEAR::equals)) {
				groupsWhoseDescendantsDontCauseClears.add(getFullName());
			}
			fullNameToPreorderRank.put(getFullName(), preorderRank++);
		}
	}

	/**
	 * Generate a Structural Mapping Model for the ExtMP import.
	 *
	 * @param sourceDocumentModel
	 * 		the SMM source Document Model joined out of the individual source models and the precomputation fragment.
	 * @param targetDocumentModel
	 * 		the SMM target Document Model joined out of the target model and the precomputation fragment
	 * @param id
	 * 		the id the generated Structural Mapping Model should have.
	 * @param notificationReceiver
	 * 		a callback to which problems encountered during generation will be reported. Only problems directly related to the
	 * 		generation are reported, so the resulting Structural Mapping Model might still be invalid even though no errors were
	 * 		reported.
	 * @param locale
	 * 		the locale to report the issues to the {@code notificationReceiver} in
	 * @return a generated Structural Mapping Model. Might be invalid.
	 */
	public static @NonNull MM_StructuralMappingModel_1 generateStructuralMappingModel(
			@NonNull IDocumentModel sourceDocumentModel,
			@NonNull IDocumentModel targetDocumentModel,
			@NonNull String id,
			@NonNull Consumer<? super RankedNotification> notificationReceiver,
			@NonNull Locale locale
	) {
		var notifier = new StructuralMappingModelNotifier(notificationReceiver, locale);
		return createInstance(sourceDocumentModel, targetDocumentModel).generateModel(id, notifier);
	}

	private static @NonNull SMMGeneratorForImport createInstance(
			@NonNull IDocumentModel sourceDocumentModel,
			@NonNull IDocumentModel targetDocumentModel
	) {
		var sourceDMInfoCollector = DocumentModelInfoCollector.collect(sourceDocumentModel);
		var targetDMInfoCollector = DocumentModelInfoCollector.collect(targetDocumentModel);
		Map<String, Entity> sourceEntitiesByFullName = sourceDMInfoCollector.entityRegistryBuilder().build().toMap();
		Map<String, Entity> targetEntitiesByFullName = targetDMInfoCollector.entityRegistryBuilder().build().toMap();
		Set<Entity> groupsWhoseDescendantsDontCauseClears = sourceDMInfoCollector.groupsWhoseDescendantsDontCauseClears().stream()
				.map(sourceEntitiesByFullName::get)
				.collect(Collectors.toSet());
		return new SMMGeneratorForImport(
				sourceEntitiesByFullName, targetEntitiesByFullName,
				groupsWhoseDescendantsDontCauseClears,
				SMMSorter.using(sourceDMInfoCollector.fullNameToPreorderRank(), targetDMInfoCollector.fullNameToPreorderRank()));
	}

	@Accessors(fluent = true) @Getter @RequiredArgsConstructor
	private static class MappingBlockBuilder {
		private final Set<FieldMappings> fieldMappings = new HashSet<>();
		private final Set<ResolutionStrategies> resolutionStrategies = new HashSet<>();

		MappingBlocks build() {
			return createMappingBlock(List.copyOf(resolutionStrategies), List.copyOf(fieldMappings));
		}
	}

	private @NonNull MM_StructuralMappingModel_1 generateModel(String id, StructuralMappingModelNotifier notifier) {
		MappingBlockBuilder mappingBlockBuilder = new MappingBlockBuilder();
		for (Entity targetField : potentialTargetFields) {
			for (Entity sourceField : potentialSourceFieldsByShortName.getOrDefault(targetField.shortName(), List.of())) {
				addToMappingBlock(sourceField, targetField, mappingBlockBuilder, notifier);
			}
		}

		for (Entity groupToClear : groupsToClearOnFirstFill) {
			var groupClearSources = targetGroupToClearingSources.get(groupToClear);
			if (groupClearSources != null && groupClearSources.hasConflict()) {
				notifier.error(null, NotificationCode.AMBIGUOUS_GROUP_CLEAR,
						groupToClear.fullName(),
						listOfEntities(groupClearSources.sourceFieldsThatCauseClear()),
						listOfEntities(groupClearSources.sourceFieldsThatCauseNoClear()));
			}
		}

		List<MappingBlocks> mappingBlocks = List.of(mappingBlockBuilder.build());
		List<GroupsToClearOnFirstFill> groupClears = groupsToClearOnFirstFill.stream()
				.map(groupToClearOnFirstFill -> createGroupToClear(groupToClearOnFirstFill.fullName()))
				.toList();

		MM_StructuralMappingModel_1 smm = createSMM(id, groupClears, mappingBlocks);
		return sorter.sortedSMM(smm);
	}

	private void addToMappingBlock(Entity sourceField, Entity targetField, MappingBlockBuilder mappingBlockBuilder,
			StructuralMappingModelNotifier notifier) {
		// 1. add field mapping
		mappingBlockBuilder.fieldMappings().add(createFieldMapping(sourceField.fullName(), targetField.fullName()));

		// 2. find and add slices
		Set<Entity> unresolvedTargetRepetitionLayers = new HashSet<>(targetField.repetitionLayers());
		var candidateSliceTargetFields = getCandidateSliceFieldsByShortName(targetField, fieldsInTargetByIrg);
		var candidateSliceSourceFields = getCandidateSliceFieldsByShortName(sourceField, potentialSliceSourceFieldsByParent);
		boolean ambiguousSlice = false;
		for (var entry : candidateSliceSourceFields.entrySet()) {
			String sliceSourceFieldName = entry.getKey();
			Set<Entity> sliceSourceFields = entry.getValue();
			String sliceTargetFieldName = sliceSourceFieldName.substring(NAME_PREFIX_FOR_SLICE_SOURCE_FIELDS.length());
			var sliceTargetFields = candidateSliceTargetFields.getOrDefault(sliceTargetFieldName, Set.of());
			if (!sliceTargetFields.isEmpty()) {
				if (sliceSourceFields.size() == 1 && sliceTargetFields.size() == 1) {
					Entity sliceSourceField = sliceSourceFields.iterator().next();
					Entity sliceTargetField = sliceTargetFields.iterator().next();
					Entity sourceGroup = sliceSourceField.hasRepetitionLayer() ? sliceSourceField.irg() : sliceSourceField.parent();
					if (sliceTargetField.hasRepetitionLayer()) { // slice target field is not repeatable --> no valid target group
						Entity targetGroup = sliceTargetField.irg();
						mappingBlockBuilder.resolutionStrategies().add(createSlice(
								sourceGroup.fullName(), targetGroup.fullName(),
								sliceSourceField.fullName(), sliceTargetField.fullName()));
						unresolvedTargetRepetitionLayers.remove(targetGroup);
					}
				} else {
					if (sliceSourceFields.size() >= 2) {
						ambiguousSlice = true;
						notifier.error(null, NotificationCode.AMBIGUOUS_SLICE,
								sourceField.fullName(),
								targetField.fullName(),
								notifier.getString(SourceOrTarget.SOURCE),
								listOfEntities(sliceSourceFields));
					}
					if (sliceTargetFields.size() >= 2) {
						ambiguousSlice = true;
						notifier.error(null, NotificationCode.AMBIGUOUS_SLICE,
								sourceField.fullName(),
								targetField.fullName(),
								notifier.getString(SourceOrTarget.TARGET),
								listOfEntities(sliceTargetFields));
					}
				}
			}
		}

		// 3. find and add folds
		if (ambiguousSlice) {
			return; // no point in trying to find folds if finding the slices failed
		}
		if (unresolvedTargetRepetitionLayers.isEmpty()) {
			// nothing left to do
		} else if (unresolvedTargetRepetitionLayers.size() == 1) {
			Entity sourceGroup = sourceField.hasRepetitionLayer() ? sourceField.irg() : sourceField.parent();
			Entity targetGroup = unresolvedTargetRepetitionLayers.iterator().next();
			mappingBlockBuilder.resolutionStrategies().add(createFold(sourceGroup.fullName(), targetGroup.fullName()));
			// add group clear
			if (shouldCauseClear(sourceField)) {
				groupsToClearOnFirstFill.add(targetGroup);
				targetGroupToClearingSources
						.computeIfAbsent(targetGroup, key -> new GroupClearSources())
						.sourceFieldsThatCauseClear().add(sourceField);
			} else {
				targetField.ancestors().forEach(ancestor ->
						targetGroupToClearingSources
								.computeIfAbsent(ancestor, key -> new GroupClearSources())
								.sourceFieldsThatCauseNoClear().add(sourceField));
			}
		} else {
			notifier.error(null, NotificationCode.AMBIGUOUS_FOLD,
					sourceField.fullName(),
					targetField.fullName(),
					listOfEntities(unresolvedTargetRepetitionLayers));
		}
	}

	private Map<String, Set<Entity>> getCandidateSliceFieldsByShortName(Entity baseField,
			Map<Entity, List<Entity>> potentialSliceFieldsByAncestor) {
		Entity rootGroup = baseField.rootGroup();
		return Stream.concat(Stream.of(EntityImpl.ROOT), baseField.ancestors().stream())
				.flatMap(ancestor -> potentialSliceFieldsByAncestor.getOrDefault(ancestor, List.of()).stream())
				.filter(rootGroup::isAncestorOfOrEqualTo)
				.collect(Collectors.groupingBy(Entity::shortName, Collectors.toSet()));
	}

	private static String listOfEntities(Collection<? extends Entity> entities) {
		return entities.stream().sorted().map(entity -> "'" + entity.fullName() + "'").collect(Collectors.joining(", "));
	}
}
