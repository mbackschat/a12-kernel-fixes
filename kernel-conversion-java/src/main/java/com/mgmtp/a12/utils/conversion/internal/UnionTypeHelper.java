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

package com.mgmtp.a12.utils.conversion.internal;

import java.util.Optional;

public interface UnionTypeHelper<T extends Enum<T> & UnionTypeHelper<T>> {
	/**
	 * @return true if the type of values corresponds to the element of the type union that is represented by this
	 * enum instance
	 */
	boolean matches(Object value);

	static <T extends Enum<T> & UnionTypeHelper<T>> Optional<T> tagFor(Object value, Class<T> enmCls) {
		for(T t: enmCls.getEnumConstants()) {
			if (t.matches(value)) {
				return Optional.of(t);
			}
		}
		return Optional.empty();
	}

	static <T extends Enum<T> & UnionTypeHelper<T>> T tagOrThrow(Object value, Class<T> enmCls) {
		return tagFor(value, enmCls)
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("unexpected type of %1$s value: %2$s (value: %3$s)",
								enmCls.getSimpleName(), value.getClass(), value)));
	}

	static <T extends Enum<T> & UnionTypeHelper<T>> boolean hasValidType(Object value, Class<T> enmCls) {
		return tagFor(value, enmCls).isPresent();
	}

	static <T extends Enum<T> & UnionTypeHelper<T>> Object checkType(Object value, Class<T> enmCls) {
		tagOrThrow(value, enmCls);
		return value;
	}
}
