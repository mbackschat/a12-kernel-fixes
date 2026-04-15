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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingMetaModelService;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.util.SMMNotificationSource;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.util.ElementReferenceGatherer;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.util.FieldOrGroup;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.util.SourceOrTarget;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.pointers._mm_structuralmappingmodel_1.PContent;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.pointers._mm_structuralmappingmodel_1._content.PMappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.pointers._mm_structuralmappingmodel_1._content._mappingblocks.PFieldMappings;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.pointers._mm_structuralmappingmodel_1._content._mappingblocks.PResolutionStrategies;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1.Content;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.GroupsToClearOnFirstFill;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.MappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.FieldMappings;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.ResolutionStrategies;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks._resolutionstrategies.type;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Checks the consistency of a Structural Mapping Model. Can do two different modes: <ul>
 * <li>{@link #standalone}: only checks that do not require the source and target DM are done</li>
 * <li>{@link #full}: all checks including those that require the source and target DM are done</li>
 * </ul>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StructuralMappingModelConsistencyChecker {
	@NonNull private final StructuralMappingModelNotifier notifier;
	private final boolean isFullCheck;
	private final Function<String, Optional<Element>> getSourceElementByFullNameFunc;
	private final Function<String, Optional<Element>> getTargetElementByFullNameFunc;

	public static StructuralMappingModelConsistencyChecker standalone(@NonNull StructuralMappingModelNotifier notifier) {
		return new StructuralMappingModelConsistencyChecker(notifier, false, null, null);
	}

	public static StructuralMappingModelConsistencyChecker full(@NonNull StructuralMappingModelNotifier notifier,
			@NonNull Function<String, Optional<Element>> getSourceElementByFullNameFunc,
			@NonNull Function<String, Optional<Element>> getTargetElementByFullNameFunc) {
		return new StructuralMappingModelConsistencyChecker(notifier, true, getSourceElementByFullNameFunc,
				getTargetElementByFullNameFunc);
	}

	public void checkModel(@NonNull MM_StructuralMappingModel_1 smm) {
		if (!checkMetaModel(smm)) {
			return; // the subsequent checks assume that all required groups, subgroups and fields of the
			// MM_StructuralMappingModel_1 are not null, so we can only continue if the metamodel checks succeeded
			// if this is ever changed, the tests must be expanded to cover cases where required fields are not specified and entity
			// full names are not valid
		}
		if (smm.content() != null) {
			checkContent(smm.content(), MM_StructuralMappingModel_1._pointer().content());
		}
	}

	private boolean checkMetaModel(@NonNull MM_StructuralMappingModel_1 smm) {
		IDocumentValidationResult result = StructuralMappingMetaModelService.validate(smm, notifier.locale());
		result.getMessages().forEach(message -> notifier.forwardMetaModelValidationMessage(message, smm));
		return result.noErrorOccurred();
	}

	private void checkContent(@NonNull Content content, @NonNull PContent<MM_StructuralMappingModel_1> pointer) {
		checkGroupsToClearDoNotConflict(content.groupsToClearOnFirstFill(), pointer);

		var references = new ElementReferenceGatherer();
		references.gatherReferencesInContent(content);
		checkElementTypesAreConsistent(references);
		if (isFullCheck) {
			checkElementsExistAndHaveCorrectTypeInDM(references);
		}

		Java8Utils.forEachIndexed(content.mappingBlocks(),
				(index, mappingBlock) -> checkMappingBlock(mappingBlock, pointer.mappingBlocks(index + 1)));
	}

	public void checkMappingBlock(@NonNull MappingBlocks mappingBlock,
			@NonNull PMappingBlocks<MM_StructuralMappingModel_1> pointer) {
		ResolutionStrategyApplicabilityContext applicabilityContext = ResolutionStrategyApplicabilityContext.create(mappingBlock);
		checkResolutionStrategiesHaveApplicableFieldMappings(mappingBlock, pointer, applicabilityContext);

		checkFieldMappings(mappingBlock, pointer, applicabilityContext);
		checkResolutionStrategies(mappingBlock, pointer, applicabilityContext);
	}

	private void checkFieldMappings(MappingBlocks mappingBlock, PMappingBlocks<MM_StructuralMappingModel_1> pointer,
			ResolutionStrategyApplicabilityContext applicabilityContext) {
		Map<String, List<FieldMappings>> fieldMappingsByTargetField = mappingBlock.fieldMappings().stream()
				.filter(fm -> !applicabilityContext.hasMultipleApplicableResolutionStrategiesForAnyLevel(
						fm.sourceFieldFullName(), fm.targetFieldFullName()))
				.collect(Collectors.groupingBy(FieldMappings::targetFieldFullName, Collectors.toList()));
		Map<String, List<ResolutionStrategies>> slicesByTargetField = mappingBlock.resolutionStrategies().stream()
				.filter(rs -> rs.type() == type.Slice
						&& rs.slice() != null
						&& StringUtils.isNotEmpty(rs.slice().targetFieldFullName()))
				.collect(Collectors.groupingBy(rs -> rs.slice().targetFieldFullName(), Collectors.toList()));
		Java8Utils.forEachIndexed(mappingBlock.fieldMappings(),
				(index, fm) -> checkFieldMapping(
						fm,
						pointer.fieldMappings(index + 1),
						applicabilityContext,
						fieldMappingsByTargetField.getOrDefault(fm.targetFieldFullName(), List.of()),
						slicesByTargetField.getOrDefault(fm.targetFieldFullName(), List.of())));
	}

	public void checkFieldMapping(
			@NonNull FieldMappings fieldMapping,
			@NonNull PFieldMappings<MM_StructuralMappingModel_1> pointer,
			@NonNull ResolutionStrategyApplicabilityContext applicabilityContext,
			@NonNull Collection<FieldMappings> fieldMappingsForTargetField,
			@NonNull Collection<ResolutionStrategies> slicesForTargetField
	) {
		if (StringUtils.isEmpty(fieldMapping.sourceFieldFullName())
				|| StringUtils.isEmpty(fieldMapping.targetFieldFullName())) {
			return; // nothing to check, metamodel will produce errors
		}
		checkFieldMappingDoesNotTargetSliceTargetField(fieldMapping, pointer, slicesForTargetField);
		checkFieldMappingWillNotCauseGuaranteedCollision(fieldMapping, pointer, applicabilityContext, fieldMappingsForTargetField);

		if (isFullCheck) {
			checkFieldMappingDoesNotHaveUnresolvableTargetRepetitionLayers(fieldMapping, pointer, applicabilityContext);
			checkFieldMappingDoesNotHaveUnusedSourceIRG(fieldMapping, pointer, applicabilityContext);
			checkFieldMappingSourceFieldIsAssignableToTargetField(fieldMapping, pointer);
		}
	}

	private void checkResolutionStrategies(MappingBlocks mappingBlock, PMappingBlocks<MM_StructuralMappingModel_1> pointer,
			ResolutionStrategyApplicabilityContext applicabilityContext) {
		Java8Utils.forEachIndexed(mappingBlock.resolutionStrategies(),
				(index, rs) -> checkResolutionStrategy(rs, pointer.resolutionStrategies(index + 1), applicabilityContext));
	}

	public void checkResolutionStrategy(@NonNull ResolutionStrategies resolutionStrategy,
			@NonNull PResolutionStrategies<MM_StructuralMappingModel_1> pointer,
			@NonNull ResolutionStrategyApplicabilityContext applicabilityContext) {
		if (StringUtils.isEmpty(resolutionStrategy.sourceGroupFullName())
				|| StringUtils.isEmpty(resolutionStrategy.targetGroupFullName())) {
			return; // nothing to check, metamodel will produce errors
		}

		checkResolutionStrategyDoesNotAlwaysApplyTogether(resolutionStrategy, pointer, applicabilityContext);
		checkResolutionStrategyDoesNotCrossOtherResolutionStrategy(resolutionStrategy, pointer, applicabilityContext);

		if (isFullCheck) {
			checkResolutionStrategyHasRepeatableTargetGroup(resolutionStrategy, pointer);
		}

		if (resolutionStrategy.type() == type.Slice && resolutionStrategy.slice() != null) {
			checkSliceSourceFieldIsDescendantOfSourceGroup(resolutionStrategy, pointer);
			checkSliceTargetFieldIsDescendantOfTargetGroup(resolutionStrategy, pointer);
			if (isFullCheck) {
				checkSliceSourceFieldIsNotRepeatablyReferenced(resolutionStrategy, pointer);
				checkSliceTargetFieldIsNotRepeatablyReferenced(resolutionStrategy, pointer);
				checkSliceSourceFieldIsAssignableToSliceTargetField(resolutionStrategy, pointer);
			}
		}

	}

	private void checkElementTypesAreConsistent(ElementReferenceGatherer references) {
		checkElementTypesAreConsistent(references, SourceOrTarget.SOURCE);
		checkElementTypesAreConsistent(references, SourceOrTarget.TARGET);
	}

	private void checkElementTypesAreConsistent(ElementReferenceGatherer references, SourceOrTarget sourceOrTarget) {
		references.references(sourceOrTarget).stream()
				.filter(ElementReferenceGatherer.References::isInconsistent)
				.forEach(inconsistentReferencesToElement ->
						notifier.error(null, NotificationCode.ENTITY_USED_AS_FIELD_AND_GROUP,
								notifier.getString(sourceOrTarget),
								inconsistentReferencesToElement.elementFullName()));
	}

	private void checkElementsExistAndHaveCorrectTypeInDM(ElementReferenceGatherer references) {
		checkElementsExistAndHaveCorrectTypeInDM(references, SourceOrTarget.SOURCE, getSourceElementByFullNameFunc);
		checkElementsExistAndHaveCorrectTypeInDM(references, SourceOrTarget.TARGET, getTargetElementByFullNameFunc);
	}

	private void checkElementsExistAndHaveCorrectTypeInDM(ElementReferenceGatherer references, SourceOrTarget sourceOrTarget,
			Function<String, Optional<Element>> getElementByFullNameFunc) {
		references.references(sourceOrTarget).stream()
				.filter(referencesToElement -> !referencesToElement.isInconsistent())
				.forEach(referencesToElement ->
						getElementByFullNameFunc.apply(referencesToElement.elementFullName())
								.ifPresentOrElse(
										elementInDM -> {
											if (!(elementInDM instanceof Field)) {
												referencesToElement.referencedAsFieldDirectlyBy().forEach(referencer ->
														notifier.error(referencer, NotificationCode.ENTITY_NOT_CORRECT_TYPE_IN_DM,
																notifier.getString(sourceOrTarget),
																referencesToElement.elementFullName(),
																notifier.getString(FieldOrGroup.FIELD),
																notifier.getString(elementInDM)));
											}
											if (!(elementInDM instanceof Group)) {
												referencesToElement.referencedAsGroupDirectlyBy().forEach(referencer ->
														notifier.error(referencer, NotificationCode.ENTITY_NOT_CORRECT_TYPE_IN_DM,
																notifier.getString(sourceOrTarget),
																referencesToElement.elementFullName(),
																notifier.getString(FieldOrGroup.GROUP),
																notifier.getString(elementInDM)));
											}
										},
										() -> Stream.concat(
														referencesToElement.referencedAsGroupDirectlyBy().stream(),
														referencesToElement.referencedAsFieldDirectlyBy().stream())
												.forEach(referencer -> notifier.error(referencer, NotificationCode.ENTITY_NOT_IN_DM,
														notifier.getString(sourceOrTarget),
														referencesToElement.elementFullName()))));
	}

	private void checkGroupsToClearDoNotConflict(List<GroupsToClearOnFirstFill> groupsToClearOnFirstFill,
			PContent<MM_StructuralMappingModel_1> pointer) {
		// Note: this is quadratic, but much more readable than the alternative --> should never really matter anyway
		for (int groupAIndex = 0; groupAIndex < groupsToClearOnFirstFill.size(); groupAIndex++) {
			var groupA = groupsToClearOnFirstFill.get(groupAIndex);
			for (int groupBIndex = 0; groupBIndex < groupsToClearOnFirstFill.size(); groupBIndex++) {
				var groupB = groupsToClearOnFirstFill.get(groupBIndex);
				if (groupAIndex != groupBIndex && ElementPathUtils.isDescendant(groupA.fullName(), groupB.fullName())) {
					notifier.error(SMMNotificationSource.of(pointer.groupsToClearOnFirstFill(groupAIndex + 1), groupA),
							NotificationCode.GROUP_CLEAR_ON_FILL_CONFLICT,
							groupA.fullName(), groupB.fullName());
					break; // only report a single conflict
				}
			}
		}
	}

	private void checkFieldMappingWillNotCauseGuaranteedCollision(FieldMappings fieldMapping,
			PFieldMappings<MM_StructuralMappingModel_1> pointer,
			ResolutionStrategyApplicabilityContext applicabilityContext, Collection<FieldMappings> otherFieldMappings) {
		var applicableResolutionStrategiesByLevel = applicabilityContext.applicableResolutionStrategyByLevel(
				fieldMapping.sourceFieldFullName(), fieldMapping.targetFieldFullName());
		List<FieldMappings> collidingFieldMappings = otherFieldMappings.stream().filter(otherFieldMapping -> {
			if (!fieldMapping.sourceFieldFullName().equals(otherFieldMapping.sourceFieldFullName())) {
				var otherApplicableResolutionStrategiesByLevel = applicabilityContext.applicableResolutionStrategyByLevel(
						otherFieldMapping.sourceFieldFullName(), otherFieldMapping.targetFieldFullName());
				return resolutionStrategiesWillChooseTheSameRepetitions(
						applicableResolutionStrategiesByLevel, otherApplicableResolutionStrategiesByLevel);
			}
			return false;
		}).toList();
		if (!collidingFieldMappings.isEmpty()) {
			notifier.error(SMMNotificationSource.of(pointer, fieldMapping),
					NotificationCode.FIELD_MAPPING_WILL_CAUSE_COLLISION,
					fieldMapping.sourceFieldFullName(),
					fieldMapping.targetFieldFullName(),
					collidingFieldMappings.stream()
							.map(FieldMappings::sourceFieldFullName).sorted()
							.map(fullName -> "'" + fullName + "'")
							.collect(Collectors.joining(", ")));
		}
	}

	/**
	 * we try to find a level for which the resolution strategies can be determined to not result in the same repetition
	 */
	private static boolean resolutionStrategiesWillChooseTheSameRepetitions(List<ResolutionStrategies> resolutionStrategiesByLevel1,
			List<ResolutionStrategies> resolutionStrategiesByLevel2) {
		for (int zeroBasedLevel = 0; zeroBasedLevel < resolutionStrategiesByLevel1.size(); zeroBasedLevel++) {
			var rs1 = resolutionStrategiesByLevel1.get(zeroBasedLevel);
			var rs2 = resolutionStrategiesByLevel2.get(zeroBasedLevel);
			if (rs1 == null || rs2 == null) {
				// if there's no resolution strategy for a level, the field mappings may still clash
				// --> continue looking on next level
			} else if (rs1.type() == type.Slice && rs2.type() == type.Slice) {
				// if there's a slice that's not shared it's indeterminable whether the same field instance is targeted
				if (!slicesResolveToSameInstance(rs1, rs2)) {
					return false;
				}
			} else if (rs1.type() == type.Fold && rs2.type() == type.Fold) {
				// if there's a fold that's not shared the field mappings are guaranteed to target different field instances
				if (!foldsResolveToSameInstance(rs1, rs2)) {
					return false;
				}
			} else {
				// if there's different resolution strategy types for a level, they target different field instances
				return false;
			}
		}
		// otherwise --> true
		return true;
	}

	private static boolean slicesResolveToSameInstance(ResolutionStrategies rs1, ResolutionStrategies rs2) {
		return rs1.slice() != null
				&& rs2.slice() != null
				&& StringUtils.isNotEmpty(rs1.slice().sourceFieldFullName())
				&& StringUtils.isNotEmpty(rs1.slice().targetFieldFullName())
				&& StringUtils.isNotEmpty(rs2.slice().sourceFieldFullName())
				&& StringUtils.isNotEmpty(rs2.slice().targetFieldFullName())
				&& rs2.slice().sourceFieldFullName().equals(rs1.slice().sourceFieldFullName())
				&& rs2.slice().targetFieldFullName().equals(rs1.slice().targetFieldFullName())
				&& rs2.targetGroupFullName().equals(rs1.targetGroupFullName());
	}

	private static boolean foldsResolveToSameInstance(ResolutionStrategies rs1, ResolutionStrategies rs2) {
		return rs1.sourceGroupFullName().equals(rs2.sourceGroupFullName())
				&& rs1.targetGroupFullName().equals(rs2.targetGroupFullName());
	}

	private void checkFieldMappingDoesNotTargetSliceTargetField(FieldMappings fieldMapping,
			PFieldMappings<MM_StructuralMappingModel_1> pointer, Collection<ResolutionStrategies> slicesForTargetField) {
		if (!slicesForTargetField.isEmpty()) {
			notifier.error(SMMNotificationSource.of(pointer, fieldMapping),
					NotificationCode.FIELD_MAPPING_TARGETS_SLICE_TARGET_FIELD,
					notifier.getString(fieldMapping));
		}
	}

	private void checkFieldMappingDoesNotHaveUnresolvableTargetRepetitionLayers(FieldMappings fieldMapping,
			PFieldMappings<MM_StructuralMappingModel_1> pointer, ResolutionStrategyApplicabilityContext applicabilityContext) {
		Java8Utils.forEachTogether(
				ElementPathUtils.getAncestorPaths(fieldMapping.targetFieldFullName()),
				applicabilityContext.applicableResolutionStrategiesByLevel(
						fieldMapping.sourceFieldFullName(), fieldMapping.targetFieldFullName()),
				(targetGroupFullName, resolutionStrategies) -> {
					if (resolutionStrategies.isEmpty()
							&& getTargetElementByFullNameFunc.apply(targetGroupFullName)
							.map(ElementUtils::convert)
							.filter(ElementUtils::isRepeatableByItself)
							.isPresent()) {
						notifier.error(SMMNotificationSource.of(pointer, fieldMapping),
								NotificationCode.FIELD_MAPPING_NO_APPLICABLE_RESOLUTION_STRATEGIES,
								targetGroupFullName,
								notifier.getString(fieldMapping));
					}
				});
	}

	private void checkFieldMappingDoesNotHaveUnusedSourceIRG(FieldMappings fieldMapping,
			PFieldMappings<MM_StructuralMappingModel_1> pointer, ResolutionStrategyApplicabilityContext applicabilityContext) {
		String sourceIRG = irg(fieldMapping.sourceFieldFullName(), getSourceElementByFullNameFunc);
		if (sourceIRG == null) {
			return; // non-repeatable source field
		}

		if (applicabilityContext.applicableResolutionStrategiesByLevel(
						fieldMapping.sourceFieldFullName(), fieldMapping.targetFieldFullName()).stream()
				.flatMap(List::stream)
				.noneMatch(rs -> ElementPathUtils.isDescendantOrEqual(rs.sourceGroupFullName(), sourceIRG))) {
			notifier.error(SMMNotificationSource.of(pointer, fieldMapping),
					NotificationCode.FIELD_MAPPING_UNUSED_SOURCE_IRG,
					notifier.getString(fieldMapping));
		}
	}

	private static String irg(String fieldFullName, Function<String, Optional<Element>> getElementByFullNameFunc) {
		List<String> ancestors = ElementPathUtils.getAncestorPaths(fieldFullName);
		for (int zeroBasedLevel = ancestors.size() - 1; zeroBasedLevel >= 0; zeroBasedLevel--) {
			String ancestor = ancestors.get(zeroBasedLevel);
			if (getElementByFullNameFunc.apply(ancestor)
					.map(ElementUtils::convert)
					.map(ElementUtils::isRepeatableByItself)
					.orElse(false)) {
				return ancestor;
			}
		}
		return null;
	}

	private void checkFieldMappingSourceFieldIsAssignableToTargetField(FieldMappings fieldMapping,
			PFieldMappings<MM_StructuralMappingModel_1> pointer) {
		checkFieldIsAssignable(
				fieldMapping.sourceFieldFullName(),
				fieldMapping.targetFieldFullName(),
				() -> SMMNotificationSource.of(pointer, fieldMapping),
				NotificationCode.FIELD_MAPPING_INCOMPATIBLE_TYPES);
	}

	private void checkResolutionStrategyDoesNotAlwaysApplyTogether(ResolutionStrategies rsLow,
			PResolutionStrategies<MM_StructuralMappingModel_1> pointer,
			ResolutionStrategyApplicabilityContext applicabilityContext) {
		applicabilityContext
				.resolutionStrategiesForTargetGroup(rsLow.targetGroupFullName()).stream()
				.filter(rsHigh -> !rsLow.sourceGroupFullName().equals(rsHigh.sourceGroupFullName()))
				.filter(rsHigh -> ElementPathUtils.isChild(rsLow.sourceGroupFullName(), rsHigh.sourceGroupFullName()))
				.forEach(rsHigh -> notifier.error(SMMNotificationSource.of(pointer, rsLow),
						NotificationCode.RESOLUTION_STRATEGY_ALWAYS_APPLIES_TOGETHER,
						notifier.getString(rsLow)));
	}

	private void checkResolutionStrategiesHaveApplicableFieldMappings(MappingBlocks mappingBlock,
			PMappingBlocks<MM_StructuralMappingModel_1> pointer, ResolutionStrategyApplicabilityContext applicabilityContext) {
		Set<Pair<String, String>> sourceAndTargetGroupsOfApplicableResolutionStrategies = mappingBlock.fieldMappings().stream()
				.filter(fm -> StringUtils.isNotEmpty(fm.sourceFieldFullName()) && StringUtils.isNotEmpty(fm.targetFieldFullName()))
				.flatMap(fm -> applicabilityContext.applicableResolutionStrategiesByLevel(
						fm.sourceFieldFullName(), fm.targetFieldFullName()).stream())
				.flatMap(List::stream)
				.map(rs -> Pair.of(rs.sourceGroupFullName(), rs.targetGroupFullName()))
				.collect(Collectors.toSet());
		Java8Utils.zipWithIndex(mappingBlock.resolutionStrategies().stream())
				.filter(indexAndRS -> {
					ResolutionStrategies rs = indexAndRS.getRight();
					return StringUtils.isNotEmpty(rs.sourceGroupFullName())
							&& StringUtils.isNotEmpty(rs.targetGroupFullName())
							&& !sourceAndTargetGroupsOfApplicableResolutionStrategies.contains(
							Pair.of(rs.sourceGroupFullName(), rs.targetGroupFullName()));
				})
				.forEach(indexAndRS -> {
					ResolutionStrategies rs = indexAndRS.getRight();
					notifier.warning(
							SMMNotificationSource.of(
									pointer.resolutionStrategies(indexAndRS.getLeft() + 1), rs),
							NotificationCode.RESOLUTION_STRATEGY_NO_APPLICABLE_FIELD_MAPPING,
							notifier.getString(rs));
				});
	}

	private void checkResolutionStrategyHasRepeatableTargetGroup(ResolutionStrategies resolutionStrategy,
			PResolutionStrategies<MM_StructuralMappingModel_1> pointer) {
		boolean isRepeatable = getTargetElementByFullNameFunc.apply(resolutionStrategy.targetGroupFullName())
				.map(ElementUtils::convert)
				.filter(ElementUtils::isGroup)
				.map(ElementUtils::isRepeatableByItself)
				.orElse(true);
		if (!isRepeatable) {
			notifier.error(SMMNotificationSource.of(pointer, resolutionStrategy),
					NotificationCode.RESOLUTION_STRATEGY_TARGETS_NON_REPEATABLE_GROUP,
					notifier.getString(resolutionStrategy));
		}
	}

	private void checkResolutionStrategyDoesNotCrossOtherResolutionStrategy(ResolutionStrategies resolutionStrategy,
			PResolutionStrategies<MM_StructuralMappingModel_1> pointer,
			ResolutionStrategyApplicabilityContext applicabilityContext) {
		crossingResolutionStrategies(resolutionStrategy, applicabilityContext).forEach(other ->
				notifier.error(SMMNotificationSource.of(pointer, resolutionStrategy), NotificationCode.RESOLUTION_STRATEGY_CROSS,
						notifier.getString(resolutionStrategy.type()),
						resolutionStrategy.sourceGroupFullName(),
						resolutionStrategy.targetGroupFullName(),
						notifier.getString(other.type()),
						other.sourceGroupFullName(),
						other.targetGroupFullName()));
	}

	/**
	 * @return the resolution strategies in {@code applicabilityContext} whose <ul>
	 * 		<li>source group is above {@code rs}'s source group, and</li>
	 * 		<li>target group is below {@code rs}'s target group.</li>
	 * 		</ul>
	 */
	public static List<ResolutionStrategies> crossingResolutionStrategies(ResolutionStrategies rs,
			ResolutionStrategyApplicabilityContext applicabilityContext) {
		return ElementPathUtils.getAncestorPaths(rs.sourceGroupFullName()).stream()
				.flatMap(sourceAncestor -> applicabilityContext.resolutionStrategiesForSourceGroup(sourceAncestor).stream())
				.filter(other -> ElementPathUtils.isDescendant(other.targetGroupFullName(), rs.targetGroupFullName())).toList();
	}

	private void checkSliceSourceFieldIsDescendantOfSourceGroup(ResolutionStrategies resolutionStrategy,
			PResolutionStrategies<MM_StructuralMappingModel_1> pointer) {
		if (StringUtils.isNotEmpty(resolutionStrategy.slice().sourceFieldFullName())
				&& !ElementPathUtils.isDescendant(
				resolutionStrategy.slice().sourceFieldFullName(),
				resolutionStrategy.sourceGroupFullName())) {
			notifier.error(SMMNotificationSource.of(pointer, resolutionStrategy),
					NotificationCode.SLICE_SOURCE_FIELD_NOT_DESCENDANT_OF_SOURCE_GROUP,
					notifier.getString(resolutionStrategy));
		}
	}

	private void checkSliceTargetFieldIsDescendantOfTargetGroup(ResolutionStrategies resolutionStrategy,
			PResolutionStrategies<MM_StructuralMappingModel_1> pointer) {
		if (StringUtils.isNotEmpty(resolutionStrategy.slice().targetFieldFullName())
				&& !ElementPathUtils.isDescendant(
				resolutionStrategy.slice().targetFieldFullName(),
				resolutionStrategy.targetGroupFullName())) {
			notifier.error(SMMNotificationSource.of(pointer, resolutionStrategy),
					NotificationCode.SLICE_TARGET_FIELD_NOT_DESCENDANT_OF_TARGET_GROUP,
					notifier.getString(resolutionStrategy));
		}
	}

	private void checkSliceSourceFieldIsNotRepeatablyReferenced(ResolutionStrategies rs,
			PResolutionStrategies<MM_StructuralMappingModel_1> pointer) {
		if (StringUtils.isNotEmpty(rs.slice().sourceFieldFullName())
				&& ElementPathUtils.isDescendant(rs.slice().sourceFieldFullName(), rs.sourceGroupFullName())
				&& isRepeatablyReferenced(rs.sourceGroupFullName(), rs.slice().sourceFieldFullName(),
				getSourceElementByFullNameFunc)) {
			notifier.error(SMMNotificationSource.of(pointer, rs),
					NotificationCode.SLICE_SOURCE_FIELD_REPEATABLY_REFERENCED,
					notifier.getString(rs));
		}
	}

	private void checkSliceTargetFieldIsNotRepeatablyReferenced(ResolutionStrategies rs,
			PResolutionStrategies<MM_StructuralMappingModel_1> pointer) {
		if (StringUtils.isNotEmpty(rs.slice().targetFieldFullName())
				&& ElementPathUtils.isDescendant(rs.slice().targetFieldFullName(), rs.targetGroupFullName())
				&& isRepeatablyReferenced(rs.targetGroupFullName(), rs.slice().targetFieldFullName(),
				getTargetElementByFullNameFunc)) {
			notifier.error(SMMNotificationSource.of(pointer, rs),
					NotificationCode.SLICE_TARGET_FIELD_REPEATABLY_REFERENCED,
					notifier.getString(rs));
		}
	}

	private static boolean isRepeatablyReferenced(String originPath, String targetPath,
			Function<String, Optional<Element>> getElementByFullNameFunc) {
		return ElementPathUtils.getPathsBetween(originPath, targetPath).stream()
				.flatMap(targetGroup -> getElementByFullNameFunc.apply(targetGroup).stream())
				.map(ElementUtils::convert)
				.anyMatch(ElementUtils::isRepeatableByItself);
	}

	private void checkSliceSourceFieldIsAssignableToSliceTargetField(ResolutionStrategies resolutionStrategy,
			PResolutionStrategies<MM_StructuralMappingModel_1> pointer) {
		if (StringUtils.isNotEmpty(resolutionStrategy.slice().sourceFieldFullName())
				&& StringUtils.isNotEmpty(resolutionStrategy.slice().targetFieldFullName())) {
			checkFieldIsAssignable(
					resolutionStrategy.slice().sourceFieldFullName(),
					resolutionStrategy.slice().targetFieldFullName(),
					() -> SMMNotificationSource.of(pointer, resolutionStrategy),
					NotificationCode.SLICE_INCOMPATIBLE_TYPES);
		}
	}

	private void checkFieldIsAssignable(
			@NonNull String sourceFieldFullName,
			@NonNull String targetFieldFullName,
			@NonNull Supplier<SMMNotificationSource<?>> sourceSupplier,
			@NonNull NotificationCode errorCode
	) {
		FieldType sourceFT = getFieldType(sourceFieldFullName, getSourceElementByFullNameFunc);
		FieldType targetFT = getFieldType(targetFieldFullName, getTargetElementByFullNameFunc);
		if (sourceFT == null || targetFT == null) {
			return;
		}
		IDocumentValidationResult result = SMMFieldTypeCheckUtils.checkFieldTypeAssignability(
				sourceFieldFullName, sourceFT, targetFieldFullName, targetFT, notifier.locale());
		result.getMessages().forEach(message -> notifier.forwardValidationMessage(message, errorCode, sourceSupplier));
	}

	private static FieldType getFieldType(String fullName, Function<String, Optional<Element>> getElementByFullNameFunc) {
		return getElementByFullNameFunc.apply(fullName)
				.filter(Field.class::isInstance)
				.map(Field.class::cast)
				.flatMap(Field::getEffectiveType)
				.orElse(null);
	}

}
