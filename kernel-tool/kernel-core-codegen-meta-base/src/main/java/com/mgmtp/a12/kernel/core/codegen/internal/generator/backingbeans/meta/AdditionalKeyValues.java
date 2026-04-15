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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;

/**
 * Helper class for the code generation of additional meta data.
 */
@StringTemplateGroup("meta_base")
public class AdditionalKeyValues extends STParameterClassBase {

	private final String name;
	private final List<IMetaInfoCodeGenParams> metaCodeGenParams;

	public AdditionalKeyValues(String name, List<IMetaInfoCodeGenParams> metaCodeGenParams) {
		this.name = name;
		this.metaCodeGenParams = metaCodeGenParams;
	}

	public List<String> getCodeFragments() {

		List<String> result = new ArrayList<>();
		if (metaCodeGenParams == null) {
			return result;
		}
		for (IMetaInfoCodeGenParams param : metaCodeGenParams) {
			String code;
			if (param.getNameValueMap().containsKey(name)) {
				code = param.getNameValueMap().get(name).toCode();
			} else {
				code = param.getDefaultCode();
			}
			AdditionalKeyValuePair pair = new AdditionalKeyValuePair(param.getKey(), code);
			result.add(pair.toCode());
		}
		return result;
	}

	public boolean isNotEmpty() {
		return metaCodeGenParams != null && !metaCodeGenParams.isEmpty();
	}
}
