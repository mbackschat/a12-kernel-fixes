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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.serializer.MDSerializerFactory;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views.MM_DependencyModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public class DependencyModelSerializationUtils {
	private static final String META_MODEL_DM_NAME = "extmpinternal/none-mm_dependencymodel-1.json";

	@Getter(lazy = true, value = AccessLevel.PRIVATE) @Accessors(fluent = true)
	private static final IDocumentModel documentModel = deserializeMetaModel();
	@Getter(lazy = true, value = AccessLevel.PRIVATE) @Accessors(fluent = true)
	private static final IDocumentV2Serializer documentSerializer =
			new MDSerializerFactory().createDocumentSerializerV2(id -> documentModel());

	public static void serialize(@NonNull MM_DependencyModel_1 document, @NonNull Writer writer) {
		documentSerializer().serializeV2(document._unwrap(), writer, DocumentSerializationConfig.builder().build());
	}

	public static @NonNull MM_DependencyModel_1 deserialize(@NonNull Reader reader) {
		return deserialize(reader, notification -> {
			if (notification.getSeverity() == Severity.ERROR) {
				throw new IllegalArgumentException("The deserialization of the dependency model failed!");
			}
		});
	}

	public static @NonNull MM_DependencyModel_1 deserialize(
			@NonNull Reader reader,
			@NonNull Consumer<? super RankedNotification> notificationReceiver
	) {
		DocumentV2 document = documentSerializer().deserializeV2(
				reader,
				documentModel().getHeader().getId(),
				DocumentDeserializationConfig.builder().build(),
				notificationReceiver::accept);
		return MM_DependencyModel_1._viewOf(document);
	}

	private static IDocumentModel deserializeMetaModel() {
		try (Reader reader = new InputStreamReader(
				Objects.requireNonNull(
						DependencyModelSerializationUtils.class.getClassLoader().getResourceAsStream(META_MODEL_DM_NAME),
						() -> "Missing dependency-model metamodel resource " + META_MODEL_DM_NAME),
				StandardCharsets.UTF_8)) {
			return new MDSerializerFactory().createDocumentModelSerializer().deserialize(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private DependencyModelSerializationUtils() {
		throw new UnsupportedOperationException("Should not be instantiated");
	}
}
