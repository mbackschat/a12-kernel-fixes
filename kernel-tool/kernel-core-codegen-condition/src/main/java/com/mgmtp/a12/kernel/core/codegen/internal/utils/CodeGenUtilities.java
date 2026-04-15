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

import com.mgmtp.a12.kernel.core.codegen.internal.generator.IBaseCodeGenData;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.CodeProfileType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

public class CodeGenUtilities {

	private CodeGenUtilities() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isFullMetaCode(IBaseCodeGenData data) {
		return isFullStandardCodeProfile(data)
				|| ProgrammingLanguage.JAVA == data.getParams().getProgrammingLanguage();
	}

	public static boolean isFullStandardCodeProfile(CodeProfileType codeProfileType, ProgrammingLanguage programmingLanguage) {
		return CodeProfileType.CP_STANDARD == codeProfileType && ProgrammingLanguage.CPLUS != programmingLanguage;
	}

	public static boolean isFullStandardCodeProfile(IBaseCodeGenData data) {
		return isFullStandardCodeProfile(data.getParams().getCodeProfile(), data.getParams().getProgrammingLanguage());
	}

	public static boolean isReducedStandardCodeProfile(IBaseCodeGenData data) {
		return CodeProfileType.CP_STANDARD == data.getParams().getCodeProfile()
				&& ProgrammingLanguage.CPLUS == data.getParams().getProgrammingLanguage();
	}

}
