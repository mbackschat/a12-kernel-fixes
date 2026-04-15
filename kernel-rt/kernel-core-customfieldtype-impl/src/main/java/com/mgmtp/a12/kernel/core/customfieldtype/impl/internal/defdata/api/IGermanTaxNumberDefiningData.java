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

import java.util.Set;

public interface IGermanTaxNumberDefiningData {

	/**
	 * Liefert die bundeslandspezifischen Formatierung der Steuernummer in der
	 * Oberflächendarstellung.
	 *
	 * @param federalStateNumber
	 * 		Nummer des Bundeslands, zu dem Informationen
	 * 		zur Formatierung der Steuernummer geliefert werden sollen
	 * @return bundeslandspezifische Formatierung
	 *
	 * @throws IllegalArgumentException
	 * 		falls die bundeslandnummer unbekannt ist oder falls keine Formatierung dazu
	 * 		gefunden wurden.
	 */
	IGermanTaxNumberFormatting getFormatting(int federalStateNumber)
			throws IllegalArgumentException;

	/**
	 * Prüft, ob das Länderkürzel bekannt ist.
	 *
	 * @param federalStateCode
	 * 		Länderkürzel eines Bundeslands, dessen Existenz geprüft werden soll
	 * @return true, falls das Länderkürzel bekannt ist
	 */
	boolean existsFederalStateCode(String federalStateCode);

	/**
	 * Liefert die bundeslandspezifischen Muster (reguläre Ausdrücke), denen die Steuernummer nicht
	 * entsprechen darf.
	 *
	 * @param federalStateNumber
	 * 		Nummer des Bundeslands, zu dem die Negativmuster geliefert werden sollen
	 * @return bundeslandspezifische Negativ-Muster, oder null falls es zum
	 * 		Bundesland keine Negativ-Muster gibt.
	 *
	 * @throws IllegalArgumentException
	 * 		falls die bundeslandnummer unbekannt ist.
	 */
	Set<String> getNegativePattern(int federalStateNumber);

	/**
	 * Liefert die notwendigen Daten zur Durchführung des Prüfziffernverfahrens.
	 *
	 * @param federalStateNumber
	 * 		Nummer des Bundeslandes
	 * @param federalTaxOfficeNumber
	 * 		Nummer des Bundesfinanzamtes
	 * @param districtNumber
	 * 		Nummer des Bezirks
	 * @return Daten zur Durchführung des Prüfziffernverfahrens
	 *
	 * @throws IllegalArgumentException
	 * 		falls keine Daten gefunden wurden.
	 */
	IGermanTaxNumberCheckDigitDefinition getCheckDigitDefinition(
			int federalStateNumber, int federalTaxOfficeNumber,
			int districtNumber) throws IllegalArgumentException;
}
