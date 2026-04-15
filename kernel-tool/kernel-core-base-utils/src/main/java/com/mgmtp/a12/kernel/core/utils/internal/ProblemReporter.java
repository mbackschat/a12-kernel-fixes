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

package com.mgmtp.a12.kernel.core.utils.internal;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.notification.Severity;

/**
 * Einfache Defaultimplementierung für {@link IProblemReporter}.
 */
public class ProblemReporter implements IProblemReporter {

	private final List<IProblem> problems = new ArrayList<>();
	private final List<IProblem> errors = new ArrayList<>();
	private final List<IProblem> warnings = new ArrayList<>();
	private Severity maxLevel = null;

	@Override
	public void reportProblem(IProblem problem) {
		problems.add(problem);
		updateMaxLevel(problem);
		if (problem.getSeverity() == Severity.ERROR) {
			errors.add(problem);
		}
		if (problem.getSeverity() == Severity.WARNING) {
			warnings.add(problem);
		}
	}

	private void updateMaxLevel(IProblem problem) {
		if (maxLevel == null) {
			maxLevel = problem.getSeverity();
		} else {
			if (maxLevel.ordinal() < problem.getSeverity().ordinal()) {
				maxLevel = problem.getSeverity();
			}
		}
	}

	public List<IProblem> getProblems() {
		return problems;
	}

	public List<IProblem> getErrors() {
		return errors;
	}

	public List<IProblem> getWarnings() {
		return warnings;
	}

	public boolean hasProblemOccurred() {
		return hasProblemOccurred(Severity.ERROR);
	}

	/**
	 * Returns true if the given level is below the highest seen problem. E.g. If we have seen only
	 * a warning, then hasProblemOccurred(WARNING) will return true, but hasProblemOccurred(ERROR)
	 * will return false.
	 *
	 * @param severity
	 * 		The weakest severity of problem we care about.
	 * @return true if the highest seen problem is at least as severe as the given level.
	 */
	public boolean hasProblemOccurred(Severity severity) {
		//Every level is above no problem => no problem above severity
		if (maxLevel == null) {
			return false;
		}

		return maxLevel.isAtLeastAsSevereAs(severity);
	}
}
