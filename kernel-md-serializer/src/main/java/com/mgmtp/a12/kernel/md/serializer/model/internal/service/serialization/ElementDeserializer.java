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

package com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.LocalizedTextMapBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.model.header.Annotation;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class ElementDeserializer extends ValueDeserializer<Element> {

	@Override
	public Element deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
		JsonNode node = p.readValueAsTree();
		String elementTypeName = node.get("type").asString();
		Element element = deriveElement(node, elementTypeName);

		element.setAnnotations(getAnnotations(node));

		if (node.hasNonNull("id")) {
			element.setId(node.get("id").asString());
		}
		if (node.hasNonNull("name")) {
			element.setName(node.get("name").asString());
		}
		element.setExternalDescription(getDescriptionText(node, "externalDescription"));
		element.setInternalDescription(getDescriptionText(node, "internalDescription"));
		if (node.hasNonNull("changeNote")) {
			element.setChangeNote(node.get("changeNote").asString());
		}

		if (element instanceof Group parent) {
			parent.getElements().forEach(child -> child.setParent(parent));
		}

		return element;
	}

	private List<Annotation> getAnnotations(JsonNode node) {
		JsonNode annotationsNode = node.get("annotations");
		if ((annotationsNode) != null && !annotationsNode.isEmpty()) {
			return KernelJsonMapper.getMapper().convertValue(annotationsNode, new TypeReference<List<Annotation>>() {
			});
		}
		return new ArrayList<>();
	}

	private ILocalizedTextMap getDescriptionText(JsonNode node, String textfieldName) throws JacksonException {
		JsonNode textNode = node.get(textfieldName);
		if ((textNode) != null) {
			return KernelJsonMapper.getMapper().treeToValue(textNode, ILocalizedTextMap.class);
		}
		return new LocalizedTextMapBuilder().build();
	}

	private Element deriveElement(JsonNode elementNode, String elementTypeName) {
		Element element = null;
		JsonNode typeNode = elementNode.get(elementTypeName);
		switch (elementTypeName) {
			case "Field":
				element = KernelJsonMapper.getMapper().treeToValue(typeNode, Field.class);
				if (element == null) {
					element = new Field();
				}
				break;
			case "Group":
				element = KernelJsonMapper.getMapper().treeToValue(typeNode, Group.class);
				if (element == null) {
					element = new Group();
				}

				break;
			case "Rule":
				element = KernelJsonMapper.getMapper().treeToValue(typeNode, Rule.class);
				if (element == null) {
					element = new Rule();
				}
				break;
			case "Computation":
				element = KernelJsonMapper.getMapper().treeToValue(typeNode, Computation.class);
				if (element == null) {
					element = new Computation();
				}
				break;
			default:
				throw new IllegalArgumentException(
						String.format("Element type := \"%s\" not valid - check the .json template", elementTypeName));
		}

		return element;
	}
}
