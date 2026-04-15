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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import java.util.ArrayList;
import java.util.List;

@StringTemplateGroup("/common/$programmingLanguage$/base")
public class CodeGenMap<K extends ICodeOutput, V extends ICodeOutput> extends STParameterClassBase {

	private final String parameterTypeAsString;
	private List<CodeGenMapEntry<K, V>> entries;

	public CodeGenMap(String parameterTypeAsString) {
		this.entries = new ArrayList<>();
		this.parameterTypeAsString = parameterTypeAsString;
	}

	public void putEntry(K key, V value) {
		entries.add(new CodeGenMapEntry<>(key, value));
	}

	public List<String> getEntryListCode() {
		List<String> result = new ArrayList<>();
		for (CodeGenMapEntry<K, V> entry : entries) {
			result.add(entry.toCode());
		}
		return result;
	}

	public boolean isNotEmpty() {
		return !entries.isEmpty();
	}

	public String getParameterTypeAsString() {
		return parameterTypeAsString;
	}

	public int size() {
		return entries.size();
	}
}
