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

package com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services;

import static com.mgmtp.a12.kernel.md.structuralmapping.internal.util.SMMConstants.FULL_NAME_MAPPING_BLOCKS;
import static com.mgmtp.a12.kernel.md.structuralmapping.internal.util.SMMConstants.FULL_NAME_RESOLUTION_STRATEGIES;
import static com.mgmtp.a12.kernel.md.structuralmapping.internal.util.StructuralMappingModelUtil.createFieldMapping;
import static com.mgmtp.a12.kernel.md.structuralmapping.internal.util.StructuralMappingModelUtil.createFold;
import static com.mgmtp.a12.kernel.md.structuralmapping.internal.util.StructuralMappingModelUtil.createSlice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.FieldWrapper;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.util.SMMNotificationSource;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency.ResolutionStrategyApplicabilityContext;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency.SMMFieldTypeCheckUtils;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency.StructuralMappingModelConsistencyChecker;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency.StructuralMappingModelNotifier;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.pointers._mm_structuralmappingmodel_1._content.PMappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.GroupsToClearOnFirstFill;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.MappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.FieldMappings;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.ResolutionStrategies;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks._resolutionstrategies.Slice;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks._resolutionstrategies.type;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@OnlyForUsage
public class StructuralMappingModelService {
	@NonNull private final DocumentModel sourceDM;
	@NonNull private final DocumentModel targetDM;
	@NonNull private final DocumentModelSearchService sourceDMSS;
	@NonNull private final DocumentModelSearchService targetDMSS;

	private StructuralMappingModelService(@NonNull DocumentModel sourceDM, @NonNull DocumentModel targetDM) {
		this.sourceDM = sourceDM;
		this.targetDM = targetDM;
		this.sourceDMSS = new DocumentModelSearchService(sourceDM);
		this.targetDMSS = new DocumentModelSearchService(targetDM);
	}

	public static @NonNull StructuralMappingModelService create(
			@NonNull DocumentModel sourceDocumentModel,
			@NonNull DocumentModel targetDocumentModel
	) {
		return new StructuralMappingModelService(sourceDocumentModel, targetDocumentModel);
	}

	/**
	 * Checks the basic consistency of the provided structural mapping model without doing checks that would require the broader
	 * context of the mapping model.
	 *
	 * @param smm
	 * 		the structural mapping model which should be checked.
	 * @param notificationReceiver
	 * 		a callback that will be fed all consistency notifications.
	 * 		<p>
	 * 		For the reported notifications, {@link RankedNotification#getSource()} always returns {@code null} or an instance of
	 *        {@link SMMNotificationSource} with the following properties: <ul>
	 * 		<li>{@link SMMNotificationSource#view()} is an instance of one of the following:<ul>
	 * 		<li>{@link FieldMappings}</li>
	 * 		<li>{@link ResolutionStrategies}</li>
	 * 		<li>{@link GroupsToClearOnFirstFill}</li>
	 * 		<li>{@link MappingBlocks}</li>
	 * 		</ul></li>
	 * 		<li>{@link SMMNotificationSource#pointer()} is a valid pointer into the provided {@code smm} that points to the concrete
	 * 		instance wrapped by {@link SMMNotificationSource#view()}. This means that {@link SMMNotificationSource#pointer()} can
	 * 		be used to uniquely identify the reported element in the SMM (e.g. to remove said element from the SMM
	 * 		with {@link GroupInstanceV2#withGroupRemoved(DocumentPointer)}).</li>
	 * 		</ul>
	 * @param locale
	 * 		the locale to report the issues to the {@code notificationReceiver} in.
	 * @return {@code true}, if the model is valid by itself (i.e. no errors were reported), otherwise {@code false}.
	 */
	public static boolean checkConsistencyStandalone(
			@NonNull MM_StructuralMappingModel_1 smm,
			@NonNull Consumer<? super RankedNotification> notificationReceiver,
			@NonNull Locale locale
	) {
		StructuralMappingModelNotifier notifier = new StructuralMappingModelNotifier(notificationReceiver, locale);
		StructuralMappingModelConsistencyChecker.standalone(notifier).checkModel(smm);
		return notifier.hasNoError();
	}

