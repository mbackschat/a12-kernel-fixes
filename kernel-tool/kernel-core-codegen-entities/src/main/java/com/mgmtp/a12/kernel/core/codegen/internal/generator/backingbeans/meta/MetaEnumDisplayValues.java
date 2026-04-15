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
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.LanguageHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringFormatUtil;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType.EnumerationValue;

/**
 * Parameter class for the templates used to generate the display values of an enumeration in
 * <code>Meta_</code>-classes.
 */
@StringTemplateGroup("meta_entity")
public class MetaEnumDisplayValues extends STParameterClassBase {

	private String commonFormatNumber;
	private Map<Locale, List<EnumerationValue>> localeToEnumerationValuesMap;

	public MetaEnumDisplayValues(String commonFormatNumber, Map<Locale, List<EnumerationValue>> oflXmlWerte) {
		this.commonFormatNumber = commonFormatNumber;
		this.localeToEnumerationValuesMap = oflXmlWerte;
	}

	public String getOberflaechenWerteCode() {
		StringBuilder oberflaechenWerteCode = new StringBuilder();
		for (Entry<Locale, List<EnumerationValue>> entry : localeToEnumerationValuesMap.entrySet()) {
			Locale sprache = entry.getKey();
			List<EnumerationValue> enumerationValues = entry.getValue();
			oberflaechenWerteCode.append((new EnumerationOberflaechenWerte(sprache, enumerationValues)).toCode());
		}
		return oberflaechenWerteCode.toString();
	}

	public int getSizeOfValues() {
		return localeToEnumerationValuesMap.values().stream()
				.flatMap(l -> l.stream())
				.mapToInt(ev -> ev.getDisplayValue().length() + ev.getXmlValue().length())
				.sum();
	}

	public String getGemeinsamesFormatNummer() {
		return commonFormatNumber;
	}

	@StringTemplateGroup("meta")
	public class EnumerationOberflaechenWerte extends STParameterClassBase {

		private Locale sprache;
		private List<EnumerationValue> enumValues;

		public EnumerationOberflaechenWerte(Locale sprache, List<EnumerationValue> enumValues) {
			this.sprache = sprache;
			this.enumValues = enumValues;
		}

		public String getOberflaechenWerteCode() {
			List<String> displayValues = enumValues.stream()
					.map(EnumerationValue::getDisplayValue)
					.collect(Collectors.toList());
			return StringFormatUtil.makeCodeString(displayValues);
		}

		public String getXmlWerteCode() {
			List<String> xmlValues = enumValues.stream()
					.map(EnumerationValue::getXmlValue)
					.collect(Collectors.toList());
			return StringFormatUtil.makeCodeString(xmlValues);
		}

		public String getLocaleCode() {
			return new LanguageHelper(sprache).toCode();
		}

		public String getNummer() {
			return commonFormatNumber;
		}
	}

}
