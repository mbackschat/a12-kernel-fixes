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

package com.mgmtp.a12.kernel.md.serializer.model.internal.service.consistency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblem;
import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.IIdNamed;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType.IEnumCategory;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType.IEnumValue;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ITypeDefType;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.LocalizedTextMapVisitor;
import com.mgmtp.a12.model.consistency.Problem;
import com.mgmtp.a12.model.notification.Severity;

public class BasicConsistencyCheck extends DocumentModelVisitor {
	private final DocumentModelConsistencyProblemReporter problemReporter;
	private final String modelId;

	private BasicConsistencyCheck(String modelId, DocumentModelConsistencyProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
		this.modelId = modelId;
	}

	public static List<Problem> checkModel(IDocumentModel model) {
		List<DocumentModelConsistencyProblem> problems = new ArrayList<>();
		checkModelInternal(model, problems::add);
		return new ArrayList<>(problems);
	}

	public static void checkModel(IDocumentModel model, IProblemReporter pr) {
		checkModelInternal(model, DocumentModelConsistencyProblemReporter.fromIProblemReporter(pr));
	}

	private static void checkModelInternal(IDocumentModel model, DocumentModelConsistencyProblemReporter pr) {
		BasicConsistencyCheck visitor = new BasicConsistencyCheck(model.getHeader().getId(), pr);
		new DocumentModelWalker().acceptDocumentModel(model, visitor);
		LocalizedTextMapVisitor.visitLocalizedTextMaps(model, visitor::checkLocalizedTextMap);
	}

	public static void checkFieldTypeDefinitions(IDocumentModel model, Collection<IFieldTypeDefinition> fieldTypeDefinitions,
			IProblemReporter pr) {
		BasicConsistencyCheck visitor = new BasicConsistencyCheck(model.getHeader().getId(),
				DocumentModelConsistencyProblemReporter.fromIProblemReporter(pr));
		fieldTypeDefinitions.forEach(visitor::visitFieldTypeDefinition);
	}

	public static void checkElements(IDocumentModel model, Collection<IElement> elements, IProblemReporter pr) {
		BasicConsistencyCheck visitor = new BasicConsistencyCheck(model.getHeader().getId(),
				DocumentModelConsistencyProblemReporter.fromIProblemReporter(pr));
		new DocumentModelWalker().acceptElements(elements, visitor);
	}

	private void checkLocalizedTextMap(ILocalizedTextMap text, Object origin, String property) {
		text.forEach((l, t) -> {
			if (t == null) {
				addLocalizedTextError(origin, property, l);
			}
		});
	}

	private void addLocalizedTextError(Object source, String property, Locale locale) {
		String id;
		BasicDocumentModelConsistencyCategory category;
		if (source instanceof IElement) {
			category = BasicDocumentModelConsistencyCategory.LOCALIZED_TEXT_ELEMENT_NULL_PROBLEM;
			id = ((IIdNamed) source).getId();
			problemReporter.reportProblem(
					new DocumentModelConsistencyProblem(modelId, source, category, Severity.ERROR, id, property,
							locale.toString()));
		} else if (source instanceof IFieldTypeDefinition) {
			category = BasicDocumentModelConsistencyCategory.LOCALIZED_TEXT_TYPEDEF_NULL_PROBLEM;
			id = ((IIdNamed) source).getId();
			problemReporter.reportProblem(
					new DocumentModelConsistencyProblem(modelId, source, category, Severity.ERROR, id, property,
							locale.toString()));
		} else {
			throw new IllegalArgumentException("Unknown source for localized text: " + source.getClass().getName());
		}
	}

