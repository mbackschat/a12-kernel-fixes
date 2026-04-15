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
package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.xml.reader;

import java.util.List;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModel;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

/**
 * Die Klasse zum Template FeldKennungIdentifierMappingLfdNr In dem Template befindet sich der
 * Klassenrahmen für die eigentlichen FeldKennung - Identifier Einträge
 */
@StringTemplateGroup("base")
public class FeldKennungIdentifierMappingLfdNr extends CodeGenModel {

	private final String fileNumber;
	private final List<IField> fields;

	public FeldKennungIdentifierMappingLfdNr(FeldKennungIdentifierMappingLfdNrBuilder builder) {
		super(builder);

		this.fileNumber = String.valueOf(builder.getFileNumber());
		this.fields = builder.getFields();
	}

	public String getFileNumber() {
		return fileNumber;
	}

	public String getLfdNr() {
		return fileNumber;
	}

	public String getFeldKennungIdentifierEntryCode() {
		StringBuilder buf = new StringBuilder();

		for (IEntity field : fields) {
			String fullName = field.getFullName();
			String shortName = EntityUtils.getShortName(fullName);
			FeldKennungIdentifierEntry entry = new FeldKennungIdentifierEntry(shortName, fullName);
			buf.append(entry.toCode());
		}

		return buf.toString();
	}

}
