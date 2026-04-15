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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.core;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.IFormatDefinition;

/**
 * Combiner for multiple types of data types.
 * Used in {@link RuntimeController} to implement EntityListOperations and ListOperOps.
 */
interface Combiner<I, O> {
	/**
	 * @return the value, that should be returned if the given field is not relevant.
	 */
	O getNonRelevantValue();

	/**
	 * Get the final value of the calculation after all field values have been supplied to
	 * {@link #addValue(Object, IFormatDefinition, boolean)}
	 *
	 * @param containsFilter
	 * 		specifies, whether there was a filter condition in one of the entities.
	 * @return
	 */
	O getValue(boolean containsFilter);

	/**
	 * Add a value to the calculation.
	 *
	 * @param value
	 * 		the value to include. May be an empty value.
	 * @param fd
	 * 		the type of the field.
	 * @param testValidity
	 * 		specifies, whether irrelevant or not given values should be omitted. The
	 * 		distinction is for the distinction between EntityListOperations and ListOperOps.
	 * 		The latter take all values into account.
	 * 		<p>
	 * 		testValidity is ignored in DateCombiner (only used in NumberCombiner), it
	 * 		seems to be a workaround for the default == 0 behaviour of VkBigDecimal and may
	 * 		cause some Auslassugsfehler to be reported as Wertfehler.
	 */
	void addValue(I value, IFormatDefinition fd, boolean testValidity);

	/**
	 * Is called if some trailing empty repetitions are optimized away
	 *
	 * @param fd
	 * 		the type of the field.
	 */
	void setOmittedTail(IFormatDefinition fd);

	/**
	 * Returns true if the value can change by subsequent addValue or setOmittedTail calls.
	 */
	default boolean canChange() {
		return true;
	}
}
