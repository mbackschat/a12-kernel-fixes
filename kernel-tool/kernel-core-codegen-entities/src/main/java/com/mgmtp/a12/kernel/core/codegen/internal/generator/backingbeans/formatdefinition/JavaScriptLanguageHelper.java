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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition;

import java.util.Locale;

import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.impl.ExtmpSettings;
import com.mgmtp.a12.kernel.core.parser.internal.util.DateFormat;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectLanguageSettings;

public class JavaScriptLanguageHelper {

	private JavaScriptLanguageHelper() {
		throw new IllegalStateException("Utility class");
	}

	public static String getJsDatumFormat(Locale language, String fieldFormat) {
		IProjectLanguageSettings relevantSettings = getLanguageSettings(language);
		if (DateFormat.isFormatWithTimeSpecification(DateFormat.of(fieldFormat))) {
			if (DateFormat.isFormatWithDateSpecification(DateFormat.of(fieldFormat))) {
				return relevantSettings.getDateTimeFormat();
			} else {
				return relevantSettings.getTimeFormat();
			}
		}
		return relevantSettings.getDateFormat();
	}

	public static boolean isJsDatumNullOptional(Locale language) {
		return getLanguageSettings(language).isDateNullOptional();
	}

	public static String getJsDatumbereichTrenner(Locale language) {
		return String.valueOf(getLanguageSettings(language).getDateRangeSeparator());
	}

	public static char getJsDezimalTrenner(Locale language) {
		return getLanguageSettings(language).getDecimalSeparator();
	}

	private static IProjectLanguageSettings getLanguageSettings(Locale language) {
		return new ExtmpSettings().getProjectLanguageSettings().get(language);
	}
}
