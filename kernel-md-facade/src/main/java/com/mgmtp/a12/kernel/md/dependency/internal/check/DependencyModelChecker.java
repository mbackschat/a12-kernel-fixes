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

package com.mgmtp.a12.kernel.md.dependency.internal.check;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;
import com.mgmtp.a12.kernel.md.dependency.internal.services.DependencyMetaModelService;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views.MM_DependencyModel_1;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content.AlternativeErrorMessages;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content.TriggerFields;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content._alternativeerrormessages.ErrorMessages;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content._triggerfields.DependentEntities;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

/**
 * Validates a generated {@link MM_DependencyModel_1} at build time (concept A12K-4295, code-generation
 * step 2). Two levels are run in order:
 * <ol>
 * <li><b>Metamodel validation</b> against {@code MM_DependencyModel_1} via
 * {@link DependencyMetaModelService#validate}. If it fails, the cross-model checks are skipped (they
 * assume a well-formed model) and validation terminates.</li>
 * <li><b>Cross-document-model consistency checks</b> ("No-Metamodel-Checks") against the root document
 * model: trigger/dependent/rule references resolve, index fields and values are valid, and the repetition
 * structure lets a trigger's repetition be carried to its dependents.</li>
 * </ol>
 *
 * <p>Every violation is reported to the {@code IProblemReporter}; the method returns {@code false} when
 * anything was reported so the caller aborts code generation instead of emitting a broken model.
 *
 * <p><b>Not checked here (H-DV rejection):</b> whether the model is generated as a unique-entities H-DV
 * (A12-DVs only) is not visible on the base {@link IDocumentModel} — the distinction lives in the
 * code-generation settings — so it is enforced at codegen integration (and the runtime service fails fast
 * as a backstop), not in this DM-only checker. One index field per group is structural
 * ({@link IGroup#getIndexField()} is single-valued); at most one across a dependent's path is checked here (B4e).
 */
public final class DependencyModelChecker {

	private final IDocumentModelSearchService searchService;
	private final List<Locale> supportedLocales;
	private final DependencyModelCheckErrorLookup errorLookup;
	private final IProblemReporter problemReporter;
	private final Locale locale;

	private DependencyModelChecker(IDocumentModel baseDocumentModel, IProblemReporter problemReporter) {
		this.locale = baseDocumentModel.getContent().getDocumentModelConfig().getConditionLanguage();
		this.searchService = new DocumentModelServiceFactory().createDocumentModelSearchService(baseDocumentModel);
		this.supportedLocales = baseDocumentModel.getHeader().getLocales();
		this.errorLookup = new DependencyModelCheckErrorLookup(locale);
		this.problemReporter = problemReporter;
	}

	/**
	 * Checks the generated dependency model against its metamodel and against the base document model.
	 *
	 * @param model
	 * 		the generated dependency model.
	 * @param baseDocumentModel
	 * 		the root document model the references are resolved against.
	 * @param problemReporter
	 * 		receives a problem for every violation.
	 * @return {@code true} if the model is valid; {@code false} if any problem was reported.
	 */
	public static boolean checkDependencyModel(
			@NonNull MM_DependencyModel_1 model,
			@NonNull IDocumentModel baseDocumentModel,
			@NonNull IProblemReporter problemReporter) {
		return new DependencyModelChecker(baseDocumentModel, problemReporter).check(model);
	}

	private boolean check(MM_DependencyModel_1 model) {
		if (!validateAgainstMetaModel(model)) {
			return false; // B9: subsequent checks assume a well-formed model
		}
		boolean valid = true;
		for (TriggerFields triggerFields : model._at(MM_DependencyModel_1._pointer().content().triggerFields())) {
			valid &= checkTrigger(triggerFields);
		}
		for (AlternativeErrorMessages message : model._at(MM_DependencyModel_1._pointer().content().alternativeErrorMessages())) {
			valid &= checkAlternativeMessage(message);
		}
		return valid;
	}

	/** Code-generation step 1 (B9): validate against the metamodel and forward the messages. */
	private boolean validateAgainstMetaModel(MM_DependencyModel_1 model) {
		IDocumentValidationResult result = DependencyMetaModelService.validate(model, locale);
		if (result.noErrorOccurred()) {
			return true;
		}
		result.getMessages().forEach(message ->
				problemReporter.reportProblem(new MVKCoreError(message.getErrorText(), Severity.ERROR, null)));
		return false;
	}

