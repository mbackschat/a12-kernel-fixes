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

import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblem;
import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelObjectReference;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelCategory;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelConsistencyRule;
import com.mgmtp.a12.model.notification.Severity;

public class MultiSelectGroupRule extends DocumentModelConsistencyRule {

	@Override
	public void checkModel(final IDocumentModel model, DocumentModelConsistencyProblemReporter pr) {
		final GroupCheckerVisitor visitor = new GroupCheckerVisitor(model.getHeader().getId(), pr);
		new DocumentModelWalker().acceptDocumentModel(model, visitor);
	}

	private static class GroupCheckerVisitor extends DocumentModelVisitor {
		private static final String MULTI_SELECT_ID = "multi-select";

		private final String modelName;
		private final DocumentModelConsistencyProblemReporter problemReporter;

		public GroupCheckerVisitor(String modelName, DocumentModelConsistencyProblemReporter problemReporter) {
			this.modelName = modelName;
			this.problemReporter = problemReporter;
		}

		@Override
		public VisitProcess visitGroup(IGroup group) {
			checkGroupIfMultiSelect(group);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private void checkGroupIfMultiSelect(IGroup group) {
			if (group.getUsageType().filter(MULTI_SELECT_ID::equals).isEmpty()) {
				return;
			}
			checkRepeatableGroup(group);
			checkRequiredField(group);
			checkAllowedElementsInGroup(group);
		}

		private void checkRepeatableGroup(IGroup group) {
			if (group.getRepeatability() <= 1) {
				problemReporter.reportProblem(new DocumentModelConsistencyProblem(
						modelName,
						group,
						DocumentModelCategory.DOCUMENT_MODEL_MULTI_SELECT_REPEATABLE_GROUP,
						Severity.ERROR,
						group.getName()));
			}
		}

		private void checkRequiredField(IGroup group) {
			Set<IField> fieldsInGroup = group.getElements().stream()
					.filter(IField.class::isInstance)
					.map(IField.class::cast)
					.collect(Collectors.toSet());
			if (fieldsInGroup.size() != 1) {
				problemReporter.reportProblem(new DocumentModelConsistencyProblem(
						modelName,
						group,
						DocumentModelCategory.DOCUMENT_MODEL_MULTI_SELECT_REQUIRED_FIELD,
						Severity.ERROR,
						group.getName()));
			} else {
				IField field = fieldsInGroup.iterator().next();
				checkFieldProperties(group, field);
			}
		}

		private void checkFieldProperties(IGroup group, IField field) {
			checkRequiredFlag(group, field);
			checkNoIndexField(group, field);
			checkFieldType(group, field);
		}

		private void checkRequiredFlag(IGroup group, IField field) {
			if (field.getRequirednessConfig().isEmpty()) {
				problemReporter.reportProblem(new DocumentModelConsistencyProblem(
						modelName,
						field,
						DocumentModelCategory.DOCUMENT_MODEL_MULTI_SELECT_FIELD_REQUIRED_FLAG,
						Severity.ERROR,
						field.getName(),
						group.getName()));
			}
		}

		private void checkNoIndexField(IGroup group, IField field) {
			boolean isFieldDefinedAsIndexField = group.getIndexField()
					.flatMap(IDocumentModelObjectReference::getDocumentModelObject)
					.map(f -> f.equals(field))
					.orElse(false);
			if (isFieldDefinedAsIndexField) {
				problemReporter.reportProblem(new DocumentModelConsistencyProblem(
						modelName,
						field,
						DocumentModelCategory.DOCUMENT_MODEL_MULTI_SELECT_FIELD_INDEX_FIELD,
						Severity.ERROR,
						field.getName(),
						group.getName()));
			}
		}

		private void checkFieldType(IGroup group, IField field) {
			boolean isEnumeration = field.getEffectiveType().map(IEnumerationType.class::isInstance).orElse(false);
			boolean isString = field.getEffectiveType().map(IStringType.class::isInstance).orElse(false);
			if (!isEnumeration && !isString) {
				problemReporter.reportProblem(new DocumentModelConsistencyProblem(
						modelName,
						field,
						DocumentModelCategory.DOCUMENT_MODEL_MULTI_SELECT_FIELD_FIELDTYPE,
						Severity.ERROR,
						field.getName(),
						group.getName()));
			}
			// at the moment, no other checks are needed
		}

		private void checkAllowedElementsInGroup(IGroup group) {
			boolean otherElementsPresent = group.getElements().stream()
					.anyMatch(e -> !(e instanceof IField) && !(e instanceof IRule));
			if (otherElementsPresent) {
				problemReporter.reportProblem(new DocumentModelConsistencyProblem(
						modelName,
						group,
						DocumentModelCategory.DOCUMENT_MODEL_MULTI_SELECT_OTHER_ELEMENTS,
						Severity.ERROR,
						group.getName()));
			}
		}
	}
}
