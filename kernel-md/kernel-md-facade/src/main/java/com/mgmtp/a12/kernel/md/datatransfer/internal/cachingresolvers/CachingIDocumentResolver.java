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

package com.mgmtp.a12.kernel.md.datatransfer.internal.cachingresolvers;

import java.util.HashMap;
import java.util.Map;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;

import lombok.NonNull;

public class CachingIDocumentResolver implements IDocumentModelResolver {
	private final IDocumentModelResolver wrapped;
	/**
	 * the cached dms must not be given to callers, because they may mutate the underlying wrapped DocumentModels.
	 * Only (deep) copies may be returned.
	 */
	private final Map<String, DocumentModel> internalCachedDMs = new HashMap<>();

	/**
	 * wraps the given resolver in a {@link CachingIDocumentResolver} if it is not already
	 * a CachingIDocumentResolver.
	 */
	public static CachingIDocumentResolver wrap(IDocumentModelResolver other) {
		if (other instanceof CachingIDocumentResolver otherCachingResolver) {
			return otherCachingResolver;
		} else {
			return new CachingIDocumentResolver(other);
		}
	}

	private CachingIDocumentResolver(IDocumentModelResolver wrapped) {
		this.wrapped = wrapped;
	}

	@Override public IDocumentModel getDocumentModelById(@NonNull String id) {
		DocumentModel internalDm = internalCachedDMs.computeIfAbsent(id, dmId -> unwrapDm(wrapped.getDocumentModelById(dmId)));
		return new DocumentModelWrapper(DocumentModelCopyService.copy(internalDm));
	}

	private DocumentModel unwrapDm(IDocumentModel dm) {
		if (dm instanceof DocumentModelWrapper dmWrapper) {
			return dmWrapper.getDocumentModel();
		} else {
			// this assumption should be ok, because IDocumentModel is @OnlyForUsage, so we do have control over which impls
			// exist. This is some burden for future maintenance, but we do already make this assumption in other places.
			throw new IllegalArgumentException("The only expected implementation of IDocumentModel is DocumentModelWrapper");
		}
	}
}
