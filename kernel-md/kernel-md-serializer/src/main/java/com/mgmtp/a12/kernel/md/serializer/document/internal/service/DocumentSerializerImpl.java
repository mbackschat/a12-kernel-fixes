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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig.Format;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationException;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSerializer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV1V2ConverterImpl;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.RankedNotificationUtils;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.json.DocumentV2JsonReader;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.json.DocumentV2JsonWriter;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.DocumentV2XmlReader;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.DocumentV2XmlWriter;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.simplexmlnode.XmlElement;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.simplexmlnode.XmlReader;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.ObjectMapperProvider;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;

public class DocumentSerializerImpl implements IDocumentSerializer, IDocumentV2Serializer {
	public static final String ID = "id";

	private final IDocumentModelResolver documentModelResolver;

	public DocumentSerializerImpl(IDocumentModelResolver documentModelResolver) {
		// uses the wrapper as the user provided ModelResolver might also not implement the `getDocumentModelSearchService` method
		this.documentModelResolver = documentModelResolver;
	}

	@Override public void serializeV2(@NonNull DocumentV2 document, @NonNull Writer writer,
			@NonNull DocumentSerializationConfig config) {
		Objects.requireNonNull(config.getFormat());

		document = checkAndNormalizeDocumentForSerialization(document, config);

		if (config.getFormat() == Format.XML) {
			DocumentV2XmlWriter.writeDocument(document, documentModelResolver, writer, config.isFailIfUnreadableXML());
		} else if (config.getFormat() == Format.JSON) {
			DocumentV2JsonWriter.writeDocument(document, documentModelResolver, writer, config.getPrettyJsonPrinter());
		} else {
			throw new DocumentSerializationException(String.format("Format '%s' is not supported.", config.getFormat().toString()));
		}
	}

	@Override
	public void serialize(IDocument document, Writer writer, DocumentSerializationConfig config) {
		serializeV2(DocumentV1V2ConverterImpl.getInstance().ofDocumentV1(document), writer, config);
	}

	@Override public DocumentV2 deserializeV2(@NonNull Reader reader, @NonNull String documentModelId,
			@NonNull DocumentDeserializationConfig config, @NonNull Consumer<RankedNotification> notificationReceiver) {
		Validate.notBlank(documentModelId);

		return switch (config.getFormat()) {
			case XML -> {
				XmlElement xmlNode = parseXml(reader);
				yield deserializeV2(xmlNode, documentModelId, config, notificationReceiver);
			}
			case JSON -> {
				JsonNode jsonNode = parseJson(reader);
				yield deserializeV2(jsonNode, documentModelId, config, notificationReceiver);
			}
		};
	}

	public DocumentV2 deserializeV2(@NonNull XmlElement xml, @NonNull String documentModelId,
			@NonNull DocumentDeserializationConfig config, @NonNull Consumer<RankedNotification> notificationReceiver) {
		Validate.notBlank(documentModelId);

		if (config.getFormat() != Format.XML) {
			throw new IllegalArgumentException("Cannot deserialize an XML element as json.");
		}

		DocumentV2 doc =
				DocumentV2XmlReader.readDocument(documentModelId, documentModelResolver, xml, notificationReceiver, config);
		// does the postprocessing in this method as it public and can be used standalone
		doc = postProcessDeserializedDocument(documentModelId, config, doc);
		return doc;
	}

	@Override public DocumentV2 deserializeV2(@NonNull JsonNode json, @NonNull String documentModelId,
			@NonNull DocumentDeserializationConfig config, @NonNull Consumer<RankedNotification> notificationReceiver) {
		Validate.notBlank(documentModelId);

		if (config.getFormat() != Format.JSON) {
			throw new IllegalArgumentException("Cannot deserialize a json node as XML.");
		}

		DocumentV2 doc =
				DocumentV2JsonReader.readDocument(documentModelId, documentModelResolver, json, notificationReceiver, config);
		// does the postprocessing in this method as it public and can be used standalone
		doc = postProcessDeserializedDocument(documentModelId, config, doc);
		return doc;
	}

	@Override
	public IDocument deserialize(Reader reader, String documentModelId, DocumentDeserializationConfig config,
			Consumer<RankedNotification> notificationReceiver) {
		return DocumentV1V2ConverterImpl.getInstance()
				.toDocumentV1(deserializeV2(reader, documentModelId, config, notificationReceiver),
						config.getDocumentFactory());
	}

	@Override public IDocument deserialize(JsonNode json, String documentModelId, DocumentDeserializationConfig config,
			Consumer<RankedNotification> notificationReceiver) {
		return DocumentV1V2ConverterImpl.getInstance()
				.toDocumentV1(deserializeV2(json, documentModelId, config, notificationReceiver));
	}

	private static JsonNode parseJson(Reader reader) {
		JsonNode jsonNode;
		try {
			ObjectMapper objectMapper = ObjectMapperProvider.createObjectMapperForDocSerializer();
			jsonNode = objectMapper.readTree(reader);
		} catch (final IOException ioe) {
			throw new DocumentSerializationException("Error while creating a parser from the reader.", ioe);
		}
		return jsonNode;
	}

	private XmlElement parseXml(Reader reader) {
		try {
			return XmlReader.deserialize(reader);
		} catch (XmlReader.XmlReaderException e) {
			throw new DocumentSerializationException("Error while creating a XML element from the reader.", e);
		}
	}

	private DocumentV2 postProcessDeserializedDocument(String documentModelId, DocumentDeserializationConfig config,
			DocumentV2 document) {
		if (config.isRemoveEmptyFieldsAndGroups()) {
			document = DocumentV2Utils.removeEmptyFieldsAndGroups(document);
		}
		if (config.isAddTransientFields()) {
			document = DmAwareDocServiceImpl.ofResolver(documentModelId, documentModelResolver).addTransientFields(document);
		}
		return document;
	}

	private DocumentV2 checkAndNormalizeDocumentForSerialization(DocumentV2 document, DocumentSerializationConfig config) {
		DocumentV2 result = document;

		DmAwareDocServiceImpl dmAwareDocService = DmAwareDocServiceImpl.ofResolver(document.getDocumentModelId(),
				documentModelResolver);

		DmAwareDocServiceImpl.DocumentAndUnknowns documentAndUnknowns = dmAwareDocService.removeAndReturnUnknowns(result);

		if (!config.isIgnoreUnknownEntities()) {
			List<DmAwareDocServiceImpl.Unknown> unknowns = documentAndUnknowns.unknowns();
			if (!unknowns.isEmpty()) {
				throw new DocumentSerializationException(
						String.format("The document contains the following unknowns:%n%s",
								unknowns.stream().map(Object::toString).collect(
										Collectors.joining(System.lineSeparator()))));
			}
		}

		result = documentAndUnknowns.document();

		List<RankedNotification> notifications = new ArrayList<>();
		if (!dmAwareDocService.checkStructuralConsistencyWithDm(result, notifications::add)) {
			String problems = RankedNotificationUtils.joinMessages(notifications);
			throw new DocumentSerializationException(
					String.format("Following errors were found:%n%s", problems));
		}

		if (config.isSkipTransientFields()) {
			result = dmAwareDocService.removeTransientFields(result);
		}
		if (config.isRemoveEmptyFieldsAndGroups()) {
			result = DocumentV2Utils.removeEmptyFieldsAndGroups(result);
		}
		return result;
	}
}
