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

import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentUniquenessCriterionWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.ElementWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.FieldTypeDefinitionWrapper;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;

public class ConsistencyCheckUtils {

	public static @NonNull Consumer<RankedNotification> problemReporterToNotificationReceiver(@NonNull IProblemReporter pr) {
		return n -> pr.reportProblem(new MVKCoreError(
				n.getMessage(),
				n.getSeverity(),
				unwrapConsistencyProblemSource(n.getSource())));
	}

	public static Object unwrapConsistencyProblemSource(Object source) {
		// Note: When adding types to this, also update the JavaDoc in IDocumentModelService#checkConsistency and DocumentModelService#checkConsistency
		return switch (source) {
			case DocumentModelWrapper documentModelWrapper -> documentModelWrapper.getDocumentModel();
			case ElementWrapper elementWrapper -> elementWrapper.getElement();
			case FieldTypeDefinitionWrapper fieldTypeDefinitionWrapper -> fieldTypeDefinitionWrapper.unwrap();
			case DocumentUniquenessCriterionWrapper wrapper -> wrapper.unwrap();
			case null -> null;
			default -> {
				assert false : "Expected problem sources to always be of certain types, but got: " + source.getClass().getName();
				yield source;
			}
		};
	}

	private ConsistencyCheckUtils() {
		throw new UnsupportedOperationException();
	}
}
