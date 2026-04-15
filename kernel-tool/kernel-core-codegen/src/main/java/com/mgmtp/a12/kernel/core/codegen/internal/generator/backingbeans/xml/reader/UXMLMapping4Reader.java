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
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.xml.IXMLZusatzfeld;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams;

/**
 * In UXMLMapping4Reader werden die Mappings initialisiert, die für den uXML-Reader
 * benötigt werden. Die Mappings von den XML-Feldkenungen und IIdentifiern sind in
 * mehrere Dateien aufgeteilt, da es ansonsten zu Problemen mit der Konstanten-Anzahl in einer Datei
 * kommen könnte.
 */
@StringTemplateGroup("base")
public class UXMLMapping4Reader extends CodeGenModel {

	private final int anzahlMappingDateien;
	private final List<IXMLZusatzfeld> zusatzFelder;

	public UXMLMapping4Reader(UXMLMapping4ReaderBuilder builder) {
		super(builder);

		this.anzahlMappingDateien = builder.getNumberOfMappingFiles();

		if (data.getParams() instanceof IExtmpCodeGenParams) {
			this.zusatzFelder = ((IExtmpCodeGenParams) data.getParams()).getXmlZusatzfelder();
		} else {
			this.zusatzFelder = null;
		}

	}

	public String getFeldKennungMappingCode() {
		StringBuilder buf = new StringBuilder();
		for (int i = 1; i <= anzahlMappingDateien; i++) {
			FeldKennungMapping mapping = new FeldKennungMapping(i);
			buf.append(mapping.toCode());
		}
		return buf.toString();

	}

	public String getZusatzFelderCode() {
		StringBuilder buf = new StringBuilder();
		for (IXMLZusatzfeld zusatzFeld : zusatzFelder) {
			ZusatzFelderListEntry entry = new ZusatzFelderListEntry(zusatzFeld.getFieldName());
			buf.append(entry.toCode());
		}

		return buf.toString();

	}
}
