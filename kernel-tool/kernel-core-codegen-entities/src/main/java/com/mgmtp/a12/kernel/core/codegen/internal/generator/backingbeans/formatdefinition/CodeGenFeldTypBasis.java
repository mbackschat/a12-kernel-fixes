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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

/**
 * Abstrakte Oberklasse zur Codegenerierung für alle Formatdefinitionen
 */
@StringTemplateGroup("formatdefinition")
public abstract class CodeGenFeldTypBasis extends STParameterClassBase {

	private String gemeinsamesFormatNumber;

	/**
	 * Muss von abgeleiteten Klassen entsprend überschrieben werden, sofern der jeweilige
	 * Feldtyp zusätzlich Variablen im Template definieren muss.
	 *
	 * @return Zeichenkette, die die zusätzliche Variablen im Template definiert
	 */
	public String getContentVariables() {
		return "";
	}

	/**
	 * Helper-Funktion, wird zur Zusammensetzung des Hashcodes aus mehreren Teilen verwendet.
	 *
	 * @param hashCode
	 * @param l
	 * @return hashCode zusammengesetzt aus dem übergebenen Hashcode und dem long-Wert
	 */
	public int hashCodeHelperAdd(int hashCode, long l) {
		return (hashCode + (int) (l ^ (l >>> 32))) * 37;
	}

	public void setGemeinsamesFormatNumber(String number) {
		gemeinsamesFormatNumber = number;
	}

	public String getGemeinsamesFormatNumber() {
		return gemeinsamesFormatNumber;
	}

	public abstract String getClassnameSuffix();

	protected Map<Locale, String> computeSpracheToFehlertext(CodeGenEntityData data, Function<Locale, String> getErrorMessage) {
		Map<Locale, String> spracheToFehlertextTmp = new HashMap<>();
		for (Locale l : data.getParams().getLanguages()) {
			String message = getErrorMessage.apply(l);
			if (message != null && message.length() > 0) {
				spracheToFehlertextTmp.put(l, message);
			}
		}
		return Collections.unmodifiableMap(spracheToFehlertextTmp);
	}

	protected String getFehlerTextForJsReducedCode(Map<Locale, String> spracheToFehlertext, Locale aktuelleSprache,
			UnaryOperator<String> replacementFunc) {
		String fehlertext = spracheToFehlertext.get(aktuelleSprache);

		if (fehlertext == null) {
			return new StringParameter("").toCode();
		}

		fehlertext = replacementFunc.apply(fehlertext);

		return new StringParameter(fehlertext).toCode();
	}
}
