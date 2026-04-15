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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

public class CodeGenEREntitiesBuilder<D extends CodeGenEntityData> extends CodeGenEntitiesBuilder<D, PackageInformation> {
	public CodeGenEREntitiesBuilder(D data, PackageInformation packageInformation, IProblemReporter problemReporter) {
		super(data, packageInformation, problemReporter);
	}

	public String getCppRuntimeNamespace() {
		if (data.getParams().getProgrammingLanguage() == ICodeGeneratorParameters.ProgrammingLanguage.CPLUS) {
			IExtmpCodeGenParams extmpParams = (IExtmpCodeGenParams) data.getParams();
			String version = extmpParams.getProductVersion();
			String[] versionParts = version.split("\\.");
			return String.format("_%s_0", versionParts[0]);
		} else {
			return null;
		}
	}

	public String getMappedPackegeName() {
		return ExtmpUtils.getMappedPackageName(data.getParams());
	}

	public String getMappedName() {
		return ExtmpUtils.getMappedName(data.getParams());
	}

	@Override public CodeGenEREntities<D> build() {
		return new CodeGenEREntities<>(this);
	}
}
