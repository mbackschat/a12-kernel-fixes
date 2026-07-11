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

import java.util.HashSet;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.CollectionWithCheapMergeCopy;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VkBigDecimal;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VkValue;

/**
 * Implementation of a {@link Combiner} for {@link VkValue} subclasses that returns the number of
 * distinct non-null values.
 * Distinctness is based on the .equals and .hashCode implementation of the submitted objects.
 */
public class NumberOfDifferentValuesCombiner<T extends VkValue> implements Combiner<T, VkBigDecimal> {

	SetOfVkValues<T> values = new SetOfVkValues<>();
	private boolean omittedEmptyValue = false;
	private boolean anyAddedValueIsNichtAngegeben = false;
	private boolean anyAddedValueIsNichtPruefungsrelevant = false;

	@Override
	public VkBigDecimal getNonRelevantValue() {
		return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
	}

	@Override
	public VkBigDecimal getValue(boolean containsFilter) {
		boolean kannGroesserWerden = containsFilter || omittedEmptyValue || anyAddedValueIsNichtAngegeben;
		boolean kannKleinerWerden = containsFilter || anyAddedValueIsNichtAngegeben; // if any non-empty values have "nichtAngegeben"
		// (more accurate name: "veränderungMöglichBeiWeiterenAngaben"), some pair of distinct values may become equal
		// -> kannKleinerWerden must be set to true.
		// Note that this does not mean that kannKleinerWerden == true if any input value to the combiner
		// was based on a Field that is nichtAngegeben -- that value would be empty (hasValue() == false)
		// and not added to .values at all
		return new VkBigDecimal(values.size(), anyAddedValueIsNichtPruefungsrelevant, kannGroesserWerden,
				kannKleinerWerden, values.getOrigins());
	}

	@Override
	public void addValue(T value, IFormatDefinition fd, boolean testValidity) {
		if (value.hasValue()) {
			values.add(value);
			anyAddedValueIsNichtAngegeben |= value.isNichtAngegeben();
			anyAddedValueIsNichtPruefungsrelevant |= value.isNichtPruefungsrelevant();
		} else {
			omittedEmptyValue = true;
		}
	}

	@Override
	public void setOmittedTail(IFormatDefinition fd) {
		omittedEmptyValue = true;
	}

	private static class SetOfVkValues<T extends VkValue> {
		private Set<T> values = new HashSet<>();
		private CollectionWithCheapMergeCopy<RtInternalIdentifier> origins = CollectionWithCheapMergeCopy.empty();

		void add(T value) { // we don't return a boolean, because even if the elem was not added, the origins may get mutated
			origins = origins.merge(value.getOrigins()); // merge the origins even if the value was already contained in values,
			// because otherwise we'd arbitrarily (or rather order-dependently) decide which origins to keep and which to discard
			values.add(value);
		}

		int size() {
			return values.size();
		}

		CollectionWithCheapMergeCopy<RtInternalIdentifier> getOrigins() {
			return origins;
		}
	}
}
