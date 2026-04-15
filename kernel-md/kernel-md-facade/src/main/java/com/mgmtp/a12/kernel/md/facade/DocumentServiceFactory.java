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

package com.mgmtp.a12.kernel.md.facade;

import java.util.Objects;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSerializer;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentService;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDmAwareDocService;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV1V2Converter;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.document.internal.service.DocumentFactoryImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV1V2ConverterImpl;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.serializer.MDSerializerFactory;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAwareDocServiceImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSearchServiceImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentServiceImpl;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Factory provides access to services related to the document.
 */
@OnlyForUsage
public class DocumentServiceFactory {

	private final IDocumentModelResolver documentModelResolver;

	/**
	 * @param documentModelResolver
	 * 		the {@link IDocumentModelResolver} instance which will be used
	 * 		to retrieve the
	 * 		information of the corresponding {@link IDocumentModel} when
	 * 		working on a specific
	 *        {@link IDocument}. May not be {@code null}.
	 */
	public DocumentServiceFactory(@NonNull IDocumentModelResolver documentModelResolver) {
		Objects.requireNonNull(documentModelResolver);
		this.documentModelResolver = documentModelResolver;
	}

	/**
	 * Creates a serializer for a {@link IDocument}.
	 *
	 * @return the instance of the service. Is never {@code null}.
	 *
	 * @deprecated use {@link #createDocumentV2Serializer()} instead.
	 */
	@Deprecated(since = "30.0.0")
	public IDocumentSerializer createDocumentSerializer() {
		return new MDSerializerFactory().createDocumentSerializer(documentModelResolver);
	}

	/**
	 * Creates a serializer for a {@link DocumentV2}.
	 *
	 * @return the instance of the service. Is never {@code null}.
	 */
	@NonNull public IDocumentV2Serializer createDocumentV2Serializer() {
		return new DocumentSerializerImpl(documentModelResolver);
	}

	/**
	 * Creates a service for the document (for more information see {@link IDocumentService}).
	 *
	 * @param document
	 * 		the {@link IDocument} instance for which the services should be executed. May
	 * 		not
	 * 		be {@code null}.
	 * @return the instance of the service. Is never {@code null}.
	 *
	 * @deprecated the {@link IDocumentService} is deprecated use {@link #createDmAwareDocService(String)} for functionality with
	 * 		the document model.
	 */
	@Deprecated(since = "30.0.0")
	public IDocumentService createDocumentService(IDocument document) {
		Objects.requireNonNull(document);
		return new DocumentServiceImpl(document, documentModelResolver);
	}

	/**
	 * Note: for additional DocumentV2-related util methods (that are <em>not</em> DM-related), see also
	 * {@link com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils}
	 */
	@NonNull public IDmAwareDocService createDmAwareDocService(@NonNull String dmId) {
		return DmAwareDocServiceImpl.ofResolver(dmId, documentModelResolver);
	}

	/**
	 * @deprecated Use methods that expect {@link DocumentV2} instead of {@link IDocument}.
	 */
	@Deprecated(since = "30.0.0")
	public IDocumentV1V2Converter createDocumentV1V2Converter() {
		return DocumentV1V2ConverterImpl.getInstance();
	}

	/**
	 * Creates a service for a specific document, which can be used to search field instances by
	 * different properties.
	 *
	 * @param document
	 * 		the document in which you want to search. May not be {@code null}.
	 * @return the instance of the service. Is never {@code null}.
	 *
	 * @deprecated Use methods of {@link DocumentV2}, {@link com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils} and
	 *        {@link IDmAwareDocService} instead.
	 */
	@Deprecated(since = "30.0.0")
	public IDocumentSearchService createDocumentSearchService(IDocument document) {
		Objects.requireNonNull(document);
		return new DocumentSearchServiceImpl(document, documentModelResolver);
	}

	/**
	 * Creates an instance of the default implementation of {@link IDocumentFactory}.
	 *
	 * @return the instance of the default creation service. Is never {@code null}.
	 *
	 * @deprecated use DocumentV2 API instead, see deprecation information of {@link IDocumentFactory}.
	 */
	@Deprecated(since = "30.0.0")
	public IDocumentFactory createDocumentFactory() {
		return new DocumentFactoryImpl();
	}

}
