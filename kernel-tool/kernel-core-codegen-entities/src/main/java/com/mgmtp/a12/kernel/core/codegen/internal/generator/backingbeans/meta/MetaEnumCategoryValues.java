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
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;

/**
 * Parameter class for the templates used to generate the category values of an enumeration in
 * <code>Meta_</code>-classes.
 */
@StringTemplateGroup("meta_entity")
public class MetaEnumCategoryValues extends STParameterClassBase {

	private String commonFormatNumber;
	private List<String> xmlValues;
	private Map<String, List<String>> categories;

	public MetaEnumCategoryValues(String commonFormatNumber, IEnumerationType type) {
		this.commonFormatNumber = commonFormatNumber;
		this.xmlValues = type.getXMLValues();
		this.categories = type.getCategoryValues();
	}

	public boolean isNotEmpty() {
		return !categories.isEmpty() && !xmlValues.isEmpty();
	}

	public int getSizeOfValues() {
		return categories.values().stream()
				.flatMap(List<String>::stream)
				.mapToInt(String::length)
				.sum();
	}

	public List<String> getCategoryValueMappingCode() {
		return IntStream.range(0, xmlValues.size())
				.mapToObj(i -> new EnumerationCategory(xmlValues.get(i), getCategoryValues(i)).toCode())
				.collect(Collectors.toList());
	}

	private Map<String, String> getCategoryValues(int i) {
		return categories.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().get(i)));
	}

	public String getGemeinsamesFormatNummer() {
		return commonFormatNumber;
	}

	@StringTemplateGroup("meta")
	public class EnumerationCategory extends STParameterClassBase {

		private String xmlValue;
		private Map<String, String> categoryValues;

		public EnumerationCategory(String xmlValue, Map<String, String> categoryValues) {
			this.xmlValue = xmlValue;
			this.categoryValues = categoryValues;
		}

		public String getNummer() {
			return commonFormatNumber;
		}

		public String getXmlValue() {
			return new StringParameter(xmlValue).toCode();
		}

		public boolean isNotEmpty() {
			return !categoryValues.isEmpty();
		}

		public List<String> getCategoryValuesCode() {
			return categoryValues.entrySet().stream()
					.map(e -> new AdditionalKeyValuePair(e.getKey(), new StringParameter(e.getValue()).toCode()).toCode())
					.collect(Collectors.toList());
		}

	}

}
