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

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class FADATJsonDeserializer {

	private FADATJsonDeserializer() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Die FADAT-Informationen werden aus einem JSON deserialisiert.
	 * Es werden die Mengen von BUFA-Nummern und von EWAz-Testfinanzämtern erstellt und
	 * zurückgegeben.
	 * Dabei werden die Daten entsprechend des Booleans 'testFinanzaemterAllowed' gefiltert
	 * (Angabe, ob TestFinanzämter mit aufgenommen werden oder nicht).
	 * Die Menge der EWAz-Testfinanzämter ist nur dann nicht leer, falls 'testFinanzaemterAllowed'
	 * gesetzt ist, und enthält dann alle Testfinanzämter der FADAT-Informationen.
	 *
	 * @param fadatInfo
	 *        {@link Reader} to the JSON-File containing the FADAT information
	 * @param testFinanzaemterAllowed
	 * 		falls <code>true</code> werden Test-Finanzämter der Menge der BUFA-Nummern und der
	 * 		Menge der EWAz-Testfinanzämter hinzugefügt, bei <code>false</code> nicht
	 * @throws IOException
	 */
	public static IFadat deserialize(Reader fadatInfo, boolean testFinanzaemterAllowed) throws IOException {
		Set<Integer> bufaNummern = new HashSet<>();
		Set<Integer> ewazTestBufaNummern = new HashSet<>();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode fadat = objectMapper.readTree(fadatInfo);
		for (JsonNode bufaObject : fadat.path("bufaNrs")) {
			boolean isTestTaxOffice = bufaObject.has("isTestTaxOffice") && bufaObject.get("isTestTaxOffice").asBoolean();
			if (!isTestTaxOffice || testFinanzaemterAllowed) {
				JsonNode bufaNrObject = bufaObject.get("bufaNr");
				if (bufaNrObject == null) {
					throw new IllegalArgumentException(
							String.format("Das durch \"%s\" definierte Objekt muss das Feld \"bufaNr\" enthalten.",
									bufaObject.toString()));
				}
				bufaNummern.add(bufaNrObject.asInt());
				if (isTestTaxOffice) {
					ewazTestBufaNummern.add(bufaNrObject.asInt());
				}
			}
		}
		return new FADATData(bufaNummern, ewazTestBufaNummern);
	}

}
