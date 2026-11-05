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

public class GeneratorConstants {

	/**
	 * <b>Note:<b>
	 * For Java and Groovy, the compatibility check between runtime and generated code does no longer rely on the version constant
	 * here and in Runtime's Constants.java, but our runtime-vs-runtime-api compatibility check relies on the version
	 * constants.
	 */
	// WARNING: Don't change this line. Updated by versionupdate script
	// Don't remove 'new String(..)' to prevent constant inlining (at compile-time).
	public static final String GENCODE_JAVA_GROOVY_RUNTIME_VERSION = new String("31.3");

	// WARNING: Don't change this line. Updated by versionupdate script
	// Don't remove 'new String(..)' to prevent constant inlining (at compile-time).
	public static final String GENCODE_TYPESCRIPT_RUNTIME_VERSION = new String("31.3");


	private GeneratorConstants() {
		throw new IllegalStateException("Utility class");
	}

}
