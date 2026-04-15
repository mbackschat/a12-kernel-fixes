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

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.NullTerminal;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams.XML_RW_Type;

/**
 * Generates code to instantiate an appropriate XMLWriter (depending on the given type).
 */
@StringTemplateGroup("meta/xml")
public class MetaXMLWriter extends STParameterClassBase {

	private final CodeGenData data;
	private final XML_RW_Type type;
	private final boolean createJustUXMLWriter;
	private final PackageInformation packageInformation;

	public MetaXMLWriter(MetaXMLWriterBuilder builder) {
		this.type = builder.getType();
		this.data = builder.getData();
		this.createJustUXMLWriter = builder.isCreateJustUXMLWriter();
		this.packageInformation = builder.getPackageInformation();
	}

	public String getName() {
		return data.getParams().getName();
	}

	public XML_RW_Type getXMLType() {
		return type;
	}

	@Override
	public String toCode(String param) {
		if (!StringUtils.isEmpty(param)) {
			throw new IllegalArgumentException("param currently not supported for MetaXMLWriter#toCode");
		}
		if (createJustUXMLWriter && type != XML_RW_Type.UXML && type != XML_RW_Type.EXML_MAPPED) {
			return new NullTerminal().toCode("nullptr");
		}
		return switch (type) {
			case SXML -> super.toCode("SXML");
			// UXML und der optionale XML-Writer wurde bei der Codegenerierung ausgewählt.
			case UXML -> super.toCode("UXML");
			// A12 model => eXML-Writer
			case EXML -> super.toCode("EXML");
			// model without unique names and mapped model => eXML-Writer or uXML-Writer for eXML
			case EXML_MAPPED -> createJustUXMLWriter
					? super.toCode("UXML4EXML")
					: super.toCode("EXML");
			// no xml writer
			case NONE -> new NullTerminal().toCode("nullptr");
		};
	}

	public String getInternalXmlWriterPackageName() {
		return packageInformation.getInternalXmlWriterPackageName();
	}

}
