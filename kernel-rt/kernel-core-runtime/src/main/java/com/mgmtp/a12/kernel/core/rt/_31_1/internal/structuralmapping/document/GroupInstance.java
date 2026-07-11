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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.document;

import java.util.List;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.Entity;

public interface GroupInstance extends EntityInstance {
	/**
	 * @param group
	 * 		the group to get.
	 * @param oneBasedRepetition
	 * 		the repetition to get (1-based).
	 * @return the group instance or {@code null} if there is none.
	 */
	GroupInstance getChildGroupInstance(Entity group, int oneBasedRepetition);

	GroupInstance getOrCreateChildGroupInstance(Entity group, int oneBasedRepetition, Origin origin);

	default GroupInstance createChildGroupInstance(Entity group, Origin origin) {
		return getOrCreateChildGroupInstance(group, getNextFreeRepetition(group), origin);
	}

	List<GroupInstance> getChildGroupInstancesInOrder(Entity group);

	int getNextFreeRepetition(Entity group);

	FieldInstance getChildFieldInstance(Entity field);

	FieldInstance getOrCreateChildFieldInstance(Entity field, Origin origin);

	@Override
	GroupInstance createIfAbsent(Origin origin);

	GroupInstance removeChildGroupInstance(Entity group, int oneBasedRepetition);

	default void remove() {
		parent().removeChildGroupInstance(entity(), repetition());
	}
}
