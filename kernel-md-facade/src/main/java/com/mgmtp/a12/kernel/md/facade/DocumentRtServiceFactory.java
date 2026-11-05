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

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentDynamicServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentStaticServiceConfig;
import com.mgmtp.a12.kernel.md.rt.internal.service.DocumentDynamicRtService;
import com.mgmtp.a12.kernel.md.rt.internal.service.DocumentStaticRtService;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Factory provides access to runtime services like validation and computation.
 */
@OnlyForUsage
public class DocumentRtServiceFactory {

	private final IDocumentModelResolver documentModelResolver;

	/**
	 * @param documentModelResolver
	 * 		the {@link IDocumentModelResolver} instance which will be used to retrieve the
	 * 		corresponding {@link IDocumentModel}. May not be {@code null}.
	 */
	public DocumentRtServiceFactory(@NonNull IDocumentModelResolver documentModelResolver) {
		Objects.requireNonNull(documentModelResolver);
		this.documentModelResolver = documentModelResolver;
	}

	/**
	 * Creates a service for the document, which can be used for the validation and/or
	 * computation.<br>
	 * Currently, both dynamic and static validations/computations are allowed.
	 * {@link IDocumentDynamicServiceConfig} or {@link IDocumentStaticServiceConfig} needs to be
	 * specified to be able to distinguish this. The used {@link IDocumentRtService} will depend on
	 * the given implementation of {@link IDocumentServiceConfig}.
	 *
	 * @param config
	 * 		configuration to create the service. Currently, it has to be an instance of either
	 *        {@link IDocumentDynamicServiceConfig} or {@link IDocumentStaticServiceConfig}.
	 * @return instance of the service.
	 *
	 * @throws IllegalArgumentException
	 * 		If no supported sub-interface of {@link IDocumentServiceConfig} is passed.
	 */
	public IDocumentRtService createDocumentRtService(@NonNull IDocumentServiceConfig config) {
		if (config instanceof IDocumentDynamicServiceConfig) {
			return new DocumentDynamicRtService((IDocumentDynamicServiceConfig) config, documentModelResolver);
		}
		if (config instanceof IDocumentStaticServiceConfig) {
			return new DocumentStaticRtService((IDocumentStaticServiceConfig) config, documentModelResolver);
		}
		throw new IllegalArgumentException("The IDocumentServiceConfig implementation passed is not supported.");
	}

}
