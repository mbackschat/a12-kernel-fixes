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
package com.mgmtp.a12.kernel.md.model.internal.service.codegen;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.CodeProfileType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;

import lombok.Getter;
import lombok.NonNull;

/**
 * Default implementation of {@link ICodeGeneratorParameters}. Returns default values for all
 * invariants in A12.
 */
public record CodeGeneratorParameters(
		@Getter List<String> restrictedValidGraphemeSet,
		@Getter char decimalSeparator,
		@Getter ProgrammingLanguage programmingLanguage,
		@Getter List<ICalculationInfo> calculationInformation,
		@Getter String name,
		@Getter String rawName,
		@Getter List<Locale> languages,
		@Getter String productVersion,
		@Getter @NonNull Optional<String> javaPackage,
		@Getter JsVkCodeGenConfig jsVkCodeGenConfig,
		boolean isPlain,
		@Getter List<IMetaInfoCodeGenParams> metaInfoCodeGenParams
) implements ICodeGeneratorParameters {
	@Override
	public CodeProfileType getCodeProfile() {
		return CodeProfileType.CP_STANDARD;
	}

	@Override
	public List<Pair<String, String>> getGeneratedFileInformation() {
		return Collections.emptyList();
	}

	@Override public Optional<String> getNondefaultPackagePrefix() {
		return javaPackage;
	}
}
