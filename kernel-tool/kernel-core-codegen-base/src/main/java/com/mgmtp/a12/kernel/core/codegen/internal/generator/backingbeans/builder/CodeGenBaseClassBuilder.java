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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.builder;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.BaseCodeGenConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.IBaseCodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenBaseClass;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BasePackageInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringFormatUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

import lombok.Getter;

/**
 * Basic backing bean to generate a class file for different languages.
 */
@Getter
public abstract class CodeGenBaseClassBuilder<D extends IBaseCodeGenData, P extends BasePackageInformation> {

	protected final D data;
	protected final P packageInformation;

	protected CodeGenBaseClassBuilder(D data, P packageInformation) {
		this.data = data;
		this.packageInformation = packageInformation;
	}

	public String getGeneratedFileInformation() {
		// only tested for JsReduced (see TestGeneratedFileInformation)
		assert data.getParams().getGeneratedFileInformation().isEmpty()
				|| data.getParams().getProgrammingLanguage() == ICodeGeneratorParameters.ProgrammingLanguage.JAVA_SCRIPT_REDUCED;

		if (data.getParams().getGeneratedFileInformation().size() > BaseCodeGenConstants.MAX_LINE_COUNT_GEN_FILE_INFO) {
			throw new IllegalArgumentException(
					"Generated file information may only have up to " + BaseCodeGenConstants.MAX_LINE_COUNT_GEN_FILE_INFO + " lines. The given generated file information contains " + data.getParams()
							.getGeneratedFileInformation().size() + " lines.");
		}
		var generatedFileInfoLines = data.getParams().getGeneratedFileInformation().stream()
				.map(e -> createGeneratedFileInformationCommentLine(e.getKey(), e.getValue())).toList();

		ICodeGeneratorParameters.ProgrammingLanguage language = data.getParams().getProgrammingLanguage();
		return StringFormatUtil.generateBlockComment(generatedFileInfoLines, language);
	}

	private String createGeneratedFileInformationCommentLine(String key, String value) {
		if (StringUtils.isEmpty(value)) {
			return key;
		}
		//Prevent lines from becoming too big.
		if (key.length() + value.length() > BaseCodeGenConstants.MAX_LINE_SIZE_GEN_FILE_INFO) {
			throw new IllegalArgumentException(
					"Comments may only have length <= " + BaseCodeGenConstants.MAX_LINE_SIZE_GEN_FILE_INFO + ". The actual length was: |key|=" + key.length() + " |value|=" + value.length());
		}
		//Prevent code injection by escaping 'end of comment' character sequence
		key = key.replace("*/", "* /");
		value = value.replace("*/", "* /");
		return String.format("           %-25s %s", key, value);
	}

	public abstract CodeGenBaseClass<D, P> build();
}
