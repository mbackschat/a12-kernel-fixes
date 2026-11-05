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

package com.mgmtp.a12.kernel.md.model.api.services.a12internal;

import java.util.function.Supplier;

import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.api.services.internal.CodeGeneratorConfigV1_2Impl;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Like {@link IValidationCodeGeneratorConfig} but with an additional method.
 * (A new interface was required because adding a method to {@link IValidationCodeGeneratorConfig} would have been breaking,
 * because custom implementations are allowed for {@link IValidationCodeGeneratorConfig}.)
 * <p/>
 * Users should only use the implementation provided by kernel, via the provided static factory methods like
 * {@link #of(String, ProgrammingLanguage)}
 */
@OnlyForUsage // keeping this @OnlyForUsage should allow us to add more methods in the future as a non-breaking change.
// keep this a12internal for now, for better future flexibility
// TODO: should we make a plan for cleanup (merge this back into IValidationCodeGeneratorConfig)?
public interface ICodeGeneratorConfigV1_2 extends IValidationCodeGeneratorConfig {

	/**
	 * implementations must make sure to return an object of a subclass that is at least as specific as the object on which the
	 * method was called.
	 *
	 * @param newPackageName the new package prefix, or {@code null} to revert to the default-package behavior. See {@link IValidationCodeGeneratorConfig#getPackageName()} for details.
	 */
	ICodeGeneratorConfigV1_2 withPackageName(String newPackageName);

	/**
	 * @param packageName base package prefix for generated code, or {@code null} to defer to the kernel default.
	 *                    See {@link com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig#getPackageName()}
	 *                    for full semantics of the absent-package case.
	 */
	static ICodeGeneratorConfigV1_2 of(String packageName, ProgrammingLanguage programmingLanguage) {
		return CodeGeneratorConfigV1_2Impl.of(packageName, () -> programmingLanguage);
	}

	/**
	 * Like {@link #of(String, ProgrammingLanguage)}, with a lazily-evaluated programming language.
	 * {@code null} is accepted for {@code packageName} with the same semantics.
	 */
	static ICodeGeneratorConfigV1_2 ofLazyProgrammingLanguage(String packageName,
			Supplier<ProgrammingLanguage> programmingLanguageLazy) {
		return CodeGeneratorConfigV1_2Impl.of(packageName, programmingLanguageLazy);
	}
}
