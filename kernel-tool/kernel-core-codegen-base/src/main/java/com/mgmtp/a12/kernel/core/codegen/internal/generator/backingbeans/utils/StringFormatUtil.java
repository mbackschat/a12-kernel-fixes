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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 *
 */
public class StringFormatUtil {

	private StringFormatUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Each element of the list is converted to a string which can be used in the generated code,
	 * the converted string is surrounded with quotation marks and finally all resulted strings are
	 * joined with commas to a single string. Don't use this method for one string, for that purpose
	 * use StringParameter(...).toCode()
	 *
	 * @param list
	 * 		list of the strings
	 * @return a string which can be used in the generated code
	 */
	public static String makeCodeString(List<String> list) {
		return list.stream()
				.map(item -> new StringParameter(item).toCode())
				.collect(Collectors.joining(", "));
	}

	/**
	 * Converts the given string to a string which can be used in the generated code.
	 * The converted string is not surrounded with quotation marks.
	 */
	public static String makeCodeString(String s, ProgrammingLanguage lang) {
		if (s == null) {
			return s;
		}

		if (lang != ProgrammingLanguage.VBA) {
			s = s.replace("\\", "\\\\").replaceAll("\\t", "\\\\t").replaceAll("\\r", "\\\\r").replaceAll("\\n", "\\\\n")
					.replace("\"", "\\\"");
		} else {
			// keine Backslashersetztung
			// andere ", \t, \n, \r-Ersetzung
			s = s.replace("\"", "\"\"").replaceAll("\\t", "\" & vbTab & \"").replaceAll("\\r\\n", "\" & vbCrLf & \"")
					.replaceAll("\\n", "\" & vbLf & \"").replaceAll("\\r", "\" & vbCr & \"");
		}
		// dollar sign is a special character in groovy
		if (lang == ProgrammingLanguage.GROOVY) {
			s = s.replace("$", "\\$");
		}

		return s;
	}

	public static String generateBlockComment(final List<String> lines, final ProgrammingLanguage lang) {
		final StringBuilder result = new StringBuilder();
		if (ProgrammingLanguage.VBA == lang) {
			lines.forEach(l -> result.append("' ").append(l).append("\n"));
		} else {
			result.append("/**\n");
			lines.forEach(l -> result.append(" * ").append(l).append("\n"));
			result.append("**/");
		}
		return result.toString();
	}
}
