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

import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * The result of a calculation done by {@link ICalculator#calculateAll(IData, ValidationMode, IResult)}. The result can be used as
 * an {@link IData} as before the introduction but can also be applied to another {@link IData} object via the
 * {@link #apply(IData)} method to create a third {@link IData} object containing all data from the original data and the data from
 * this calculation result.
 * <p>
 * We expect this interface to be only implemented by kernel.
 */
@OnlyForUsage
public interface ICalculationResult extends IData {
	/**
	 * Applies the result to a {@link IData}-object preferably the original data, whose {@link IData#addValue(IIdentifier, String)}
	 * method works correctly. A specialty is here, that for removed fields (previously calculated fields that are not calculated
	 * anymore), the value <code>null</code> will be added to the data. There may be also cases, where an empty value is calculated
	 * on a field, in which case the field should be counted as removed as well.
	 *
	 * @param originalData
	 * 		the data the calculation result should be applied to.
	 */
	IData apply(IData originalData);
}
