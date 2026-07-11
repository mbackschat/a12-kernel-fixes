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

package com.mgmtp.a12.kernel.md.model.a12internal.services;

import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelSearchServiceImpl;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.ElementWrapper;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * This service offers the search functionality in {@link DocumentModel}s.
 * <p>
 * <b>Attention:</b> This class needs to be re-instantiated whenever the {@link DocumentModel} is
 * changed because the content of the {@link DocumentModel} is cached internally.
 */
@OnlyForUsage
public class DocumentModelSearchService {

	private final IDocumentModelSearchService extSearchService;

	public DocumentModelSearchService(DocumentModel documentModel) {
		IDocumentModel extDocumentModel = new DocumentModelService().convertToExternal(documentModel);
		this.extSearchService = new DocumentModelSearchServiceImpl(extDocumentModel);
	}

	/**
	 * <b>Note:</b> the search by path (see {@link #getByPath(String)}) only works for {@code paths}
	 * relative to the oldest ancestor of the given {@link Element}s (for which the parent is
	 * {@code null}).
	 *
	 * @param elements
	 * 		list of {@link Element}s in which one wants to search. May not be
	 *        {@code null} or
	 * 		empty.
	 */
	public DocumentModelSearchService(List<Element> elements) {
		List<IElement> extElements = ElementUtils.convert(elements);
		this.extSearchService = new DocumentModelSearchServiceImpl(extElements);
	}

	/**
	 * Searches for an {@link Element} with the given id.
	 *
	 * @param id
	 * 		element id. May not be {@code null} or empty.
	 * @return the corresponding {@link Element}.
	 */
	public Optional<Element> getById(String id) {
		Optional<IElement> extElement = extSearchService.getById(id);
		return extElement.map(ElementWrapper.class::cast).map(ElementWrapper::getElement);
	}

	/**
	 * Searches for an {@link Element} with the given path.
	 *
	 * @param path
	 * 		element path. May not be {@code null} or empty.
	 * @return the corresponding {@link Element}.
	 */
	public Optional<Element> getByPath(String path) {
		Optional<IElement> extElement = extSearchService.getByPath(path);
		return extElement.map(ElementWrapper.class::cast).map(ElementWrapper::getElement);
	}

}
