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

import static com.mgmtp.a12.kernel.md.model.api.IGroup.IIncludeDetails.IncludeLevel.SINGLE_RG;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Group.IncludeDetails;
import com.mgmtp.a12.kernel.md.model.a12internal.IDocumentModelObjectReference;

public class GroupSerializer extends StdSerializer<Group> {

	private static final long serialVersionUID = 851398548904511218L;

	private final Set<String> elementIdsInResult;

	public GroupSerializer(Set<String> elementIdsInResult) {
		this(null, elementIdsInResult);
	}

	public GroupSerializer(Class<Group> t, Set<String> elementIdsInResult) {
		super(t);
		this.elementIdsInResult = elementIdsInResult;
	}

	@Override
	public void serialize(Group value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeNumberField("repeatability", value.getRepeatability());
		if (!value.getLabel().isEmpty()) {
			gen.writeFieldName("label");
			gen.writeObject(value.getLabel());
		}
		Optional<IDocumentModelObjectReference<Field>> indexField = value.getIndexField();
		if (indexField.isPresent() && (elementIdsInResult.isEmpty() // [A12K-2835]
				|| indexField.get().getDocumentModelObject().map(Field::getId).map(elementIdsInResult::contains).orElse(false))) {
			gen.writeStringField("indexFieldName", indexField.get().getRelativePath());
		}
		Optional<String> usageType = value.getUsageType();
		if (usageType.isPresent()) {
			gen.writeStringField("usageType", usageType.get());
		}
		if (!value.getSortFields().isEmpty()) {
			gen.writeFieldName("sortFields");
			gen.writeStartArray();
			// no stream since "throws IOException" is not recognized inside the stream
			for (Group.SortField sf : value.getSortFields()) {
				gen.writeObject(sf);
			}
			gen.writeEndArray();
		}
		Optional<IncludeDetails> includeDetails = value.getIncludeDetails();
		if (includeDetails.isPresent()
				&& includeDetails.get().getModelReference().getAlias() != null
				&& !includeDetails.get().getModelReference().getAlias().isEmpty()) {
			gen.writeStringField("modelAlias", includeDetails.get().getModelReference().getAlias());
			if (includeDetails.get().getIncludeLevel() != SINGLE_RG) {
				// SINGLE_RG is the default, so we only write the field if it has a non-default value
				gen.writeStringField("includeLevel", includeDetails.get().getIncludeLevel().name());
			}
			SerializationUtils.writeBoolean(gen, "excludeRules", includeDetails.get().isExcludeRules());
			SerializationUtils.writeBoolean(gen, "excludeComputations", includeDetails.get().isExcludeComputations());
		}
		if (!value.getElements().isEmpty()) {
			gen.writeFieldName("elements");
			gen.writeStartArray();
			// no stream since "throws IOException" is not recognized inside the stream
			for (Element e : value.getElements()) {
				gen.writeObject(e);
			}
			gen.writeEndArray();
		}

		gen.writeEndObject();
	}

}
