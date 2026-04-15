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

package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api;

public interface IGermanTaxNumberCheckDigitDefinition {

	/**
	 * Liefert die Bezeichnung des Prüfziffernverfahrens.
	 * Die folgenden Bezeichnungen sind als Rückgabewerte möglich:
	 * <ul>
	 * <li>2er-Verfahren</li>
	 * <li>11er-Verfahren</li>
	 * <li>modifiziertes 11er-Verfahren</li>
	 * <li>11er-Verfahren-NRW</li>
	 * </ul>
	 *
	 * @return Bezeichnung des Prüfziffernverfahrens
	 */
	public String getDescription();

	/**
	 * Gibt den Array der Faktoren zurück, die in diesem Prüfziffernverfahren jeweils mit den
	 * einzelnen Stellen der Steuernummer multipliziert werden.
	 * <p>
	 * Der Array enthält 12 Werte. Der erste Wert wird im Prüfziffernverfahren mit der ersten Stelle
	 * der Steuernummer multipliziert, der zweite Wert wird mit der zweiten Stelle der Steuernummer
	 * multipliziert usw.
	 * </p>
	 * <p>
	 * Die 13. Stelle der Steuernummer ist die Prüfziffer. Mit der Prüfziffer wird das
	 * Berechnungsergebnis des Prüfziffernverfahrens verglichen, sie kommt ansonsten im Algorithmus
	 * nicht vor. Aus diesem Grund enthält der Array nur 12 Werte.
	 * </p>
	 *
	 * @return Array der Faktoren (stets ungleich null, weil jedes Prüfziffernverfahren Faktoren
	 * 		braucht)
	 */
	public int[] getFactors();

	/**
	 * Gibt den Array der Summanden zurück, die in diesem Prüfziffernverfahren jeweils zu den
	 * einzelnen Stellen der Steuernummer addiert werden.
	 * <p>
	 * Der Array enthält 12 Werte. Der erste Wert wird im Prüfziffernverfahren zur ersten Stelle der
	 * Steuernummer hinzuaddiert, der zweite Wert wird zur zweiten Stelle der Steuernummer
	 * hinzuaddiert usw.
	 * </p>
	 * <p>
	 * Die 13. Stelle der Steuernummer ist die Prüfziffer. Mit der Prüfziffer wird das
	 * Berechnungsergebnis des Prüfziffernverfahrens verglichen, sie kommt ansonsten im Algorithmus
	 * nicht vor. Aus diesem Grund enthält der Array nur 12 Werte.
	 * </p>
	 *
	 * @return Array der Summanden bzw. null, falls in diesem Prüfziffernverfahren keine Summanden
	 * 		verwendet werden.
	 */
	public int[] getSummands();
}
