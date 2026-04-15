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

import com.mgmtp.a12.kernel.md.model.api.IDocumentModelInfo;

/**
 * Configuration in order to create a service for a document. This configuration is the base for
 * more specific configurations for either static services for which the code for the model must not
 * be generated at runtime or for dynamic services for which the code for the model is generated at
 * runtime.
 * <p>
 * <b>Note:</b> This interface is not meant to be implemented directly, rather the caller has to
 * provide an implementation of one of its subinterfaces {@link IDocumentStaticServiceConfig} or
 * {@link IDocumentDynamicServiceConfig}, respectively.
 */
public interface IDocumentServiceConfig {

	/**
	 * @return name of the variant, may be empty. Formerly known as <i>application</i>.
	 **/
	Optional<String> getVariant();

	/**
	 * @return a {@link ILabelProvider}-instance to customize the string representation of fields in error
	 * 		texts, may be empty, in which case a default representation will be used.
	 **/
	Optional<ILabelProvider> getLabelProvider();

	/**
	 * Configuration to set a custom way how to determine the ID of a document model (DM). This ID will be used as key
	 * while caching the generated code and also while loading/getting the generated code from the cache.
	 * <p>
	 * <b>Example of an use case:</b> In ExtMP, a document model can have different versions (see
	 * {@link IDocumentModelInfo#getBranch()}) and each version can have different working states (see
	 * {@link IDocumentModelInfo#getRevision()}) and in some cases, the runtime services should be executed for two or more
	 * version/state combinations. Since the ID of the generated code is the same in the previous cases (it is only based on the ID
	 * stored in the DM), the custom DM ID provided in the implementation given in this method is used to differentiate them and
	 * cache the corresponding generated code.
	 *
	 * @return the {@link IDocumentModelIdProvider}. It should be empty if you want to use the default
	 * 		implementation that is generally recommended.
	 */
	default Optional<IDocumentModelIdProvider> getModelIdProvider() {
		return Optional.empty();
	}
}