	/**
	 * Checks the full consistency of the provided structural mapping model including checks that require the broader context of
	 * the mapping model.
	 *
	 * @param smm
	 * 		the structural mapping model which should be checked.
	 * @param notificationReceiver
	 * 		a callback that will be fed all consistency notifications.
	 * 		<p>
	 * 		For the reported notifications, {@link RankedNotification#getSource()} always returns {@code null} or an instance of
	 *        {@link SMMNotificationSource} with the following properties: <ul>
	 * 		<li>{@link SMMNotificationSource#view()} is an instance of one of the following:<ul>
	 * 		<li>{@link FieldMappings}</li>
	 * 		<li>{@link ResolutionStrategies}</li>
	 * 		<li>{@link GroupsToClearOnFirstFill}</li>
	 * 		<li>{@link MappingBlocks}</li>
	 * 		</ul></li>
	 * 		<li>{@link SMMNotificationSource#pointer()} is a valid pointer into the provided {@code smm} that points to the concrete
	 * 		instance wrapped by {@link SMMNotificationSource#view()}. This means that {@link SMMNotificationSource#pointer()} can
	 * 		be used to uniquely identify the reported element in the SMM (e.g. to remove said element
	 * 		with {@link GroupInstanceV2#withGroupRemoved(DocumentPointer)}).</li>
	 * 		</ul>
	 * @param locale
	 * 		the locale to report the issues to the {@code notificationReceiver} in.
	 * @return {@code true}, if the model is valid by itself and in the context of the mapping model (i.e. no  errors were
	 * 		reported), otherwise {@code false}.
	 */
	public boolean checkConsistencyFull(
			@NonNull MM_StructuralMappingModel_1 smm,
			@NonNull Consumer<? super RankedNotification> notificationReceiver,
			@NonNull Locale locale
	) {
		StructuralMappingModelNotifier notifier = new StructuralMappingModelNotifier(notificationReceiver, locale);
		StructuralMappingModelConsistencyChecker.full(notifier, sourceDMSS::getByPath, targetDMSS::getByPath).checkModel(smm);
		return notifier.hasNoError();
	}

	/**
	 * Add a field mapping from {@code sourceFieldFullName} to {@code targetFieldFullName} to the provided {@code smm}. Also add
	 * the resolution strategies that are necessary so that all the field mapping's target repetition layers have an applicable
	 * resolution strategy.
	 *
	 * @param smm
	 * 		the Structural Mapping Model to add the field mapping to. This is not mutated. Must be valid (see
	 *        {@link #checkConsistencyFull}).
	 * @param sourceFieldFullName
	 * 		the full name of the field mapping's source field. Must be a field in the source DM.
	 * @param targetFieldFullName
	 * 		the full name of the field mapping's target field. Must be a field in the target DM.
	 * @return a valid Structural Mapping Model with the field mapping and resolution strategies added, or the provided {@code smm}
	 * 		if the field mapping can never be made valid (check the field mapping using {@link #findValidTargetFields(String)} first
	 * 		in order to avoid this case).
	 *
	 * @throws IllegalArgumentException
	 * 		if the source field or target field are not fields in the respective Document Model
	 */
	public @NonNull MM_StructuralMappingModel_1 addFieldMapping(
			@NonNull MM_StructuralMappingModel_1 smm,
			@NonNull String sourceFieldFullName,
			@NonNull String targetFieldFullName
	) {
		var possibleModifications = optionsForAddFieldMapping(smm, sourceFieldFullName, targetFieldFullName);
		return possibleModifications.stream()
				.filter(OptionForAddFieldMapping::originalMappingBlockHadApplicableResolutionStrategies).findFirst()
				.or(() -> possibleModifications.stream().findFirst())
				.map(OptionForAddFieldMapping::modifiedSMM)
				.orElse(smm);
	}

	public record OptionForAddFieldMapping(
			@NonNull MM_StructuralMappingModel_1 originalSMM,
			@NonNull MappingBlocks modifiedMappingBlock,
			@NonNull PMappingBlocks<MM_StructuralMappingModel_1> pointerToModifiedMappingBlock,
			boolean originalMappingBlockHadApplicableResolutionStrategies
	) {
		public MM_StructuralMappingModel_1 modifiedSMM() {
			return originalSMM._with(pointerToModifiedMappingBlock, modifiedMappingBlock);
		}

		public int repetitionOfModifiedMappingBlock() {
			return Java8Utils.getLast(pointerToModifiedMappingBlock._unwrap().getPathParts()).repetitionIndex();
		}
	}

