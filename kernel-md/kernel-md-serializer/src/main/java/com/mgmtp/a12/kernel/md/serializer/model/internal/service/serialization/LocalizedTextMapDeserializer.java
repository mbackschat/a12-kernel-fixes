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

import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;

import com.mgmtp.a12.kernel.md.model.a12internal.LocalizedTextMapBuilder;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.node.JsonNodeType;

public class LocalizedTextMapDeserializer extends StdDeserializer<ILocalizedTextMap> {

	public LocalizedTextMapDeserializer() {
		this(ILocalizedTextMap.class);
	}

	protected LocalizedTextMapDeserializer(Class<?> clazz) {
		super(clazz);
	}

	@Override
	public ILocalizedTextMap deserialize(JsonParser parser, DeserializationContext ctxt) {
		LocalizedTextMapBuilder builder = new LocalizedTextMapBuilder();

		JsonNode arr = parser.readValueAsTree();
		for (JsonNode entry : arr) {
			Locale locale = getLocaleFromLocalizedText(entry);
			String text = getTextFromLocalizedText(entry);
			builder.add(locale, text);
		}
		return builder.build();
	}

	private Locale getLocaleFromLocalizedText(JsonNode localizedText) {
		JsonNode localeNode = localizedText.get(LocalizedTextMapSerializer.LOCALE_KEY);
		if (localeNode == null) {
			throw new IllegalArgumentException("Missing locale for localized text");
		}
		if (localeNode.getNodeType() != JsonNodeType.STRING) {
			throw new IllegalArgumentException("Unexpected node type for localized text: " + localeNode.getNodeType());
		}
		String localeStr = localeNode.asString();
		return LocaleUtils.toLocale(localeStr);
	}

	private String getTextFromLocalizedText(JsonNode localizedText) {
		JsonNode textNode = localizedText.get(LocalizedTextMapSerializer.TEXT_KEY);
		if (textNode == null) {
			throw new IllegalArgumentException("Missing localized text text");
		}
		if (textNode.getNodeType() != JsonNodeType.STRING) {
			throw new IllegalArgumentException("Unexpected node type for localized text: " + textNode.getNodeType());
		}
		return localizedText.get(LocalizedTextMapSerializer.TEXT_KEY).asString();
	}
}
