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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.execution;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document.Document;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document.EntityInstance;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document.FieldInstance;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.model.StructuralMappingModelForCodegen;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true) @Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StructuralMapperVK {
	@NonNull private final StructuralMappingModelForCodegen structuralMappingModel;
	@NonNull private final List<MappingBlockExecutor> mappingBlockExecutors;

	// called from generated code
	public static StructuralMapperVK fromModel(@NonNull StructuralMappingModelForCodegen structuralMappingModel) {
		return new StructuralMapperVK(
				structuralMappingModel,
				structuralMappingModel.mappingBlocks().stream().map(MappingBlockExecutor::fromMappingBlock).toList());
	}

	/**
	 * Executes the structural mapping according to {@link #structuralMappingModel()}.
	 *
	 * @param sourceData
	 * 		the source data. Not modified in any way.
	 * @param targetData
	 * 		the target data. Not modified in any way.
	 * @param identifierCreationFunc
	 * 		a function used to create the {@link IIdentifier}s in the returned {@link StructuralMappingResultVK}
	 * @return the result of the structural mapping
	 */
	public StructuralMappingResultVK map(@NonNull IData sourceData, @NonNull IData targetData,
			@NonNull BiFunction<String, long[], IIdentifier> identifierCreationFunc) {
		Document sourceDocument = convertData(sourceData, structuralMappingModel.sourceElementsByFullName());
		Document targetDocument = convertData(targetData, structuralMappingModel.targetElementsByFullName());
		StructuralMappingState structuralMappingState = mapInternal(sourceDocument, targetDocument);
		return createResult(structuralMappingState, identifierCreationFunc);
	}

	private StructuralMappingState mapInternal(Document sourceDocument, Document targetDocument) {
		StructuralMappingState structuralMappingState = new StructuralMappingState();
		for (MappingBlockExecutor mappingBlockExecutor : mappingBlockExecutors) {
			mappingBlockExecutor.execute(sourceDocument, targetDocument, structuralMappingState);
		}
		return structuralMappingState;
	}

	private static StructuralMappingResultVK createResult(StructuralMappingState state,
			BiFunction<String, long[], IIdentifier> identifierCreationFunc) {
		var mappedFieldInstances = state.targetFieldInstanceToSourceFieldInstance().entrySet().stream()
				.map(entry -> createMappedFieldInstance(entry.getValue(), entry.getKey(), identifierCreationFunc))
				.toList();
		var collisions = state.targetFieldInstanceToCollidingSourceFieldInstances().entrySet().stream()
				.map(entry -> new StructuralMappingCollisionVK(
						createMappedFieldInstance(
								state.targetFieldInstanceToSourceFieldInstance().get(entry.getKey()),
								entry.getKey(),
								identifierCreationFunc),
						entry.getValue().stream()
								.map(sourceFieldInstance ->
										createMappedFieldInstance(sourceFieldInstance, entry.getKey(), identifierCreationFunc))
								.toList()))
				.toList();
		var clearedGroupInstances = state.clearedGroupInstances().stream()
				.map(clearedGroupInstance -> convertToIIdentifier(clearedGroupInstance, identifierCreationFunc))
				.toList();
		return new StructuralMappingResultVK(mappedFieldInstances, collisions, clearedGroupInstances);
	}

	private static StructurallyMappedFieldInstanceVK createMappedFieldInstance(FieldInstance sourceFieldInstance,
			FieldInstance targetFieldInstance, BiFunction<String, long[], IIdentifier> identifierCreationFunc) {
		return new StructurallyMappedFieldInstanceVK(
				convertToIIdentifier(sourceFieldInstance, identifierCreationFunc),
				convertToIIdentifier(targetFieldInstance, identifierCreationFunc),
				(String) sourceFieldInstance.value());
	}

	private Document convertData(IData data, Map<String, Entity> entitiesByFullName) {
		Document document = new Document(entitiesByFullName);
		for (IIdentifier id : data) {
			Entity entity = lowestReferencedAncestor(id.getName(), entitiesByFullName);
			if (entity != null) { // if the entity is not referenced in the mapping, we can safely filter it out, but referenced ancestors must be kept
				String value = data.getValue(id);
				if (value != null && !value.isEmpty()) {
					if (entity.isField()) {
						document.getOrCreateFieldInstance(entity, id.getIndices(), value);
					} else {
						document.getOrCreateGroupInstance(entity, id.getIndices());
					}
				}
			}
		}
		return document;
	}

	private static IIdentifier convertToIIdentifier(EntityInstance entityInstance,
			BiFunction<String, long[], IIdentifier> identifierCreationFunc) {
		return identifierCreationFunc.apply(entityInstance.fullName(), entityInstance.repetitions());
	}

	private Entity lowestReferencedAncestor(String fullName, Map<String, Entity> referencedEntitiesByFullName) {
		String ancestorFullName = fullName;
		Entity ancestor = referencedEntitiesByFullName.get(fullName);
		while (ancestor == null && !ancestorFullName.isEmpty()) {
			int lastSeparatorIndex = ancestorFullName.lastIndexOf(IIdentifier.SEPARATOR);
			ancestorFullName = ancestorFullName.substring(0, lastSeparatorIndex);
			ancestor = referencedEntitiesByFullName.get(ancestorFullName);
		}
		return ancestor;
	}
}
