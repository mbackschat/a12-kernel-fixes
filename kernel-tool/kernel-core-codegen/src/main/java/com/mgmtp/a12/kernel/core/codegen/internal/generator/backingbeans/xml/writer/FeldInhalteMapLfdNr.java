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
package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.xml.writer;

import java.util.List;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModel;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

/**
 * Klasse zum gleichnamigen Template. Es erzeugt eine der n Klassen, in denen die Feldhinhalte
 * definiert sind.
 */
@StringTemplateGroup("base")
public class FeldInhalteMapLfdNr extends CodeGenModel {

	private final int fileNumber;
	private final List<IField> fields;

	public FeldInhalteMapLfdNr(FeldInhalteMapLfdNrBuilder builder) {
		super(builder);
		this.fileNumber = builder.getFileNumber();
		this.fields = builder.getFields();
	}

	public String getLfdNr() {
		return String.valueOf(fileNumber);
	}

	public String getFeldInhalteCode() {
		StringBuilder sb = new StringBuilder();
		for (IField feld : fields) {
			FeldInhalteEntry entry = new FeldInhalteEntry(feld);
			sb.append(entry.toCode());
		}

		return sb.toString();
	}

}
