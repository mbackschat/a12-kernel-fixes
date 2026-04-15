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

import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberCheckDigitDefinition;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberDefiningData;

/**
 * Diese Klasse bietet Methoden um bestimmte Informationen am Interface IDefdatenSteuernummer
 * abzufragen. Die Methoden wurden in diese Klasse ausgelagert, um eine einheitliche
 * Fehlerbehandlung bei unerwarteter/fehlerhafter Befüllung des Interfaces IDefdatenSteuernummer zu
 * erreichen.
 */
public class DefdataStnrAccessHelper {

	private DefdataStnrAccessHelper() {
		throw new IllegalStateException("Utility class");
	}

	public static final int BEZIRKSNUMMER_BEGIN_IDX = 5;

	public static IGermanTaxNumberCheckDigitDefinition getPruefziffernverfahrenDefinition(
			final IGermanTaxNumberDefiningData defdaten,
			final int bundeslandnummer, final int bundesfinanzamtsnummer,
			final int bezirksnummer) {
		IGermanTaxNumberCheckDigitDefinition verfahrenDef = defdaten.getCheckDigitDefinition(bundeslandnummer,
				bundesfinanzamtsnummer, bezirksnummer);
		if (verfahrenDef == null) {
			throw new IllegalArgumentException(
					"In den defininierenden Daten ist kein Prüfziffernverfahren zu den Kriterien Bundeslandnummer=" +
							bundeslandnummer + " Bundesfinanzamtsnummer=" + bundesfinanzamtsnummer + " Bezirksnummer=" + bezirksnummer + " definiert.");
		}
		return verfahrenDef;
	}

	public static int getIdxBezirkEnd(final IGermanTaxNumberDefiningData defdaten,
			final int bundeslandnummer) {
		int stellenBezirksnummer = defdaten.getFormatting(bundeslandnummer).getNumPositionsDistrict();
		if (stellenBezirksnummer < 0 || (BEZIRKSNUMMER_BEGIN_IDX + stellenBezirksnummer) > 13) {
			// Integrationsproblem
			throw new IllegalArgumentException(String.format("Die defininierenden Daten liefern zur " +
							"Bundeslandnummer %s die unerwartete Stellenanzahl %s der Bezirksnummer . " +
							"Die Bezirksnummer kann deshalb nicht aus der Steuernummer extrahiert werden.",
					bundeslandnummer, stellenBezirksnummer));
		}
		return BEZIRKSNUMMER_BEGIN_IDX + stellenBezirksnummer;
	}

	public static int[] getSummanden(
			final IGermanTaxNumberCheckDigitDefinition verfahrenDef) {
		int[] summanden = verfahrenDef.getSummands();
		if (summanden == null) {
			// Integrationsproblem
			throw new IllegalArgumentException("Fehler in den defininierenden Daten. " +
					"Der Array mit den im Prüfziffernverfahren notwendigen Summanden ist null.");
		}
		if (summanden.length != 12) {
			// Integrationsproblem
			throw new IllegalArgumentException("Fehler in den defininierenden Daten. " +
					"Der Array mit den im Prüfziffernverfahren notwendigen Summanden hat nicht die erwartete Länge 12.");
		}
		return summanden;
	}

	public static int[] getFaktoren(
			final IGermanTaxNumberCheckDigitDefinition verfahrenDef) {
		int[] faktoren = verfahrenDef.getFactors();
		if (faktoren == null) {
			// Integrationsproblem
			throw new IllegalArgumentException("Fehler in den defininierenden Daten. " +
					"Der Array mit den im Prüfziffernverfahren notwendigen Faktoren ist null.");
		}
		if (faktoren.length != 12) {
			// Integrationsproblem
			throw new IllegalArgumentException("Fehler in den defininierenden Daten. " +
					"Der Array mit den im Prüfziffernverfahren notwendigen Faktoren hat nicht die erwartete Länge 12.");
		}
		return faktoren;
	}
}
