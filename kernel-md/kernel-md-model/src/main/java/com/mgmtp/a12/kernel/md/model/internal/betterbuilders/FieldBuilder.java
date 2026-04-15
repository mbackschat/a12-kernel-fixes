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
package com.mgmtp.a12.kernel.md.model.internal.betterbuilders;

import java.util.function.UnaryOperator;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.ft.FieldTypeBuilder;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.ft.StringTypeBuilder;

public class FieldBuilder {

	private final DocumentModel documentModel;
	private final Field.FieldBuilder fieldBuilder;
	private FieldTypeBuilder fieldTypeBuilder;
	private Group parent;

	private FieldBuilder(DocumentModel documentModel) {
		this.documentModel = documentModel;
		this.fieldBuilder = Field.builder()
				.isGlobal(false)
				.isTransient(false);
		this.fieldTypeBuilder = StringTypeBuilder.builder(); // this is somewhat arbitrary, but in line with what SME does. I think the improved practicality outweighs the arbitraryness here
	}

	public static FieldBuilder with(DocumentModel documentModel) {
		return new FieldBuilder(documentModel);
	}

	public FieldBuilder id(String id) {
		fieldBuilder.id(id);
		return this;
	}

	public FieldBuilder name(String fullName) {
		String pathWithRepetitions = BuilderUtil.pathPartOf(fullName);
		parent = GroupBuilder.with(documentModel).ensureGroup(pathWithRepetitions);
		fieldBuilder
				.id(fullName)
				.name(BuilderUtil.lastPartOf(fullName));
		return this;
	}

	public <T extends FieldTypeBuilder> FieldBuilder ft(T fieldTypeBuilder) {
		this.fieldTypeBuilder = fieldTypeBuilder;
		return this;
	}

	public FieldBuilder modify(UnaryOperator<Field.FieldBuilder> modifier) {
		modifier.apply(fieldBuilder);
		return this;
	}

	public Field build() {
		fieldBuilder.fieldType(fieldTypeBuilder.build());
		Field field = fieldBuilder.build();
		parent.addElement(field);
		return field;
	}
}
