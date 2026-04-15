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

package com.mgmtp.a12.kernel.md.model.internal.impl;

import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString
public class RankedNotificationImpl implements RankedNotification {

	private final String message;
	private final Severity severity;
	private final Object source;

	public static @NonNull RankedNotificationImpl of(String message, Severity severity, Object source) {
		return new RankedNotificationImpl(message, severity, source);
	}

	public static @NonNull RankedNotificationImpl error(String message) {
		return error(message, null);
	}

	public static @NonNull RankedNotificationImpl error(String message, Object source) {
		return of(message, Severity.ERROR, source);
	}

	public static @NonNull RankedNotificationImpl warning(String message) {
		return warning(message, null);
	}

	public static @NonNull RankedNotificationImpl warning(String message, Object source) {
		return of(message, Severity.WARNING, source);
	}

	public static @NonNull RankedNotificationImpl info(String message) {
		return info(message, null);
	}

	public static @NonNull RankedNotificationImpl info(String message, Object source) {
		return of(message, Severity.INFO, source);
	}
}
