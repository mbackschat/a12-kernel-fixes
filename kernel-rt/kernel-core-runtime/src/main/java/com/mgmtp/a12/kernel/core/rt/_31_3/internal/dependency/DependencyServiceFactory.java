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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.dependency;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt.a12internal.dependency.IDependencyService;

import lombok.NonNull;

/**
 * Builds the real {@link IDependencyService} for a piece of generated code from its embedded dependency
 * model. The generated {@code getDependencyService()} factory method calls {@link #loadFromClasspath} with
 * the resource path the code generation embedded the serialized model under; code without a dependency
 * model keeps the no-op default service instead.
 *
 * <p>Kept as a static entry point (like {@code IPrecompModelDataMapper.loadFromClasspath}) so the generated
 * code needs to know neither {@link DependencyModelAccessor} nor {@link DependencyServiceImpl}.
 */
public final class DependencyServiceFactory {

	private DependencyServiceFactory() {
		throw new UnsupportedOperationException("Should not be instantiated");
	}

	/**
	 * Loads the serialized dependency model from the classpath and builds the service.
	 *
	 * @param resourcePath
	 * 		the classpath resource the code generation embedded the serialized model under.
	 * @param classLoader
	 * 		the class loader to load the resource with (passed by the generated code so its generat-specific
	 * 		class loader is used).
	 * @param metaData
	 * 		the root model's structural view, obtained from the generated metadata.
	 * @return the dependency service backed by the embedded model.
	 *
	 * @throws RuntimeException
	 * 		if the resource is missing - a generated dependency factory must always find its model.
	 */
	public static IDependencyService loadFromClasspath(@NonNull String resourcePath, @NonNull ClassLoader classLoader,
			@NonNull IMetaDataValidierung metaData) {
		try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				throw new RuntimeException("cannot find dependency model resource " + resourcePath);
			}
			Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
			DependencyModelAccessor accessor = DependencyModelAccessor.fromSerialized(reader);
			return new DependencyServiceImpl(accessor, metaData);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