	/**
	 * For each Mapping Block in the provided {@code smm}, as well as an empty Mapping Block, try to add the field mapping from
	 * {@code sourceFieldFullName} to {@code targetFieldFullName} and all missing resolution strategies to it, then check if the
	 * resulting SMM would still be valid. If it is still valid, include the modification in the result.
	 *
	 * @param smm
	 * 		the Structural Mapping Model to add the field mapping to. This is not mutated. Must be valid (see
	 *        {@link #checkConsistencyFull}).
	 * @param sourceFieldFullName
	 * 		the full name of the field mapping's source field. Must be a field in the source DM.
	 * @param targetFieldFullName
	 * 		the full name of the field mapping's target field. Must be a field in the target DM.
	 * @return a list of valid {@link OptionForAddFieldMapping}s. Might be empty if the field mapping can never be made valid (check
	 * 		the field mapping using {@link #findValidTargetFields(String)} first in order to avoid this case).
	 *
	 * @throws IllegalArgumentException
	 * 		if the source field or target field are not fields in the respective Document Model
	 */
	public List<OptionForAddFieldMapping> optionsForAddFieldMapping(
			@NonNull MM_StructuralMappingModel_1 smm,
			@NonNull String sourceFieldFullName,
			@NonNull String targetFieldFullName
	) {
		// sanity check of parameters
		sourceField(sourceFieldFullName);
		targetField(targetFieldFullName);

		List<MappingBlocks> mappingBlocks = smm._at(MM_StructuralMappingModel_1._pointer().content().mappingBlocks());
		return Java8Utils.zipWithIndex(Stream.concat(mappingBlocks.stream(), Stream.of(MappingBlocks._empty())))
				.<OptionForAddFieldMapping>mapMulti((indexAndMappingBlock, consumer) -> {
					int mappingBlockIndex = indexAndMappingBlock.getLeft();
					MappingBlocks mappingBlock = indexAndMappingBlock.getRight();

					// add field mapping to mapping block, or return if it already exists
					FieldMappings fieldMapping = createFieldMapping(sourceFieldFullName, targetFieldFullName);
					if (mappingBlock.fieldMappings().contains(fieldMapping)) {
						return; // not caught in validity check below, because this check is in the metamodel
					}
					mappingBlock = mappingBlock._withAppendedRepetition(MappingBlocks._pointer().fieldMappings(), fieldMapping);

					// add missing resolution strategies to mapping block
					var applicabilityContext = ResolutionStrategyApplicabilityContext.create(mappingBlock);
					List<ResolutionStrategies> resolutionStrategies = findMissingResolutionStrategies(
							sourceFieldFullName, targetFieldFullName, applicabilityContext, mappingBlock.fieldMappings());
					for (ResolutionStrategies rs : resolutionStrategies) {
						mappingBlock = mappingBlock._withAppendedRepetition(MappingBlocks._pointer().resolutionStrategies(), rs);
					}

					// add modified smm to result if the new mapping block is still valid
					var mappingBlockPointer = MM_StructuralMappingModel_1._pointer().content().mappingBlocks(mappingBlockIndex + 1);
					if (checkMappingBlock(mappingBlock)) {
						boolean mbHadApplicableRS = applicabilityContext
								.hasApplicableResolutionStrategies(sourceFieldFullName, targetFieldFullName);
						consumer.accept(new OptionForAddFieldMapping(smm, mappingBlock, mappingBlockPointer, mbHadApplicableRS));
					}
				}).toList();
	}

	private boolean checkMappingBlock(MappingBlocks mappingBlock) {
		StructuralMappingModelNotifier notifier = new StructuralMappingModelNotifier(notification -> {/* do nothing */}, Locale.US);
		var pointer = MM_StructuralMappingModel_1._pointer().content().mappingBlocks(1); // pointer doesn't matter here
		StructuralMappingModelConsistencyChecker
				.full(notifier, sourceDMSS::getByPath, targetDMSS::getByPath)
				.checkMappingBlock(mappingBlock, pointer);
		return !notifier.hasError();
	}

