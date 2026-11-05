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

import com.mgmtp.a12.kernel.md.model.a12internal.Element;

import tools.jackson.core.JsonGenerator;

public class ElementSerializerUtil {

	private ElementSerializerUtil() {
		throw new UnsupportedOperationException("Utility classes may not be instantiated!");
	}

	public static void serializeElementBase(Element value, JsonGenerator gen) {
		String subTypeName = value.getClass().getSimpleName();
		gen.writeStringProperty("type", subTypeName);
		gen.writeStringProperty("id", value.getId());
		gen.writeStringProperty("name", value.getName());
		if (!value.getAnnotations().isEmpty()) {
			gen.writeName("annotations");
			KernelJsonMapper.getWriterForNoNonEmpty().writeValue(gen, value.getAnnotations());
		}
		if (!value.getExternalDescription().isEmpty()) {
			gen.writeName("externalDescription");
			gen.writePOJO(value.getExternalDescription());
			//mapper.writeValue(gen, value.getExternalDescription());
		}
		if (!value.getInternalDescription().isEmpty()) {
			gen.writeName("internalDescription");
			gen.writePOJO(value.getInternalDescription());
			//mapper.writeValue(gen, value.getInternalDescription());
		}
		value.getChangeNote().ifPresent(changeNote -> gen.writeStringProperty("changeNote", changeNote));
	}
}
