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
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemUtils;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

/**
 * Corresponding class to {@link ProblemUtils}, but for document models.
 */
public class DMProblemUtils {

	private DMProblemUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String join(Collection<? extends RankedNotification> notifications, Severity minSeverity) {
		return notifications.stream()
				.filter(m -> m.getSeverity().isAtLeastAsSevereAs(minSeverity))
				.map(n -> convertSourceToString(n) + n.getMessage())
				.collect(Collectors.joining(System.lineSeparator()));
	}

	public static String convertSourceToString(RankedNotification notification) {
		return switch (notification.getSource()) {
			case null -> "";
			case IField field -> String.format("Field with path '%s': ", field.getFullName());
			case IGroup group -> String.format("Group with path '%s': ", group.getFullName());
			case IRule rule -> String.format("Rule with path '%s': ", rule.getFullName());
			case IBaseCalculationService.ICalculationRule calculationRule ->
					String.format("Computation with path '%s': ", calculationRule.getRule().getFullName());
			// The following can occur as notification sources during conversion in ValidationModelConverter
			case com.mgmtp.a12.kernel.md.model.api.IField field ->
					String.format("Field with path '%s': ", ElementUtils.getPath(field));
			case com.mgmtp.a12.kernel.md.model.api.IGroup group ->
					String.format("Group with path '%s': ", ElementUtils.getPath(group));
			case com.mgmtp.a12.kernel.md.model.api.IRule rule -> String.format("Rule with path '%s': ", ElementUtils.getPath(rule));
			case com.mgmtp.a12.kernel.md.model.api.IComputation computation ->
					String.format("Computation with path '%s': ", ElementUtils.getPath(computation));
			default -> throw new IllegalArgumentException(
					"The java type '" + notification.getSource().getClass().getName() + "' is not supported.");
		};
	}
}
