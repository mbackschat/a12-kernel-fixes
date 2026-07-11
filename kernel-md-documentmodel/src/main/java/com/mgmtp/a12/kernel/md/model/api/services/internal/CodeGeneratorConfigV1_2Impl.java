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

package com.mgmtp.a12.kernel.md.model.api.services.internal;

import java.util.Optional;
import java.util.function.Supplier;

import com.mgmtp.a12.kernel.md.model.api.services.a12internal.ICodeGeneratorConfigV1_2;

import lombok.NonNull;

public record CodeGeneratorConfigV1_2Impl(String packageName, Supplier<ProgrammingLanguage> programmingLanguageLazy)
		implements ICodeGeneratorConfigV1_2 {
	public CodeGeneratorConfigV1_2Impl {
		if (packageName != null && packageName.endsWith(".")) {
			packageName = packageName.substring(0, packageName.length() - 1);
		}
	}

	public static CodeGeneratorConfigV1_2Impl of(String packageName, Supplier<ProgrammingLanguage> programmingLanguageLazy) {
		return new CodeGeneratorConfigV1_2Impl(packageName, programmingLanguageLazy);
	}

	@Override public Optional<String> getPackageName() {
		return Optional.ofNullable(packageName);
	}

	@Override public @NonNull ProgrammingLanguage getProgrammingLanguage() {
		return programmingLanguageLazy.get();
	}

	@Override public CodeGeneratorConfigV1_2Impl withPackageName(String newPackageName) {
		return new CodeGeneratorConfigV1_2Impl(newPackageName, this.programmingLanguageLazy);
	}
}
