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

package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.impl;

import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberFormatting;

class GermanTaxNumberFormatting implements IGermanTaxNumberFormatting {

	/** Anzahl der Stellen für den Bezirk */
	int numPositionsDistrict;
	/** Anzahl der Stellen für die Finanzamtsnummer */
	int numPositionsTaxOffice;
	/** Anzahl der Stellen für die Unterscheidungsnummer */
	int numPositionsDifferentiationNumber;
	/** Prefix der Steuernummer */
	String prefix;
	/** Trennzeichen zwischen Finanzamtsnummer und Bezirk, kann Leerstring sein */
	String delimiter1;
	/** Trennzeichen zwischen Bezirk und Unterscheidungsnummer, kann Leerstring sein */
	String delimiter2;
	/** Trennzeichenn zwischen Unterscheidungsnummer und Prüfziffer, kann Leerstring sein */
	String delimiter3;

	GermanTaxNumberFormatting(int stFaNr, int stBezirkNr, int stUNr, String prefix, String trennz1, String trennz2,
			String trennz3) {
		this.numPositionsDistrict = stBezirkNr;
		this.numPositionsTaxOffice = stFaNr;
		this.numPositionsDifferentiationNumber = stUNr;
		this.prefix = prefix == null ? "" : prefix;
		this.delimiter1 = trennz1 == null ? "" : trennz1;
		this.delimiter2 = trennz2 == null ? "" : trennz2;
		this.delimiter3 = trennz3 == null ? "" : trennz3;
	}

	@Override
	public int getNumPositionsDistrict() {
		return this.numPositionsDistrict;
	}

	@Override
	public int getNumPositionsTaxOffice() {
		return this.numPositionsTaxOffice;
	}

	@Override
	public int getNumPositionsDifferentiationNumber() {
		return this.numPositionsDifferentiationNumber;
	}

	@Override
	public String getPrefix() {
		return this.prefix;
	}

	@Override
	public String getDelimiter1() {
		return this.delimiter1;
	}

	@Override
	public String getDelimiter2() {
		return this.delimiter2;
	}

	@Override
	public String getDelimiter3() {
		return this.delimiter3;
	}

}
