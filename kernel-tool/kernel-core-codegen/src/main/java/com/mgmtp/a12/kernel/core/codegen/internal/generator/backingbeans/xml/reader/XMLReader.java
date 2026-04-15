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

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModel;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams.XML_RW_Type;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;

/**
 * Die Klasse ist die zentrale Klasse für den generierten XML-Reader. Hier werden die verschiedenen
 * Reader-Arten angesteuert.
 */
@StringTemplateGroup("base")
public class XMLReader extends CodeGenModel {

	private final String modelName;
	private final String version;
	private final XML_RW_Type type;

	public XMLReader(XMLReaderBuilder builder) {
		super(builder);

		this.modelName = ExtmpUtils.getModelName(data.getParams());
		this.version = ExtmpUtils.getModelVersion(data.getParams());

		this.type = ExtmpUtils.getXmlType(data.getParams());
	}

	public String getModelName() {
		return modelName.toLowerCase();
	}

	public String getVersion() {
		return version;
	}

	public XML_RW_Type getXmlReaderType() {
		return type;
	}

	public boolean isReReDv() {
		return data.isReReDv();
	}
}
