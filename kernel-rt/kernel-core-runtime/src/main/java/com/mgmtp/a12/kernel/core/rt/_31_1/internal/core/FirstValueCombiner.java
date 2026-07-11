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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.core;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VkBigDecimal;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VkDate;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VkDateRange;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VkString;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VkValue;

/**
 * Implementation of a {@link Combiner} for {@link VkDate} that returns the first given value.
 */
public abstract class FirstValueCombiner<T extends VkValue> implements Combiner<T, T> {
	private T value = null;
	private boolean notGivenFieldBeforeFirstGiven = false;
	private boolean nonRelevantFieldBeforeFirstGiven = false;
	private boolean omittedTail = false;

	public static class FirstValueCombinerDate extends FirstValueCombiner<VkDate> {

		@Override public VkDate getNonRelevantValue() {
			return VkDate.NON_RELEVANT_DATE;
		}

		@Override protected VkDate notGivenValue() {
			return VkDate.NOT_GIVEN_DATE;
		}

		@Override protected VkDate createValue(VkDate value, boolean notGiven, boolean nonRelevant) {
			return new VkDate(value.getDate(), notGiven, nonRelevant, value.getOrigins());
		}
	}

	public static class FirstValueCombinerNumber extends FirstValueCombiner<VkBigDecimal> {

		@Override public VkBigDecimal getNonRelevantValue() {
			return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
		}

		@Override protected VkBigDecimal notGivenValue() {
			return VkBigDecimal.NICHT_ANGEGEBENE_ZAHL;
		}

		@Override protected VkBigDecimal createValue(VkBigDecimal value, boolean notGiven, boolean nonRelevant) {
			return new VkBigDecimal(value.getBigDecimalValue(), nonRelevant, notGiven, notGiven, value.getOrigins());
		}
	}

	public static class FirstValueCombinerDateRange extends FirstValueCombiner<VkDateRange> {

		@Override public VkDateRange getNonRelevantValue() {
			return VkDateRange.NON_RELEVANT_DATE_RANGE;
		}

		@Override protected VkDateRange notGivenValue() {
			return VkDateRange.NOT_GIVEN_DATE_RANGE;
		}

		@Override protected VkDateRange createValue(VkDateRange value, boolean notGiven, boolean nonRelevant) {
			return new VkDateRange(value.getStart(), value.getEnd(), notGiven, nonRelevant, value.getOrigins());
		}
	}

	public static class FirstValueCombinerString extends FirstValueCombiner<VkString> {

		@Override public VkString getNonRelevantValue() {
			return VkString.NICHT_PRUEF_REL_STRING;
		}

		@Override protected VkString notGivenValue() {
			return VkString.NICHT_ANGEGEBENER_STRING;
		}

		@Override protected VkString createValue(VkString value, boolean notGiven, boolean nonRelevant) {
			return new VkString(value.getWert(), notGiven, nonRelevant, value.getOrigins());
		}
	}

	protected abstract T notGivenValue();

	protected abstract T createValue(T value, boolean notGiven, boolean nonRelevant);

	@Override
	public T getValue(boolean containsFilter) {
		boolean notGiven = notGivenFieldBeforeFirstGiven || containsFilter || ((value == null) && omittedTail);
		boolean nonRelevant = nonRelevantFieldBeforeFirstGiven;// || containsFilter; would be impractical because it *very* generously silences errors
		T value = this.value != null ? this.value : notGivenValue(); // _currently_ redundant

		if (nonRelevant) {
			return getNonRelevantValue();
		}

		return createValue(value, notGiven, nonRelevant);
	}

	@Override
	public boolean canChange() {
		return this.value == null && !nonRelevantFieldBeforeFirstGiven;
	}

	@Override
	public void addValue(T value, IFormatDefinition fd, boolean testValidity) {
		if (this.value == null) {
			if (value.isNichtAngegeben()) {
				notGivenFieldBeforeFirstGiven = true;
			}
			if (value.isNichtPruefungsrelevant()) {
				nonRelevantFieldBeforeFirstGiven = true;
				return;
			}
			if (value.hasValue()) {
				this.value = value;
			}
		}
	}

	@Override
	public void setOmittedTail(IFormatDefinition fd) {
		omittedTail = true;
	}
}