	private boolean checkTrigger(TriggerFields triggerFields) {
		String triggerName = triggerFields.triggerFieldName();
		Optional<IField> triggerField = field(triggerName);
		boolean valid = triggerField.isPresent();
		if (!valid) {
			// B1
			errorLookup.reportError(problemReporter, null, DependencyModelCheckErrorType.MVK_INVALID_TRIGGER_FIELD, triggerName);
		}
		List<DependentEntities> dependents = triggerFields.dependentEntities();
		for (DependentEntities dependent : dependents) {
			valid &= checkDependent(dependent, triggerName);
		}
		valid &= checkNoIndirectReferences(dependents);
		return valid;
	}

	private boolean checkDependent(DependentEntities dependent, String triggerName) {
		String dependentPath = dependent.dependentEntity();
		Optional<IElement> dependentElement = searchService.getByPath(dependentPath)
				.filter(element -> element instanceof IField || element instanceof IGroup);
		if (dependentElement.isEmpty()) {
			// B2
			errorLookup.reportError(problemReporter, null, DependencyModelCheckErrorType.MVK_INVALID_DEPENDENT_ENTITY,
					dependentPath);
			return false;
		}
		boolean valid = true;
		String semanticIndexValue = dependent.semanticIndexValue();
		if (semanticIndexValue != null && !semanticIndexValue.isEmpty()) {
			valid &= checkSemanticIndex(dependentPath, dependentElement.get(), semanticIndexValue, triggerName);
		}
		return valid;
	}

	private boolean checkSemanticIndex(String dependentPath, IElement dependentElement, String semanticIndexValue,
			String triggerName) {
		List<IndexFieldLocation> indexFields = indexFieldsOnPath(dependentPath);
		if (indexFields.isEmpty()) {
			// B4a
			errorLookup.reportError(problemReporter, dependentElement,
					DependencyModelCheckErrorType.MVK_INVALID_INDEX_FIELD, dependentPath);
			return false;
		}
		if (indexFields.size() > 1) {
			// B4e: a single semantic index value cannot say which of several path index fields it addresses
			errorLookup.reportError(problemReporter, dependentElement,
					DependencyModelCheckErrorType.MVK_MULTIPLE_INDEX_FIELDS, dependentPath);
			return false;
		}
		IndexFieldLocation indexField = indexFields.get(0);
		boolean valid = true;
		if (!isAdmissibleIndexValue(indexField.indexField(), semanticIndexValue)) {
			// B4d
			errorLookup.reportError(problemReporter, dependentElement,
					DependencyModelCheckErrorType.MVK_INVALID_INDEX_VALUE, semanticIndexValue, dependentPath,
					indexField.indexFieldFullName());
			valid = false;
		}
		// B7: the trigger's indices are copied only down to the common ancestor of trigger and dependent; the
		// index-field layer is then resolved by value, so a repeatable group between them would leave the
		// index value unable to resolve to a single repetition.
		int commonAncestorLevel = ElementPathUtils.commonAncestorLevel(triggerName, dependentPath);
		if (repeatableGroupBetween(commonAncestorLevel, indexField.groupFullName())) {
			errorLookup.reportError(problemReporter, dependentElement,
					DependencyModelCheckErrorType.MVK_INVALID_INDEX_FOR_TRIGGER, triggerName, dependentPath);
			valid = false;
		}
		return valid;
	}

	/** B3: within one trigger, a dependent group must not also list a sub-entity whose repetitions it covers. */
	private boolean checkNoIndirectReferences(List<DependentEntities> dependents) {
		boolean valid = true;
		for (DependentEntities ancestor : dependents) {
			String ancestorPath = ancestor.dependentEntity();
			if (group(ancestorPath).isEmpty()) {
				// a dependent group absent from the base model is already reported by the existence check (B2);
				continue;
			}
			for (DependentEntities descendant : dependents) {
				String descendantPath = descendant.dependentEntity();
				if (ElementPathUtils.isDescendant(descendantPath, ancestorPath) && covers(ancestor, descendant)) {
					errorLookup.reportError(problemReporter, null,
							DependencyModelCheckErrorType.MVK_INVALID_INDIRECT_REFERENCE, descendantPath, ancestorPath);
					valid = false;
				}
			}
		}
		return valid;
	}

	/**
	 * Whether the group's repetitions include the sub-entity's: the group covers all repetitions (no semantic
	 * index) or the same repetition (same semantic index). Non-overlapping coverage (e.g. the sub-entity for all
	 * repetitions, the group for one concrete index) is allowed.
	 */
	private static boolean covers(DependentEntities ancestor, DependentEntities descendant) {
		String ancestorIndex = ancestor.semanticIndexValue();
		return ancestorIndex == null || ancestorIndex.isEmpty() || ancestorIndex.equals(descendant.semanticIndexValue());
	}

