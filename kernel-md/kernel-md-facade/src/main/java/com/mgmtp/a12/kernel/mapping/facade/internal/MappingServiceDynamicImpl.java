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

package com.mgmtp.a12.kernel.mapping.facade.internal;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.mgmtp.a12.kernel.mapping.rt.a12internal.IMappingServiceA12Internal;
import com.mgmtp.a12.kernel.mapping.rt.api.MappingResult;
import com.mgmtp.a12.kernel.mapping.rt.api.SourceDocument;
import com.mgmtp.a12.kernel.mapping.service.internal.DataTransferModelGroovyCode;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.rt.a12internal.service.datatransfer.DataTransferServiceAdapter;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MappingServiceDynamicImpl implements IMappingServiceA12Internal, Serializable {
	private final @NonNull DataTransferModelGroovyCode modelCode;
	private transient DataTransferServiceAdapter adapter;

	@Serial @NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	public synchronized DataTransferServiceAdapter getOrCompileAdapter() {
		// since adapter is transient and may get lost after deserialization, we can never assume it to be non-null
		if (adapter == null) {
			adapter = DataTransferServiceAdapter.wrap(modelCode.getOrCompileDataTransferService(),
					modelCode.getOrCompileTargetMetaModel());
		}
		return adapter;
	}

	@Override
	public @NonNull MappingResult transferData(@NonNull List<SourceDocument> sourceDocuments,
			@NonNull DocumentV2 targetDocument, @NonNull DocumentProcessingConfig config) {
		return getOrCompileAdapter().transferData(sourceDocuments, targetDocument, config);
	}

	@Override public @NonNull ISortedSourceDocumentResult sortSourceDocument(@NonNull SourceDocument sourceDocument,
			@NonNull DocumentProcessingConfig config) {
		return getOrCompileAdapter().sortSourceDocument(sourceDocument, config);
	}
}
