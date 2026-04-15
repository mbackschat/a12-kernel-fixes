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

package com.mgmtp.a12.kernel.md.model.internal.service.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;

import lombok.Getter;

@Getter
public class TransientFieldsInGroupsFinder extends DocumentModelVisitorWithFullNames {
	/**
	 * contains
	 * 1. innermost repeatable group of the transient field
	 * 2. the path from the IRG to the transient field (the groups here are all non-repeatable)
	 * <p>
	 * e.g. ("/a/b", "/c/d"), for the transient field /a[1]/b[5]/c[1]/d (group repeatabilities denoted with [])
	 * e.g. ("", "/g1/g2/f1"), for the transient field /g1[1]/g2[1]/f1 (group repeatabilities denoted with [])
	 */
	private final List<Map.Entry<String, String>> transientFields = new ArrayList<>(); // intentionally not a map! keys not unique
	private final List<String> repeatableGroupPathStack = new LinkedList<>(List.of(""));

	@Override
	public void enterGroup(IGroup group) {
		if (group.getRepeatability() > 1) {
			repeatableGroupPathStack.addLast(getFullName());
		}
	}

	@Override
	public void leaveGroup(IGroup group) {
		if (group.getRepeatability() > 1) {
			repeatableGroupPathStack.removeLast();
		}
	}

	@Override public VisitProcess visitField(IField field) {
		if (!field.isTransient()) {
			return super.visitField(field);
		}

		String irg = repeatableGroupPathStack.getLast(); // if not repeatable, then ""
		transientFields.add(Map.entry(irg, getFullName().substring(irg.length())));
		return super.visitField(field);
	}
}
