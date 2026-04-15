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

package com.mgmtp.a12.kernel.md.model.internal.services.join.addition;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition.IAdditionOrigins;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;

public class DocumentModelInfo4AdditionExtractor extends DocumentModelVisitor {

	public static void extractRootInfo(DocumentModel rootModel, IAdditionOrigins additionOrigins) {
		var walker = new com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker();
		var visitor = new DocumentModelInfo4AdditionExtractor(additionOrigins, additionOrigins::addRootElement);
		walker.acceptDocumentModel(rootModel, visitor);
	}

	public static void extractElementsInAdditive(Group group, IAdditionOrigins additionOrigins,
			Map<String, String> origAddIdByModifiedAddId) {
		var walker = new com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker();
		var visitor = new DocumentModelInfo4AdditionExtractor(additionOrigins, additionOrigins::addAdditiveElement,
				origAddIdByModifiedAddId);
		walker.acceptElements(List.of(group), visitor);
	}

	private final IAdditionOrigins additionOrigins;
	private final Consumer<String> elementAdder;
	private final Map<String, String> origAddIdByModifiedAddId;

	private DocumentModelInfo4AdditionExtractor(IAdditionOrigins additionOrigins, Consumer<String> elementAdder) {
		this(additionOrigins, elementAdder, null);
	}

	private DocumentModelInfo4AdditionExtractor(IAdditionOrigins additionOrigins, Consumer<String> elementAdder,
			Map<String, String> origAddIdByModifiedAddId) {
		this.additionOrigins = additionOrigins;
		this.elementAdder = elementAdder;
		this.origAddIdByModifiedAddId = origAddIdByModifiedAddId;
	}

	@Override public DocumentModelWalker.VisitProcess visitFieldTypeDefinition(FieldTypeDefinition fieldTypeDefinition) {
		additionOrigins.addRootTypeDef(fieldTypeDefinition.getId());
		return super.visitFieldTypeDefinition(fieldTypeDefinition);
	}

	@Override public DocumentModelWalker.VisitProcess visitElement(Element element) {
		elementAdder.accept(element.getId());
		if (origAddIdByModifiedAddId != null) {
			AdditiveModelJoiner.addJoinedIdToAdditiveIdMapping(origAddIdByModifiedAddId, element.getId(), additionOrigins);
		}
		return super.visitElement(element);
	}
}
