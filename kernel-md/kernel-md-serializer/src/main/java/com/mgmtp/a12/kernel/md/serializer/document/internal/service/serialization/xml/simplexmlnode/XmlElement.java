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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

public record XmlElement(
		@NonNull String name,
		@NonNull Map<String, String> attributes,
		@NonNull List<XmlElement> children,
		@NonNull String textContent
) {
	public XmlElement {
		children = Collections.unmodifiableList(new ArrayList<>(children));
		if (!children.isEmpty()) {
			if (!textContent.isBlank()) {
				throw new IllegalArgumentException(
						"Found child element/s and non blank textContent, we do not allow for mixed content");
			}
			// by our definition an element with children has an empty textContent
			textContent = "";
		}
		// Normalize the end-of-line representations here, because differences would be lost during serialization/deserialization,
		// which would violate our design goal of deserialize(serialize(x)) == x
		// see also https://www.w3.org/TR/2008/REC-xml-20081126/#sec-line-ends
		textContent = textContent.replaceAll("(\r\n|\r)", "\n");
		// Note: attribute values would technically also need normalization, but for now we don't care because we don't expect this
		// to happen here
		attributes = Collections.unmodifiableMap(new HashMap<>(attributes));
	}

	/**
	 * @param children
	 * 		(must be empty if textContent is non-empty)
	 * @param textContent
	 * 		(must be empty if children is non-empty)
	 */
	public static XmlElement of(
			@NonNull String name,
			@NonNull Map<String, String> attributes,
			@NonNull List<XmlElement> children,
			@NonNull String textContent
	) {
		return new XmlElement(name, attributes, children, textContent);
	}

	/**
	 * creates an element that can have attributes and children (but not textContent)
	 */
	public static XmlElement of(
			@NonNull String name,
			@NonNull Map<String, String> attributes,
			@NonNull List<XmlElement> children
	) {
		return new XmlElement(name, attributes, children, "");
	}

	/**
	 * creates an element that can have attributes and textContent (but no children)
	 */
	public static XmlElement of(
			@NonNull String name,
			@NonNull Map<String, String> attributes,
			@NonNull String textContent
	) {
		return new XmlElement(name, attributes, List.of(), textContent);
	}

	/**
	 * creates an element with no attributes that can have a textContent (but no children)
	 */
	public static XmlElement of(
			@NonNull String name,
			@NonNull String textContent
	) {
		return new XmlElement(name, Map.of(), List.of(), textContent);
	}

	public XmlElement withAttributesUpdated(@NonNull Map<String, String> attributesUpdate) {
		var newAttrs = new HashMap<>(attributes);
		newAttrs.putAll(attributesUpdate);
		return new XmlElement(name, newAttrs, children, textContent);
	}
}