	private List<ResolutionStrategies> findMissingResolutionStrategies(String sourceFieldFullName,
			String targetFieldFullName, ResolutionStrategyApplicabilityContext applicabilityContext,
			List<FieldMappings> fieldMappings) {
		// get applicable resolution strategies
		var applicableResolutionStrategiesByLevel = applicabilityContext
				.applicableResolutionStrategiesByLevel(sourceFieldFullName, targetFieldFullName);
		var applicableResolutionStrategies = applicableResolutionStrategiesByLevel.stream().flatMap(List::stream).toList();

		// determine unresolved target repetition layers
		String lowestUsedTargetLayer = applicableResolutionStrategies.stream()
				.map(ResolutionStrategies::targetGroupFullName).max(Comparator.naturalOrder()).orElse("/");
		List<String> unusedTargetLayersOrdered = // ordered from root to leaf
				ElementPathUtils.getPathsBetween(lowestUsedTargetLayer, targetFieldFullName);
		List<String> unresolvedTargetRepetitionLayersOrdered = // ordered from root to leaf
				unusedTargetLayersOrdered.stream().filter(fullName -> targetGroup(fullName).getRepeatability() > 1).toList();
		if (unresolvedTargetRepetitionLayersOrdered.isEmpty()) {
			return List.of();
		}

		// determine source layers to match with
		String lowestUsedSourceLayer = applicableResolutionStrategies.stream()
				.map(ResolutionStrategies::sourceGroupFullName).max(Comparator.naturalOrder()).orElse("/");
		List<String> unusedSourceLayersOrdered = // ordered from root to leaf
				ElementPathUtils.getPathsBetween(lowestUsedSourceLayer, sourceFieldFullName);
		List<String> unusedSourceRepetitionLayersOrdered = // ordered from root to leaf
				unusedSourceLayersOrdered.stream().filter(fullName -> sourceGroup(fullName).getRepeatability() > 1).toList();

		List<String> sourceLayersToMatchWith;
		if (unusedSourceRepetitionLayersOrdered.isEmpty()) {
			String fallbackSourceLayer = ElementPathUtils.getNumberOfPathParts(lowestUsedSourceLayer) > 0
					? lowestUsedSourceLayer
					: unusedSourceLayersOrdered.getFirst(); // == root group
			sourceLayersToMatchWith = List.of(fallbackSourceLayer);
		} else {
			sourceLayersToMatchWith = unusedSourceRepetitionLayersOrdered;
		}

		// match layers
		List<ResolutionStrategies> resolutionStrategiesOrdered = new ArrayList<>(); // ordered from root to leaf (both source and target)
		// if there are fewer unresolved TRLs than source layers to match with, use the lowest source layers and skip the higher ones
		int sourceLayerIndexIndex = Math.max(0, sourceLayersToMatchWith.size() - unresolvedTargetRepetitionLayersOrdered.size());
		for (String trlFullName : unresolvedTargetRepetitionLayersOrdered) {
			resolutionStrategiesOrdered.add(createFold(sourceLayersToMatchWith.get(sourceLayerIndexIndex), trlFullName));
			// if there are more unresolved TRLs than source layers to match with, reuse the lowest source layer as often as necessary
			sourceLayerIndexIndex = Math.min(sourceLayersToMatchWith.size() - 1, sourceLayerIndexIndex + 1);
		}

		// if there is any applicability conflict with an existing resolution strategy, we switch to the most defensive variant
		// which is to use the source field's parent group for all subsequent missing resolution strategies
		Java8Utils.zipWithIndex(resolutionStrategiesOrdered.stream()).filter(indexAndRs -> {
			ResolutionStrategies rs = indexAndRs.getRight();
			return applicabilityContext.resolutionStrategiesForTargetGroup(rs.targetGroupFullName()).stream()
					.map(ResolutionStrategies::sourceGroupFullName)
					.filter(StringUtils::isNotEmpty)
					.anyMatch(sourceGroup -> ElementPathUtils.isDescendant(sourceGroup, rs.sourceGroupFullName()));
		}).findFirst().ifPresent(indexAndRs -> {
			// The first match is the lowest resolution strategy for which we have an applicability conflict. Everything above is
			// valid and does not need to be adjusted. Everything below is changed to the parent group of the source field. While we
			// could check for each source group how low it needs to go to be valid, it would make the code much more complex. This
			// solution seems good enough since it always guarantees the result to be valid if possible. In either case the result
			// will most likely need to be manually adjusted by the modeler.
			String sourceFieldParentFullName = ElementPathUtils.getParentPath(sourceFieldFullName);
			for (int i = indexAndRs.getLeft(); i < sourceLayersToMatchWith.size(); i++) {
				resolutionStrategiesOrdered.set(i,
						createFold(sourceFieldParentFullName, resolutionStrategiesOrdered.get(i).targetGroupFullName()));
			}
		});

		// convert to slice where possible
		Set<String> targetFields = fieldMappings.stream()
				.map(FieldMappings::targetFieldFullName)
				.filter(StringUtils::isNotEmpty)
				.collect(Collectors.toSet());
		return resolutionStrategiesOrdered.stream().map(rs -> convertToSliceIfPossible(rs, targetFields)).toList();
	}

