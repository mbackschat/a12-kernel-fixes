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

package com.mgmtp.a12.kernel.core.codegen.internal.utils;

public class VBALineNormalizer {

	private static final String LINE_SEPERATOR = System.getProperty("line.separator");
	private static final String NEWLINE = "\n";
	private static final int MAX_VBA_ZEILENLAENGE = 1000;
	private static final char[] BREAKABLE_CHARS = new char[] { ')', ',' };

	private VBALineNormalizer() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * In VBA dürfen Zeilen nicht länger als 1024 Zeichen sein. Sind sie es dennoch, so trennt der
	 * Import die Zeilen selbstständig. Die VBA-Datei wird korrupt, falls die Trennung innerhalb
	 * eines Strings durchgeführt wurde.
	 * <p>
	 * Die Methode stellt sicher, dass die Zeilen korrekt umgebrochen werden.
	 *
	 * @param content
	 * 		zu pruefende Zeile
	 * @return fuer VBA normalisierten Zeilenlaengen
	 *
	 * @throws GeneratorException
	 * 		e
	 */
	public static String normalizeLineLength(String content) {
		content = VBALineNormalizer.normalizeLineSeperator(content);
		StringBuilder normalisiert = new StringBuilder();
		String[] split = content.split(NEWLINE);
		for (String string : split) {
			normalisiert.append(normalizeSingleLine(string));
		}
		return normalisiert.toString();
	}

	/**
	 * Ruft sich rekursiv auf und teilt eine Zeile in kleinere Zeilen auf.
	 * <p>
	 * Annahme: Eine Zeile wird nur dann so groß, wenn sie einen String enthält (z.B.
	 * Fehlermeldungstext).
	 * <p>
	 * Umgebrochen werden die Zeilen deshalb nur am Ende oder innerhalb eines Strings.
	 *
	 * @param zeile
	 * 		zu normalisierende Zeile
	 * @return getrennter String
	 *
	 * @throws GeneratorException
	 * 		e
	 */
	private static String normalizeSingleLine(String zeile) {
		// Abbruchbedingung: Länge des Inhaltes ist innerhalb erlaubtem Rahmen
		if (zeile.length() < MAX_VBA_ZEILENLAENGE) {
			return zeile + NEWLINE; // kein Unterstrich, da letzte Zeile
		}

		String subStr = zeile.substring(0, MAX_VBA_ZEILENLAENGE);
		if (subStr.indexOf('\"') < 0) {
			// No string in the line, so break on a closing parenthesis
			int maxTrennIndex = -1;
			for (char breakingChar : BREAKABLE_CHARS) {
				int trennIndex = subStr.lastIndexOf(breakingChar);
				if (trennIndex > maxTrennIndex) {
					maxTrennIndex = trennIndex;
				}
			}
			if (maxTrennIndex > 0) {
				return createNormalizedResult(zeile, "", " _", maxTrennIndex + 1);
			}
			throw new IllegalArgumentException(String.format(
					"Die Zeile '%s' kann nicht gesplittet werden und überschreitet die maximale Zeilelänge %s in VBA.",
					zeile, MAX_VBA_ZEILENLAENGE));
		}
		int trennIndex = getPositionClosingQuote(zeile);
		if (trennIndex <= MAX_VBA_ZEILENLAENGE) {
			// Standardfall: es wird am Ende eines Strings umgebrochen
			return createNormalizedResult(zeile, "", " _", trennIndex);
		} else {
			// wir haben einen sehr langen String
			// Wir wissen, dass wir innerhalb eines Strings sind. Die Position des öffnenden Anführungszeichens
			// ist kleiner als MAX_VBA_ZEILENLAENGE, die des schließenden ist 'trennIndex'.
			// In diesem Fall wird also innerhalb des Strings umgebrochen.
			return createNormalizedResult(zeile, "\"", " & _", MAX_VBA_ZEILENLAENGE);
		}
	}

