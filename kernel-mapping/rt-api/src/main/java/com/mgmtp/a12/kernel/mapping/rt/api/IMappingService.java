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

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Used to execute the modeled mapping.
 */
@OnlyForUsage
public interface IMappingService {

	/**
	 * Map data from the source documents into the target document according to the mapping models.
	 *
	 * @param sourceDocuments
	 * 		The source documents to be mapped into the target. Those documents are processed according to the ordering in the list,
	 * 		so that when there are multiple documents for the same {@link SourceDocument#sourceName()}, the repetitions for the
	 * 		document in that source will also be chosen based on this ordering (documents coming first will receive lower
	 * 		repetitions). This means that the order of this list can have an effect on the result of the mapping! May not be
	 *        {@code null}.
	 * 		Hint: a SourceDocument can be created via {@link SourceDocument#of(String, String, DocumentV2)}
	 * @param targetDocument
	 * 		The document which the data should be mapped into. This document is passed as a parameter because it could already contain
	 * 		values. May not be {@code null}. Note: The usual validation/computation code of the target model is loaded based on its
	 *        {@link DocumentV2#getDocumentModelId()}. If this model does not match the model specified in the mapping model, errors can
	 * 		occur - especially if the models are structurally different or if field types differ. This case is not caught, because if
	 * 		both models differ, for example, only through rules/computations or fields that is not mapped into, it can be
	 * 		used without problems.
	 * @param config
	 * 		The {@link DocumentProcessingConfig} including the locale in which error messages are returned. May not be {@code null}.
	 * @return The {@link MappingResult}. For detailed descriptions of the information this object provides, please refer to
	 * 		the JavaDoc of the returned class. Never {@code null}.
	 */
	@NonNull MappingResult transferData(
			@NonNull List<SourceDocument> sourceDocuments,
			@NonNull DocumentV2 targetDocument,
			@NonNull DocumentProcessingConfig config
	);
}
