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

import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.builder.CodeGenTableHolderBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

/**
 * Parameter class for the creation of table accessor code of the form "T_&lt;modelName&gt;".
 * <p>
 * Generated Java code:
 * The generated class contains only static code but is necessary to load the tables with the
 * correct class loader and omit circular dependencies.
 * <p>
 * Generated C++ code:
 * The generated source file contains the content of all CSV table files in compressed byte format
 * because separate/explicit CSV files may not be delivered to and by projGamma.
 */
@StringTemplateGroup("root")
public class CodeGenTableHolder extends CodeGenModel {
	private int currentTableFileCpp = 0;
	private List<CodeGenTableCompressed> tablesCompressed;

	public CodeGenTableHolder(CodeGenTableHolderBuilder builder) {
		super(builder);
		this.tablesCompressed = builder.getCodeGenTablesCompressed();
	}

	public List<String> getConstantDeclarations() {
		return tablesCompressed.stream()
				.map(t -> t.toCode("constDecl"))
				.collect(Collectors.toList());
	}

	public String getConstantDefinition() {
		return tablesCompressed.get(currentTableFileCpp++).toCode("constDef");
	}

	public int getNumTableFilesCpp() {
		return tablesCompressed.size();
	}

	public List<String> getTableEnumTs() {
		return tablesCompressed.stream()
				.map(t -> t.toCode("tableEnumT"))
				.collect(Collectors.toList());
	}

}
