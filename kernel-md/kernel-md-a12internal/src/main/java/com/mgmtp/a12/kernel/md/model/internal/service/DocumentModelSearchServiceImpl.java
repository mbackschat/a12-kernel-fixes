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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelVisitorWithFullNames;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;

import lombok.NonNull;

public class DocumentModelSearchServiceImpl implements IDocumentModelSearchService {

	private final Map<String, IElement> elementsByPath = new HashMap<>();
	private final Map<String, IElement> elementsById = new HashMap<>();

	/**
	 * For performance reasons, please get the instance rather from
	 * {@link IDocumentModelResolver#getDocumentModelSearchService(String)}.
	 *
	 * @param documentModel
	 */
	public DocumentModelSearchServiceImpl(@NonNull IDocumentModel documentModel) {
		DocumentModelSearchInfoCollector collector = new DocumentModelSearchInfoCollector();
		new DocumentModelWalker().acceptDocumentModel(documentModel, collector);
	}

	public DocumentModelSearchServiceImpl(@NonNull List<IElement> elements) {
		DocumentModelSearchInfoCollector collector = new DocumentModelSearchInfoCollector();
		new DocumentModelWalker().acceptElements(elements, collector);
	}

	@Override
	public Optional<IElement> getById(@NonNull String id) {
		return Optional.ofNullable(elementsById.get(id));
	}

	@Override
	public Optional<IElement> getByPath(@NonNull String path) {
		return Optional.ofNullable(elementsByPath.get(path));
	}

	@Override
	public Optional<String> getGroupOrFieldPathById(String id) {
		IElement element = elementsById.get(id);
		if (element instanceof IGroup || element instanceof IField) {
			return Optional.of(ElementUtils.getPath(element));
		} else {
			return Optional.empty();
		}
	}

	private class DocumentModelSearchInfoCollector extends DocumentModelVisitorWithFullNames {
		@Override
		public DocumentModelWalker.VisitProcess visitElement(@NonNull IElement element) {
			elementsById.put(element.getId(), element);
			elementsByPath.put(getFullName(), element);
			return super.visitElement(element);
		}
	}

}
