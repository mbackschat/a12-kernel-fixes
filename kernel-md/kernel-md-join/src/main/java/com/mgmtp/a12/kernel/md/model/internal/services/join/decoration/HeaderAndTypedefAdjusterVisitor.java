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

import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelJoinUtil;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.SerializationUtils;
import com.mgmtp.a12.model.header.Header;

public class HeaderAndTypedefAdjusterVisitor extends DocumentModelVisitor {
	private final Set<String> typeDefinitionIdsRoot;
	private final DocumentModel resultModel;

	public HeaderAndTypedefAdjusterVisitor(DocumentModel resultModel) {
		this.resultModel = resultModel;
		this.typeDefinitionIdsRoot =
				resultModel.getContent().getTypeDefinitions().stream().map(FieldTypeDefinition::getId).collect(Collectors.toSet());
	}

	@Override
	public VisitProcess visitFieldTypeDefinition(FieldTypeDefinition fieldTypeDefinition) {
		if (!typeDefinitionIdsRoot.contains(fieldTypeDefinition.getId())) {
			resultModel.getContent().getTypeDefinitions().add(SerializationUtils.copy(fieldTypeDefinition));
		}
		return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
	}

	@Override
	public VisitProcess visitHeader(Header header) {
		DocumentModelJoinUtil.mergeModelReferences(resultModel, header);
		return VisitProcess.CONTINUE_TRAVERSAL;
	}
}
