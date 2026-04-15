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

import static com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage.CPLUS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.MetaCodeGenHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.StringUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;
import com.mgmtp.a12.model.header.Annotation;

/**
 * The base class of template parameter classes representing complete files for a specific model.
 * <p>
 * It contains basic parameters like the name of the model or the package name for a class.
 */
@StringTemplateGroup("root")
public class CodeGenModel extends CodeGenEREntities<CodeGenData> {

	public CodeGenModel(CodeGenModelBuilder builder) {
		super(builder);
		// the document model

	}

	public String getPublicPackageName() {
		return packageInformation.getPublicPackageName();
	}

	/**
	 * This additional values should be generated for A12 and for ExtMP.
	 */
	protected List<IMetaInfoCodeGenParams> createCommonAdditionalInfos() {
		List<IMetaInfoCodeGenParams> result = new ArrayList<>();
		Optional<String> baseYear = data.getModelSpecificSettings().getBaseYear().map(String::valueOf);
		String baseYearStr = switch (programmingLanguage) {
			case JAVA, GROOVY, JAVA_SCRIPT_VK -> baseYear.map(s -> StringUtil.toCode(s, programmingLanguage)).orElse("null");
			case VBA -> String.format("CStr(%s)", baseYear.orElse("0"));
			case CPLUS, JAVA_SCRIPT_REDUCED -> baseYear.orElse("0");
		};
		if (programmingLanguage != CPLUS) {
			result.add(createModelMetaInfo("BaseYear", baseYearStr, true));
		}
		switch (programmingLanguage) {
			case JAVA, GROOVY, JAVA_SCRIPT_VK -> {
				result.add(createModelMetaInfo("TimeZone",
						new StringParameter(data.getModelSpecificSettings().getTimeZone()).toCode(), true));
				result.add(createModelMetaInfo("ModelId", new StringParameter(data.getParams().getRawName()).toCode(), true));

				List<Annotation> annotations = data.getModelSpecificSettings().getAnnotations();
				if (annotations != null && !annotations.isEmpty()) {
					result.add(createModelMetaInfo("Annotation", MetaCodeGenHelper.getAnnotationsCode(annotations), false));
				}
			}
		}

		return result;
	}

	private IMetaInfoCodeGenParams createModelMetaInfo(String key, String value, boolean internal) {
		return MetaCodeGenHelper.createMetaInfoCodeGenParam(key, value, IMetaInfoCodeGenParams.META_ELEMENT_TYPE_MODEL, "",
				internal);
	}
}
