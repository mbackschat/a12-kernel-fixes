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

import java.util.Date;
import java.util.Objects;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.model.notification.Severity;

/**
 * Parent Exception to handle all errors in the MVK core that may occur during checks.
 * <p>
 * The parser specific methods in {@link IProblem} concerning the location of the error in the
 * condition text all return the default value 0.
 */
public class MVKCoreError extends IllegalArgumentException implements IProblem {

	private static final long serialVersionUID = -2557288588425886592L;

	protected final Severity severity;
	protected final Date date;
	protected final Object origin;

	/**
	 * A convenience constructor for the default severity {@link Severity#ERROR}.
	 *
	 * @param message
	 * 		the error message that should be displayed to the user.
	 * @param origin
	 * 		the origin of the error.
	 */
	public MVKCoreError(String message, Object origin) {
		this(message, Severity.ERROR, origin);
	}

	/**
	 * @param message
	 * 		the error message that should be displayed to the user.
	 * @param severity
	 * 		the severity of the error (see {@link Severity}).
	 * @param origin
	 * 		the origin of the error.
	 */
	public MVKCoreError(String message, Severity severity, Object origin) {
		super(message);
		this.severity = severity;
		this.origin = origin;
		this.date = new Date();
	}

	/**
	 * @param message
	 * 		the error message that should be displayed to the user.
	 * @param severity
	 * 		the severity of the error (see {@link Severity}).
	 * @param origin
	 * 		the origin of the error.
	 * @param cause
	 * 		the Throwable (e.g. exception that caused the error, if applicable.
	 */
	public MVKCoreError(String message, Severity severity, Object origin, Throwable cause) {
		super(message, cause);
		this.severity = severity;
		this.origin = origin;
		this.date = new Date();
	}

	/**
	 * Copy constructor.
	 *
	 * @param sourceProblem
	 * 		the original problem to be copied.
	 */
	protected MVKCoreError(IProblem sourceProblem) {
		super(sourceProblem.getMessage());
		this.severity = sourceProblem.getSeverity();
		this.origin = sourceProblem.getSource();
		this.date = sourceProblem.getDate();
	}

	@Override
	public Severity getSeverity() {
		return severity;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public Object getSource() {
		return origin;
	}

	@Override
	public int getLine() {
		return -1;
	}

	@Override
	public int getSourceStart() {
		return -1;
	}

	@Override
	public int getSourceEnd() {
		return -1;
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		MVKCoreError that = (MVKCoreError) o;
		return Objects.equals(getMessage(), that.getMessage())
				&& getSeverity() == that.getSeverity()
				// && Objects.equals(getDate().toInstant(), that.getDate().toInstant()) // don't compare date - otherwise
				// identical problems that are created twice at different times should be considered the same
				&& Objects.equals(getSource(), that.getSource())
				&& getSourceStart() == that.getSourceStart()
				&& getSourceEnd() == that.getSourceEnd()
				&& getLine() == that.getLine()
				&& getLineEnd() == that.getLineEnd();
	}

	@Override public int hashCode() {
		return Objects.hash(getMessage(), getSeverity(),
				//getDate().toInstant(), // don't compare date - otherwise identical problems that are created twice at different
				// times should be considered the same
				// getOrigin(), // in hashCode, we also skip origin - this should make debugging easier if the origin object does
				// not implement hashCode/equals, and will probably not lead to many additional hash collisions.
				getSourceStart(), getSourceEnd(), getLine(), getLineEnd());
	}
}