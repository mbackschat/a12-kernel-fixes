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

import java.util.Arrays;

/**
 * Klasse kapselt die Überprüfung ob nur gültige Grapheme aus einem vorgegebenen Zeichensatz verwendet
 * wurden.
 *
 * <p>A12K-2608: Zeichensätze bestehen aus:</p>
 * <ul>
 *     <li>Einer Menge von einzelnen Unicode Code-Points (Grapheme der Länge 1)</li>
 *     <li>Einer Menge von kombinierten Zeichen (Code-Point-Sequenzen, (Grapheme der Länge > 1))</li>
 * </ul>
 * <p>
 * Nach Möglichkeit sollten kombinierte Zeichen genau ein Graphem beschreiben.
 * Jedes Zeichen sollte dazu beitragen neue Zeichenketten zu erlauben und eindeutig von anderen Zeichen unterscheidbar sein.
 * Somit gelten aktuell folgende Bedingungen an den Zeichensatz, damit die Zeichensatzprüfung funktioniert:
 * <ul>
 *     <li>Ein kombiniertes Zeichen im Zeichensatz darf nicht vollständig durch andere kürzere Zeichen darstellbar sein</li>
 *     <li>
 *         Ist ein kombiniertes Zeichen Z zerlegbar, sodass Z = p + s
 *         mit präfix Graphem(en) p und Suffix-Graphem s
 *         und alle Grapheme im p kommen im Zeichensatz vor,
 *         dann darf s kein vollständiger Präfix eines anderen kombinierten Zeichens im Zeichensatz sein.
 *     </li>
 * </ul>
 *
 * <p>Negativ-Beispiel zu Punkt 2</p>
 * <p>Zeichensatz: {[0x004B,0x035F,0x0068], [0x004B,0x035F], [0x0068, 0x0304]}</p>
 * <ul>
 *     <li>[0x004B,0x035F,0x0068] zerlegbar in p = [[0x004B,0x035F]] und s = [0x0068]</li>
 *     <li>Alle Grapheme in p kommen im Zeichensatz vor und s ist Präfix von Zeichen [0x0068, 0x0304]</li>
 *     <li>Damit ist mindestens folgende Code-Point-Sequenz mehrdeutig: [0x004B,0x035F,0x0068,0x0304]</li>
 * </ul>
 * Wie geht man damit um?
 * <ul>
 *     <li>Ansatz I: [0x004B,0x035F,0x0068] aus dem Zeichensatz entfernen und [0x0068] hinzufügen</li>
 *     <li>Ansatz II: [0x004B,0x035F] entfernen</li>
 * </ul>
 *
 * <p>
 * <strong>PLEASE NOTE:</strong> This class exists duplicated in kernel-core-runtime and kernel-core-base-utils.
 * Please keep the behavior of both LegalCharTester implementations consistent!
 * </p>
 */
public class LegalCharTester {

	private final int[][] legalCodepoints;

	private final TrieNode trieRoot;
	private final boolean deactivateLegalCharCheck;

	public LegalCharTester(final int[][] legalCodepoints,
			final TrieNode legalGraphemeTrie,
			final boolean deactivateLegalCharCheck) {
		this.legalCodepoints = legalCodepoints;
		this.trieRoot = legalGraphemeTrie;
		this.deactivateLegalCharCheck = deactivateLegalCharCheck;
	}

	/**
	 * Prüft, ob im übergebenen String nur Grapheme aus dem erlaubten
	 * Zeichensatz verwendet werden. Der erlaubte Zeichensatz wird aus
	 * dem MetaObjekt geholt, der Teil der generierten Sourcen ist.
	 * <p>
	 * Die Prüfung matcht greedy auf das längste gültige Graphem.
	 * Die damit einhergehenden Anforderungen an den Zeichensatz sind in der Klassendokumentation beschrieben.
	 * </p>
	 *
	 * @param value
	 * 		zu überprüfende Zeichenkette
	 * @return true, falls nur gültige Grapheme verwendet werden,
	 * 		false sonst.
	 */

	public boolean pruefeZeichensatz(final String value) {
		if (noCharsetDefined()) {
			return deactivateLegalCharCheck || value.chars()
					.mapToObj(i -> (char) i)
					.noneMatch(Character::isSurrogate);
		}
		final int length = value.length();
		int currentIndex = 0;
		while (currentIndex < length) {
			int c = currentIndex;
			TrieNode match = trieRoot;
			// If the Trie is populated, find the longest matching grapheme
			if (!trieRoot.children().isEmpty()) {
				// Note: If graphemes can get longer than 3: change this to remember the longest match if trieNode.terminal() == true gets encountered
				while (c < length) {
					final TrieNode nextMatch = match.findNext(value.codePointAt(c));
					// match until a non match occurs -> c is the end of the string, and we are done
					// or c is at the position of the longest prefix + 1 for the next iteration
					if (nextMatch != null) {
						match = nextMatch;
						c++;
					} else {
						break;
					}
				}
			}
			if (match.terminal()) {
				currentIndex = c; // found a combined grapheme - move forward
			} else if (rangeSearch(legalCodepoints, value.codePointAt(currentIndex))) {
				currentIndex++; // no combined grapheme - test for graphemes of length 1
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean noCharsetDefined() {
		return (legalCodepoints == null || legalCodepoints.length == 0 ||
				legalCodepoints[0] == null || legalCodepoints[0].length == 0) && (trieRoot.children().isEmpty());
	}

	/**
	 * Does an index i exist such that legalCodepointRanges[0][i] <= character <= legalCodepointRanges[1][i] ?
	 */
	private static boolean rangeSearch(final int[][] legalCodepointRanges, final int character) {
		final int index = Arrays.binarySearch(legalCodepointRanges[0], character);
		if (index >= 0) {
			return true;
		}
		final int insertionPoint = -(index + 1);
		return insertionPoint != 0 && legalCodepointRanges[1][insertionPoint - 1] >= character;
	}

}
