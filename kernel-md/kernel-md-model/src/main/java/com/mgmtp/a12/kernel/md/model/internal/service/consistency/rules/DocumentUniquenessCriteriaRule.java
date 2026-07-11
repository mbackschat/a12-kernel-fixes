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

package com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextErrorLookup;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblem;
import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentUniquenessCriterion;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IRequirednessConfig;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelCategory;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelConsistencyRule;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.FieldWrapper;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

public class DocumentUniquenessCriteriaRule extends DocumentModelConsistencyRule {
	@Override public void checkModel(IDocumentModel model, DocumentModelConsistencyProblemReporter pr) {
		checkDocumentUniquenessCriteraNamesAreUnique(model, pr);
		model.getContent().getDocumentUniquenessCriteria().forEach(criterion -> {
			checkDocumentUniquenessCriterionConsistency(model, criterion, pr);
		});
	}

	private static void checkDocumentUniquenessCriteraNamesAreUnique(
			@NonNull IDocumentModel documentModel,
			@NonNull DocumentModelConsistencyProblemReporter pr
	) {
		Map<String, List<IDocumentUniquenessCriterion>> nameToCriteria = documentModel.getContent().getDocumentUniquenessCriteria()
				.stream()
				.collect(Collectors.groupingBy(IDocumentUniquenessCriterion::getName));
		nameToCriteria.forEach((name, criteria) -> {
			if (criteria.size() > 1) {
				criteria.forEach(criterion -> {
					pr.reportProblem(new DocumentModelConsistencyProblem(
							documentModel.getHeader().getId(), criterion,
							DocumentModelCategory.DOCUMENT_MODEL_UNIQUENESS_CRITERION_DUPLICATE_NAME,
							Severity.ERROR, name));
				});
			}
		});
	}

	public static void checkDocumentUniquenessCriterionConsistency(
			@NonNull IDocumentModel documentModel,
			@NonNull IDocumentUniquenessCriterion documentUniquenessCriterion,
			@NonNull DocumentModelConsistencyProblemReporter pr
	) {
		DocumentModel documentModelIntern = ExternalDocumentModelService.getInternDocumentModel(documentModel);
		String criterionName = documentUniquenessCriterion.getName();
		SequencedSet<String> fieldFullNames = documentUniquenessCriterion.getFieldFullNames();

		// Ensure that every Document Uniqueness Criterion has a non-empty name.
		if (StringUtils.isEmpty(criterionName)) {
			pr.reportProblem(new DocumentModelConsistencyProblem(
					documentModel.getHeader().getId(), documentUniquenessCriterion,
					DocumentModelCategory.DOCUMENT_MODEL_UNIQUENESS_CRITERION_EMPTY_NAME,
					Severity.ERROR));
		}

		// Ensure that a Document Uniqueness Criterion consists of at least one field.
		if (fieldFullNames.isEmpty()) {
			pr.reportProblem(new DocumentModelConsistencyProblem(
					documentModel.getHeader().getId(), documentUniquenessCriterion,
					DocumentModelCategory.DOCUMENT_MODEL_UNIQUENESS_CRITERION_FIELDS_EMPTY,
					Severity.ERROR, criterionName));
		}

		for (String fieldFullName : fieldFullNames) {
			Optional<IField> fieldOpt = DocumentModelUtils.getElementByPath(documentModelIntern, fieldFullName)
					.filter(Field.class::isInstance).map(Field.class::cast).map(FieldWrapper::new);
			if (fieldOpt.isEmpty()) {
				pr.reportProblem(new DocumentModelConsistencyProblem(
						documentModel.getHeader().getId(), documentUniquenessCriterion,
						DocumentModelCategory.DOCUMENT_MODEL_UNIQUENESS_CRITERION_FIELD_NOT_FOUND,
						Severity.ERROR, criterionName, fieldFullName));
				continue;
			}

			IField field = fieldOpt.get();

			// Ensure that fields used as part of a Document Uniqueness Criterion are required in a way that
			// guarantees a value in the document. A requiredness config with mode "relativeToParent" only
			// requires a value if the parent group is filled, so the field might still be empty. Only the mode
			// "absoluteOrRelativeToNextRepAncestor" guarantees the field is filled.
			boolean alwaysRequired = field.getRequirednessConfig()
					.map(config -> config.getMode() == IRequirednessConfig.Mode.absoluteOrRelativeToNextRepAncestor)
					.orElse(false);
			if (!alwaysRequired) {
				pr.reportProblem(new DocumentModelConsistencyProblem(
						documentModel.getHeader().getId(), documentUniquenessCriterion,
						DocumentModelCategory.DOCUMENT_MODEL_UNIQUENESS_CRITERION_FIELD_NOT_REQUIRED,
						Severity.ERROR, criterionName, fieldFullName));
			}

			// Ensure that fields used as part of a Document Uniqueness Criterion are not repeatable.
			if (ElementUtils.isRepeatable(field)) {
				pr.reportProblem(new DocumentModelConsistencyProblem(
						documentModel.getHeader().getId(), documentUniquenessCriterion,
						DocumentModelCategory.DOCUMENT_MODEL_UNIQUENESS_CRITERION_FIELD_REPEATABLE,
						Severity.ERROR, criterionName, fieldFullName));
			}

			// Ensure that fields used as part of a Document Uniqueness Criterion are not transient.
			if (field.isTransient()) {
				pr.reportProblem(new DocumentModelConsistencyProblem(
						documentModel.getHeader().getId(), documentUniquenessCriterion,
						DocumentModelCategory.DOCUMENT_MODEL_UNIQUENESS_CRITERION_FIELD_TRANSIENT,
						Severity.ERROR, criterionName, fieldFullName));
			}
		}

		// Ensure there is one error message defined per Document Model Locale.
		for (Locale locale : documentModel.getHeader().getLocales()) {
			if (StringUtils.isEmpty(documentUniquenessCriterion.getErrorMessage().get(locale))) {
				pr.reportProblem(new DocumentModelConsistencyProblem(
						documentModel.getHeader().getId(), documentUniquenessCriterion,
						DocumentModelCategory.DOCUMENT_MODEL_UNIQUENESS_CRITERION_MISSING_ERROR_TEXT,
						Severity.ERROR, criterionName, locale.toString()));
			}
		}

		checkDocumentUniquenessCriterionErrorText(documentModel, documentUniquenessCriterion, pr);
	}

