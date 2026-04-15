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
package com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

/**
 * Provides validation of decimal place specification for {@link NumberDataType}s. The VK does only
 * allow a certain number of digits and decimal places that this class.
 */
public class NumberLimits {

	public static final int MVK_MAX_DIGITS = 15;

	public static double getMaxAllowedValue(final int maxDecimalPlaces) {
		return Math.pow(10, (MVK_MAX_DIGITS - maxDecimalPlaces)) - Math.pow(10, (-maxDecimalPlaces));
	}

	private NumberLimits() {
		throw new IllegalStateException("Utility class");
	}

	public static String printValue(final double value, final int maxDecimalPlaces, final char decimalSeparator) {
		final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		df.setMaximumFractionDigits(maxDecimalPlaces);
		df.setMaximumIntegerDigits(MVK_MAX_DIGITS - maxDecimalPlaces);
		df.setRoundingMode(RoundingMode.DOWN);
		df.setGroupingUsed(false);
		final DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator(decimalSeparator);
		df.setDecimalFormatSymbols(dfs);
		return df.format(value);
	}
}
