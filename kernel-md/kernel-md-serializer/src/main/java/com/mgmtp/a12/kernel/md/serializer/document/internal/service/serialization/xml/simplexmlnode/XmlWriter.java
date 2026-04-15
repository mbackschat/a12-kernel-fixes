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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.simplexmlnode;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Comparator;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationException;

public class XmlWriter {

	public static final String BASE_INDENT = StringUtils.repeat(" ", 2);

	private XmlWriter() {
		throw new IllegalStateException("This class should not be instantiated, use the static methods instead.");
	}

	public static String serializeToString(XmlElement element) {
		StringWriter writer = new StringWriter();
		XmlWriter.serialize(element, writer);
		return writer.toString();
	}

	public static void serialize(XmlElement element, Writer writer) {
		XMLStreamWriter xmlStreamWriter;
		try {
			xmlStreamWriter = XMLOutputFactory.newDefaultFactory().createXMLStreamWriter(writer);
			xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
			xmlStreamWriter.writeCharacters("\n");
			serializeInternal(element, 0, xmlStreamWriter);
			xmlStreamWriter.writeEndDocument();
			xmlStreamWriter.flush();
		} catch (XMLStreamException e) {
			throw new DocumentSerializationException("Error while creating/writing XMLStreamWriter.", e);
		}
	}

	/**
	 * expects that the writer is currently at the start of a new line. restores that invariant when returning
	 */
	private static void serializeInternal(XmlElement element, int indentLevel, XMLStreamWriter writer) throws XMLStreamException {
		String indent = StringUtils.repeat(BASE_INDENT, indentLevel);

		writer.writeCharacters(indent);
		if(element.children().isEmpty() && element.textContent().isEmpty()) {
			writer.writeEmptyElement(element.name());
			writeAttributes(element, writer);
		} else if(!element.textContent().isEmpty()) {
			assert element.children().isEmpty() : "this should already be guaranteed by the XmlElement constructor";
			writer.writeStartElement(element.name());
			writeAttributes(element, writer);
			writer.writeCharacters(element.textContent());
			writer.writeEndElement();
		} else {
			writer.writeStartElement(element.name());
			writeAttributes(element, writer);
			writer.writeCharacters("\n");
			for (var child : element.children()) {
				serializeInternal(child, indentLevel + 1, writer);
			}
			writer.writeCharacters(indent);
			writer.writeEndElement();
		}
		writer.writeCharacters("\n");
	}

	private static void writeAttributes(XmlElement element, XMLStreamWriter writer) throws XMLStreamException {
		for (var entry : element.attributes().entrySet().stream()
				// attributes do not have an inherent ordering, so we normalize the order in which they occur in the string
				// representation
				.sorted(Comparator.comparing((Map.Entry<String, String> k) -> !k.getKey().startsWith("xmlns"))
						.thenComparing(Map.Entry.comparingByKey())).toList()
		) {
			writer.writeAttribute(entry.getKey(), entry.getValue());
		}
	}
}