	public static void checkDocumentUniquenessCriterionErrorText(
			@NonNull IDocumentModel documentModel,
			@NonNull IDocumentUniquenessCriterion documentUniquenessCriterion,
			@NonNull DocumentModelConsistencyProblemReporter pr
	) {
		String criterionName = documentUniquenessCriterion.getName();

		// Build the set of allowed parameter values. Differently to field and rule error texts the suffix is _always_ ".value"
		// regardless of the condition language set in the Document Model.
		SequencedSet<String> fieldFullNames = documentUniquenessCriterion.getFieldFullNames();
		Set<String> allowedParameterTexts = fieldFullNames.stream()
				.flatMap(fn -> Stream.of(fn, fn + ".value"))
				.collect(Collectors.toSet());

		// Ensure that all error messages defined for the Document Uniqueness Criterion are valid.
		for (Map.Entry<Locale, String> entry : documentUniquenessCriterion.getErrorMessage().entrySet()) {
			Locale locale = entry.getKey();
			String errorText = entry.getValue();
			if (StringUtils.isEmpty(errorText)) {
				continue;
			}

			IProblemReporter forwardingPR = problem -> pr.reportProblem(
					new DocumentModelConsistencyProblem(documentModel.getHeader().getId(), documentUniquenessCriterion,
							DocumentModelCategory.DOCUMENT_MODEL_UNIQUENESS_CRITERION_INVALID_ERROR_TEXT,
							problem.getSeverity(), criterionName, locale.toString(), problem.getMessage()));
			List<String> parameters = FehlertextUtil.checkErrorTextAndExtractParameters(
					errorText, new FehlertextErrorLookup(Locale.US), forwardingPR, documentModel, false);
			parameters.forEach(parameterText -> {
				if (!allowedParameterTexts.contains(parameterText)) {
					pr.reportProblem(new DocumentModelConsistencyProblem(
							documentModel.getHeader().getId(), documentUniquenessCriterion,
							DocumentModelCategory.DOCUMENT_MODEL_UNIQUENESS_CRITERION_INVALID_ERROR_TEXT_PARAMETER,
							Severity.ERROR, criterionName, locale.toString()));
				}
			});
		}
	}
}
