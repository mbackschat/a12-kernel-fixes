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

package com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl;

import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.FeldIdUtil;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IGroupIndexParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.ISemanticIndex;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.BaseErrorTextParameterLexer;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.languages.ErrortextParameterMultiLanguageHandler;

import lombok.Getter;
import lombok.Setter;

/**
 *
 */
public class GroupIndexParameter extends ErrortextParameter implements IGroupIndexParameter {

	private String group;
	@Getter private final String groupFullName;

	@Getter @Setter private ISemanticIndex semanticIndex;

	public GroupIndexParameter(String group, String groupFullName, ISemanticIndex semanticIndex) {
		this.group = group;
		this.groupFullName = groupFullName;
		this.semanticIndex = semanticIndex;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public void setGroup(String group) {
		this.group = group;
	}

	@Override
	public String format(Locale lang) {
		ErrortextParameterMultiLanguageHandler mlh = getMultiLanguageHandler(lang);
		String groupName = group != null ?
				FeldIdUtil.quoteNameIfNecessary(group, mlh) :
				mlh.getTerminalName(BaseErrorTextParameterLexer.RULE_GROUP);
		var semanticIndexSpec = Optional.ofNullable(semanticIndex).map(ISemanticIndex::getSemanticIndexSpec)
				.orElse(null);
		String semanticIndexReference = formatSemanticIndex(semanticIndexSpec, lang);
		return String.format("#%s%s", groupName, semanticIndexReference);
	}

}
