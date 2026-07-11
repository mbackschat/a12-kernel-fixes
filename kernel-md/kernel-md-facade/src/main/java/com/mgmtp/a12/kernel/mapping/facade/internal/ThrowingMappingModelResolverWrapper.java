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

package com.mgmtp.a12.kernel.mapping.facade.internal;

import java.util.Objects;

import com.mgmtp.a12.kernel.mapping.facade.IMappingModelResolver;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;

import lombok.NonNull;

public class ThrowingMappingModelResolverWrapper implements IMappingModelResolver {
	@NonNull private final IMappingModelResolver delegate;

	private ThrowingMappingModelResolverWrapper(final @NonNull IMappingModelResolver delegate) {
		this.delegate = delegate;
	}

	public static ThrowingMappingModelResolverWrapper wrap(final @NonNull IMappingModelResolver delegate) {
		if (delegate instanceof ThrowingMappingModelResolverWrapper wrappedDelegate) {
			return wrappedDelegate;
		}
		return new ThrowingMappingModelResolverWrapper(delegate);
	}

	@Override public MM_MappingModel_2 getMappingModelById(@NonNull String id) {
		return Objects.requireNonNull(
				delegate.getMappingModelById(id),
				() -> "The mapping model with id '%s' could not be found.".formatted(id));
	}

	@Override public MM_StructuralMappingModel_1 getStructuralMappingById(@NonNull String id) {
		return Objects.requireNonNull(
				delegate.getStructuralMappingById(id),
				() -> "The structural mapping model with id '%s' could not be found.".formatted(id));
	}

	@Override public IDocumentModel getDocumentModelById(@NonNull String id) {
		return Objects.requireNonNull(
				delegate.getDocumentModelById(id),
				() -> "The document model with id '%s' could not be found.".formatted(id));
	}
}
