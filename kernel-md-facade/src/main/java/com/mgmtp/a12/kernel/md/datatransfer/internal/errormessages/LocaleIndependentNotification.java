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

/**
 * Helper class to let the codegen-related code report warnings without caring about the locale in which the
 * warnings will ultimately be communicated to the end-user.
 * Note that this does not provide full localization like the localizables in typescript, because
 * the message parameters are not themselves localizables (also, no customization of messages in supported).
 * <p/>
 * This class should become redundant once A12 localization exists in java.
 */
public class LocaleIndependentNotification {
	private final Severity severity;
	private final DataTransferErrorEnum notificationCode;
	private final Object[] notificationMessageArgs;

	private LocaleIndependentNotification(Severity severity, DataTransferErrorEnum notificationCode,
			Object[] notificationMessageArgs) {
		this.severity = severity;
		this.notificationCode = notificationCode;
		this.notificationMessageArgs = notificationMessageArgs;
	}

	public static LocaleIndependentNotification of(Severity severity, DataTransferErrorEnum notificationCode,
			Object... notificationMessageArgs) {
		return new LocaleIndependentNotification(severity, notificationCode, notificationMessageArgs);
	}

	public static LocaleIndependentNotification warning(DataTransferErrorEnum notificationCode, Object... notificationMessageArgs) {
		return of(Severity.WARNING, notificationCode, notificationMessageArgs);
	}

	public DataTransferErrorEnum getNotificationCode() {
		return notificationCode;
	}

	public Severity getSeverity() {
		return severity;
	}

	public Object[] getNotificationMessageArgs() {
		return notificationMessageArgs;
	}

	public String format(Locale locale) {
		return new DataTransferCodegenErrorLookup(locale).getErrorMessage(this.notificationCode, this.notificationMessageArgs);
	}

	@Override public String toString() {
		return format(Locale.US);
	}
}
