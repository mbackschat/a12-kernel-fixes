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
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.xml.IXMLZusatzfeld;

/**
 * Klasse zum gleichnamigen Template. Die zentrale Klasse UXMLMapping4Writer enthält alle
 * relevanten Mappings und Listen die für den XML-Writer notwendig sind. Ausgelagert in eigene
 * Klassen wurden die Mappings mit den Feldinhalten, da ansonsten die Beschränkungen der JRE
 * überschritten werden.
 */
@StringTemplateGroup("base")
public class UXMLMapping4Writer extends CodeGenModel {

	private final int anzahlMappingDateien;
	private final List<String> vordrucke;
	private final List<IXMLZusatzfeld> zusatzFelder;

	public UXMLMapping4Writer(UXMLMapping4WriterBuilder builder) {
		super(builder);

		this.anzahlMappingDateien = builder.getNumberOfMappingFiles();
		this.vordrucke = builder.getRootgroups();
		this.zusatzFelder = builder.getAdditionalFields();
	}

	public String getZusatzFeldMappingCode() {
		StringBuilder sb = new StringBuilder();
		for (IXMLZusatzfeld zusatzFeld : zusatzFelder) {

			ZusatzFeldEntry feld = new ZusatzFeldEntry(zusatzFeld.getFieldName(), zusatzFeld.getFormatTag(), zusatzFeld.getForm(),
					false);
			sb.append(feld.toCode());
		}
		return sb.toString();
	}

	public String getVordruckMappingCode() {
		StringBuilder sb = new StringBuilder();
		for (String vordruck : vordrucke) {
			VordruckEntry entry = new VordruckEntry(vordruck);
			sb.append(entry.toCode());
		}
		return sb.toString();

	}

	public String getFeldInhalteMapClassCode() {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= anzahlMappingDateien; i++) {
			FeldInhalteMapClassEntry entry = new FeldInhalteMapClassEntry(i);
			sb.append(entry.toCode());
		}
		return sb.toString();
	}
}
