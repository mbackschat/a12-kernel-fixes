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

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class XmlReader {
	private XmlReader() {
		throw new IllegalStateException("This class should not be instantiated, use the static methods instead.");
	}

	/**
	 * Deserialize XML from a String into an XmlElement structure.
	 */
	public static XmlElement deserialize(String xml) {
		return deserialize(new StringReader(xml));
	}

	/**
	 * Deserialize XML from a Reader into an XmlElement structure.
	 */
	public static XmlElement deserialize(Reader reader) {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			// Disable external entity processing for security
			factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
			factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

			XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);

			// Skip to the first start element (the xml standard only allows the declaration or comments before the root element)
			//noinspection StatementWithEmptyBody
			while (xmlReader.hasNext() && xmlReader.next() != XMLStreamConstants.START_ELEMENT) {
				// Just advance to the first element
			}

			XmlElement result = parseElement(xmlReader);
			xmlReader.close();
			return result;
		} catch (XMLStreamException e) {
			throw new XmlReaderException(e);
		}
	}

	/**
	 * Wrapper for some throwable. The purpose of this class is to more clearly distinguish
	 * the exceptions that come out of the {@link #deserialize} method.
	 */
	public static class XmlReaderException extends RuntimeException {
		public XmlReaderException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * Parse a single XML element and its content.
	 */
	@SuppressWarnings("java:S1119") // usage of labels
	private static XmlElement parseElement(XMLStreamReader reader) throws XMLStreamException {
		Validate.isTrue(reader.isStartElement());
		String elementName = joinNonEmptyWithColons(reader.getPrefix(), reader.getLocalName());

		Map<String, String> attributes = new HashMap<>();
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String attrName = joinNonEmptyWithColons(reader.getAttributePrefix(i), reader.getAttributeLocalName(i));
			String attrValue = reader.getAttributeValue(i);
			attributes.put(attrName, attrValue);
		}
		for (int i = 0; i < reader.getNamespaceCount(); i++) {
			String attrName = joinNonEmptyWithColons("xmlns", reader.getNamespacePrefix(i));
			String attrValue = reader.getNamespaceURI(i);
			attributes.put(attrName, attrValue);
		}

		List<XmlElement> children = new ArrayList<>();
		StringBuilder textContent = new StringBuilder();

		XmlElement result = null;
		label_w:
		while (reader.hasNext()) {
			int eventType = reader.next();
			switch (eventType) {
				// we handle CDATA the same as normal text, but do not write CDATA with the writer ourselves
				case XMLStreamConstants.CDATA, XMLStreamConstants.CHARACTERS -> textContent.append(reader.getText());
				case XMLStreamConstants.START_ELEMENT -> children.add(parseElement(reader));
				case XMLStreamConstants.END_ELEMENT -> {
					result = XmlElement.of(elementName, attributes, children, textContent.toString());
					break label_w; // return and don't advance the reader any further, the caller expects to continue reading from here
				}
				case XMLStreamConstants.COMMENT -> {
					// ignore comments
				}
				default -> {
					assert false : "Unexpected event type: " + eventType;
				}
			}
		}
		if (result == null) {
			throw new XMLStreamException("Unexpected end of input");
		}
		return result;
	}

	private static String joinNonEmptyWithColons(String prefix, String local) {
		return Stream.of(prefix, local).filter(Predicate.not(StringUtils::isEmpty)).collect(Collectors.joining(":"));
	}
}