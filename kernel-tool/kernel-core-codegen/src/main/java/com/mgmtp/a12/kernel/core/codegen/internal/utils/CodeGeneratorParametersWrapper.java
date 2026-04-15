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

package com.mgmtp.a12.kernel.core.codegen.internal.utils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.CodeProfileType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IDynamicEnumerationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;

public class CodeGeneratorParametersWrapper implements ICodeGeneratorParameters {

	private final ICodeGeneratorParameters wrapped;
	private final List<ICalculationInfo> calculationInfoOverride;

	protected CodeGeneratorParametersWrapper(ICodeGeneratorParameters wrapped,
			List<ICalculationInfo> calculationInfoOverride) {
		this.wrapped = wrapped;
		this.calculationInfoOverride = calculationInfoOverride;
	}

	/**
	 * Create a wrapper object that overrides calculationInfo of a wrapper subclass that is suitable for the given implementation
	 * of ICodeGeneratorParameters (some parts of the code generaton expect to get specific implementations of
	 * ICodeGeneratorParameters, so it is not sufficient to use one generic wrapper...
	 */
	public static ICodeGeneratorParameters wrap(
			ICodeGeneratorParameters wrapped,
			List<ICalculationInfo> calculationInfoOverride) {
		if (wrapped instanceof IExtmpCodeGenParams extmpCodeGenParams) {
			return new ExtmpCodeGeneratorParametersWrapper(extmpCodeGenParams, calculationInfoOverride);
		} else {
			return new CodeGeneratorParametersWrapper(wrapped, calculationInfoOverride);
		}
	}

	@Override public ProgrammingLanguage getProgrammingLanguage() {
		return wrapped.getProgrammingLanguage();
	}

	@Override public String getName() {
		return wrapped.getName();
	}

	@Override public String getRawName() {
		return wrapped.getRawName();
	}

	@Override public Optional<String> getNondefaultPackagePrefix() {
		return wrapped.getNondefaultPackagePrefix();
	}

	@Override public List<Locale> getLanguages() {
		return wrapped.getLanguages();
	}

	@Override public CodeProfileType getCodeProfile() {
		return wrapped.getCodeProfile();
	}

	@Override public List<Pair<String, String>> getGeneratedFileInformation() {
		return wrapped.getGeneratedFileInformation();
	}

	@Override public List<ICalculationInfo> getCalculationInformation() {
		return calculationInfoOverride;
	}

	@Override public String getProductVersion() {
		return wrapped.getProductVersion();
	}

	@Override public List<IDynamicEnumerationInfo> getDynamicEnumerationInformation() {
		return wrapped.getDynamicEnumerationInformation();
	}

	@Override public JsVkCodeGenConfig getJsVkCodeGenConfig() {
		return wrapped.getJsVkCodeGenConfig();
	}

	@Override
	public List<String> restrictedValidGraphemeSet() {
		return wrapped.restrictedValidGraphemeSet();
	}

	@Override public char getDecimalSeparator() {
		return wrapped.getDecimalSeparator();
	}

	@Override public List<IMetaInfoCodeGenParams> getMetaInfoCodeGenParams() {
		return wrapped.getMetaInfoCodeGenParams();
	}

	@Override public boolean isPlain() {
		return wrapped.isPlain();
	}
}
