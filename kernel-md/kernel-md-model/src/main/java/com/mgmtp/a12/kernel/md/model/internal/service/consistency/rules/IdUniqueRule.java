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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblem;
import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelCategory;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelConsistencyRule;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.model.notification.Severity;

public class IdUniqueRule extends DocumentModelConsistencyRule {

	private static class NonUniqueIdCollector extends DocumentModelVisitor {
		Map<String, IElement> ids = new HashMap<>();
		Map<String, List<IElement>> duplicateIds = new HashMap<>();

		@Override
		public VisitProcess visitElement(IElement element) {
			IElement oldElement = ids.put(element.getId(), element);
			if (oldElement != null) {
				getDuplicateIds()
						// Add the old element once
						.computeIfAbsent(element.getId(), k -> new ArrayList<>(Collections.singleton(oldElement)))
						.add(element);
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		public Map<String, List<IElement>> getDuplicateIds() {
			return duplicateIds;
		}
	}

	@Override
	public void checkModel(final IDocumentModel documentModel, DocumentModelConsistencyProblemReporter pr) {
		NonUniqueIdCollector collector = new NonUniqueIdCollector();
		new DocumentModelWalker().acceptDocumentModel(documentModel, collector);
		Map<String, List<IElement>> duplicates = collector.getDuplicateIds();
		duplicates.forEach((id, elements) -> {
			String elementPaths = elements.stream()
					.map(ElementUtils::getPath)
					.collect(Collectors.joining(", "));
			String modelId = documentModel.getHeader().getId();
			elements.forEach(element ->
					pr.reportProblem(createProblem(element, elementPaths, modelId)));
		});
	}

	private DocumentModelConsistencyProblem createProblem(IElement element, String elementPaths, String modelId) {
		return new DocumentModelConsistencyProblem(
				modelId,
				element,
				DocumentModelCategory.DOCUMENT_MODEL_ID_NOT_UNIQUE,
				Severity.ERROR,
				element.getId(),
				ElementUtils.getPath(element),
				elementPaths);
	}

}
