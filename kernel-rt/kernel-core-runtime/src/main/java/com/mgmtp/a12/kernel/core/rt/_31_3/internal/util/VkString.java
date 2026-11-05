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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.util;

import org.apache.commons.lang3.StringUtils;

import lombok.NonNull;

/**
 * Wrapper um für Strings, die bei der Auswertung von Regelbedingungen ermittelt werden, zusätzlich
 * zu speichern, ob ein nicht angegebenes oder nicht prüfungsrelevantes Feld gefunden wurde. Ist das
 * Feld nicht prüfungsrelevant, wird ein umgebender Vergleich zu false ausgewertet. Falls das Feld
 * nicht angegeben wurde, kann es zu Auslassungsfehlern kommen.
 */
public class VkString extends VkValue {

	public static final VkString NICHT_PRUEF_REL_STRING = createNichtPruefungsrelevantenString();
	public static final VkString NICHT_ANGEGEBENER_STRING = createNichtAngegebenenString();

	private final @NonNull String wert;
	private final boolean nichtAngegeben;
	private final boolean nichtPruefungsrelevant;

	public VkString(String wert) {
		// This should only be used when creating from a string constant in generated code
		this(wert, CollectionWithCheapMergeCopy.empty());
	}

	public VkString(String wert, CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		super(origins);
		this.wert = wert != null ? wert : "";
		nichtAngegeben = false;
		nichtPruefungsrelevant = false;
	}

	public VkString(String wert, boolean nichtAngegeben, boolean nichtPruefungsrelevant) {
		this(wert, nichtAngegeben, nichtPruefungsrelevant, CollectionWithCheapMergeCopy.empty());
	}

	public VkString(String wert, boolean nichtAngegeben, boolean nichtPruefungsrelevant,
			CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		super(origins);
		this.wert = wert != null ? wert : "";
		this.nichtAngegeben = nichtAngegeben;
		this.nichtPruefungsrelevant = nichtPruefungsrelevant;
	}

	public VkString add(VkString other) {
		String erg = this.wert + other.wert;
		boolean na = this.nichtAngegeben || other.nichtAngegeben;
		boolean np = this.nichtPruefungsrelevant || other.nichtPruefungsrelevant;
		return new VkString(erg, na, np, this.getOrigins().merge(other.getOrigins()));
	}

	public VkString add(String other) { // NOTE: maybe rename to addConstant
		return add(other, CollectionWithCheapMergeCopy.empty());
	}

	public VkString add(String other, CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		String erg = this.wert + other;
		boolean na = this.nichtAngegeben;
		boolean np = this.nichtPruefungsrelevant;
		return new VkString(erg, na, np, this.getOrigins().merge(origins));
	}

	private static VkString createNichtPruefungsrelevantenString() {
		return new VkString("", false, true, CollectionWithCheapMergeCopy.empty());
	}

	private static VkString createNichtAngegebenenString() {
		return new VkString("", true, false, CollectionWithCheapMergeCopy.empty());
	}

	public boolean isNichtAngegeben() {
		return nichtAngegeben;
	}

	public boolean isNichtPruefungsrelevant() {
		return nichtPruefungsrelevant;
	}

	public @NonNull String getWert() {
		return wert;
	}

	public boolean hasValue() {
		return !StringUtils.isEmpty(wert);
	}

	public int length() {
		return wert.length();
	}

	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other.getClass() != getClass()) {
			return false;
		}
		VkString o = (VkString) other;
		return this.wert.equals(o.wert);
	}

	public int hashCode() {
		return this.wert.hashCode();
	}

	public String toString() {
		return "VkString(" + wert + ", " + nichtAngegeben + ", " + nichtPruefungsrelevant + ")";
	}
}
