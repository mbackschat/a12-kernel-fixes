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

package com.mgmtp.a12.kernel.md.model.internal.services.join.util;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporterProxy;

/**
 * In this implementation, we want to distinguish between critical issues (issues which should lead to abort the process) and other
 * issues (problems, warnings, infos which should produce a result).
 */
public class JoiningProblemReporter extends ProblemReporterProxy {

	private ProblemReporterProxy criticalIssues;

	public JoiningProblemReporter(IProblemReporter problemReporter) {
		this(problemReporter, null);
	}

	/**
	 * @param criticalIssues
	 * 		if {@code null}, the problems reported in {@link #reportCriticalProblem(IProblem)} will be reported to
	 *        {@code problemReporter} and in this case, {@link #hasCriticalErrorOccurred()} considers the same problems as
	 *        {@link #hasErrorOccurred()}. If not {@code null}, the problems reported in {@link #reportCriticalProblem(IProblem)} will
	 * 		be
	 * 		gathered there and only these problems will be considered when calling {@link #hasCriticalErrorOccurred()}.
	 */
	public JoiningProblemReporter(IProblemReporter problemReporter, IProblemReporter criticalIssues) {
		super(problemReporter);
		if (criticalIssues != null) {
			this.criticalIssues = new ProblemReporterProxy(criticalIssues);
		}
	}

	public void reportCriticalProblem(IProblem criticalProblem) {
		if (criticalIssues == null) {
			super.reportProblem(criticalProblem);
		} else {
			criticalIssues.reportProblem(criticalProblem);
		}
	}

	public boolean hasCriticalErrorOccurred() {
		if (criticalIssues == null) {
			return super.hasErrorOccurred();
		} else {
			return criticalIssues.hasErrorOccurred();
		}
	}

}
