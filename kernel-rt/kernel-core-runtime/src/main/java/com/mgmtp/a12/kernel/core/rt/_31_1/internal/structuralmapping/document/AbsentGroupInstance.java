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

public class AbsentGroupInstance extends AbstractAbsentEntityInstance implements GroupInstance {
	public AbsentGroupInstance(Entity entity, GroupInstance someAncestor) {
		super(entity, someAncestor);
	}

	@Override
	public GroupInstance getChildGroupInstance(Entity group, int oneBasedRepetition) {
		return null;
	}

	@Override
	public GroupInstance getOrCreateChildGroupInstance(Entity group, int oneBasedRepetition, Origin origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<GroupInstance> getChildGroupInstancesInOrder(Entity group) {
		return List.of();
	}

	@Override
	public int getNextFreeRepetition(Entity group) {
		return 1;
	}

	@Override
	public FieldInstance getChildFieldInstance(Entity field) {
		return null;
	}

	@Override
	public FieldInstance getOrCreateChildFieldInstance(Entity field, Origin origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GroupInstance removeChildGroupInstance(Entity group, int oneBasedRepetition) {
		return null;
	}

	@Override
	public GroupInstance createIfAbsent(Origin origin) {
		return parent().createIfAbsent(origin).getOrCreateChildGroupInstance(entity, 1, origin);
	}

	@Override
	public void printJsonOn(StringBuilder sb) {
		sb.append("{}");
	}
}
