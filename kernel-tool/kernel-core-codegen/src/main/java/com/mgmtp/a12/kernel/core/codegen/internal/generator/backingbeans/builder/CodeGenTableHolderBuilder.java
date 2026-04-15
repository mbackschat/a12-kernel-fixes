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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModelBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenTableCompressed;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenTableHolder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationDataTable;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/**
 * Builds the {@link CodeGenTableHolder}.
 */
public class CodeGenTableHolderBuilder extends CodeGenModelBuilder {

	private Map<String, IEnumerationDataTable> enumerationTables;

	public CodeGenTableHolderBuilder(Map<String, IEnumerationDataTable> enumerationTables, CodeGenData data,
			IProblemReporter problemReporter, PackageInformation packageInformation) {
		super(data, problemReporter, packageInformation);
		this.enumerationTables = enumerationTables;
	}

	public List<CodeGenTableCompressed> getCodeGenTablesCompressed() {
		List<CodeGenTableCompressed> result = new ArrayList<>();
		if (data.getParams().getProgrammingLanguage() == ProgrammingLanguage.CPLUS) {
			// currently we need to generate compressed tables only for C++, so we avoid the expensive work for all other languages
			int i = 0;
			for (Map.Entry<String, IEnumerationDataTable> enumerationTable : enumerationTables.entrySet()) {
				result.add(new CodeGenTableCompressedBuilder(enumerationTable.getKey(), enumerationTable.getValue().getBytes(), i++,
						data, problemReporter, packageInformation).build());
			}
		}
		return result;
	}

	@Override
	public CodeGenTableHolder build() {
		return new CodeGenTableHolder(this);
	}
}
