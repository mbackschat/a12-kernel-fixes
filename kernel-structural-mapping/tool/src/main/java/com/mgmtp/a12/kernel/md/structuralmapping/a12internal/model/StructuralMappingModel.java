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

package com.mgmtp.a12.kernel.md.structuralmapping.a12internal.model;

import java.io.Serial;
import java.io.Serializable;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ModelHeaderUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.SerializableDocument;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingMetaModelService;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

@OnlyForUsage
public class StructuralMappingModel extends SerializableDocument implements Model, Serializable {
	// NOTE no additional attributes allowed (subclass of SerializableDocument)

	@Serial @NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	private StructuralMappingModel(@NonNull MM_StructuralMappingModel_1 smm) {
		super(smm._unwrap());
	}

	public static StructuralMappingModel of(@NonNull MM_StructuralMappingModel_1 smm) {
		return new StructuralMappingModel(smm);
	}

	public MM_StructuralMappingModel_1 typedDocument() {
		return MM_StructuralMappingModel_1._viewOf(document);
	}

	public Header getHeader() {
		return typedDocument().header() != null
				? ModelHeaderUtils.fromGroupInstance(typedDocument().header()._unwrap())
				: null;
	}

	/**
	 * @return a copy of the receiver with the header set to the provided {@code header}
	 */
	public StructuralMappingModel withHeader(@NonNull Header header) {
		GroupInstanceV2 groupInstance = ModelHeaderUtils.toGroupInstance(header);
		MM_StructuralMappingModel_1 updated = typedDocument()
				._with(MM_StructuralMappingModel_1._pointer().header(),
						com.mgmtp.a12.kernel.mmtypings.ct_model_header_1.views._ct_model_header_1.Header._viewOf(
								groupInstance));
		return of(updated);
	}

	@Override
	public StructuralMappingModelContent getContent() {
		return new StructuralMappingModelContent(typedDocument().content());
	}

	@Override
	protected String getDocumentModelId() {
		return StructuralMappingMetaModelService.documentModel().getHeader().getId();
	}

	@Override
	protected IDocumentV2Serializer getDocumentSerializer() {
		return StructuralMappingMetaModelService.documentSerializer();
	}
}
