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

import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV2;

import lombok.NonNull;
import tools.jackson.databind.JsonNode;

/**
 * Serializer for a {@link DocumentV2}.
 */
@OnlyForUsage
public interface IDocumentV2Serializer {

	/**
	 * Deserializes the document contained in the given {@link Reader}.
	 * <p>
	 * <b>Notes:</b>
	 * <ul>
	 * <li>While deserializing a {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2}, the corresponding value will be converted
	 * from the string representation to the java type defined in the corresponding {@link com.mgmtp.a12.kernel.md.model.api.IField}
	 * (see also
	 * {@link IDmAwareDocService#convertToJavaTypeV2(String, String, Consumer)}). If an
	 * error occurred while converting the value, the error will be reported in the passed
	 * {@link Consumer} and the value in string format will be set to the corresponding
	 * {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2}.</li>
	 * <li>The document id (if present) will be always handled as a string.</li>
	 * </ul>
	 *
	 * @param reader
	 * 		the reader that provides a string representation of the document to be
	 * 		deserialized. May not be {@code null}.
	 * @param documentModelId
	 * 		id of the corresponding {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel}. May not be {@code null} or blank;
	 * @param config
	 * 		configuration for the deserialization. May not be {@code null}.
	 * @param notificationReceiver
	 * 		the {@link RankedNotification}s, found during the process, will be reported to this callback. May not be {@code null}.
	 * @return the deserialized {@link DocumentV2}.
	 *
	 * @throws DocumentSerializationException
	 * 		if problems occur that prevent further processing
	 * @throws UnsupportedOperationException
	 * 		in case the document id is neither a string nor a number
	 */
	DocumentV2 deserializeV2(@NonNull Reader reader, @NonNull String documentModelId, @NonNull DocumentDeserializationConfig config,
			Consumer<RankedNotification> notificationReceiver);

	/**
	 * similar to
	 * {@link #deserializeV2(Reader, String, DocumentDeserializationConfig, Consumer)}, but uses a {@link JsonNode} instead of a
	 * {@link Reader}.
	 * <p>
	 * The configuration used by kernel to create the {@link JsonNode} can be found in
	 * {@code ObjectMapperProvider#createObjectMapperForDocSerializer}.
	 */
	DocumentV2 deserializeV2(@NonNull JsonNode json, @NonNull String documentModelId,
			@NonNull DocumentDeserializationConfig config, @NonNull Consumer<RankedNotification> notificationReceiver);

	/**
	 * Serializes the given {@link DocumentV2}.
	 * <p>
	 * <b>Note:</b> While serializing, every time an {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2} is
	 * present in the {@link DocumentV2}, the group will be inserted into the serialized document, even if no
	 * instances of the groups descendants are present.
	 * <b>Example:</b> Given an {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel} {@code DM} containing an
	 * {@link com.mgmtp.a12.kernel.md.model.api.IGroup} {@code G1} containing an {@link com.mgmtp.a12.kernel.md.model.api.IField}
	 * {@code F} as well as an {@link com.mgmtp.a12.kernel.md.model.api.IGroup} {@code G2}.
	 * When an {@link DocumentV2} of {@code DM} with an {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2} of
	 * {@code G1}, which does <b>not</b> contain any {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2} of
	 * {@code F} and also <b>no</b> {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2} of {@code G2}, is
	 * serialized, the result will contain the following snippet for {@code G1}:
	 * <ul>
	 * <li>For XML: <code>&lt;G1&gt;&lt;/G1&gt;</code></li>
	 * <li>For JSON: <code>"G1" : { }</code></li>
	 * </ul>
	 * </p>
	 * <p>
	 * The field values will not get (formally) validated. If the {@link BasicTypeV2 BasicType} of a field value does not match the
	 * field from the {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel}, the behavior is unspecified.
	 * If only valid values (value matches the type in the respective {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel}) should be written, then the document
	 * has to be validated before serialization.
	 *
	 * @param document
	 * 		the {@link DocumentV2} to be serialized. May not be {@code null}.
	 * @param writer
	 * 		the writer that obtains the serialized document. May not be {@code null}.
	 * @param config
	 * 		configuration of the serialization. May not be {@code null}.
	 */
	void serializeV2(@NonNull DocumentV2 document, @NonNull Writer writer, @NonNull DocumentSerializationConfig config);
}
