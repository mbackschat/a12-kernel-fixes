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

import java.util.List;

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

import lombok.NonNull;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class FieldtypeDeserializer extends ValueDeserializer<FieldType> {

	@Override
	public FieldType deserialize(JsonParser p, DeserializationContext ctxt) {
		JsonNode node = p.readValueAsTree();
		if (node.get("type") == null) {
			return null;
		}
		String fieldTypeName = node.get("type").asString();

		return deriveFieldtype(node, fieldTypeName);
	}

	private @NonNull FieldType deriveFieldtype(JsonNode fieldtypeNode, String fieldTypeName) {
		JsonNode typeNode = fieldtypeNode.get(fieldTypeName);
		if (typeNode == null) {
			return switch (fieldTypeName) {
				case "StringType" -> StringType.builder().build();
				case "NumberType" -> NumberType.builder().build();
				case "DateType" -> DateType.builder().build();
				case "DateTimeType" -> DateTimeType.builder().build();
				case "DateRangeType" -> DateRangeType.builder().format("").build();
				case "DateFragmentType" -> DateFragmentType.builder().formatOfFragment("").build();
				case "EnumerationType" -> EnumerationType.builder().values(List.of()).build();
				case "TimeType" -> TimeType.builder().build();
				case "TypeDefType" -> TypeDefType.builder().id("").build();
				case "CustomFieldType" -> CustomFieldType.builder().name("").build();
				case "ConfirmType" -> ConfirmType.builder().build();
				case "BooleanType" -> BooleanType.builder().build();
				default -> throw new IllegalArgumentException("FieldType not valid - check the .json template");
			};
		}
		Class<? extends FieldType> fieldTypeClass = switch (fieldTypeName) {
			case "StringType" -> StringType.class;
			case "NumberType" -> NumberType.class;
			case "DateType" -> DateType.class;
			case "DateTimeType" -> DateTimeType.class;
			case "DateRangeType" -> DateRangeType.class;
			case "DateFragmentType" -> DateFragmentType.class;
			case "EnumerationType" -> EnumerationType.class;
			case "TimeType" -> TimeType.class;
			case "TypeDefType" -> TypeDefType.class;
			case "CustomFieldType" -> CustomFieldType.class;
			case "ConfirmType" -> ConfirmType.class;
			case "BooleanType" -> BooleanType.class;
			default -> throw new IllegalArgumentException("FieldType not valid - check the .json template");
		};
		return KernelJsonMapper.getMapper().treeToValue(typeNode, fieldTypeClass);
	}
}
