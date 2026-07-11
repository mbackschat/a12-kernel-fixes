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
import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;

import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.node.JsonNodeType;

public class LocalizedHintListDeserializer extends StdDeserializer<StringType.HintList> {

	public LocalizedHintListDeserializer() {
		this(StringType.HintList.class);
	}

	protected LocalizedHintListDeserializer(Class<?> clazz) {
		super(clazz);
	}

	@Override
	public StringType.HintList deserialize(JsonParser parser, DeserializationContext ctxt) {
		StringType.HintList.HintListBuilder builder = StringType.HintList.builder();
		JsonNode localizedHintListNode = parser.readValueAsTree();
		Locale locale = getLocaleFromLocalizedHintList(localizedHintListNode);
		List<String> hintList = getValuesFromLocalizedHintList(localizedHintListNode);
		builder.locale(locale);
		builder.values(hintList);
		return builder.build();
	}

	private Locale getLocaleFromLocalizedHintList(JsonNode localizedText) {
		JsonNode localeNode = localizedText.get(LocalizedHintListSerializer.LOCALE_KEY);
		if (localeNode == null) {
			throw new IllegalArgumentException("Missing locale for localized text");
		}
		if (localeNode.getNodeType() != JsonNodeType.STRING) {
			throw new IllegalArgumentException("Unexpected node type for localized text: " + localeNode.getNodeType());
		}
		String localeStr = localeNode.asString();
		return LocaleUtils.toLocale(localeStr);
	}

	private List<String> getValuesFromLocalizedHintList(JsonNode localizedText) {
		JsonNode hintListNode = localizedText.get(LocalizedHintListSerializer.VALUES_KEY);
		if (hintListNode == null) {
			throw new IllegalArgumentException("Missing localized text text");
		}
		if (hintListNode.getNodeType() != JsonNodeType.ARRAY) {
			throw new IllegalArgumentException("Unexpected node type for localized text: " + hintListNode.getNodeType());
		}
		List<String> hintListValues = new ArrayList<>();
		hintListNode.forEach(e -> hintListValues.add(e.asString()));
		return hintListValues;
	}
}
