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
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.VariantFilter;

public class RuleSerializer extends StdSerializer<Rule> {

	private static final long serialVersionUID = 5841770312742112434L;

	public RuleSerializer() {
		this(null);
	}

	public RuleSerializer(Class<Rule> t) {
		super(t);
	}

	@Override
	public void serialize(Rule value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("errorEntityRelPath", value.getErrorEntity().getRelativePath());
		gen.writeStringField("errorCode", value.getErrorCode());
		gen.writeStringField("errorCondition", value.getErrorCondition());
		gen.writeObjectField("severity", value.getSeverity());
		if (!value.getErrorMessage().isEmpty()) {
			gen.writeFieldName("errorMessage");
			JsonMapper.getWriterForNoNonEmpty().writeValue(gen, value.getErrorMessage());
		}
		if (!value.getGuiPatterns().isEmpty()) {
			gen.writeFieldName("guiPatterns");
			JsonMapper.getWriterForNoNonEmpty().writeValue(gen, value.getGuiPatterns());
		}
		Optional<VariantFilter> variantFilter = value.getVariantFilter();
		if (variantFilter.isPresent()) {
			gen.writeFieldName("variantFilter");
			JsonMapper.getWriterForNoNonEmpty().writeValue(gen, variantFilter.get());
		}
		if (!value.getVariantErrorMessages().isEmpty()) {
			gen.writeFieldName("variantErrorMessages");
			JsonMapper.getWriterForNoNonEmpty().writeValue(gen, value.getVariantErrorMessages());
		}
		gen.writeEndObject();
	}

}
