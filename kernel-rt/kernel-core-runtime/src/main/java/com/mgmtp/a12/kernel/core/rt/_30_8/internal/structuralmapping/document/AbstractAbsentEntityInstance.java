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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.document;

import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.Entity;

/**
 * Represents an entity instance that is not part of the document by assuming that all levels between the entity of the absent
 * entity instance and the closest ancestor actually contained in the document are at repetition {@literal 1}.
 * <p>
 * Can be used e.g. to represent field instances whose innermost repeatable group is present in the document, as if they were normal
 * unfilled field instances (in order to simplify code where that difference doesn't matter).
 * <p>
 * Immutable!
 */
public abstract class AbstractAbsentEntityInstance extends AbstractEntityInstance {
	protected final GroupInstance closestPresentAncestor;
	protected GroupInstance parent;

	protected AbstractAbsentEntityInstance(Entity entity, GroupInstance someAncestor) {
		super(entity);
		this.closestPresentAncestor = someAncestor instanceof AbsentGroupInstance absentGroupInstance
				? absentGroupInstance.closestPresentAncestor
				: someAncestor;
	}

	@Override
	public GroupInstance parent() {
		if (parent == null) {
			parent = entity.parent().equals(closestPresentAncestor.entity())
					? closestPresentAncestor
					: new AbsentGroupInstance(entity.parent(), closestPresentAncestor);
		}
		return parent;
	}

	@Override
	public int repetition() {
		return 1;
	}

	@Override
	public boolean isPresent() {
		return false;
	}

	@Override
	public Origin origin() {
		return Origin.UNKNOWN;
	}

	@Override
	public String toString() {
		return closestPresentAncestor.toString()
				+ "(/"
				+ entity.withAncestorsUntil(closestPresentAncestor.level()).stream()
				.map(Entity::shortName)
				.collect(Collectors.joining("/"))
				+ ")";
	}
}
