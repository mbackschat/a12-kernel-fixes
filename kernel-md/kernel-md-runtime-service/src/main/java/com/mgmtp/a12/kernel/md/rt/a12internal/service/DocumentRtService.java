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

/**
 * Provides functionality which may be used only by other a12 components.
 */
@OnlyForUsage
public class DocumentRtService {

	private final IDocumentServiceConfig config;
	private final String variant;
	private final IModelProvider modelProvider;

	/**
	 * @param config
	 * 		configuration to create the service. Currently, it has to be an instance of either
	 *        {@link IDocumentDynamicServiceConfig} or {@link IDocumentStaticServiceConfig}.
	 * @throws IllegalArgumentException
	 * 		If no supported sub-interface of {@link IDocumentServiceConfig} is passed.
	 */
	public DocumentRtService(IDocumentServiceConfig config) {
		this.config = config;
		this.variant = config.getVariant().orElse(null);
		this.modelProvider = initModelProvider();
	}

	private IModelProvider initModelProvider() {
		if (config instanceof IDocumentDynamicServiceConfig dynamicConfig) {
			return new DynamicModelProvider(dynamicConfig);
		}
		if (config instanceof IDocumentStaticServiceConfig staticConfig) {
			return new StaticModelProvider(staticConfig);
		}
		throw new IllegalArgumentException("The IDocumentServiceConfig implementation passed is not supported.");
	}

	/**
	 * Get the {@link IMetaModel} for the given document model.
	 *
	 * @param model
	 * 		the {@link IDocumentModel} whose {@link IMetaModel} should be returned.
	 * @return the {@link IMetaModel} of the document model passed
	 */
	public IMetaModel getMetaModel(IDocumentModel model) {
		return modelProvider.getMetaModel(model, variant);
	}

	/**
	 * Get the {@link IValidator} for the given document model.
	 *
	 * @param model
	 * 		the {@link IDocumentModel} whose {@link IValidator} should be returned.
	 * @return the {@link IValidator} of the document model passed
	 */
	public IValidator getValidator(IDocumentModel model) {
		return modelProvider.getValidator(model, variant);
	}

	/**
	 * Get the {@link ICalculator} for the given document model.
	 *
	 * @param model
	 * 		the {@link IDocumentModel} whose {@link ICalculator} should be returned.
	 * @return the {@link ICalculator} of the document model passed
	 */
	public ICalculator getCalculator(IDocumentModel model) {
		return modelProvider.getCalculator(model, variant);
	}

}
