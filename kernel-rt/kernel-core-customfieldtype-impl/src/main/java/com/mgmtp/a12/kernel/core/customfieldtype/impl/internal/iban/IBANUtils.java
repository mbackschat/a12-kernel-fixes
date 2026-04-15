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

package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.iban;

import java.math.BigInteger;

/**
 * Diesen Code in eine getrennte 'internal'-Klasse verschoben, denn er wird auch zur Prüfung des BOP
 * CustomFieldTypes LEI-Code verwendet und muss deshalb public sein. Durch diese Klasse soll
 * vermieden werden, dass die public-Methode Teil der API ist.
 */
public class IBANUtils {

	private static final BigInteger PRUEFZIFFERN_DIVISOR = BigInteger.valueOf(97l);
	private static final BigInteger ERWARTETER_REST = BigInteger.valueOf(1l);

	private IBANUtils() {
		throw new UnsupportedOperationException("No instatiation for static helper classes!");
	}

	/**
	 * @brief Ist die Prüfziffer der IBAN gültig?
	 * 		<p>
	 * 		Pruefziffernberechnung gemaess "IBAN-Prüfziffernberechnung", 06/03, Swiss Interbank
	 * 		Clearing AG (ISO 7064 nach dem Modulo 9710 Verfahren)
	 * 		</p>
	 * 		<p>
	 * 		Bei dieser Prüfung werden zunächst die ersten vier Zeichen an das Ende der IBAN
	 * 		verschoben (ausschließlich für IBAN mit Schiebeposition = 4). Falls die
	 * 		Schiebeposition auf 0 gesetzt wird, bedeutet dies, dass keine Verschiebung erfolgt).
	 * 		Anschließend erfolgt eine Konvertierung:
	 * 		anhand einer Konversionstabelle wird jede Stelle der IBAN in eine Zahl konvertiert.
	 * 		Die erhaltene Zahl wird durch die Prüfziffer 97 dividiert und geprüft, ob der Rest
	 * 		dieser Division dem erwarteten Rest von 1 entspricht.
	 * 		</p>
	 */
	public static boolean istPruefzifferGueltig(final String iban, int schiebeposition) {
		// 1. Schritt (Ausschließlich für IBAN mit Schiebeposition = 4): Schiebe die ersten vier Zeichen nach hinten.
		// 2. Schritt: Konvertiere A-Z in Ziffern.
		StringBuilder convertedIBAN = new StringBuilder();
		int ibanLength = iban.length();
		try {
			for (int i = schiebeposition; i < ibanLength; i++) {
				convertedIBAN.append(pruefziffernKonversionstabelle(iban.charAt(i)));
			}
			for (int i = 0; i < Math.min(schiebeposition, ibanLength); i++) {
				convertedIBAN.append(pruefziffernKonversionstabelle(iban.charAt(i)));
			}
		} catch (IllegalArgumentException e) {
			// ERICBASIS_DEBUG("IllegalArgumentException - weil es sich um einen unerwarteteten Buchstaben handelt.");
			return false;
		}
		// 3. Schritt: Division.
		BigInteger rest = pruefziffernDivision(convertedIBAN.toString());
		if (!ERWARTETER_REST.equals(rest)) {
			// ERICBASIS_DEBUG("Prüfzifferndivisionsrest ist ungültig");
			return false;
		}

		// ERICBASIS_DEBUG("Prüfziffer ist gültig.");
		return true;
	}

