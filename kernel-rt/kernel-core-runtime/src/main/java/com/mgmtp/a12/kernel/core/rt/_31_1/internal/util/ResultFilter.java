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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICause;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;

public class ResultFilter implements IResult {

	public interface IResultFilter {
		boolean pass(String ruleName, String errorCode, IIdentifier errorField, String hintMessage, ErrorType errorType);
	}

	private final IResult delegator;
	private final IResultFilter filter;

	public ResultFilter(IResult delegator, IResultFilter filter) {
		this.delegator = delegator;
		this.filter = filter;
	}

	@Override public void addMessageV2(String ruleName, String errorCode, String messageStr, ErrorType type, SeverityType severity,
			ICause possiblyCausingEntities) {
		IIdentifier errorField = possiblyCausingEntities.getIterationEntity();
		if (filter.pass(ruleName, errorCode, errorField, messageStr, type)) {
			delegator.addMessageV2(ruleName, errorCode, messageStr, type, severity, possiblyCausingEntities);
		}
	}

	@Override
	public void addInfo(String ruleName, String infoCode, IIdentifier infoField, String infoMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		if (filter.pass(ruleName, infoCode, infoField, infoMessage, type)) {
			delegator.addInfo(ruleName, infoCode, infoField, infoMessage, type, referencedFields, refOmissionErrorResponsible,
					unknowns);
		}
	}

	@Override
	public void addHint(String ruleName, String hintCode, IIdentifier hintField, String hintMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		if (filter.pass(ruleName, hintCode, hintField, hintMessage, type)) {
			delegator.addHint(ruleName, hintCode, hintField, hintMessage, type, referencedFields, refOmissionErrorResponsible,
					unknowns);
		}
	}

	@Override
	public void addError(String ruleName, String errorCode, IIdentifier errorField, String errorMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		if (filter.pass(ruleName, errorCode, errorField, errorMessage, type)) {
			delegator.addError(ruleName, errorCode, errorField, errorMessage, type, referencedFields, refOmissionErrorResponsible,
					unknowns);
		}
	}

	@Override
	public int getCountOf(ErrorType type) {
		return delegator.getCountOf(type);
	}

	@Override public int getCount(ErrorType type, SeverityType severityThreshold) {
		return delegator.getCount(type, severityThreshold);
	}
}
