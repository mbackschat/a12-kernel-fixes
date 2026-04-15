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

package com.mgmtp.a12.kernel.core.utils.a12internal.calculation;

import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.utils.internal.metaModel.DataImpl;

@OnlyForUsage
public class RuntimeCalculationUtils {
	private RuntimeCalculationUtils() {
		throw new UnsupportedOperationException("Static UtilityClass cannot be instantiated!");
	}

	/**
	 * Applies the result of a calculation (an {@link IData}-object returned by
	 * {@link ICalculator#calculateAll(IData, ValidationMode, IResult)} to another {@link IData}-object preferably the original
	 * data and returns a new {@link IData}-object. A specialty is here, that for removed fields (previously calculated fields that
	 * are not calculated anymore) will be removed from the returned {@link IData}-object, the empty string will be added to the
	 * data. And also fields that were calculated empty will be removed.
	 *
	 * @param calculationResult
	 * 		the data of the calculation result as returned from {@link ICalculator#calculateAll(IData, ValidationMode, IResult)}
	 * @param originalData
	 * 		the data the calculation result should be applied to.
	 * @deprecated Use {@link com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculationResult#apply(IData)} instead.
	 */
	@Deprecated(since = "30.6.0")
	public static IData apply(IData calculationResult, IData originalData) {
		IData result = new DataImpl();
		originalData.forEach(id -> result.addValue(id, originalData.getValue(id)));
		applyTo(calculationResult, result);
		return result;
	}

	/**
	 * Applies the result of a calculation (an {@link IData}-object returned by
	 * {@link ICalculator#calculateAll(IData, ValidationMode, IResult)} to another {@link IData}-object preferably the original
	 * data, whose {@link IData#addValue(IIdentifier, String)}
	 * method works correctly. A specialty is here, that for removed fields (previously calculated fields that are not calculated
	 * anymore), the empty string will be added to the data. There may be also cases, where an empty value is calculated
	 * on a field, in which case the field should be counted as removed as well.
	 *
	 * @param calculationResult
	 * 		the data of the calculation result as returned from {@link ICalculator#calculateAll(IData, ValidationMode, IResult)}
	 * @param originalData
	 * 		the data the calculation result should be applied to.
	 * @deprecated Use {@link com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculationResult#apply(IData)} instead.
	 */
	@Deprecated(since = "30.6.0")
	public static void applyTo(IData calculationResult, IData originalData) {
		calculationResult.forEach(id -> originalData.addValue(id, calculationResult.getValue(id)));
	}
}
