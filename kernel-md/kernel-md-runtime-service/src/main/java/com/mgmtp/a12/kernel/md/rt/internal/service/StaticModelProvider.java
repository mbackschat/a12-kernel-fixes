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

package com.mgmtp.a12.kernel.md.rt.internal.service;

import static com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelResolverUtils.getDocumentModelOrThrow;

import java.util.Optional;

import com.mgmtp.a12.kernel.core.rt.a12internal.IGeneratedCodeAccessFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelServiceUtils;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentStaticServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IStaticModelCodeCache;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StaticModelProvider implements IModelProvider {

	private final IDocumentStaticServiceConfig config;
	private final IDocumentModelResolver documentModelResolver;

	@Override
	public IGeneratedCodeAccessFactory getGeneratedCodeAccessFactory(String documentModelId) {
		return getStaticModelCodeAccessor(documentModelId).getAccessFactory();
	}

	private StaticModelCodeAccessor getStaticModelCodeAccessor(String documentModelId) {
		IDocumentModel documentModel = getDocumentModelOrThrow(documentModelResolver, documentModelId);
		String docModelId = createModelCodeIdentifier(documentModel);
		Optional<IStaticModelCodeCache> cacheOpt = config.getCache();
		if (cacheOpt.isPresent()) {
			Object obj = cacheOpt.get().getModelCode(docModelId);
			if (obj == null) {
				fillCache(docModelId, cacheOpt.get());
			}
			return (StaticModelCodeAccessor) cacheOpt.get().getModelCode(docModelId);
		} else {
			return StaticModelCodeAccessor.builder().build(docModelId, config);
		}
	}

	private synchronized void fillCache(String docModelId, IStaticModelCodeCache cache) {
		Object obj = cache.getModelCode(docModelId); // re-check that there's no matching entry, inside the synchronized block.
		if (obj == null) {
			cache.addModelCode(docModelId, StaticModelCodeAccessor.builder().build(docModelId, config));
		}
	}

	@Override
	public String createModelCodeIdentifier(IDocumentModel documentModel) {
		return config.getModelIdProvider()
				.orElse(DocumentModelServiceUtils::createGeneratedCodeId)
				.getDocumentModelId(documentModel);
	}
}