	/**
	 * Konvertiere a-zA-Z in zweistellige Ziffernfolge.
	 * <p>
	 * Durch die vorausgehende formale Prüfung ist sichergestellt, dass hier nur
	 * a-zA-Z0-9 als Parameter buchstabe auftreten.
	 */
	private static String pruefziffernKonversionstabelle(final char buchstabe) throws IllegalArgumentException {
		//Cobertura:/Alle aufgelisteten Chars werden getestet
		switch (buchstabe) {
			case '0':
				return "0";
			case '1':
				return "1";
			case '2':
				return "2";
			case '3':
				return "3";
			case '4':
				return "4";
			case '5':
				return "5";
			case '6':
				return "6";
			case '7':
				return "7";
			case '8':
				return "8";
			case '9':
				return "9";

			case 'A':
				return "10";
			case 'B':
				return "11";
			case 'C':
				return "12";
			case 'D':
				return "13";
			case 'E':
				return "14";
			case 'F':
				return "15";
			case 'G':
				return "16";
			case 'H':
				return "17";
			case 'I':
				return "18";
			case 'J':
				return "19";
			case 'K':
				return "20";
			case 'L':
				return "21";
			case 'M':
				return "22";
			case 'N':
				return "23";
			case 'O':
				return "24";
			case 'P':
				return "25";
			case 'Q':
				return "26";
			case 'R':
				return "27";
			case 'S':
				return "28";
			case 'T':
				return "29";
			case 'U':
				return "30";
			case 'V':
				return "31";
			case 'W':
				return "32";
			case 'X':
				return "33";
			case 'Y':
				return "34";
			case 'Z':
				return "35";

			case 'a':
				return "10";
			case 'b':
				return "11";
			case 'c':
				return "12";
			case 'd':
				return "13";
			case 'e':
				return "14";
			case 'f':
				return "15";
			case 'g':
				return "16";
			case 'h':
				return "17";
			case 'i':
				return "18";
			case 'j':
				return "19";
			case 'k':
				return "20";
			case 'l':
				return "21";
			case 'm':
				return "22";
			case 'n':
				return "23";
			case 'o':
				return "24";
			case 'p':
				return "25";
			case 'q':
				return "26";
			case 'r':
				return "27";
			case 's':
				return "28";
			case 't':
				return "29";
			case 'u':
				return "30";
			case 'v':
				return "31";
			case 'w':
				return "32";
			case 'x':
				return "33";
			case 'y':
				return "34";
			case 'z':
				return "35";
			default:
				throw new IllegalArgumentException("The IBAN contains an invalid letter.");
		}
	}

	/**
	 * Berechne den Rest einer Division.
	 * <p>
	 * Der Ziffernstring ist möglicherweise zu lang, um in ein int o.ä. zu
	 * passen. Deshalb wird die Division häppchenweise mit jeweils sieben
	 * Zeichen durchgeführt.
	 *
	 * @return Rest der Division.
	 *
	 * @throws NumberFormatException
	 * 		- weil nicht nur Ziffern im String vorhanden sind.
	 */
	private static BigInteger pruefziffernDivision(final String ziffern) throws NumberFormatException {
		BigInteger dividend = new BigInteger(ziffern);
		return dividend.mod(PRUEFZIFFERN_DIVISOR);
	}

	/**
	 * Convert display value to internal value. Check that the blanks occur after every 4th character and remove the blanks.
	 *
	 * @return internal value or <code>null</code>, if the display value is invalid.
	 */
	public static String convertDisplay2Internal(String displayValue) {
		if (displayValue.indexOf(' ') != -1) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < displayValue.length(); i++) {
				// Blanks must be at 4, 9, 14, ...
				if ((i + 1) % 5 == 0) {
					if (displayValue.charAt(i) != ' ') {
						return null;
					}
				} else {
					if (displayValue.charAt(i) == ' ') {
						return null;
					}
					sb.append(displayValue.charAt(i));
				}
			}
			return sb.toString();
		} else {
			return displayValue;
		}
	}

	/**
	 * Convert internal value to display value. Insert a blank after every 4 characters.
	 *
	 * @return display value
	 */
	public static String convertInternal2Display(String xmlValue) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < xmlValue.length(); i++) {
			// Insert blanks at 4, 8, 12, ...
			if (i > 0 && i % 4 == 0) {
				sb.append(' ');
			}
			sb.append(xmlValue.charAt(i));
		}
		return sb.toString();
	}
}
