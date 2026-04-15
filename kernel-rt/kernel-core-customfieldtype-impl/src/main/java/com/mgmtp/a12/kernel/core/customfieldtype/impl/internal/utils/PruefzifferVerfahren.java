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

package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.utils;

/**
 * Validierungsverfahren einer Prüfziffer für Einheitswert-Aktenzeichen und Steuernummer
 */
public class PruefzifferVerfahren {

	public enum Verfahren {
		A1,
		A2,
		A3,
		NRW,
		STNR
	}

	;

	private PruefzifferVerfahren() {
	}

	public static boolean pruefeMitElferVerfahren(final String nummer, final int pruefziffer,
			final int[] faktoren) {
		int sum = 0;
		for (int i = 0; i < faktoren.length; i++) {
			sum += (faktoren[i] * Integer.parseInt(String.valueOf(nummer.charAt(i))));
		}
		return pruefePruefzifferAusDifferenz(sum, 11, pruefziffer);
	}

	public static boolean pruefeMitZweierVerfahren(final String nummer, final int pruefziffer,
			final int[] faktoren, final int[] summanden) {
		int sum = 0;
		for (int i = 0; i < summanden.length; i++) {
			int summe = (summanden[i] + Integer.parseInt(String.valueOf(nummer.charAt(i))));
			int quersummeEinstellig = quersummeEinstellig(einerstelle(summe) * faktoren[i]);
			sum += quersummeEinstellig;
		}
		return pruefePruefzifferAusDifferenz(sum, 10, pruefziffer);
	}

	// Pruefziffer: Differenz zwischen errechneter Summe und der naechst-groesseren, durch x teilbaren, Zahl
	private static int pruefzifferAusDifferenz(final int summe, final int x) {
		int m = summe % x;
		if (m != 0) {
			m = x - m;
		}
		return m;
	}

	public static boolean pruefePruefzifferAusDifferenz(final int summe, final int x, final int pruefziffer) {
		int p = pruefzifferAusDifferenz(summe, x);
		return valuesAreEqual(p, pruefziffer);
	}

	private static int quersummeEinstellig(final int zahl) {
		notNegative(zahl);
		int quer = zahl;
		while (quer >= 10) {
			quer = quersumme(quer);
		}
		return quer;
	}

	public static int quersumme(int zahl) {
		notNegative(zahl);
		int result = 0;
		while (zahl > 0) {
			result += zahl % 10;
			zahl /= 10;
		}
		return result;
	}

	public static int einerstelle(int sum) {
		notNegative(sum);
		return sum % 10;
	}

	public static boolean valuesAreEqual(final double a, final int b) {
		return (Double.compare(a, b) == 0);
	}

	private static void notNegative(final int zahl) {
		if (zahl < 0) {
			throw new IllegalArgumentException("Parameter ist negativ.");
		}
	}
}
