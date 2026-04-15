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

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import com.mgmtp.a12.model.serialization.A12DefaultJsonPrettyPrinter;

/**
 * Provides a Jackson {@link ObjectMapper} for JSON (de)serialization, (currently) specifically for
 * document models.
 */
public class ObjectMapperProvider {

	private ObjectMapperProvider() {
		throw new IllegalStateException("Utility class");
	}

	public static ObjectMapper createPreconfiguredObjectMapper() {
		return new ObjectMapper()
				.enable(SerializationFeature.INDENT_OUTPUT)
				.enable(JsonParser.Feature.ALLOW_COMMENTS)
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
				.setVisibility(PropertyAccessor.ALL, Visibility.NONE)
				.setVisibility(PropertyAccessor.GETTER, Visibility.PUBLIC_ONLY)
				.setVisibility(PropertyAccessor.IS_GETTER, Visibility.PUBLIC_ONLY)
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
				.configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false)
				.setDefaultPrettyPrinter(new A12DefaultJsonPrettyPrinter());
	}

	public static ObjectMapper createObjectMapperForDocSerializer() {
		return new ObjectMapper()
				.enable(JsonParser.Feature.ALLOW_COMMENTS)
				// we want to preserve trailing zeroes and the scale of the value
				.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
				.configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false);
	}
}