	private boolean checkAlternativeMessage(AlternativeErrorMessages message) {
		String ruleName = message.ruleName();
		boolean valid = true;
		if (rule(ruleName).isEmpty()) {
			// B5
			errorLookup.reportError(problemReporter, null, DependencyModelCheckErrorType.MVK_INVALID_RULE, ruleName);
			valid = false;
		}
		for (ErrorMessages errorMessage : message.errorMessages()) {
			String language = errorMessage.language();
			if (!isSupportedLanguage(language)) {
				// B6
				errorLookup.reportError(problemReporter, null, DependencyModelCheckErrorType.MVK_INVALID_LANGUAGE, language,
						ruleName);
				valid = false;
			}
			String parameter = firstParameter(errorMessage.errorMessage());
			if (parameter != null) {
				// B8
				errorLookup.reportError(problemReporter, null,
						DependencyModelCheckErrorType.MVK_INVALID_ALTERNATIVE_MESSAGE_PARAMETER, ruleName, parameter);
				valid = false;
			}
		}
		return valid;
	}

	// --- structural helpers ---

	/** Every group on the path (the dependent's own group or an ancestor) that carries an index field, innermost first. */
	private List<IndexFieldLocation> indexFieldsOnPath(String dependentPath) {
		List<IndexFieldLocation> locations = new ArrayList<>();
		for (String groupPath : groupPathsFromInnermost(dependentPath)) {
			var indexFieldOpt = group(groupPath).flatMap(IGroup::getIndexField);
			if (indexFieldOpt.isEmpty()) {
				continue;
			}
			String indexFieldFullName = ElementPathUtils.absPath(groupPath, indexFieldOpt.orElseThrow().getRelativePath());
			field(indexFieldFullName).ifPresent(
					indexField -> locations.add(new IndexFieldLocation(groupPath, indexFieldFullName, indexField)));
		}
		return locations;
	}

	/** The dependent's own group (if it is a group) followed by its ancestor groups, innermost first. */
	private List<String> groupPathsFromInnermost(String dependentPath) {
		List<String> groups = ElementPathUtils.getAncestorPaths(dependentPath).reversed();
		if (group(dependentPath).isPresent()) {
			groups.addFirst(dependentPath);
		}
		return groups;
	}

	private boolean isAdmissibleIndexValue(IField indexField, String value) {
		return indexField.getEffectiveType()
				.filter(IEnumerationType.class::isInstance)
				.map(IEnumerationType.class::cast)
				.map(enumType -> enumType.getValues().stream().anyMatch(enumValue -> value.equals(enumValue.getValue())))
				// non-enumeration index fields do not constrain the admissible values in Stage 1
				.orElse(true);
	}

	/**
	 * A repeatable group strictly between the common-ancestor level (exclusive) and the index field's group
	 * (exclusive). {@code commonAncestorLevel} is 0-based ({@code -1} = the model root, see
	 * {@link ElementPathUtils#commonAncestorLevel}); the ancestor list is ordered outermost-first, so an
	 * entry's index equals its hierarchy level.
	 */
	private boolean repeatableGroupBetween(int commonAncestorLevel, String indexFieldGroup) {
		List<String> ancestors = ElementPathUtils.getAncestorPaths(indexFieldGroup);
		for (int level = commonAncestorLevel + 1; level < ancestors.size(); level++) {
			if (isRepeatableGroup(ancestors.get(level))) {
				return true;
			}
		}
		return false;
	}

	private boolean isRepeatableGroup(String fullName) {
		return group(fullName).map(group -> group.getRepeatability() > 1).orElse(false);
	}

	private boolean isSupportedLanguage(String language) {
		// compared on the language part only (Locale#getLanguage → "de")
		return supportedLocales.stream().anyMatch(supported -> supported.getLanguage().equals(language));
	}

	/**
	 * The first {@code $...$} parameter referenced by an error text, or {@code null} if it references none.
	 * A doubled {@code $$} is an escaped literal dollar (mirrors {@code FehlertextUtil#mapParamsInFehlertext}) instead of a
	 * parameter.
	 */
	private static String firstParameter(String errorText) {
		if (errorText == null || errorText.isEmpty()) {
			return null;
		}
		String[] parts = errorText.split("\\$", -1);
		for (int i = 1; i < parts.length; i += 2) {
			if (!parts[i].isEmpty()) {
				return parts[i];
			}
		}
		return null;
	}

	private Optional<IField> field(String fullName) {
		return searchService.getFieldByPath(fullName);
	}

	private Optional<IGroup> group(String fullName) {
		return searchService.getGroupByPath(fullName);
	}

	private Optional<IRule> rule(String fullName) {
		return searchService.getRuleByPath(fullName);
	}

	/** A resolved index field together with the group that declares it. */
	private record IndexFieldLocation(String groupFullName, String indexFieldFullName, IField indexField) {
	}
}
