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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.LocalizedTextMapBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.model.header.Annotation;

public class ElementDeserializer extends JsonDeserializer<Element> {

	@Override
	public Element deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonNode node = p.readValueAsTree();
		String elementTypeName = node.get("type").asText();
		Element element = deriveElement(node, elementTypeName);

		element.setAnnotations(getAnnotations(node));

		element.setId(node.get("id").asText());
		element.setName(node.get("name").asText());
		element.setExternalDescription(getDescriptionText(node, "externalDescription"));
		element.setInternalDescription(getDescriptionText(node, "internalDescription"));

		if (element instanceof Group) {
			Group parent = (Group) element;
			parent.getElements().stream().forEach(child -> child.setParent(parent));
		}

		return element;
	}

	private List<Annotation> getAnnotations(JsonNode node) {
		JsonNode annotationsNode = node.get("annotations");
		if ((annotationsNode) != null && !annotationsNode.isEmpty()) {
			return JsonMapper.getMapper().convertValue(annotationsNode, new TypeReference<List<Annotation>>() {
			});
		}
		return new ArrayList<>();
	}

	private ILocalizedTextMap getDescriptionText(JsonNode node, String textfieldName) throws JsonProcessingException {
		JsonNode textNode = node.get(textfieldName);
		if ((textNode) != null) {
			return JsonMapper.getMapper().treeToValue(textNode, ILocalizedTextMap.class);
		}
		return new LocalizedTextMapBuilder().build();
	}

	private Element deriveElement(JsonNode elementNode, String elementTypeName) throws IOException {
		Element element = null;
		JsonNode typeNode = elementNode.get(elementTypeName);
		switch (elementTypeName) {
			case "Field":
				element = JsonMapper.getMapper().treeToValue(typeNode, Field.class);
				if (element == null) {
					element = new Field();
				}
				break;
			case "Group":
				element = JsonMapper.getMapper().treeToValue(typeNode, Group.class);
				if (element == null) {
					element = new Group();
				}
				final Group group = (Group) element;
				JsonNode excludeRulesNode = typeNode.get("excludeRules");
				if (excludeRulesNode != null) {
					group.getIncludeDetails().ifPresent(id -> id.setExcludeRules(excludeRulesNode.booleanValue()));
				}
				JsonNode excludeComputations = typeNode.get("excludeComputations");
				if (excludeComputations != null) {
					group.getIncludeDetails().ifPresent(id -> id.setExcludeComputations(excludeComputations.booleanValue()));
				}
				// Note: it is a bit ugly that we need to handle the includeLevel attribute here, even though it actually
				// is an attribute of IncludeDetails (not directly of the group). But in the json representation, the IncludeDetails
				// don't have their own corresponding tree node - instead the include details are flattened into the group (modelAlias
				// includeLevel, excludeRules, excludeComputations).
				if (typeNode.get("includeLevel") != null) {
					assert group.getIncludeDetails().isPresent()
							: "expect includeDetails to be non-null for groups that have a includeLevel attribute (group name " + element.getName() + ")";
					group.getIncludeDetails().ifPresent(id ->
							id.setIncludeLevel(IGroup.IIncludeDetails.IncludeLevel.valueOf(typeNode.get("includeLevel").asText())));
				}
				break;
			case "Rule":
				element = JsonMapper.getMapper().treeToValue(typeNode, Rule.class);
				if (element == null) {
					element = new Rule();
				}
				break;
			case "Computation":
				element = JsonMapper.getMapper().treeToValue(typeNode, Computation.class);
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
