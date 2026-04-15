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

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringFormatUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * String utility class to help normalizing strings for use as method names or similar in the
 * generated code.
 */
public class StringUtil {

	private static final String[][] feldNamenReplacements = {
			{ ".", "__pkt__" },
			{ ":", "__dpkt__" },
			{ "/", "__s__" },
			{ "-", "__b__" },
			{ "§", "__j__" }

	};

	public enum SpecialChars {
		UmlautA("Ä", "Ae"),
		Umlauta("ä", "ae"),
		UmlautU("Ü", "Ue"),
		Umlautu("ü", "ue"),
		UmlautO("Ö", "Oe"),
		Umlauto("ö", "oe"),
		ScharfesS("ß", "ss");

		private String alt;
		private String neu;

		private SpecialChars(String alt, String neu) {
			this.alt = alt;
			this.neu = neu;
		}

		public String replace(String s) {
			return s.replaceAll(alt, neu);
		}

	}

	/**
	 * Konvertiert einen String der potentiell Sonderzeichen enthält in einen String der als
	 * identifier zulaessig ist.
	 *
	 * @param input
	 * @return
	 *
	 * @attention Die Implementierung muss mit der C++ Implementierung übereinstimmen.
	 */
	public static String normalizeSpecialChars(String input) {
		if (input == null) {
			return null;
		}
		String result = input;
		for (SpecialChars z : SpecialChars.values()) {
			result = z.replace(result);
		}

		// alles was nicht brav ist wird auf _ gemapped
		// Punkte und Doppelpunkte werden in die Ersatzdarstellung gemapped,
		// damit beim Codegenerieren hier keine Probleme mit der Verwendung der
		// Namen in den Methoden entstehen.
		return result.replaceAll("[^A-Za-z_0-9.:]", "_");
	}

	/**
	 * This method should be only used in kernel-ext project.
	 * While code generation use StringParameter(...).toCode()
	 *
	 * @param str
	 * 		string to be adapted in order to be written in generated code
	 * @param lang
	 * 		corresponding programming language
	 * @return an escaped (according to the given language) string between quotation marks
	 */
	public static String toCode(String str, ProgrammingLanguage lang) {
		String escapedStr = StringFormatUtil.makeCodeString(str, lang);
		return String.format("\"%s\"", escapedStr != null ? escapedStr : "");
	}

	/**
	 * Konvertiert einen Namen in eine Ersatzdarstellung, die für die Codegenerierung verwendet
	 * werden kann, wenn der Name Teil eines Methodennamens ist.
	 *
	 * @return gibt den konvertierten Namen zurück
	 */
	public static String transformToMethodName(String name) {
		if (name == null) {
			return "";
		}
		for (SpecialChars z : SpecialChars.values()) {
			name = z.replace(name);
		}

		for (String[] replacement : feldNamenReplacements) {
			name = name.replace(replacement[0], replacement[1]);
		}
		return name;
	}

}
