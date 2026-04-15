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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.IEntity;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;

public class EntityCollectorVisitor extends DocumentModelVisitor {

	private Map<String, IEntity> entitiesByFullName = new HashMap<>();
	private Map<String, FieldTypeDefinition> fieldTypeDefinitionsById = new HashMap<>();
	private List<Group> collectedGroupsWithIncludeDetails = new ArrayList<>();

	public List<Group> getCollectedGroupsWithIncludeDetails() {
		return collectedGroupsWithIncludeDetails;
	}

	public Map<String, IEntity> getEntitiesByFullName() {
		return entitiesByFullName;
	}

	public Map<String, FieldTypeDefinition> getFieldTypeDefinitionsById() {
		return fieldTypeDefinitionsById;
	}

	@Override
	public VisitProcess visitRule(final Rule rule) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitComputation(final Computation computation) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitField(final Field field) {
		entitiesByFullName.put(ElementUtils.getPath(field), field);
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitGroup(final Group group) {
		entitiesByFullName.put(ElementUtils.getPath(group), group);

		group.getIncludeDetails().ifPresent(includeDetail -> {
			if (includeDetail.getModelReference() != null
					&& includeDetail.getModelReference().getAlias() != null
					&& !includeDetail.getModelReference().getAlias().isEmpty()
					&& includeDetail.getModelReference().getModelType() == null
					&& includeDetail.getModelReference().getPurpose() == null
					&& includeDetail.getModelReference().getReference() == null) {
				collectedGroupsWithIncludeDetails.add(group);
			}
		});
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitFieldTypeDefinition(final FieldTypeDefinition fieldTypeDefinition) {
		fieldTypeDefinitionsById.put(fieldTypeDefinition.getId(), fieldTypeDefinition);
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

}
