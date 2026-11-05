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

package com.mgmtp.a12.kernel.md.dependency.internal.generator;

import static com.mgmtp.a12.kernel.md.model.internal.service.utils.AnnotationUtils.findAnnotation;
import static com.mgmtp.a12.kernel.md.model.internal.service.utils.AnnotationUtils.hasAnnotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.dependency.internal.DependencyModelUtil;
import com.mgmtp.a12.kernel.md.dependency.internal.check.DependencyModelCheckErrorLookup;
import com.mgmtp.a12.kernel.md.dependency.internal.check.DependencyModelCheckErrorType;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelVisitorWithFullNames;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views.MM_DependencyModel_1;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content._triggerfields.TriggerAction;

import lombok.NonNull;

/**
 * Builds the dependency model from the dependency annotations of a base document model.
 *
 * <p>The transformation is gated by the model-level {@link #ANNOTATION_ACTIVE} master switch: if it is
 * absent the annotations are not evaluated at all and no model is produced. Otherwise every trigger
 * field, dependent entity (grouped by its trigger field) and alternative error message becomes an entry
 * of an {@link MM_DependencyModel_1}. Stage 1 always maps to {@link TriggerAction#hide} and puts the
 * single alternative message text into the German ({@code de}) entry.
 *
 * <p>A malformed {@link #ANNOTATION_DEPENDENT_ENTITY} value, or a {@code DependentEntity} referencing a path
 * that is not marked with a {@link #ANNOTATION_TRIGGER_FIELD} annotation, is reported to the
 * {@code IProblemReporter}; when anything was reported no model is returned, so the caller terminates code
 * generation instead of emitting a broken model.
 */
public final class DependencyModelGenerator {

	/** Model-level master switch; when absent, no dependency model is generated. */
	public static final String ANNOTATION_ACTIVE = "_A12.kernel.DependencyModel.Active";
	/** Marks a field as a trigger field. */
	public static final String ANNOTATION_TRIGGER_FIELD = "_A12.kernel.DependencyModel.TriggerField";
	/** Marks a field or group as hidden when the referenced trigger(s) fire. */
	public static final String ANNOTATION_DEPENDENT_ENTITY = "_A12.kernel.DependencyModel.DependentEntity";
	/** Alternative error text of a rule, used when a causing entity is hidden. */
	public static final String ANNOTATION_ALTERNATIVE_MESSAGE = "_A12.kernel.DependencyModel.AlternativeMessage";

	/** Stage-1 language of the single alternative message (see concept <i>Scope &amp; Limitations</i>). */
	private static final Locale ALTERNATIVE_MESSAGE_LOCALE = Locale.GERMAN;

	private static final String TRIGGER_SEPARATOR = ";";
	private static final String INDEX_SEPARATOR = ",";

	private DependencyModelGenerator() {
		throw new UnsupportedOperationException("Should not be instantiated");
	}

	/**
	 * Builds the dependency model from the annotations of the given base document model.
	 *
	 * @param baseDocumentModel
	 * 		the annotated base document model.
	 * @param problemReporter
	 * 		receives a problem for every malformed {@code DependentEntity} annotation.
	 * @return the produced model; empty if the {@link #ANNOTATION_ACTIVE} master switch is not set, or if
	 * 		a problem was reported.
	 */
	public static Optional<MM_DependencyModel_1> generateDependencyModel(
			@NonNull IDocumentModel baseDocumentModel,
			@NonNull IProblemReporter problemReporter) {
		if (!hasAnnotation(baseDocumentModel.getHeader().getAnnotations(), ANNOTATION_ACTIVE)) {
			return Optional.empty();
		}

		DependencyModelCheckErrorLookup errorLookup =
				new DependencyModelCheckErrorLookup(baseDocumentModel.getContent().getDocumentModelConfig().getConditionLanguage());
		AnnotationCollector collector = new AnnotationCollector(problemReporter, errorLookup);
		new DocumentModelWalker().acceptDocumentModel(baseDocumentModel, collector);
		collector.reportUnannotatedTriggerFields();
		if (collector.hasProblem) {
			return Optional.empty();
		}

		return Optional.of(collector.buildModel(baseDocumentModel.getHeader().getId()));
	}

	/** One parsed trigger spec of a {@code DependentEntity} value. */
	private record TriggerSpec(String triggerFieldPath, String semanticIndexValue) {
	}

	/** A hidden entity attached to a trigger field, in model form; {@code origin} is kept for error reporting. */
	private record Dependent(String entityPath, String semanticIndexValue, IElement origin) {
	}

	/** A rule's single (German) alternative error message. */
	private record AlternativeMessage(String ruleName, String text) {
	}

	/**
	 * Collects the trigger fields, their dependents and the alternative messages while walking the base
	 * document model, parsing the {@code DependentEntity} grammar as it goes.
	 */
	private static final class AnnotationCollector extends DocumentModelVisitorWithFullNames {

		private final IProblemReporter problemReporter;
		private final DependencyModelCheckErrorLookup errorLookup;

		/** Trigger-field path -> its dependents, in reference-encounter order (drives the produced trigger order). */
		private final Map<String, List<Dependent>> dependentsByTrigger = new LinkedHashMap<>();
		private final List<AlternativeMessage> alternativeMessages = new ArrayList<>();

		/** Full names of the fields marked with a {@code TriggerField} annotation. */
		private final Set<String> annotatedTriggerFields = new HashSet<>();

		private boolean hasProblem;

