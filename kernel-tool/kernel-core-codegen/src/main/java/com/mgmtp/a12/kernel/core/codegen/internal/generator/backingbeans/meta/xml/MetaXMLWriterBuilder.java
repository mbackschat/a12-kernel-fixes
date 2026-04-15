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
package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.xml;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.xml.writer.XMLWriter;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams.XML_RW_Type;

/**
 * Builder to generate a {@link MetaXMLWriter} which is used to generate code to instatiate an
 * {@link XMLWriter} in generated code.
 */
public class MetaXMLWriterBuilder {

	private final XML_RW_Type type;
	private final CodeGenData data;
	private final boolean createJustUXMLWriter;
	private final PackageInformation packageInfo;

	public MetaXMLWriterBuilder(CodeGenData data, XML_RW_Type type, boolean createJustUXMLWriter, PackageInformation packageInfo) {
		this.data = data;
		this.type = type;
		this.createJustUXMLWriter = createJustUXMLWriter;
		this.packageInfo = packageInfo;
	}

	public XML_RW_Type getType() {
		return this.type;
	}

	public CodeGenData getData() {
		return this.data;
	}

	public boolean isCreateJustUXMLWriter() {
		return this.createJustUXMLWriter;
	}

	public PackageInformation getPackageInformation() {
		return this.packageInfo;
	}

	public MetaXMLWriter build() {
		return new MetaXMLWriter(this);
	}
}
