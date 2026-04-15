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

package com.mgmtp.a12.kernel.core.codegen.internal.generator;

import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.IEntryPoints;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.SourceCodeCollectorV2;

/**
 * This interface allows for an abstraction of the code generation process.
 * <p>
 * A code generation process contains the following steps:
 * <ol>
 * <li>Create a {@link SourceCodeCollectorV2}, that will
 * collect the individual source code entries, as well as optional {@link IEntryPoints} that describe e.g. fqns of relevant entry point classes in the generated code.</li>
 * <li>An {@link ICodeGenerator} is created.</li>
 * <li>The code generator generates the source code entries by executing the
 * {@link ICodeGenerator#generateCode()} method. The individual source code entries will be written
 * to the generated {@link SourceCodeCollectorV2}.</li>
 * <li>The generated code is retrieved from via {@link SourceCodeCollectorV2#toCodeGenResult(com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage)}.</li>
 * </ol>
 */
public interface ICodeGenerator {
	public boolean generateCode();
}
