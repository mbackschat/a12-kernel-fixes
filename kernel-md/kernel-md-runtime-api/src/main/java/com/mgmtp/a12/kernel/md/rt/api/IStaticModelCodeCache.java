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

package com.mgmtp.a12.kernel.md.rt.api;

import lombok.NonNull;

/**
 * The cache for the code statically generated from document models.
 * <p>
 * In contrast to the dynamic solution (see {@link IDocumentDynamicServiceConfig} or {@link IModelCodeCache}), the first use
 * of the generated code is quite performant (code generation and parsing for Groovy or compilation for Java have
 * already been done in advance). However, the access class for validation, calculation, etc. was deliberately implemented as a
 * singleton, because loading static information of the document model takes some time and it is sufficient to do it only once.
 * However, the reference to this class cannot be held internally in Kernel because
 * <ul>
 * <li>In environments where document models can change at runtime of the application, it must be possible to delete references
 * so that they can be replaced with references to the newly generated code.</li>
 * <li>In environments in which a lot of document models are deployed but only a few are actually used very often, it
 * might make sense to quickly release references for rarely used document models in order to save resources.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Note:</b> The implementing class has to be thread safe and must be provided by the caller.
 * </p>
 */
public interface IStaticModelCodeCache {

	/**
	 * Returns the cached model code belonging to the given identifier.
	 *
	 * @param modelCodeIdentifier
	 * 		string to identify the model code.
	 * @return the cached model code or {@code null} if there is no cached model code for the given
	 * 		model code identifier.
	 * 		<p>
	 * 		<b>Attention:</b> Only for Kernel internal usage.
	 * 		</p>
	 */
	IStaticModelCode getModelCode(@NonNull String modelCodeIdentifier);

	/**
	 * Adds the given {@code modelCode} to the cache for the given {@code modelCodeIdentifier}.
	 * Overwrites the cached model code in case the cache already contains an entry for the given
	 * {@code modelCodeIdentifier}.
	 *
	 * @param modelCodeIdentifier
	 * 		string to identify the model code.
	 * @param modelCode
	 * 		model code to be cached.
	 * 		<p>
	 * 		<b>Attention:</b> Only for Kernel internal usage.
	 * 		</p>
	 */
	void addModelCode(@NonNull String modelCodeIdentifier, @NonNull IStaticModelCode modelCode);
}
