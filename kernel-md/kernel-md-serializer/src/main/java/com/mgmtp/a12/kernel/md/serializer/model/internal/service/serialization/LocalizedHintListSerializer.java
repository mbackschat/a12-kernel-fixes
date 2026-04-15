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
import java.io.Serial;
import java.io.UncheckedIOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;

public class LocalizedHintListSerializer extends StdSerializer<StringType.HintList> {

	@Serial private static final long serialVersionUID = -3422521314367054070L;

	public static final String LOCALE_KEY = "locale";
	public static final String VALUES_KEY = "values";

	public LocalizedHintListSerializer() {
		this(null);
	}

	public LocalizedHintListSerializer(Class<StringType.HintList> t) {
		super(t);
	}

	@Override
	public void serialize(StringType.HintList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		List<String> hintList = value.getValues();
		if (hintList.isEmpty()) {
			return;
		}
		try {
			gen.writeStartObject();
			gen.writeStringField(LOCALE_KEY, value.getLocale().toString());
			gen.writeArrayFieldStart(VALUES_KEY);
			for (String hint : hintList) {
				gen.writeObject(hint);
			}
			gen.writeEndArray();
			gen.writeEndObject();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
