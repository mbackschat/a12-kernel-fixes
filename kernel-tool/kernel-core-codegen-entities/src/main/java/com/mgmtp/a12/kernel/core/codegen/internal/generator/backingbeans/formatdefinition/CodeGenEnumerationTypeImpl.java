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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.FieldUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType.EnumerationValue;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 *
 */
public class CodeGenEnumerationTypeImpl extends CodeGenFeldTypBasis {

	private final List<String> xmlValues;
	private final Map<Locale, List<EnumerationValue>> displayValueMap = new HashMap<>();
	private final Map<String, List<String>> categoryValueMap;
	private final Map<Locale, String> spracheToFehlertext = new HashMap<>();
	private final String sValue;

	private final IEnumerationType enumType;
	private final ProgrammingLanguage lang;
	private final String name;

	public CodeGenEnumerationTypeImpl(IFieldType type, CodeGenEntityData data) {
		this.enumType = (IEnumerationType) type;
		this.lang = data.getParams().getProgrammingLanguage();
		this.name = data.getParams().getName();

		for (Locale l : data.getParams().getLanguages()) {
			String message = enumType.getErrorMessage(l);
			if (message != null) {
				spracheToFehlertext.put(l, message);
			}
		}

		this.xmlValues = enumType.getXMLValues();
		this.categoryValueMap = enumType.getCategoryValues();
		this.sValue = enumType.getsValue();

	}

	public IEnumerationType getEnumType() {
		return enumType;
	}

	public Map<Locale, String> getFehlertexte() {
		return spracheToFehlertext;
	}

	public String getName() {
		return name;
	}

	public ProgrammingLanguage getLang() {
		return lang;
	}

	public boolean isTableReference() {
		return FieldUtils.isTableEnum(enumType);
	}

	public String getTableName() {
		return new StringParameter(enumType.getTableName().orElse("")).toCode();
	}

	public String getColumnName() {
		return new StringParameter(enumType.getColumnName().orElse("")).toCode();
	}

	public List<String> getDependentColumns() {
		return enumType.getDependentColumns().stream()
				.map(s -> new StringParameter(s).toCode())
				.collect(Collectors.toList());
	}

	public boolean isWithDependentColumns() {
		return !enumType.getDependentColumns().isEmpty();
	}

	/**
	 * Die Methode wird für die C++ und VBA Codegenerierung verwendet. Da es dort keine
	 * Mehrsprachigkeit gibt, wird hier der deutsche Fehlertext zurückgegeben.
	 */
	public String getFehlerText() {
		// Locale.GERMANY used for C++ and VBA only
		String fehlerText = spracheToFehlertext.get(Locale.GERMANY);
		if (fehlerText == null) {
			return "";
		}
		return fehlerText;
	}

	public String getFehlerTextCode() {
		return new StringParameter(getFehlerText()).toCode();
	}

	public String getWerteCode() {
		return StringUtils.join(getWerte(), ", ");
	}

	public List<String> getWerte() {
		return makeCodeStringsInAnfuehrungszeichen(xmlValues);
	}

	/**
	 * Fügt Gänsefüße vorne und hinten an alle Strings der Liste an
	 */
	private List<String> makeCodeStringsInAnfuehrungszeichen(Collection<String> werte) {
		List<String> result = new ArrayList<>();
		for (String wert : werte) {
			result.add(new StringParameter(wert).toCode());
		}
		return result;
	}

	public List<String> getGermanOberflaechenWerte() {
		// Locale.GERMANY used for C++ only
		List<EnumerationValue> list = displayValueMap.get(Locale.GERMANY);
		if (list != null) {
			List<String> result = new LinkedList<>();
			for (EnumerationValue e : list) {
				result.add(e.getDisplayValue());
			}
			return makeCodeStringsInAnfuehrungszeichen(result);
		} else {
			return Collections.emptyList();
		}
	}

	public String getSValueCode() {
		return new StringParameter(sValue).toCode();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((xmlValues == null) ? 0 : xmlValues.hashCode());
		result = prime * result + spracheToFehlertext.hashCode();
		result = prime * result + displayValueMap.hashCode();
		result = prime * result + ((categoryValueMap == null) ? 0 : categoryValueMap.hashCode());
		result = prime * result + ((sValue == null) ? 0 : sValue.hashCode());
		result = prime * result + enumType.getTableName().hashCode();
		result = prime * result + enumType.getColumnName().hashCode();
		result = prime * result + ((enumType.getDependentColumns() == null) ? 0 : enumType.getDependentColumns().hashCode());
		return result;
	}

	/**
	 * this method is important to determine if two field definitions are equal.
	 *
	 * @return <code>true</code> if both field definitions are equals, <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CodeGenEnumerationTypeImpl other = (CodeGenEnumerationTypeImpl) obj;
		return Objects.equals(categoryValueMap, other.categoryValueMap) &&
				Objects.equals(displayValueMap, other.displayValueMap) &&
				Objects.equals(spracheToFehlertext, other.spracheToFehlertext) &&
				Objects.equals(xmlValues, other.xmlValues) &&
				Objects.equals(sValue, other.sValue) &&
				Objects.equals(enumType.getTableName(), other.enumType.getTableName()) &&
				Objects.equals(enumType.getColumnName(), other.enumType.getColumnName()) &&
				Objects.equals(enumType.getDependentColumns(), other.enumType.getDependentColumns());
	}

	public boolean hasOberflaechenWerte() {
		return !displayValueMap.isEmpty();
	}

	public boolean hasCategoryValues() {
		return !categoryValueMap.isEmpty();
	}

	public String getCategoryValuesCpp() {
		StringBuilder sb = new StringBuilder();
		Set<String> categories = categoryValueMap.keySet();
		for (int currentXmlValueIndex = 0; currentXmlValueIndex < xmlValues.size(); currentXmlValueIndex++) {
			for (String category : categories) {
				sb.append(new StringParameter(categoryValueMap.get(category).get(currentXmlValueIndex)).toCode()).append(", ");
			}
			sb.append("// ").append(xmlValues.get(currentXmlValueIndex)).append("\n");
		}

		return sb.toString();
	}

	public List<String> getCategories() {
		return makeCodeStringsInAnfuehrungszeichen(categoryValueMap.keySet());
	}

	public Map<Locale, List<EnumerationValue>> getDisplayValueMap() {
		return displayValueMap;
	}

	public void setOberflaechenwerte(Collection<Locale> unterstuetzteSprachen) {
		if (unterstuetzteSprachen.stream().anyMatch(l -> enumType.getDisplayValues(l) != null)) {
			for (Locale language : unterstuetzteSprachen) {
				List<EnumerationValue> list = enumType.getDisplayValues(language);
				if (list != null) {
					displayValueMap.put(language, list);
				}
			}
		}
	}

	@Override
	public String getContentVariables() {
		return toCode("content");
	}

	public String getClassnameSuffix() {
		if (lang == ProgrammingLanguage.CPLUS) {
			if (isTableReference()) {
				return "TableEnumeration";
			} else {
				return "Enumeration";
			}
		} else {
			return "String";
		}
	}
}
