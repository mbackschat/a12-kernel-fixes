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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.dependency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IndexFieldCache;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtExternalIdentifierWithSemanticIndex;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RuntimeUtils;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.dependency.IDependencyService;
import com.mgmtp.a12.kernel.core.rt.a12internal.dependency.IHiddenEntityGroup;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifierWithSemanticIndex;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ResultBaseImpl;

import lombok.NonNull;

/**
 * The {@link IDependencyService}, backed by a deserialized dependency model
 * ({@link DependencyModelAccessor}) and the root model's structural view ({@link IMetaDataValidierung}).
 * Generated code without a dependency model uses the no-op {@code DefaultDependencyService} instead.
 */
public final class DependencyServiceImpl implements IDependencyService {

	/**
	 * The language used to look up the alternative error message. The post-processing signature carries no
	 * {@code ValidationMode}, so the default validation language ({@link ValidationMode#getLanguage()} is
	 * {@link Locale#GERMANY}) is used - consistent with the Stage-1 German-only scope.
	 */
	private static final String VALIDATION_LANGUAGE = Locale.GERMANY.getLanguage();

	private final DependencyModelAccessor modelAccessor;
	private final IMetaDataValidierung metaData;

	/** Semantic-index dependent path -> the index field (and its layer) that resolves it. Immutable. */
	private final Map<String, String> indexFieldByDependent;
	/** Full names of the index fields to feed into the {@link IndexFieldCache} while scanning the data. Immutable. */
	private final Set<String> indexFieldNames;

	DependencyServiceImpl(@NonNull DependencyModelAccessor modelAccessor, @NonNull IMetaDataValidierung metaData) {
		if (metaData.hasUniqueEntities()) {
			throw new IllegalStateException(
					"Dependency modelling is not supported for H-DVs, only for A12-DVs");
		}
		this.modelAccessor = modelAccessor;
		this.metaData = metaData;
		this.indexFieldByDependent = computeIndexTargets();
		this.indexFieldNames = Set.copyOf(indexFieldByDependent.values());
	}

	@Override
	public Set<IIdentifier> getInputFields() {
		return Stream.concat(modelAccessor.triggerFieldNames().stream(), indexFieldNames.stream())
				.map(this::observationIdentifier)
				.collect(Collectors.toSet());
	}

	@Override
	public List<IHiddenEntityGroup> getHiddenEntities(IData inputFieldValues) {
		Map<String, List<IIdentifier>> firedTriggers = new HashMap<>();
		IndexFieldCache indexFieldCache = new IndexFieldCache(metaData);
		scanData(inputFieldValues, firedTriggers, indexFieldCache);

		return modelAccessor.triggerFieldNames().stream()
				.<IHiddenEntityGroup>mapMulti((triggerFieldName, result) -> {
					List<IIdentifier> firedInstances = firedTriggers.get(triggerFieldName);
					List<DependentEntity> dependents = modelAccessor.dependentEntitiesOf(triggerFieldName);
					if (firedInstances != null && !dependents.isEmpty()) {
						for (IIdentifier triggerInstance : firedInstances) {
							// A Set collapses identical ids; overlapping (mutually covering) ids are intentionally kept.
							Set<IIdentifier> triggerHidden = dependents.stream().map(dependent ->
											hiddenIdentifier(dependent, triggerFieldName, triggerInstance.getIndices(), indexFieldCache))
									.collect(Collectors.toSet());
							result.accept(IHiddenEntityGroup.of(triggerInstance, triggerHidden));
						}
					}
				}).toList();
	}

	@Override
	public IResult wrapForDependencyPostprocessing(IResult original, Set<IIdentifier> hiddenEntities) {
		return new DependencyPostprocessingResult(original, hiddenEntities, modelAccessor, VALIDATION_LANGUAGE, metaData);
	}

	private Map<String, String> computeIndexTargets() {
		return modelAccessor.semanticIndexDependentPaths().stream().collect(Collectors.toMap(
				dependentPath -> dependentPath,
				dependentPath -> {
					List<IMetaField> indexFields = semanticIndexFields(dependentPath);
					if (indexFields.size() != 1) {
						throw new IllegalStateException("Semantic-index dependent " + dependentPath
								+ " needs exactly one index field on its path, found " + indexFields.size());
					}
					return fullName(indexFields.getFirst());
				}));
	}

	/** The identifier the caller observes: {@code 0} on every repeatable layer, {@code 1} otherwise. */
	private IIdentifier observationIdentifier(String entityFullName) {
		List<Long> maxIndices = maxIndicesOf(entityFullName);
		long[] indices = new long[maxIndices.size()];
		for (int layer = 0; layer < indices.length; layer++) {
			indices[layer] = isRepeatable(maxIndices.get(layer)) ? IIdentifier.ALL_INDICES : 1L;
		}
		return new RtExternalIdentifier(entityFullName, indices);
	}

	/**
	 * Single pass over the data: buckets the given (non-empty) trigger instances by field name, and feeds the
	 * relevant index-field values into {@code indexFieldCache} so a semantic index resolves by lookup rather
	 * than by re-scanning the group's repetitions (and with the same value normalization as validation).
	 */
	private void scanData(IData data, Map<String, List<IIdentifier>> firedTriggers, IndexFieldCache indexFieldCache) {
		Set<String> triggerFieldNames = modelAccessor.triggerFieldNames();
		for (IIdentifier candidate : data) {
			String name = candidate.getName();
			if (triggerFieldNames.contains(name) && isGiven(data.getValue(candidate))) {
				firedTriggers.computeIfAbsent(name, key -> new ArrayList<>()).add(candidate);
			}
			if (indexFieldNames.contains(name)) {
				RtInternalIdentifier indexFieldId = new RtInternalIdentifier(metaData.getUniqueName(name), candidate.getIndices());
				indexFieldCache.setIndexFieldValue(indexFieldId, data.getValue(candidate), null);
			}
		}
	}