	/**
	 * Teilt eine Zeile an der vorgegebenen Stelle 'trennIndex'. An das letzte Zeichen wird
	 * der 'stringTrenner' und der 'zeilenTrenner' angefügt. An die neue Zeile wird ebenfalls
	 * der 'StringTrenner angefügt. Dies entspricht zusätzlichen Anfuehrungszeichen.
	 * <p>
	 * Struktur:
	 * [ZEILE-Teil1] [stringTrenner] [zeilenTrenner] + \n
	 * [stringTrenner] [ZEILE-Teil2] ...
	 *
	 * @param zeile
	 * 		aufzuteilende Zeile
	 * @param stringTrenner
	 * 		Zeichen, dass bei der Trennung hinzugefügt werden soll
	 * @param zeilenTrenner
	 * 		Zeichen, dass als Zeilenkonkatinierung dient
	 * @param trennIndex
	 * 		Index, an dem getrennt werden soll
	 * @return aufgeteilte Teile
	 *
	 * @throws GeneratorException
	 * 		Exception
	 */
	private static String createNormalizedResult(String zeile, String stringTrenner, String zeilenTrenner, int trennIndex) {
		StringBuilder normalisiert = new StringBuilder();
		String teil1 = zeile.substring(0, trennIndex);
		normalisiert.append(teil1);
		normalisiert.append(stringTrenner);
		normalisiert.append(zeilenTrenner);
		normalisiert.append("\n");

		String teil2 = stringTrenner + zeile.substring(trennIndex);
		normalisiert.append(normalizeSingleLine(teil2));
		return normalisiert.toString();
	}

	/**
	 * Ist kein Anfuehrungszeichen enthalten, so wird eine Exception erzeugt.
	 * <p>
	 * Sonst wird die Position nach dem schließenden Anfuehrungszeichen zurückgegeben, das am
	 * nächsten an
	 * MAX_VBA_ZEILENLAENGE liegt.
	 *
	 * @param zeile
	 * 		, die geteilt werden soll
	 * @return Position des schließenden Anfuehrungszeichens
	 */
	private static int getPositionClosingQuote(final String zeile) {
		int trennIndex = 0;
		String iterationsZeile = zeile;
		while (trennIndex < MAX_VBA_ZEILENLAENGE) {
			int startIndex = findPositionQuote(iterationsZeile); // String Start
			if (trennIndex + startIndex > MAX_VBA_ZEILENLAENGE) {
				// der Startpunkt liegt hinter MAX_VBA_ZEILENLAENGE => fertig
				break;
			}
			String textNachLineBreak = iterationsZeile.substring(startIndex + 1, iterationsZeile.length());
			// Es müssen immer Paare von Anführungszeichen sein, deshalb kann 'endeIndex' nicht den Wert -1 haben.
			int endeIndex = findPositionQuote(textNachLineBreak); // String Ende
			int trennIndexCandidate = trennIndex + startIndex + 1 + endeIndex + 1; // +1 für öffnedes und schließendes Anführungszeichen
			if (trennIndexCandidate >= MAX_VBA_ZEILENLAENGE) {
				if (trennIndex == 0) {
					// Notwendig damit erkannt wird, dass innerhalb eines Strings umgebrochen werden muss.
					trennIndex = trennIndexCandidate;
				}
				break;
			}
			trennIndex = trennIndexCandidate;
			iterationsZeile = zeile.substring(trennIndex);
			if (iterationsZeile.length() < MAX_VBA_ZEILENLAENGE) {
				break;
			}
		}
		return trennIndex;
	}

	/**
	 * @param inhalt
	 * 		der aufgeteilt werden soll
	 * @return erstes alleiniges auftreten eines Anfuehrungszeichens
	 */
	private static int findPositionQuote(final String inhalt) {
		int posAnfuehrungszeichen = inhalt.indexOf('\"');
		if (posAnfuehrungszeichen == -1) {
			// falls kein String vorhanden ist kann nicht aufgeteilt werden
			throw new IllegalArgumentException(String.format(
					"Die Zeile '%s' kann nicht gesplittet werden und überschreitet die maximale Zeilelänge %s in VBA.",
					inhalt, MAX_VBA_ZEILENLAENGE));
		}
		// Das Zeichen nach dem Anführungszeichen wird geprüft. Falls es auch ein Anführungszeichen ist, ist es
		// ein escaptes Anführungszeichen (z.B. falls der Fehlertext ein Anführungszeichen enthält).
		// Falls es ein escaptes Anführungszeichen ist, zählt es nicht als Ende des Strings.
		if (inhalt.length() > (posAnfuehrungszeichen + 2) && inhalt.charAt(posAnfuehrungszeichen + 1) == '"') {
			String textNachEscaptemAnfuehrungszeichen = inhalt.substring(posAnfuehrungszeichen + 2, inhalt.length());
			return posAnfuehrungszeichen + 2 + findPositionQuote(textNachEscaptemAnfuehrungszeichen);
		}
		return posAnfuehrungszeichen;
	}

	/**
	 * Wird für die VBA-Generierung verwendet. Es muss IMMMER '\r\n' als LineSeperator verwendet
	 * werden
	 *
	 * @param inhalt
	 * 		inhalt der zu generierenden Datei
	 * @return normalized Inhalt
	 */
	private static String normalizeLineSeperator(String inhalt) {
		return inhalt.replaceAll(LINE_SEPERATOR, "\r\n");
	}

}
