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
package com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes;

import java.math.BigDecimal;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.NumberType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.NonNull;

public class NumberTypeWrapper extends FieldTypeWrapper<NumberType> implements INumberType {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	public NumberTypeWrapper(@NonNull NumberType numberType) {
		super(numberType);
	}

	@Override
	public int getMinFractionalDigits() {
		return fieldType.getMinFractionalDigits();
	}

	@Override
	public int getMaxFractionalDigits() {
		return fieldType.getMaxFractionalDigits();
	}

	@Override
	public Optional<BigDecimal> getMinValue() {
		return fieldType.getMinValue();
	}

	@Override
	public Optional<BigDecimal> getMaxValue() {
		return fieldType.getMaxValue();
	}

	@Override
	public boolean isZeroNotAllowed() {
		return fieldType.isZeroNotAllowed();
	}

	@Override
	public boolean isPositivesOnly() {
		return fieldType.isPositivesOnly();
	}

	@Override
	public Optional<String> getTrait() {
		return fieldType.getTrait();
	}

	@Override
	public Optional<Integer> getMinLength() {
		return fieldType.getMinLength();
	}

	@Override
	public Optional<Integer> getMaxLength() {
		return fieldType.getMaxLength();
	}

	@Override
	public Optional<Integer> getMaxIntegerDigits() {
		return fieldType.getMaxIntegerDigits();
	}

	@Override
	public boolean isLeadingZerosAllowed() {
		return fieldType.isLeadingZerosAllowed();
	}

}
