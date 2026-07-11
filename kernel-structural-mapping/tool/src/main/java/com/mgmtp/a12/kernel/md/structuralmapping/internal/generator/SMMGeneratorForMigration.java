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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelObjectReference;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelVisitorWithFullNames;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.SMMGeneratorConfig;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency.ResolutionStrategyApplicabilityContext;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.util.SMMSorter;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.util.StructuralMappingModelUtil;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content.MappingBlocks;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.FieldMappings;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks.ResolutionStrategies;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views._mm_structuralmappingmodel_1._content._mappingblocks._resolutionstrategies.Type;
import com.mgmtp.a12.model.header.Annotation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * The internal implementation used to generate the SMM for mappings between two very similar models, as is the case in the data
 * transfer from the previous year.
 * <p>
 * The implementation defines and uses the following terminology: <ul>
 * <li>"effective short name": when a field is an override, its actual short name includes the override suffix whereas the effective
 * short name does not include this suffix (e.g. "E1234567_nextYear" becomes "E1234567")</li>
 * <li>"effective full name": depending on the include level of the source in the mapping model, the actual full name may contain an
 * additional or incorrect root group, whereas the effective full name eliminates or modifies the name of this root group and also
 * uses the effective short name (e.g. the source field "/PreviousVersion/RootGroup/MyField" has the effective full name
 * "/RootGroup/MyField" so that it successfully matches with the target field "/RootGroup/MyField", since the source was included
 * with level "MODEL_ROOT".</li>
 * <li>"unique field": if a field's effective short name matches the pattern for uniqueness, it is considered a "unique field"</li>
 * <li>"non-unique field": inverse of "unique field"</li>
 * </ul>
 * <p>
 * relevant concept page: MGM Wiki Shortlink x/-FGjG
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SMMGeneratorForMigration {
	@NonNull private final Map<String, TransferField> effectiveFullNameToNonUniqueSourceTransferField;
	@NonNull private final Map<String, TransferField> effectiveFullNameToNonUniqueTargetTransferField;
	@NonNull private final Map<String, TransferField> effectiveShortNameToUniqueSourceTransferField;
	@NonNull private final Map<String, TransferField> effectiveShortNameToUniqueTargetTransferField;
	@NonNull private final Map<String, TransferField> actualFullNameToSourceTransferField;
	@NonNull private final Map<String, TransferField> actualFullNameToTargetTransferField;
	@NonNull private final SMMSorter sorter;
	@NonNull private final SMMGeneratorConfig config;

	/**
	 * @param actualToEffectiveRootGroupShortName
	 * 		A function that is used to determine the effective short name to use for the root group when matching source and target
	 * 		fields by their full name and generating resolution strategies. Return value may be {@code null} if the root group
	 * 		should be ignored.
	 * @param resolutionStrategyOverrides
	 * 		A list of resolution strategies to use in the generated SMM. When generating the resolution strategies for a field
	 * 		mapping, the generator will only generate a resolution strategy for those target repetition layers which do not already
	 * 		have an applicable resolution strategy (either from this list or a previous step). This means that the resolution
	 * 		strategies in this list will always take precedence.
	 */
	public static MM_StructuralMappingModel_1 generateStructuralMappingModel(
			@NonNull IDocumentModel sourceDM,
			@NonNull IDocumentModel targetDM,
			@NonNull UnaryOperator<String> actualToEffectiveRootGroupShortName,
			@NonNull List<ResolutionStrategies> resolutionStrategyOverrides,
			@NonNull SMMGeneratorConfig config
	) {
		TransferFieldCollector sourceCollector = TransferFieldCollector.collect(sourceDM, config,
				Optional.empty(), config.sourceFieldNameSuffixForOverride(), actualToEffectiveRootGroupShortName);
		TransferFieldCollector targetCollector = TransferFieldCollector.collect(targetDM, config,
				config.targetAnnotationForNoDataTransfer(), Optional.empty(), actualToEffectiveRootGroupShortName);
		SMMSorter sorter = SMMSorter.using(sourceCollector.fullNameToPreorderRank(), targetCollector.fullNameToPreorderRank());
		return new SMMGeneratorForMigration(
				sourceCollector.effectiveFullNameToNonUniqueTransferField(),
				targetCollector.effectiveFullNameToNonUniqueTransferField(),
				sourceCollector.effectiveShortNameToUniqueTransferField(),
				targetCollector.effectiveShortNameToUniqueTransferField(),
				sourceCollector.actualFullNameToTransferField(),
				targetCollector.actualFullNameToTransferField(),
				sorter,
				config
		).generate(resolutionStrategyOverrides);
	}

	@RequiredArgsConstructor @Accessors(fluent = true)
	private static class TransferFieldCollector extends DocumentModelVisitorWithFullNames {
		@NonNull private final SMMGeneratorConfig config;
		@NonNull private final Optional<String> annotationForNoDataTransfer;
		@NonNull private final Optional<String> fieldNameSuffixForOverride;
		@NonNull private final UnaryOperator<String> actualToEffectiveRootGroupShortName;

		@Getter private final Map<String, TransferField> effectiveShortNameToUniqueTransferField = new HashMap<>();
		@Getter private final Map<String, TransferField> effectiveFullNameToNonUniqueTransferField = new HashMap<>();
		@Getter private final Map<String, TransferField> actualFullNameToTransferField = new HashMap<>();
		@Getter private final Map<String, Integer> fullNameToPreorderRank = new HashMap<>();

		private final List<IGroup> ancestors = new LinkedList<>();
		private int preorderRank = 0;
		private int exclusionDepth = 0;

		public static TransferFieldCollector collect(IDocumentModel dm, SMMGeneratorConfig config,
				Optional<String> annotationForNoDataTransfer, Optional<String> fieldNameSuffixForOverride,
				UnaryOperator<String> actualToEffectiveRootGroupShortName) {
			TransferFieldCollector collector = new TransferFieldCollector(config, annotationForNoDataTransfer,
					fieldNameSuffixForOverride, actualToEffectiveRootGroupShortName);
			new DocumentModelWalker().acceptDocumentModel(dm, collector);
			return collector;
		}

		@Override public void enterElement(IElement element) {
			super.enterElement(element);
			fullNameToPreorderRank.put(getFullName(), preorderRank++);
			if (annotationForNoDataTransfer.map(annotation -> hasAnnotation(element, annotation)).orElse(false)) {
				exclusionDepth++;
			}
		}

		@Override public void leaveElement(IElement element) {
			if (annotationForNoDataTransfer.map(annotation -> hasAnnotation(element, annotation)).orElse(false)) {
				exclusionDepth--;
			}
			super.leaveElement(element);
		}

		@Override public void enterField(IField field) {
			String actualShortName = field.getName();
			boolean isOverride = fieldNameSuffixForOverride.map(actualShortName::endsWith).orElse(false);
			String effectiveShortName = isOverride
					? actualShortName.substring(0, actualShortName.length() - fieldNameSuffixForOverride.orElseThrow().length())
					: actualShortName;
			IGroup rootGroup = ancestors.getFirst();
			String effectiveShortNameForRootGroup = actualToEffectiveRootGroupShortName.apply(rootGroup.getName());
			List<PathPart> effectiveAncestors = Stream.concat(
					Optional.ofNullable(effectiveShortNameForRootGroup)
							.filter(StringUtils::isNotEmpty).stream()
							.map(name -> PathPart.of(rootGroup, name)),
					ancestors.stream().skip(1).map(PathPart::of)
			).toList();
			boolean hasUniqueShortName = config.patternForUniqueShortNames()
					.map(pattern -> pattern.matcher(effectiveShortName).matches())
					.orElse(false);
			String effectiveFullName = ElementUtils.createPath(
					Stream.concat(effectiveAncestors.stream().map(PathPart::name), Stream.of(effectiveShortName)).toList());
			TransferField transferField = new TransferField(field, getFullName(), isOverride, exclusionDepth > 0,
					hasUniqueShortName, effectiveAncestors, effectiveShortName, effectiveFullName);
			actualFullNameToTransferField.put(transferField.actualFullName(), transferField);
			if (hasUniqueShortName) {
				effectiveShortNameToUniqueTransferField.compute(effectiveShortName,
						(key, oldValue) -> resolveOverride(oldValue, transferField));
			} else {
				effectiveFullNameToNonUniqueTransferField.compute(effectiveFullName,
						(key, oldValue) -> resolveOverride(oldValue, transferField));
			}
		}

		private TransferField resolveOverride(TransferField oldValue, TransferField newValue) {
			// Note: isExcluded() is not considered here - overrides are meant to be evaluated irrespective
			// of overrides, and exclusions only matter for the field that prevails after override resolution.
			// (Modelers should know this in order to use overrides and exclusions correctly)
			if (oldValue == null) {
				return newValue;
			} else if (!oldValue.isOverride() && newValue.isOverride()) {
				oldValue.overrideField(newValue);
				newValue.baseField(oldValue);
				return newValue;
			} else {
				// first override field encountered wins
				// first base field encountered wins
				if (oldValue.isOverride() && !newValue.isOverride()) {
					if (oldValue.baseField() == null) {
						oldValue.baseField(newValue);
					}
					newValue.overrideField(oldValue);
				}
				return oldValue;
			}
		}

		@Override public void enterGroup(IGroup group) {
			ancestors.addLast(group);
			fullNameToPreorderRank.put(getFullName(), preorderRank++);
		}

		@Override public void leaveGroup(IGroup group) {
			ancestors.removeLast();
		}

		private static boolean hasAnnotation(IElement element, String annotationKey) {
			return element.getAnnotations().stream().map(Annotation::getName).anyMatch(annotationKey::equals);
		}
	}

	@Accessors(fluent = true) @Getter
	@RequiredArgsConstructor
	private static class TransferField {
		@NonNull private final IField field;
		@NonNull private final String actualFullName;

		private final boolean isOverride;
		private final boolean isExcluded;
		private final boolean hasUniqueShortName;

		@NonNull private final List<PathPart> effectiveAncestors;
		@NonNull private final String effectiveShortName;
		@NonNull private final String effectiveFullName;

		/**
		 * the field that is overridden by this field for the purpose of SMM generation, if this field is an override field.
		 */
		@Setter private TransferField baseField;
		/**
		 * the field that overrides this field for the purpose of SMM generation, if any.
		 */
		@Setter private TransferField overrideField;

		public List<IGroup> repetitionLayers() {
			return effectiveAncestors.stream()
					.map(PathPart::group)
					.filter(group -> group.getRepeatability() > 1)
					.toList();
		}

		@Override public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(getClass().getSimpleName());
			sb.append("(");
			sb.append("actualFullName='").append(actualFullName).append('\'');
			sb.append(", effectiveFullName='").append(effectiveFullName).append('\'');
			sb.append(")");
			return sb.toString();
		}
	}

	public record PathPart(
			IGroup group,
			String name
	) {
		public static PathPart of(IGroup group) {
			return of(group, group.getName());
		}

		public static PathPart of(IGroup group, String name) {
			return new PathPart(group, name);
		}
	}

	private MM_StructuralMappingModel_1 generate(@NonNull List<ResolutionStrategies> resolutionStrategyOverrides) {
		Collection<PotentialFieldMapping> potentialFieldMappings = findPotentialFieldMappings();
		Collection<ResolutionStrategies> resolutionStrategies = generateResolutionStrategies(potentialFieldMappings,
				resolutionStrategyOverrides);

		Set<String> sliceTargetFields = sliceTargetFields(resolutionStrategies);
		potentialFieldMappings = potentialFieldMappings.stream()
				.filter(fm -> !sliceTargetFields.contains(fm.targetField().actualFullName()))
				.toList();

		MappingBlocks mappingBlock = StructuralMappingModelUtil.createMappingBlock(List.copyOf(resolutionStrategies),
				potentialFieldMappings.stream().map(PotentialFieldMapping::createFieldMapping).toList());
		MM_StructuralMappingModel_1 smm = StructuralMappingModelUtil.createSMM(
				config.idForGeneratedSMM(), List.of(), List.of(mappingBlock));
		return sorter.sortedSMM(smm);
	}

	private static Set<String> sliceTargetFields(Collection<ResolutionStrategies> resolutionStrategies) {
		return resolutionStrategies.stream()
				.filter(rs -> Type.Slice == rs.type())
				.map(slice -> slice.slice().targetFieldFullName())
				.collect(Collectors.toSet());
	}

	private Collection<ResolutionStrategies> generateResolutionStrategies(
			Collection<PotentialFieldMapping> fieldMappings,
			List<ResolutionStrategies> resolutionStrategyOverrides
	) {
		List<PotentialFieldMapping> sortedFieldMappings = fieldMappings.stream()
				.sorted(Comparator // NOTE: The order does not make a functional difference. However, it ensures that our tests for
						// generateResolutionStrategiesForLowestLevel can affect the order in which the field mappings are processed.
						.comparing((PotentialFieldMapping fm) -> sorter.getTargetRank(fm.targetField().actualFullName()))
						.thenComparing(fm -> sorter.getSourceRank(fm.sourceField().actualFullName())))
				.toList();
		var applicabilityContext = ResolutionStrategyApplicabilityContext.create(resolutionStrategyOverrides);
		generateResolutionStrategiesFromCondensedPaths(sortedFieldMappings, applicabilityContext);
		generateResolutionStrategiesForLowestLevel(sortedFieldMappings, applicabilityContext);
		return applicabilityContext.resolutionStrategies();
	}

	private void generateResolutionStrategiesFromCondensedPaths(
			Collection<PotentialFieldMapping> fieldMappings,
			ResolutionStrategyApplicabilityContext applicabilityContext // is modified!
	) {
		for (PotentialFieldMapping fm : fieldMappings) {
			List<PathPart> condensedSourceAncestors = condensedAncestors(
					fm.sourceField().effectiveAncestors(), fm.targetField().effectiveAncestors());
			List<PathPart> condensedTargetAncestors = condensedAncestors(
					fm.targetField().effectiveAncestors(), fm.sourceField().effectiveAncestors());
			if (pathsMatch(condensedSourceAncestors, condensedTargetAncestors)) {
				for (int i = 0; i < condensedTargetAncestors.size(); i++) {
					IGroup sourceGroup = condensedSourceAncestors.get(i).group();
					IGroup targetGroup = condensedTargetAncestors.get(i).group();
					if (targetGroup.getRepeatability() == 1) {
						continue;
					}
					var applicableResolutionStrategies = applicabilityContext.applicableResolutionStrategiesForTargetGroup(
							fm.sourceField().actualFullName(), ElementUtils.getPath(targetGroup));
					if (!applicableResolutionStrategies.isEmpty()) {
						continue;
					}
					ResolutionStrategies rs = generateResolutionStrategyOrNull(sourceGroup, targetGroup);
					if (rs != null) {
						applicabilityContext.addResolutionStrategy(rs);
					}
				}
			}
		}
	}

	/**
	 * For each field mapping, generate a resolution strategy (and add it to the provided {@code applicabilityContext}) between the
	 * lowest repetition layer of the source and target field if: <ul>
	 * <li>the source and target field have the same number of repetition layers</li>
	 * <li>the repeatability of the lowest repetition layer did not decrease</li>
	 * <li>the second lowest repetition layer (if it exists) has a resolution strategy</li>
	 * </ul>
	 */
	private void generateResolutionStrategiesForLowestLevel(
			Collection<PotentialFieldMapping> fieldMappings,
			ResolutionStrategyApplicabilityContext applicabilityContext // is modified!
	) {
		Collection<PotentialFieldMapping> currentFieldMappings = fieldMappings;
		while (true) {
			Collection<PotentialFieldMapping> deferredFieldMappings = new ArrayList<>();
			for (PotentialFieldMapping fm : currentFieldMappings) {
				List<IGroup> sourceRepetitionLayers = fm.sourceField().repetitionLayers();
				List<IGroup> targetRepetitionLayers = fm.targetField().repetitionLayers();
				if (sourceRepetitionLayers.size() != targetRepetitionLayers.size() || sourceRepetitionLayers.isEmpty()) {
					continue;
				}
				IGroup lowestSRL = sourceRepetitionLayers.getLast();
				IGroup lowestTRL = targetRepetitionLayers.getLast();
				if (lowestSRL.getRepeatability() > lowestTRL.getRepeatability()) {
					continue;
				}
				var applicableResolutionStrategiesForLowestTRL = applicabilityContext.applicableResolutionStrategiesForTargetGroup(
						fm.sourceField().actualFullName(), ElementUtils.getPath(lowestTRL));
				if (!applicableResolutionStrategiesForLowestTRL.isEmpty()) {
					continue;
				}
				if (sourceRepetitionLayers.size() >= 2) {
					String secondLowestTRLFullName = ElementUtils.getPath(targetRepetitionLayers.reversed().get(1));
					var applicableResolutionStrategiesForSecondLowestTRL = applicabilityContext.applicableResolutionStrategiesForTargetGroup(
							fm.sourceField().actualFullName(), secondLowestTRLFullName);
					if (applicableResolutionStrategiesForSecondLowestTRL.isEmpty()) {
						// in this case, another subsequent field mapping might generate the necessary resolution strategy, so we
						// need to retry this field mapping again later
						deferredFieldMappings.add(fm);
						continue;
					}
				}
				ResolutionStrategies resolutionStrategy = generateResolutionStrategyOrNull(lowestSRL, lowestTRL);
				if (resolutionStrategy != null) {
					applicabilityContext.addResolutionStrategy(resolutionStrategy);
				}
			}
			if (deferredFieldMappings.isEmpty() || deferredFieldMappings.size() == currentFieldMappings.size()) {
				// no deferred field mappings or all of the field mappings were deferred
				// => all possible resolution strategies were found
				return;
			}
			currentFieldMappings = deferredFieldMappings;
		}
	}

	private ResolutionStrategies generateResolutionStrategyOrNull(IGroup sourceGroup, IGroup targetGroup) {
		TransferField sliceSourceField = getIndexFieldFullName(sourceGroup)
				.map(actualFullNameToSourceTransferField::get)
				.map(field -> field.overrideField() != null ? field.overrideField() : field)
				.filter(field -> !field.isExcluded())
				.orElse(null);
		TransferField sliceTargetField = getIndexFieldFullName(targetGroup)
				.map(actualFullNameToTargetTransferField::get)
				.map(field -> field.overrideField() != null ? field.overrideField() : field)
				.filter(field -> !field.isExcluded())
				.orElse(null);
		if (sliceSourceField != null && sliceTargetField != null) {
			return StructuralMappingModelUtil.createSlice(
					ElementUtils.getPath(sourceGroup), ElementUtils.getPath(targetGroup),
					sliceSourceField.actualFullName(), sliceTargetField.actualFullName());
		} else if (sliceSourceField == null && sliceTargetField != null) {
			// target group has index field, but source group does not --> input from modeller required
			return null;
		}
		return StructuralMappingModelUtil.createFold(ElementUtils.getPath(sourceGroup), ElementUtils.getPath(targetGroup));
	}

	@RequiredArgsConstructor
	@Accessors(fluent = true) @Getter
	private static class PotentialFieldMapping {
		@NonNull private final TransferField sourceField;
		@NonNull private final TransferField targetField;

		public FieldMappings createFieldMapping() {
			return StructuralMappingModelUtil.createFieldMapping(sourceField.actualFullName(), targetField.actualFullName());
		}
	}

	private Collection<PotentialFieldMapping> findPotentialFieldMappings() {
		List<PotentialFieldMapping> fieldMappingsFromFullNames = findPotentialFieldMappings(
				effectiveFullNameToNonUniqueSourceTransferField, effectiveFullNameToNonUniqueTargetTransferField);
		if (config.patternForUniqueShortNames().isEmpty()) {
			return fieldMappingsFromFullNames;
		}
		List<PotentialFieldMapping> fieldMappingsFromShortNames = findPotentialFieldMappings(
				effectiveShortNameToUniqueSourceTransferField, effectiveShortNameToUniqueTargetTransferField);
		return Stream.concat(fieldMappingsFromShortNames.stream(), fieldMappingsFromFullNames.stream()).toList();
	}

	private List<PotentialFieldMapping> findPotentialFieldMappings(
			Map<String, TransferField> potentialSourceTransferFieldsByShortOrFullName,
			Map<String, TransferField> potentialTargetTransferFieldsByShortOrFullName
	) {
		List<PotentialFieldMapping> potentialFieldMappings = new ArrayList<>();
		potentialSourceTransferFieldsByShortOrFullName.forEach((effectiveSourceFieldShortOrFullName, sourceField) -> {
			TransferField targetField = potentialTargetTransferFieldsByShortOrFullName.get(effectiveSourceFieldShortOrFullName);
			if (targetField != null && !sourceField.isExcluded() && !targetField.isExcluded()) {
				potentialFieldMappings.add(
						new PotentialFieldMapping(sourceField, targetField));
			}
		});
		return potentialFieldMappings;
	}

	/**
	 * Create a stripped path which only contains those path parts which:
	 * - have a repeatability > 1, or
	 * - have a short name that can also be found in the reference path
	 */
	public static List<PathPart> condensedAncestors(List<PathPart> pathToStrip, List<PathPart> referencePath) {
		Set<String> shortNamesInReferencePath = referencePath.stream().map(PathPart::name).collect(Collectors.toSet());
		return pathToStrip.stream()
				.filter(ancestor -> ancestor.group().getRepeatability() > 1
						|| shortNamesInReferencePath.contains(ancestor.name()))
				.toList();
	}

	private static boolean pathsMatch(List<PathPart> sourcePath, List<PathPart> targetPath) {
		if (sourcePath.size() != targetPath.size()) {
			return false;
		}
		for (int i = 0; i < sourcePath.size(); i++) {
			PathPart sourcePathPart = sourcePath.get(i);
			PathPart targetPathPart = targetPath.get(i);
			if (sourcePathPart.group().getRepeatability() > targetPathPart.group().getRepeatability()) {
				return false;
			}
			if (!Objects.equals(sourcePathPart.name(), targetPathPart.name())) {
				return false;
			}
		}
		return true;
	}

	private static Optional<String> getIndexFieldFullName(IGroup group) {
		return group.getIndexField()
				.map(IDocumentModelObjectReference::getRelativePath)
				.map(relativePath -> ElementUtils.resolveRelativePath(group, relativePath));
	}
}
