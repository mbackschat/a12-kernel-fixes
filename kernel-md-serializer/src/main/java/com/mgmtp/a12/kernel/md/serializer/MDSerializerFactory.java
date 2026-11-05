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

package com.mgmtp.a12.kernel.md.serializer;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Provides access to (de)serialization of {{@link DocumentV2}} and {@link IDocumentModel}.
 */
@OnlyForUsage
public class MDSerializerFactory {
	/**
	 * Creates a serializer for a {@link DocumentV2}.
	 *
	 * @param documentModelResolver
	 * 		the {@link IDocumentModelResolver} instance which will be used
	 * 		to retrieve the
	 * 		information of the corresponding {@link IDocumentModel} when
	 * 		working on a specific
	 *        {@link DocumentV2}. May not be {@code null}.
	 * @return the instance of the service. Is never {@code null}.
	 */
	public @NonNull IDocumentV2Serializer createDocumentSerializerV2(@NonNull IDocumentModelResolver documentModelResolver) {
		return new DocumentSerializerImpl(documentModelResolver);
	}

	/**
	 * Creates an {@link IDocumentModelSerializer}.
	 *
	 * @return the corresponding instance. Is never {@code null}.
	 */
	public @NonNull IDocumentModelSerializer createDocumentModelSerializer() {
		return new DocumentModelSerializerImpl();
	}
}
