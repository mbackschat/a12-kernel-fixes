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

import com.mgmtp.a12.kernel.core.parser.internal.util.DateFormat;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateType.PartlyKnownDateType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 *
 */
public class FormatUtil {

	private FormatUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static String datumFormat2Pattern(String format, PartlyKnownDateType teilbekanntArt, ProgrammingLanguage lang,
			boolean nullOptional) {
		String pattern;
		if (DateFormat.isFormatWithTimeSpecification(DateFormat.of(format))) {
			pattern = datumFormat2PatternWithTime(format);
		} else {
			pattern = datumFormat2PatternNoTime(format, teilbekanntArt, nullOptional);
		}
		if (lang == ProgrammingLanguage.JAVA_SCRIPT_REDUCED) {
			// für Java Skript müssen auch die / escaped werden, allerdings nur in Patterns
			pattern = pattern.replace("/", "\\/");
		}
		return pattern;
	}

	private static String datumFormat2PatternNoTime(String format, PartlyKnownDateType teilbekanntArt, boolean nullOptional) {
		String f = format.replace("TT", "dd").replace("tt", "dd").replace("JJ", "yy").replace("jj", "yy").replace("YY", "yy");

		if (PartlyKnownDateType.BEKANNT != teilbekanntArt) {
			if (nullOptional) {
				f = f.replace("dd", "([4-9]|0[0-9]?|[1-2][0-9]?|3([0-1])?)");
			} else {
				f = f.replace("dd", "([0-2][0-9]|3[0-1])");
			}
		} else {
			if (nullOptional) {
				f = f.replace("dd", "([4-9]|0[1-9]|[1-2][0-9]?|3([0-1])?)");
			} else {
				f = f.replace("dd", "(0[1-9]|[1-2][0-9]|3[0-1])");
			}
		}

		if (PartlyKnownDateType.MONAT_OPTIONAL == teilbekanntArt ||
				PartlyKnownDateType.JAHR_OPTIONAL == teilbekanntArt) {
			if (nullOptional) {
				f = f.replace("MM", "(10|11|12|0|1|2|3|4|5|6|7|8|9|00|01|02|03|04|05|06|07|08|09)");
			} else {
				f = f.replace("MM", "(10|11|12|00|01|02|03|04|05|06|07|08|09)");
			}
		} else {
			if (nullOptional) {
				f = f.replace("MM", "(10|11|12|1|2|3|4|5|6|7|8|9|01|02|03|04|05|06|07|08|09)");
			} else {
				f = f.replace("MM", "(10|11|12|01|02|03|04|05|06|07|08|09)");
			}
		}

		if (PartlyKnownDateType.JAHR_OPTIONAL == teilbekanntArt) {
			f = f.replace("y", "[0-9]");
		} else {
			f = f.replace("yyyy", "([0-9][0-9][0-9][1-9]|[0-9][0-9][1-9][0-9]|[0-9][1-9][0-9][0-9]|[1-9][0-9][0-9][0-9])");
		}

		f = f.replace(".", "\\.");

		return f;
	}

	public static String datumFormat2PatternWithTime(String format) {
		// See also ValidationDateParser#getDatePattern
		return format
				.replace("dd", "(0[1-9]|[1-2][0-9]|3[0-1])")
				.replace("d", "([4-9]|0[1-9]|[1-2][0-9]?|3([0-1])?)")
				.replace("MM", "(10|11|12|01|02|03|04|05|06|07|08|09)")
				.replace("M", "(10|11|12|1|2|3|4|5|6|7|8|9|01|02|03|04|05|06|07|08|09)")
				.replace("yyyy", "([0-9][0-9][0-9][1-9]|[0-9][0-9][1-9][0-9]|[0-9][1-9][0-9][0-9]|[1-9][0-9][0-9][0-9])")
				// Time field conversion
				.replace("HH", "([0-1][0-9]|2[0-3])")
				.replace("mm", "([0-5][0-9])")
				.replace("ss", "([0-5][0-9])")
				.replace("hh", "(0[0-9]|1[0-2])")
				// 12/24h conversion
				.replace("a", "(AM|PM)")
				// Date/time separator conversion
				.replaceAll("'([^']*)'", "$1");
	}

}
