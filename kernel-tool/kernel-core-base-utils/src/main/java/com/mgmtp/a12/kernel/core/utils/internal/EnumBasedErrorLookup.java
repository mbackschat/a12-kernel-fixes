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

import java.util.Locale;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.notification.Severity;

/**
 * The base class for lookups of error messages using an enum.
 * <p>
 * The deriving classes have to provide the specific enum to use.
 *
 * @param <T>
 * 		the enum to find the error message with
 */
public abstract class EnumBasedErrorLookup<T extends Enum<T>> extends MVKErrorLookup {

	/**
	 * @param locale
	 * 		the locale used for the lookup of error messages that may occur during checking
	 * 		the calculation.
	 */
	public EnumBasedErrorLookup(Locale locale) {
		super(locale);
	}

	/**
	 * @param errorType
	 * 		the error constant.
	 * @param args
	 * 		the strings for replacing the placeholders in the message.
	 * @return the error message for the specified error in the language defined for the instance
	 * 		with placeholders replaced.
	 */
	public String getErrorMessage(T errorType, Object... args) {
		return this.getErrorMessage(errorType.name(), args);
	}

	/**
	 * Returns the bare error message for the given errorKey and locale. The error key is NOT
	 * appended to the end of the message. See
	 * {@link MVKErrorLookup#getBareErrorMessage(String, String, Locale, Object...)}}.
	 *
	 * @param errorType
	 * 		the error constant.
	 * @param args
	 * 		the strings for replacing the placeholders in the message.
	 * @return the error message for the specified error in the language defined for the instance
	 * 		with placeholders replaced.
	 */
	public String getBareErrorMessage(T errorType, Object... args) {
		return super.getBareErrorMessage(errorType.name(), args);
	}

	/**
	 * Reports the problem using {@link IProblemReporter} and {@link MVKCoreError}.
	 *
	 * @param pr
	 * 		the problem reporter to report to
	 * @param severity
	 * 		the severity of the problem
	 * @param origin
	 * 		any object that is the reason for the problem
	 * @param errorType
	 * 		the error constant.
	 * @param args
	 * 		the strings for replacing the placeholders in the message.     *
	 */
	public void report(IProblemReporter pr, Severity severity, Object origin, T errorType, Object... args) {
		report(pr, severity, origin, null, errorType, args);
	}

	public void report(IProblemReporter pr, Severity severity, Object origin, Throwable cause, T errorType, Object... args) {
		String errorMsg = getErrorMessage(errorType, args);
		MVKCoreError problem = cause == null
				? new MVKCoreError(errorMsg, severity, origin)
				: new MVKCoreError(errorMsg, severity, origin, cause);
		pr.reportProblem(problem);
	}

	/**
	 * Reports the problem using {@link IProblemReporter} and {@link MVKCoreError}.
	 * With severity {@link Severity#ERROR}
	 *
	 * @param pr
	 * 		the problem reporter to report to
	 * @param origin
	 * 		any object that is the reason for the problem
	 * @param errorType
	 * 		the error constant.
	 * @param args
	 * 		the strings for replacing the placeholders in the message.     *
	 */
	public void reportError(IProblemReporter pr, Object origin, T errorType, Object... args) {
		report(pr, Severity.ERROR, origin, errorType, args);
	}

	/**
	 * see {@link #reportError(IProblemReporter, Object, Enum, Object...)}
	 */
	public void reportError(Consumer<? super IProblem> pr, Object origin, T errorType, Object... args) {
		report(pr::accept, Severity.ERROR, origin, errorType, args);
	}

	public void reportError(Consumer<? super IProblem> pr, Object origin, Throwable cause, T errorType, Object... args) {
		report(pr::accept, Severity.ERROR, origin, cause, errorType, args);
	}
}