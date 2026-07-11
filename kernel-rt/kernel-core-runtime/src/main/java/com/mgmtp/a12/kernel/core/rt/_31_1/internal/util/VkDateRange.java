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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.util.Date;
import java.util.Objects;

import lombok.Getter;

/**
 * Wrapper um für Datumsangaben, die bei der Auswertung von Regelbedingungen ermittelt werden,
 * zusätzlich zu speichern, ob ein nicht angegebenes oder nicht prüfungsrelevantes Feld gefunden
 * wurde. Ist das Feld nicht prüfungsrelevant, wird ein umgebender Vergleich zu false ausgewertet.
 * Falls das Feld nicht angegeben wurde, kann es zu Auslassungsfehlern kommen.
 */
@Getter public class VkDateRange extends VkValue {

	public static final VkDateRange NON_RELEVANT_DATE_RANGE = new VkDateRange(null, null, false, true,
			CollectionWithCheapMergeCopy.empty());
	public static final VkDateRange NOT_GIVEN_DATE_RANGE = new VkDateRange(null, null, true, false,
			CollectionWithCheapMergeCopy.empty());
	/**
	 * Does not satisfy the otherwise-probably-true "null => nichtAngegeben"
	 * invariant. Should only be used in controlled contexts where e.g.
	 * withNichtAngebenOred() is called before the VkDate is released.
	 */
	public static final VkDateRange NULL_DATE_RANGE = new VkDateRange(null, null, false, false,
			CollectionWithCheapMergeCopy.empty());

	private final Date start;
	private final Date end;
	/**
	 * -- GETTER --
	 * isNichtAngegeben should be similar to the semantics of kannGroesserWerden/kannKleinerWerden
	 * in VkBigDecimal, but less precise, as nichtAngegeben does not keep the lower/upper bounds on
	 * the value that kannGroesserWerden/kannKleinerWerden provide
	 * <p>
	 * Note that even when nichtAngegeben == true, date may be set and != null (e.g. a MaxWert where
	 * some fields are set)
	 *
	 * @return true if this date or any of the fields that it is based on are nichtAngegeben
	 */
	private final boolean nichtAngegeben;
	private final boolean nichtPruefungsrelevant;

	public VkDateRange(Date start, Date end) {
		this(start, end, CollectionWithCheapMergeCopy.empty());
	}

	public VkDateRange(Date start, Date end, CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		this(start, end, false, false, origins);
	}

	public VkDateRange(Date start, Date end, boolean nichtAngegeben, boolean nichtPruefungsrelevant) {
		this(start, end, nichtAngegeben, nichtPruefungsrelevant, CollectionWithCheapMergeCopy.empty());
	}

	public VkDateRange(Date start, Date end, boolean nichtAngegeben, boolean nichtPruefungsrelevant,
			CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		super(origins);
		this.start = start;
		this.end = end;
		this.nichtAngegeben = nichtAngegeben;
		this.nichtPruefungsrelevant = nichtPruefungsrelevant;
	}

	public boolean hasValue() {
		return start != null && end != null;
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		VkDateRange that = (VkDateRange) o;
		return Objects.equals(start, that.start) && Objects.equals(end, that.end);
	}

	@Override public int hashCode() {
		return Objects.hash(start, end);
	}

	/**
	 * create a copy of this VkDate with nichtAngegeben set to true
	 * <p>
	 * (setting nichtAngegeben to false is not supported because
	 * the resulting object could become inconsistent
	 * if .date == null)
	 */
	public VkDateRange withNichtAngegeben() {
		return new VkDateRange(start, end, true, nichtPruefungsrelevant, getOrigins());
	}

	@Override
	public String toString() {
		return String.format("%s-%s (nichtAngegeben = %s, nichtPrüfungsrelevant = %s)", start, end, nichtAngegeben,
				nichtPruefungsrelevant);
	}
}
