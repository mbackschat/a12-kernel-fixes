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

package com.mgmtp.a12.kernel.mapping.rt.a12internal;

import java.util.List;

import com.mgmtp.a12.kernel.mapping.rt.api.IMappingService;
import com.mgmtp.a12.kernel.mapping.rt.api.SourceDocument;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;

import lombok.NonNull;

public interface IMappingServiceA12Internal extends IMappingService {

	/**
	 * The sorting result which always has a sourceDocument result and may also have messages generated during sorting.
	 */
	interface ISortedSourceDocumentResult {
		/**
		 * @return the sorted source document or the original document if the sorting was not successful.
		 */
		@NonNull SourceDocument sourceDocumentResult();

		/**
		 * @return the messages during sorting.
		 */
		@NonNull List<IMessage> messages();

		static @NonNull ISortedSourceDocumentResult of(
				@NonNull SourceDocument sourceDocument,
				@NonNull List<IMessage> errorMessages
		) {
			return new SortedSourceDocumentResultImpl(sourceDocument, errorMessages);
		}
	}

	/**
	 * Sort a source document according to the sorting information specified in the source model.
	 * The provided source document has to be formally valid. If wrong values are encountered during sorting, the error messages are
	 * returned in {@link ISortedSourceDocumentResult#messages()}.
	 *
	 * @param sourceDocument
	 * 		the source document to be sorted.
	 * @param config
	 * 		a configuration to supply required validation data.
	 * @return a {@link ISortedSourceDocumentResult} containing the sorted document and potential error messages. The result's
	 *        {@link SourceDocument#document()} may be unsorted if there were any messages in
	 *        {@link ISortedSourceDocumentResult#messages()}.
	 */
	@NonNull ISortedSourceDocumentResult sortSourceDocument(@NonNull SourceDocument sourceDocument,
			@NonNull DocumentProcessingConfig config);
}
