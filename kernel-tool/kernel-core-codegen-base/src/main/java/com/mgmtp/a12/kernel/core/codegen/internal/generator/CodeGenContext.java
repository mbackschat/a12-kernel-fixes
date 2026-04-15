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

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.Product;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * The generation context for the code generator.
 * <p>
 * The information needed for code generation is split into two parts,
 * <ul>
 * <li>the {@link CodeGenData}, that specify the parameters used for building the backing
 * bean hierarchy ({@link CodeGenModel}), and
 * <li>
 * <li>the {@link CodeGenContext}, that distinguishes the different options for code generation
 * based on the backing bean hierarchy.</li>
 * </ul>
 * <p>
 * This context is closely coupled to the acquisition of templates, because the different options
 * are implemented by selecting different templates.
 */
public class CodeGenContext {
	private static final ThreadLocal<CodeGenContext> INSTANCE_HOLDER = new ThreadLocal<>();

	private final Product product;
	private final ProgrammingLanguage programmingLanguage;

	public static CodeGenContext getInstance() {
		CodeGenContext instance = INSTANCE_HOLDER.get();
		if (instance == null) {
			throw new IllegalStateException("The code gen context was not initialized before polling!");
		}
		return instance;
	}

	public static void initialize(Product product, ProgrammingLanguage programmingLanguage) {
		INSTANCE_HOLDER.set(new CodeGenContext(product, programmingLanguage));
	}

	private CodeGenContext(Product product, ProgrammingLanguage programmingLanguage) {
		super();
		this.product = product;
		this.programmingLanguage = programmingLanguage;
	}

	public Product getProduct() {
		return product;
	}

	public ProgrammingLanguage getProgrammingLanguage() {
		return programmingLanguage;
	}

	public static void unload() {
		INSTANCE_HOLDER.remove();
	}
}
