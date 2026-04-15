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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.LanguageHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringFormatUtil;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

/**
 * * Parameter class for the templates used to generate the hint lists of a string (or NAE) in
 * <code>Meta_</code>-classes.
 */
@StringTemplateGroup("meta_entity")
public class MetaStringHintLists extends STParameterClassBase {

	private final String number;
	private final Map<Locale, List<String>> localeToStringsMap;

	public MetaStringHintLists(String number, Map<Locale, List<String>> werte) {
		this.number = number;
		this.localeToStringsMap = werte;
	}

	public String getHintListsCode() {
		StringBuilder hintListsCode = new StringBuilder();
		for (Entry<Locale, List<String>> entry : localeToStringsMap.entrySet()) {
			Locale sprache = entry.getKey();
			List<String> hintLists = entry.getValue();
			hintListsCode.append((new HintLists(sprache, hintLists)).toCode());
		}
		return hintListsCode.toString();
	}

	public int getSizeOfValues() {
		return localeToStringsMap.values().stream()
				.flatMap(Collection::stream)
				.mapToInt(String::length)
				.sum();
	}

	public String getGemeinsamesFormatNummer() {
		return number;
	}

	@StringTemplateGroup("meta")
	public class HintLists extends STParameterClassBase {

		private final Locale locale;
		private final List<String> values;

		public HintLists(Locale language, List<String> enumValues) {
			this.locale = language;
			this.values = enumValues;
		}

		public String getValuesCode() {
			return StringFormatUtil.makeCodeString(values);
		}

		public String getLocaleCode() {
			return new LanguageHelper(locale).toCode();
		}

		public String getNummer() {
			return number;
		}

	}

}