	@Override
	public VisitProcess visitFieldTypeDefinition(IFieldTypeDefinition fieldTypeDefinition) {
		if (StringUtils.isEmpty(fieldTypeDefinition.getId())) {
			problemReporter.reportProblem(new DocumentModelConsistencyProblem(modelId, fieldTypeDefinition,
					BasicDocumentModelConsistencyCategory.TYPEDEF_ID_EMPTY, Severity.ERROR,
					fieldTypeDefinition.getName()));
		}
		if (StringUtils.isEmpty(fieldTypeDefinition.getName())) {
			problemReporter.reportProblem(new DocumentModelConsistencyProblem(modelId, fieldTypeDefinition,
					BasicDocumentModelConsistencyCategory.TYPEDEF_NAME_EMPTY, Severity.ERROR,
					fieldTypeDefinition.getId()));
		}
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitElement(IElement element) {
		if (StringUtils.isEmpty(element.getId())) {
			problemReporter.reportProblem(
					new DocumentModelConsistencyProblem(modelId, element, BasicDocumentModelConsistencyCategory.ID_EMPTY,
							Severity.ERROR,
							ElementUtils.getPath(element)));
		}
		if (StringUtils.isEmpty(element.getName())) {
			problemReporter.reportProblem(
					new DocumentModelConsistencyProblem(modelId, element, BasicDocumentModelConsistencyCategory.NAME_EMPTY,
							Severity.ERROR, element.getId()));
		}
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitField(IField field) {
		if (field.getFieldType() instanceof IEnumerationType) {
			checkEnumerationType((IEnumerationType) field.getFieldType(), field);
		}
		if (field.getFieldType() instanceof ITypeDefType) {
			checkTypeDefType((ITypeDefType) field.getFieldType(), field);
		}
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	private void checkTypeDefType(ITypeDefType fieldType, IField field) {
		if (StringUtils.isEmpty(fieldType.getId())) {
			problemReporter.reportProblem(new DocumentModelConsistencyProblem(modelId, field,
					BasicDocumentModelConsistencyCategory.TYPEDEF_TYPE_REF_EMPTY, Severity.ERROR,
					field.getId()));
		}
	}

	private void checkEnumerationType(IEnumerationType enumType, IField field) {
		Map<String, Set<IEnumValue>> duplicateValues = Java8Utils.findDuplicates(enumType.getValues().stream(),
				IEnumValue::getValue);
		if (!duplicateValues.isEmpty()) {
			duplicateValues.forEach((v, ev) -> problemReporter.reportProblem(
					new DocumentModelConsistencyProblem(modelId, field,
							BasicDocumentModelConsistencyCategory.ENUM_TYPE_DUPLICATE_VALUE, Severity.ERROR,
							field.getId(), v)));
		}
		Map<String, Set<IEnumCategory>> duplicateCategories = Java8Utils.findDuplicates(enumType.getCategories().stream(),
				IEnumCategory::getName);
		if (!duplicateCategories.isEmpty()) {
			duplicateCategories.forEach((v, ev) -> problemReporter.reportProblem(
					new DocumentModelConsistencyProblem(modelId, field,
							BasicDocumentModelConsistencyCategory.ENUM_TYPE_DUPLICATE_CATEGORIES, Severity.ERROR,
							field.getId(), v)));
		}
	}

	@Override
	public VisitProcess visitRule(IRule rule) {
		if (StringUtils.isEmpty(rule.getErrorCode())) {
			problemReporter.reportProblem(
					new DocumentModelConsistencyProblem(modelId, rule, BasicDocumentModelConsistencyCategory.ERROR_CODE_EMPTY,
							Severity.ERROR, rule.getId()));
		}
		if (StringUtils.isEmpty(rule.getErrorCondition())) {
			problemReporter.reportProblem(
					new DocumentModelConsistencyProblem(modelId, rule, BasicDocumentModelConsistencyCategory.ERROR_CONDITION_EMPTY,
							Severity.ERROR,
							rule.getId()));
		}
		if (StringUtils.isEmpty(rule.getErrorEntity().getRelativePath())) {
			problemReporter.reportProblem(
					new DocumentModelConsistencyProblem(modelId, rule, BasicDocumentModelConsistencyCategory.ERROR_ENTITY_EMPTY,
							Severity.ERROR, rule.getId()));
		}
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitComputation(IComputation computation) {
		if (StringUtils.isEmpty(computation.getComputedField().getRelativePath())) {
			problemReporter.reportProblem(new DocumentModelConsistencyProblem(modelId, computation,
					BasicDocumentModelConsistencyCategory.COMPUTED_FIELD_EMPTY, Severity.ERROR,
					computation.getId()));
		}
		computation.getComputationAlternatives().stream()
				.filter(a -> StringUtils.isEmpty(a.getOperation()))
				.findAny()
				.ifPresent(a -> problemReporter.reportProblem(
						new DocumentModelConsistencyProblem(modelId, computation,
								BasicDocumentModelConsistencyCategory.COMPUTATION_OPERATION_EMPTY, Severity.ERROR,
								computation.getId())));

		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitGroup(IGroup group) {
		group.getIndexField().ifPresent(i -> {
			if (StringUtils.isEmpty(i.getRelativePath())) {
				problemReporter.reportProblem(new DocumentModelConsistencyProblem(modelId, group,
						BasicDocumentModelConsistencyCategory.INDEX_FIELD_NAME_EMPTY, Severity.ERROR,
						group.getId()));
			}
		});
		group.getSortFields().stream()
				.filter(f -> StringUtils.isEmpty(f.getField().getRelativePath()))
				.findAny()
				.ifPresent(f -> problemReporter.reportProblem(
						new DocumentModelConsistencyProblem(modelId, group,
								BasicDocumentModelConsistencyCategory.SORT_FIELD_NAME_EMPTY, Severity.ERROR,
								group.getId())));
		return VisitProcess.CONTINUE_TRAVERSAL;
	}
}
