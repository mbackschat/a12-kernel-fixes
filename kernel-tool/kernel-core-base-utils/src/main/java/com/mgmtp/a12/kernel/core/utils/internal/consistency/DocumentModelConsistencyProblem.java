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

package com.mgmtp.a12.kernel.core.utils.internal.consistency;

import java.util.Locale;

import com.mgmtp.a12.model.consistency.ConsistencyCategory;
import com.mgmtp.a12.model.consistency.ConsistencyProblem;
import com.mgmtp.a12.model.consistency.Problem;
import com.mgmtp.a12.model.notification.Severity;

public class DocumentModelConsistencyProblem implements Problem {
	private final ConsistencyProblem problem;

	public DocumentModelConsistencyProblem(
			String modelId,
			Object source,
			ConsistencyCategory category,
			Severity severity,
			String... parameters
	) {
		this.problem = new ConsistencyProblem(modelId, category, source, severity, (Object[]) parameters);
	}

	@Override public String getModelId() {
		return problem.getModelId();
	}

	@Override public ConsistencyCategory getCategory() {
		return problem.getCategory();
	}

	@Override public String getLocalizedMessage(Locale locale) {
		return problem.getLocalizedMessage(locale);
	}

	@Override public String getMessage() {
		return problem.getMessage();
	}

	@Override public Severity getSeverity() {
		return problem.getSeverity();
	}

	@Override public Object getSource() {
		return problem.getSource();
	}
}
