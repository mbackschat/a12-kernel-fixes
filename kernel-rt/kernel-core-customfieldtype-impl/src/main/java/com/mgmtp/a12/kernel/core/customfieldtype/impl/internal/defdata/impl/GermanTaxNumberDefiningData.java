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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberCheckDigitDefinition;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberDefiningData;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberFormatting;

public class GermanTaxNumberDefiningData implements IGermanTaxNumberDefiningData {

	private static GermanTaxNumberDefiningData ME = new GermanTaxNumberDefiningData();

	public static GermanTaxNumberDefiningData getInstance() { return ME; }

	// Key ist jeweils die Bundeslandnummer
	private final Map<Integer, String> federalStateCodeMap;
	private final Map<Integer, IGermanTaxNumberFormatting> formattingMap;
	private final Map<Integer, Set<String>> negativePatternMap;
	// Key ist die VerfahrenId
	private final Map<String, IGermanTaxNumberCheckDigitDefinition> methodMap;
	// Key ist zusammengesetzt aus Bundeslandnummer, Bundesfinanzamtsnummer und
	// Bezirksnummer. Wert ist die VerfahrenId.
	private final Map<String, String> methodMappingMap;

	private GermanTaxNumberDefiningData() {
		GermanTaxNumberDefiningDataBuilder dataBuilder = StnrVerifierDeserializer.deserialize();
		this.federalStateCodeMap = Collections.unmodifiableMap(dataBuilder.getFederalStateCodeMap());
		this.formattingMap = Collections.unmodifiableMap(dataBuilder.getFormattingMap());
		this.negativePatternMap = Collections.unmodifiableMap(dataBuilder.getNegativePatternMap());
		this.methodMap = Collections.unmodifiableMap(dataBuilder.getMethodMap());
		this.methodMappingMap = Collections.unmodifiableMap(dataBuilder.getMethodMappingMap());
	}

	@Override
	public IGermanTaxNumberFormatting getFormatting(int bundeslandnummer) throws IllegalArgumentException {
		if (!existsBundeslandnummer(bundeslandnummer)) {
			throw new IllegalArgumentException("Die Bundeslandnummer \"" + bundeslandnummer + "\" ist nicht bekannt.");
		}
		IGermanTaxNumberFormatting formatting = formattingMap.get(bundeslandnummer);
		if (formatting == null) {
			throw new IllegalArgumentException("Zur Bundeslandnummer \"" + bundeslandnummer + "\" existiert keine Formatierung.");
		}
		return formatting;
	}

	@Override
	public boolean existsFederalStateCode(String laenderkuerzel) {
		return federalStateCodeMap.containsValue(laenderkuerzel);
	}

	private boolean existsBundeslandnummer(int bundeslandnummer) {
		return federalStateCodeMap.containsKey(bundeslandnummer);
	}

	@Override
	public Set<String> getNegativePattern(int bundeslandnummer) {
		if (!existsBundeslandnummer(bundeslandnummer)) {
			throw new IllegalArgumentException("Die Bundeslandnummer \"" + bundeslandnummer + "\" ist nicht bekannt.");
		}
		return negativePatternMap.get(bundeslandnummer);
	}

	@Override
	public IGermanTaxNumberCheckDigitDefinition getCheckDigitDefinition(int bundeslandnummer, int bundesfinanzamtsnummer,
			int bezirksnummer) throws IllegalArgumentException {
		// Prüfen, ob das Verfahren über den Bezirk - als Teil des Finanzamts - zugeordnet ist.
		String methodId = methodMappingMap.get(getKeyMethodMapping(bundeslandnummer, bundesfinanzamtsnummer,
				bezirksnummer)); // Zuordnung hängt an der Bezirksnummer
		methodId = methodId != null ?
				methodId :
				methodMappingMap.get(getKeyMethodMapping(bundeslandnummer, bundesfinanzamtsnummer,
						null)); // Zuordnung hängt an der bundesfinanzamtsnummer
		methodId = methodId != null ?
				methodId :
				methodMappingMap.get(getKeyMethodMapping(bundeslandnummer, null, null)); // Zuordnung hängt an der Bundeslandnummer
		if (methodId == null) {
			throw new IllegalArgumentException(
					"Den Daten Bundeslandnummer=\"" + bundeslandnummer + "\", bundesfinanzamtsnummer=\"" + bundesfinanzamtsnummer + "\" und Bezirksnummer=\"" +
							bezirksnummer + "\" wurde keine Verfahrensdefinition zugeordnet.");
		}
		IGermanTaxNumberCheckDigitDefinition retVal = methodMap.get(methodId);
		if (retVal == null) {
			throw new IllegalArgumentException(
					"Den Daten Bundeslandnummer=\"" + bundeslandnummer + "\", bundesfinanzamtsnummer=\"" + bundesfinanzamtsnummer + "\" und Bezirksnummer=\"" +
							bezirksnummer + "\" wurde das Verfahren mit der Id=\"" + methodId + "\" zugeordnet. Zum Verfahren mit dieser Id existieren aber keine Daten.");
		}
		return retVal;
	}

	protected static String getKeyMethodMapping(Integer bundeslandnr, Integer bufanr, Integer bezirknr) {
		String retval = "";
		retval = bundeslandnr != null && bundeslandnr != 0 ? retval + "#" + bundeslandnr : retval + "#";
		retval = bufanr != null && bufanr != 0 ? retval + "#" + bufanr : retval + "#";
		retval = bezirknr != null ? retval + "#" + bezirknr : retval + "#";
		return retval;
	}
}
