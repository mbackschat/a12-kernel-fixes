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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.stream.IntStream;

import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.DocumentJsonAndXmlSerializationInfoImpl;

import lombok.NonNull;
import tools.jackson.core.PrettyPrinter;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ValueNode;

public class DocumentV2JsonWriter {
	protected static final JsonNodeFactory JSON_FACTORY = JsonNodeFactory.instance;

	private DocumentV2JsonWriter() {
		throw new IllegalStateException("This class should not be instantiated, use the static methods instead.");
	}

	/**
	 * set PrettyPrinter pp = null to deactivate pretty printing
	 */
	public static void writeDocument(@NonNull DocumentV2 document, @NonNull IDocumentModelResolver modelResolver,
			@NonNull Writer writer, PrettyPrinter pp)
			throws DocumentSerializationException {
		JsonNode docJson = writeDocument(document, modelResolver);
		formatJson(docJson, writer, pp);
	}

	// note: we should probably require a DocumentV2 (not any GroupInstanceV2)
	// here, because we cannot actually correctly serialize groups that
	// have direct fields
	public static JsonNode writeDocument(@NonNull DocumentV2 document, @NonNull IDocumentModelResolver modelResolver) {
		ISerializationInfo info = new DocumentJsonAndXmlSerializationInfoImpl(modelResolver, document.getDocumentModelId());
		return documentToJson(document, info);
	}

	private static void formatJson(JsonNode docJson, Writer writer, PrettyPrinter pp) {
		JsonMapper mapper = JsonMapper.builder()
				.enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
				.disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
				.defaultPrettyPrinter(pp)
				.build();
		if (pp != null) {
			mapper.writerWithDefaultPrettyPrinter().writeValue(writer, docJson);
		} else {
			mapper.writeValue(writer, docJson);
		}

		try {
			writer.flush();
		} catch (final IOException ioe) {
			throw new DocumentSerializationException("Error while creating/writing JsonGenerator.", ioe);
		}
	}

	private static ObjectNode documentToJson(@NonNull DocumentV2 documentV2, @NonNull ISerializationInfo info) {
		GroupInstanceV2 rootOfRoots = documentV2.toRootRootGroup();
		if (documentV2.getId().isPresent()) {
			rootOfRoots = rootOfRoots.withDirectFieldValue(DocumentSerializerImpl.ID, documentV2.getId().get());
		}

		return groupToJson(rootOfRoots, info);
	}

	private static ObjectNode groupToJson(@NonNull GroupInstanceV2 group, @NonNull ISerializationInfo info) {
		ObjectNode groupNode = JSON_FACTORY.objectNode();

		// we are not checking if the fieldValue has the correct type given by fieldElement, the user would need to validate
		group.directFields().stream().sorted(Map.Entry.comparingByKey()).forEach(field -> {
			ISerializationInfo infoForChild = info.forChild(field.getKey());
			groupNode.set(field.getKey(), fieldToJson(field.getValue(), infoForChild));
		});

		group.directSubgroups().stream().sorted(Map.Entry.comparingByKey()).forEach(subgroup -> {
			ISerializationInfo infoForChild = info.forChild(subgroup.getKey());
			groupNode.set(subgroup.getKey(), repetitionsToJson(subgroup.getValue(), infoForChild));
		});

		return groupNode;
	}

	private static JsonNode repetitionsToJson(@NonNull RepetitionsV2 repetitions, @NonNull ISerializationInfo info) {
		if (info.isGroupRepeatable()) {
			ArrayNode repetitionsNode = JSON_FACTORY.arrayNode();

			IntStream.range(1, repetitions.size() + 1).forEach(i ->
					repetitionsNode.add(groupToJson(repetitions.get(i), info.atIndex(i)))
			);

			return repetitionsNode;
		} else {
			assert repetitions.size() <= 1 : "excessive repetitions for non-repeatable groups should have already been rejected";
			return groupToJson(repetitions.get(1), info);
		}
	}

	private static ValueNode fieldToJson(@NonNull FieldInstanceV2 field, @NonNull ISerializationInfo info) {
		return info.toJsonFieldNode(field.value());
	}

	public interface ISerializationInfo {
		/**
		 * Converts the value to a Json value. A value should always be convertible.
		 * The implementation chooses the suitable conversion.
		 * <p/>
		 * Note: if the value cannot be converted, this method may throw an exception (which may abort the serialization), if no
		 * fallback behavior is used that might set string values instead.
		 * The intention is that documents are supposed to be checked for unknown or incompatible entries
		 * before serialization.
		 */
		ValueNode toJsonFieldNode(Object value);

		/**
		 * The implementation decides if the current element can be a repeatable group
		 */
		boolean isGroupRepeatable();

		/**
		 * Create a new info with the child elements name.
		 */
		ISerializationInfo forChild(String childName);

		/**
		 * Changes the info to have a specific repetitionIndex.
		 */
		ISerializationInfo atIndex(int repetitionIndex);
	}
}
