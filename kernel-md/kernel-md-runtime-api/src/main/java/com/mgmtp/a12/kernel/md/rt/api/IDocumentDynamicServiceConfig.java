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

import java.util.Optional;

/**
 * Configuration to create a service for a document. This is meant to be used for dynamic services
 * for which the code for the model is generated at runtime.
 * <p>
 * <b>Note:</b> The caller has to provide an implementation of this interface with an own
 * implementation of {@link IModelCodeCache}.
 */
public interface IDocumentDynamicServiceConfig extends IDocumentServiceConfig {

	/**
	 * @return the cache for the generated code of the models.
	 */
	IModelCodeCache getCache();

	/**
	 * Configuration to set a custom hashing function for Document Models. This function is used to determine whether the Document
	 * Model currently provided via the {@link com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver} is the same as
	 * the one that was used to create {@link IModelCode} stored in the {@link #getCache()}.. The id of the Document Model used to
	 * create a cache entry of type {@link IModelCode} is given as input into this function, and the resulting hash remembered as
	 * {@link IModelCode#getModelHash()}}.
	 * <p>
	 * Possible implementations (this list is not exhaustive):<ul>
	 * <li>MD5 hash of the serialized DM (current default)</li>
	 * <li>serialized String of the DM</li>
	 * <li>internal DB id of the DM version (assumption: unique between all DMs &amp; changes with every DM update)</li>
	 * <li>an unchanging string constant (makes it so existing cache entries will never be updated by Kernel)</li>
	 * </ul>
	 * <p>
	 * <b>Warning:</b> Only set this configuration if you know exactly what you're doing. An inadequate implementation may cause the
	 * {@link IModelCodeCache} to retain stale entries.
	 *
	 * @return the custom hashing function to use. It should be empty if you want to use the default implementation that is
	 * 		generally recommended.
	 */
	// internal note: Both Data Services & TDS are planning to return a string constant in order to fully disable our hashing
	default Optional<IModelCodeHashProvider> getModelCodeHashProvider() {
		return Optional.empty();
	}

	/**
	 * @return {@code true} if ExtMP-specific information should be considered, otherwise {@code false}. This is only relevant for
	 * 		ExtMP projects, otherwise this should be left at its default value. See also {@link com.mgmtp.a12.kernel.md.utils.NotInD}.
	 */
	default boolean considerNotInD() {
		return false;
	}
}