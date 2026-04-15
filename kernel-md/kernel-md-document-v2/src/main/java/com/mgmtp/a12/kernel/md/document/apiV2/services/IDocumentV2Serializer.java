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

package com.mgmtp.a12.kernel.md.document.apiV2.services;

import java.io.Reader;
import java.io.Writer;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV2;

import lombok.NonNull;

/**
 * Serializer for a {@link DocumentV2}.
 */
@OnlyForUsage
public interface IDocumentV2Serializer {

	/**
	 * similar to
	 * {@link com.mgmtp.a12.kernel.md.document.api.services.IDocumentSerializer#deserialize(Reader, String,
	 * DocumentDeserializationConfig, Consumer)},
	 * but creates an {@link DocumentV2}
	 */
	DocumentV2 deserializeV2(@NonNull Reader reader, @NonNull String documentModelId, @NonNull DocumentDeserializationConfig config,
			Consumer<RankedNotification> notificationReceiver);

	/**
	 * similar to
	 * {@link com.mgmtp.a12.kernel.md.document.api.services.IDocumentSerializer#deserialize(JsonNode, String,
	 * DocumentDeserializationConfig, Consumer)}, but creates an {@link DocumentV2}.
	 * <p>
	 * The configuration used by kernel to create the {@link JsonNode} can be found in
	 * {@code ObjectMapperProvider#createObjectMapperForDocSerializer}.
	 */
	DocumentV2 deserializeV2(@NonNull JsonNode json, @NonNull String documentModelId,
			@NonNull DocumentDeserializationConfig config, @NonNull Consumer<RankedNotification> notificationReceiver);

	/**
	 * similar to
	 * {@link com.mgmtp.a12.kernel.md.document.api.services.IDocumentSerializer#serialize(IDocument, Writer,
	 * DocumentSerializationConfig)}, but takes an {@link DocumentV2}.
	 * <p>
	 * The field values will not get (formally) validated. If the {@link BasicTypeV2 BasicType} of a field value does not match the
	 * field from the {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel}, the behavior is unspecified.
	 * If only valid values (value matches the type in the respective {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel}) should be written, then the document
	 * has to be validated before serialization.
	 */
	void serializeV2(@NonNull DocumentV2 document, @NonNull Writer writer, @NonNull DocumentSerializationConfig config);
}
