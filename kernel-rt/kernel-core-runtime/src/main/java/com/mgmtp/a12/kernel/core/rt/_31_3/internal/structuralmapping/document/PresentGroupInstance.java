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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.Entity;

public class PresentGroupInstance extends AbstractPresentEntityInstance implements GroupInstance {
	private final int repetition;
	private final Origin origin;
	private final Map<String, GroupInstances> childGroups = new HashMap<>();
	private final Map<String, FieldInstance> childFields = new HashMap<>();

	public PresentGroupInstance(Entity entity, PresentGroupInstance parent, int repetition, Origin origin) {
		super(entity, parent);
		this.repetition = repetition;
		this.origin = origin;
	}

	@Override
	public Origin origin() {
		return origin;
	}

	private GroupInstances getOrCreateChildGroupInstances(Entity group) {
		return childGroups.computeIfAbsent(group.shortName(), key -> new GroupInstances(group, this));
	}

	private GroupInstances getChildGroupInstances(Entity group) {
		return childGroups.get(group.shortName());
	}

	@Override
	public List<GroupInstance> getChildGroupInstancesInOrder(Entity group) {
		GroupInstances groupInstances = getChildGroupInstances(group);
		return groupInstances != null
				? groupInstances.getAllGroupInstancesInOrder()
				: List.of();
	}

	@Override
	public GroupInstance getChildGroupInstance(Entity group, int oneBasedRepetition) {
		GroupInstances instances = getChildGroupInstances(group);
		return instances != null
				? instances.getGroupInstanceAtRepetition(oneBasedRepetition)
				: null;
	}

	@Override
	public GroupInstance getOrCreateChildGroupInstance(Entity group, int oneBasedRepetition, Origin origin) {
		return getOrCreateChildGroupInstances(group).getOrCreateGroupInstanceAtRepetition(oneBasedRepetition, origin);
	}

	@Override
	public GroupInstance createChildGroupInstance(Entity group, Origin origin) {
		GroupInstances groupInstances = getOrCreateChildGroupInstances(group);
		return groupInstances.getOrCreateGroupInstanceAtRepetition(groupInstances.getNextFreeRepetition(), origin);
	}

	@Override
	public GroupInstance removeChildGroupInstance(Entity group, int oneBasedRepetition) {
		GroupInstances instances = getChildGroupInstances(group);
		return instances != null
				? instances.removeGroupInstanceAtRepetition(oneBasedRepetition)
				: null;
	}

	@Override
	public int getNextFreeRepetition(Entity group) {
		GroupInstances groupInstances = getChildGroupInstances(group);
		return groupInstances != null
				? groupInstances.getNextFreeRepetition()
				: 1;
	}

	@Override
	public FieldInstance getChildFieldInstance(Entity field) {
		return childFields.get(field.shortName());
	}

	@Override
	public FieldInstance getOrCreateChildFieldInstance(Entity field, Origin origin) {
		return childFields.computeIfAbsent(field.shortName(), key -> new PresentFieldInstance(field, this, origin));
	}

	@Override
	public int repetition() {
		return repetition;
	}

	@Override
	public GroupInstance createIfAbsent(Origin origin) {
		return this;
	}

	@Override
	public void printJsonOn(StringBuilder sb) {
		sb.append("{");
		boolean isFirst = true;
		for (var childField : childFields.values()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(",");
			}
			childField.printJsonOn(sb);
		}
		for (var groupInstances : childGroups.values()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(",");
			}
			groupInstances.printJsonOn(sb);
		}
		sb.append("}");
	}
}
