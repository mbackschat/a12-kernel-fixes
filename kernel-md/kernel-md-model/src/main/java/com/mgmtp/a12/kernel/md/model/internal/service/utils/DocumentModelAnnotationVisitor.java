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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.model.header.Header;

public class DocumentModelAnnotationVisitor extends DocumentModelVisitor {
	private String key;
	private final Map<String, String> entityPath2Value = new HashMap<>();
	private final DocumentModelService docModelService;

	public DocumentModelAnnotationVisitor(String antKey) {
		key = antKey;
		docModelService = new DocumentModelService();
	}

	@Override
	public VisitProcess visitHeader(final Header header) {
		CollectionUtils.emptyIfNull(header.getAnnotations()).stream()
				.filter(a -> a.getName().equals(key))
				.findFirst()
				.ifPresent(ant -> {
					var val = ant.getValue();
					entityPath2Value.put("", val == null ? "" : val);
				});
		return super.visitHeader(header);
	}

	@Override
	public VisitProcess visitElement(Element element) {
		CollectionUtils.emptyIfNull(element.getAnnotations()).stream()
				.filter(a -> a.getName().equals(key))
				.findFirst()
				.ifPresent(ant -> {
					var val = ant.getValue();
					entityPath2Value.put(docModelService.getPath(element), val == null ? "" : val);
				});
		return super.visitElement(element);
	}

	public Map<String, String> getAllAnnotations() {
		return entityPath2Value;
	}

}