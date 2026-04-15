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
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;

public class ElementSerializer extends StdSerializer<Element> {

	private static final long serialVersionUID = 1915617763547198032L;

	private final GroupSerializer groupSerializer;
	private final FieldSerializer fieldSerializer;
	private final RuleSerializer ruleSerializer;
	private final ComputationSerializer computationSerializer;
	private final Set<String> elementIdsInResult;

	public ElementSerializer(Set<String> elementIdsInResult) {
		this(null, elementIdsInResult);
	}

	public ElementSerializer(Class<Element> t, Set<String> elementIdsInResult) {
		super(t);
		this.groupSerializer = new GroupSerializer(elementIdsInResult);
		this.fieldSerializer = new FieldSerializer();
		this.ruleSerializer = new RuleSerializer();
		this.computationSerializer = new ComputationSerializer();
		this.elementIdsInResult = elementIdsInResult;
	}

	@Override
	public void serialize(Element value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (!elementIdsInResult.isEmpty() && !elementIdsInResult.contains(value.getId())) {
			return;
		}
		gen.writeStartObject();
		ElementSerializerUtil.serializeElementBase(value, gen);
		gen.writeFieldName(value.getClass().getSimpleName());
		if (value instanceof Group) {
			groupSerializer.serialize((Group) value, gen, provider);
		} else if (value instanceof Field) {
			fieldSerializer.serialize((Field) value, gen, provider);
		} else if (value instanceof Rule) {
			ruleSerializer.serialize((Rule) value, gen, provider);
		} else if (value instanceof Computation) {
			computationSerializer.serialize((Computation) value, gen, provider);
		}
		gen.writeEndObject();
	}

}
