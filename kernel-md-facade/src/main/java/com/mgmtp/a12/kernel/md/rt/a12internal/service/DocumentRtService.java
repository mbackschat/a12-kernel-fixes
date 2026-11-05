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
package com.mgmtp.a12.kernel.md.rt.a12internal.service;

import com.mgmtp.a12.kernel.core.rt.a12internal.IGeneratedCodeAccessFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentDynamicServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentStaticServiceConfig;
import com.mgmtp.a12.kernel.md.rt.internal.service.DynamicModelProvider;
import com.mgmtp.a12.kernel.md.rt.internal.service.IModelProvider;
import com.mgmtp.a12.kernel.md.rt.internal.service.StaticModelProvider;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Provides functionality which may be used only by other a12 components.
 */
@OnlyForUsage
public class DocumentRtService {

	private final IDocumentServiceConfig config;

	/**
	 * @param config
	 * 		configuration to create the service. Currently, it has to be an instance of either
	 *        {@link IDocumentDynamicServiceConfig} or {@link IDocumentStaticServiceConfig}.
	 * @throws IllegalArgumentException
	 * 		If no supported sub-interface of {@link IDocumentServiceConfig} is passed.
	 */
	public DocumentRtService(@NonNull IDocumentServiceConfig config) {
		this.config = config;
	}

	private IGeneratedCodeAccessFactory getAccessFactory(IDocumentModel model) {
		IModelProvider modelProvider = switch (config) {
			case IDocumentDynamicServiceConfig dynamicConfig -> new DynamicModelProvider(dynamicConfig, id -> model);
			case IDocumentStaticServiceConfig staticConfig -> new StaticModelProvider(staticConfig, id -> model);
			default -> throw new IllegalArgumentException("The IDocumentServiceConfig implementation passed is not supported.");
		};
		return modelProvider.getGeneratedCodeAccessFactory(model.getHeader().getId());
	}

	/**
	 * Get the {@link IMetaModel} for the given document model.
	 *
	 * @param model
	 * 		the {@link IDocumentModel} whose {@link IMetaModel} should be returned.
	 * @return the {@link IMetaModel} of the document model passed
	 */
	public IMetaModel getMetaModel(IDocumentModel model) {
		return getAccessFactory(model).getMetaObject();
	}

	/**
	 * Get the {@link IValidator} for the given document model.
	 *
	 * @param model
	 * 		the {@link IDocumentModel} whose {@link IValidator} should be returned.
	 * @return the {@link IValidator} of the document model passed
	 */
	public IValidator getValidator(IDocumentModel model) {
		return getAccessFactory(model).getValidator();
	}

	/**
	 * Get the {@link ICalculator} for the given document model.
	 *
	 * @param model
	 * 		the {@link IDocumentModel} whose {@link ICalculator} should be returned.
	 * @return the {@link ICalculator} of the document model passed
	 */
	public ICalculator getCalculator(IDocumentModel model) {
		return getAccessFactory(model).getCalculator();
	}

}
