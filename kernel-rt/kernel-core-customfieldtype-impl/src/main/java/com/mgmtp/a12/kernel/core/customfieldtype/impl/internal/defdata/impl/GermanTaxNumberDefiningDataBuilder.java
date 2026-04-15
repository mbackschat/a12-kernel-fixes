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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberCheckDigitDefinition;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberFormatting;

class GermanTaxNumberDefiningDataBuilder {

	// Key ist jeweils die Bundeslandnummer
	private final Map<Integer, String> federalStateCodeMap;
	private final Map<Integer, IGermanTaxNumberFormatting> formattingMap;
	private final Map<Integer, Set<String>> negativePatternMap;
	// Key ist die VerfahrenId
	private final Map<String, IGermanTaxNumberCheckDigitDefinition> methodMap;
	// Key ist zusammengesetzt aus Bundeslandnummer, Bundesfinanzamtsnummer und
	// Bezirksnummer. Wert ist die VerfahrenId.
	private final Map<String, String> methodMappingMap;

	GermanTaxNumberDefiningDataBuilder() {
		this.federalStateCodeMap = new HashMap<>();
		this.formattingMap = new HashMap<>();
		this.negativePatternMap = new HashMap<>();
		this.methodMap = new HashMap<>();
		this.methodMappingMap = new HashMap<>();
	}

	void addFormatting(int bundeslandnr, IGermanTaxNumberFormatting formatierung) {
		formattingMap.put(bundeslandnr, formatierung);
	}

	void addMethodDefinition(String id, String name, int[] faktoren, int[] summanden) {
		if (!methodMap.containsKey(id)) {
			GermanTaxNumberCheckDigitDefinition methodDef = new GermanTaxNumberCheckDigitDefinition(name, faktoren, summanden);
			methodMap.put(id, methodDef);
		} else {
			// Integrationsproblem
			throw new IllegalArgumentException("Das  Prüfziffernverfahren  mit der Id \"" +
					id + "\" existiert bereits..");
		}
	}

	void addMethodMapping(Integer bundeslandnr, Integer bufanr, String bezirkVon, String bezirkBis, String verfahrenId) {
		if (!methodMap.containsKey(verfahrenId)) {
			throw new IllegalArgumentException("Es existiert kein Verfahren mit der Id \"" + verfahrenId + "\".");
		}
		Iterator<String> keys = getAllKeysMethodMapping(bundeslandnr, bufanr, bezirkVon, bezirkBis).iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			if (methodMappingMap.containsKey(key)) {
				// Integrationsproblem
				throw new IllegalArgumentException("Dem Schluessel \"" + key + "\" wurde schon ein Verfahren zugeordnet.");
			}
			methodMappingMap.put(key, verfahrenId);
		}
	}

	private Set<String> getAllKeysMethodMapping(Integer bundeslandnr, Integer bufanr, String bezirkVon, String bezirkBis) {
		final Set<String> keys = new HashSet<>();
		if (isBlankString(bezirkVon) && isBlankString(bezirkBis)) {
			keys.add(GermanTaxNumberDefiningData.getKeyMethodMapping(bundeslandnr, bufanr, null));
			return keys;
		}
		final String exceptionMessageIntro = "Bei der Zuordnung des Prüfziffernverfahrens zur Bundesfinanzamtsnummer \"" + bufanr;
		if (!isBlankString(bezirkVon) && !isBlankString(bezirkBis)) {
			int iBezirkVon;
			try {
				iBezirkVon = Integer.parseInt(bezirkVon);
			} catch (NumberFormatException e) {
				// Integrationsproblem
				throw new IllegalArgumentException(exceptionMessageIntro + "\" ist die untere Grenze der Bezirke keine Zahl.", e);
			}
			int iBezirkBis;
			try {
				iBezirkBis = Integer.parseInt(bezirkBis);
			} catch (NumberFormatException e) {
				// Integrationsproblem
				throw new IllegalArgumentException(exceptionMessageIntro + "\" ist die obere Grenze der Bezirke keine Zahl.", e);
			}
			if (iBezirkVon > iBezirkBis) {
				// Integrationsproblem
				throw new IllegalArgumentException(
						exceptionMessageIntro + "\" ist die untere Grenze der Bezirke groesser als die obere Grenze.");
			}
			int bezirk = iBezirkVon;
			for (int i = iBezirkVon; i <= iBezirkBis; i++) {
				keys.add(GermanTaxNumberDefiningData.getKeyMethodMapping(bundeslandnr, bufanr, bezirk));
				bezirk++;
			}
			return keys;
		}
		// Integrationsproblem
		throw new IllegalArgumentException(
				exceptionMessageIntro + "\" wurde nur die untere oder die obere Grenze der Bezirke gesetzt. Es muessen " +
						"entweder beide Werte oder keiner gesetzt werden.");
	}

	void addFederalStateCode(Integer bundeslandnummer, String laenderkuerzel) {
		federalStateCodeMap.put(bundeslandnummer, laenderkuerzel);
	}

	void addNegativePattern(Integer bundeslandnummer, String negativMuster) {
		if (negativePatternMap.containsKey(bundeslandnummer)) {
			negativePatternMap.get(bundeslandnummer).add(negativMuster);
		} else {
			Set<String> set = new HashSet<>();
			set.add(negativMuster);
			negativePatternMap.put(bundeslandnummer, set);
		}
	}

	private boolean isBlankString(String string) {
		return string == null || string.trim().isEmpty();
	}

	Map<Integer, String> getFederalStateCodeMap() {
		return federalStateCodeMap;
	}

	Map<Integer, IGermanTaxNumberFormatting> getFormattingMap() {
		return formattingMap;
	}

	Map<Integer, Set<String>> getNegativePatternMap() {
		return negativePatternMap;
	}

	Map<String, IGermanTaxNumberCheckDigitDefinition> getMethodMap() {
		return methodMap;
	}

	Map<String, String> getMethodMappingMap() {
		return methodMappingMap;
	}

}
