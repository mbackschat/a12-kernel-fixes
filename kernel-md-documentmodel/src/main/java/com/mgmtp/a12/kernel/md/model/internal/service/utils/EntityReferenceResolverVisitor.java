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

package com.mgmtp.a12.kernel.md.model.internal.service.utils;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.IDocumentModelObjectReference;
import com.mgmtp.a12.kernel.md.model.a12internal.IEntity;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandError;

import lombok.NonNull;

public class EntityReferenceResolverVisitor extends DocumentModelVisitor {

	protected Map<String, FieldTypeDefinition> fieldTypeDefinitionsById;
	private final Map<String, IEntity> entitiesByFullName;
	private final IProblemReporter problemReporter;

	public EntityReferenceResolverVisitor(Map<String, IEntity> entitiesByFullName,
			Map<String, FieldTypeDefinition> fieldTypeDefinitionsById, IProblemReporter problemReporter) {
		this.entitiesByFullName = entitiesByFullName;
		this.fieldTypeDefinitionsById = fieldTypeDefinitionsById;
		this.problemReporter = problemReporter;
	}

	@Override
	public VisitProcess visitField(Field field) {
		if (field.getFieldType() instanceof TypeDefType typeDefType) {
			if (StringUtils.isEmpty(typeDefType.getId())) {
				reportMissingReference(field, "type definition");
			} else {
				FieldTypeDefinition entity = fieldTypeDefinitionsById.get(typeDefType.getId());
				if (entity == null) {
					reportUnresolvableTypeReference(typeDefType.getId(), field);
				}
				// note: intentionally sets the referenced td to null/empty if it could not be resolved
				typeDefType.setTypeDefinition(entity);
			}
		}
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitRule(final Rule rule) {
		addDocumentModelObjectToRef(rule.getErrorEntity(), rule, rule, IEntity.class, "error entity");
		rule.getFilterForPossiblyCausingEntities().ifPresent(filterForPossiblyCausingEntities -> {
			filterForPossiblyCausingEntities.getEntitiesEnabledAsPossibleCauseIfFilled().forEach(dmo ->
					addDocumentModelObjectToRef(dmo, rule, rule.getParent(), IEntity.class,
							"entities enabled as possible cause if filled"));
			filterForPossiblyCausingEntities.getEntitiesEnabledAsPossibleCauseIfNotFilled().forEach(dmo ->
					addDocumentModelObjectToRef(dmo, rule, rule.getParent(), IEntity.class,
							"entities enabled as possible cause if not filled"));
		});
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@SuppressWarnings("unchecked")
	private <T> void addDocumentModelObjectToRef(
			IDocumentModelObjectReference<T> entityRef,
			@NonNull Element referenceHolder,
			@NonNull Element referenceBase,
			@NonNull Class<T> classOfReferencedEntity,
			@NonNull String origin
	) {
		if (entityRef == null) { // This is an error case that breaks some @NonNull promises (e.g. Computation#computedField).
			reportMissingReference(referenceHolder, origin);
			return;
		}
		String fullNameOfReferencedEntity = ElementUtils.resolveRelativePath(referenceBase, entityRef.getRelativePath());
		IEntity entity = entitiesByFullName.get(fullNameOfReferencedEntity);
		if (entity == null) {
			reportUnresolvableReference(entityRef.getRelativePath(), referenceHolder, origin);
		} else if (!classOfReferencedEntity.isAssignableFrom(entity.getClass())) {
			reportIncompatibleReference(entity, referenceHolder, classOfReferencedEntity, origin);
			entity = null;
		}
		// note: intentionally sets the referenced object to null if it could not be resolved
		entityRef.setDocumentModelObject((T) entity);
	}

	@Override
	public VisitProcess visitComputation(final Computation computation) {
		addDocumentModelObjectToRef(computation.getComputedField(), computation, computation, Field.class, "computed field");
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitGroup(final Group group) {
		group.getIndexField().ifPresent(indexField ->
				addDocumentModelObjectToRef(indexField, group, group, Field.class, "index field"));
		group.getSortFields().forEach(sortField ->
				addDocumentModelObjectToRef(sortField.getField(), group, group, Field.class, "sort field"));
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	private void reportUnresolvableReference(String relativePath, Element referenceHolder, String origin) {
		String message = DocumentModelExpandError.UNRESOLVABLE_REFERENCE_MSG.getMessage(origin, "relativePath=" + relativePath);
		problemReporter.reportProblem(new MVKCoreError(message, referenceHolder));
	}

	private void reportUnresolvableTypeReference(String id, Element baseElement) {
		String message = DocumentModelExpandError.UNRESOLVABLE_REFERENCE_MSG.getMessage("type definition", "id=" + id);
		problemReporter.reportProblem(new MVKCoreError(message, baseElement));
	}

	private void reportMissingReference(Element element, String origin) {
		String message = DocumentModelExpandError.MISSING_REFERENCE_MSG.getMessage(origin, ElementUtils.getPath(element));
		problemReporter.reportProblem(new MVKCoreError(message, element));
	}

	private void reportIncompatibleReference(IEntity referencedEntity, Element referenceHolder,
			Class<?> expectedClassOfReferencedEntity, String origin) {
		String message = DocumentModelExpandError.INCOMPATIBLE_REFERENCE_MSG.getMessage(
				origin, ElementUtils.getPath(referenceHolder), expectedClassOfReferencedEntity.getSimpleName(),
				referencedEntity.getClass().getSimpleName());
		problemReporter.reportProblem(new MVKCoreError(message, referenceHolder));
	}
}
