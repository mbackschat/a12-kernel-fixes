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

package com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages;

import java.util.Locale;

import com.mgmtp.a12.model.notification.Severity;

public class DataTransferCodegenException extends RuntimeException {
	private final LocaleIndependentNotification notification;
	private final Object triggerOrCause; // stored here just for debugging

	public DataTransferCodegenException(final DataTransferErrorEnum errorType, Object... errorMessageArgs) {
		this(null, errorType, errorMessageArgs);
	}

	/**
	 * @param triggerOrCause
	 * 		can be set if this exception was caused by another throwable or some problem message. If it is a
	 * 		throwable, it will also be set as the {@link Throwable#getCause()} of this exception.
	 */
	public DataTransferCodegenException(Object triggerOrCause, DataTransferErrorEnum errorType, Object... errorMessageArgs) {
		super(triggerOrCause instanceof Throwable cause ? cause : null);
		this.triggerOrCause = triggerOrCause;
		// assume that severity is ERROR if an exception occurred
		this.notification = LocaleIndependentNotification.of(Severity.ERROR, errorType, errorMessageArgs);
	}

	public DataTransferErrorEnum getErrorType() {
		return this.notification.getNotificationCode();
	}

	public Object[] getErrorMessageArgs() {
		return this.notification.getNotificationMessageArgs();
	}

	public String format(Locale locale) {
		return this.notification.format(locale);
	}

	@Override public String toString() {
		return format(Locale.US);
	}

	public LocaleIndependentNotification getNotification() {
		return notification;
	}
}
