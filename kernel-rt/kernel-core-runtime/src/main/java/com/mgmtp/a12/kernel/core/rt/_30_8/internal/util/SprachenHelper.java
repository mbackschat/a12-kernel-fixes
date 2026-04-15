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

import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;

/**
 * Die Klasse wird zur Abfrage von sprachspezifischer Darstellungsinformation in der Runtime
 * verwendet. Die hier verwendeten Werte für die Darstellungsinfo müssen den entsprechenden
 * Einträgen in der Datenbank entsprechen. Da die Runtime nicht generiert wird, sind diese Werte
 * hier zusätzlich hinterlegt.
 */
public class SprachenHelper {

	private SprachenHelper() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Gibt für einen Schlüssel der Darstellungsinformation den für die durch das Locale
	 * spezifizierte Sprache hinterlegten Wert zurück. Wird verwendet, wenn für einen Schlüssel die
	 * Konstante {@link PresentationInformation#LANGUAGE_DEFINED} übergeben wurde.
	 * <p>
	 * Ist der Wert {@link PresentationInformation#LANGUAGE_DEFINED} für den übergebenen Schlüssel
	 * nicht erlaubt, oder wurde kein Locale bzw. ein Locale für eine nicht unterstützte Sprache
	 * übergeben, wird eine {@link IllegalArgumentException} geworfen.
	 * </p>
	 */
	public static String getSprachspezifischeDarstellungsinfo(String darstellungsinfoKey,
			PresentationInformationValues defaultWerte) {
		if (defaultWerte == null) {
			throw new IllegalArgumentException(
					String.format(
							"If the constant LANGUAGE_DEFINED is used for the key '%s', a Locale specifying the language has to be passed.",
							darstellungsinfoKey));
		}

		if (PresentationInformation.DECIMAL_SEPARATOR.equals(darstellungsinfoKey)) {
			return defaultWerte.getDezimalTrenner();
		}
		if (PresentationInformation.THOUSANDS_SEPARATOR.equals(darstellungsinfoKey)) {
			return defaultWerte.getThousandsSeparator();
		}
		if (PresentationInformation.DATE_SEPARATOR.equals(darstellungsinfoKey)) {
			return defaultWerte.getDatumTrenner();
		}
		if (PresentationInformation.DATE_RANGE_SEPARATOR.equals(darstellungsinfoKey)) {
			return defaultWerte.getDatumbereichTrenner();
		}
		if (PresentationInformation.DATE_ZERO_OPTIONAL.equals(darstellungsinfoKey)) {
			return defaultWerte.isDatumNullOptional();
		}
		if (PresentationInformation.DATE_FRAGMENT_ORDERING.equals(darstellungsinfoKey)) {
			return defaultWerte.getDatumReihenfolge();
		}
		if (PresentationInformation.DATE_TIME_FORMAT.equals(darstellungsinfoKey)) {
			return defaultWerte.getDateTimeFormat();
		}
		if (PresentationInformation.TIME_FORMAT.equals(darstellungsinfoKey)) {
			return defaultWerte.getTimeFormat();
		}

		throw new IllegalArgumentException(
				String.format("The constant LANGUAGE_DEFINED is not allowed for the key '%s'.", darstellungsinfoKey));
	}
}
