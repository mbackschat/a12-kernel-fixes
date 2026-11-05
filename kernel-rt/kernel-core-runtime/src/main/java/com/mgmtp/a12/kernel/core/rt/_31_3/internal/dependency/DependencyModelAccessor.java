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

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views.MM_DependencyModel_1;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1.Content;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content.AlternativeErrorMessages;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content.TriggerFields;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content._alternativeerrormessages.ErrorMessages;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content._triggerfields.DependentEntities;

import lombok.NonNull;

/**
 * Indexed read access over a deserialized dependency model (the JSON {@code MM_DependencyModel_1}). This
 * class is the <b>single</b> place that knows the DocumentV2 typed-view representation: it deserializes using
 * {@link DependencyModelSerializationUtils#deserialize(Reader)} and every subsequent typed-view read is confined here.
 *
 * <p>The typed view is read <b>exactly once</b>, at construction, into plain indexed maps (trigger field
 * -> its dependents, and rule -> language -> alternative message). All later lookups hit those
 * maps and are constant-time, so nothing re-iterates the typed accessors per query.
 *
 * <p>Callers receive only plain types ({@link DependentEntity}, {@link String}, {@link Optional}), so the
 * serialization format stays an implementation detail — swapping it is a change to this class alone.
 */
final class DependencyModelAccessor {

	/** Trigger-field path -> its dependents, in model-declaration order (insertion-ordered map). */
	private final Map<String, List<DependentEntity>> dependentsByTrigger;

	/** Rule name -> (language -> alternative error message). */
	private final Map<String, Map<String, String>> messagesByRuleAndLanguage;

	/** Distinct dependent-entity paths declared with a semantic index, in declaration order. */
	private final Set<String> semanticIndexDependentPaths;

	private DependencyModelAccessor(MM_DependencyModel_1 model) {
		Content content = model.content();
		this.dependentsByTrigger = indexDependents(content);
		this.messagesByRuleAndLanguage = indexMessages(content);
		this.semanticIndexDependentPaths = collectSemanticIndexDependentPaths(dependentsByTrigger);
	}

	/**
	 * Deserializes an embedded dependency-model resource into a runtime model. This is the single
	 * localized deserialization spot for the runtime side (see the class documentation).
	 */
	static DependencyModelAccessor fromSerialized(@NonNull Reader reader) {
		return of(DependencyModelSerializationUtils.deserialize(reader));
	}

	/** Wraps an already-deserialized model (for in-memory construction and tests). */
	static DependencyModelAccessor of(@NonNull MM_DependencyModel_1 model) {
		return new DependencyModelAccessor(model);
	}

	/** The trigger-field paths declared by the model. */
	Set<String> triggerFieldNames() {
		return dependentsByTrigger.keySet();
	}

	/**
	 * The distinct dependent-entity paths declared with a semantic index.
	 */
	Set<String> semanticIndexDependentPaths() {
		return semanticIndexDependentPaths;
	}

	/**
	 * The entities that depend on the given trigger field, in model-declared (pre-resolution) form: a
	 * dependent without a semantic index hides all repetitions; one with a semantic index is marked
	 * {@link com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier#SEMANTIC_INDEX} and carries
	 * the index-field value for later resolution against the data.
	 */
	List<DependentEntity> dependentEntitiesOf(String triggerFieldName) {
		return dependentsByTrigger.getOrDefault(triggerFieldName, List.of());
	}

	/**
	 * The alternative error message for the given rule in the given language, if the model defines one;
	 * empty otherwise (the caller then keeps the original message).
	 */
	Optional<String> alternativeMessage(String ruleName, String language) {
		Map<String, String> byLanguage = messagesByRuleAndLanguage.get(ruleName);
		return byLanguage == null ? Optional.empty() : Optional.ofNullable(byLanguage.get(language));
	}

	// --- one-time indexing off the typed view. The model is validated at code-generation time,
	// so mandatory groups are present, keys are unique. ---

	private static Map<String, List<DependentEntity>> indexDependents(Content content) {
		Map<String, List<DependentEntity>> index = new LinkedHashMap<>();
		for (TriggerFields trigger : content.triggerFields()) {
			index.put(trigger.triggerFieldName(),
					trigger.dependentEntities().stream()
							.map(DependencyModelAccessor::toDependentEntity)
							.toList());
		}
		return index;
	}

	private static Map<String, Map<String, String>> indexMessages(Content content) {
		Map<String, Map<String, String>> index = new HashMap<>();
		for (AlternativeErrorMessages alternative : content.alternativeErrorMessages()) {
			Map<String, String> byLanguage = new HashMap<>();
			for (ErrorMessages errorMessage : alternative.errorMessages()) {
				byLanguage.put(errorMessage.language(), errorMessage.errorMessage());
			}
			index.put(alternative.ruleName(), byLanguage);
		}
		return index;
	}

	private static Set<String> collectSemanticIndexDependentPaths(Map<String, List<DependentEntity>> dependentsByTrigger) {
		return dependentsByTrigger.values().stream()
				.flatMap(Collection::stream)
				.filter(DependentEntity::hasSemanticIndex)
				.map(DependentEntity::entityPath)
				.collect(Collectors.toSet());
	}

	private static DependentEntity toDependentEntity(DependentEntities dependent) {
		String semanticIndexValue = dependent.semanticIndexValue();
		return semanticIndexValue == null
				? DependentEntity.ofAllRepetitions(dependent.dependentEntity())
				: DependentEntity.ofSemanticIndex(dependent.dependentEntity(), semanticIndexValue);
	}
}
