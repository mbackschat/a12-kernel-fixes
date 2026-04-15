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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.AdditionalKeyValues;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util.MetaInfoCodeGenParams;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;
import com.mgmtp.a12.model.header.Annotation;

public class MetaCodeGenHelper {

	public static IMetaInfoCodeGenParams createMetaInfoCodeGenParam(String key, String value, String elementType, String fullName) {
		return createMetaInfoCodeGenParam(key, value, elementType, fullName, false);
	}

	public static IMetaInfoCodeGenParams createMetaInfoCodeGenParam(String key, String value, String elementType, String fullName, boolean internal) {
		MetaInfoCodeGenParams mi = new MetaInfoCodeGenParams();
		mi.setMetaElementType(elementType);
		mi.setKey(key);
		mi.setInternal(internal);
		mi.addNameValuePair(fullName, value);
		return mi;
	}

	public static String getAnnotationsCode(List<Annotation> annotations) {
		var metaInfoCodeGenParamsList = annotations.stream()
				.map(e -> createMetaInfoCodeGenParam(e.getName(), new StringParameter(e.getValue()).toCode(), "", ""))
				.collect(Collectors.toList());
		// Notice, we reuse the existing AdditionalKeyValues for the annotations. This results in annotations being a map similar
		// to the additional values map. Annotations map is then included into the additional values map as an element.
		return new AdditionalKeyValues("", metaInfoCodeGenParamsList).toCode();
	}
}