	/**
	 * @return a slice, if all of the following apply: <ol>
	 * 		<li>target group has index field</li>
	 * 		<li>source group has either index field or field with the same name</li>
	 * 		<li>the two fields have compatible types</li>
	 * 		<li>the slice target field is not also a field mapping's target field.</li>
	 * 		</ol>
	 * 		Otherwise, return {@code fold}.
	 */
	private ResolutionStrategies convertToSliceIfPossible(ResolutionStrategies fold, Set<String> targetFieldFullNames) {
		Group targetGroup = targetGroup(fold.targetGroupFullName());
		Group sourceGroup = sourceGroup(fold.sourceGroupFullName());
		if (targetGroup.getIndexField().isPresent()) {
			Field sliceTargetField = targetGroup.getIndexField().orElseThrow().getDocumentModelObject()
					.orElseThrow(() -> new IllegalArgumentException("unresolved references in target DM"));
			String sliceTargetFieldFullName = ElementUtils.getPath(sliceTargetField);
			Field sliceSourceField;
			if (sourceGroup.getIndexField().isPresent()) {
				sliceSourceField = sourceGroup.getIndexField().orElseThrow().getDocumentModelObject()
						.orElseThrow(() -> new IllegalArgumentException("unresolved references in source DM"));
			} else {
				sliceSourceField = sourceGroup.getElements().stream()
						.filter(element -> sliceTargetField.getName().equals(element.getName()))
						.filter(Field.class::isInstance).map(Field.class::cast)
						.findFirst().orElse(null);
			}
			if (sliceSourceField != null && !targetFieldFullNames.contains(sliceTargetFieldFullName)) {
				FieldType srlIndexFieldType = sliceSourceField.getEffectiveType()
						.orElseThrow(() -> new IllegalArgumentException("unresolved references in source DM"));
				FieldType trlIndexFieldType = sliceTargetField.getEffectiveType()
						.orElseThrow(() -> new IllegalArgumentException("unresolved references in target DM"));
				if (SMMFieldTypeCheckUtils.isFieldTypeAssignable(srlIndexFieldType, trlIndexFieldType)) {
					return createSlice(
							fold.sourceGroupFullName(), fold.targetGroupFullName(),
							ElementUtils.getPath(sliceSourceField), sliceTargetFieldFullName);
				}
			}
		}
		// no slice was found --> return the fold
		return fold;
	}

