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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblem;
import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelCategory;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelConsistencyRule;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

public class AttachmentGroupRule extends DocumentModelConsistencyRule {

	private static final String CONTENT = "content";

	private static final String[] EXIST_FIELDS_RULE_SET = { "original_filename", "internal_filename", CONTENT, "attachment_id",
			"size", "mime_type", "category", "description" };
	private static final Map<Locale, List<String>> RULES_REQUIRED = Map.of(
			Locale.US, List.of(
					"GroupFilled(RuleGroup) and NotExactlyOneFieldFilled(attachment_id, content)",
					"FieldFilled(content) and FieldNotFilled(size)",
					"GroupFilled(RuleGroup) and FieldNotFilled(internal_filename)",
					"GroupFilled(RuleGroup) and FieldNotFilled(mime_type)"
			),
			Locale.GERMANY, List.of(
					"KontextAngegeben(RegelKontext) und NichtGenauEinFeldAngegeben(attachment_id, content)",
					"FeldAngegeben(content) und FeldNichtAngegeben(size)",
					"KontextAngegeben(RegelKontext) und FeldNichtAngegeben(internal_filename)",
					"KontextAngegeben(RegelKontext) und FeldNichtAngegeben(mime_type)"
			)
	);

	@Override
	public void checkModel(final IDocumentModel model, DocumentModelConsistencyProblemReporter pr) {
		final AttachmentGroupCheckerVisitor visitor = new AttachmentGroupCheckerVisitor(model, pr);
		new DocumentModelWalker().acceptDocumentModel(model, visitor);
	}

	private static class AttachmentGroupCheckerVisitor extends DocumentModelVisitor {

		private final String modelName;
		private final Locale conditionLanguage;
		private final DocumentModelConsistencyProblemReporter problemReporter;

		public AttachmentGroupCheckerVisitor(IDocumentModel model, DocumentModelConsistencyProblemReporter problemReporter) {
			this.modelName = model.getHeader().getId();
			this.conditionLanguage = model.getContent().getDocumentModelConfig().getConditionLanguage();
			this.problemReporter = problemReporter;
		}

		@Override
		public VisitProcess visitGroup(@NonNull IGroup group) {
			checkAttachmentForGroup(group);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private void checkAttachmentForGroup(IGroup group) {
			if (group.getUsageType().filter(DocumentModelUtils.ATTACHMENT_NAME::equals).isEmpty()) {
				return;
			}

			List<String> fieldNames = group.getElements().stream().filter(IField.class::isInstance).map(IElement::getName).toList();
			List<String> rulesInAttachmentGroup = group.getElements().stream()
					.filter(IRule.class::isInstance)
					.map(rule -> ((IRule) rule).getErrorCondition()).toList();

			Map<DocumentModelCategory, List<String>> problemsRules = verifyProblemsWithExistFields(fieldNames,
					Arrays.asList(EXIST_FIELDS_RULE_SET));
			problemsRules.putAll(verifyProblemsWithExistRules(rulesInAttachmentGroup, RULES_REQUIRED.get(conditionLanguage)));

			for (Entry<DocumentModelCategory, List<String>> entry : problemsRules.entrySet()) {
				DocumentModelCategory documentModelCategoryMessage = entry.getKey();
				List<String> problemsForCategory = entry.getValue();
				String errorMessage = String.join(", ", problemsForCategory);
				problemReporter.reportProblem(new DocumentModelConsistencyProblem(
						modelName,
						group,
						documentModelCategoryMessage,
						Severity.ERROR,
						errorMessage,
						group.getName()));
			}

			if (!fieldNames.contains(CONTENT)) {
				return;
			}
			IElement contentField = group.getElements().stream().filter(element -> element.getName().equals(CONTENT)).findFirst()
					.orElse(null);
			if (!(contentField instanceof IField field)) {
				return;
			}
			if (!(field.getFieldType() instanceof IStringType contentDataType)) {
				return;
			}
			if (!contentDataType.isNoValueValidation() || !contentDataType.isLineBreaksPermitted()) {
				problemReporter.reportProblem(new DocumentModelConsistencyProblem(
						modelName,
						group,
						DocumentModelCategory.DOCUMENT_MODEL_ATTACHMENT_CONTENT_NO_VALUE_VALIDATION,
						Severity.ERROR,
						group.getName()));
			}
		}

		private static Map<DocumentModelCategory, List<String>> verifyProblemsWithExistFields(List<String> fieldNames,
				List<String> fieldNamesNeedToBeVerified) {
			Map<DocumentModelCategory, List<String>> problems = new EnumMap<>(DocumentModelCategory.class);
			List<String> fieldsMissing = getMissingRules(fieldNamesNeedToBeVerified, fieldNames);
			if (!fieldsMissing.isEmpty()) {
				problems.put(DocumentModelCategory.DOCUMENT_MODEL_ATTACHMENT_MISSING_FIELD, fieldsMissing);
			}
			return problems;
		}

		private static Map<DocumentModelCategory, List<String>> verifyProblemsWithExistRules(List<String> rulesContext,
				List<String> ruleNamesNeedToBeVerified) {
			Map<DocumentModelCategory, List<String>> problems = new EnumMap<>(DocumentModelCategory.class);
			List<String> rulesMissing = getMissingRules(ruleNamesNeedToBeVerified, rulesContext);
			if (!rulesMissing.isEmpty()) {
				problems.put(DocumentModelCategory.DOCUMENT_MODEL_ATTACHMENT_REQUIRED_RULES, rulesMissing);
			}
			return problems;
		}

		private static List<String> getMissingRules(List<String> dataList, List<String> checkedList) {
			List<String> fieldsMissing = new ArrayList<>(dataList);
			fieldsMissing.removeAll(checkedList);
			return fieldsMissing;
		}
	}
}
