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

package com.mgmtp.a12.kernel.md.datatransfer.internal;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BaseCodeGenUtils;
import com.mgmtp.a12.kernel.md.model.api.services.a12internal.ICodeGeneratorConfigV1_2;

public class Util {
	/**
	 * Appends "." + packageSuffix to the {@link ICodeGeneratorConfigV1_2#getPackageName()} of this config.<p/>
	 * If the package name was empty, the resulting config will have the packageName
	 * {@code <default package name>.<packageSuffix>}.
	 *
	 * @implNote util method; there should be no need to override the default impl
	 */
	public static <C extends ICodeGeneratorConfigV1_2> C extendPackageName(C config, String packageSuffix) {
		if(packageSuffix.startsWith(".")) {
			throw new IllegalArgumentException("packageSuffix must not start with a '.', but got " + packageSuffix);
		}
		assert !packageSuffix.endsWith(".") : packageSuffix; // possibly ok but unexpected
		@SuppressWarnings("unchecked") // withPackageName specifies (in javadoc) that implementations must ensure that the returned
		// object's class is at least as specific that the original -> this cast is safe
		C updated = (C) config.withPackageName(
				BaseCodeGenUtils.getNormalizedPackageNameOrDefault(config.getPackageName()) + packageSuffix);
		return updated;
	}
}
