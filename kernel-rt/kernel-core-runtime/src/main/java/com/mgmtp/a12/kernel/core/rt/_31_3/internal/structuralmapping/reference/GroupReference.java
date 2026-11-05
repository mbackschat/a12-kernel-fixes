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
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document.AbsentGroupInstance;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document.EntityInstance;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document.GroupInstance;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true) @Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupReference implements EntityReference<GroupInstance> {
	@NonNull private final Entity turningPoint;
	@NonNull private final Entity referencedEntity;
	@NonNull private final Entity[] downwardSteps;

	public static GroupReference between(Entity from, Entity to) {
		Entity turningPoint = from.commonAncestor(to);
		Entity[] downwardSteps = to.withAncestorsUntil(turningPoint.level()).toArray(Entity[]::new);
		return new GroupReference(turningPoint, to, downwardSteps);
	}

	public static GroupReference upward(Entity to) {
		return new GroupReference(to, to, new Entity[0]);
	}

	@Override
	public GroupInstance resolveInstance(EntityInstance from) {
		GroupInstance result = resolveTurningPoint(from);
		for (Entity downwardStep : downwardSteps) {
			GroupInstance child = result.getChildGroupInstance(downwardStep, 1);
			if (child == null) {
				return new AbsentGroupInstance(referencedEntity, result);
			}
			result = child;
		}
		return result;
	}

	@Override
	public Iterator<GroupInstance> resolvePresentInstances(EntityInstance from) {
		GroupInstance turningPointInstance = resolveTurningPoint(from);
		return turningPoint.equals(referencedEntity)
				? new SingletonIterator<>(turningPointInstance)
				: new InstanceIterator(turningPointInstance);

	}

	private GroupInstance resolveTurningPoint(EntityInstance from) {
		EntityInstance result = from;
		while (!result.entity().equals(turningPoint)) {
			result = result.parent();
		}
		return (GroupInstance) result;
	}

	private class InstanceIterator implements Iterator<GroupInstance> {
		@SuppressWarnings("unchecked")
		Iterator<? extends GroupInstance>[] iteratorPerLevel = new Iterator[downwardSteps.length + 1];
		GroupInstance[] groupInstancePerLevel = new GroupInstance[iteratorPerLevel.length];
		GroupInstance next;
		int level = 1;
		boolean reset = true;

		InstanceIterator(GroupInstance from) {
			groupInstancePerLevel[0] = from;
			next = findNext();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public GroupInstance next() {
			GroupInstance result = next;
			if (result == null) {
				throw new NoSuchElementException();
			}
			next = findNext();
			return result;
		}

		private GroupInstance findNext() {
			while (level >= 1) {
				if (reset) {
					iteratorPerLevel[level] =
							groupInstancePerLevel[level - 1].getChildGroupInstancesInOrder(downwardSteps[level - 1]).iterator();
				}

				if (iteratorPerLevel[level].hasNext()) {
					groupInstancePerLevel[level] = iteratorPerLevel[level].next();
				} else {
					reset = false;
					level--;
					continue;
				}

				if (level + 1 < iteratorPerLevel.length) {
					reset = true;
					level++;
				} else {
					reset = false;
					return groupInstancePerLevel[level];
				}
			}
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("..");
		sb.append(referencedEntity.shortName());
		for (Entity downwardStep : downwardSteps) {
			sb.append("/");
			sb.append(downwardStep.shortName());
		}
		return sb.toString();
	}
}
