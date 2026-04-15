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
package com.mgmtp.a12.kernel.md.model.internal.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Group.IncludeDetails;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.ModelReference;

/**
 * A visitor that replaces included contents by their include elements.
 */
public class DocumentModelCollapseService {

	private final DocumentModel documentModel;
	private final Set<FieldTypeDefinition> typeDefsToRemove;

	public static void collapse(DocumentModel documentModel) {
		new DocumentModelCollapseService(documentModel).collapseInternal();
	}

	private DocumentModelCollapseService(final DocumentModel documentModel) {
		this.documentModel = documentModel;
		this.typeDefsToRemove = new HashSet<>();
	}

	private void collapseInternal() {
		DocumentModelWalker walker = new DocumentModelWalker();
		walker.acceptDocumentModel(documentModel, new IncludeCollapseVisitor());
		documentModel.getContent().getTypeDefinitions().removeAll(typeDefsToRemove);

		// release references to previously included entities/type-definitions to prevent memory leaks
		// the references coming from the includes cannot be resolved anymore and will be set to null
		DocumentModelUtils.resolveAndUpdateReferences(documentModel, new ProblemReporter());
	}

	/**
	 * A visitor that replaces included contents by their include elements.
	 */
	private class IncludeCollapseVisitor extends DocumentModelVisitor {

		@Override public VisitProcess visitHeader(Header header) {
			Set<ModelReference> modelReferencesToRemove = header.getModelReferences().stream()
					.filter(r -> DocumentModelExpandService.TRANSITIVE_INCLUDE.equals(r.getPurpose()))
					.collect(Collectors.toUnmodifiableSet());
			header.getModelReferences().removeAll(modelReferencesToRemove);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitGroup(final Group group) {
			final Optional<IncludeDetails> optionalIncludeDetails = group.getIncludeDetails();
			if (optionalIncludeDetails.isPresent()) {
				// remove everything that is contained in this group
				List<Element> elementsToRemove = new ArrayList<>(group.getElements());
				elementsToRemove.forEach(group::removeElement);
				// unset usageType again
				group.setUsageType(null);
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitFieldTypeDefinition(final FieldTypeDefinition fieldTypeDefinition) {
			final Set<List<ModelReference>> modelRefPaths = fieldTypeDefinition.getAllModelReferencePaths();
			if (!modelRefPaths.isEmpty()) {
				typeDefsToRemove.add(fieldTypeDefinition);
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}
	}
}
