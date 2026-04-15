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

package com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IMVK_Service;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * A container holding the configuration parameters for services like {@link IMVK_Service}.
 * It's used in an {@link ServiceFactory} for creating a service.
 */
@OnlyForUsage
public class ServiceConfiguration implements IServiceConfiguration {

	private IEC entityContainer;
	private IModelSpecificSettings modelSpecificSettings;
	private IProjectSpecificSettings pss;
	private final ParserStrictness parserStrictness;

	public ServiceConfiguration(IEC entityContainer, IModelSpecificSettings modelSpecificSettings, IProjectSpecificSettings pss) {
		this(entityContainer, modelSpecificSettings, pss, ParserStrictness.STRICT);
	}

	public ServiceConfiguration(IEC entityContainer, IModelSpecificSettings modelSpecificSettings, IProjectSpecificSettings pss,
			ParserStrictness parserStrictness) {
		this.entityContainer = entityContainer;
		this.modelSpecificSettings = modelSpecificSettings;
		this.pss = pss;
		this.parserStrictness = parserStrictness;
	}

	/**
	 * Returns the IEC object, that is used to create the service.
	 *
	 * @return IEC connected to the service.
	 */
	public IEC getIEC() {
		return entityContainer;
	}

	/**
	 * Returns the {@link IModelSpecificSettings}, that is used to create the service.
	 *
	 * @return the {@link IModelSpecificSettings} connected to the service.
	 */
	public IModelSpecificSettings getModelSpecificSettings() {
		return modelSpecificSettings;
	}

	/**
	 * Returns the {@link IProjectSpecificSettings}, the service uses.
	 *
	 * @return the projectspecific settings.
	 */
	public IProjectSpecificSettings getProjectSpecificSettings() {
		return pss;
	}

	@Override
	public ParserStrictness strictness() {
		return parserStrictness;
	}
}
