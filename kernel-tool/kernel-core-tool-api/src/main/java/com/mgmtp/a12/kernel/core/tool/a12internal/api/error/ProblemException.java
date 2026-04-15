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

package com.mgmtp.a12.kernel.core.tool.a12internal.api.error;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * An instance of this class is thrown as checked exception if the API demands an Exception instead of an {@link IProblemReporter}
 */
@OnlyForUsage
public class ProblemException extends Exception {
	List<IProblem> problems;

	public ProblemException(List<IProblem> problems) {
		super(combineMessage(null, problems));
		this.problems = problems;
	}

	public ProblemException(String message, List<IProblem> problems) {
		super(combineMessage(message, problems));
		this.problems = problems;
	}

	public ProblemException(String message, Throwable cause, List<IProblem> problems) {
		super(combineMessage(message, problems), cause);
		this.problems = problems;
	}

	public ProblemException(Throwable cause, List<IProblem> problems) {
		super(combineMessage(null, problems), cause);
		this.problems = problems;
	}

	public List<IProblem> getProblems() {
		return problems;
	}

	private static String combineMessage(String message, List<IProblem> problems) {
		return (message == null ? "" : message + ": " + System.lineSeparator())
				+ joinProblems(problems);
	}

	/**
	 * @return a string with the given error messages
	 */
	private static String joinProblems(List<IProblem> problems) {
		return problems.stream()
				.map(p -> Objects.toString(p.getSource()) + ": " + p.getMessage())
				.collect(Collectors.joining(System.lineSeparator()));
	}
}