		AnnotationCollector(IProblemReporter problemReporter, DependencyModelCheckErrorLookup errorLookup) {
			this.problemReporter = problemReporter;
			this.errorLookup = errorLookup;
		}

		@Override public void enterField(@NonNull IField field) {
			if (hasAnnotation(field.getAnnotations(), ANNOTATION_TRIGGER_FIELD)) {
				annotatedTriggerFields.add(getFullName());
			}
			collectDependent(field);
		}

		@Override public void enterGroup(@NonNull IGroup group) {
			collectDependent(group);
		}

		@Override public void enterRule(@NonNull IRule rule) {
			findAnnotation(rule.getAnnotations(), ANNOTATION_ALTERNATIVE_MESSAGE).ifPresent(annotation -> {
				String text = annotation.getValue();
				alternativeMessages.add(new AlternativeMessage(getFullName(), text == null ? "" : text));
			});
		}

		private void collectDependent(IElement element) {
			// a present-but-empty/null value is caught as a malformed grammar by parseTriggerSpecs
			findAnnotation(element.getAnnotations(), ANNOTATION_DEPENDENT_ENTITY)
					.flatMap(annotation -> parseTriggerSpecs(annotation.getValue(), element))
					.ifPresent(specs -> {
						for (TriggerSpec spec : specs) {
							dependentsByTrigger
									.computeIfAbsent(spec.triggerFieldPath(), key -> new ArrayList<>())
									.add(new Dependent(getFullName(), spec.semanticIndexValue(), element));
						}
					});
		}

		/**
		 * Parses the {@code DependentEntity} value grammar
		 * {@code <TriggerField>(',' <IndexValue>)? (';' <TriggerField>(',' <IndexValue>)?)*} (whitespace
		 * around each spec and each part is trimmed). Reports a problem and returns empty on the first
		 * malformed spec.
		 */
		private Optional<List<TriggerSpec>> parseTriggerSpecs(String value, IElement origin) {
			if (value == null || value.isBlank()) {
				return reportMalformed(DependencyModelCheckErrorType.MVK_MALFORMED_DEPENDENT_ENTITY, value, origin);
			}
			List<TriggerSpec> specs = new ArrayList<>();
			for (String rawSpec : value.split(TRIGGER_SEPARATOR, -1)) {
				String spec = rawSpec.trim();
				String[] parts = spec.split(INDEX_SEPARATOR, -1);
				if (spec.isEmpty() || parts.length > 2) {
					return reportMalformed(DependencyModelCheckErrorType.MVK_MALFORMED_DEPENDENT_ENTITY, value, origin);
				}
				String triggerFieldPath = parts[0].trim();
				if (triggerFieldPath.isEmpty()) {
					return reportMalformed(DependencyModelCheckErrorType.MVK_MALFORMED_DEPENDENT_ENTITY, value, origin);
				}
				String semanticIndexValue = null;
				if (parts.length == 2) {
					semanticIndexValue = parts[1].trim();
					if (semanticIndexValue.isEmpty()) {
						return reportMalformed(DependencyModelCheckErrorType.MVK_MALFORMED_DEPENDENT_ENTITY_INDEX, value, origin);
					}
				}
				specs.add(new TriggerSpec(triggerFieldPath, semanticIndexValue));
			}
			return Optional.of(specs);
		}

		private Optional<List<TriggerSpec>> reportMalformed(DependencyModelCheckErrorType errorType, String value,
				IElement origin) {
			hasProblem = true;
			// message args: (1) the dependent field/group path, (2) the offending annotation value
			errorLookup.reportError(problemReporter, origin, errorType, getFullName(), value);
			return Optional.empty();
		}

		/**
		 * Reports {@link DependencyModelCheckErrorType#MVK_TRIGGER_FIELD_NOT_ANNOTATED} for every dependent
		 * entity that references a path carrying no {@code TriggerField} annotation. Run after the walk,
		 * because a {@code DependentEntity} may reference a trigger field declared later in the model.
		 */
		private void reportUnannotatedTriggerFields() {
			dependentsByTrigger.forEach((triggerFieldPath, dependents) -> {
				if (!annotatedTriggerFields.contains(triggerFieldPath)) {
					hasProblem = true;
					for (Dependent dependent : dependents) {
						// message args: (1) the dependent field/group path, (2) the referenced non-trigger path
						errorLookup.reportError(problemReporter, dependent.origin(),
								DependencyModelCheckErrorType.MVK_TRIGGER_FIELD_NOT_ANNOTATED,
								dependent.entityPath(), triggerFieldPath);
					}
				}
			});
		}

		private MM_DependencyModel_1 buildModel(String headerId) {
			// every entry has at least one dependent (created via computeIfAbsent on the first reference)
			var triggerFields = dependentsByTrigger.entrySet().stream()
					.map(entry -> {
						var dependentEntities = entry.getValue().stream()
								.map(dependent -> DependencyModelUtil.createDependentEntity(dependent.entityPath(),
										dependent.semanticIndexValue()))
								.toList();
						return DependencyModelUtil.createTriggerField(entry.getKey(), TriggerAction.hide, dependentEntities);
					})
					.toList();
			var altMessages = alternativeMessages.stream()
					.map(message -> DependencyModelUtil.createAlternativeMessage(message.ruleName(),
							Map.of(ALTERNATIVE_MESSAGE_LOCALE, message.text())))
					.toList();

			return DependencyModelUtil.createDependencyModel(headerId, triggerFields, altMessages);
		}
	}
}
