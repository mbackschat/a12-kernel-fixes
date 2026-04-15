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

package com.mgmtp.a12.kernel.mapping.facade;

import java.util.Optional;

import com.mgmtp.a12.kernel.md.rt.api.IModelCodeCache;
import com.mgmtp.a12.model.utils.Experimental;

import lombok.NonNull;

/**
 * The service configuration for the dynamic serverside mapping via groovy.
 */
@Experimental
public interface DynamicMappingServiceConfig {

	static @NonNull DynamicMappingServiceConfig of(
			@NonNull String mappingModelId,
			@NonNull IMappingModelResolver mappingModelResolver,
			@NonNull IModelCodeCache cache,
			IMappingModelCacheIdProvider mappingModelIdProvider) {
		return new DynamicMappingServiceConfigImpl(mappingModelId, mappingModelResolver, cache,
				Optional.ofNullable(mappingModelIdProvider));
	}

	/**
	 * The id of the mapping model. This used mapping model is resolved from the mappingModelResolver via
	 * {@link IMappingModelResolver#getMappingModelById(String)};
	 */
	String mappingModelId();

	/**
	 * The resolver used to resolve the mapping model, the structural mapping model and the document models.
	 */
	IMappingModelResolver mappingModelResolver();

	/**
	 * An optional provider that provides the id used to put the dynamically generated code into the {@link IModelCodeCache}.
	 * If there is already an entry in the cache for this id, the hash generated from the metamodel and its constituents will be
	 * checked against the corresponding hash of the mapping model used to generate the code from. This is to omit collisions.
	 * <p>
	 * Therefore, the generated id should be specific enough to omit unnecessary recreations of code for the execution and it should
	 * be general enough to omit multiple entries for the same code in the {@link IModelCodeCache} for the "same" model.
	 * <p>
	 * If no provider is specified, the default will use the id defined in the header of the mapping model.
	 */
	Optional<IMappingModelCacheIdProvider> mappingModelCacheIdProvider();

	/**
	 * The model cache the generated code will be stored for reuse if the same mapping model should be used for transfer again.
	 * <p>
	 * For the notion of "same" model see {@link #mappingModelCacheIdProvider()}
	 */
	IModelCodeCache modelCodeCache();
}
