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

package com.mgmtp.a12.kernel.core.rt.a12internal.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * An implementation of IResult that is available to be used directly or extended by custom implementations.
 * {@link #messageV1Hook(ResultMessageV1)} and {@link #messageV2Hook(ResultMessageV2)}) are intended to be overridden by subclasses,
 * if needed.
 */
@OnlyForUsage
public class ResultBaseImpl implements IResult {
	private final List<ResultMessageV1> v1Messages = new ArrayList<>();
	private final List<ResultMessageV2> v2Messages = new ArrayList<>();

	/**
	 * subclasses can override this method in order to get notified when {@link #addInfo}/{@link #addHint}/{@link #addError} got
	 * called
	 */
	protected void messageV1Hook(ResultMessageV1 messageV1) {
		// (subclasses may do something here)
	}

	/**
	 * subclasses can override this method in order to get notified when {@link #addMessageV2} got called
	 */
	protected void messageV2Hook(ResultMessageV2 messageV2) {
		// (subclasses may do something here)
	}

	/**
	 * The messages that have been reported to this IResult via {@link #addError}, {@link #addHint} or {@link #addInfo}.
	 */
	public List<ResultMessageV1> v1Messages() {
		return Collections.unmodifiableList(v1Messages);
	}

	/**
	 * The messages that have been reported to this IResult via {@link #addMessageV2}.
	 */
	public List<ResultMessageV2> v2Messages() {
		return Collections.unmodifiableList(v2Messages);
	}

	@Override public void addMessageV2(String ruleName, String errorCode, String messageStr, ErrorType type, SeverityType severity,
			ICause possiblyCausingEntities) {
		ResultMessageV2 message = ResultMessageV2.of(ruleName, errorCode, messageStr, type, severity, possiblyCausingEntities);
		v2Messages.add(message);
		messageV2Hook(message);
	}

	@Override public void addInfo(String ruleName, String infoCode, IIdentifier infoField, String infoMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		ResultMessageV1 message = ResultMessageV1.of(ruleName, infoCode, infoField, infoMessage, type, SeverityType.INFO,
				referencedFields, refOmissionErrorResponsible, unknowns);
		v1Messages.add(message);
		messageV1Hook(message);
	}

	@Override public void addHint(String ruleName, String hintCode, IIdentifier hintField, String hintMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		ResultMessageV1 message = ResultMessageV1.of(ruleName, hintCode, hintField, hintMessage, type, SeverityType.HINT,
				referencedFields, refOmissionErrorResponsible, unknowns);
		v1Messages.add(message);
		messageV1Hook(message);
	}

	@Override public void addError(String ruleName, String errorCode, IIdentifier errorField, String errorMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		ResultMessageV1 message = ResultMessageV1.of(ruleName, errorCode, errorField, errorMessage, type, SeverityType.ERROR,
				referencedFields, refOmissionErrorResponsible, unknowns);
		v1Messages.add(message);
		messageV1Hook(message);
	}

	@Override public int getCountOf(ErrorType type) {
		return (int) v1Messages.stream()
				.filter(m -> type == null || m.type() == null || type == m.type())
				.count();
	}

	@Override public int getCount(ErrorType type, SeverityType severityThreshold) {
		return (int) v1Messages.stream()
				.filter(m -> type == null || m.type() == null || type == m.type())
				.filter(m -> m.severity().isMoreSevereThanOrEqual(severityThreshold))
				.count();
	}
}
