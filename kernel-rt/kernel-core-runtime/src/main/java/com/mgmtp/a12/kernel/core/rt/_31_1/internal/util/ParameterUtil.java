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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.util.Map;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

public class ParameterUtil {

	private ParameterUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Checks if the passed parameter is of type {@link Map} with key type {@code K} and value type {@code V} (e.g.
	 * <code>{@link Map}&lt;{@link IIdentifier}, {@link String}&gt;</code>). If not, an {@link IllegalArgumentException} is thrown.
	 *
	 * @param p
	 * 		parameter to be checked
	 * @param paramCardinalNumber
	 * 		used for the error message
	 * @param expectedKeyType replaces the expected key type in the error message
	 * @param expectedValueType replaces the expected value type in the error message
	 */
	public static <K, V> void checkIsMap(Object p, String paramCardinalNumber, Class<K> expectedKeyType, Class<V> expectedValueType) {
		if (p != null && (!(p instanceof Map<?, ?> map) || map.entrySet().stream().anyMatch(
				e -> !expectedKeyType.isInstance(e.getKey()) || !expectedValueType.isInstance(e.getValue())))) {
			throw new IllegalArgumentException(
					String.format("Der %s zusaetzlich zum Schluessel uebergebene Parameter muss vom Typ Map<%s, %s> sein.",
							paramCardinalNumber, expectedKeyType.getSimpleName(), expectedValueType.getSimpleName()));
		}
	}

}
