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

package com.mgmtp.a12.utils.conversion.a12internal;

import java.math.BigDecimal;
import java.time.Instant;

import com.mgmtp.a12.utils.conversion.InstantRange;
import com.mgmtp.a12.utils.conversion.internal.UnionTypeHelper;

public enum BasicTypeV2 implements UnionTypeHelper<BasicTypeV2> {
	NULL(null),
	STRING(String.class),
	BOOLEAN(Boolean.class),
	BIG_DECIMAL(BigDecimal.class),
	INSTANT(Instant.class),
	INSTANT_RANGE(InstantRange.class);
	private final Class<?> runtimeClass;

	BasicTypeV2(Class<?> runtimeClass) {
		this.runtimeClass = runtimeClass;
	}

	public boolean matches(Object value) {
		if (runtimeClass == null) {
			return value == null;
		} else {
			return runtimeClass.isInstance(value);
		}
	}

	public static BasicTypeV2 tagOrThrow(Object value) {
		return UnionTypeHelper.tagOrThrow(value, BasicTypeV2.class);
	}

	public static boolean hasValidType(Object value) {
		return UnionTypeHelper.hasValidType(value, BasicTypeV2.class);
	}

	public static Object checkType(Object value) {
		return UnionTypeHelper.checkType(value, BasicTypeV2.class);
	}
}