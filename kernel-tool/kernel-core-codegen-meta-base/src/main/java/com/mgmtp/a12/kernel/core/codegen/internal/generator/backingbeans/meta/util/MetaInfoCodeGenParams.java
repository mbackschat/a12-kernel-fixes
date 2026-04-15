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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;

public class MetaInfoCodeGenParams implements IMetaInfoCodeGenParams {

	private String metaElementType;
	private String key;
	private String defaultCode;
	private Map<String, MetaInfoValue> nameValueMap = new HashMap<>();
	private boolean internal = false;

	private final class MetaInfoValueImpl implements MetaInfoValue {

		private final String code;

		private MetaInfoValueImpl(String code) {
			this.code = code;
		}

		@Override
		public String toCode() {
			return code;
		}
	}

	public void setMetaElementType(String type) {
		this.metaElementType = type;
	}

	public void setInternal(boolean internal) {
		this.internal = internal;
	}

	public boolean isInternal() {
		return internal;
	}

	@Override
	public String getMetaElementType() {
		return metaElementType;
	}

	public void setKey(String k) {
		this.key = k;
	}

	@Override
	public String getKey() {
		return key;
	}

	public void setDefaultCode(String code) {
		this.defaultCode = code;
	}

	@Override
	public String getDefaultCode() {
		return defaultCode;
	}

	public void addNameValuePair(String name, String value) {
		this.nameValueMap.put(name, new MetaInfoValueImpl(value));
	}

	@Override
	public Map<String, MetaInfoValue> getNameValueMap() {
		return nameValueMap;
	}

	public static List<IMetaInfoCodeGenParams> getFilteredSortedAdditionalValuesCpp(
			List<IMetaInfoCodeGenParams> additionalValues,
			List<String> additionalValuesCppOrdering
	) {
		// For C++ the additional values have to be in specific order so that they conform to the structs' component order
		Map<String, IMetaInfoCodeGenParams> tmpMap = additionalValues.stream()
				.collect(Collectors.toMap(IMetaInfoCodeGenParams::getKey, Function.identity()));
		return additionalValuesCppOrdering.stream().filter(tmpMap::containsKey).map(tmpMap::get).toList();
	}
}