	/**
	 * Find all target fields which the provided source field could be mapped to. A field returned by this function is guaranteed to
	 * be addable as a field mapping to any SMM with {@link #addFieldMapping}.
	 *
	 * @param sourceFieldFullName
	 * 		the full name of the source field for which to find valid target fields. Must be a field in the source DM.
	 * @return the full names of all source fields which the provided target field could be mapped to
	 */
	public @NonNull Set<String> findValidTargetFields(
			@NonNull String sourceFieldFullName
	) {
		Field sourceField = sourceField(sourceFieldFullName);
		return targetFieldsByFullName().entrySet().stream()
				.filter(fullNameAndTargetField -> isValidFieldMapping(sourceField, fullNameAndTargetField.getValue()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}

	private boolean isValidFieldMapping(Field sourceField, Field targetField) {
		FieldType sourceFieldType = sourceField.getEffectiveType()
				.orElseThrow(() -> new IllegalArgumentException("unresolved references in source DM"));
		FieldType targetFieldType = targetField.getEffectiveType()
				.orElseThrow(() -> new IllegalArgumentException("unresolved references in target DM"));
		return SMMFieldTypeCheckUtils.isFieldTypeAssignable(sourceFieldType, targetFieldType)
				&& !(isRepeatable(sourceField) && !isRepeatable(targetField));
	}

	private boolean isRepeatable(Field field) {
		return ElementUtils.isRepeatable(new FieldWrapper(field));
	}

	private Map<String, Field> targetFieldsByFullName() {
		Map<String, Field> targetFieldsByFullName = new HashMap<>();
		new DocumentModelWalker().acceptDocumentModel(targetDM, new DocumentModelVisitor() {
			private final LinkedList<String> pathStack = new LinkedList<>(List.of(""));

			@Override public void leaveField(Field field) {
				targetFieldsByFullName.put(pathStack.getLast() + "/" + field.getName(), field);
			}

			@Override public void enterGroup(Group group) {
				pathStack.addLast(pathStack.getLast() + "/" + group.getName());
			}

			@Override public void leaveGroup(Group group) {
				pathStack.removeLast();
			}
		});
		return targetFieldsByFullName;
	}

	/**
	 * For the resolution strategy at {@code resolutionStrategyPointer} in {@code smm}, provides the types which
	 * {@link ResolutionStrategies#type()} can be set to without causing the provided {@code smm} to become invalid.
	 *
	 * @param smm
	 * 		the Structural Mapping Model which contains the resolution strategy
	 * @param resolutionStrategyPointer
	 * 		a pointer to the resolution strategy in {@code smm}.
	 * @return resolution strategy types which {@link ResolutionStrategies#type()} could be changed to without causing the provided
	 *        {@code smm} to become invalid.
	 *
	 * @throws IllegalArgumentException
	 * 		if <ul>
	 * 		<li>{@code resolutionStrategyPointer} does not point to a resolution strategy, or</li>
	 * 		<li>{@link ResolutionStrategies#sourceGroupFullName()} is not a group in the source DM, or</li>
	 * 		<li>{@link ResolutionStrategies#targetGroupFullName()} is not a group in the target DM.</li>
	 * 		</ul>
	 */
	public @NonNull Set<type> findValidResolutionStrategyTypes(
			@NonNull MM_StructuralMappingModel_1 smm,
			@NonNull DocumentPointer resolutionStrategyPointer
	) {
		getResolutionStrategy(smm, resolutionStrategyPointer); // check resolution strategy pointer
		MappingBlocks mappingBlock = getMappingBlock(smm, resolutionStrategyPointer.parent());
		int resolutionStrategyRepetition = Java8Utils.getLast(resolutionStrategyPointer.getPathParts()).repetitionIndex();
		return Arrays.stream(type.values()).filter(rsType -> {
			MappingBlocks modifiedMappingBlock = mappingBlock
					._with(MappingBlocks._pointer().resolutionStrategies(resolutionStrategyRepetition).type(), rsType);
			return checkMappingBlock(modifiedMappingBlock);
		}).collect(Collectors.toSet());
	}

	/**
	 * For the resolution strategy at {@code resolutionStrategyPointer} in {@code smm}, provides the full names of groups in the
	 * source DM which {@link ResolutionStrategies#sourceGroupFullName()} could be changed to without causing the provided
	 * {@code smm} to become invalid.
	 *
	 * @param smm
	 * 		the Structural Mapping Model which contains the resolution strategy
	 * @param resolutionStrategyPointer
	 * 		a pointer to the resolution strategy in {@code smm}.
	 * @return the full names of groups in the source DM which {@link ResolutionStrategies#sourceGroupFullName()} could be changed
	 * 		to without causing the provided {@code smm} to become invalid.
	 *
	 * @throws IllegalArgumentException
	 * 		if <ul>
	 * 		<li>{@code resolutionStrategyPointer} does not point to a resolution strategy, or</li>
	 * 		<li>{@link ResolutionStrategies#sourceGroupFullName()} is not a group in the source DM, or</li>
	 * 		<li>{@link ResolutionStrategies#targetGroupFullName()} is not a group in the target DM.</li>
	 * 		</ul>
	 */
	public @NonNull Set<String> findValidSourceGroups(
			@NonNull MM_StructuralMappingModel_1 smm,
			@NonNull DocumentPointer resolutionStrategyPointer
	) {
		ResolutionStrategies resolutionStrategy = getResolutionStrategy(smm, resolutionStrategyPointer);
		MappingBlocks mappingBlock = getMappingBlock(smm, resolutionStrategyPointer.parent());
		int resolutionStrategyRepetition = Java8Utils.getLast(resolutionStrategyPointer.getPathParts()).repetitionIndex();

		return mappingBlock.fieldMappings().stream()
				.filter(fm -> isApplicable(fm, resolutionStrategy))
				.map(FieldMappings::sourceFieldFullName)
				.map(ElementPathUtils::splitAbsPath)
				.reduce((pathA, pathB) -> pathA.subList(0, ElementPathUtils.commonAncestorLevel(pathA, pathB) + 1))
				.map(ElementPathUtils::toAbsPathString).stream()
				.<String>mapMulti((commonAncestorOfApplicableSourceFields, consumer) -> {
					ElementPathUtils.getPathsBetween("/", commonAncestorOfApplicableSourceFields).forEach(consumer);
					consumer.accept(commonAncestorOfApplicableSourceFields);
				})
				.filter(possibleSourceGroup -> {
					var pointer = MappingBlocks._pointer().resolutionStrategies(resolutionStrategyRepetition).sourceGroupFullName();
					MappingBlocks modifiedMappingBlock = mappingBlock._with(pointer, possibleSourceGroup);
					return checkMappingBlock(modifiedMappingBlock);
				}).collect(Collectors.toSet());
	}

	/**
	 * For the resolution strategy at {@code resolutionStrategyPointer} in {@code smm}, provides the full names of fields in the
	 * source DM which {@link Slice#sourceFieldFullName()} could be changed to without causing the provided {@code smm} to become
	 * invalid.
	 *
	 * @param smm
	 * 		the Structural Mapping Model which contains the resolution strategy
	 * @param resolutionStrategyPointer
	 * 		a pointer to the resolution strategy in {@code smm}.
	 * @return the full names of fields in the source DM which {@link Slice#sourceFieldFullName()} could be changed
	 * 		to without causing the provided {@code smm} to become invalid.
	 *
	 * @throws IllegalArgumentException
	 * 		if <ul>
	 * 		<li>{@code resolutionStrategyPointer} does not point to a resolution strategy, or</li>
	 * 		<li>{@link ResolutionStrategies#sourceGroupFullName()} is not a group in the source DM, or</li>
	 * 		<li>{@link ResolutionStrategies#targetGroupFullName()} is not a group in the target DM.</li>
	 * 		</ul>
	 */
	public @NonNull Set<String> findValidSliceSourceFields(
			@NonNull MM_StructuralMappingModel_1 smm,
			@NonNull DocumentPointer resolutionStrategyPointer
	) {
		ResolutionStrategies resolutionStrategy = getResolutionStrategy(smm, resolutionStrategyPointer);
		Group sourceGroup = sourceGroup(resolutionStrategy.sourceGroupFullName());
		String sliceTargetFieldFullName = resolutionStrategy._at(ResolutionStrategies._pointer().slice().targetFieldFullName());
		FieldType sliceTargetFieldType = StringUtils.isNotEmpty(sliceTargetFieldFullName)
				? getFieldType(targetField(sliceTargetFieldFullName))
				: null;
		// 1) the field must be below the target group
		// 2) the path from the target group to the field must not contain a repetition layer
		List<Field> possibleFields = GatherNonRepeatableDescendantFields.below(sourceGroup);
		return possibleFields.stream()
				// 3) field types must match
				.filter(sliceSourceField -> sliceTargetFieldType == null
						|| SMMFieldTypeCheckUtils.isFieldTypeAssignable(getFieldType(sliceSourceField), sliceTargetFieldType))
				.map(ElementUtils::getPath)
				.collect(Collectors.toSet());
	}

	/**
	 * For the resolution strategy at {@code resolutionStrategyPointer} in {@code smm}, provides the full names of fields in the
	 * target DM which {@link Slice#targetFieldFullName()} could be changed to without causing the provided {@code smm} to become
	 * invalid.
	 *
	 * @param smm
	 * 		the Structural Mapping Model which contains the resolution strategy
	 * @param resolutionStrategyPointer
	 * 		a pointer to the resolution strategy in {@code smm}.
	 * @return the full names of fields in the source DM which {@link Slice#targetFieldFullName()} could be changed to without
	 * 		causing the provided {@code smm} to become invalid.
	 *
	 * @throws IllegalArgumentException
	 * 		if <ul>
	 * 		<li>{@code resolutionStrategyPointer} does not point to a resolution strategy, or</li>
	 * 		<li>{@link ResolutionStrategies#sourceGroupFullName()} is not a group in the source DM, or</li>
	 * 		<li>{@link ResolutionStrategies#targetGroupFullName()} is not a group in the target DM.</li>
	 * 		</ul>
	 */
	public @NonNull Set<String> findValidSliceTargetFields(
			@NonNull MM_StructuralMappingModel_1 smm,
			@NonNull DocumentPointer resolutionStrategyPointer
	) {
		ResolutionStrategies resolutionStrategy = getResolutionStrategy(smm, resolutionStrategyPointer);
		Group targetGroup = targetGroup(resolutionStrategy.targetGroupFullName());
		String sliceSourceFieldFullName = resolutionStrategy._at(ResolutionStrategies._pointer().slice().sourceFieldFullName());
		FieldType sliceSourceFieldType = StringUtils.isNotEmpty(sliceSourceFieldFullName)
				? getFieldType(sourceField(sliceSourceFieldFullName))
				: null;
		Set<String> targetFieldFullNames = getMappingBlock(smm, resolutionStrategyPointer.parent()).fieldMappings().stream()
				.map(FieldMappings::targetFieldFullName)
				.filter(StringUtils::isNotEmpty)
				.collect(Collectors.toSet());
		// 1) the field must be below the target group
		// 2) the path from the target group to the field must not contain a repetition layer
		List<Field> possibleFields = GatherNonRepeatableDescendantFields.below(targetGroup);
		return possibleFields.stream()
				// 3) field types must match
				.filter(sliceTargetField -> sliceSourceFieldType == null
						|| SMMFieldTypeCheckUtils.isFieldTypeAssignable(sliceSourceFieldType, getFieldType(sliceTargetField)))
				.map(ElementUtils::getPath)
				// 4) slice's target field must not also be a field mapping's target field
				.filter(sliceTargetFieldFullName -> !targetFieldFullNames.contains(sliceTargetFieldFullName))
				.collect(Collectors.toSet());
	}

	private static boolean isApplicable(FieldMappings fieldMapping, ResolutionStrategies resolutionStrategy) {
		return StringUtils.isNotEmpty(fieldMapping.sourceFieldFullName())
				&& StringUtils.isNotEmpty(fieldMapping.targetFieldFullName())
				&& ElementPathUtils.isDescendant(fieldMapping.sourceFieldFullName(), resolutionStrategy.sourceGroupFullName())
				&& ElementPathUtils.isDescendant(fieldMapping.targetFieldFullName(), resolutionStrategy.targetGroupFullName());
	}

	@Getter @Accessors(fluent = true)
	private static class GatherNonRepeatableDescendantFields extends DocumentModelVisitor {
		private final List<Field> nonRepeatableDescendantFields = new ArrayList<>();

		public static List<Field> below(Group group) {
			var visitor = new GatherNonRepeatableDescendantFields();
			new DocumentModelWalker().acceptElements(group.getElements(), visitor);
			return visitor.nonRepeatableDescendantFields();
		}

		@Override public com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess visitGroup(Group group) {
			if (group.getRepeatability() > 1) {
				return com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
			}
			return super.visitGroup(group);
		}

		@Override public void leaveField(Field field) {
			nonRepeatableDescendantFields.add(field);
		}
	}

	private static FieldType getFieldType(Field field) {
		return field.getEffectiveType().orElseThrow(() -> new IllegalArgumentException("unresolved references in DM"));
	}

	private static ResolutionStrategies getResolutionStrategy(MM_StructuralMappingModel_1 smm,
			DocumentPointer resolutionStrategyPointer) {
		if (!resolutionStrategyPointer.fullName().equals(FULL_NAME_RESOLUTION_STRATEGIES)) {
			throw new IllegalArgumentException(
					"The provided pointer does not point to a resolution strategy: " + resolutionStrategyPointer.fullName());
		}
		ResolutionStrategies resolutionStrategy = ResolutionStrategies._viewOf(smm._unwrap().group(resolutionStrategyPointer));
		if (StringUtils.isEmpty(resolutionStrategy.sourceGroupFullName())
				|| StringUtils.isEmpty(resolutionStrategy.targetGroupFullName())) {
			throw new IllegalArgumentException("The provided resolution strategy must have a source and target group.");
		}
		return resolutionStrategy;
	}

	private static MappingBlocks getMappingBlock(MM_StructuralMappingModel_1 smm, DocumentPointer mappingBlockPointer) {
		if (!mappingBlockPointer.fullName().equals(FULL_NAME_MAPPING_BLOCKS)) {
			throw new IllegalArgumentException(
					"The provided pointer does not point to a mapping block: " + mappingBlockPointer.fullName());
		}
		return MappingBlocks._viewOf(smm._unwrap().group(mappingBlockPointer));
	}

	private Field sourceField(String fullName) {
		if (!(sourceElement(fullName) instanceof Field field)) {
			throw new IllegalArgumentException("The element " + fullName + " is not a field in the source");
		}
		return field;
	}

	private Field targetField(String fullName) {
		if (!(targetElement(fullName) instanceof Field field)) {
			throw new IllegalArgumentException("The element " + fullName + " is not a field in the target");
		}
		return field;
	}

	private Group sourceGroup(String fullName) {
		if (!(sourceElement(fullName) instanceof Group group)) {
			throw new IllegalArgumentException("The element " + fullName + " is not a group in the source");
		}
		return group;
	}

	private Group targetGroup(String fullName) {
		if (!(targetElement(fullName) instanceof Group group)) {
			throw new IllegalArgumentException("The element " + fullName + " is not a group in the target");
		}
		return group;
	}

	private Element sourceElement(String fullName) {
		return sourceDMSS.getByPath(fullName).orElseThrow(
				() -> new IllegalArgumentException("The element " + fullName + " does not exist in the source"));
	}

	private Element targetElement(String fullName) {
		return targetDMSS.getByPath(fullName).orElseThrow(
				() -> new IllegalArgumentException("The element " + fullName + " does not exist in the target"));
	}
}
