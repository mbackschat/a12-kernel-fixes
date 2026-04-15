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
package com.mgmtp.a12.kernel.md.document.api.services;

import java.io.Reader;
import java.io.Writer;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.model.notification.RankedNotification;

/**
 * Serializer for a {@link IDocument}.
 *
 * @deprecated use {@code IDocumentV2Serializer} instead.
 */
@OnlyForUsage
@Deprecated(since = "30.0.0")
public interface IDocumentSerializer {

	/**
	 * Deserializes the document contained in the given {@link Reader}.
	 * <p>
	 * <b>Notes:</b>
	 * <ul>
	 * <li>While deserializing a {@link IFieldInstance}, the corresponding value will be converted
	 * from the string representation to the java type defined in the corresponding {@link IField}
	 * (see also
	 * {@link IDocumentService#convertToJavaType(IFieldInstance, String, Consumer)}). If an
	 * error occurred while converting the value, the error will be reported in the passed
	 * {@link Consumer} and the value in string format will be set to the corresponding
	 * {@link IFieldInstance}.</li>
	 * <li>The document id (if present) will be always handled as a string.</li>
	 * </ul>
	 *
	 * @param reader
	 * 		the reader that provides a string representation of the document to be
	 * 		deserialized. May not be {@code null}.
	 * @param documentModelId
	 * 		id of the corresponding {@link IDocumentModel}. May not be {@code null} or blank;
	 * @param config
	 * 		configuration for the deserialization. May not be {@code null}.
	 * @param notificationReceiver
	 * 		the {@link RankedNotification}s, found during the process, will be reported to this callback. May not be {@code null}.
	 * @return the deserialized {@link IDocument}.
	 *
	 * @throws DocumentSerializationException
	 * 		if problems occur that prevent further processing
	 * @throws UnsupportedOperationException
	 * 		in case the document id is neither a string nor a number
	 */
	IDocument deserialize(Reader reader, String documentModelId, DocumentDeserializationConfig config,
			Consumer<RankedNotification> notificationReceiver);

	/**
	 * similar to {@link #deserialize(Reader, String, DocumentDeserializationConfig, Consumer)} but uses a {@link JsonNode} instead
	 * of a {@link Reader} as input.
	 *
	 * @throws IllegalArgumentException
	 * 		if the configuration wants to deserialize XML from the {@link JsonNode}.
	 */
	IDocument deserialize(JsonNode json, String documentModelId, DocumentDeserializationConfig config,
			Consumer<RankedNotification> notificationReceiver);

	/**
	 * Serializes the given {@link IDocument}.
	 * <p>
	 * <b>Note:</b> While serializing, every time an {@link IGroupInstance} is present in the
	 * {@link IDocument}, the group will be inserted into the serialized document, even if no
	 * instances of the groups descendants are present.
	 * <b>Example:</b> Given an {@link IDocumentModel} {@code DM} containing an {@link IGroup}
	 * {@code G1} containing an {@link IField} {@code F} as well as an {@link IGroup} {@code G2}.
	 * When an {@link IDocument} of {@code DM} with an {@link IGroupInstance} of {@code G1}, which
	 * does <b>not</b> contain any {@link IFieldInstance} of {@code F} and also <b>no</b>
	 * {@link IGroupInstance} of {@code G2}, is serialized, the result will contain the following
	 * snippet for {@code G1}:
	 * <ul>
	 * <li>For XML: <code>&lt;G1&gt;&lt;/G1&gt;</code></li>
	 * <li>For JSON: <code>"G1" : { }</code></li>
	 * </ul>
	 * </p>
	 *
	 * @param document
	 * 		the {@link IDocument} to be serialized. May not be {@code null}.
	 * @param writer
	 * 		the writer that obtains the serialized document. May not be {@code null}.
	 * @param config
	 * 		configuration of the serialization. May not be {@code null}.
	 * @throws DocumentSerializationException
	 * 		if problems occur that prevent further processing. For example, if the
	 *        {@link IDocument} contains gaps in the repetitions: the {@link IDocument}
	 * 		contains G1[1] and G[3] but not G[2]. For other reasons see
	 *        {@link IDocumentService#checkEntities(Consumer)}.
	 */
	void serialize(IDocument document, Writer writer, DocumentSerializationConfig config);
}
