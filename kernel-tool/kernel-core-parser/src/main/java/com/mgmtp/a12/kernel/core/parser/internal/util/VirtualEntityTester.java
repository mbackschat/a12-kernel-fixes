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

package com.mgmtp.a12.kernel.core.parser.internal.util;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;

/**
 * Tests a {@link IParseTreeEntity} whether there is a {@link IVirtual} entity referenced.
 */
public class VirtualEntityTester extends ASTVisitor {
	public static boolean containsVirtualEntities(IParseTreeEntity parseTree) {
		VirtualEntityTester visitor = new VirtualEntityTester();
		parseTree.traverse(visitor);
		return visitor.hasVirtualEntities;
	}

	private boolean hasVirtualEntities = false;

	@Override public boolean visit(IEntityId p) {
		if (p instanceof IVirtual) {
			hasVirtualEntities = true;
		}
		return !hasVirtualEntities;
	}

	@Override public boolean visitGeneral(IParseTreeEntity e) {
		// We can prune if we have found one virtual entity
		return !hasVirtualEntities;
	}
}
