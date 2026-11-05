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

import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ErrorCause;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICause;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;

/**
 * While converting a value for a field instance the information about the specific instance is
 * lost and the corresponding errors after converting cannot be assigned to the right field
 * instance. This class wraps the {@link IResult} instance given for the {@code this} instance
 * and stores the information about the specific field instance for which the value should be
 * converted.
 * The result of a format validation before conversion.
 */
public class ResultForConversion implements IResult {

	IResult result;
	IIdentifier id;

	public ResultForConversion(IResult result, IIdentifier id) {
		this.result = result;
		this.id = id;
	}

	@Override
	public void addInfo(String ruleName, String infoCode, IIdentifier infoField, String infoMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		result.addInfo(ruleName, infoCode, id, infoMessage, type, Set.of(id), refOmissionErrorResponsible, unknowns);
	}

	@Override
	public void addHint(String ruleName, String hintCode, IIdentifier hintField, String hintMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		result.addHint(ruleName, hintCode, id, hintMessage, type, Set.of(id), refOmissionErrorResponsible, unknowns);
	}

	@Override
	public void addError(String ruleName, String errorCode, IIdentifier errorField, String errorMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		result.addError(ruleName, errorCode, id, errorMessage, type, Set.of(id), refOmissionErrorResponsible, unknowns);
	}

	@Override
	public void addMessageV2(String ruleName, String errorCode, String messageStr, ErrorType type, SeverityType severity,
			ICause possiblyCausingEntities) {
		result.addMessageV2(ruleName, errorCode, messageStr, type, severity, ErrorCause.createSingleFieldCause(id));
	}

	@Override
	public int getCountOf(ErrorType type) {
		return result.getCountOf(type);
	}

	@Override
	public int getCount(SeverityType severityThreshold) {
		return result.getCount(severityThreshold);
	}

	@Override public int getCount(ErrorType type, SeverityType severityThreshold) {
		return result.getCount(type, severityThreshold);
	}

}
