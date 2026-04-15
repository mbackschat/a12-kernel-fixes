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

package com.mgmtp.a12.kernel.mapping.rt.api;

import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * This class encapsulates {@link DocumentV2} to add meta information.
 */
@OnlyForUsage // custom impls could in principle make sense, but then
// it would be trickier to extend the interface in the future without
// a breaking change - and users probably don't need custom implementations
// because they can use the static factory method ISourceDocument.of(...)
// to obtain an instance.
// similar to com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.SourceData
public interface SourceDocument {
	/**
	 * The name of the source this document belongs to, as defined in the mapping model.
	 * <p>
	 * Note: Different source names in the mapping model might reference the same Document Model (e.g. in a mapping model a modeller
	 * might specify the source names 'InvoiceAddress' and 'ShippingAddress' that both reference the same document model 'Address').
	 **/
	@NonNull String sourceName();

	/**
	 * An id that will be returned in {@link SourceField#sourceInstanceId()}. This can be used to the identify the source.
	 * {@link DocumentV2#getId()} can be used if it is actually {@link Optional#isPresent()} and unique within the list in
	 * {@link IMappingService#transferData(List, DocumentV2, DocumentProcessingConfig)}.
	 **/
	@NonNull String sourceInstanceId();

	@NonNull DocumentV2 document();

	static @NonNull SourceDocument of(
			@NonNull String sourceName,
			@NonNull String sourceInstanceId,
			@NonNull DocumentV2 document
	) {
		return new SourceDocumentImpl(sourceName, sourceInstanceId, document);
	}
}
