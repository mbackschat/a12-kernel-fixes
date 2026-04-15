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

package com.mgmtp.a12.kernel.md.rt.internal.service;

import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Message2RankedNotification implements RankedNotification {
	@NonNull private final IMessage message;

	public static Message2RankedNotification of(@NonNull IMessage message) {
		return new Message2RankedNotification(message);
	}

	@Override public String getMessage() {
		return message.getErrorText() + " [" + message.getErrorCode() + "]";
	}

	@Override public Severity getSeverity() {
		return mapSeverity(message.getSeverity());
	}

	@Override public Object getSource() {
		return message.getErrorFieldPointer().toDocumentPointer().orElse(null);
	}

	@Override public String toString() {
		return "Message2RankedNotification{" +
				"message='" + getMessage() + '\'' +
				", severity=" + getSeverity() +
				", source=" + getSource() +
				'}';
	}

	public static Severity mapSeverity(IMessage.Severity severity) {
		return switch (severity) {
			case ERROR -> Severity.ERROR;
			case WARNING -> Severity.WARNING;
			case INFO -> Severity.INFO;
		};
	}
}
