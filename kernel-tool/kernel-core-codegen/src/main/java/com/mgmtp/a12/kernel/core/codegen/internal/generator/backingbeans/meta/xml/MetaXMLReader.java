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
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.NullTerminal;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams.XML_RW_Type;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

/**
 * Generates code to instantiate an appropriate XML Reader (depending on the given type).
 */
@StringTemplateGroup("meta/xml")
public class MetaXMLReader extends STParameterClassBase {

	private final CodeGenData data;
	private final XML_RW_Type type;
	private final PackageInformation packageInformation;
	private final String realModelName;
	private final String rootElementCustomName;
	private final boolean rootGroupLevelAsRoot;
	private final String realRootGroupName;
	private final boolean withAnhaenge;

	public MetaXMLReader(MetaXMLReaderBuilder builder) {
		this.type = builder.getType();
		this.data = builder.getData();
		this.packageInformation = builder.getPackageInformation();
		ICodeGeneratorParameters params = builder.getData().getParams();
		this.realModelName = ExtmpUtils.getModelName(params);
		this.rootElementCustomName = ExtmpUtils.getRootElementCustomName(params);
		this.rootGroupLevelAsRoot = ExtmpUtils.isRootGroupLevelAsRoot(params);
		this.realRootGroupName = rootGroupLevelAsRoot ? determineRealRootGroupName() : "";
		this.withAnhaenge = containsAnhaenge();
	}

	private String determineRealRootGroupName() {
		return data.getEc().getDirectlyContainedEntities("/")
				.stream()
				.map(IEntity::getFullName)
				.map(name -> name.startsWith("/") ? name.substring(1) : name)
				.filter(name -> !ExtmpUtils.ROOTGROUP_ANHAENGE_NAME.equals(name))
				.findFirst()
				.orElse("");
	}

	private boolean containsAnhaenge() {
		return data.getEc().getDirectlyContainedEntities("/")
				.stream()
				.map(IEntity::getFullName)
				.map(name -> name.startsWith("/") ? name.substring(1) : name)
				.anyMatch(ExtmpUtils.ROOTGROUP_ANHAENGE_NAME::equals);
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
			throw new IllegalArgumentException("param currently not supported for MetaXMLReader#toCode");
		}
		String actualParam = switch (type) {
			case SXML -> "SXML";
			case UXML -> "UXML";
			case EXML, EXML_MAPPED -> "EXML";
			case NONE -> "none";
		};
		return super.toCode(actualParam);
	}

	public String getNullTerminal() {
		return new NullTerminal().toCode("nullptr");
	}

	public String getInternalXmlReaderPackageName() {
		return packageInformation.getInternalXmlReaderPackageName();
	}

	public boolean isRootGroupLevelAsRoot() {
		return rootGroupLevelAsRoot;
	}

	public String getRealRootGroupName() {
		return new StringParameter(realRootGroupName).toCode();
	}

	public String getRootElementCustomName() {
		return new StringParameter(rootElementCustomName).toCode();
	}

	public String getRealModelName() {
		return new StringParameter(realModelName).toCode();
	}

	public boolean isWithAnhaenge() {
		return withAnhaenge;
	}

	public boolean isReReDv() {
		return data.isReReDv();
	}

}
