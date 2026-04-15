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
package com.mgmtp.a12.kernel.md.model.api.fieldtypes;

import java.math.BigDecimal;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * For {@link IField}s containing numbers.
 */
@OnlyForUsage
public interface INumberType extends IFieldType {

	/**
	 * @return the minimum number of required digits after the decimal separator. Must be greater
	 * 		equals zero.
	 */
	int getMinFractionalDigits();

	/**
	 * @return the maximum number of allowed digits after the decimal separator. Must be greater
	 * 		equals zero.
	 */
	int getMaxFractionalDigits();

	/**
	 * @return the minimum value of this number.
	 */
	Optional<BigDecimal> getMinValue();

	/**
	 * @return the maximum value of this number.
	 */
	Optional<BigDecimal> getMaxValue();

	/**
	 * @return whether zero is not or is a valid value for this number.
	 */
	boolean isZeroNotAllowed();

	/**
	 * @return whether only positive or also negative values are valid for this number.
	 */
	boolean isPositivesOnly();

	/**
	 * @return the trait of the the number or an empty Optional instance if no trait has been
	 * 		specified. At the moment following types are allowed:
	 * 		<ul>
	 * 		<li>amount</li>
	 * 		<li>percent</li>
	 * 		<li>permille</li>
	 * 		</ul>
	 */
	Optional<String> getTrait();

	/**
	 * @return the minimum length of the number value or an empty Optional instance if no minimum
	 * 		length is specified.
	 */
	Optional<Integer> getMinLength();

	/**
	 * @return the maximum length of the number value or an empty Optional instance if no maximum
	 * 		length is specified.
	 */
	Optional<Integer> getMaxLength();

	/**
	 * Returns the maximum number of digits which can be present before the decimal separator. If it
	 * is unspecified, returns {@link Optional#empty()}. If the max number of integer digits is set,
	 * it will always be at least 1.
	 *
	 * @return the the maximum number of digits before the decimal separator
	 */
	Optional<Integer> getMaxIntegerDigits();

	/**
	 * @return whether leading zeros are allowed in the user input for this number, e.g.
	 *        {@code 00030}.
	 */
	boolean isLeadingZerosAllowed();
}
