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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

public class BaseCodeGenUtils { // originally moved to this new class in kernel-core-codegen-base from CodeGenUtilities.java

	private BaseCodeGenUtils() {
	}

	/**
	 * return the packagename (or if null, {@link ICodeGeneratorParameters#DEFAULT_PACKAGE_PATH}), with a trailing "." if it did not
	 * already end with "."
	 */
	public static String getNormalizedPackageNameOrDefault(String packageName) {
		return getNormalizedPackageNameOrDefault(Optional.ofNullable(packageName));
	}

	/**
	 * see {@link #getNormalizedPackageNameOrDefault(String)}
	 */
	public static String getNormalizedPackageNameOrDefault(Optional<String> packageName) {
		return getNormalizedPackageNameOrDefaultInternal(packageName, ICodeGeneratorParameters.DEFAULT_PACKAGE_PATH);
	}

	/**
	 * return the packagename from {@link ICodeGeneratorParameters#getNondefaultPackagePrefix()} of the given params if it is
	 * present, or {@link ICodeGeneratorParameters#DEFAULT_PACKAGE_PATH}; with a trailing "."
	 */
	public static String getNormalizedPackageNameOrDefault(ICodeGeneratorParameters params) {
		return getNormalizedPackageNameOrDefaultInternal(params.getNondefaultPackagePrefix(),
				ICodeGeneratorParameters.DEFAULT_PACKAGE_PATH);
	}

	/**
	 * return the packagename from {@link ICodeGeneratorParameters#getNondefaultPackagePrefix()} of the given params if it is
	 * present, or nonstandardDefaultPath; with a trailing "." if it did not
	 * already end with "."
	 */
	public static String getNormalizedPackageNameOrDefault(ICodeGeneratorParameters params, String nonstandardDefaultPath) {
		return getNormalizedPackageNameOrDefaultInternal(params.getNondefaultPackagePrefix(), nonstandardDefaultPath);
	}

	private static String getNormalizedPackageNameOrDefaultInternal(Optional<String> packageName, String defaultPackageName) {
		return verifyTrailingDot(packageName.orElse(defaultPackageName));
	}

	private static String verifyTrailingDot(final String pkg) {
		if (pkg.endsWith(".")) {
			return pkg;
		} else {
			return pkg + ".";
		}
	}

	public static String packageAsGroovyClassnamePrefix(String packageName) {
		// A groovy class *must* start with a capital letter. Else it cannot be compiled correctly (unexpected modifier private)
		return makeValidGroovyClassname(packageName + "_");
	}

	public static String encodeToGroovyClassname(String fullClassName) {
		return makeValidGroovyClassname(fullClassName);
	}

	public static String encodeToGroovyClassname(List<String> fqn) {
		return StringUtils.capitalize(String.join("_", fqn));
	}

	private static String makeValidGroovyClassname(String fullClassName) {
		return StringUtils.capitalize(fullClassName.replace('.', '_'));
	}
}
