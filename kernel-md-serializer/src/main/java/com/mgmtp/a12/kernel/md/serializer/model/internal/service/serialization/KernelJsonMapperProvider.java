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
import com.mgmtp.a12.model.serialization.A12DefaultJsonPrettyPrinter;

import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Provides a Jackson {@link ObjectMapper} for JSON (de)serialization, (currently) specifically for
 * document models.
 */
public class KernelJsonMapperProvider {

	private KernelJsonMapperProvider() {
		throw new IllegalStateException("Utility class");
	}

	public static JsonMapper.Builder createPreconfiguredObjectMapper() {
		return createObjectMapperForDocSerializer()
			.enable(SerializationFeature.INDENT_OUTPUT)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
			.changeDefaultVisibility(vc -> vc.with(Visibility.NONE)
					.withGetterVisibility(Visibility.PUBLIC_ONLY)
					.withIsGetterVisibility(Visibility.PUBLIC_ONLY))
			.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
			.defaultPrettyPrinter(new A12DefaultJsonPrettyPrinter());
	}

	public static JsonMapper.Builder createObjectMapperForDocSerializer() {
		return JsonMapper.builder()
				.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
				.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
				.enable(JsonReadFeature.ALLOW_YAML_COMMENTS)
				// we want to preserve trailing zeroes and the scale of the value
				.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
				.disable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES)
				.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
				.disable(StreamWriteFeature.AUTO_CLOSE_TARGET);
	}
}
