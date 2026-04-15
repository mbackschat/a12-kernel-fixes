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
package com.mgmtp.a12.kernel.md.model.internal.services.join.error;

import java.util.Calendar;
import java.util.Date;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.model.notification.Severity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Container for document model joining related problems.
 */
@Getter
@Builder
@AllArgsConstructor
public class JoinProblem implements IProblem {

	private Severity severity;
	private Object source;
	private String message;
	private Date date;

	public static JoinProblem createError(Object source, String message) {
		return JoinProblem.builder()
				.severity(Severity.ERROR)
				.message(message)
				.source(source)
				.date(Calendar.getInstance().getTime())
				.build();
	}

	public static JoinProblem createWarning(Object source, String message) {
		return JoinProblem.builder()
				.severity(Severity.WARNING)
				.message(message)
				.source(source)
				.date(Calendar.getInstance().getTime())
				.build();
	}

	@Override
	public int getLine() {
		return -1;
	}

	@Override
	public int getSourceStart() {
		return -1;
	}

	@Override
	public int getSourceEnd() {
		return -1;
	}

	@Override public String toString() {
		return "JoinProblem.createError(" + this.source + ", " + this.message + ")";
	}
}
