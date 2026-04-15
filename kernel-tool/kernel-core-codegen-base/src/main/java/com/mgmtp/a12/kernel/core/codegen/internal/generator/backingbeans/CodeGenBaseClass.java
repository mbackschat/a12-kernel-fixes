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

import com.mgmtp.a12.kernel.core.codegen.internal.generator.IBaseCodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.builder.CodeGenBaseClassBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BasePackageInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

/**
 * The base class of template parameter classes representing general class files.
 * <p>
 * It contains basic parameters like the name of the fragment or the package name for a class.
 *
 * @param <D>
 * 		the {@link IBaseCodeGenData} used in this class.
 */
public class CodeGenBaseClass<D extends IBaseCodeGenData, P extends BasePackageInformation> extends STParameterClassBase {

	// model elements
	protected final D data;
	protected final P packageInformation;
	protected final String generatedFileInformation;
	protected final ICodeGeneratorParameters.ProgrammingLanguage programmingLanguage;

	public CodeGenBaseClass(CodeGenBaseClassBuilder<D, P> builder) {
		this.data = builder.getData();
		this.packageInformation = builder.getPackageInformation();
		this.generatedFileInformation = builder.getGeneratedFileInformation();
		this.programmingLanguage = data.getParams().getProgrammingLanguage();
	}

	// ---------------------- SIMPLE PARAMETERS -------------------------
	public D getData() {
		return data;
	}

	public String getName() {
		return data.getParams().getName();
	}

	public String getRawName() {
		return data.getParams().getRawName();
	}

	public String getPackageName() {
		return packageInformation.getVKPackageName();
	}

	public String getGroovyPackageClassnamePrefix() {
		return packageInformation.getPackageAsGroovyClassnamePrefix();
	}

	public String getGeneratedFileInformation() {
		return generatedFileInformation;
	}
}