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

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.Entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GroupInstances {
	private final Entity group;
	private final PresentGroupInstance parent;
	private final SortedMap<Integer, GroupInstance> repetitionToInstance = new TreeMap<>(); // if access to children here ever
	// becomes performance relevant (which seems unlikely right now), we could change this into a list or some other dedicated data
	// structure instead

	public GroupInstance getOrCreateGroupInstanceAtRepetition(int oneBasedRepetition, EntityInstance.Origin origin) {
		return repetitionToInstance.computeIfAbsent(oneBasedRepetition,
				key -> new PresentGroupInstance(group, parent, oneBasedRepetition, origin));
	}

	public List<GroupInstance> getAllGroupInstancesInOrder() {
		return List.copyOf(repetitionToInstance.values());
	}

	public GroupInstance getGroupInstanceAtRepetition(int oneBasedRepetition) {
		return repetitionToInstance.get(oneBasedRepetition);
	}

	public GroupInstance removeGroupInstanceAtRepetition(int oneBasedRepetition) {
		return repetitionToInstance.remove(oneBasedRepetition);
	}

	public int getNextFreeRepetition() {
		if (repetitionToInstance.isEmpty()) {
			return 1;
		} else if (repetitionToInstance.size() == repetitionToInstance.lastKey()) {
			return repetitionToInstance.size() + 1;
		} else {
			// this is linear, though highly unlikely to matter
			int previousRepetition = 0;
			for (int usedRepetition : repetitionToInstance.keySet()) {
				if (usedRepetition != previousRepetition + 1) {
					return previousRepetition + 1;
				}
				previousRepetition = usedRepetition;
			}
			return previousRepetition + 1;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(parent);
		sb.append("/");
		sb.append(group.shortName());
		sb.append("[");
		if (!repetitionToInstance.isEmpty()) {
			if (repetitionToInstance.size() == repetitionToInstance.lastKey()) {
				sb.append("1-");
				sb.append(repetitionToInstance.size());
			} else {
				sb.append(repetitionToInstance.keySet().stream()
						.map(Object::toString)
						.collect(Collectors.joining(",")));
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public void printJsonOn(StringBuilder sb) {
		sb.append("\"");
		sb.append(group.shortName());
		sb.append("\"");
		sb.append(":[");
		boolean isFirst = true;
		for (GroupInstance instance : getAllGroupInstancesInOrder()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(",");
			}
			instance.printJsonOn(sb);
		}
		sb.append("]");
	}
}
