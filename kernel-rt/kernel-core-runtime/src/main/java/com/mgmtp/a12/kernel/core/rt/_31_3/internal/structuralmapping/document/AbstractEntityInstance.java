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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.Entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true) @Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractEntityInstance implements EntityInstance {
	protected final Entity entity;
	private int cachedHashCode;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EntityInstance entityInstance)) {
			return false;
		}
		if (!entity.equals(entityInstance.entity())) {
			return false;
		}
		EntityInstance ancestorA = this;
		EntityInstance ancestorB = entityInstance;
		while (!ancestorA.isRoot()) {
			if (ancestorA.repetition() != ancestorB.repetition()) {
				return false;
			}
			ancestorA = ancestorA.parent();
			ancestorB = ancestorB.parent();
		}
		return true;
	}

	@Override
	public int hashCode() {
		if (cachedHashCode == 0) {
			int result = 1;
			EntityInstance parent = parent();
			result = result * 59 + (parent == null ? 43 : parent.hashCode());
			result = result * 59 + entity.hashCode();
			result = result * 59 + repetition();
			if (result == 0) {
				result = Integer.MIN_VALUE;
			}
			cachedHashCode = result;
		}
		return cachedHashCode;
	}
}
