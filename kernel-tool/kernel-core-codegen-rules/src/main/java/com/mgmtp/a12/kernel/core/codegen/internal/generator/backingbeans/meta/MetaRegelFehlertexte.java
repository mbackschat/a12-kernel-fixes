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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

/**
 * Parameter class for the string template representing the set of error messages for one rule in
 * the meta API.
 */
@StringTemplateGroup("meta_rules")
public class MetaRegelFehlertexte extends STParameterClassBase {

	private final RuleInfo ruleInfo;
	private final Map<Locale, Fehlertext> fehlertexte;
	private final Map<String, String> parameterMapping;

	MetaRegelFehlertexte(MetaRegelFehlertexteBuilder builder) {
		this.ruleInfo = builder.getRuleInfo();
		this.fehlertexte = builder.getFehlertexte();
		this.parameterMapping = builder.getParameterMapping();
	}

	public String getRegelFullName() {
		return ruleInfo.getRule().getFullName();
	}

	public String getFehlertexteCode() {
		StringBuilder fehlertexteCode = new StringBuilder();
		for (String code : fehlertexte.values().stream()
				.map(STParameterClassBase::toCode).sorted().collect(Collectors.toList())) {
			fehlertexteCode.append(code);
		}
		return fehlertexteCode.toString();
	}

	public Integer getRegelNummer() {
		return ruleInfo.getRuleNumber();
	}

	public String getFehlerTextCode(Locale locale) {
		if (ruleInfo.isPreliminary() && fehlertexte.get(locale) == null) {
			return "\"\"";
		}
		return fehlertexte.get(locale).getFehlerTextCode();
	}

	public List<String> getParameterMappingCode() {
		return parameterMapping.entrySet().stream()
				.map(e -> new StringParameter(e.getKey()).toCode() + ", " + new StringParameter(e.getValue()).toCode())
				.collect(Collectors.toList());
	}

	public RuleInfo getRule() {
		return ruleInfo;
	}
}
