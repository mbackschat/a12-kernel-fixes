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

package com.mgmtp.a12.kernel.core.codegen.internal.utils;

import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.xml.IXMLZusatzfeld;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.xml.XMLSchemaNode;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.vba.VBAFieldGroupData;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;

import lombok.NonNull;

public class ExtmpCodeGeneratorParametersWrapper extends CodeGeneratorParametersWrapper implements IExtmpCodeGenParams {

	protected final IExtmpCodeGenParams wrappedAsIExtmp_CodeGenParams;

	protected ExtmpCodeGeneratorParametersWrapper(IExtmpCodeGenParams wrapped,
			List<ICalculationInfo> calculationInfoOverride) {
		super(wrapped, calculationInfoOverride);
		this.wrappedAsIExtmp_CodeGenParams = wrapped;
	}

	@Override public IEXMLSchemaData getEXMLSchemaData() {
		return wrappedAsIExtmp_CodeGenParams.getEXMLSchemaData();
	}

	@Override public Map<String, List<VBAFieldGroupData>> getVBAModuleGroups() {
		return wrappedAsIExtmp_CodeGenParams.getVBAModuleGroups();
	}

	@Override public boolean isFeldkennungsbasiert() {
		return wrappedAsIExtmp_CodeGenParams.isFeldkennungsbasiert();
	}

	@Override public String getModelVersion() {
		return wrappedAsIExtmp_CodeGenParams.getModelVersion();
	}

	@Override public String getModelName() {
		return wrappedAsIExtmp_CodeGenParams.getModelName();
	}

	@Override public String getApplicationName() {
		return wrappedAsIExtmp_CodeGenParams.getApplicationName();
	}

	@Override public XML_RW_Type getXmlType() {
		return wrappedAsIExtmp_CodeGenParams.getXmlType();
	}

	@Override public String getAssignedPackageName() {
		return wrappedAsIExtmp_CodeGenParams.getAssignedPackageName();
	}

	@Override public List<XMLSchemaNode> getXmlSchemaNodes() {
		return wrappedAsIExtmp_CodeGenParams.getXmlSchemaNodes();
	}

	@Override public List<IXMLZusatzfeld> getXmlZusatzfelder() {
		return wrappedAsIExtmp_CodeGenParams.getXmlZusatzfelder();
	}

	@Override public IMappedData getMappedModelParameter() {
		return wrappedAsIExtmp_CodeGenParams.getMappedModelParameter();
	}

	@Override public @NonNull ExtmpPackageInformation getExtmpPackageInformation() {
		return wrappedAsIExtmp_CodeGenParams.getExtmpPackageInformation();
	}
}
