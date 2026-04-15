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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;

import lombok.Getter;

/**
 * Gather the information about transient fields in a {@link IDocumentModel}.
 */
@Getter
public class TransientFieldsFinder extends DocumentModelVisitorWithFullNames {

	/**
	 * @return names of the repeatable transient fields mapped by the corresponding parent path. Is
	 * 		never {@code null}.
	 */
	private final Map<String, Set<String>> repeatableTransientFieldsByParentPath = new HashMap<>();
	/**
	 * @return names of the non-repeatable transient fields mapped by the corresponding parent path.
	 * 		Is never {@code null}.
	 */
	private final Set<String> nonRepeatableTransientFieldPaths = new HashSet<>();
	/**
	 * @return all the paths of transient fields. Is never {@code null}.
	 */
	private final Set<String> allTransientFieldPaths = new HashSet<>();

	@Override
	public VisitProcess visitField(IField field) {
		if (!field.isTransient()) {
			return super.visitField(field);
		}
		if (ElementUtils.isRepeatable(field)) {
			Set<String> transientFields = repeatableTransientFieldsByParentPath.computeIfAbsent(getParentFullName(),
					k -> new HashSet<>());
			transientFields.add(field.getName());
		} else {
			nonRepeatableTransientFieldPaths.add(getFullName());
		}
		allTransientFieldPaths.add(getFullName());
		return super.visitField(field);
	}
}
