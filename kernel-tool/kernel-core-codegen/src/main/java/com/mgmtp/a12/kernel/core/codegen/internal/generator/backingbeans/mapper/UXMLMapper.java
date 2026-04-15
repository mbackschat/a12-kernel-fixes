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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.mapper;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModel;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModelBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BaseCodeGenUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

@StringTemplateGroup("base")
public class UXMLMapper extends CodeGenModel {

	public UXMLMapper(CodeGenModelBuilder builder) {
		super(builder);
	}

	public String getPublicClassNamePrefix() {
		return packageInformation.getPublicUXmlMapperClassNamePrefix();
	}

	public String getModelName() {
		return ExtmpUtils.getModelName(data.getParams());
	}

	public String getPackagePrefixVK() {
		return getPackageNameInternal(data.getParams(), null);
	}

	private static String getPackageNameInternal(ICodeGeneratorParameters codegenParams, String defaultPackage) {
		String packageName = defaultPackage != null
				? BaseCodeGenUtils.getNormalizedPackageNameOrDefault(codegenParams, defaultPackage)
				: BaseCodeGenUtils.getNormalizedPackageNameOrDefault(codegenParams);
		assert packageName.endsWith(".") : "expected getNormalizedPackageName to return a string ending in '.'";
		return packageName.substring(0,
				packageName.length() - 1); // we return the prefix without trailing dot for clearer readability in the templates (so that the template contains the separating dot explicitly)
	}
}
