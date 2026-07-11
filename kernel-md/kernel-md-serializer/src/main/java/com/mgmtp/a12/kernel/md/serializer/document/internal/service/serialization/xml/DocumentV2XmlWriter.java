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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.simplexmlnode.XmlElement;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.simplexmlnode.XmlWriter;

import lombok.NonNull;

public class DocumentV2XmlWriter {

	private DocumentV2XmlWriter() {
		throw new IllegalStateException("This class should not be instantiated, use the static methods instead.");
	}

	public static void writeDocument(@NonNull DocumentV2 document, @NonNull IDocumentModelResolver modelResolver,
			@NonNull Writer writer)
			throws DocumentSerializationException {
		XmlElement documentXmlNode;
		try {
			documentXmlNode = writeDocument(document, modelResolver);
		} catch (XmlElement.InvalidXmlCharacterException e) {
			throw new DocumentSerializationException("Document contains invalid characters.", e);
		}
		formatXml(documentXmlNode, writer);
	}

	private static XmlElement writeDocument(@NonNull DocumentV2 document, @NonNull IDocumentModelResolver modelResolver) {
		ISerializationInfo info = new DocumentJsonAndXmlSerializationInfoImpl(modelResolver, document.getDocumentModelId());
		return documentToXml(document, info);
	}

	private static void formatXml(XmlElement docXml, Writer writer)
			throws DocumentSerializationException {
		String xmlString = XmlWriter.serializeToString(docXml);
		try {
			writer.write(xmlString);
		} catch (final IOException e) {
			throw new DocumentSerializationException("Error while writing to Writer.", e);
		}
	}

	private static XmlElement documentToXml(@NonNull DocumentV2 document, @NonNull ISerializationInfo info) {
		Map<String, String> docAttributes = new HashMap<>(Map.of(
				"xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"));

		document.getId().ifPresent(id -> docAttributes.put(DocumentSerializerImpl.ID, id));

		return groupToXml(document.getDocumentModelId(), document.toRootRootGroup(), info).withAttributesUpdated(docAttributes);
	}

	private static XmlElement groupToXml(@NonNull String ownName, @NonNull GroupInstanceV2 group, ISerializationInfo info) {
		List<XmlElement> children = new ArrayList<>();
		// Note about the ordering of child elements
		// - fields before groups (in principle this is arbitrary, but it seems a bit nicer for readability, and it matches
		//   the ordering of our json serialization
		// - fields vs fields as well as groups vs groups are sorted alphabetically by name
		group.directFields().stream().sorted(Map.Entry.comparingByKey()).forEach(fieldEntry -> {
			ISerializationInfo infoForChild = info.forChild(fieldEntry.getKey());
			children.add(fieldToXml(fieldEntry.getKey(), fieldEntry.getValue(), infoForChild));
		});

		group.directSubgroups().stream().sorted(Map.Entry.comparingByKey()).forEach(repsEntry -> {
			String subGroupName = repsEntry.getKey();
			ISerializationInfo infoForChild = info.forChild(subGroupName);

			RepetitionsV2 repetitions = repsEntry.getValue();
			IntStream.range(1, repetitions.size() + 1).forEach(i ->
					children.add(groupToXml(subGroupName, repetitions.get(i), infoForChild.atIndex(i))));
		});

		return XmlElement.of(ownName, Map.of(), children);
	}

	private static XmlElement fieldToXml(@NonNull String ownName, @NonNull FieldInstanceV2 field,
			@NonNull ISerializationInfo info) {
		return info.toXmlFieldNode(ownName, field.value());
	}

	public interface ISerializationInfo {
		/**
		 * Converts the value to an XML value. A value should always be convertible.
		 * The implementation chooses the suitable conversion.
		 */
		XmlElement toXmlFieldNode(String ownName, Object value);

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
