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

import java.io.Reader;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;
import lombok.NonNull;

/**
 * An extension to the {@link IDocumentModelResolver} that can also retrieve mapping models ({@link MM_MappingModel_2}) and
 * structural mapping models ({@link MM_StructuralMappingModel_1}).
 * <p>
 * The returned document models have to be fully expanded already.
 */
public interface IMappingModelResolver extends IDocumentModelResolver {
	/**
	 * Returns a standard implementation on base of readers for the ids.
	 *
	 * @param readerForId
	 * 		a function providing a reader for the given id.
	 * 		It can result in readers for <ul>
	 * 		<li>unexpanded or expanded document models for document model retrieval. Unexpanded ones will we expanded.</li>
	 * 		<li>serialized documents of the correct type for mapping models or structural mapping models</li>
	 * 		</ul>
	 * @param rankedNotificationReceiver
	 * 		a receiver, receiving errors during parsing.
	 * @return a {@link IMappingModelResolver}, that
	 */
	static IMappingModelResolver of(Function<String, Reader> readerForId, Consumer<RankedNotification> rankedNotificationReceiver) {
		return new MappingModelResolverImpl(readerForId, rankedNotificationReceiver);
	}

	MM_MappingModel_2 getMappingModelById(@NonNull String id);

	MM_StructuralMappingModel_1 getStructuralMappingById(@NonNull String id);
}
