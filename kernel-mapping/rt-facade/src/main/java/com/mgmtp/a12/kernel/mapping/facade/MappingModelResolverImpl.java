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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mgmtp.a12.kernel.md.combination.a12internal.ExpandingDmResolver;
import com.mgmtp.a12.kernel.md.combination.a12internal.UnexpandedModelResolverImpl;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.utils.Experimental;

import lombok.NonNull;

@Experimental
class MappingModelResolverImpl implements IMappingModelResolver {

	private final Function<String, Reader> readerForId;
	private final ExpandingDmResolver dmResolver;
	private final Consumer<RankedNotification> notificationReceiver;

	public MappingModelResolverImpl(Function<String, Reader> readerForId, Consumer<RankedNotification> notificationReceiver) {
		this.readerForId = readerForId;
		this.dmResolver = ExpandingDmResolver.of(new UnexpandedModelResolverImpl(readerForId), Locale.US, notificationReceiver);
		this.notificationReceiver = notificationReceiver;
	}

	private DocumentV2 readDocumentV2(Reader reader, String dmFile) {
		DocumentDeserializationConfig config = DocumentDeserializationConfig.builder().ignoreUnknownEntities(true).build();
		return new DocumentSerializerImpl(createResolver(dmFile)).deserializeV2(reader,
				dmFile, config, notificationReceiver);
	}

	private static IDocumentModelResolver createResolver(String dmFile) {
		return id -> {
			try (Reader reader = new InputStreamReader(
					Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(dmFile))
			)) {
				return new DocumentModelSerializerImpl().deserialize(reader);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		};
	}

	private DocumentV2 getDocumentV2ById(@NonNull String id, String dmFile) {
		try (Reader reader = this.readerForId.apply(id)) {
			return readDocumentV2(reader, dmFile);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override public MM_MappingModel_2 getMappingModelById(@NonNull String id) {
		return MM_MappingModel_2._viewOf(getDocumentV2ById(id, "extmpinternal/codegen-mm_mappingmodel-2.json"));
	}

	@Override public MM_StructuralMappingModel_1 getStructuralMappingById(@NonNull String id) {
		return MM_StructuralMappingModel_1._viewOf(getDocumentV2ById(id, "extmpinternal/codegen-mm_structuralmappingmodel-1.json"));
	}

	@Override public IDocumentModel getDocumentModelById(@NonNull String id) {
		return dmResolver.getDocumentModelById(id);
	}
}
