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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.BooleanType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.ConfirmType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.CustomFieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateFragmentType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateRangeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateTimeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.NumberType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TimeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.UnspecifiedType;
import com.mgmtp.a12.model.header.Annotation;

public class FieldtypeDeserializer extends JsonDeserializer<FieldType> {

	@Override
	public FieldType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonNode node = p.readValueAsTree();
		if (node.get("type") == null) {
			return null;
		}
		String fieldTypeName = node.get("type").asText();

		FieldType fieldType = deriveFieldtype(node, fieldTypeName);

		fieldType.setAnnotations(getAnnotations(node));

		return fieldType;
	}

	private List<Annotation> getAnnotations(JsonNode node) {
		JsonNode annotationsNode = node.get("annotations");
		if ((annotationsNode) != null) {
			return JsonMapper.getMapper().convertValue(annotationsNode, new TypeReference<List<Annotation>>() {
			});
		}
		return new ArrayList<>();
	}

	private FieldType deriveFieldtype(JsonNode fieldtypeNode, String fieldTypeName) throws IOException {
		FieldType fieldType = null;
		JsonNode typeNode = fieldtypeNode.get(fieldTypeName);
		switch (fieldTypeName) {
			case "StringType":
				if (typeNode == null) {
					fieldType = StringType.builder().build();
				} else {
					fieldType = JsonMapper.getMapper().treeToValue(typeNode, StringType.class);
				}
				break;
			case "NumberType":
				if (typeNode == null) {
					fieldType = NumberType.builder().build();
				} else {
					fieldType = JsonMapper.getMapper().treeToValue(typeNode, NumberType.class);
				}
				break;
			case "DateType":
				// typeNode is never null
				fieldType = JsonMapper.getMapper().treeToValue(typeNode, DateType.class);
				break;
			case "DateTimeType":
				// typeNode is never null
				fieldType = JsonMapper.getMapper().treeToValue(typeNode, DateTimeType.class);
				break;
			case "DateRangeType":
				// typeNode is never null
				fieldType = JsonMapper.getMapper().treeToValue(typeNode, DateRangeType.class);
				break;
			case "DateFragmentType":
				// typeNode is never null
				fieldType = JsonMapper.getMapper().treeToValue(typeNode, DateFragmentType.class);
				break;
			case "EnumerationType":
				// typeNode is never null
				fieldType = JsonMapper.getMapper().treeToValue(typeNode, EnumerationType.class);
				break;
			case "TimeType":
				// typeNode is never null
				fieldType = JsonMapper.getMapper().treeToValue(typeNode, TimeType.class);
				break;
			case "TypeDefType":
				// typeNode is never null
				fieldType = JsonMapper.getMapper().treeToValue(typeNode, TypeDefType.class);
				break;
			case "CustomFieldType":
				// typeNode is never null
				fieldType = JsonMapper.getMapper().treeToValue(typeNode, CustomFieldType.class);
				break;
			case "ConfirmType":
				if (typeNode == null) {
					fieldType = ConfirmType.builder().build();
				} else {
					fieldType = JsonMapper.getMapper().treeToValue(typeNode, ConfirmType.class);
				}
				break;
			case "BooleanType":
				if (typeNode == null) {
					fieldType = BooleanType.builder().build();
				} else {
					fieldType = JsonMapper.getMapper().treeToValue(typeNode, BooleanType.class);
				}
				break;
			case "UnspecifiedType":
				if (typeNode == null) {
					fieldType = UnspecifiedType.builder().build();
				} else {
					fieldType = JsonMapper.getMapper().treeToValue(typeNode, UnspecifiedType.class);
				}
				break;
			default:
				throw new IllegalArgumentException("FieldType not valid - check the .json template");
		}

		return fieldType;
	}
}
