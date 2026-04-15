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
import java.util.Optional;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.RequirednessConfig;
import com.mgmtp.a12.kernel.md.model.a12internal.VariantFilter;

public class FieldSerializer extends StdSerializer<Field> {

	private static final long serialVersionUID = -996567860450043243L;

	public FieldSerializer() {
		this(null);
	}

	public FieldSerializer(Class<Field> t) {
		super(t);
	}

	@Override
	public void serialize(Field value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeObjectField("fieldType", value.getFieldType());
		if (!value.getLabel().isEmpty()) {
			gen.writeFieldName("label");
			gen.writeObject(value.getLabel());
		}
		SerializationUtils.writeBoolean(gen, "global", value.isGlobal());
		SerializationUtils.writeBoolean(gen, "transient", value.isTransient());
		Optional<RequirednessConfig> requirednessConfig = value.getRequirednessConfig();
		if (requirednessConfig.isPresent()) {
			gen.writeFieldName("requirednessConfig");
			gen.writeObject(requirednessConfig.get());
		}
		Optional<VariantFilter> variantFilter = value.getVariantFilter();
		if (variantFilter.isPresent()) {
			gen.writeFieldName("variantFilter");
			gen.writeObject(variantFilter.get());
		}
		if (!value.getHelperText().isEmpty()) {
			gen.writeFieldName("helperText");
			gen.writeObject(value.getHelperText());
		}
		Optional<String> addInfoOpt = value.getAdditionalInfo();
		if (addInfoOpt.isPresent()) {
			gen.writeFieldName("additionalInfo");
			gen.writeObject(addInfoOpt.get());
		}
		gen.writeEndObject();
	}

}
