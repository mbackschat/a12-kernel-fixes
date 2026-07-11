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

import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.FormatDefinitionZahl;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VkBigDecimal;

/**
 * Abstract implementation of a {@link Combiner} for {@link VkBigDecimal}s.
 * Contains specific implementations for FirstFilledValue, MinValue, MaxValue and
 * Sum.
 */
abstract class NumberCombiner implements Combiner<VkBigDecimal, VkBigDecimal> {
	static class MinWertCombiner extends NumberCombiner {
		public VkBigDecimal combineWith(VkBigDecimal d2) {
			return (first) ? d2 : value.min(d2);
		}

		public boolean kannGroesserWerdenBeiNichtAngFeld() {
			return false;
		}

		public boolean kannKleinerWerdenBeiNichtAngFeld(IFormatDefinition fd) {
			return true;
		}
	}

	static class MaxWertCombiner extends NumberCombiner {
		public VkBigDecimal combineWith(VkBigDecimal d2) {
			return (first) ? d2 : value.max(d2);
		}

		public boolean kannGroesserWerdenBeiNichtAngFeld() {
			return true;
		}

		public boolean kannKleinerWerdenBeiNichtAngFeld(IFormatDefinition fd) {
			return false;
		}
	}

	static class SummeCombiner extends NumberCombiner {
		public VkBigDecimal combineWith(VkBigDecimal d2) {
			return value.add(d2);
		}

		public boolean kannGroesserWerdenBeiNichtAngFeld() {
			return true;
		}

		public boolean kannKleinerWerdenBeiNichtAngFeld(IFormatDefinition fd) {
			if (!(fd instanceof FormatDefinitionZahl)) {
				return true;
			}
			return ((FormatDefinitionZahl) fd).isSignAllowed();
		}
	}

	protected boolean nichtAngFeldGefunden = false;
	protected boolean kannKleinerWerden = false;
	protected boolean first = true;
	protected VkBigDecimal value = VkBigDecimal.ZAHL_0;

	@Override
	public VkBigDecimal getNonRelevantValue() {
		return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
	}

	@Override
	public void setOmittedTail(IFormatDefinition fd) {
		setEmptyValueGiven(fd);
	}

	private void setEmptyValueGiven(IFormatDefinition fd) {
		nichtAngFeldGefunden = true;
		kannKleinerWerden = kannKleinerWerden || kannKleinerWerdenBeiNichtAngFeld(fd);
	}

	@Override
	public void addValue(VkBigDecimal val, IFormatDefinition fd, boolean testValidity) {
		if (testValidity &&
				val.getBigDecimalValue().equals(VkBigDecimal.NICHT_ANGEGEBENE_ZAHL.getBigDecimalValue()) &&
				(val.isNichtPruefungsrelevant() || val.kannGroesserWerden() || val.kannKleinerWerden())) {
			setEmptyValueGiven(fd);
			return;
		}
		value = this.combineWith(val);
		first = false;
	}

	@Override
	public VkBigDecimal getValue(boolean containsFilter) {
		if (containsFilter) {// in presence of a filter condition we assume that the result always can become bigger or smaller
			return new VkBigDecimal(value.getBigDecimalValue(), false, true, true, value.getOrigins());
		}
		if (nichtAngFeldGefunden) {
			/*
			 * Wenn keines der Felder angegeben ist (dann ist first = true), kann die Zahl immer
			 * größer oder kleiner werden (weil dann 0 zurückgegeben wird).
			 */
			boolean kannGroesserWerden = first || kannGroesserWerdenBeiNichtAngFeld();
			kannKleinerWerden = kannKleinerWerden || first;
			return new VkBigDecimal(value.getBigDecimalValue(), false, kannGroesserWerden, kannKleinerWerden, value.getOrigins());
		}
		return value;
	}

	abstract VkBigDecimal combineWith(VkBigDecimal d);

	abstract boolean kannGroesserWerdenBeiNichtAngFeld();

	abstract boolean kannKleinerWerdenBeiNichtAngFeld(IFormatDefinition fd);
}