	private IIdentifier hiddenIdentifier(DependentEntity dependent, String triggerFieldName, long[] triggerIndices,
			IndexFieldCache indexFieldCache) {
		String path = dependent.entityPath();
		// Copy the trigger's indices only for the shared prefix (the common ancestor of trigger and dependent);
		// the dependent's own layers below it default to 0/1 (see layerIndices).
		int copiedLayerCount = EntityUtil.commonAncestorLevel(triggerFieldName, path) + 1;
		long[] indices = layerIndices(maxIndicesOf(path), triggerIndices, copiedLayerCount);
		if (!dependent.hasSemanticIndex()) {
			return new RtExternalIdentifier(path, indices);
		}
		return resolveSemanticIndex(dependent, indices, triggerFieldName, triggerIndices, indexFieldCache);
	}

	/** Copied trigger indices for the shared prefix, then {@code 0} for repeatable layers and {@code 1} otherwise. */
	private static long[] layerIndices(List<Long> maxIndices, long[] triggerFieldIndices, int copiedTriggerLevels) {
		long[] indices = new long[maxIndices.size()];
		System.arraycopy(triggerFieldIndices, 0, indices, 0, copiedTriggerLevels);
		for (int layer = copiedTriggerLevels; layer < indices.length; layer++) {
			indices[layer] = isRepeatable(maxIndices.get(layer)) ? IIdentifier.ALL_INDICES : 1L;
		}
		return indices;
	}

	/**
	 * Resolves the dependent's semantic index via the index built during the data scan: the repetition whose
	 * index field equals the declared value becomes a concrete index; if none matches the layer becomes
	 * {@link IIdentifier#UNKNOWN} and the result carries the index value via {@link IIdentifierWithSemanticIndex}.
	 */
	private IIdentifier resolveSemanticIndex(DependentEntity dependent, long[] indices, String triggerFieldName,
			long[] triggerFieldIndices, IndexFieldCache indexFieldCache) {
		String dependentPath = dependent.entityPath();
		String indexFieldName = indexFieldByDependent.get(dependentPath);
		String indexGroup = EntityUtil.getParentPath(indexFieldName);
		int indexGroupLayer = EntityUtil.getIndexOfHierarchyLevel(indexGroup);
		String wantedValue = dependent.semanticIndexValue();

		// The index field's indices: trigger-copied shared prefix, 1s in between (B7 forbids a repeatable group
		// between the common ancestor and the index group), then the index-group layer resolved by value.
		long[] indexFieldIndices = indexFieldIndices(indexFieldName, triggerFieldIndices,
				EntityUtil.commonAncestorLevel(triggerFieldName, indexFieldName) + 1);
		long resolved = indexFieldCache.lookup(indexGroup,
				RuntimeUtils.createListForSubArray(indexFieldIndices, 0, indexGroupLayer), wantedValue);
		indices[indexGroupLayer] = resolved;
		indexFieldIndices[indexGroupLayer] = resolved;
		IIdentifier indexFieldId = new RtExternalIdentifier(indexFieldName, indexFieldIndices);
		IMetaField metaField = metaData.getMetaField(indexFieldId.getName());
		IResult result = new ResultBaseImpl();
		Object indexValueAsBasicType = metaField.convertWithOnlyBasicTypeCheck(wantedValue, null, result);
		return new RtExternalIdentifierWithSemanticIndex(dependentPath, indices, IIdentifier.USB_NOT_SPECIFIED, indexFieldId,
				wantedValue, indexValueAsBasicType);
	}

	/**
	 * The index field's indices: the shared prefix (its common ancestor with the trigger) copied from the
	 * trigger, everything below filled with {@code 1} (B7 guarantees no repeatable group in between).
	 */
	private static long[] indexFieldIndices(String indexFieldName, long[] triggerFieldIndices, int copiedLevels) {
		int indexFieldDepth = EntityUtil.countHierarchyLevels(indexFieldName);
		int copied = Math.min(copiedLevels, Math.min(indexFieldDepth - 1, triggerFieldIndices.length));
		long[] indices = new long[indexFieldDepth];
		System.arraycopy(triggerFieldIndices, 0, indices, 0, copied);
		Arrays.fill(indices, copied, indices.length, 1L);
		return indices;
	}

	/** The index fields of the nearest group on the path (the entity's own group or an ancestor) that has any; empty if none. */
	private List<IMetaField> semanticIndexFields(String dependentPath) {
		String group = metaData.isGroupDefined(dependentPath) ? dependentPath : EntityUtil.getParentPath(dependentPath);
		while (group != null && !group.isEmpty()) {
			if (metaData.isGroupDefined(group)) {
				List<IMetaField> indexFields = metaData.getIndexFields(group);
				if (!indexFields.isEmpty()) {
					return indexFields;
				}
			}
			group = EntityUtil.getParentPath(group);
		}
		return List.of();
	}

	private List<Long> maxIndicesOf(String entityFullName) {
		if (metaData.isGroupDefined(entityFullName)) {
			return metaData.getMaxGroupIndices(entityFullName);
		}
		return metaData.getMaxFieldIndices(metaData.getMetaField(entityFullName));
	}

	private static String fullName(IMetaField field) {
		return (String) field.getValue(IMetaKeys.FIELD_FULLNAME);
	}

	private static boolean isRepeatable(long maxIndex) {
		// != 1 (not > 1), mirroring getInnermostRepeatableGroup: a "0"/unbounded max also counts as repeatable.
		return maxIndex != 1L;
	}

	private static boolean isGiven(String value) {
		return value != null && !value.isEmpty();
	}
}
