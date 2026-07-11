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

public class PresentationInformationValues {

	private final String dezimalTrenner;
	private final String datumTrenner;
	private final String datumReihenfolge;
	private final String datumNullOptional;
	private final String datumbereichTrenner;
	private final String dateTimeFormat;
	private final String timeFormat;
	private final String thousandsSeparator;

	public PresentationInformationValues(String dezimalTrenner, String thousandsSeparator, String datumTrenner,
			String datumReihenfolge, String datumNullOptional, String datumbereichTrenner, String dateTimeFormat,
			String timeFormat) {
		this.dezimalTrenner = dezimalTrenner;
		this.thousandsSeparator = thousandsSeparator;
		this.datumTrenner = datumTrenner;
		this.datumReihenfolge = datumReihenfolge;
		this.datumNullOptional = datumNullOptional;
		this.datumbereichTrenner = datumbereichTrenner;
		this.dateTimeFormat = dateTimeFormat;
		this.timeFormat = timeFormat;
	}

	public String getDezimalTrenner() {
		return dezimalTrenner;
	}

	public String getDatumTrenner() {
		return datumTrenner;
	}

	public String getDatumReihenfolge() {
		return datumReihenfolge;
	}

	public String isDatumNullOptional() {
		return datumNullOptional;
	}

	public String getDatumbereichTrenner() {
		return datumbereichTrenner;
	}

	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public String getThousandsSeparator() {
		return thousandsSeparator;
	}
}
