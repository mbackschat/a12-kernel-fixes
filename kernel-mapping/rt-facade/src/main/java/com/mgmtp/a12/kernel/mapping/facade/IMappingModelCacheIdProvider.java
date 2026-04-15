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

import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.model.utils.Experimental;

import lombok.NonNull;

/**
 * Provides a custom way to determine the ID for a mapping model (e.g. in case the ID contained in the document model is not enough
 * to identify the mapping model unambiguously).
 * <p>
 * This provider is used together with the {@link com.mgmtp.a12.kernel.md.rt.api.IModelCodeCache} and determines the used cache
 * keys.
 * If the mapping model or one of its constituents changes, we still recreate the model code and replace it in the cache, because
 * we check the consistency of the cache entry additionally with a hash value.
 * <p>
 * <b>Note:</b> The caller has to provide an implementation of this interface.
 */
@Experimental
public interface IMappingModelCacheIdProvider {

	/**
	 * @param mappingModel
	 *        {@link MM_MappingModel_2} from and for which the ID should be determined.
	 * @return mapping model ID, which is calculated from the parameters. This ID is e.g. used to
	 * 		load the generated code. Must not be {@code null} or empty.
	 */
	@NonNull String getMappingModelCacheId(@NonNull MM_MappingModel_2 mappingModel);
}
