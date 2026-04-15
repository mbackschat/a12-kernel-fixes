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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

@StringTemplateGroup("/common/$programmingLanguage$/base")
public class MapParameter<K extends STParameterClassBase, V extends STParameterClassBase> extends STParameterClassBase {

	private final List<K> keys = new ArrayList<>();
	private final List<V> values = new ArrayList<>();

	public static <T, K extends STParameterClassBase, V extends STParameterClassBase>
	Collector<T, ?, MapParameter<K, V>> collector(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper) {
		return Collector.of(
				MapParameter::new,
				(map, element) -> {
					K key = keyMapper.apply(element);
					V value = valueMapper.apply(element);
					map.addEntry(key, value);
				},
				(map1, map2) -> {
					map1.keys.addAll(map2.keys);
					map1.values.addAll(map2.values);
					return map1;
				}
		);
	}

	private void addEntry(K key, V value) {
		keys.add(key);
		values.add(value);
	}

	public List<String> getKeyCodeFragments() {
		return keys.stream().map(STParameterClassBase::toCode).toList();
	}

	public List<String> getValueCodeFragments() {
		return values.stream().map(STParameterClassBase::toCode).toList();
	}

	public boolean isEmpty() {
		return keys.isEmpty();
	}

	public int size() {
		return keys.size();
	}

}
