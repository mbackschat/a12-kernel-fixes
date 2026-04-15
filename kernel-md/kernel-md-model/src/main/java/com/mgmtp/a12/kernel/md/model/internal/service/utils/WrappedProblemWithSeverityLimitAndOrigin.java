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

import java.util.Date;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.model.notification.Severity;

/**
 * A wrapper class for IProblems that allows to override some of the fields in an IProblem:
 * <ul>
 *   <li>limit the severity be providing a maximum severity, to indicate that while a problem may be an error for parsing, it may be
 *       just a warning in the context of document model expansion (where invalid rules are simply left unconverted).</li>
 *   <li>override the origin object</li>
 * </ul>
 * The original problem can be retrieved via {@link #unwrap()}, in case more detailed information from the specific
 * IProblem implementation is needed.
 */
public class WrappedProblemWithSeverityLimitAndOrigin implements IProblem {
	protected final IProblem originalProblem;
	protected final Severity severity;
	protected final Object origin;

	public WrappedProblemWithSeverityLimitAndOrigin(
			IProblem originalProblem,
			Severity maxSeverity,
			Object origin) {
		this.originalProblem = originalProblem;
		this.severity = originalProblem.getSeverity().isAtLeastAsSevereAs(maxSeverity)
				? maxSeverity
				: originalProblem.getSeverity();
		this.origin = origin;
	}

	/**
	 * Wraps an IProblem consumer, so that the severity of forwarded problems is reduced to WARNING.
	 */
	public static Consumer<IProblem> forwardAsNonError(Consumer<? super IProblem> originalConsumer) {
		return forwardLimitSeverity(originalConsumer, Severity.WARNING);
	}

	/**
	 * Wraps an IProblem consumer, so that the severity of forwarded problems is reduced to maxSeverity.
	 */
	public static Consumer<IProblem> forwardLimitSeverity(Consumer<? super IProblem> originalConsumer, Severity maxSeverity) {
		return p -> originalConsumer.accept(new WrappedProblemWithSeverityLimitAndOrigin(p, maxSeverity, p.getSource()));
	}

	public IProblem unwrap() {
		return originalProblem;
	}

	@Override public Severity getSeverity() {
		return this.severity;
	}

	@Override public String getMessage() {
		return originalProblem.getMessage();
	}

	@Override public Date getDate() {
		return originalProblem.getDate();
	}

	@Override public Object getSource() {
		return origin;
	}

	@Override public int getLine() {
		return originalProblem.getLine();
	}

	@Override public int getSourceStart() {
		return originalProblem.getSourceStart();
	}

	@Override public int getSourceEnd() {
		return originalProblem.getSourceEnd();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + getSeverity() + ": " + getMessage();
	}
}
