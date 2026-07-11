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
package com.mgmtp.a12.kernel.md.model.internal.services.join.util;

import java.util.Optional;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;

/**
 * Helper class containing utility methods for {@link DocumentModelVisitor}s.
 */
public class DocumentModelVisitorUtil {

	private final DocumentModelSearchService resultModelSearchService;
	private final DocumentModel resultModel;

	public DocumentModelVisitorUtil(DocumentModel resultModel) {
		this.resultModelSearchService = new DocumentModelSearchService(resultModel);
		this.resultModel = resultModel;
	}

	public Optional<Element> getResultElement(String path) {
		return resultModelSearchService.getByPath(path);
	}

	public Group getResultParentGroup(String path) {
		String parentGroupPath = ElementPathUtils.getParentPath(path);
		Optional<Element> resultParentGroupOpt = resultModelSearchService.getByPath(parentGroupPath);
		return resultParentGroupOpt.map(Group.class::cast)
				.orElseGet(() -> resultModel.getContent().getModelRoot());
	}

	public void removeElementFromResultModel(Element element) {
		String path = ElementUtils.getPath(element);
		Group resultParentGroup = this.getResultParentGroup(path);
		Optional<Element> elementInResultModel =
				resultParentGroup.getElements().stream().filter(e -> e.getId().equals(element.getId())).findFirst();
		elementInResultModel.ifPresent(resultParentGroup::removeElement);
	}
}
