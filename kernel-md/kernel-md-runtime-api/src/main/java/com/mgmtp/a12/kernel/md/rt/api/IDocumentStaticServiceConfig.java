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

import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;

/**
 * Configuration to create a service for a document. This is meant to be used for static services
 * for which the code for the model must not be generated at runtime, but is either already present in the
 * classpath or can be loaded via a specific {@link ClassLoader}.
 * <p>
 * <b>Note:</b> The caller has to provide an implementation of this interface.
 */
public interface IDocumentStaticServiceConfig extends IDocumentServiceConfig {

	/**
	 * @param documentModelId
	 * 		the document model id according to {@link IDocumentModelIdProvider#getDocumentModelId}.
	 * @return the java package for the specified document model, that means, the java package which the generated java code resides
	 * 		in. This value must fit to the value returned in your implementation of
	 *        {@link IValidationCodeGeneratorConfig#getPackageName()} when you generated the code you want to use here.
	 * 		If you used the default implementation in {@link IValidationCodeGeneratorConfig#getPackageName()} return
	 *        {@link Optional#empty()} here.
	 */
	default Optional<String> getModelPackage(String documentModelId) {
		return Optional.empty();
	}

	/**
	 * @param documentModelId
	 * 		the document model id according to {@link IDocumentModelIdProvider#getDocumentModelId}.
	 * 		This parameter can be evaluated, for example, if the application uses a large number of document models
	 * 		that are to be dynamically exchanged individually. In this case, a specific class loader could be used
	 * 		to access the generated code of a document model.
	 * @return the {@link ClassLoader} via which the code generated for the document model should be loaded. If the generated code
	 * 		is available in the application's classpath, no specific implementation is required here. If a specific classloader is
	 * 		returned here, it must extend the default classloader - e.g. as follows:
	 * 		<br>
	 * 		<pre>{@code
	 * 					URL[] urls = new URL[]{pathToGeneratJar.toUri().toURL()};
	 * 					ClassLoader myClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
	 *             }</pre>
	 */
	default Optional<ClassLoader> getModelCodeClassLoader(String documentModelId) {
		return Optional.empty();
	}

	/**
	 * @return the cache for the statically generated code of the document models or {@link Optional#empty()} if no caching is desired
	 * 		(with the disadvantages described in {@link IStaticModelCodeCache}).
	 */
	default Optional<IStaticModelCodeCache> getCache() {
		return Optional.empty();
	}

}
