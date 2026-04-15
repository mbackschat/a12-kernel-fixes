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

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Represents a message that gets submitted to {@link IResult#addError}
 * (or addHint, addInfo) or {@link IResult#addMessageV2}
 * <p>
 * Does not include the referenced fields and the errorField, and other attributes that differ
 * between V1 and V2 methods (if that info is needed, see {@link ResultMessageV1} and {@link ResultMessageV2})
 * <p>
 * Only the kernel-provided implementation of this interface should be used (available via {@link #of})
 */
@OnlyForUsage
public interface ResultMessageCommon {
	String ruleName();

	/**
	 * corresponds to the {@code errorCode} parameter in {@link IResult#addMessageV2} and to
	 * {@code infoCode}/{@code hintCode}/{@code errorCode} in
	 * {@link IResult#addInfo}/{@link IResult#addHint}/{@link IResult#addError}
	 */
	String errorCode();

	/**
	 * corresponds to the {@code messageStr} parameter in {@link IResult#addMessageV2}
	 * and to {@code infoMessage}/{@code hintMessage}/{@code errorMessage} in
	 * {@link IResult#addInfo}/{@link IResult#addHint}/{@link IResult#addError}
	 */
	String messageStr();

	IResult.ErrorType type();

	SeverityType severity();

	static ResultMessageCommon of(
			String ruleName,
			String errorCode,
			String messageStr,
			IResult.ErrorType type,
			SeverityType severity
	) {
		return new ResultMessageCommonImpl(
				ruleName,
				errorCode,
				messageStr,
				type,
				severity
		);
	}
}