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

package com.mgmtp.a12.kernel.core.codegen.internal.validation;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ResultBaseImpl;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ResultMessageV1;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;
import com.mgmtp.a12.model.notification.Severity;

/**
 * An instance of an implementation of <code>AbstractProblemReporterValidationResult</code> converts
 * all incoming hints and errors provided by the validation to {@link IProblem}s, that are reported
 * to a {@link IProblemReporter}.
 * {@link AbstractProblemReporterValidationResult#retrieveErrorOrigin(IIdentifier)}
 */
public abstract class AbstractProblemReporterValidationResult extends ResultBaseImpl implements IResult {

	private final IProblemReporter problemReporter;

	protected AbstractProblemReporterValidationResult(IProblemReporter pr) {
		this.problemReporter = pr;
	}

	@Override protected void messageV1Hook(ResultMessageV1 messageV1) {
		String message = messageV1.messageStr();
		if (!IResult.FORMAL_VALIDATION_RULE_NAME.equals(messageV1.ruleName())) {
			message = prepareMessage(message, messageV1.errorCode());
		}
		Severity mdSeverity = switch (messageV1.severity()) {
			case INFO -> Severity.INFO;
			case HINT -> Severity.WARNING;
			case ERROR -> Severity.ERROR;
		};
		Object infoSource = retrieveErrorOrigin(messageV1.field());
		this.problemReporter.reportProblem(new MVKCoreError(message, mdSeverity, infoSource));
	}

	protected abstract Object retrieveErrorOrigin(IIdentifier errorEntity);

	private String prepareMessage(String errorMessage, String errorCode) {
		return String.format("%s [%s]", errorMessage, errorCode);
	}
}
