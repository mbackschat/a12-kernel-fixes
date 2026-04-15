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
package com.mgmtp.a12.kernel.md.model.internal.services.join.decoration;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;

public class GroupAdjusterVisitor extends DocumentModelVisitor {
	private final Map<String, Element> newFullNamesToElement;
	private final DocumentModelService documentModelService = new DocumentModelService();

	public GroupAdjusterVisitor(Map<String, EntityFullName> formerToNewFullNamesMapper) {
		this.newFullNamesToElement = formerToNewFullNamesMapper.values().stream()
				.collect(Collectors.toMap(EntityFullName::getFullName, EntityFullName::getElement));
	}

	@Override
	public VisitProcess visitGroup(final Group group) {
		group.getSortFields().forEach(sf -> sf.getField().getDocumentModelObject()
						.map(documentModelService::getPath)
						.map(newFullNamesToElement::get)
						.map(e -> assertInstanceOf(e, Field.class))
						.filter(Field.class::isInstance)
						.map(Field.class::cast)
						.ifPresent(field -> sf.getField().setRelativePath(field.getName())));
		group.getIndexField().ifPresent(dmor ->
				assertPresent(dmor.getDocumentModelObject())
						.map(documentModelService::getPath)
						.map(newFullNamesToElement::get)
						.map(e -> assertInstanceOf(e, Field.class))
						.filter(Field.class::isInstance)
						.map(Field.class::cast)
						.ifPresent(field -> dmor.setRelativePath(field.getName())));
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	private <T> Optional<T> assertPresent(Optional<T> o) {
		assert o.isPresent();
		return o;
	}

	private <T, C> T assertInstanceOf(T e, Class<C> c) {
		assert c.isInstance(e) : "expected an object that is instance of " + c + " but got " + e.getClass();
		return e;
	}
}
