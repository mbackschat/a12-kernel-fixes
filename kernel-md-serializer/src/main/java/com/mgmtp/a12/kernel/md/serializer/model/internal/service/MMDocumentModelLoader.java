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

package com.mgmtp.a12.kernel.md.serializer.model.internal.service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelSearchServiceImpl;

import lombok.NonNull;

public class MMDocumentModelLoader {
	private MMDocumentModelLoader() {
		throw new UnsupportedOperationException("util class should not be instantiated");
	}

	public static IDocumentModel deserializeMMDocumentModel(@NonNull String metaModelDmName) {
		try (InputStreamReader reader = new InputStreamReader(
				Objects.requireNonNull(
						MMDocumentModelLoader.class.getClassLoader().getResourceAsStream(metaModelDmName)),
				StandardCharsets.UTF_8)) {
			return new DocumentModelSerializerImpl().deserialize(reader);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to load the meta model " + metaModelDmName, e);
		}
	}

	public static IDocumentModelResolver createResolver(@NonNull IDocumentModel documentModel) {
		DocumentModelSearchServiceImpl searchService = new DocumentModelSearchServiceImpl(documentModel);
		return new IDocumentModelResolver() {
			@Override
			public IDocumentModel getDocumentModelById(@NonNull String id) {
				return documentModel;
			}

			@Override
			public @NonNull Optional<IDocumentModelSearchService> getDocumentModelSearchService(@NonNull String documentModelId) {
				return Optional.of(searchService);
			}
		};
	}

	public static IDocumentModelResolver createResolver(@NonNull String metaModelDmName) {
		IDocumentModel documentModel = deserializeMMDocumentModel(metaModelDmName);
		return createResolver(documentModel);
	}
}
