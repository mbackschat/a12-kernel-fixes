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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.dynamicEnum;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModelBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IDynamicEnumerationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

public class CodeGenDynamicEnumerationsBuilder extends CodeGenModelBuilder {

	private final boolean expandGroups;

	public CodeGenDynamicEnumerationsBuilder(CodeGenData data, IProblemReporter problemReporter,
			PackageInformation packageInformation, boolean expandGroups) {
		super(data, problemReporter, packageInformation);
		this.expandGroups = expandGroups;
	}

	/**
	 * Create the code generation objects for dynamic enumerations.
	 *
	 * @return list of code-generation objects.
	 */
	public List<CodeGenDynamicEnumeration> getDynamicEnumerations() {
		List<IDynamicEnumerationInfo> dynEnumInfos = data.getParams().getDynamicEnumerationInformation();
		Function<IEntityId, String> uniqueFieldNamesMapper = e -> data.getUniqueFieldNames().get(e.getFullName());

		return dynEnumInfos.stream()
				.map(dei -> new CodeGenDynamicEnumerationBuilder(dei, data, uniqueFieldNamesMapper, expandGroups).build())
				.collect(Collectors.toList());
	}

	@Override
	public CodeGenDynamicEnumerations build() {
		return new CodeGenDynamicEnumerations(this);
	}

}
