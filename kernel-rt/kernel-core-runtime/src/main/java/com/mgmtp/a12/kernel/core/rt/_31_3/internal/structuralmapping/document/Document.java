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

import java.util.Map;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.EntityImpl;

public class Document extends PresentGroupInstance {
	private final Map<String, Entity> fullNameToEntity;

	public Document(Map<String, Entity> fullNameToEntity) {
		super(EntityImpl.ROOT, null, 1, Origin.FROM_INPUT);
		this.fullNameToEntity = fullNameToEntity;
	}

	public GroupInstance getOrCreateGroupInstance(String fullName, long[] repetitions) {
		Entity entityToGet = fullNameToEntity.get(fullName);
		if (entityToGet == null) {
			return null;
		}

		return getOrCreateGroupInstance(entityToGet, repetitions);
	}

	public FieldInstance getOrCreateFieldInstance(String fullName, long[] repetitions, Object value) {
		Entity entityToGet = fullNameToEntity.get(fullName);
		if (entityToGet == null) {
			return null;
		}
		return getOrCreateFieldInstance(entityToGet, repetitions, value);
	}

	public FieldInstance getOrCreateFieldInstance(Entity entityToGet, long[] repetitions, Object value) {
		if (value == null || "".equals(value)) {
			return null;
		}

		GroupInstance parentInstance = getOrCreateGroupInstance(entityToGet.parent(), repetitions);
		FieldInstance fieldInstance = parentInstance.getOrCreateChildFieldInstance(entityToGet, Origin.FROM_INPUT);
		fieldInstance.setValue(value, Origin.FROM_INPUT);
		return fieldInstance;
	}

	public GroupInstance getOrCreateGroupInstance(Entity entityToGet, long[] repetitions) {
		if (entityToGet.level() > repetitions.length) {
			throw new IllegalArgumentException("repetitions array length is too small for given entity");
		}

		GroupInstance ancestorInstance = this;
		for (Entity ancestor : entityToGet.withAncestors()) {
			ancestorInstance = ancestorInstance.getOrCreateChildGroupInstance(
					ancestor, Math.toIntExact(repetitions[ancestor.levelIndex()]), Origin.FROM_INPUT);
		}
		return ancestorInstance;
	}
}
