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

package com.mgmtp.a12.kernel.core.utils.internal.services.serviceparams;

import java.util.Set;

public class FormalErrorCodesProvider {

	private FormalErrorCodesProvider() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Provides the formal error codes that can occur during formal validation.
	 * Duplicates error codes defined in 'IFormaleFehlerConstants'.
	 *
	 * @return a set of valid formal error codes in lower case
	 */
	public static Set<String> getFormalErrorCodes() {
		return Set.of(
				"berechnungswertfehler",
				"datumbereichdatumfalsch",
				"datumbereichformatfalsch",
				"datumbereichnichtgueltig",
				"datumbereichtrennerfehlt",
				"datumfalsch",
				"datumformatfalsch",
				"falscheanzahlindices",
				"feldjafalsch",
				"feldjaneinfalsch",
				"feldleer",
				"feldunbekannt",
				"formalepruefung",
				"fuehrendesblank",
				"keinusbangegeben",
				"mandatoryfield",
				"mandatorygroup",
				"nachfolgendesblank",
				"pflichtfeldnichtvorhanden",
				"pflichtfeldpruefung",
				"solitaryindex",
				"stelligkeitzulang",
				"stringfalschesmuster",
				"stringmitnewline",
				"stringzukurz",
				"stringzulang",
				"testidnummer",
				"uniqueindex",
				"uniquevalue",
				"usbangegebenbeimaxusbnull",
				"wertnurleerzeichen",
				"wertzulang",
				"zahlfalscheanzahlnachkommastellen",
				"zahlhatdezimaltrenner",
				"zahlhatfuehrendenullen",
				"zahlhatungueltigezeichen",
				"zahlistnull",
				"zahlmitplus",
				"zahlmitvorzeichen",
				"zahlnullenthaeltvorzeichen",
				"zahlohnedezimaltrenner",
				"zahlzugross",
				"zahlzuklein",
				"zahlzukurz",
				"zahlzulang",
				"zahlzuvielevorkommastellen",
				"zeichennichtimzeichensatz",
				"zugrossekontextnummer",
				"zugrosselfdnummer",
				"zugrosserusb",
				"zugrossezeile",
				"zukleinekontextnummer",
				"zukleinelfdnummer",
				"zukleinerusb",
				"zukleinezeile"
		);
	}

}
