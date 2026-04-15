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

import java.util.LinkedHashSet;
import java.util.Set;

import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;

public class VariantDocumentModelVisitor extends DocumentModelVisitor {

	private Set<String> variants = new LinkedHashSet<>();

	@Override
	public VisitProcess visitField(IField field) {
		field.getVariantFilter().ifPresent(v -> variants.addAll(v.getVariants()));
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitRule(IRule rule) {
		rule.getVariantFilter().ifPresent(v -> variants.addAll(v.getVariants()));
		variants.addAll(rule.getVariantErrorMessages().keySet());
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitComputation(IComputation computation) {
		computation.getVariantFilter().ifPresent(v -> variants.addAll(v.getVariants()));
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	public Set<String> getVariants() {
		return variants;
	}

}
