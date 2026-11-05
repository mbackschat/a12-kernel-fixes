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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.reference;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document.AbsentFieldInstance;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document.EntityInstance;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document.FieldInstance;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document.GroupInstance;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true) @Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldReference implements EntityReference<FieldInstance> {
	private final GroupReference parentReference;
	private final Entity referencedEntity;

	public static FieldReference between(Entity from, Entity to) {
		return new FieldReference(GroupReference.between(from, to.parent()), to);
	}

	@Override
	public FieldInstance resolveInstance(EntityInstance from) {
		GroupInstance parent = parentReference.resolveInstance(from);
		if (parent.isPresent()) {
			FieldInstance child = parent.getChildFieldInstance(referencedEntity);
			if (child != null) {
				return child;
			}
		}
		return new AbsentFieldInstance(referencedEntity, parent);
	}

	@Override
	public Iterator<FieldInstance> resolvePresentInstances(EntityInstance from) {
		return new InstanceIterator(from);
	}

	private class InstanceIterator implements Iterator<FieldInstance> {
		Iterator<? extends GroupInstance> parentIterator;
		FieldInstance next;

		InstanceIterator(EntityInstance from) {
			this.parentIterator = parentReference.resolvePresentInstances(from);
			this.next = findNext();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public FieldInstance next() {
			FieldInstance result = next;
			if (result == null) {
				throw new NoSuchElementException();
			}
			next = findNext();
			return result;
		}

		private FieldInstance findNext() {
			while (parentIterator.hasNext()) {
				FieldInstance result = parentIterator.next().getChildFieldInstance(referencedEntity);
				if (result != null) {
					return result;
				}
			}
			return null;
		}
	}
}
