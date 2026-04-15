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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService.ICalculationRule;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

public class RankedNotificationUtils {

	private RankedNotificationUtils() {
		throw new UnsupportedOperationException("This is an utility class and must not be initialized");
	}

	public static String joinMessages(Collection<? extends RankedNotification> notifications) {
		return notifications.stream()
				.map(RankedNotification::getMessage)
				.collect(Collectors.joining(System.lineSeparator()));
	}

	public static boolean hasErrors(Collection<? extends RankedNotification> notifications) {
		return notifications.stream()
				.anyMatch(m -> m.getSeverity() == Severity.ERROR);
	}

	public static List<String> getErrorMessages(Collection<? extends RankedNotification> notifications) {
		return notifications.stream()
				.filter(m -> m.getSeverity() == Severity.ERROR)
				.map(RankedNotification::getMessage)
				.collect(Collectors.toList());
	}

	public static List<String> getMessages(Collection<? extends RankedNotification> notifications) {
		return notifications.stream()
				.map(RankedNotification::getMessage)
				.collect(Collectors.toList());
	}

	public static String joinErrorMessages(Collection<? extends RankedNotification> notifications) {
		return joinMessages(notifications, Severity.ERROR);
	}

	public static String joinWarningMessages(Collection<? extends RankedNotification> notifications) {
		return joinMessages(notifications, Severity.WARNING);
	}

	public static String joinInfoMessages(Collection<? extends RankedNotification> notifications) {
		return joinMessages(notifications, Severity.INFO);
	}

	private static String joinMessages(Collection<? extends RankedNotification> notifications, Severity severity) {
		return notifications.stream()
				.filter(m -> m.getSeverity() == severity)
				.map(m -> convertSourceToString(m) + m.getMessage())
				.collect(Collectors.joining(System.lineSeparator()));
	}

	public static String convertSourceToString(RankedNotification notification) {
		Object source = notification.getSource();
		if (source == null) {
			return "";
		}
		if (source instanceof IField field) {
			return String.format("Field with path '%s': ", field.getFullName());
		} else if (source instanceof IGroup group) {
			return String.format("Group with path '%s': ", group.getFullName());
		} else if (source instanceof IRule rule) {
			return String.format("Rule with path '%s': ", rule.getFullName());
		} else if (source instanceof ICalculationRule calculationRule) {
			return String.format("Computation with path '%s': ", calculationRule.getRule().getFullName());
		} else {
			// this should not happen
			throw new IllegalArgumentException("The java type '" + source.getClass().getName() + "' is not supported.");
		}
	}

}
