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

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;

/**
 * Builder for the parameter class for the string template representing the set of error messages
 * for a rule.
 */
public class MetaRegelFehlertexteBuilder {
	private final RuleInfo ruleInfo;
	private final Map<Locale, Fehlertext> fehlertexte;
	private final CodeGenData data;
	private final Map<String, String> full2UniqueFieldName;

	public MetaRegelFehlertexteBuilder(RuleInfo ruleInfo, CodeGenData data) {
		this.ruleInfo = ruleInfo;
		this.data = data;
		this.full2UniqueFieldName = data.getUniqueFieldNames();

		this.fehlertexte = ruleInfo.getErrorMessages().entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> createErrorText(e.getKey(), e.getValue())));
	}

	private Fehlertext createErrorText(Locale locale, String errorText) {
		return new FehlertextBuilder(locale, errorText, ruleInfo, data, full2UniqueFieldName)
				.build();
	}

	Map<Locale, Fehlertext> getFehlertexte() {
		return fehlertexte;
	}

	RuleInfo getRuleInfo() {
		return ruleInfo;
	}

	Map<String, String> getParameterMapping() {
		return fehlertexte.values().stream()
				.flatMap(e -> e.getParameterMapping().entrySet().stream())
				.distinct()
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public MetaRegelFehlertexte build() {
		return new MetaRegelFehlertexte(this);
	}
}
