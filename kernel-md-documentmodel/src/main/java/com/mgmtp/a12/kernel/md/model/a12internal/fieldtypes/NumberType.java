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

package com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes;

import java.math.BigDecimal;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * For {@link Field}s containing numbers.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NumberType extends FieldType {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return the minimum number of decimal places which are required for the number.
	 * @param minFractionalDigits
	 * 		the new minimum number of decimal places which should be required
	 * 		for the number.
	 */
	private int minFractionalDigits;
	/**
	 * @return the maximum number of decimal places which are allowed for the number.
	 * @param maxFractionalDigits
	 * 		the new maximum number of decimal places which should be allowed
	 * 		for the number.
	 */
	private int maxFractionalDigits;
	/**
	 * @return the lower limit for the number.
	 * @param minValue
	 * 		the new lower limit for the number. May be {@code null} to unset.
	 */
	private BigDecimal minValue;
	/**
	 * @return the upper limit which the number may not exceed.
	 * @param maxValue
	 * 		the new upper limit which the number may not exceed. May be {@code null} to
	 * 		unset.
	 */
	private BigDecimal maxValue;
	/**
	 * @return {@code false} if {@code 0} is allowed for the number. Otherwise {@code true}.
	 * @param zeroNotAllowed
	 * 		use {@code false} if {@code 0} should be allowed for the number. Otherwise
	 *        {@code false}.
	 */
	private boolean zeroNotAllowed = false;
	/**
	 * @return {@code false} if negative values are allowed for the number. Otherwise {@code true}.
	 * @param positivesOnly
	 * 		use {@code false} if negative values should be allowed for the number.
	 * 		Otherwise {@code true}.
	 */
	private boolean positivesOnly = false;
	/**
	 * @return the type the number can be. At the moment following types are allowed:
	 * 		<ul>
	 * 		<li>amount</li>
	 * 		<li>percent</li>
	 * 		<li>permille</li>
	 * 		</ul>
	 * @param trait
	 * 		the new type the number can be (for allowed types see {@link #getTrait()}). May
	 * 		be {@code null} to unset.
	 */
	private String trait;
	/**
	 * @param minLength
	 * 		the minimum length, may be {@code null}. See also
	 *        {@link INumberType#getMinLength()}.
	 */
	private Integer minLength;
	/**
	 * @param maxLength
	 * 		the maximum length, may be {@code null}. See also
	 *        {@link INumberType#getMaxLength()}.
	 */
	private Integer maxLength;
	/**
	 * @param maxIntegerDigits
	 * 		the maximum number of digits which can be present before the decimal separator.
	 * 		May be {@code null}. See also {@link INumberType#getMaxIntegerDigits()}.
	 */
	private Integer maxIntegerDigits;
	/**
	 * @return see {@link INumberType#isLeadingZerosAllowed()}.
	 * @param leadingZerosAllowed
	 * 		use {@code true} if leading zeros should be allowed for this number. Otherwise
	 *        {@code false}.
	 */
	private boolean leadingZerosAllowed = false;

	@Builder(builderClassName = "NumberTypeBuilder")
	private NumberType(int minFractionalDigits, int maxFractionalDigits, BigDecimal minValue, BigDecimal maxValue,
			boolean zeroNotAllowed, boolean positivesOnly, String trait,
			Integer minLength, Integer maxLength, Integer maxIntegerDigits, boolean leadingZerosAllowed) {
		this.minFractionalDigits = minFractionalDigits;
		this.maxFractionalDigits = maxFractionalDigits;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.zeroNotAllowed = zeroNotAllowed;
		this.positivesOnly = positivesOnly;
		this.trait = trait;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.maxIntegerDigits = maxIntegerDigits;
		this.leadingZerosAllowed = leadingZerosAllowed;
	}

	public Optional<BigDecimal> getMinValue() {
		return Optional.ofNullable(minValue);
	}

	public Optional<BigDecimal> getMaxValue() {
		return Optional.ofNullable(maxValue);
	}

	public Optional<String> getTrait() {
		return Optional.ofNullable(trait);
	}

	/**
	 * @return the minimum length required for the number.
	 */
	public Optional<Integer> getMinLength() {
		return Optional.ofNullable(minLength);
	}

	/**
	 * @return the maximum length required for the number.
	 */
	public Optional<Integer> getMaxLength() {
		return Optional.ofNullable(maxLength);
	}

	/**
	 * @return the maximum number of digits which can be present before the decimal separator.
	 */
	public Optional<Integer> getMaxIntegerDigits() {
		return Optional.ofNullable(maxIntegerDigits);
	}

	public static class NumberTypeBuilder {
		private boolean zeroNotAllowed = false; // in order to make NumberTypeBuilder.zeroAllowed() optional with the right default value
		private boolean positivesOnly = false; // in order to make NumberTypeBuilder.minusSignAllowed() optional with the right default value
		private boolean leadingZerosAllowed = false; // in order to make NumberTypeBuilder.leadingZerosAllowed() optional with the right default value
	}

}
