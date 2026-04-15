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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StnrVerifierDeserializer {

	private static final String STNR_VERIFIER_NAME = "internal/StNrVerifier.json";

	private StnrVerifierDeserializer() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Deserialisierung der StnrVerifier-Daten aus der Datei 'internal/StNrVerifier.json'.
	 * Die Datei 'internal/StNrVerifier.json' wird mit demselben Projekt ausgeliefert und kann sich somit zur Laufzeit der Anwendung
	 * nicht ändern. Es genügt deshalb diese Funktion einmal aufzurufen.
	 *
	 * @throws UncheckedIOException
	 */
	public static GermanTaxNumberDefiningDataBuilder deserialize() {
		GermanTaxNumberDefiningDataBuilder dataBuilder = new GermanTaxNumberDefiningDataBuilder();
		try (Reader reader = getReaderToGermanTaxNumberDefiningData()) {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode parser = objectMapper.readTree(reader);
			traverseMethods(parser.path("verfahren"), dataBuilder);
			traverseBundeslaender(parser.path("bundeslaender"), dataBuilder);
		} catch (IOException e) {
			throw new UncheckedIOException(String.format("Failed to read from '%s'.", STNR_VERIFIER_NAME), e);
		}
		return dataBuilder;
	}

	/**
	 * @return {@link Reader} for the file "StNrVerifier.json". <br><b>Note:</b> The invoker is responsible to close this {@link Reader}.
	 */
	public static Reader getReaderToGermanTaxNumberDefiningData() {
		@SuppressWarnings("resource") // we close at usage
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(STNR_VERIFIER_NAME); // callers are responsible for closing the stream by closing the reader (?)
		InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
		return new BufferedReader(streamReader);
	}

	private static void traverseMethods(JsonNode methods, GermanTaxNumberDefiningDataBuilder container) {
		for (JsonNode method : methods) {
			String id = deriveRequiredFieldValue(method, "id");
			String name = deriveRequiredFieldValue(method, "name");
			int[] arrFaktoren = traverseStellen(method.path("faktoren"));
			int[] arrSummanden = method.has("summanden") ? traverseStellen(method.path("summanden")) : null;
			container.addMethodDefinition(id, name, arrFaktoren, arrSummanden);
		}
	}

	private static int[] traverseStellen(JsonNode node) {
		int[] arr = new int[12];
		arr[0] = deriveRequiredFieldValueAsInt(node, "stelle1");
		arr[1] = deriveRequiredFieldValueAsInt(node, "stelle2");
		arr[2] = deriveRequiredFieldValueAsInt(node, "stelle3");
		arr[3] = deriveRequiredFieldValueAsInt(node, "stelle4");
		arr[4] = deriveRequiredFieldValueAsInt(node, "stelle5");
		arr[5] = deriveRequiredFieldValueAsInt(node, "stelle6");
		arr[6] = deriveRequiredFieldValueAsInt(node, "stelle7");
		arr[7] = deriveRequiredFieldValueAsInt(node, "stelle8");
		arr[8] = deriveRequiredFieldValueAsInt(node, "stelle9");
		arr[9] = deriveRequiredFieldValueAsInt(node, "stelle10");
		arr[10] = deriveRequiredFieldValueAsInt(node, "stelle11");
		arr[11] = deriveRequiredFieldValueAsInt(node, "stelle12");
		return arr;
	}

	/**
	 * Diese Methode befüllt eine Liste mit den Nummern aller Bundeslaender. Diese Liste wird zu
	 * Validierungen verwendet.
	 *
	 * @param doc
	 * @param container
	 */
	private static void traverseBundeslaender(JsonNode bundeslaender, GermanTaxNumberDefiningDataBuilder container) {
		for (JsonNode bundesland : bundeslaender) {
			String kuerzel = bundesland.path("kuerzel").asText();

			// 1. Nummern des aktuellen Bundeslands
			Set<Integer> myBundeslandNrn = getBundeslandnummern(container, bundesland.path("nr"), kuerzel);

			// 2. Verfahren zuordnen
			assignMethod(container, bundesland.path("verfahren"), myBundeslandNrn);

			// 3. Format
			traverseFormatting(container, bundesland.path("format"), myBundeslandNrn);

			// 4. Negativmuster
			traverseNegativePattern(container, bundesland.path("negativmuster"), myBundeslandNrn);
		}
	}

	private static Set<Integer> getBundeslandnummern(GermanTaxNumberDefiningDataBuilder container, JsonNode bundeslandNummern,
			String kuerzel) {
		Set<Integer> innerBundeslandNrn = new HashSet<>();
		for (JsonNode nummer : bundeslandNummern) {
			Integer bundeslandNummer = nummer.asInt();
			innerBundeslandNrn.add(bundeslandNummer);
			container.addFederalStateCode(bundeslandNummer, kuerzel);
		}
		return innerBundeslandNrn;
	}

	private static void assignMethod(GermanTaxNumberDefiningDataBuilder container, JsonNode methods, Set<Integer> myBundeslandNrn) {
		for (JsonNode method : methods) {
			String verfahrenId = deriveRequiredFieldValue(method, "verfahrenId");
			Integer bufanr = deriveIntOrNull(method, "bufanr");
			String bezirkVon = method.path("bezirkVon").asText(null);
			String bezirkBis = method.path("bezirkBis").asText(null);

			Iterator<Integer> it = myBundeslandNrn.iterator();
			while (it.hasNext()) {
				container.addMethodMapping(it.next(), bufanr, bezirkVon, bezirkBis, verfahrenId);
			}
		}
	}

	private static void traverseFormatting(GermanTaxNumberDefiningDataBuilder container, JsonNode format,
			Set<Integer> myBundeslandNrn) {
		Integer stFaNr = deriveRequiredFieldValueAsInt(format, "stellenFinanzamtsnr");
		Integer stBezirkNr = deriveRequiredFieldValueAsInt(format, "stellenBezirksnr");
		Integer stUNr = deriveRequiredFieldValueAsInt(format, "stellenUnterscheidungsnr");
		String prefix = format.path("prefix").asText(null);
		String trennz1 = format.path("trennzeichen1").asText(null);
		String trennz2 = format.path("trennzeichen2").asText(null);
		String trennz3 = format.path("trennzeichen3").asText(null);
		GermanTaxNumberFormatting formatting = new GermanTaxNumberFormatting(stFaNr, stBezirkNr, stUNr, prefix, trennz1, trennz2,
				trennz3);
		Iterator<Integer> it = myBundeslandNrn.iterator();
		while (it.hasNext()) {
			container.addFormatting(it.next(), formatting);
		}
	}

	private static void traverseNegativePattern(GermanTaxNumberDefiningDataBuilder container, JsonNode negativePattern,
			Set<Integer> myBundeslandNrn) {
		for (JsonNode pattern : negativePattern) {
			String wert = deriveRequiredFieldValue(pattern, "wert");
			Iterator<Integer> it = myBundeslandNrn.iterator();
			while (it.hasNext()) {
				container.addNegativePattern(it.next(), wert);
			}
		}
	}

	private static Integer deriveIntOrNull(JsonNode node, String fieldName) {
		JsonNode number = node.get(fieldName);
		return number != null ? number.asInt() : null;
	}

	private static String deriveRequiredFieldValue(JsonNode node, String fieldName) {
		JsonNode text = node.get(fieldName);
		if (text != null) {
			return text.asText();
		}
		// Integrationsproblem
		throw new IllegalArgumentException(String.format("Das durch \"%s\" definierte Objekt muss das Feld \"%s\" enthalten.",
				node.toString(), fieldName));
	}

	private static Integer deriveRequiredFieldValueAsInt(JsonNode node, String fieldName) {
		JsonNode number = node.get(fieldName);
		if (number != null) {
			return number.asInt();
		} else {
			// Integrationsproblem
			throw new IllegalArgumentException(
					String.format("Der Wert des Feldes \"%s\" des in \"%s\" definierten Objekts muss vom Typ Integer sein.",
							fieldName, node.toString()));
		}
	}
}
