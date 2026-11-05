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
package com.mgmtp.a12.kernel.md.model.migration.internal.json.step;

import static com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils.MAPPER;

import java.util.List;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationInfo;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import lombok.NonNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Migration step that replaces the removed UnspecifiedType field type with StringType and adds the
 * {@code _take_reference_configuration} annotation so that decoration joining still works.
 * A warning is also added to the field's internalDescription.
 */
public class RemoveUnspecifiedTypeStep {

	private static final String UNSPECIFIED_TYPE = "UnspecifiedType";
	private static final String STRING_TYPE = "StringType";
	private static final String TAKE_REFERENCE_CONFIGURATION = "_take_reference_configuration";
	public static final String WARNING_TEXT =
			"WARNING: This field previously used UnspecifiedType which has been removed. Please review and assign the correct field type.";

	private RemoveUnspecifiedTypeStep() {
	}

	public static void execute(@NonNull JsonNode model, MigrationLogger migrationLogger, ICustomMigrationConfig customMigConfig) {

		List<JsonNode> fields = ModelJsonUtil.extractAllFields(model);
		for (JsonNode fieldElement : fields) {
			JsonNode fieldContent = fieldElement.get(ModelJsonUtil.TYPE_FIELD);
			if (fieldContent == null || !fieldContent.has(ModelJsonUtil.FIELD_FIELD_TYPE)) {
				continue;
			}
			JsonNode fieldTypeNode = fieldContent.get(ModelJsonUtil.FIELD_FIELD_TYPE);
			if (!fieldTypeNode.has(ModelJsonUtil.FIELD_FIELD_TYPE_TYPE)) {
				continue;
			}
			String typeName = fieldTypeNode.get(ModelJsonUtil.FIELD_FIELD_TYPE_TYPE).asString();
			if (UNSPECIFIED_TYPE.equals(typeName)) {
				replaceUnspecifiedType((ObjectNode) fieldContent, (ObjectNode) fieldElement);
				migrationLogger.addInfo(new MigrationInfo(WARNING_TEXT,
						fieldElement.get(ModelJsonUtil.FIELD_ID) != null ?
								fieldElement.get(ModelJsonUtil.FIELD_ID).asString() :
								"unknown"));
			}
		}
	}

	private static void replaceUnspecifiedType(ObjectNode fieldContent, ObjectNode fieldElement) {
		// Replace fieldType with StringType
		ObjectNode newFieldType = MAPPER.createObjectNode();
		newFieldType.put(ModelJsonUtil.FIELD_FIELD_TYPE_TYPE, STRING_TYPE);
		fieldContent.set(ModelJsonUtil.FIELD_FIELD_TYPE, newFieldType);

		// Add _take_reference_configuration annotation
		ArrayNode annotations = fieldElement.has(ModelJsonUtil.ANNOTATIONS)
				? (ArrayNode) fieldElement.get(ModelJsonUtil.ANNOTATIONS)
				: MAPPER.createArrayNode();
		ObjectNode annotation = MAPPER.createObjectNode();
		annotation.put(ModelJsonUtil.ANNOTATION_NAME, TAKE_REFERENCE_CONFIGURATION);
		annotation.put(ModelJsonUtil.ANNOTATION_VALUE, "");
		annotations.add(annotation);
		fieldElement.set(ModelJsonUtil.ANNOTATIONS, annotations);
	}
}
