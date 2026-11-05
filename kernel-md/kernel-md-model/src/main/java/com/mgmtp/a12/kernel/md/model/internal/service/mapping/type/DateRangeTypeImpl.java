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

package com.mgmtp.a12.kernel.md.model.internal.service.mapping.type;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateRangeType;

class DateRangeTypeImpl implements IDateRangeType {

	private String formatOfDate;
	private String separator;
	private InterpretationOfYear interpretationOfYear;

	public DateRangeTypeImpl(String formatOfDate, String separator,
			com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType.InterpretationOfYear intOfYr) {
		this.formatOfDate = formatOfDate;
		this.separator = separator;
		this.interpretationOfYear = getCoreInterpretationOfYear(intOfYr);
	}

	@Override
	public String getFormatOfDate() {
		return formatOfDate;
	}

	@Override
	public String getSeparator() {
		return separator;
	}

	@Override
	public InterpretationOfYear getInterpretationOfYear() {
		return interpretationOfYear;
	}

	private InterpretationOfYear getCoreInterpretationOfYear(
			com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType.InterpretationOfYear intOfYr) {
		if (intOfYr == null) {
			return IDateRangeType.InterpretationOfYear.STANDARD;
		}
		switch (intOfYr) {
			case FROM:
				return IDateRangeType.InterpretationOfYear.SUBSEQUENT;
			case TO:
				return IDateRangeType.InterpretationOfYear.PREVIOUS;
			default:
				throw new IllegalArgumentException(String.format("Unknown interpretation of year in date range: %s", intOfYr));
		}
	}

	/**
	 * @deprecated isAdditionalCheck is no longer evaluated and its value is therefore irrelevant. This attribute will be
	 * removed with A12K-4439.
	 */
	@Deprecated(since = "31.2.0")
	@Override
	public boolean isAdditionalCheck() {
		return false;
	}
}
