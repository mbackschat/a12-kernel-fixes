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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.util;

import java.util.Date;
import java.util.function.BiFunction;

/**
 * Wrapper um für Datumsangaben, die bei der Auswertung von Regelbedingungen ermittelt werden,
 * zusätzlich zu speichern, ob ein nicht angegebenes oder nicht prüfungsrelevantes Feld gefunden
 * wurde. Ist das Feld nicht prüfungsrelevant, wird ein umgebender Vergleich zu false ausgewertet.
 * Falls das Feld nicht angegeben wurde, kann es zu Auslassungsfehlern kommen.
 */
public class VkDate extends VkValue {

	public static final VkDate NON_RELEVANT_DATE = new VkDate(null, false, true, CollectionWithCheapMergeCopy.empty());
	public static final VkDate NOT_GIVEN_DATE = new VkDate(null, true, false, CollectionWithCheapMergeCopy.empty());
	/**
	 * Does not satisfy the otherwise-probably-true "null => nichtAngegeben"
	 * invariant. Should only be used in controlled contexts where e.g.
	 * withNichtAngebenOred() is called before the VkDate is released.
	 */
	public static final VkDate NULL_DATE = new VkDate(null, false, false, CollectionWithCheapMergeCopy.empty());

	private final Date date;
	private final boolean nichtAngegeben;
	private final boolean nichtPruefungsrelevant;

	public VkDate(Date date) {
		this(date, CollectionWithCheapMergeCopy.empty());
	}

	public VkDate(Date date, CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		this(date, false, false, origins);
	}

	public VkDate(Date date, boolean nichtAngegeben, boolean nichtPruefungsrelevant) {
		this(date, nichtAngegeben, nichtPruefungsrelevant, CollectionWithCheapMergeCopy.empty());
	}

	public VkDate(Date date, boolean nichtAngegeben, boolean nichtPruefungsrelevant,
			CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		super(origins);
		this.date = date;
		this.nichtAngegeben = nichtAngegeben;
		this.nichtPruefungsrelevant = nichtPruefungsrelevant;
	}

	/**
	 * isNichtAngegeben should be similar to the semantics of kannGroesserWerden/kannKleinerWerden
	 * in VkBigDecimal, but less precise, as nichtAngegeben does not keep the lower/upper bounds on
	 * the value that kannGroesserWerden/kannKleinerWerden provide
	 * <p>
	 * Note that even when nichtAngegeben == true, date may be set and != null (e.g. a MaxWert where
	 * some fields are set)
	 *
	 * @return true if this date or any of the fields that it is based on are nichtAngegeben
	 */
	public boolean isNichtAngegeben() {
		return nichtAngegeben;
	}

	public boolean isNichtPruefungsrelevant() {
		return nichtPruefungsrelevant;
	}

	public boolean hasValue() {
		return date != null;
	}

	public Date getDate() {
		return date;
	}

	public VkDate max(VkDate other) {
		return combine(other, (d1, d2) -> d1.date.compareTo(d2.date) >= 0 ? d1 : d2);
	}

	public VkDate min(VkDate other) {
		return combine(other, (d1, d2) -> d1.date.compareTo(d2.date) <= 0 ? d1 : d2);
	}

	private VkDate combine(VkDate other, BiFunction<VkDate, VkDate, VkDate> select) {
		boolean nichtAngegeben = this.nichtAngegeben || other.nichtAngegeben;
		boolean nichtPruefungsrelevant = this.nichtPruefungsrelevant || other.nichtPruefungsrelevant;

		if (nichtPruefungsrelevant) {
			return NON_RELEVANT_DATE;
		}

		VkDate value = null;
		if (this.hasValue() && other.hasValue()) {
			value = select.apply(this, other);
		} else if (this.hasValue()) {
			value = this;
		} else if (other.hasValue()) {
			value = other;
		}

		return new VkDate(value != null ? value.date : null, nichtAngegeben, nichtPruefungsrelevant,
				value != null ? value.getOrigins() : null);
	}

	/**
	 * create a copy of this VkDate with nichtAngegeben set to true
	 * <p>
	 * (setting nichtAngegeben to false is not supported because
	 * the resulting object could become inconsistent
	 * if .date == null)
	 */
	public VkDate withNichtAngegeben() {
		return new VkDate(date, true, nichtPruefungsrelevant, getOrigins());
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other.getClass() != getClass()) {
			return false;
		}
		VkDate o = (VkDate) other;
		return (this.date == null) ? (o.date == null) : this.date.equals(o.date);
	}

	@Override
	public int hashCode() {
		return (this.date == null) ? 0 : this.date.hashCode();
	}

	@Override
	public String toString() {
		String postFix = " (nichtAngegeben = " + nichtAngegeben + ", nichtPruefungsrelevant = " + nichtPruefungsrelevant + ")";
		if (date == null) {
			return "null" + postFix;
		}
		return date.toString() + postFix;
	}
}
